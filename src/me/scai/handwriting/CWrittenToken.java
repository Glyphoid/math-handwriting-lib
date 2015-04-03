package me.scai.handwriting;

import java.util.LinkedList;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import me.scai.handwriting.CHandWritingTokenImageData;
import me.scai.parsetree.TerminalSet;
import me.scai.parsetree.MathHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/* CWrittenToken: a written token, consisting of one or more strokes (CStrokes) */
public class CWrittenToken {
	/* Member variables */
//	private final static Logger logger = Logger.getLogger(CWrittenToken.class.getName()); //DEBUG
	
	private LinkedList<CStroke> strokes = new LinkedList<>();
	public boolean bNormalized = false;
	private float min_x = Float.MAX_VALUE, max_x = Float.MIN_VALUE;
	private float min_y = Float.MAX_VALUE, max_y = Float.MIN_VALUE;
	public float width = 0f;
	public float height = 0f;
	
	private String recogWinner;
	private double [] recogPs;
	
	/* The type of a token, according to the terminal set */
	public String tokenTermType = null;
	
	/* ~Member variables */
	
	/* Constructor */
	public CWrittenToken() {};
	
	/* Copy constructor */
	public CWrittenToken(CWrittenToken wt0) {
		for (CStroke stroke : wt0.strokes) {
			addStroke(stroke);
		}
		
		bNormalized = wt0.bNormalized;
		min_x = wt0.min_x;
		min_y = wt0.min_y;
		width = wt0.width;
		height = wt0.height;
		
		if (recogWinner != null) {
			recogWinner = new String(wt0.recogWinner);
		}
		
		if (recogPs != null) {
			recogPs = new double[wt0.recogPs.length];
			for (int i = 0; i < wt0.recogPs.length; ++i) {
				recogPs[i] = wt0.recogPs[i];
			}
		}
		
		if (wt0.tokenTermType != null) {
			tokenTermType = new String(wt0.tokenTermType);
		}
	}
	
	
	/* Constructor: From JSON string */
	/* Expected fields: 
	 *   numStrokes : Number of strokes
	 *   strokes    : A nested array for stroke data. Each element of the array are expected to have the following fields:
	 *                numPoints: Number of discrete points
	 *                x:         An array of x data
	 *                y:         An array of y data 
	 *                  (x and y data are assumed to follow the HTML canvas definition of coordinates)
	 */
	public CWrittenToken(String jsonStrokes) {
		JsonObject jsonToken = null;
		int numStrokes;
		JsonObject strokes = null;
		
//		logger.info("Beginning to parse JSON");
		try {
			jsonToken = new JsonParser().parse(jsonStrokes).getAsJsonObject();
			
			numStrokes = jsonToken.getAsJsonPrimitive("numStrokes").getAsInt();
			
//			logger.info("jsonToken = " + jsonToken);
//			logger.info("numStokes = " + numStrokes);
			
			strokes = jsonToken.getAsJsonObject("strokes");
			for (int i = 0; i < numStrokes; ++i) {				
				String key = String.format("%d", i);
				
				JsonObject tStroke = strokes.getAsJsonObject(key);
				int numPoints = tStroke.getAsJsonPrimitive("numPoints").getAsInt();
				
				JsonArray xs = tStroke.getAsJsonArray("x");
				JsonArray ys = tStroke.getAsJsonArray("y");
				
				CStroke s = new CStroke(xs.get(0).getAsFloat(), ys.get(0).getAsFloat());
				for (int j = 1; j < numPoints; ++j) {
					s.addPoint(xs.get(j).getAsFloat(), ys.get(j).getAsFloat());
				}
				
				this.strokes.add(s);
			}
		}
		catch (IndexOutOfBoundsException oobE) {
			throw new RuntimeException("Index exceeded bound(s) during parsing of JSON representation of written token");
		}
		catch (Exception exc) {
			throw new RuntimeException("JSONException occurred during parsing of JSON representation of written token" + exc.getMessage());
		}

		this.normalizeAxes();
	}
	
	public CWrittenToken(float [] t_bnds, String t_recogWinner, double [] t_recogPs) {
		min_x = t_bnds[0];
		min_y = t_bnds[1];
		max_x = t_bnds[2];
		max_y = t_bnds[3];
		
		recogWinner = t_recogWinner;
		recogPs = t_recogPs;
		
		width = max_x - min_x;
		height = max_y - min_y;
	}
	
