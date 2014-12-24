package me.scai.handwriting;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;

import java.util.Properties;
import java.net.URL;

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

import me.scai.parsetree.MathHelper;

public class TokenRecogEngineSDV extends TokenRecogEngine implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/* Feature settings */
	private int npPerStroke = 16;
	private int maxNumStrokes = 4;	
	
	
	/* Inner interface */
	/* Inner interface: Progress bar updater */
	public interface ProgBarUpdater {
		public void update(); /* To be overridden in derived classes */
	}
	private transient ProgBarUpdater progBarUpdater = null;
	
	/* Testing data */
	ArrayList<float []> sdveDataTest = null;
	ArrayList<Integer> trueLabelsTest = null;
	
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
	
	public void readDataFromDir(String inDirName, 
							    int testRatioDenom, int testRatioNumer, 
					            ArrayList<float []> sdvDataTrain,
					            ArrayList<float []> sdvDataTest, 
							    ArrayList<Integer> trueLabelsTrain, 
							    ArrayList<Integer> trueLabelsTest,
							    ArrayList<String> aTokenNames) {
		ArrayList<float []> sdvData = new ArrayList<float []>();
		ArrayList<Integer> trueLabels = new ArrayList<Integer>();
		
		ArrayList<String> trueTokens = new ArrayList<String>();
		
		if ( bDebug ) 
			System.out.println("Input directory: " + inDirName);
		
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
			  System.out.println("Reading data from subdirectory: " + allFiles[i].getPath()); //DEBUG
			  this.readDataFromDir(allFiles[i].getPath(), testRatioDenom, testRatioNumer, 
			                       sdvDataTrain, sdvDataTest, trueLabelsTrain, trueLabelsTest, aTokenNames);
			}
		}
		
		/* Get the list of all .wt files */
		File [] files = inDir.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
		        return (name.startsWith(wt_file_prefix) && name.endsWith(wt_file_suffix));
		    }
		});
		
		if ( bDebug )
			System.out.println("Found " + files.length + " potentially valid input .im files");
		
		sdvData.ensureCapacity(files.length);
		trueTokens.ensureCapacity(files.length);
		
		for (int i = 0; i < files.length; ++i) {
			if ( bDebug )
				System.out.print("Reading data from file: " + files[i].getName() + " ...");
			
			float [] sdve = null;
			try {
				/* Actual reading */
				CWrittenToken t_wt = new CWrittenToken(files[i]);
				
				String wtPath = files[i].getPath();
				String imPath = wtPath.substring(0, wtPath.length() - wt_file_suffix.length()) + 
						        im_file_suffix;
				File imFile = new File(imPath);
				CHandWritingTokenImageData t_imData = 
						CHandWritingTokenImageData.readImFile(imFile, 
								                              bIncludeTokenSize,
								                              bIncludeTokenWHRatio, 
								                              bIncludeTokenNumStrokes);
//				if ( t_imData.tokenName.equals("p") ) {
//				  System.out.println("tokenName = \"" + t_imData.tokenName + "\"");
//				}
				
				float [] im_wh = new float[2];
				im_wh[0] = t_imData.w;
				im_wh[1] = t_imData.h;
				float [] sdv = t_wt.getSDV(npPerStroke, maxNumStrokes, im_wh);
				
				sdve = this.addExtraDimsToSDV(sdv, t_wt);
				
				sdvData.add(sdve);
				trueTokens.add(t_imData.tokenName);
			}
			catch (Exception e) {
				System.err.println("WARNING: Failed to read valid data from file: " + files[i].getName());
			}
			
			if ( bDebug )
				System.out.print(" Done\n");
		}
		
		/* Get the set of unique token names */
		HashSet<String> uTokenNames = new HashSet<String>();		
		uTokenNames.addAll(trueTokens);
		if ( bDebug ) 
			System.out.println("Discovered " + uTokenNames.size() + " unique token names.");
		
		aTokenNames.ensureCapacity(uTokenNames.size());
		for (String s : uTokenNames) {
			if ( !aTokenNames.contains(s) ) {
				aTokenNames.add(s);
			}
			//aTokenNames.add(s);
		}
		//Collections.sort(aTokenNames); /* Deactivate sorting so that it does not fall part under recursive calling */
		
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
		
		/* Populate the sdveTrain, sdveTest, trueLabelsTrain and trueLabesTest lists */
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
					sdvDataTest.add(sdvData.get(idx));
					trueLabelsTest.add(trueLabels.get(idx));
				}
				else {
					nTrainTotal++;
					nTrainToken++;
					sdvDataTrain.add(sdvData.get(idx));
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
	public void train(String inDirName) {
		if (inDirName.length() < 1) {
			System.err.println("ERROR: The specified input directory name is empty");
		} else {
			/* Reset training progress percentage */
			trainingProgPercent = 0;
//			if ( trainingProgDialog != null ) {				 
//				trainingProgDialog.setProgress(trainingProgPercent);
//			}
			if ( progBarUpdater != null )
				progBarUpdater.update();
			
			int testRatioDenom = 20;
			int testRatioNumer = 3;
			ArrayList<float []> sdveDataTrain = new ArrayList<float []>();
			sdveDataTest = new ArrayList<float []>();
			/* sdve: SDV + extra dimensions of feature vector (e.g., width and height, width-height ratio, number of strokes) */
			
			ArrayList<Integer> trueLabelsTrain = new ArrayList<Integer>();
			trueLabelsTest = new ArrayList<Integer>();
			
			tokenNames = new ArrayList<String>();
			
			readDataFromDir(inDirName, 
					        testRatioDenom, testRatioNumer, 
					        sdveDataTrain, sdveDataTest, 
					        trueLabelsTrain, trueLabelsTest, 
					        tokenNames);
			
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
	
	private float [] addExtraDimsToSDV(float [] sdv, 
			                           CWrittenToken wt) {
		float [] sdve = null;
		float [] extraDims = new float[4];	
		/* The size needs to be expanded if more potential options 
		 * are added in the future */
		
		int nExtraDims = 0;
		/* Get the extra dimensions */
		if ( this.bIncludeTokenSize ) {
			float [] t_bnds = wt.getBounds();
			
			extraDims[nExtraDims++] = t_bnds[2] - t_bnds[0];	/* Width */
			extraDims[nExtraDims++] = t_bnds[3] - t_bnds[1];	/* Height */
		}
		if ( this.bIncludeTokenWHRatio ) {
			float [] t_bnds = wt.getBounds();
			
			extraDims[nExtraDims++] = (t_bnds[3] - t_bnds[1]) / (t_bnds[2] - t_bnds[0]);
		}
		if ( this.bIncludeTokenNumStrokes ) {
			extraDims[nExtraDims++] = wt.nStrokes();
		}
		
		/* Include the extra dimensions */
		if ( nExtraDims == 0 ) {
			sdve = sdv; 
		}
		else {
			sdve = new float[sdv.length + nExtraDims];
			
			System.arraycopy(sdv, 0, sdve, 0, sdv.length);
			for (int j = 0; j < nExtraDims; ++j)
				sdve[j + sdv.length] = extraDims[j];
		}
		
		return sdve;
	}
	
	/* Recognize, with CWrittenToken, not image data, as input. 
	 * This is more general.
	 */
	@Override
	public int recognize(CWrittenToken wt, double [] outPs) {
		float [] wh = null;
		if ( wt.width != 0.0f && wt.height != 0.0f ) {
			wh = new float[2];
			wh[0] = wt.width;
			wh[1] = wt.height;
		}
			
		float [] sdv = wt.getSDV(npPerStroke, maxNumStrokes, wh); /* TODO */
		
		float [] sdve = addExtraDimsToSDV(sdv, wt);
		
		return recognize(sdve, outPs);
	}
	
	/* Get error rate on test data set */
	private float getTestErrRate() {
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
		final boolean bLoadEngineFromDisk = false; /* false: train; true: test */
		
		final int hiddenLayerSize1 = 160; /* Orig: 100 */
		final int hiddenLayerSize2 = 0;
		final int trainMaxIter = 200;
		final double trainThreshErr = 0.001;
		
		final int t_npPerStroke = 16;
		final int t_maxNumStrokes = 4;
		
		final boolean bIncludeTokenSize = false;
		final boolean bIncludeTokenWHRatio = false;
		final boolean bIncludeTokenNumStrokes = true;
		
 
		Properties props = new Properties();
		URL url = TokenRecogEngineSDV.class.getResource("/resources/handwriting.properties");		
		
		try {
//			props.load(new FileInputStream("/handwriting.properties"));
			props.load(url.openStream());
		}
		catch (Exception exc) {
			System.err.println("Failed to load property file: " + exc.getMessage());
		}
		
//		final String letterDir = "C:\\Users\\scai\\Dropbox\\Plato\\data\\letters";
		final String letterDir = props.getProperty("letterDir");
//		final String tokenEngineSerFN = "C:\\Users\\scai\\Dropbox\\Plato\\engines\\token_engine.sdv.sz0_whr0_ns1.ser";
		final String tokenEngineSerFN = props.getProperty("tokenEngineSerFN");
		/* ~Token engine settings */
				
		/* TODO: Make both TokenRecogEngine and TokenRecogEngineSDV inherit from a 
		 * common parent class. 
		 */
		if ( !bLoadEngineFromDisk ) {
			TokenRecogEngineSDV tokEngine = new TokenRecogEngineSDV(hiddenLayerSize1, 
					hiddenLayerSize2, 
					trainMaxIter, 
					trainThreshErr);
			
			System.out.println("New instance of tokEngine created.");

			int [] ivs = new int[2];
			ivs[0] = t_npPerStroke;
			ivs[1] = t_maxNumStrokes;

			boolean [] bvs = new boolean[3];
			bvs[0] = bIncludeTokenSize;
			bvs[1] = bIncludeTokenWHRatio;
			bvs[2] = bIncludeTokenNumStrokes;

			tokEngine.setFeatures(ivs, bvs);
			tokEngine.train(letterDir);

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
		else {
			ObjectInputStream objInStream = null;
			TokenRecogEngine tokEngine = null;
			try {
				objInStream = new ObjectInputStream(new FileInputStream(tokenEngineSerFN));
								tokEngine = (TokenRecogEngineSDV) objInStream.readObject();
				/* TODO: Add option of TokenRecogEngineSDV */
				
				//objInStream.close();
			}
			catch (IOException e) {
				System.err.println("READ_TOKEN_ENGINE_ERROR: Failed to read token engine from file: " 
			                       + tokenEngineSerFN);
			}
			catch (ClassNotFoundException e) {
				System.err.println("READ_TOKEN_ENGINE_ERROR: Failed to read token engine from file: "
			                       + tokenEngineSerFN);
			}
			finally {
				try {
					objInStream.close();
				}
				catch (IOException e) {
					System.err.println("TOKEN_ENGINE_INPUT_STREAM_CLOSE_ERR: Failed to close token engine input stream");
				}
			}
			
			CWrittenToken wt = new CWrittenToken();
			
			//String data_dir = "C:\\Users\\systemxp\\Documents\\Dropbox\\Plato\\data\\letters\\";
			//String token_num = "1099";
			String data_dir = "C:\\Users\\systemxp\\Documents\\Dropbox\\Plato\\data\\letters\\new2\\";
			String token_num = "365";
			String wt_fn = data_dir + tokEngine.wt_file_prefix + token_num + tokEngine.wt_file_suffix;
			String im_fn = data_dir + tokEngine.im_file_prefix + token_num + tokEngine.im_file_suffix;
			
			File wt_f = new File(wt_fn);
			File im_f = new File(im_fn);
			
			CHandWritingTokenImageData t_imData = null;
			try {
				wt.readFromFile(wt_f);
				t_imData = 
						CHandWritingTokenImageData.readImFile(im_f, 
								                              bIncludeTokenSize,
								                              bIncludeTokenWHRatio, 
								                              bIncludeTokenNumStrokes);
				wt.width = t_imData.w;
				wt.height = t_imData.h;
			}
			catch (IOException ioe) {
				throw new RuntimeException();
			}
			
			double [] ps = new double[tokEngine.tokenNames.size()];			
			int recogIdx = tokEngine.recognize(wt, ps);
			String recogToken = tokEngine.getTokenName(recogIdx);
			
			System.out.println(wt_fn + ": true label = \"" + t_imData.tokenName + "\" recognized as \"" + recogToken + "\"");
		}
		
	}
	
}
