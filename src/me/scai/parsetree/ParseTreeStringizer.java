package me.scai.parsetree;

public class ParseTreeStringizer {
	public final static String parsingErrString = "[Parsing failed: Syntax error]";
	
	/* Input: n: root of the parse tree */
	/* Currently based on recursion. */
	public static String stringize(Node n) {
		if ( n == null )
			return parsingErrString;
					
			String s = null;
			
		if ( n.prodSumString.equals("DIGIT_STRING --> DIGIT DIGIT_STRING") )
			s = n.ch[0].termName + stringize(n.ch[1]);
		else if ( n.prodSumString.equals("DIGIT_STRING --> DIGIT") )
			s = n.ch[0].termName;
		else if ( n.prodSumString.equals("DECIMAL_NUMBER --> POINT DIGIT_STRING DIGIT_STRING") )
			s = stringize(n.ch[1]) + n.ch[0].termName + stringize(n.ch[2]);
		else if ( n.prodSumString.equals("DECIMAL_NUMBER --> MINUS_OP DECIMAL_NUMBER") )
			s = "-" +  stringize(n.ch[1]);
		else if ( n.prodSumString.equals("EXPR_LV1 --> EXPONENTIATION") ) 
			s = stringize(n.ch[0]);
		else if ( n.prodSumString.equals("EXPONENTIATION --> EXPR_LV2 EXPR_LV2") )
			s = "(" + stringize(n.ch[0]) + " ^ " + stringize(n.ch[1]) + ")";
		else if ( n.prodSumString.equals("EXPR_LV1 --> DECIMAL_NUMBER") )
			s = stringize(n.ch[0]);
		else if ( n.prodSumString.equals("EXPR_LV1 --> ADDITION") )
			s = stringize(n.ch[0]);
		else if ( n.prodSumString.equals("EXPR_LV1 --> SUBTRACTION") )
			s = stringize(n.ch[0]);
		else if ( n.prodSumString.equals("EXPR_LV1 --> MULTIPLICATION") )
			s = stringize(n.ch[0]);
		else if ( n.prodSumString.equals("ADDITION --> PLUS_OP EXPR_LV2 EXPR_LV2") )
			s = "(" + stringize(n.ch[1]) + " + " + stringize(n.ch[2]) + ")"; /* Order??? */
		else if ( n.prodSumString.equals("SUBTRACTION --> MINUS_OP EXPR_LV2 EXPR_LV2") )
			s = "(" + stringize(n.ch[1]) + " - " + stringize(n.ch[2]) + ")"; /* Order??? */
		else if ( n.prodSumString.equals("MULTIPLICATION --> MULT_OP EXPR_LV2 EXPR_LV2") )
			s = "(" + stringize(n.ch[1]) + " * " + stringize(n.ch[2]) + ")"; /* Order??? */
		else if ( n.prodSumString.equals("DECIMAL_NUMBER --> DIGIT_STRING") )
			s = stringize(n.ch[0]);			
		else if ( n.prodSumString.equals("EXPR_LV2 --> EXPR_LV1") )		/* ? */
			s = stringize(n.ch[0]);
		else if ( n.prodSumString.equals("EXPR_LV2 --> MINUS_OP EXPR_LV2") )
			s = "-" + stringize(n.ch[1]);
		else if ( n.prodSumString.equals("EXPR_LV2 --> PLUS_OP EXPR_LV2") )
			s = "+" + stringize(n.ch[1]);
		else if ( n.prodSumString.equals("EXPR_LV2 --> FRACTION") )
			s = stringize(n.ch[0]);
		else if ( n.prodSumString.equals("FRACTION --> MINUS_OP EXPR_LV2 EXPR_LV2") )
			s = "(" + stringize(n.ch[1]) + " / " + stringize(n.ch[2]) + ")"; /* Order??? */
		else if ( n.prodSumString.equals("ROOT --> EXPR_LV2") )
			s = stringize(n.ch[0]);
		else
			throw new RuntimeException("stringize(): unrecognized production summary string: " + n.prodSumString);
		
		return s;
	}
	
}
