package me.scai.handwriting;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.io.Serializable;

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


public class TokenRecogEngine implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static boolean bDebug = true;
	private final static String wt_file_prefix = "L_";
	private final static String wt_file_suffix = ".wt";
	private final static String im_file_prefix = "L_";
	private final static String im_file_suffix = ".im";
	
	/* Feature settings */
	private boolean bIncludeTokenSize = false;
	private boolean bIncludeTokenWHRatio = true;
	private boolean bIncludeTokenNumStrokes = true;
	
	/* Network and training settings */
	int hiddenLayer1_size = 80;
	int hiddenLayer2_size = 0;
	boolean useTanh = false;
	
	/* Training strategy */
	private final double strategyError = 0.25;
	private final int strategyCycles = 100; 
	private int trainMaxIter = 100;
	//private double trainSeconds = 10.0;
	private double trainThreshErrRate = 0.01;
	
	private BasicNetwork bnet = null;
	
	private static final int trainingPercent_loadData = 10;
	protected int trainingProgPercent = 0;
	
	/* Methods */
	public int getTrainMaxIter() {
		return trainMaxIter;
	}
	
	public double getTrainThreshErrRate() {
		return trainThreshErrRate;
	}
	
	/* The set of all token names (unique, e.g., a, b, c, ...) */
	public ArrayList<String> tokenNames = null;
	
	/* Inner interface */
	/* Inner interface: Progress bar updater */
	public interface ProgBarUpdater {
		public void update(); /* To be overridden in derived classes */
	}
	private transient ProgBarUpdater progBarUpdater = null;
	
	/* Constructors */
	public TokenRecogEngine() {
		
	}
	
	public TokenRecogEngine(int t_hiddenLayer1_size, 
						    int t_hiddenLayer2_size, 
						    int t_maxIter, 
						    double t_threshErrRate) {
		hiddenLayer1_size = t_hiddenLayer1_size;
		hiddenLayer2_size = t_hiddenLayer2_size;
		trainMaxIter = t_maxIter;
		trainThreshErrRate = t_threshErrRate;
		bnet = null;
		tokenNames = null;
	}
	
	/* Function for setting a new progBarUpdater */
	protected void setProgBarUpdater(ProgBarUpdater pbu) {
		progBarUpdater = pbu;
	}
	
	/* Set the features set (token size, width-height ratio (WHR) and number of strokes (NS) */
	/* Inputs: length==3 boolean array */
	public void setFeatures(boolean [] fs) {
		if ( fs.length == 3 ) {
			bIncludeTokenSize = fs[0];
			bIncludeTokenWHRatio = fs[1];
			bIncludeTokenNumStrokes = fs[2];
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
							           ArrayList<Double []> imgData, 
							           ArrayList<Integer> trueLabels, 
							           ArrayList<String> aTokenNames) {
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
	}
	
	/* Form MLData with CHandWritingTokenImageData */
	public static MLData getMLData(CHandWritingTokenImageData imDat) {
		int ni = imDat.imData.length;
		MLData mlData = new BasicMLData(ni);
		
		for (int j = 0; j < ni; j++)
			mlData.setData(j, imDat.imData[j]);
		
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
		
		do {
			train.iteration();

			final long current = System.currentTimeMillis();
			final double elapsed = (double)(current - start) / 1000;// seconds
			//remaining = seconds - elapsed;

			int iteration = train.getIteration();
			
			errRate = train.getError();
			nIter++;
			
			if ( bVerbose )
				System.out.println("Iteration #" + Format.formatInteger(iteration)
						+ " Error:" + Format.formatPercent(train.getError())
						+ " elapsed time = " + Format.formatTimeSpan((int) elapsed));
			
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
		while (nIter <= maxIter && errRate > threshErrorRate);
		train.finishTraining();
	}
	
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
			
			ArrayList<Double []> imgData = new ArrayList<Double []>();
			ArrayList<Integer> trueLabels = new ArrayList<Integer>();
			tokenNames = new ArrayList<String>();
			
			readDataFromDir(inDirName, imgData, trueLabels, tokenNames);			
			
			/* Output sanity check */
			if ( imgData.size() == 0 ) {
				System.err.println("ERROR: Could not load any data from input directory");
				System.exit(1);
			}
			
			if ( imgData.size() != trueLabels.size() ) {
				System.err.println("ERROR: Unexpected error: Size mismatch in the results of readDataFromDir()");
				System.exit(1);
			}
			
			/* Prepare ML data */
			int nt = tokenNames.size(); /* Number of unique tokens */
			int ni = imgData.get(0).length;
			
			BasicMLDataSet trainSet = new BasicMLDataSet();
			for (int i = 0; i < trueLabels.size(); ++i) {
				/* True label */
				MLData ideal = new BasicMLData(nt);
				final int idx = trueLabels.get(i);
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
					inData.setData(j, imgData.get(i)[j]);
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
	public boolean isReadyToRecognize() {
		return (bnet != null) && (tokenNames != null);
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

	/* Re-format all .im files */
	public void reFormatImFiles(String inDirName, int imgW, int imgH) {
		File inDir = new File(inDirName);
		
		File [] files = inDir.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
		        return (name.startsWith(wt_file_prefix) && name.endsWith(wt_file_suffix));
		    }
		});
		
		if ( files.length == 0 )
			return;
		
		for (int i = 0; i < files.length; ++i) {
			CWrittenToken t_wToken = new CWrittenToken();
			
			String letter = null;
			try {
				letter = t_wToken.readFromFile(files[i]);
			}
			catch (IOException e) {
				
			}
			
			//t_wToken.normalizeAxes();			
			
			/* Get the name of the .im file */
			String t_path = inDirName.endsWith("/") ? inDirName : inDirName + "/";
			String imFN = t_path + files[i].getName().replace(wt_file_suffix, im_file_suffix);
			System.out.println("Processing file: " + imFN);
						
			float [] wh = null;
			try {
				wh = CWrittenToken.getTokenWidthHeightFromImFile(imFN);
			}
			catch (IOException e) {
				/* TODO */
			}
			t_wToken.width = wh[0];
			t_wToken.height = wh[1];
			t_wToken.bNormalized = true;
			
			try {
				t_wToken.writeImgFile(imFN, letter, imgW, imgH);
			}
			catch (IOException e) {
				
			}
		}
	}
	
	/* Testing routine */
//	public static void main(String [] args) {
//		
//	}
	
}
