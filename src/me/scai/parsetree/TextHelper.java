package me.scai.parsetree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class TextHelper {
	public static String [] readLinesTrimmedNoComment(final String fileName, final String commentString)
		throws FileNotFoundException, IOException {
		File wtsFile = new File(fileName);
		if ( !wtsFile.isFile() )
			throw new FileNotFoundException("Cannot find file for reading: " + fileName);
		
		FileInputStream fin = null;
		BufferedReader in = null;
		try {			
			fin = new FileInputStream(wtsFile);
			in = new BufferedReader(new InputStreamReader(fin));
		}
		catch ( IOException e ) {
			throw new IOException("IOException during reading of text file: " + fileName);
		}
		
		ArrayList<String> lineList = new ArrayList<String>();
		String line;
		while ( in.ready() ) {
			line = in.readLine().trim();
			
			if ( line.startsWith(commentString) ) 
				continue;
			
			if ( line.contains(commentString) ) {
				String [] items = line.split(commentString);
				line = items[0].trim();
			}
			
			lineList.add(line);
		}
		
		in.close();
		
		String [] lines = new String[lineList.size()];
		
		lineList.toArray(lines);
		
		return lines;
	}
	
	public static String [] removeTrailingEmptyLines(String [] lines) {
		ArrayList<String> linesList = new ArrayList<String>(Arrays.asList(lines));
		boolean bEndEmptyLine = linesList.get(linesList.size() - 1).equals("");
		while ( bEndEmptyLine ) {
			linesList.remove(linesList.size() - 1);
			
			bEndEmptyLine = linesList.get(linesList.size() - 1).equals("");
		}
		
		String [] newLines = new String[linesList.size()];
		linesList.toArray(newLines);
		
		return newLines;
	}
	
	public static int [] findAll(final String s, final String subs) {
		if ( s.length() == 0 || subs.length() == 0 )
			return null;
		
		ArrayList<Integer> indices = new ArrayList<Integer>();
		int i = 0;
		int len = subs.length();
		
		while ( s.indexOf(subs, i) != -1 ) {
			indices.add(s.indexOf(subs, i) + i);
			i += s.indexOf(subs, i) + len; 
		}
		
		int [] r = new int[indices.size()];  
		for (int n = 0; i < indices.size(); ++i)
			r[n] = indices.get(n);
		
		return r;
	}
	
	public static int numInstances(final String s, final String subs) {
		if ( s.length() == 0 || subs.length() == 0 )
			return 0;
		
		int nInstances = 0;
		int i = 0;
		int len = subs.length();
		
		while ( s.indexOf(subs, i) != -1 ) {
			nInstances++;
			i += s.indexOf(subs, i) + len; 
		}
		
		return nInstances;
		
	}
}