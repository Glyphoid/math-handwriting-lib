package me.scai.handwriting;

import java.io.*;
import java.util.*;

import me.scai.handwriting.ml.DataSet;
import me.scai.handwriting.ml.DataSetWithStringLabels;
import me.scai.handwriting.ml.MachineLearningHelper;
import me.scai.handwriting.tokens.TokenSettings;
import me.scai.handwriting.utils.DataIOHelper;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.ResetStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.Format;
import org.encog.util.simple.EncogUtility;

import com.google.gson.JsonObject;

import me.scai.parsetree.MathHelper;

public class TokenRecogEngineSDV extends TokenRecogEngine implements Serializable {
	private static final long serialVersionUID = 2L;
	
	/* Feature settings */
	private int npPerStroke = 16;
	private int maxNumStrokes = 4;	
	
	private float dotMaxWidth = 3;
	private float dotMaxHeight = 3;
	private int dotTokenIndex = -1;
	
	private String [] hardCodedTokens;
	
	/* Inner interface */
	/* Inner interface: Progress bar updater */
	public interface ProgBarUpdater {
		public void update(); /* To be overridden in derived classes */
	}
	private transient ProgBarUpdater progBarUpdater = null;
	private transient TokenDegeneracy tokenDegen = null;
	
	/* Validation data */
	transient List<float []> sdveDataTrain = null;
	transient List<Integer> trueLabelsTrain = null;

	transient List<float []> sdveDataValidation = null;
	transient List<Integer> trueLabelsValidation = null;

    /* Validation data */
    transient List<float []> sdveDataTest = null;
    transient List<Integer> trueLabelsTest = null;
	
	/* Constructors */
	public TokenRecogEngineSDV() {
		super();
		
		bIncludeTokenSize = false;
		bIncludeTokenWHRatio = false;
		bIncludeTokenNumStrokes = true;
		
		hiddenLayer1_size = 50;
		hiddenLayer2_size = 0;
		useTanh = false;		
	}
	
	public void loadTokenDegeneracy(JsonObject tokenDegenObj) {
		tokenDegen = new TokenDegeneracy(tokenDegenObj);
	}
	
	public void setHardCodedTokens(String [] tHardCodedTokens) {
		hardCodedTokens = tHardCodedTokens;
	}
	
	public TokenRecogEngineSDV(int t_hiddenLayer1_size, 
						       int t_hiddenLayer2_size, 
						       int t_maxIter, 
						       double t_threshErrRate) {
		this();
		
		hiddenLayer1_size = t_hiddenLayer1_size;
		hiddenLayer2_size = t_hiddenLayer2_size;
		trainMaxIter = t_maxIter;
		trainThreshErrRate = t_threshErrRate;
		bnet = null;
		tokenNames = null;
	}
	
	/* Set the features set (token size, width-height ratio (WHR) and number of strokes (NS) */
	/* Inputs: length==3 boolean array */
	@Override
	public void setFeatures(int [] ivs, boolean [] bvs) {
		if ( ivs.length == 2 && bvs.length == 3 ) {
			npPerStroke = ivs[0];
			maxNumStrokes = ivs[1];
			
			bIncludeTokenSize = bvs[0];
			bIncludeTokenWHRatio = bvs[1];
			bIncludeTokenNumStrokes = bvs[2];
		}
		else {
			System.err.println("Incorrect number of elements in feature setting input");
		}
	}
	
	public boolean [] getFeatures() {
		boolean [] fs = new boolean[3];
		fs[0] = bIncludeTokenSize;
		fs[1] = bIncludeTokenWHRatio;
		fs[2] = bIncludeTokenNumStrokes;
		
		return fs;
	}
	
	private boolean isTokenHardCoded(String token) {
		if (hardCodedTokens == null) {
			return false;
		}
		else {
			boolean isTokenHardCoded = false;
			for (String hardCodedToken : hardCodedTokens) {
				if ( token.equals(hardCodedToken) ) {
					isTokenHardCoded = true;
					break;
				}	
			}
			
			return isTokenHardCoded;
		}
		
	}

