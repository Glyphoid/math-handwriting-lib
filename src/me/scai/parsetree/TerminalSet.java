package me.scai.parsetree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TerminalSet {
	/* Constants */
	public final static String commentString = "#";
//	public final static String epsString = "EPS";
	
	/* Members */
	HashMap<String, String []> type2TokenMap = new HashMap<String, String []>();
	HashMap<String, String>	token2TypeMap = new HashMap<String, String>();
	
	/* Constructor */
	/* Default constructor */
	public TerminalSet() {}
	
	public void readFromFile(String tsFileName) 
			throws IOException {
		String [] lines = null;
		try {
			lines = TextHelper.readLinesTrimmedNoComment(tsFileName, commentString);
		}
		catch ( Exception e ) {
			throw new IOException("Failed to read terminal set from file: " + tsFileName);
		}
		finally {
			
		}
		
		for (int k = 0; k < lines.length; ++k) {
			if ( lines[k].length() == 0 )
				continue;	/* Skip empty lines */
			
			String t_line = lines[k].replace("\t", " ");
			String [] t_items = t_line.split(" ");
						
			String t_type = t_items[0];			
			if ( t_items[0].length() == 0 )
				throw new IOException("Failed to read terminal set from file: " + tsFileName);
			
			ArrayList<String> lst_tokens = new ArrayList<String>();
			for (int j = 1; j < t_items.length; ++j) {
				if ( t_items[j].length() > 0 ) {
					lst_tokens.add(t_items[j]);
				}
			}
			
			String [] t_tokens = new String[lst_tokens.size()];
			lst_tokens.toArray(t_tokens);
			
			/* Add to type-to-token map */
			type2TokenMap.put(t_type, t_tokens);
			
			/* Add to token-to-type map */
			for (int j = 0; j < t_tokens.length; ++j)
				token2TypeMap.put(t_tokens[j], t_type);
		}
	}
	
	/* Get the type of a token */
	public String getTypeOfToken(String token) {
		return token2TypeMap.get(token);
		
	}
	
	/* Test if a type is a terminal type */
	public boolean isTypeTerminal(String type) {
//		if ( type == epsString )
//			return true; /* EPS is a terminal. */
//		else
		return type2TokenMap.keySet().contains(type);
	}
	
	/* Test if a token belongs to a terminal type */
	public boolean isTokenTerminal(String token) {
		 return token2TypeMap.keySet().contains(token);
	}
	
	/* Factory method */
	public static TerminalSet createFromFile(String tsFileName)
		throws Exception {
		TerminalSet ts = new TerminalSet();
		
		try {
			ts.readFromFile(tsFileName);
		}
		catch ( Exception e ) {
			throw new Exception("Failed to create TerminalSet from file: " + tsFileName);
		}
		
		return ts;
	}
	
	/* Testing routine */
	public static void main(String [] args) {
		final String tsFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\terminals.txt";
		
		try {
			TerminalSet.createFromFile(tsFN);
		}
		catch ( Exception e ) {
			System.err.println(e.getMessage());
		}
	}
	
}