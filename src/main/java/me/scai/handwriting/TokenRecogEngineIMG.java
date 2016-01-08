package me.scai.handwriting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;





import java.util.LinkedList;




import me.scai.parsetree.MathHelper;


//import org.encog.Encog;
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

public class TokenRecogEngineIMG extends TokenRecogEngine {
	private static final long serialVersionUID = 1L;
	
	/* Feature settings */
	private int writtenTokenImgW;
	private int writtenTokenImgH;
	
	/* Testing data */
	ArrayList<Double []> imgDataTest = null;
	ArrayList<Integer> trueLabelsTest = null;
	
	/* Constructors */
	public TokenRecogEngineIMG() {
		super();
		
		bIncludeTokenSize = false;
		bIncludeTokenWHRatio = true;
		bIncludeTokenNumStrokes = true;

		hiddenLayer1_size = 80;
		hiddenLayer2_size = 0;
		useTanh = false;
	}
	
	public TokenRecogEngineIMG(int t_hiddenLayer1_size, 
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
			writtenTokenImgW = ivs[0];
			writtenTokenImgH = ivs[1];
			
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
	
	public void readDataFromDir(String inDirName, 
			                    int testRatioDenom, int testRatioNumer, 
							    ArrayList<Double []> imgDataTrain,
							    ArrayList<Double []> imgDataTest, 
							    ArrayList<Integer> trueLabelsTrain,
							    ArrayList<Integer> trueLabelsTest, 
							    ArrayList<String> aTokenNames) {
		ArrayList<Double []> imgData = new ArrayList<Double []>();
		ArrayList<Integer> trueLabels = new ArrayList<Integer>();
		
		ArrayList<String> trueTokens = new ArrayList<String>();
		ArrayList<Integer> nws = new ArrayList<Integer>(); /* Records all image widths */
		ArrayList<Integer> nhs = new ArrayList<Integer>(); /* Records all image heights */
		
		if ( bDebug ) 
			System.out.println("Input directory: " + inDirName);
		
		File inDir = new File(inDirName);
		
		/* Test the existence of the input directory */
		if ( !inDir.isDirectory() ) {
			System.err.println("Cannot find directory " + inDirName);
			System.exit(1);
		}
		
		/* Get the list of all .im files */
		File [] files = inDir.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
		        return (name.startsWith(im_file_prefix) && name.endsWith(im_file_suffix));
		    }
		});
		
		if ( bDebug )
			System.out.println("Found " + files.length + " potentially valid input .im files");
		
		imgData.ensureCapacity(files.length);
		trueTokens.ensureCapacity(files.length);
		nws.ensureCapacity(files.length);
		nhs.ensureCapacity(files.length);
		
		for (int i = 0; i < files.length; ++i) {
			if ( bDebug )
				System.out.print("Reading data from file: " + files[i].getName() + " ...");
			
			try {
				/* Actual reading */
				CHandWritingTokenImageData t_imData = 
						CHandWritingTokenImageData.readImFile(files[i], 
								                              bIncludeTokenSize,
								                              bIncludeTokenWHRatio, 
								                              bIncludeTokenNumStrokes);
				
				imgData.add(t_imData.imData);
				trueTokens.add(t_imData.tokenName);
				nws.add(t_imData.nw);
				nhs.add(t_imData.nh);
			}
			catch (IOException e) {
				System.err.println("WARNING: Failed to read valid data from file: " + files[i].getName());
			}
			
			if ( bDebug )
				System.out.print(" Done\n");
		}
		
		/* Check the uniqueness of nw and nh */			
		HashSet<Integer> uws = new HashSet<Integer>();
		uws.addAll(nws);
		
		HashSet<Integer> uhs = new HashSet<Integer>();
		uhs.addAll(nhs);
		
		if ( uws.size() != 1 || uhs.size() != 1 ) {
			System.err.println("ERROR: The dimensions of the image in the .im files are not unique.");
			System.exit(1);
		}
		
		/* Get the set of unique token names */
		HashSet<String> uTokenNames = new HashSet<String>();
		uTokenNames.addAll(trueTokens);			
		if ( bDebug ) 
			System.out.println("Discovered " + uTokenNames.size() + " unique token names.");
		
		aTokenNames.ensureCapacity(uTokenNames.size());
		for (String s : uTokenNames)
			aTokenNames.add(s);
		Collections.sort(aTokenNames);
		
		/* Generate true labels */
		trueLabels.ensureCapacity(trueTokens.size());
		//Integer [] trueLabels = new Integer[trueTokens.size()];
		for (int i = 0; i < trueTokens.size(); ++i)
			trueLabels.add(aTokenNames.indexOf(trueTokens.get(i)));
		
		/* Divide the data set into training and test sets */
		HashMap<String, Integer> tokenIdxMap = new HashMap<String, Integer>();
		for (int i = 0; i < aTokenNames.size(); ++i) 
			tokenIdxMap.put(aTokenNames.get(i), i);
		
		ArrayList<LinkedList<Integer>> tokenIndices = new ArrayList<LinkedList<Integer>>();
		ArrayList<LinkedList<Integer>> isTest = new ArrayList<LinkedList<Integer>>();
		for (int i = 0; i < aTokenNames.size(); ++i) {
			tokenIndices.add(new LinkedList<Integer>());
			isTest.add(new LinkedList<Integer>());
		}
		
		for (int i = 0; i < trueTokens.size(); ++i) {
			String trueToken = trueTokens.get(i);
			int tokenIndex = tokenIdxMap.get(trueToken);
			tokenIndices.get(tokenIndex).add(i);
			
			int n = tokenIndices.get(tokenIndex).size();
			int m = n % testRatioDenom;
			if ( m < testRatioNumer )
				isTest.get(tokenIndex).add(1);
			else
				isTest.get(tokenIndex).add(0);
		}
		
		/* Populate the imgDataTrain, imgDataTest, trueLabelsTrain and trueLabesTest lists */
		int nTrainTotal = 0;
		int nTestTotal = 0;
		
		for (int i = 0; i < tokenIndices.size(); ++i) {
			int nTrainToken = 0; 
			int nTestToken = 0;
						
			for (int j = 0; j < tokenIndices.get(i).size(); ++j) {
				int idx = tokenIndices.get(i).get(j);
				
				if ( isTest.get(i).get(j) == 1 ) {
					nTestTotal++;
					nTestToken++;
					imgDataTest.add(imgData.get(idx));
					trueLabelsTest.add(trueLabels.get(idx));
				}
				else {
					nTrainTotal++;
					nTrainToken++;
					imgDataTrain.add(imgData.get(idx));
					trueLabelsTrain.add(trueLabels.get(idx));
				}
			}
			
			if ( bDebug )
				System.out.println("Token \"" + aTokenNames.get(i) + "\": " + 
			                       "nTrainToken = " + nTrainToken + 
						           "; nTestToken = " + nTestToken);
		}
		
		/* Print summary */
		if ( bDebug )
			System.out.println("All tokens: nTrainTotal = " + nTrainTotal + "; nTestTotal = " + nTestTotal);
	}
	
	/* Form MLData with CHandWritingTokenImageData */		
	public static MLData getMLData(CHandWritingTokenImageData imDat) {
		int ni = imDat.imData.length;
		MLData mlData = new BasicMLData(ni);
		
		for (int j = 0; j < ni; j++)
			mlData.setData(j, imDat.imData[j]);
		
		return mlData;
	}
	
	public static MLData getMLData(Double [] imData) {
		int ni = imData.length;
		MLData mlData = new BasicMLData(ni);
		
		for (int j = 0; j < ni; j++)
			mlData.setData(j, imData[j]);
		
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
		float [] testErrRates = new float[maxIter + 1];
		
		do {			
			train.iteration();

			final long current = System.currentTimeMillis();
			final double elapsed = (double)(current - start) / 1000;// seconds
			//remaining = seconds - elapsed;

			int iteration = train.getIteration();

			errRate = train.getError();
			iter_bnets[nIter] = (BasicNetwork) bnet.clone();
			testErrRates[nIter] = getTestErrRate();
			
			nIter++;
			/* Get the test error rate */
			
			if ( bVerbose )
				System.out.println("Iteration #" + Format.formatInteger(iteration)
						+ " training error rate = " + Format.formatPercent(train.getError())
						+ "; test error rate = " + Format.formatPercent(testErrRates[nIter - 1])
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
		} 
		while (nIter <= maxIter);
//		while (nIter <= maxIter && errRate > threshErrorRate);
		
		int minErrIter = MathHelper.minIndex(testErrRates);
		if ( bDebug )
			System.out.println("minErrIter = " + minErrIter + 
					           "; minTestErrRate = " + testErrRates[minErrIter]);
		bnet = (BasicNetwork) iter_bnets[minErrIter].clone();
		
		train.finishTraining();
	}	
	
	@Override
	public void train(String inDirName, String outDataDirName) {
		int testRatioDenom = 20;
		int testRatioNumer = 3;
		
		if (inDirName.length() < 1) {
			System.err.println("EXRROR: The specified input directory name is empty");
		} else {
			/* Reset training progress percentage */
			trainingProgPercent = 0;
//			if ( trainingProgDialog != null ) {				 
//				trainingProgDialog.setProgress(trainingProgPercent);
//			}
			if ( progBarUpdater != null )
				progBarUpdater.update();
			
			ArrayList<Double []> imgDataTrain = new ArrayList<Double []>();
			imgDataTest = new ArrayList<Double []>();
			
			ArrayList<Integer> trueLabelsTrain = new ArrayList<Integer>();
			trueLabelsTest = new ArrayList<Integer>();
			
			tokenNames = new ArrayList<String>();
			
			readDataFromDir(inDirName, 
					        testRatioDenom, testRatioNumer, 
					        imgDataTrain, imgDataTest, 
					        trueLabelsTrain, trueLabelsTest, 
					        tokenNames);
			
			/* Output sanity check */
			if ( imgDataTrain.size() == 0 ) {
				System.err.println("ERROR: Could not load any training data from input directory");
				System.exit(1);
			}
			
			if ( imgDataTrain.size() != trueLabelsTrain.size() ) {
				System.err.println("ERROR: Unexpected error: Size mismatch in the results of readDataFromDir()");
				System.exit(1);
			}
			
			/* Prepare ML data */
			int nt = tokenNames.size(); /* Number of unique tokens */
			int ni = imgDataTrain.get(0).length;
			
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
					inData.setData(j, imgDataTrain.get(i)[j]);
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
	
	
	public int recognize(Double [] imData, double [] outPs) {
		if ( !isReadyToRecognize() ) {
			System.err.println("ERROR: Letter recognition engine not ready to perform recognition");
			return -1;
		}
		
		final double [] ps = bnet.compute(getMLData(imData)).getData();
		if ( outPs.length == ps.length ) {
			for (int i = 0; i < ps.length; ++i)
				outPs[i] = ps[i];
		}
		else {
			System.err.println("WRONG_OUTPS_LENGTH: Wrong length of input argument: outPs");
		}
		
		return bnet.winner(getMLData(imData));
	}	
	
	/* Recognize using the currently version of network */
	public int recognize(CHandWritingTokenImageData imData, double [] outPs) {
		if ( !isReadyToRecognize() ) {
			System.err.println("ERROR: Letter recognition engine not ready to perform recognition");
			return -1;
		}
		
		final double [] ps = bnet.compute(getMLData(imData)).getData();
		if ( outPs.length == ps.length ) {
			for (int i = 0; i < ps.length; ++i)
				outPs[i] = ps[i];
		}
		else {
			System.err.println("WRONG_OUTPS_LENGTH: Wrong length of input argument: outPs");
		}
		
		return bnet.winner(getMLData(imData));
	}
	
	/* Recognize, with CWrittenToken, not image data, as input. 
	 * This is more general.
	 */
	@Override
	public int recognize(CWrittenToken wt, double [] outPs) {
		CHandWritingTokenImageData imgDat 
			= wt.getImageData(writtenTokenImgW, writtenTokenImgH,  
					  		  bIncludeTokenSize, 
					  		  bIncludeTokenWHRatio, 
					  		  bIncludeTokenNumStrokes);
		
		return recognize(imgDat, outPs);
	}
	
	/* Get error rate on test data set */
	private float getTestErrRate() {
		if ( imgDataTest == null || trueLabelsTest == null )
			throw new RuntimeException("Test data have not be generated yet");
		
		int nTested = 0;
		int nErr = 0;
		double [] ps = new double[tokenNames.size()];
		for (int i = 0; i < imgDataTest.size(); ++i) {
			int recogIdx = recognize(imgDataTest.get(i), ps);
			
			int trueIdx = trueLabelsTest.get(i);
			
			nTested++;
			if (recogIdx != trueIdx)
				nErr++;
			
//			System.out.println("\"" + tokenNames.get(trueIdx) + "\" recognized as \"" +
//			                   tokenNames.get(recogIdx) + "\"");
		}
		
//		System.out.println("Tested = " + nTested + "; nErr = " + nErr + 
//	                       "; errRate = " + (float) nErr / (float) nTested);
	                       
		return (float) nErr / (float) nTested;
	}
	
	/* Testing routine */
	public static void main(String [] args) {
		/* Token engine settings */
		final boolean bLoadEngineFromDisk = false;
		
		final int hiddenLayerSize1 = 80;
		final int hiddenLayerSize2 = 0;
		final int trainMaxIter = 200;
		final double trainThreshErr = 0.0001;
		
		final int WT_W = 16;
		final int WT_H = 16;
		
		final boolean bIncludeTokenSize = false;
		final boolean bIncludeTokenWHRatio = true;
		final boolean bIncludeTokenNumStrokes = true;
		
		final String letterDir = "C:\\Users\\systemxp\\Documents\\My Dropbox\\Plato\\data\\letters";
		
		final String tokenEngineSerFN = "C:\\Users\\systemxp\\Documents\\My Dropbox\\Plato\\engines\\img.ser";
		/* ~Token engine settings */
				
		if ( !bLoadEngineFromDisk ) {
			TokenRecogEngineIMG tokEngine = new TokenRecogEngineIMG(hiddenLayerSize1, 
					                                                hiddenLayerSize2, 
					                                                trainMaxIter, 
                                                 					trainThreshErr);
			System.out.println("New instance of tokEngine created.");

			int [] ivs = new int[2];
			ivs[0] = WT_W;
			ivs[1] = WT_H;

			boolean [] bvs = new boolean[3];
			bvs[0] = bIncludeTokenSize;
			bvs[1] = bIncludeTokenWHRatio;
			bvs[2] = bIncludeTokenNumStrokes;

			tokEngine.setFeatures(ivs, bvs);
			tokEngine.train(letterDir, null);

			/* Test run on all the previous data */
			/* Get the list of all .wt files */
			float errRate = tokEngine.getTestErrRate();
			System.out.println("Final test error rate = " + errRate);

			/* Serialize to disk */
			ObjectOutputStream objOutStream = null;
			try {
				objOutStream = new ObjectOutputStream(new FileOutputStream(tokenEngineSerFN));
				objOutStream.writeObject(tokEngine);
				objOutStream.flush();
			}
			catch (IOException e) {
				System.err.println("WRITE_TOKEN_ENGINE_ERROR: Failed to write token engine to file: " + tokenEngineSerFN);
			}
			finally {
				try {
					objOutStream.close();
				}
				catch (IOException e) {
					System.err.println("TOKEN_ENGINE_OUTPUT_STREAM_CLOSE_ERR: Failed to close token engine output stream");
				}
			}
		}

	}
	
}