	public CWrittenToken(File wtFile) {		
		try {
			readFromFile(wtFile);
		}
		catch (IOException ioe) {
			throw new RuntimeException("IOException occurred during construction of CWrittenToken from File");
		}
	}
	
	/* Clear */
	public void clear() {
		strokes.clear();
		
		min_x = Float.MAX_VALUE;
		max_x = Float.MIN_VALUE;
		min_y = Float.MAX_VALUE;
		max_y = Float.MIN_VALUE;
		
		width = 0f;
		height = 0f;
		
		bNormalized = false;
	}
	
	/* Whether there are no strokes in this token */
	public boolean isEmpty() {
		return strokes.isEmpty();
	}
	
	/* Remove a given stroke */
	public void removeStroke(int i) {
		if ( i < 0 ) {
			System.err.println("Index for removal is negative");
			return;
		}
		
		if ( i >= strokes.size() ) {
			System.err.println("Index for removal exceeds number of strokes");
			return;			
		}
		
		strokes.remove(i);
	}
	
	/* Remove the last stroke */
	public void removeLastStroke() {
		if ( strokes.size() == 0 )
			return;
		
		removeStroke(strokes.size() - 1);
	}
	
	/* Adding the initial point of a new stroke */
	public void newStroke(float x, float y) {
		strokes.add(new CStroke(x, y));
	}
	
	/* Adding points to existing strokes */
	public void addPoint(float x, float y) {
		strokes.getLast().addPoint(x, y);
	}
	
	/* Add a new stroke.
	 * Unlike newStroke, this adds an existing stroke, 
	 * not just creates the first point */
	public void addStroke(final CStroke stroke) {
		strokes.add(stroke);
	}

	/* Get reference to the last token */
	public CStroke lastStroke() {
		return strokes.getLast();
	}
	
	/* Get the number of strokes */
	public int nStrokes() {
		return strokes.size();
	}
	
	/* Get the bounds: min_x, min_y, max_x, max_y */
	public float [] getBounds() {
		if ( !bNormalized )
			return null;
		
		float [] bounds = new float[4];
		
		bounds[0] = min_x;
		bounds[1] = min_y;
		bounds[2] = max_x;
		bounds[3] = max_y;
		
		return bounds;
	}
	
	public float getCentralX() {
		if ( !bNormalized )
			throw new RuntimeException("Calling getCentralX before normalization");
		
		return 0.5f * (min_x + max_x); 
	}
	
	public float getCentralY() {
		if ( !bNormalized )
			throw new RuntimeException("Calling getCentralY before normalization");
		
		return 0.5f * (min_y + max_y); 
	}
	
	
	@Override 
	public String toString() {
		String s = new String("CWrittenToken (nStrokes=");
		s += strokes.size() + "):\n";
		for (int i = 0; i < strokes.size(); ++i) {
			s += strokes.get(i).toString() + "\n";
		}
		
		return s;
	}
	
	/* Save strokes data to ASCII file 
	 * Input argument: wtFileName - name of the ASCII-format written token file */
	public void writeFile(String wtFileName,  String letter)
			throws IOException {
		try {
			PrintWriter writer = new PrintWriter(wtFileName);
			if ( letter != null )
				writer.println("Token name: " + letter);
			writer.print(this.toString());
			writer.close();
		}
		catch (java.io.FileNotFoundException e) {
			/* TODO */
			throw new IOException();			
		}
		
	}
	
	public static float [] getTokenWidthHeightFromImFile(String imFN)
		throws IOException {
		float [] wh = new float[2];
		
		FileInputStream fin = null;
		BufferedReader in = null;
		try {
			File imFile = new File(imFN);
			fin = new FileInputStream(imFile);
			in = new BufferedReader(new InputStreamReader(fin));
		}
		catch ( IOException e ) {
			throw new IOException();
		}
		
		String line = in.readLine();
		if ( !line.startsWith("Token name: ") ) { /* Assume a very specific file format */
			in.close();
			throw new IOException();
		}
		
		
		line = in.readLine();
		if ( !line.startsWith("n_w = ") ) { /* Assume a very specific file format */
			in.close();
			throw new IOException();
		}
		
		line = in.readLine();
		if ( !line.startsWith("n_h = ") ) { /* Assume a very specific file format */
			in.close();
			throw new IOException();
		}
		
		line = in.readLine();
		if ( line.startsWith("ns = ") ) 
			line = in.readLine();	/* New format: line "ns = " already exists */		
		if ( !line.startsWith("w = ") ) { /* Assume a very specific file format */
			in.close();
			throw new IOException();
		}
		wh[0] = Float.parseFloat(line.replace("w = ", ""));
		
		line = in.readLine();
		if ( !line.startsWith("h = ") ) { /* Assume a very specific file format */
			in.close();
			throw new IOException();
		}
		wh[1] = Float.parseFloat(line.replace("h = ", ""));

		in.close();
		return wh;
	}
	
