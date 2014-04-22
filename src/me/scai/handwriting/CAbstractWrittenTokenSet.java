package me.scai.handwriting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class CAbstractWrittenTokenSet {
	protected int nt = 0; /* Number of tokens */
	protected String [] tokenNames = null;
	
	/* Geometric bounds of the token set */
	protected float min_x = Float.MAX_VALUE;
	protected float min_y = Float.MAX_VALUE;
	protected float max_x = Float.MIN_VALUE;
	protected float max_y = Float.MIN_VALUE;
	
	public ArrayList<String> recogWinners = new ArrayList<String>();
	public ArrayList<double []> recogPs = new ArrayList<double []>();
	
	/* Get the number of tokens */
	public int nTokens() {
		return nt;
	}
	
	/* Test if the set is empty */
	public boolean empty() {
		return (nt == 0);
	}
	
	/* Add a new token */
	public void addOneToken() {
		nt++;
	}
	
	/* Set char set: the set of possible token names.
	 * For example, these can be used in conjunction with recogPs. */
	public void setTokenNames(String [] t_tokenNames) {
		tokenNames = t_tokenNames;
	}
	
	/* Write data to .wts file */
	public void writeToFileBrief(String fileName, boolean bBrief) 
			throws IOException, IllegalStateException {
		PrintWriter writer = null;
		try {			
			writer = new PrintWriter(fileName);
			if ( bBrief ) {
				writer.print(getStringBrief());
			}
			else {
				/* TODO */
			}
		}
//		catch ( IOException e ) {
//			/* TODO */
//			throw e;
//		}
		catch ( FileNotFoundException e) {
			/* TODO */
			throw new IOException();
		}
		catch ( IllegalStateException e ) {
			/* TODO */
			throw e;
		}
		finally {
			if ( writer != null )
				writer.close();
		}
	}	
	
	public void deleteOneToken() {
		nt--;
	}
	
	/* *** Abstract methods *** */	
	protected abstract void calcBounds();
	public abstract String getStringBrief();
	
}