	public Map<String, DataSet> readDataFromDir(String inDirName, List<String> labels) { //, ArrayList<String> aTokenNames
        TokenSettings tokenSettings = new TokenSettings(
                bIncludeTokenSize,
                bIncludeTokenWHRatio,
                bIncludeTokenNumStrokes,
                hardCodedTokens,
                npPerStroke,
                maxNumStrokes,
                tokenDegen
        );

        DataSetWithStringLabels dataSetWithStringLabels = MachineLearningHelper.readDataFromDir(inDirName, tokenSettings);
        DataSet dataSet = MachineLearningHelper.convertStringLabelsToIndices(dataSetWithStringLabels);

        assert(labels != null);
        assert(labels.isEmpty());
        labels.addAll(dataSet.getLabelNames());

        // Strategy for dividing the data into training and test subsets
        Map<String, Float> dataDiv = new HashMap<>();
        dataDiv.put("training", 0.8f);      // TODO: Externalize
        dataDiv.put("validation", 0.1f);
        dataDiv.put("test", 0.1f);

        return MachineLearningHelper.divideIntoSubsetsEvenlyAndRandomly(dataSet, dataDiv);
	}
	
	/* Form MLData with CHandWritingTokenImageData */
	public static MLData getMLData(float [] sdve) {
		int ni = sdve.length;
		MLData mlData = new BasicMLData(ni);
		
		for (int j = 0; j < ni; j++)
			mlData.setData(j, sdve[j]);
		
		return mlData;
	}
	
	public int getTrainingProgPercent() {
		return trainingProgPercent;
	}
	
	public void trainConsoleLim(final MLTrain train,
			final BasicNetwork network, final MLDataSet trainingSet,
			final int maxIter, final double threshErrorRate,  
			final boolean bVerbose) {
		//double remaining;

		if ( bVerbose )
			System.out.println("Beginning training ...");

		double errRate = 1.0;
		double minusLogThreshErrRate = -1.0 * Math.log10(threshErrorRate);
		final long start = System.currentTimeMillis();
		int nIter = 0;

		BasicNetwork [] iter_bnets = new BasicNetwork[maxIter + 1];
		float [] validationErrRates = new float[maxIter + 1];
        float [] testErrRates = new float[maxIter + 1];
		
		do {			
			train.iteration();

			final long current = System.currentTimeMillis();
			final double elapsed = (double)(current - start) / 1000;// seconds
			//remaining = seconds - elapsed;

			int iteration = train.getIteration();

			errRate = train.getError();
			iter_bnets[nIter] = (BasicNetwork) bnet.clone();
			validationErrRates[nIter] = getValidationErrorRate();
            testErrRates[nIter] = getTestErrorRate();
			
			nIter++;
			/* Get the test error rate */
			
			if ( bVerbose )
				System.out.println("Iteration #" + Format.formatInteger(iteration)
						+ " training error rate = " + Format.formatPercent(train.getError())
						+ "; validation error rate = " + Format.formatPercent(validationErrRates[nIter - 1])
						+ "; elapsed time = " + Format.formatTimeSpan((int) elapsed));

			double pctIter = (double) nIter / maxIter;
			double minusLogErrRate = -1.0 * Math.log10(errRate); 
			double pctErr = minusLogErrRate / minusLogThreshErrRate;
			double pctProg = (pctIter > pctErr) ? pctIter : pctErr;
			int newPctProg = trainingPercent_loadData + (int) ((100 - trainingPercent_loadData) * pctProg);
			if ( newPctProg > trainingProgPercent ) {
				trainingProgPercent = newPctProg;

				/* A more elegant solution based on inner class and inheritance */
				if ( progBarUpdater != null )
					progBarUpdater.update();
			}
		}  while (nIter <= maxIter);
//		while (nIter <= maxIter && errRate > threshErrorRate);
		
		int minErrIter = MathHelper.minIndex(validationErrRates);
        System.out.println("minErrIter = " + minErrIter +
                           "; minValidationErrRate = " + validationErrRates[minErrIter] +
                           "; minTestErrRate = " + testErrRates[minErrIter]);
		bnet = (BasicNetwork) iter_bnets[minErrIter].clone();
		
		train.finishTraining();
	}	