	/* Load data from an ASCII file */
	public String readFromFile(File wtFile) 
		throws IOException {	
		clear(); /* Reset data fields and prepare for new data */
		
		FileInputStream fin = null;
		BufferedReader in = null;
		try {
			fin = new FileInputStream(wtFile);
			in = new BufferedReader(new InputStreamReader(fin));
		}
		catch ( IOException e ) {
			throw new IOException();
		}
		
		String line = null;
		String tokenName = null;
		line = in.readLine();
		if ( !line.startsWith("Token name: ") ) { /* Assume a very specific file format */
			in.close();
			throw new IOException();
		}
		tokenName = line.replace("Token name: ", "");
		
		line = in.readLine();
		if ( !line.startsWith("CWrittenToken (nStrokes=") ) {
			in.close();
			throw new IOException();
		}
		int nStrokes = Integer.parseInt(line.replace("CWrittenToken (nStrokes=", "").replace("):", ""));
		
		for (int i = 0; i < nStrokes; ++i) {
			CStroke t_stroke = new CStroke();
			
			line = in.readLine();
			if ( !line.startsWith("Stroke (np=") ) {
				in.close();
				throw new IOException();
			}
			
			/* Read X strings */
			line = in.readLine();
			line = line.replace("\\s+", "").replace("\t", ""); /* Strip white spaces */
			if ( !line.startsWith("xs=[") || !line.endsWith("]") ) {
				in.close();
				throw new IOException();
			}
			line = line.replace("xs=[", "").replace("]", "");
			String [] strXs = line.split(",");
			
			/* Read Y strings */
			line = in.readLine();
			line = line.replace("\\s+", "").replace("\t", ""); /* Strip white spaces */
			if ( !line.startsWith("ys=[") || !line.endsWith("]") ) {
				in.close();
				throw new IOException();
			}
			line = line.replace("ys=[", "").replace("]", "");
			String [] strYs = line.split(",");
			
			/* Length sanity check */
			if ( strXs.length != strYs.length ) {
				in.close();
				throw new IOException();
			}
			
			for (int j = 0; j < strXs.length; ++j)
				t_stroke.addPoint(Float.parseFloat(strXs[j]), Float.parseFloat(strYs[j]));
			
			strokes.add(t_stroke);
		}
		
				
		in.close();
		return tokenName;
	}
	
	/* Get token image data (CHandWritingTokenImageData) */
	public CHandWritingTokenImageData getImageData(int nw, int nh,
												   boolean includeTokenSize, 
												   boolean includeTokenWHRatio, 
												   boolean includeTokenNumStrokes) {
		/* TODO: Debug */
		if ( !this.bNormalized ) // Watch out for bugs
			normalizeAxes();
		
		CHandWritingTokenImageData tokenImgDat = new CHandWritingTokenImageData();
		tokenImgDat.nw = nw;
		tokenImgDat.nh = nh;
		
		tokenImgDat.nStrokes = this.nStrokes();
		
		//int dcnt = 4; /* Number of data elements before the start of the image data (e.g., w, h, whr and nStrokes) */
		
		/* Calculate the number of extra features */
		int nExtras = 0;
		if ( includeTokenSize )
			nExtras += 2;
		if ( includeTokenWHRatio )
			nExtras += 1;
		if ( includeTokenNumStrokes )
			nExtras += 1; 
		
		/* Allocate space */
		tokenImgDat.imData = new Double[nExtras + nw * nh];
		
		int dcnt = 0;		
		if ( includeTokenNumStrokes )
			tokenImgDat.imData[dcnt++] = (double) this.nStrokes();
		if ( includeTokenSize ) {
			tokenImgDat.imData[dcnt++] = (double) width;
			tokenImgDat.imData[dcnt++] = (double) height;
		}
		if ( includeTokenWHRatio ) {
			tokenImgDat.imData[dcnt++] = (double) width / (double) height;
		}
		
		int [] wtIm = getImageMap(nw, nh);
		for (int i = 0; i < wtIm.length; ++i)
			tokenImgDat.imData[dcnt + i] = (double)wtIm[i];
		
		return tokenImgDat;
	}
	
