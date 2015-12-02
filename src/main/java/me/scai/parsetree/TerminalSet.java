package me.scai.parsetree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.net.URL;

import me.scai.handwriting.TokenDegeneracy;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;


public class TerminalSet {
	/* Constants */
	public final static String commentString = "#";
	
	public final static String terminalNameTypePrefix = "TERMINAL(";
	public final static String terminalNameTypeSuffix = ")";
//	public final static String epsString = "EPS";
	
	/* Members */
	HashMap<String, String []> type2TokenMap = new HashMap<String, String []>();
	HashMap<String, String>	token2TypeMap = new HashMap<String, String>();
	HashMap<String, String> token2TexNotationMap = new HashMap<String, String>();
	
	private TokenDegeneracy tokenDegen;
	
	/* Constructor */
	/* Default constructor */
	public TerminalSet() {}
	
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
		
//		System.out.println("Done calling readLinesTrimmedNoCommentFromUrl()"); //DEBUG
//		System.out.println("lines.length = " + lines.length); //DEBUG
		
		/* Concatenate the lines to a single string */
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines.length; ++i) {
			sb.append(lines[i]);
			sb.append("\n");
		}
		
//		System.out.println("Done concatenating string"); //DEBUG
		
		JsonObject obj = new JsonParser().parse(sb.toString()).getAsJsonObject();
		
//		System.out.println("Done calling JsonParser.parse()"); //DEBUG
		
		/* Read the terminals and their types */
		JsonObject termsObj = obj.get("terminals").getAsJsonObject();	
//		System.out.println("termsObj = " + termsObj); //DEBUG
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
//		System.out.println("Done extracting terminal types"); //DEBUG
		
		/* Read the TeX notations */
		JsonObject texObj = obj.get("texNotations").getAsJsonObject();		
		for (Map.Entry<String, JsonElement> entry : texObj.entrySet()) {
			String termName = entry.getKey();
			String texNotation = entry.getValue().getAsString();
			
			token2TexNotationMap.put(termName, texNotation);
		}
//		System.out.println("Done reading terminal Math TeX notations"); //DEBUG
		
//		System.out.println("Obtaining tokenDegenObj"); //DEBUG
		JsonObject tokenDegenObj = obj.get("tokenDegeneracy").getAsJsonObject();
//		System.out.println("tokenDegenObj = " + tokenDegenObj); //DEBUG
		tokenDegen = new TokenDegeneracy(tokenDegenObj);
		
//		System.out.println("Done creating new TokenDegeneracy instance"); //DEBUG
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
		if (type2TokenMap.keySet().contains(type)) {
			return true;
		}
		else {
			if (isTerminalNameType(type)) { /* TODO: Use regex */
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	/* Test if a token belongs to a terminal type */
	public boolean isTokenTerminal(String token) {
		 return token2TypeMap.keySet().contains(token);
	}
	
	/* Determine whether a token matches a type. Normally, this just entails
	 * a lookup for the type of the token. But a special case is where the 
	 * type is something like "TERMINAL(l)"
	 */
	public boolean match(String tokenName, String typeName) {
		if (isTerminalNameType(typeName)) {
			return terminalNameTypeMatches(typeName, tokenName);
		}
		else {
			String tTokenType = getTypeOfToken(tokenName);
            return typeName.equals(tTokenType); // null guard
		}
	}
	
	public static boolean isTerminalNameType(String typeName) {
		return typeName.startsWith(terminalNameTypePrefix) && 
			   typeName.endsWith(terminalNameTypeSuffix);
	}
	
	public static String getTerminalNameTypeTokenName(String typeName) {
		return typeName.replace(terminalNameTypePrefix, "").replace(terminalNameTypeSuffix, "");
	}
	
	public boolean terminalNameTypeMatches(String terminalNameType, String tokenName) {
		String tTokenName = getTerminalNameTypeTokenName(terminalNameType);
				
		if (tTokenName.equals(tokenName)) {
			return true;
		}
		else {
			/* Look up the degeneracy table to find alternatives, e.g., "o" for "0"*/
			Set<String> altTokenNames = tokenDegen.getAlternatives(tokenName);
			
			if (altTokenNames != null) {
				return altTokenNames != null && altTokenNames.contains(tTokenName);
			}
			else {
				return false;
			}
		}
	}
	
	
	public boolean typeListContains(List<String> types, String tokenName) {
		Iterator<String> typesIt = types.iterator();		
		while (typesIt.hasNext()) {
			String tType = typesIt.next();
			
			if (isTerminalNameType(tType)) {
				if (terminalNameTypeMatches(tType, tokenName)) {
					return true;
				}
			}
		}
		
		String tTokenType = getTypeOfToken(tokenName);
		return types.contains(tTokenType);
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