	@Override
	public void train(String inDirName, String outDataDirName) {
		if (inDirName.length() < 1) {
			System.err.println("ERROR: The specified input directory name is empty");
		} else {
			/* Reset training progress percentage */
			trainingProgPercent = 0;

			if ( progBarUpdater != null )
				progBarUpdater.update();

			sdveDataValidation = new ArrayList<>();

			trueLabelsValidation = new ArrayList<>();
			tokenNames = new ArrayList<>();

			Map<String, DataSet> divDataSets = readDataFromDir(inDirName, tokenNames);

            sdveDataTrain = divDataSets.get("training").getX();
            trueLabelsTrain = divDataSets.get("training").getY();

            /* Data sanity checks */
            assert( !sdveDataTrain.isEmpty() );
            assert( !trueLabelsTrain.isEmpty() );
            assert(sdveDataTrain.size() == trueLabelsTrain.size());

            sdveDataValidation = divDataSets.get("validation").getX();
            trueLabelsValidation = divDataSets.get("validation").getY();

            sdveDataTest = divDataSets.get("test").getX();
            trueLabelsTest = divDataSets.get("test").getY();

            assert( !sdveDataValidation.isEmpty() );
            assert( !trueLabelsValidation.isEmpty() );
            assert(sdveDataValidation.size() == trueLabelsValidation.size());

            /* Write data to csv files */
            /* Write training data */
            File f = new File(outDataDirName, "sdve_train_data.csv");
            DataIOHelper.printFloatDataToCsvFile(sdveDataTrain, f);
			System.out.println("Wrote training data to " + f.getAbsolutePath());

            /* Write validation data */
            f = new File(outDataDirName, "sdve_validation_data.csv");
            DataIOHelper.printFloatDataToCsvFile(sdveDataValidation, f);
            System.out.println("Wrote validation data to " + f.getAbsolutePath());

            /* Write test data */
            f = new File(outDataDirName, "sdve_test_data.csv");
            DataIOHelper.printFloatDataToCsvFile(sdveDataTest, new File(outDataDirName, "sdve_test_data.csv"));
            System.out.println("Wrote test data to " + f.getAbsolutePath());

            /* Write training labels */
            /* Determine the range of the label indices */
            int minLabel = Integer.MAX_VALUE;
            int maxLabel = Integer.MIN_VALUE;
            for (int label : trueLabelsTrain) {
                if (label < minLabel) {
                    minLabel = label;
                }
                if (label > maxLabel) {
                    maxLabel = label;
                }
            }

            assert(minLabel == 0);
            int nLabels = maxLabel - minLabel + 1;

            assert(nLabels == tokenNames.size());

            /* Get the count stats */
            int[] countStatsTrain = new int[nLabels];
            for (int trueLabel : trueLabelsTrain) {
                countStatsTrain[trueLabel]++;
            }

            for (int i = 0; i < countStatsTrain.length; ++i) {
                if (countStatsTrain[i] == 0) {
                    throw new RuntimeException("No training data is available for token " + tokenNames.get(i));
                }
            }

            int[] countStatsValidation = new int[nLabels];
            for (int trueLabel : trueLabelsValidation) {
                countStatsValidation[trueLabel]++;
            }

            int[] countStatsTest = new int[nLabels];
            for (int trueLabel : trueLabelsTest) {
                countStatsTest[trueLabel]++;
            }

            for (int i = 0; i < countStatsValidation.length; ++i) {
                if (countStatsValidation[i] == 0) {
                    throw new RuntimeException("No test data is available for token " + tokenNames.get(i));
                }
            }

            /* Print count stats for observation */
            for (int i = 0; i < nLabels; ++i) {
                System.out.printf("%s:\t(%d + %d + %d) = %d\n", tokenNames.get(i),
                                  countStatsTrain[i], countStatsValidation[i], countStatsTest[i],
                                  countStatsTrain[i] + countStatsValidation[i]);
            }

            /* Write train labels */
            f = new File(outDataDirName, "sdve_train_labels.csv");
            DataIOHelper.printLabelsDataToOneHotCsvFile(trueLabelsTrain, nLabels, f);
            System.out.println("Wrote training labels to " + f.getAbsolutePath());

            /* Write validation labels */
            f = new File(outDataDirName, "sdve_validation_labels.csv");
            DataIOHelper.printLabelsDataToOneHotCsvFile(trueLabelsValidation, nLabels, f);
            System.out.println("Wrote validation labels to " + f.getAbsolutePath());

            /* Write test labels */
            f = new File(outDataDirName, "sdve_test_labels.csv");
            DataIOHelper.printLabelsDataToOneHotCsvFile(trueLabelsTest, nLabels, f);
            System.out.println("Wrote test labels to " + f.getAbsolutePath());

            /* Write token names file */
            PrintWriter pw = null;
            try {
                f = new File(outDataDirName, "token_names.txt");
                pw = new PrintWriter(f);
                for (String tokenName : tokenNames) {
                    pw.println(tokenName);
                }

                System.out.println("Wrote token names to " + f.getAbsolutePath());

            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            } finally {
                pw.close();
            }

			/* Add dot, if it is not included */
			for (String hardCodedToken : hardCodedTokens) {
				if (tokenNames.indexOf(hardCodedToken) == -1) {
					tokenNames.add(hardCodedToken);
				}
			}
			
			/* Determine the index of the dot (".") token */
			if (dotTokenIndex == -1) {
				dotTokenIndex = tokenNames.indexOf(".");
			}
			
			/* Output sanity check */
			if ( sdveDataTrain.size() == 0 ) {
				System.err.println("ERROR: Could not load any training data from input directory");
				System.exit(1);
			}
			
			if ( sdveDataTrain.size() != trueLabelsTrain.size() ) {
				System.err.println("ERROR: Unexpected error: Size mismatch in the results of readDataFromDir()");
				System.exit(1);
			}
			
			/* Prepare ML data */
			int nt = tokenNames.size(); /* Number of unique tokens */
			int ni = sdveDataTrain.get(0).length;
			
			BasicMLDataSet trainSet = new BasicMLDataSet();
			for (int i = 0; i < trueLabelsTrain.size(); ++i) {
				/* True label */
				MLData ideal = new BasicMLData(nt);
				final int idx = trueLabelsTrain.get(i);
				for (int j = 0; j < nt; j++) {
					if (j == idx) {
						ideal.setData(j, 1.0);
					} else {
						ideal.setData(j, 0.0);
					}
				}
				
				/* Data */
				MLData inData = new BasicMLData(ni);
				for (int j = 0; j < ni; j++) {
					inData.setData(j, sdveDataTrain.get(i)[j]);
				}
				
				trainSet.add(inData, ideal);
			}
			
			if ( bDebug )
				System.out.println("Done preparing data from ML: # of entries = " + trainSet.size() + 
								   "; dimension of input = " + trainSet.getInputSize() +
								   "; dimensoin of ideal = " + trainSet.getIdealSize());
			
			trainingProgPercent = trainingPercent_loadData;
//			if ( trainingProgDialog != null ) {
//				trainingProgDialog.setProgress(trainingProgPercent);
//			}
			if ( progBarUpdater != null )
				progBarUpdater.update();
			
			/* Perform training */
			bnet = EncogUtility.simpleFeedForward(trainSet.getInputSize(), 
										          hiddenLayer1_size, hiddenLayer2_size, 
											      trainSet.getIdealSize(), useTanh);
			if ( bDebug )
				System.out.println("Created network: " + bnet.toString());
			
			if ( bDebug )
				System.out.println("Commencing training ... ");

			final ResilientPropagation train = new ResilientPropagation(bnet, trainSet);
			train.addStrategy(new ResetStrategy(strategyError, strategyCycles));

			final boolean bVerbose = true;
//			EncogUtility.trainConsole_lim(train, bnet, trainSet, trainMaxIter, trainThreshErrRate, bVerbose);			
			trainConsoleLim(train, bnet, trainSet, trainMaxIter, trainThreshErrRate, bVerbose);
			if ( bDebug )
				System.out.println("Training stopped");
			
			trainingProgPercent = 100;
//			if ( trainingProgDialog != null ) {
//				trainingProgDialog.setProgress(trainingProgPercent);
//			}
			if ( progBarUpdater != null )
				progBarUpdater.update();
			
			/* Test a few */
//			if ( bDebug ) {
//				final int IDX_TEST_BEG = 800;
//				final int IDX_TEST_END = 900;
//				
//				for (int i = IDX_TEST_BEG; i < IDX_TEST_END; ++i) {
//					double [] t_ideal = trainSet.get(i).getIdeal().getData();
//					int true_label = 0;
//					for (; true_label < t_ideal.length; ++true_label)
//						if ( t_ideal[true_label] != 0.0 )
//							break;
//					
//					if ( true_label == t_ideal.length )
//						continue;
//	
//					final int winner = bnet.winner(trainSet.get(i).getInput());
//					
//					System.out.println("Input #" + i + ": true_label=" + true_label + "; net_winner=" + winner);
//				}
//			}
		}
		
		//Encog.getInstance().shutdown();
	}
	