	/* Save image data to ASCII file */
	public void writeImgFile(String imgFileName, String letter, int w, int h) 
		throws IOException {
		try {
			int [] wtIm = getImageMap(w, h);
			
			PrintWriter writer = new PrintWriter(imgFileName);
			
			if ( letter != null )
				writer.println("Token name: " + letter);

			writer.println("n_w = " + w);
			writer.println("n_h = " + h);
			writer.println("ns = " + this.nStrokes()); /* Number of strokes */
			writer.println("w = " + width);
			writer.println("h = " + height);
			
			for (int i = 0; i < h; ++i) {
				for (int j = 0; j < w; ++j) {
					writer.print(String.format("%d ", wtIm[i * w + j]));
				}
				writer.print("\n");
			}
			
			writer.close();
		}
		catch (java.io.FileNotFoundException e) {
			/* TODO */
			throw new IOException();
		}
	}
	
	/* Normalize axes */
	public void normalizeAxes() {
		if ( bNormalized ) {
			return;
		}
		
		if ( strokes.size() == 0 ) {
			System.err.println("EMPTY_STROKES_ERR: There are no storkes in this written token.");
		}
		
		for (int i = 0; i < strokes.size(); ++i) {
			if ( strokes.get(i).min_x < min_x ) min_x = strokes.get(i).min_x;
			if ( strokes.get(i).max_x > max_x ) max_x = strokes.get(i).max_x;
			if ( strokes.get(i).min_y < min_y ) min_y = strokes.get(i).min_y;
			if ( strokes.get(i).max_y > max_y ) max_y = strokes.get(i).max_y;
		}
		
		width = max_x - min_x;
		height = max_y - min_y;
		
		for (int i = 0; i < strokes.size(); ++i) {
			strokes.get(i).normalizeAxes(min_x, max_x, min_y, max_y);
		}
		
		bNormalized = true;
	}
	
	/* Generate image map with specified width and height */
	public int [] getImageMap(int w, int h) {
		if ( w <= 0 || h <= 0 ) {
			System.err.println("INVALID_DIMENSIONS_ERR: Width and/or height have non-positive values");
		}
				
		if ( !bNormalized )
			normalizeAxes();
		
		int [] im = new int[w * h];
		
		for (int i = 0; i < strokes.size(); ++i)
			strokes.get(i).fillImageMap(im, w, h);
		
		return im;
	}
	
	public int [] getImageMapNoNormalization(int w, int h) {
		if ( w <= 0 || h <= 0 ) {
			System.err.println("INVALID_DIMENSIONS_ERR: Width and/or height have non-positive values");
		}
		
		int [] im = new int[w * h];
		
		for (int i = 0; i < strokes.size(); ++i)
			strokes.get(i).fillImageMap(im, w, h);
		
		return im;
	}
	
	public void setRecogWinner(String rw) {
		recogWinner = rw;
	}
	
	public String getRecogWinner() {
		return recogWinner;
	}
	
	public void setRecogPs(double [] ps) {
		recogPs = ps;
	}
	
	public double [] getRecogPs() {
		return recogPs;
	}
	
