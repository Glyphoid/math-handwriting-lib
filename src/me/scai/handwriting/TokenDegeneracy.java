package me.scai.handwriting;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TokenDegeneracy {
//	private static final String RESOURCES_DIR = "resources";
//	private static final String RESOURCES_CONFIG_DIR = "config";
//	private static final String TOKEN_DEGENERACY_CONFIG_FN = "token_degeneracy.json";	                                                         
	
	private static final Gson gson = new Gson();
	
	private Map<String, String> tab = new HashMap<>();	
	private Map<String, Set<String> > altMap = new HashMap<>(); /* Full map of alternatives */
	
	/* Constructor */
	public TokenDegeneracy(JsonObject obj) {
		System.out.println("TokenDegeneracy constructor: obj = " + obj); //DEBUG
		for (Map.Entry<String, JsonElement> kv : obj.entrySet()) {
			String tokenName = kv.getKey();
			String substituteTokenName = kv.getValue().getAsString();
			
			if (tokenName.equals(substituteTokenName)) {
				throw new RuntimeException("The token name \"" + tokenName + "\" and its alternative \"" + substituteTokenName + "\" are identical, which is not allowed in a token degeneracy table");
			}
			
			tab.put(tokenName, substituteTokenName);
		}
		
		prepareAlternatives();
	}
	
	/* Get the degenerated token name */
	public String getDegenerated(String tokenName) {
		String degen = tab.get(tokenName);
		
		if (degen != null) {
			System.out.println("\"" + tokenName + "\" --> \"" + degen + "\"");
		}
	
		return (degen == null) ? tokenName : degen;
	}
	
	/* Prepare the full map of alternatives */
	private void prepareAlternatives() {
		for (Map.Entry<String, String> entry : tab.entrySet()) {
			String t0 = entry.getKey();
			String t1 = entry.getValue();
			
			if (!altMap.containsKey(t0)) {
				altMap.put(t0, new HashSet<String>());
			}
			altMap.get(t0).add(t1);
			
			if (!altMap.containsKey(t1)) {
				altMap.put(t1, new HashSet<String>());				
			}
			altMap.get(t1).add(t0);
		}
	}
	
	/* Get all alternatives of a token */
	public Set<String> getAlternatives(String token) {
		return altMap.get(token);
	}
}
