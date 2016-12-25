package me.scai.handwriting.ml;

import me.scai.handwriting.CHandWritingTokenImageData;
import me.scai.handwriting.CStroke;
import me.scai.handwriting.CWrittenToken;
import me.scai.handwriting.tokens.TokenFileSettings;
import me.scai.handwriting.tokens.TokenSettings;
import me.scai.parsetree.MathHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class MachineLearningHelper {



    public static DataSetWithStringLabels readDataFromDir(final String inDirName,
                                                          TokenSettings tokenSettings,
                                                          int newImageSize) {
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
            if ( allFiles[i].isDirectory() &&
                 allFiles[i].getName().indexOf(".") != 0 ) { // Skip hidden folders
                System.out.println("Reading data from subdirectory: " + allFiles[i].getPath());

                DataSetWithStringLabels dataSetWithStringLabels =
                        readDataFromDir(allFiles[i].getPath(), tokenSettings, newImageSize);
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

                float [] im_wh = new float[2]; // Width and height
                im_wh[0] = t_imData.w;
                im_wh[1] = t_imData.h;

                t_wt.width = t_imData.w;
                t_wt.height = t_imData.h;

                if (newImageSize <= 0) {
                    x = getSdveVector(t_wt, tokenSettings);
                } else {
                    x = getNewImagePlusSdveVector(t_wt, tokenSettings, newImageSize, t_imData.h, t_imData.w);
                }

                r.addSample(x, tokenSettings.getTokenDegeneracy().getDegenerated(t_imData.tokenName));

            } catch (Exception e) {
                System.err.println("ERROR: Failed to read valid data from file: " + files[i].getName() + 
                                   " due to exception " + e.toString());
            }

        }

        return r;
    }

    public static float[][] getNewImageData(CWrittenToken wt, int imgSize, float origImgH, float origImgW) {
        if (imgSize <= 0) {
            throw new IllegalArgumentException("Invalid image height and width");
        }

        if (origImgH == 0f && origImgW == 0f) {
            throw new IllegalStateException("Written token instance has zero values in both height and width");
        }

        float grid = 1.0f / imgSize;
        float hg = grid * 0.5f;  // half-grid size
        float hgd = hg * 1.414f;

        float lim_x = 1f;
        float lim_y = 1f;
        if (origImgH > origImgW) {
            // Shrink lim_x
            lim_x = origImgW / origImgH;
        } else {
            // Shrink lim_y
            lim_y = origImgH / origImgW;
        }

        float[][] img = new float[imgSize][imgSize];

        for (int n = 0; n < wt.nStrokes(); ++n) {
            CStroke stroke = wt.getStroke(n);

            boolean down = false;

            if (stroke.nPoints() <= 0) {
                throw new IllegalStateException("Empty stroke");
            } else {
                final int np = stroke.nPoints();
                final float[] xs = stroke.getXs();
                final float[] ys = stroke.getYs();

//                if (np == 2 && n == 1 && wt.getStroke(1).getXs()[0] == 0.977f) {
//                    int iii = 111; // DEBUG
//                }

                if (np == 1 || (np == 2 && xs[0] == xs[1] && ys[0] == ys[1])) {
                    // Single dot

                    int ix = Math.round(xs[0] * (imgSize - 1) * lim_x);
                    int iy = Math.round(ys[0] * (imgSize - 1) * lim_y);

                    img[iy][ix] = 1.0f;
                    down = true;

                } else {
                    for (int i = 0; i < imgSize; ++i) {
                        for (int j = 0; j < imgSize; ++j) {
                            float ctr_x = (i + 0.5f) / imgSize;
                            float ctr_y = (j + 0.5f) / imgSize;

                            for (int k = 0; k < np -1; ++k) {
                                float x0 = xs[k] * lim_x;
                                float y0 = ys[k] * lim_y;
                                float x1 = xs[k + 1] * lim_x;
                                float y1 = ys[k + 1] * lim_y;

                                if (x0 == x1 && y0 == y1) {
                                    /* The two points are the same */
                                    if ((x0 - ctr_x) * (x0 - ctr_x) + (y0 - ctr_y) * (y0 - ctr_y) <= hgd * hgd) {
                                        img[j][i] = 1f;
                                    }
                                } else {
                                    float d = dist(x0, y0, x1, y1, ctr_x, ctr_y);

                                    if (d <= hgd) {
                                        float v = (hgd - d) / hgd;
                                        img[j][i] = 1f - (1f - img[j][i]) * ((1f - v) / 2f);
                                        down = true;
                                    }
                                }
                            }
                        }
                    }

                }
            }

            if (!down) {
                System.out.println("Warning: no point for stroke");
            }

        }

        return img;
    }


    /**
     * Calculate the distance from point (xp, yp) to the line passing through points (x1, y1) and (x2, y2)
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param xp
     * @param yp
     * @return Distance
     */
    private static float dist(float x1, float y1, float x2, float y2, float xp, float yp) {
        float px = x2 - x1;
        float py = y2 - y1;

        float q = px * px + py * py;

        float u = ((xp - x1) * px + (yp - y1) * py) / q;

        if (u > 1f) {
            u = 1f;
        } else if (u < 0f) {
            u = 0f;
        }

        float x = x1 + u * px;
        float y = y1 + u * py;

        float dx = x - xp;
        float dy = y - yp;

        // Note: If the actual distance does not matter,
        // if you only want to compare what this function
        /// returns to other results of this function, you
        // can just return the squared distance instead
        // (i.e. remove the sqrt) to gain a little performance

        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float[] getSdveVector(CWrittenToken wt, TokenSettings tokenSettings) {
        assert(wt.width > 0 || wt.height > 0);

        float[] im_wh = new float[] {wt.width, wt.height};

        float[] sdv = wt.getSDV(tokenSettings.getNpPerStroke(), tokenSettings.getMaxNumStrokes(), im_wh);
        float[] sepv = wt.getSEPV(tokenSettings.getMaxNumStrokes()); // Stroke endpoint vector

        return addExtraDimsToSDV(sdv, sepv, wt.width, wt.height, wt.nStrokes(),
                tokenSettings.isIncludeTokenSize(),
                tokenSettings.isIncludeTokenWHRatio(),
                tokenSettings.isIncludeTokenNumStrokes());
    }

    public static float[] getNewImagePlusSdveVector(CWrittenToken wt, TokenSettings tokenSettings,
                                                    int imgSize, float origImgH, float origImgW) {
        float[] x_sdve = getSdveVector(wt, tokenSettings);
        float[][] x_img = getNewImageData(wt, imgSize, origImgH, origImgW);

        assert(x_img.length == imgSize);
        assert(x_img[0].length == imgSize);

        float[] x = new float[imgSize * imgSize + x_sdve.length];
        int cnt = 0;
        for (int i = 0; i < imgSize; ++i) {
            for (int j = 0; j < imgSize; ++j) {
                x[cnt++] = x_img[i][j];
            }
        }

        for (float v : x_sdve) {
            x[cnt++] = v;
        }

        return x;
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
