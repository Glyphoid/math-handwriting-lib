package me.scai.parsetree;

public class ParseTreeStringizer {
	/* Input: n: root of the parse tree */
	/* Currently based on recursion. */
	public static String stringize(Node n) {
		String s = null;
		
		switch ( n.prodSumString ) {
			case "DIGIT_STRING --> DIGIT DIGIT_STRING":
				s = n.ch[0].termName + stringize(n.ch[1]);
				break;
			case "DIGIT_STRING --> DIGIT EPS":
				s = n.ch[0].termName;
				break;			
			case "DECIMAL_NUMBER --> POINT DIGIT_STRING DIGIT_STRING":
				s = stringize(n.ch[1]) + n.ch[0].termName + stringize(n.ch[2]);
				break;
			case "FRACTION --> MINUS_OP DECIMAL_NUMBER DECIMAL_NUMBER":
				s = "(" + stringize(n.ch[1]) + " / " + stringize(n.ch[2]) + ")"; /* Order??? */
				break;
			case "DECIMAL_NUMBER --> MINUS_OP DECIMAL_NUMBER":
				s = "-" +  stringize(n.ch[1]);
				break;
			case "SUBTRACTION --> MINUS_OP DECIMAL_NUMBER DECIMAL_NUMBER":
				s = "(" + stringize(n.ch[1]) + " - " + stringize(n.ch[2]) + ")"; /* Order??? */
				break;
			default:
				System.err.println("stringize(): unrecognized production summary string: " + n.prodSumString);
				break;
		}
		
		return s;
	}
	
}
