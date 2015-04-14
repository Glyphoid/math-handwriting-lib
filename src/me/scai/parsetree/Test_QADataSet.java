package me.scai.parsetree;

class QADataEntry {
	/* Member variables */
	public String tokenSetFileName;
	public String correctParseRes;
	public String correctMathTex;

	/* ~Member variables */

	/* Constructor */
	public QADataEntry(final String t_tokenSetFileName,
			final String t_correctParseRes) {
		tokenSetFileName = t_tokenSetFileName;
		correctParseRes = t_correctParseRes;
	}

	public QADataEntry(final String t_tokenSetFileName,
			final String t_correctParseRes, final String t_correctMathTex) {
		tokenSetFileName = t_tokenSetFileName;
		correctParseRes = t_correctParseRes;
		correctMathTex = t_correctMathTex;
	}
}

public class Test_QADataSet {
	/* Member variables */
	QADataEntry[] entries = {
			new QADataEntry("1", "12", "12"),
			new QADataEntry("2", "236", "236"),
			new QADataEntry("4", "77", "77"),
			new QADataEntry("6", "36", "36"),
			new QADataEntry("9", "-28", "-{28}"),
			new QADataEntry("10", "(21 - 3)", "{21}-{3}"),
			new QADataEntry("11", "(21 + 3)", "{21}+{3}"),
			new QADataEntry("12", "(21 - 5)", "{21}-{5}"),
			new QADataEntry("13", "009", "009"),
			new QADataEntry("14", "900", "900"),
			new QADataEntry("15", "100", "100"),
			new QADataEntry("18", "(56 - 3)", "{56}-{3}"),
			new QADataEntry("21", "(29 / 3)", "\\frac{29}{3}"),
			new QADataEntry("22", "--3", "-{-{3}}"),
			new QADataEntry("23", "(9 ^ 3)", "{9}^{3}"),
			new QADataEntry("24", "(2 ^ -3)", "{2}^{-{3}}"), /* Error due to geometric imprecision? */
			new QADataEntry("103", "(68 ^ 75)", "{68}^{75}"),
			new QADataEntry("104", "(2 ^ 34)", "{2}^{34}"),
			new QADataEntry("106", "(258 ^ 76)", "{258}^{76}"),
			new QADataEntry("107", "(256 ^ 481)", "{256}^{481}"),
			new QADataEntry("108", "(289 ^ 643)", "{289}^{643}"),
			new QADataEntry("27", "(5 / 8)", "\\frac{5}{8}"),
			new QADataEntry("28", "((5 / 8) / 9)", "\\frac{\\frac{5}{8}}{9}"),
			new QADataEntry("29", "(3 / (2 / 7))", "\\frac{3}{\\frac{2}{7}}"),
			new QADataEntry("32", "(1 - (2 / 3))", "{1}-{\\frac{2}{3}}"),
			new QADataEntry("34", "(4 / (5 + (2 / 3)))", "\\frac{4}{{5}+{\\frac{2}{3}}}"),
			new QADataEntry("36", "(23 / 4)", "\\frac{23}{4}"),
			new QADataEntry("37", "((5 + 9) / ((3 / 2) - 1))", "\\frac{{5}+{9}}{{\\frac{3}{2}}-{1}}"),
			new QADataEntry("41", "((4 - 2) / 3)", "\\frac{{4}-{2}}{3}"),
			new QADataEntry("42", "((7 - 8) / 10)", "\\frac{{7}-{8}}{10}"),
			new QADataEntry("43", "((3 + 1) / 4)", "\\frac{{3}+{1}}{4}"),
			new QADataEntry("44", "(72 / 3)", "\\frac{72}{3}"),
			new QADataEntry("45", "((8 - 3) / 4)", "\\frac{{8}-{3}}{4}"),
			new QADataEntry("48", "8.3", "8.3"),
			new QADataEntry("49", "4.0", "4.0"),
			new QADataEntry("50", "0.01", "0.01"),
			new QADataEntry("51", "-53", "-{53}"),
			new QADataEntry("52", "-7.4", "-{7.4}"),
			new QADataEntry("53", "(8.1 / 0.9)", "\\frac{8.1}{0.9}"),
			new QADataEntry("54", "(-1 / -3.2)", "\\frac{-{1}}{-{3.2}}"),
			new QADataEntry("55", "(-4.2 / (7 + 3))", "\\frac{-{4.2}}{{7}+{3}}"),
			new QADataEntry("56", "(5 * 3)", "{5}\\ast{3}"),
			new QADataEntry("57", "(3 * 4)", "{3}\\ast{4}"),
			new QADataEntry("58", "(-2 * 8)", "{-{2}}\\ast{8}"),
			new QADataEntry("59", "(2 * -3)", "{2}\\ast{-{3}}"),
			new QADataEntry("60", "(2 * +3)", "{2}\\ast{+{3}}"),
			new QADataEntry("67", "2", "2"),
			new QADataEntry("68", "0", "0"),
			new QADataEntry("69", "1.20", "1.20"),
			new QADataEntry("70", "0.02", "0.02"),
			new QADataEntry("72", "-1", "-{1}"),
			new QADataEntry("73", "-1.2", "-{1.2}"),
			new QADataEntry("74", "-0.11", "-{0.11}"),
			new QADataEntry("75", "-12", "-{12}"),
			new QADataEntry("76", "-13.9", "-{13.9}"),
			new QADataEntry("83", "(0 + 0)", "{0}+{0}"),
			new QADataEntry("84", "(1.3 + 4)", "{1.3}+{4}"),
			new QADataEntry("85", "(4 + 2.1)", "{4}+{2.1}"),
			new QADataEntry("86", "(2.0 + 1.1)", "{2.0}+{1.1}"),
			new QADataEntry("88", "(-1 + -3)", "{-{1}}+{-{3}}"),
			new QADataEntry("89", "(-3.0 + -1)", "{-{3.0}}+{-{1}}"),
			new QADataEntry("90", "((1 + 2) + 3)", "{{1}+{2}}+{3}"),
			new QADataEntry("91", "((2 - 3) - 4)"),
			new QADataEntry("100", "-3", "-{3}"),
			new QADataEntry("101", "+3", "+{3}"),
			new QADataEntry("110", "((4 + 3) + 8)"), /* AssocLeft3B */
			new QADataEntry("111", "((8 - 5) + 2)"), /* AssocLeft3B */
			new QADataEntry("112", "((4 + 48) - 5)"), /* AssocLeft3B */
			new QADataEntry("113", "((4 - 8) + 5)"), /* AssocLeft3B */
			new QADataEntry("114", "(2 ^ (3 ^ 4))"), /* AssocRight2B */
			new QADataEntry("115", "(0.5 ^ (2 ^ 3))"), /* AssocRight2B */
			new QADataEntry("98", ParseTreeStringizer.parsingErrString),
			new QADataEntry("99", ParseTreeStringizer.parsingErrString),
			new QADataEntry("sim_1", "((1 * 2) + (3 * 4))", "{{1}\\ast{2}}+{{3}\\ast{4}}"), /* Add - multiplication precedence */
			new QADataEntry("sim_2", "-(1 / 2)", "-\\frac{1}{2}"), /* Negative of high-level expressions */
			new QADataEntry("sim_3", "-(23 / 4)", "-\\frac{23}{4}"), /* Negative of high-level expressions */
			new QADataEntry("sim_4", "((1 / 2) + (3 / 4))", "{\\frac{1}{2}}+{\\frac{3}{4}}"),
			new QADataEntry("sim_5", "((1 / 2) * (3 / 4))", "{\\frac{1}{2}}\\ast{\\frac{3}{4}}"), /* Multiplication of two fractions */
			new QADataEntry("sim_6", "((1 * 10) + (2 * 3))"),
			new QADataEntry("sim_7", "((1 + (2 * 3)) - 4)"),
			new QADataEntry("sim_8", "((5 / 8) * (4 / 7))"), /* Multiplication of two fractions */
			new QADataEntry("sim_9", "((4 + ((2 * 3) * 5)) + 8)"),
			// new QADataEntry("sim_10", "((9 - (4 * 8)) + 2)"), /* Why does this token set cause error? */
			new QADataEntry("sim_11", "((1 / 2) ^ 3)"), /* Exponentiation of a fraction */
			new QADataEntry("sim_12", "((1 + (10 * 20)) + 3)"),
			new QADataEntry("sim_13", "(1 * (2 ^ 3))"),
			new QADataEntry("sim_14", "((4 ^ 5) * (2 ^ 3))"),
			new QADataEntry("sim_15", "((2 + ((3 ^ 4) * (2 ^ 3))) - 5)"),
			new QADataEntry("sim_16", "((1 + ((2 * 3) * 4)) + 5)"),
			new QADataEntry("sim_17", "(1 + (((2 * 3) * 4) * 5))"),
			new QADataEntry("sim_18", "(((2 * 3) * 4) * 5)"),
			new QADataEntry("sim_19", "(2 * (3 / 4))"),
			new QADataEntry("sim_20", "((11 * 22) * 33)"),
			new QADataEntry("sim_21", "((3 * (4 / 5)) * 2)"),
			// new QADataEntry("sim_22", "((((23 / 45) * 7) * (15 / 26)) * 4) + (2 * 5))"), /* Causes hanging. TODO: Debug. */
			new QADataEntry("sim_23", "(((12 / 13) * 5) + (28 * 3))"),
			new QADataEntry("sim_24", "((1 + 2) / (3 * 4))"),
			new QADataEntry("sim_25", "((1 + 2))", "\\left({1}+{2}\\right)"),
			new QADataEntry("sim_26", "((2 + 3))", "\\left({2}+{3}\\right)"),
			new QADataEntry("sim_27", "((20 + 3))", "\\left({20}+{3}\\right)"),
			new QADataEntry("sim_28", "(((1 + 2) + 3))"),
			new QADataEntry("sim_29", "((1 - 2))", "\\left({1}-{2}\\right)"),
			new QADataEntry("sim_68", "((a ^ 2)*b)", "{{a}^{2}}{b}"), /* Variable exponent and multiplication */
			// new QADataEntry("sim_69", "(a*(b ^ 2))", "{a}{{b}^{2}}"), //TODO
			new QADataEntry("sim_30", "((2 * 3))", "\\left({2}\\times{3}\\right)"),
			new QADataEntry("sim_31", "((3 * 45))", "\\left({3}\\ast{45}\\right)"),
			new QADataEntry("sim_32", "(((2 * 4)) ^ 6)", "{\\left({2}\\times{4}\\right)}^{6}"),
			new QADataEntry("sim_33", "(12 * 34)", "{12}\\ast{34}"),
			new QADataEntry("sim_34", "(sqrt(16))", "\\sqrt{16}"),
			new QADataEntry("sim_35", "(sqrt(4))", "\\sqrt{4}"),
			new QADataEntry("sim_36", "(1 + (sqrt(243)))", "{1}+{\\sqrt{243}}"),
			new QADataEntry("sim_37", "(sqrt((5 - 3.8)))", "\\sqrt{{5}-{3.8}}"),
			new QADataEntry("sim_38", "(sqrt((3 / 5)))", "\\sqrt{\\frac{3}{5}}"),
			new QADataEntry("sim_39", "(2 + (sqrt((1 / 6))))", "{2}+{\\sqrt{\\frac{1}{6}}}"),
			new QADataEntry("sim_40", "(sqrt(((11 / 22) / 33)))", "\\sqrt{\\frac{\\frac{11}{22}}{33}}"),
			new QADataEntry("sim_41", "(sqrt((7 + (1 / 12))))", "\\sqrt{{7}+{\\frac{1}{12}}}"),
			new QADataEntry("sim_42", "(sqrt(((3 - 4) + 5)))"),
			new QADataEntry("sim_43", "((sqrt(2)) / 3)", "\\frac{\\sqrt{2}}{3}"),
			new QADataEntry("sim_44", "(sqrt((sqrt(2))))", "\\sqrt{\\sqrt{2}}"),
			new QADataEntry("sim_45", "((sqrt((sqrt(21)))) / 8)", "\\frac{\\sqrt{\\sqrt{21}}}{8}"),
			new QADataEntry("sim_46", "(sqrt((1 + (sqrt(4)))))", "\\sqrt{{1}+{\\sqrt{4}}}"),
			new QADataEntry("sim_47", "(sqrt(((sqrt(4)) / ((sqrt(9)) + (sqrt(16))))))"),
			new QADataEntry("sim_48", "gr_al", "\\alpha"),
			new QADataEntry("sim_49", "(gr_al*gr_be)", "{\\alpha}{\\beta}"),
			new QADataEntry("sim_50", "(A ^ B)", "{A}^{B}"),
			new QADataEntry("sim_51", "ln(3)", "\\ln{3}"),
			new QADataEntry("sim_52", "sin(7)", "\\sin{7}"),
			new QADataEntry("sim_53", "cos(G)", "\\cos{G}"),
			new QADataEntry("sim_54", "sin(gr_al)", "\\sin{\\alpha}"),
			new QADataEntry("sim_55", "ln(2.7)", "\\ln{2.7}"),
			new QADataEntry("sim_56", "sin((5 / 3))", "\\sin{\\frac{5}{3}}"),
			new QADataEntry("sim_57", "cos((gr_be ^ 2))", "\\cos{{\\beta}^{2}}"),
			new QADataEntry("sim_58", "ln((sqrt(2)))", "\\ln{\\sqrt{2}}"),
			new QADataEntry("sim_59", "(sin(1) + 8)", "{\\sin{1}}+{8}"),
			new QADataEntry("sim_60", "(A + sin(3))", "{A}+{\\sin{3}}"),
			new QADataEntry("sim_61", "cos(sin(72))", "\\cos{\\sin{72}}"), // Nested function, 1 level
			new QADataEntry("sim_62", "ln(sin(cos(0)))", "\\ln{\\sin{\\cos{0}}}"), // Nested function, 2 levels
			new QADataEntry("sim_63", "(sin(8) / 4)", "\\frac{\\sin{8}}{4}"),
			new QADataEntry("sim_64", "(sin(A) / cos(A))", "\\frac{\\sin{A}}{\\cos{A}}"),
			new QADataEntry("sim_65", "(sqrt(sin(A)))", "\\sqrt{\\sin{A}}"),
			new QADataEntry("sim_66", "((sqrt(sin(A))) / cos(A))", "\\frac{\\sqrt{\\sin{A}}}{\\cos{A}}"),
			new QADataEntry("sim_67", "cos(gr_al)", "\\cos{\\alpha}"),
			new QADataEntry("sim_70", "[3; 4]", "\\begin{bmatrix}3\\\\4\\end{bmatrix}"),                         // Column vector
			new QADataEntry("sim_71", "[sin(A); cos(A)]", "\\begin{bmatrix}\\sin{A}\\\\\\cos{A}\\end{bmatrix}"), // Column vector
			new QADataEntry("sim_72", "[2, 3]",    "\\begin{bmatrix}2&3\\end{bmatrix}"),     // Row vector
			new QADataEntry("sim_73", "[1, 2, 3]", "\\begin{bmatrix}1&2&3\\end{bmatrix}"),   // Row vector
			new QADataEntry("sim_74", "[12, 34]",  "\\begin{bmatrix}12&34\\end{bmatrix}"),   // Row vector
			new QADataEntry("sim_75", "[(1 / 2), (3 / 5)]",   "\\begin{bmatrix}\\frac{1}{2}&\\frac{3}{5}\\end{bmatrix}"), // Row vector with fractions
			new QADataEntry("sim_76", "[(3 ^ 4), 5]",         "\\begin{bmatrix}{3}^{4}&5\\end{bmatrix}"),                 // Row vector with exponentiation
			new QADataEntry("sim_77", "[a, b]",               "\\begin{bmatrix}a&b\\end{bmatrix}"),                       // Row vector with symbols
			new QADataEntry("sim_78", "[(a*b)]",              "\\begin{bmatrix}{a}{b}\\end{bmatrix}"),                    // Row vector with symbols
			new QADataEntry("sim_79", "[(a*b), c, (x*y)]",    "\\begin{bmatrix}{a}{b}&c&{x}{y}\\end{bmatrix}"),           // Row vector with symbols
			new QADataEntry("sim_80", "[1, 2; 3, 4]",         "\\begin{bmatrix}1&2\\\\3&4\\end{bmatrix}"),                // Matrix: 2x2
			new QADataEntry("sim_81", "[(a*b), c; x, (y*z)]", "\\begin{bmatrix}{a}{b}&c\\\\x&{y}{z}\\end{bmatrix}"),      // Matrix: 2x2, with symbols
			new QADataEntry("sim_82", "[(a*b), c; x, (y*u)]", "\\begin{bmatrix}{a}{b}&c\\\\x&{y}{u}\\end{bmatrix}"),      // Matrix: 2x2, with symbols
			new QADataEntry("sim_83", "[(1 / 2), 3; 4, 9]",   "\\begin{bmatrix}\\frac{1}{2}&3\\\\4&9\\end{bmatrix}"),     // Matrix: 2x2, with symbols and fraction
			new QADataEntry("sim_84", "[(a ^ 2), b; 0, (a ^ 3)]",    "\\begin{bmatrix}{a}^{2}&b\\\\0&{a}^{3}\\end{bmatrix}"),  // Matrix: 2x2, with symbols and fraction
			new QADataEntry("sim_85", "[2, 4, 6; 1, 3, 5; 0, 7, 9]", "\\begin{bmatrix}2&4&6\\\\1&3&5\\\\0&7&9\\end{bmatrix}"), // Matrix: 3x3, with symbols and fraction
			new QADataEntry("sim_86", "ln(88)", "\\ln{\\left(88\\right)}"),
			new QADataEntry("sim_87", "sin(((2 + B)))", "\\sin{\\left({2}+{B}\\right)}"),
			new QADataEntry("sim_88", "sin((sqrt(A)))", "\\sin{\\left(\\sqrt{A}\\right)}"),
			new QADataEntry("sim_89", "(sin(A) + cos(B))", "{\\sin{\\left(A\\right)}}+{\\cos{\\left(B\\right)}}"),
			new QADataEntry("sim_90", "(2*sin(gr_al))", "{2}{\\sin{\\left(\\alpha\\right)}}"),
			// new QADataEntry("sim_91", "(sin(A)*cos(B))", "{\\sin{\\left(A\\right)}}{\\cos{\\left(B\\right)}}"), //TODO
			new QADataEntry("sim_92", "[(2 ^ 3); (3 ^ 4)]", "\\begin{bmatrix}{2}^{3}\\\\{3}^{4}\\end{bmatrix}"),
			new QADataEntry("sim_93", "[(A ^ 2); (B ^ 3)]", "\\begin{bmatrix}{A}^{2}\\\\{B}^{3}\\end{bmatrix}"),
			// new QADataEntry("sim_94", "[(x ^ 2); (y ^ 3)]"), //TODO
			new QADataEntry("sim_95", "(A = [1, 2; 3, 4])", "{A}={\\begin{bmatrix}1&2\\\\3&4\\end{bmatrix}}"),  // Assignment of matrix value
			new QADataEntry("sim_96", "(B = 2)", "{B}={2}"), // Assignment of double value
			new QADataEntry("sim_97", "A", "A"),    // STATEFUL EVAL
			new QADataEntry("sim_98", "(B + A)", "{B}+{A}"),   // STATEFUL EVAL
			new QADataEntry("sim_99", "(A + B)", "{A}+{B}"),   // STATEFUL EVAL
			new QADataEntry("sim_100", "((A + B) + A)", "{{A}+{B}}+{A}"),   // STATEFUL EVAL
			new QADataEntry("sim_101", "(3*A)", "{3}{A}"),    // STATEFUL EVAL
			new QADataEntry("sim_102", "(A - B)", "{A}-{B}"),  // STATEFUL EVAL
			new QADataEntry("sim_103", "(B - A)", "{B}-{A}"),  // STATEFUL EVAL
			new QADataEntry("sim_104", "(A*B)", "{A}{B}"),    // STATEFUL EVAL
			new QADataEntry("sim_105", "det(A)", "\\det{A}"),   // STATEFUL EVAL
			new QADataEntry("sim_106", "det(A)", "\\det{\\left(A\\right)}"),   // STATEFUL EVAL
			new QADataEntry("sim_107", "det(((A + B)))", "\\det{\\left({A}+{B}\\right)}"),   // STATEFUL EVAL
			new QADataEntry("sim_108", "rank(A)", "\\rank{A}"),   // STATEFUL EVAL
			new QADataEntry("sim_109", "(f(x) = (2*x))", "{f{\\left(x\\right)}}={{2}{x}}"), // Function definition
			new QADataEntry("sim_110", "(g(y) = 1)", "{g{\\left(y\\right)}}={1}"),     // Function definition
			new QADataEntry("sim_111", "(h(x, y) = (x*y))", "{h{\\left(x,y\\right)}}={{x}{y}}"), // Function definition
			new QADataEntry("sim_112", "f(3)", "f{\\left(3\\right)}"),             // Evaluation of custom function
			new QADataEntry("sim_115", "f(27)", "f{\\left(27\\right)}"),             // Evaluation of custom function
			new QADataEntry("sim_113", "f((3 + 2))", "f{\\left({3}+{2}\\right)}"), // Evaluation of custom function
			new QADataEntry("sim_114", "f(-0.5)", "f{\\left(-{0.5}\\right)}"),      // Evaluation of custom function
			new QADataEntry("sim_116", "f((sqrt(3)))", "f{\\left(\\sqrt{3}\\right)}"),             // Evaluation of custom function
			new QADataEntry("sim_117", "(f(4) + 2)", "{f{\\left(4\\right)}}+{2}"),  // Custom function term as level-1 expression
			new QADataEntry("sim_118", "(1 / f((2 + 3)))", "\\frac{1}{f{\\left({2}+{3}\\right)}}"),  // Custom function term as level-1 expression
			new QADataEntry("sim_119", "(3*f(9))", "{3}{f{\\left(9\\right)}}"),  					 // Custom function term as level-1 expression
			new QADataEntry("sim_120", "(f(1)*g(0))", "{f{\\left(1\\right)}}{g{\\left(0\\right)}}"), // Custom function term as level-1 expression
			new QADataEntry("sim_121", "((2*f(3)) + (4*g(5)))", "{{2}{f{\\left(3\\right)}}}+{{4}{g{\\left(5\\right)}}}"), // Custom function term as level-1 expression
			new QADataEntry("sim_124", "Sigma((i = 1) : (8))(i)", "\\sum\\limits_{{i}={1}}^{8}{i}"),     // Sigma sum term // TODO: Full sigma term
			new QADataEntry("sim_125", "Sigma((n = -2) : ((sqrt(4))))((n ^ 2))", "\\sum\\limits_{{n}={-{2}}}^{\\sqrt{4}}{{n}^{2}}"),     // Sigma sum term // TODO: Full sigma term
	};
	/* ~Member variables */
}


