package me.scai.handwriting.ml;

import me.scai.handwriting.CHandWritingTokenImageData;
import me.scai.handwriting.CWrittenToken;
import me.scai.handwriting.tokens.TokenFileSettings;
import me.scai.handwriting.tokens.TokenSettings;
import me.scai.parsetree.MathHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class MachineLearningHelper {


    public static DataSetWithStringLabels readDataFromDir(final String inDirName, TokenSettings tokenSettings) {

        DataSetWithStringLabels r = new DataSetWithStringLabels();

        File inDir = new File(inDirName);

		/* Test the existence of the input directory */
        if ( !inDir.isDirectory() ) {
            System.err.println("Cannot find directory " + inDirName);
            System.exit(1);
        }

        File [] allFiles = inDir.listFiles();

		/* Recursively retrieve data from sub-directories */
        for (int i = 0; i < allFiles.length; ++i) {
            if ( allFiles[i].isDirectory() ) {
                System.out.println("Reading data from subdirectory: " + allFiles[i].getPath());

                DataSetWithStringLabels dataSetWithStringLabels = readDataFromDir(allFiles[i].getPath(), tokenSettings);
                r.addAll(dataSetWithStringLabels);
            }
        }

		/* Get the list of all .wt files */
        File [] files = inDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.startsWith(TokenFileSettings.WT_FILE_PREFIX) &&
                        name.endsWith(TokenFileSettings.WT_FILE_SUFFIX));
            }
        });


        for (int i = 0; i < files.length; ++i) {

            float[] x = null;
            try {
				/* Actual reading */
                CWrittenToken t_wt = new CWrittenToken(files[i]);

                String wtPath = files[i].getPath();
                String imPath = wtPath.substring(0, wtPath.length() - TokenFileSettings.WT_FILE_SUFFIX.length()) +
                        TokenFileSettings.IM_FILE_SUFFIX;
                File imFile = new File(imPath);
                CHandWritingTokenImageData t_imData =
                        CHandWritingTokenImageData.readImFile(imFile,
                                tokenSettings.isIncludeTokenSize(),
                                tokenSettings.isIncludeTokenWHRatio(),
                                tokenSettings.isIncludeTokenNumStrokes());

				/* Skip exclusion tokens */
                if ( tokenSettings.isTokenHardCoded(t_imData.tokenName) ) {
                    continue;
                }

                float [] im_wh = new float[2];
                im_wh[0] = t_imData.w;
                im_wh[1] = t_imData.h;

				/* TODO: Combine into a function */
                float [] sdv = t_wt.getSDV(tokenSettings.getNpPerStroke(), tokenSettings.getMaxNumStrokes(), im_wh);
                float [] sepv = t_wt.getSEPV(tokenSettings.getMaxNumStrokes()); // Stroke endpoint vector
                x = addExtraDimsToSDV(sdv, sepv, t_imData.w, t_imData.h, t_imData.nStrokes,
                                      tokenSettings.isIncludeTokenSize(),
                                      tokenSettings.isIncludeTokenWHRatio(),
                                      tokenSettings.isIncludeTokenNumStrokes());

                r.addSample(x, tokenSettings.getTokenDegeneracy().getDegenerated(t_imData.tokenName));

            } catch (Exception e) {
                System.err.println("WARNING: Failed to read valid data from file: " + files[i].getName());
            }

        }

        return r;
    }

    /**
     * Convert string-labeled dataset into index-labeled data set
     * @param in    Input string-labeled dataset
     * @return      Input index-labeled dataset
     */
    public static DataSet convertStringLabelsToIndices(DataSetWithStringLabels in) {
        Set<String> uniqueLabelsSet = new HashSet<>();

        uniqueLabelsSet.addAll(in.getY());

        List<String> uniqueLabels = new ArrayList<>();

        uniqueLabels.addAll(uniqueLabelsSet);

        List<float[]> inXs = in.getX();
        List<String> inYs = in.getY();

        DataSet out = new DataSet();

        Iterator<float[]> inXsIter = inXs.iterator();
        Iterator<String> inYsIter = inYs.iterator();

        while (inXsIter.hasNext()) {
            float[] x = inXsIter.next();
            String label = inYsIter.next();

            out.addSample(x, uniqueLabels.indexOf(label));
        }

        out.setLabelNames(uniqueLabels);

        return out;
    }



    /**
     * Divide a dataset into a number of subsets. The names and ratios of the subsets are specificed in ratiosMap
     * @param in         The input dataset
     * @param ratiosMap  A map from subset name to subset count ratio. The ratio values must some to 1.
     * @return           A map from subset name to sub-datasets
     */
    public static Map<String, DataSet> divideIntoSubsetsEvenlyAndRandomly(DataSet in, Map<String, Float> ratiosMap) {
        int nSubsets = ratiosMap.size(); // Number of subsets

        if (nSubsets == 0) {
            throw new IllegalArgumentException("ratiosMap is empty");
        }

        ArrayList<String> names = new ArrayList<>();
        ArrayList<Float> ratios = new ArrayList<>();
        names.ensureCapacity(nSubsets);
        ratios.ensureCapacity(nSubsets);

        // Verify that the ratiosMap sum up to one
        float sum = 0f;
        for (Map.Entry<String, Float> entry : ratiosMap.entrySet()) {
            names.add(entry.getKey());

            float ratio = entry.getValue();
            sum += ratio;

            ratios.add(ratio);
        }

        if (Math.abs(sum - 1f) > 1e-6f) {
            throw new IllegalArgumentException("ratios in ratiosMap do not sum up to 1");
        }

        // Prepare output structure
        Map<String, DataSet> out = new HashMap<>();

        for (String name : names) {
            out.put(name, new DataSet());
        }

        // Determine the indices to the different labels
        List<List<Integer>> labelIndices = new ArrayList<>();

        // Reserve space
        int nuy = in.numUniqueYs(); // Number of unique y values

        ((ArrayList) labelIndices).ensureCapacity(nuy);
        for (int i = 0; i < nuy; ++i) {
            labelIndices.add(new LinkedList<Integer>());
        }

        // Populate the member lists of labelIndices
        List<float[]> inXs = in.getX();
        List<Integer> inYs = in.getY();
        Iterator<Integer> inYsIter = inYs.iterator();

        int counter = 0;
        while(inYsIter.hasNext()) {
            int idx = inYsIter.next();

            assert(idx >= 0 && idx < nuy); //TODO: Get rid of the assumption that the label values are contiguous from 0 to nuy - 1

            labelIndices.get(idx).add(counter++);
        }

        // Iterate through the labels and assign the individual samples to proper subsets, randomly
        for (int i = 0; i < nuy; ++i) {
            List<Integer> indices = labelIndices.get(i);

            int n = indices.size();

            if (n == 0) {
                throw new IllegalStateException("Found a label without any data samples");
            }

            int[] bins = MathHelper.randomlyAssignToBins(n, ratios);

            assert(bins.length == n);

            for (int j = 0; j < n; ++j) {
                int bin = bins[j];
                int index = indices.get(j);

                out.get(names.get(bin)).addSample(inXs.get(index), inYs.get(index));
            }

        }

        return out;

    }

    /* The SEPV (Stroke end-points vector) will be appended to the end SDV,
	 * before other features are appended.
	 * */
    public static float[] addExtraDimsToSDV(float[] sdv, float[] sepv, float width, float height, int numStrokes,
                                             boolean includeTokenSize,
                                             boolean includeTokenWHRatio,
                                             boolean includeTokenNumStrokes) {
        float [] sdve = null;
        float [] extraDims = new float[4];
		/* The size needs to be expanded if more potential options
		 * are added in the future */

        int nExtraDims = 0;
//		nExtraDims += sepv.length; /* TODO: Make optional */
		/* Get the extra dimensions */
        if ( includeTokenSize ) {

            extraDims[nExtraDims++] = width;	/* Width */
            extraDims[nExtraDims++] = height;	/* Height */
        }

        if ( includeTokenWHRatio ) {
            if (width == 0.0) {
                extraDims[nExtraDims++] = 100.0f;
            } else {
                extraDims[nExtraDims++] = height / width;
            }
        }
        if ( includeTokenNumStrokes ) {
            extraDims[nExtraDims++] = numStrokes;
        }

		/* Include the extra dimensions */
        if ( nExtraDims == 0 ) {
            sdve = sdv;
        } else {
            sdve = new float[sdv.length + sepv.length + nExtraDims]; /* TODO: Make SEPV optional */

            System.arraycopy(sdv, 0, sdve, 0, sdv.length);
            System.arraycopy(sepv, 0, sdve, sdv.length, sepv.length);

            int i0 = sdv.length + sepv.length;
            for (int j = 0; j < nExtraDims; ++j) {
                sdve[j + i0] = extraDims[j];
            }
        }

        return sdve;
    }
}
