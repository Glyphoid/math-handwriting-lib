package me.scai.handwriting;

import java.util.LinkedList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import me.scai.handwriting.CHandWritingTokenImageData;

/* CWrittenToken: a written token, consisting of one or more strokes (CStrokes) */
public class CWrittenToken {
	private LinkedList<CStroke> strokes = new LinkedList<CStroke>();
	public boolean bNormalized = false;
	private float min_x = Float.MAX_VALUE, max_x = Float.MIN_VALUE;
	private float min_y = Float.MAX_VALUE, max_y = Float.MIN_VALUE;
	public float width = 0f;
	public float height = 0f;
	
	String recogWinner;
	double [] recogPs;
	
	/* Constructor */
	public CWrittenToken() {};
	
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
		if ( strokes.size() == 0 )
			System.err.println("EMPTY_STROKES_ERR: There are no storkes in this written token.");
		
		for (int i = 0; i < strokes.size(); ++i) {
			if ( strokes.get(i).min_x < min_x ) min_x = strokes.get(i).min_x;
			if ( strokes.get(i).max_x > max_x ) max_x = strokes.get(i).max_x;
			if ( strokes.get(i).min_y < min_y ) min_y = strokes.get(i).min_y;
			if ( strokes.get(i).max_y > max_y ) max_y = strokes.get(i).max_y;
		}
		
		width = max_x - min_x;
		height = max_y - min_y;
		
		for (int i = 0; i < strokes.size(); ++i)
			strokes.get(i).normalizeAxes(min_x, max_x, min_y, max_y);
		
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
}