	/* Convert the CWrittenToken to SEPV (Stroke end-points vector): 
	 * For each stroke, there are four numerical values, all in normalized coordinate systems: [0.0, 1.0] 
	 * 	{startPointNormX, startPointNormY, endPointNormX, endPointNormY}
	 */
	public float [] getSEPV(final int maxNumStrokes) {
		
		float [] sepv = new float[4 * maxNumStrokes];
		int ns = (nStrokes() <= maxNumStrokes) ? nStrokes() : maxNumStrokes;
		
		for (int i = 0; i < ns; ++i) {
			int np = strokes.get(i).nPoints(); /* Number of points */
			float [] sxs = strokes.get(i).getXs(); /* Stroke X values */
			float [] sys = strokes.get(i).getYs(); /* Stroke Y values */
			
			sepv[i * 4] = sxs[0];
			sepv[i * 4 + 1] = sys[0];
			sepv[i * 4 + 2] = sxs[np - 1];
			sepv[i * 4 + 3] = sys[np - 1];
		}
		
		return sepv;
	}

	
	/* Convert the CWrittenToken to SDV (Stroke direction vector):
	 * Input arguments (Parameters for the SDV generation):
	 * 		npPerStroke:   Number of points per stroke. 
	 * 		maxNumStrokes: Maximum number of strokes, 
	 *      wh:            (Optional) true width and height (for legacy .im and .wt files). 
	 *                         If not used, set to null.
	 * 			will discard any strokes after the maximum number */
	public float [] getSDV(final int npPerStroke, 
						   final int maxNumStrokes, 
						   final float [] wh) {
		float hwRatio = 1.0f;
		if ( wh != null ) {
			if ( wh.length != 2 )
				throw new RuntimeException("wh ratio does not have the expected length (2)");
			hwRatio = wh[1] / wh[0];
		}
		
		if ( npPerStroke < 2 )
			throw new RuntimeException("The input value of npPerStroke is too small");
		if ( maxNumStrokes < 1 )
			throw new RuntimeException("The input value of maxNumStrokes is too small");
		
		if ( !bNormalized )
			this.normalizeAxes();
		
		int sdvLen = (npPerStroke - 1) * maxNumStrokes;
		float [] sdv = new float[sdvLen];
		
		int nStrokesToProcess = (this.nStrokes() > maxNumStrokes) ? maxNumStrokes : this.nStrokes();
		
		int sdvIdx = 0;
		for (int i = 0; i < nStrokesToProcess; ++i) {
			float [] xs = strokes.get(i).getXs();
			float [] ys = strokes.get(i).getYs();
			
			int N = xs.length;
			
			if ( N <= 2 ) {
				sdvIdx += (npPerStroke - 1);
				continue; /* TODO: Think about what this means for dots */
			}
			
			float [] dx = MathHelper.diff(xs);
			float [] dy = MathHelper.diff(ys);
			
			float [] ls = new float[dx.length];
			for (int j = 0; j < dx.length; ++j)
				ls[j] = (float) Math.sqrt((double) (dx[j] * dx[j] + dy[j] * dy[j]));
			
			float [] cuml = MathHelper.cumsum(ls, true);	/* Include initial zero */
			float suml = MathHelper.sum(ls);
			float ulen = suml / (npPerStroke - 1);
			
//			float [] sxs = new float[npPerStroke];	/* TODO: Optimize: Avoid repeated new */
//			float [] sys = new float[npPerStroke];
			
			float cx = xs[0];
			float cy = ys[0];
			float cl = 0f;
			int cp = 0;

			for (int k = 0; k < npPerStroke - 1; ++k) {
				cl += ulen;
				
				while ( cuml[cp] < cl && cp + 1 < cuml.length - 1 )
					++cp;
				
				float cf;
				float cx1;
				float cy1;
				
				if (cuml[cp] == cuml[cp - 1]) {
					cf = 0.0f;
					
					if (sdvIdx > 0) {
						sdv[sdvIdx] = sdv[sdvIdx - 1];
					}
					else {
						sdv[sdvIdx] = 0.0f;
					}
				}
				else {
					cf = (cl - cuml[cp - 1]) / (cuml[cp] - cuml[cp - 1]);
					
					cx1 = xs[cp - 1] + (xs[cp] - xs[cp - 1]) * cf;
					cy1 = ys[cp - 1] + (ys[cp] - ys[cp - 1]) * cf;
					
					sdv[sdvIdx] = (float) Math.atan2((double) ((cy1 - cy) * hwRatio), 
							                         (double) (cx1 - cx));
					
					cx = cx1;
					cy = cy1;
				}
				
				sdvIdx++;
				
			}
		}
		
		return sdv;
	}
	
	/* Get the terminal type of token */
	public void getTokenTerminalType(TerminalSet termSet) {
		if ( recogWinner != null )
			tokenTermType = termSet.getTypeOfToken(recogWinner);
	}
	
	/* Get the individual strokes */
	public CStroke getStroke(int i) {
		if (i < 0 || i >= nStrokes()) {
			return null;
		}
		else {
			return strokes.get(i);
		}
	}
	
//	/* main() for testing */
//	public static void main(String [] args) {
//		final String testWT_fn = "C:\\Users\\systemxp\\Documents\\My Dropbox\\Plato\\data\\letters\\L_100.wt";
//		
//		final int npPerStroke = 16;
//		final int maxNumStrokes = 4;
//		
//		System.out.println("CWrittenToken.main started");
//		
//		File testWT_f = new File(testWT_fn);
//		CWrittenToken wt = new CWrittenToken(testWT_f);
//		
//		float [] sdv = wt.getSDV(npPerStroke, maxNumStrokes, null);
//	}
	
}