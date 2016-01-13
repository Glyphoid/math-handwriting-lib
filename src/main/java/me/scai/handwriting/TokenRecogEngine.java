package me.scai.handwriting;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.encog.neural.networks.BasicNetwork;

public abstract class TokenRecogEngine implements Serializable {
	protected static final long serialVersionUID = 1L;
	
	protected final static boolean bDebug = false;
	protected final static String wt_file_prefix = "L_";
	protected final static String wt_file_suffix = ".wt";
	protected final static String im_file_prefix = "L_";
	protected final static String im_file_suffix = ".im";
	
	/* Feature settings */
	protected boolean bIncludeTokenSize;
	protected boolean bIncludeTokenWHRatio;
	protected boolean bIncludeTokenNumStrokes;	
	
	/* Network and training settings */
	int hiddenLayer1_size;
	int hiddenLayer2_size;
	boolean useTanh;
	
	/* Training strategy */
	protected final double strategyError = 0.25;
	protected final int strategyCycles = 100; 
	protected int trainMaxIter = 100;
	//private double trainSeconds = 10.0;
	protected double trainThreshErrRate = 0.01;
	
	protected BasicNetwork bnet = null;
	
	protected static final int trainingPercent_loadData = 10;
	protected int trainingProgPercent = 0;
	
	/* The set of all token names (unique, e.g., a, b, c, ...) */
	public ArrayList<String> tokenNames = null;
	
	/* Inner interface */
	/* Inner interface: Progress bar updater */
	public Object trainingProgressDisplay;
	/* e.g., an instance of android.app.ProgressDialog */
	
	public interface ProgBarUpdater {
		public void update(); /* To be overridden in derived classes */
	}
	
	protected transient ProgBarUpdater progBarUpdater = null;
	
	/* Constructors (to be overridden) */
	public TokenRecogEngine() {}
	
	public TokenRecogEngine(int t_hiddenLayer1_size, 
                            int t_hiddenLayer2_size, 
                            int t_maxIter, 
                            double t_threshErrRate) {}
	
	/* Non-abstract methods */
	/* Get the i-th token name */
	public String getTokenName(int i) {
		return tokenNames.get(i);
	}

    public List<String> getAllTokenNames() {
        return tokenNames;
    }
		
	public int getTrainMaxIter() {
		return trainMaxIter;
	}
	
	public double getTrainThreshErrRate() {
		return trainThreshErrRate;
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
	
	/* Function for setting a new progBarUpdater */
	public void setProgBarUpdater(ProgBarUpdater pbu) {
		progBarUpdater = pbu;
	}
	
	public void setTrainingProgDialog(Object trainProgDisplay) {
		trainingProgressDisplay = trainProgDisplay;
		
		setProgBarUpdater(new ProgBarUpdater() {
			public void update() {
				if ( trainingProgressDisplay.getClass().getName() == "android.app.ProgressDialog" ) {
					try {
						Method method = trainingProgressDisplay.getClass().getMethod("setProgress");
						method.invoke(trainingProgressDisplay, trainingProgPercent);
					}
					catch ( NoSuchMethodException nsme ) {
						throw new RuntimeException("Encountered NoSuchMethodException");
					}
					catch ( InvocationTargetException ite ) {
						throw new RuntimeException("Encountered InvocationTargetException");
					}
					catch ( IllegalAccessException iae ) {
						throw new RuntimeException("Encountered IllegalAccessException");
					}
				}
//				trainingProgDialog.setProgress(trainingProgPercent);
			}
		});
	}
	
	/* Abstract methods */	
	public abstract void train(String inDirName, String outDataDirName);
	
	public abstract boolean isReadyToRecognize();
	
	public abstract void setFeatures(int [] ivs, boolean [] bvs);

	public abstract boolean isTokenHardCoded(CWrittenToken wt);
	
	public abstract int recognize(CWrittenToken wt, double [] outPs);

    public boolean isIncludeTokenSize() {
        return bIncludeTokenSize;
    }

    public boolean isIncludeTokenWHRatio() {
        return bIncludeTokenWHRatio;
    }

    public boolean isIncludeTokenNumStrokes() {
        return bIncludeTokenNumStrokes;
    }
}
