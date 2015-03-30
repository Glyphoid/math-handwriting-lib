package me.scai.parsetree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.net.URL;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;


public class TerminalSet {
	/* Constants */
	public final static String commentString = "#";
//	public final static String epsString = "EPS";
	
	/* Members */
	HashMap<String, String []> type2TokenMap = new HashMap<String, String []>();
	HashMap<String, String>	token2TypeMap = new HashMap<String, String>();
	HashMap<String, String> token2TexNotationMap = new HashMap<String, String>();
	
	/* Constructor */
	/* Default constructor */
	public TerminalSet() {}
	
	public void readFromUrl(URL tsFileUrl) 
			throws IOException {
		String [] lines = null;
		try {
			lines = TextHelper.readLinesTrimmedNoCommentFromUrl(tsFileUrl, commentString);
		}
		catch ( Exception e ) {
			throw new IOException("Failed to read terminal set from URL: \"" + tsFileUrl + "\"");
		}
		finally {
			
		}
		
		for (int k = 0; k < lines.length; ++k) {
			if ( lines[k].length() == 0 )
				continue;	/* Skip empty lines */
			
			String t_line = lines[k].replace("\t", " ");
			String [] t_items = t_line.split(" ");
						
			String t_type = t_items[0];			
			if ( t_items[0].length() == 0 ) {
//				throw new IOException("Failed to read terminal set from file: " + tsFileName);
				throw new IOException("Failed to read terminal set from URL: \"" + tsFileUrl + "\"");
			}
			
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
			for (int j = 0; j < t_tokens.length; ++j) {
				token2TypeMap.put(t_tokens[j], t_type);
			}
		}
	}
	
	public void readFromJsonAtUrl(URL tsFileUrl) 
			throws IOException {
		String [] lines = null;
		try {
			lines = TextHelper.readLinesTrimmedNoCommentFromUrl(tsFileUrl, commentString);
		}
		catch ( Exception e ) {
//			throw new IOException("Failed to read terminal set from file: " + tsFileName);
			throw new IOException("Failed to read terminal set from URL: \"" + tsFileUrl + "\"");
		}
		finally {
			
		}
		
		/* Concatenate the lines to a single string */
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines.length; ++i) {
			sb.append(lines[i]);
			sb.append("\n");
		}
		
		JsonObject obj = new JsonParser().parse(sb.toString()).getAsJsonObject();
		
		/* Read the terminals and their types */
		JsonObject termsObj = obj.get("terminals").getAsJsonObject();	
		for (Map.Entry<String, JsonElement> entry : termsObj.entrySet()) {
			String typeName = (String) entry.getKey();
			
			JsonArray termsArray = entry.getValue().getAsJsonArray();
			
			Iterator<JsonElement> termsIt = termsArray.iterator();
			List<String> lstTerms = new ArrayList<String>();
			while (termsIt.hasNext()) {
				String termName = termsIt.next().getAsString();
				lstTerms.add(termName);
			}
			
			/* Add to type-to-token map */
			String [] terms = new String[lstTerms.size()];
			lstTerms.toArray(terms);
			type2TokenMap.put(typeName, terms);
			
			/* Add to token-to-type map */
			for (int j = 0; j < terms.length; ++j) {
				token2TypeMap.put(terms[j], typeName);
			}
		}
		
		/* Read the TeX notations */
		JsonObject texObj = obj.get("texNotations").getAsJsonObject();		
		for (Map.Entry<String, JsonElement> entry : texObj.entrySet()) {
			String termName = entry.getKey();
			String texNotation = entry.getValue().getAsString();
			
			token2TexNotationMap.put(termName, texNotation);
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
	public static TerminalSet createFromUrl(URL tsFileUrl)
		throws Exception {
		TerminalSet ts = new TerminalSet();
		
		try {
			ts.readFromUrl(tsFileUrl);
		}
		catch ( Exception e ) {
			throw new Exception("Failed to create TerminalSet from URL: \"" + tsFileUrl + "\"");
		}
		
		return ts;
	}
	
	/* Factory method: From a JSON file */
	public static TerminalSet createFromJsonAtUrl(URL tsJsonFileUrl) 
		throws Exception {
		TerminalSet ts = new TerminalSet();
		
		try {
			ts.readFromJsonAtUrl(tsJsonFileUrl);
		}
		catch ( Exception e ) {
			throw new Exception("Failed to create TerminalSet from JSON file at URL: \"" + tsJsonFileUrl + "\"");
		}
		
		return ts;
	}
	
	/* Testing routine */
//	public static void main(String [] args) {
//		final String tsFN = "C:\\Users\\scai\\Plato\\handwriting\\graph_lang\\terminals.txt";
//		
//		try {
//			TerminalSet.createFromFile(tsFN);
//		}
//		catch ( Exception e ) {
//			System.err.println(e.getMessage());
//		}
//	}
	
}