	/* Status: is the engine trained and ready to recognize tokens? */
	@Override
	public boolean isReadyToRecognize() {
		return (bnet != null) && (tokenNames != null);
	}
		
	/* Recognize using the currently version of network */
	public int recognize(float [] sdve, double [] outPs) {
		if ( !isReadyToRecognize() ) {
			System.err.println("ERROR: Letter recognition engine not ready to perform recognition");
			return -1;
		}
		
		MLData sdveMLData = getMLData(sdve);
		final double [] ps = bnet.compute(sdveMLData).getData();
		if ( outPs.length == ps.length ) {
			for (int i = 0; i < ps.length; ++i)
				outPs[i] = ps[i];
		}
		else {
			System.err.println("WRONG_OUTPS_LENGTH: Wrong length of input argument: outPs");
		}
		
		return bnet.winner(getMLData(sdve));
	}
	

	
	/* Recognize, with CWrittenToken, not image data, as input. 
	 * This is more general.
	 */
	@Override
	public int recognize(CWrittenToken wt, double [] outPs) {
		float [] wh = null;
		
		/* If the token is small enough in both width and height, recognize it as a dot */
		if ( wt.width <= dotMaxWidth && wt.height <= dotMaxHeight ) {
			if (dotTokenIndex == -1) {
				dotTokenIndex = tokenNames.indexOf(".");
			}
			
			if (dotTokenIndex >= 0) {
				/* Dummy outPs under the condition of force dot recognition */
				for (int i = 0; i < outPs.length; ++i) {
					if (i == dotTokenIndex) {
						outPs[i] = 1.0;
					}
					else {
						outPs[i] = 0.0;
					}
				}
				
	
				return dotTokenIndex;
			}
		}
		
		if ( wt.width != 0.0f && wt.height != 0.0f ) {
			wh = new float[2];
			wh[0] = wt.width;
			wh[1] = wt.height;
		}
			
		float [] sdv = wt.getSDV(npPerStroke, maxNumStrokes, wh);
		float [] sepv = wt.getSEPV(maxNumStrokes); /* TODO: Make optional */

		float [] sdve = MachineLearningHelper.addExtraDimsToSDV(sdv, sepv, wt.width, wt.height, wt.nStrokes(),
                bIncludeTokenSize,
                bIncludeTokenWHRatio,
                bIncludeTokenNumStrokes);

		return recognize(sdve, outPs);
	}
	
	/* Get error rate on test data set */
	float getValidationErrorRate() {
		if ( sdveDataValidation == null || trueLabelsValidation == null )
			throw new RuntimeException("Test data have not be generated yet");
		
		int nTested = 0;
		int nErr = 0;
		double [] ps = new double[tokenNames.size()];
		for (int i = 0; i < sdveDataValidation.size(); ++i) {
			int recogIdx = recognize(sdveDataValidation.get(i), ps);
			
			int trueIdx = trueLabelsValidation.get(i);
			
			nTested++;
			if (recogIdx != trueIdx)
				nErr++;

		}

		return (float) nErr / (float) nTested;
	}

    public float getTestErrorRate() {
        if ( sdveDataTest == null || trueLabelsTest == null )
            throw new RuntimeException("Test data have not be generated yet");

        int nTested = 0;
        int nErr = 0;
        double [] ps = new double[tokenNames.size()];
        for (int i = 0; i < sdveDataTest.size(); ++i) {
            int recogIdx = recognize(sdveDataTest.get(i), ps);

            int trueIdx = trueLabelsTest.get(i);

            nTested++;
            if (recogIdx != trueIdx)
                nErr++;

        }

        return (float) nErr / (float) nTested;
    }

	
}
