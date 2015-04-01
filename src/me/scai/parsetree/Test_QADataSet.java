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
		correctParseRes  = t_correctParseRes;
	}
	
	public QADataEntry(final String t_tokenSetFileName, 
	                   final String t_correctParseRes, 
	                   final String t_correctMathTex) {
		tokenSetFileName = t_tokenSetFileName;
		correctParseRes  = t_correctParseRes;
		correctMathTex   = t_correctMathTex;
	}
}

public class Test_QADataSet {
	/* Member variables */
	QADataEntry [] entries = {
//			                  new QADataEntry("1",     "12",             "12"), 
//			                  new QADataEntry("2",     "236",            "236"), 
//			                  new QADataEntry("4",     "77",             "77"), 
//			                  new QADataEntry("6", 	   "36",             "36"), 
//			                  new QADataEntry("9",     "-28",            "-{28}"), 
//			                  new QADataEntry("10",    "(21 - 3)",       "{21}-{3}"), 
//			                  new QADataEntry("11",    "(21 + 3)",       "{21}+{3}"), 
//			                  new QADataEntry("12",    "(21 - 5)",       "{21}-{5}"), 
//			                  new QADataEntry("13",    "009",            "009"), 
//			                  new QADataEntry("14",    "900",            "900"), 
//			                  new QADataEntry("15",    "100",            "100"), 
//			                  new QADataEntry("18",    "(56 - 3)",       "{56}-{3}"), 
//			                  new QADataEntry("21",    "(29 / 3)",       "\\frac{29}{3}"), 
//			                  new QADataEntry("22",    "--3",            "-{-{3}}"), 
//			                  new QADataEntry("23",    "(9 ^ 3)",        "{9}^{3}"), 
//			                  new QADataEntry("24",    "(2 ^ -3)",       "{2}^{-{3}}"), 		/* Error due to geometric imprecision? */
//			                  new QADataEntry("103",   "(68 ^ 75)",      "{68}^{75}"), 
//			                  new QADataEntry("104",     "(2 ^ 34)",       "{2}^{34}"), 
//			                  new QADataEntry("106",     "(258 ^ 76)",     "{258}^{76}"), 
//			                  new QADataEntry("107",     "(256 ^ 481)",    "{256}^{481}"), 
//			                  new QADataEntry("108",     "(289 ^ 643)",    "{289}^{643}"), 
//			                  new QADataEntry("27",      "(5 / 8)",        "\\frac{5}{8}"),
//			                  new QADataEntry("28",      "((5 / 8) / 9)",  "\\frac{\\frac{5}{8}}{9}"), 
//			                  new QADataEntry("29",      "(3 / (2 / 7))",  "\\frac{3}{\\frac{2}{7}}"), 
//			                  new QADataEntry("32",      "(1 - (2 / 3))",  "{1}-{\\frac{2}{3}}"), 
//			                  new QADataEntry("34",      "(4 / (5 + (2 / 3)))",  "\\frac{4}{{5}+{\\frac{2}{3}}}"), 
//			                  new QADataEntry("36",      "(23 / 4)",             "\\frac{23}{4}"), 
//			                  new QADataEntry("37",      "((5 + 9) / ((3 / 2) - 1))", "\\frac{{5}+{9}}{{\\frac{3}{2}}-{1}}"), 
//			                  new QADataEntry("41",      "((4 - 2) / 3)",  "\\frac{{4}-{2}}{3}"),
//			                  new QADataEntry("42",       "((7 - 8) / 10)",  "\\frac{{7}-{8}}{10}"),
//			                  new QADataEntry("43",       "((3 + 1) / 4)",   "\\frac{{3}+{1}}{4}"), 
//			                  new QADataEntry("44",       "(72 / 3)",        "\\frac{72}{3}"), 
//			                  new QADataEntry("45",       "((8 - 3) / 4)",   "\\frac{{8}-{3}}{4}"), 
//			                  new QADataEntry("48",       "8.3",             "8.3"), 
//			                  new QADataEntry("49",       "4.0",             "4.0"), 
//			                  new QADataEntry("50",       "0.01",            "0.01"), 
//			                  new QADataEntry("51",       "-53",             "-{53}"), 
//			                  new QADataEntry("52",       "-7.4",            "-{7.4}"), 
//			                  new QADataEntry("53",       "(8.1 / 0.9)",     "\\frac{8.1}{0.9}"), 
//			                  new QADataEntry("54",       "(-1 / -3.2)",     "\\frac{-{1}}{-{3.2}}"), 
//			                  new QADataEntry("55",       "(-4.2 / (7 + 3))",  "\\frac{-{4.2}}{{7}+{3}}"), 
//			                  new QADataEntry("56",       "(5 * 3)",         "{5}\\ast{3}"), 
//			                  new QADataEntry("57",       "(3 * 4)",         "{3}\\ast{4}"), 
//			                  new QADataEntry("58",       "(-2 * 8)",        "{-{2}}\\ast{8}"), 
//			                  new QADataEntry("59",       "(2 * -3)",        "{2}\\ast{-{3}}"), 
//			                  new QADataEntry("60",       "(2 * +3)",        "{2}\\ast{+{3}}"), 
//			                  new QADataEntry("67",       "2",               "2"), 
//			                  new QADataEntry("68",       "0",               "0"), 
//							  new QADataEntry("69",       "1.20",            "1.20"), 
//							  new QADataEntry("70",       "0.02",            "0.02"), 
//							  new QADataEntry("72",       "-1",              "-{1}"), 
//							  new QADataEntry("73",       "-1.2",            "-{1.2}"), 
//							  new QADataEntry("74",       "-0.11",           "-{0.11}"), 
//							  new QADataEntry("75",       "-12",             "-{12}"), 
//							  new QADataEntry("76",       "-13.9",           "-{13.9}"), 
//							  new QADataEntry("83",       "(0 + 0)",         "{0}+{0}"), 
//							  new QADataEntry("84",       "(1.3 + 4)",       "{1.3}+{4}"), 
//							  new QADataEntry("85",       "(4 + 2.1)",       "{4}+{2.1}"), 
//							  new QADataEntry("86",       "(2.0 + 1.1)",     "{2.0}+{1.1}"), 
//							  new QADataEntry("88",       "(-1 + -3)",       "{-{1}}+{-{3}}"),  
//						      new QADataEntry("89",       "(-3.0 + -1)",     "{-{3.0}}+{-{1}}"), 
//						      new QADataEntry("90",       "((1 + 2) + 3)",   "{{1}+{2}}+{3}"), 
//						      new QADataEntry("91",       "((2 - 3) - 4)"), 
//						      new QADataEntry("100",      "-3"), 
//						      new QADataEntry("101",      "+3"),
//						      new QADataEntry("110",      "((4 + 3) + 8)"),			/* AssocLeft3B */
//						      new QADataEntry("111",      "((8 - 5) + 2)"),			/* AssocLeft3B */
//						      new QADataEntry("112",      "((4 + 48) - 5)"), 		/* AssocLeft3B */
//						      new QADataEntry("113",      "((4 - 8) + 5)"), 		/* AssocLeft3B */
//						      new QADataEntry("114",      "(2 ^ (3 ^ 4))"),			/* AssocRight2B */
//						      new QADataEntry("115",      "(0.5 ^ (2 ^ 3))"), 		/* AssocRight2B */
//						      //new QADataEntry("98",       TokenSetParser.errStr), 
//						      //new QADataEntry("99",       TokenSetParser.errStr), 
//						      new QADataEntry("98",       ParseTreeStringizer.parsingErrString), 
//						      new QADataEntry("99",       ParseTreeStringizer.parsingErrString),
//						      new QADataEntry("sim_1",    "((1 * 2) + (3 * 4))"), 				/* Add - multiplication precedence */
//						      new QADataEntry("sim_2",    "-(1 / 2)"),				/* Negative of high-level expressions */
//						      new QADataEntry("sim_3",    "-(23 / 4)"), 			/* Negative of high-level expressions */
//						      new QADataEntry("sim_4",    "((1 / 2) + (3 / 4))"),
//						      new QADataEntry("sim_5",    "((1 / 2) * (3 / 4))"), 	/* Multiplication of two fractions */
//						      new QADataEntry("sim_6",    "((1 * 10) + (2 * 3))"),
//						      new QADataEntry("sim_7",    "((1 + (2 * 3)) - 4)"),
//						      new QADataEntry("sim_8",    "((5 / 8) * (4 / 7))"),	/* Multiplication of two fractions */
//						      new QADataEntry("sim_9",    "((4 + ((2 * 3) * 5)) + 8)"),
////							  new QADataEntry("sim_10",   "((9 - (4 * 8)) + 2)"), 	/* Why does this token set cause error? */
//						      new QADataEntry("sim_11",   "((1 / 2) ^ 3)"), 		/* Exponentiation of a fraction */
//						      new QADataEntry("sim_12",   "((1 + (10 * 20)) + 3)"), 						      
//						      new QADataEntry("sim_13",   "(1 * (2 ^ 3))"), 
//						      new QADataEntry("sim_14",   "((4 ^ 5) * (2 ^ 3))"), 
//						      new QADataEntry("sim_15",   "((2 + ((3 ^ 4) * (2 ^ 3))) - 5)"),
//						      new QADataEntry("sim_16",   "((1 + ((2 * 3) * 4)) + 5)"), 
//						      new QADataEntry("sim_17",   "(1 + (((2 * 3) * 4) * 5))"), 
//						      new QADataEntry("sim_18",   "(((2 * 3) * 4) * 5)"), 
//						      new QADataEntry("sim_19",   "(2 * (3 / 4))"), 
//						      new QADataEntry("sim_20",   "((11 * 22) * 33)"),
//						      new QADataEntry("sim_21",   "((3 * (4 / 5)) * 2)"), 
//						      //new QADataEntry("sim_22",   "(((((23 / 45) * 7) * (15 / 26)) * 4) + (2 * 5))"), /* Causes hanging. TODO: Debug. */
//						      new QADataEntry("sim_23",   "(((12 / 13) * 5) + (28 * 3))"),
//						      new QADataEntry("sim_24",   "((1 + 2) / (3 * 4))"), 
//						      new QADataEntry("sim_25",   "((1 + 2))"), 
//						      new QADataEntry("sim_26",   "((2 + 3))"), 
//						      new QADataEntry("sim_27",   "((20 + 3))"), 
//						      new QADataEntry("sim_28",   "(((1 + 2) + 3))"), 
//						      new QADataEntry("sim_29",   "((1 - 2))"), 
//						      new QADataEntry("sim_30",   "((2 * 3))",              "\\left({2}\\times{3}\\right)"), 
//						      new QADataEntry("sim_31",   "((3 * 45))",             "\\left({3}\\ast{45}\\right)"), 
//						      new QADataEntry("sim_32",   "(((2 * 4)) ^ 6)",        "{\\left({2}\\times{4}\\right)}^{6}"), 
//						      new QADataEntry("sim_33",   "(12 * 34)",              "{12}\\ast{34}"),
//						      new QADataEntry("sim_34",   "(sqrt(16))",             "\\sqrt{16}"),
//						      new QADataEntry("sim_35",   "(sqrt(4))",              "\\sqrt{4}"),
//						      new QADataEntry("sim_36",   "(1 + (sqrt(243)))",      "{1}+{\\sqrt{243}}"),
//						      new QADataEntry("sim_37",   "(sqrt((5 - 3.8)))",      "\\sqrt{{5}-{3.8}}"),
//						      new QADataEntry("sim_38",   "(sqrt((3 / 5)))",        "\\sqrt{\\frac{3}{5}}"),
//						      new QADataEntry("sim_39",   "(2 + (sqrt((1 / 6))))",  "{2}+{\\sqrt{\\frac{1}{6}}}"),
//						      new QADataEntry("sim_40",   "(sqrt(((11 / 22) / 33)))", "\\sqrt{\\frac{\\frac{11}{22}}{33}}"),
//			                  new QADataEntry("sim_41",   "(sqrt((7 + (1 / 12))))",   "\\sqrt{{7}+{\\frac{1}{12}}}"),
//			                  new QADataEntry("sim_42",   "(sqrt(((3 - 4) + 5)))"), 
//			                  new QADataEntry("sim_43",   "((sqrt(2)) / 3)"),
//			                  new QADataEntry("sim_44",   "(sqrt((sqrt(2))))",        "\\sqrt{\\sqrt{2}}"),
//			                  new QADataEntry("sim_45",   "((sqrt((sqrt(21)))) / 8)", "\\frac{\\sqrt{\\sqrt{21}}}{8}"),
//			                  new QADataEntry("sim_46",   "(sqrt((1 + (sqrt(4)))))"),
//			                  new QADataEntry("sim_47",   "(sqrt(((sqrt(4)) / ((sqrt(9)) + (sqrt(16))))))"), 
//			                  new QADataEntry("sim_48",   "gr_al",                    "\\alpha"),
//			                  new QADataEntry("sim_49",   "(gr_al*gr_be)",            "{\\alpha}{\\beta}"),
//			                  new QADataEntry("sim_50",   "(A ^ B)",                  "{A}^{B}"),
//			                  new QADataEntry("sim_51",   "ln(3)",                    "\\ln{3}"),
//			                  new QADataEntry("sim_52",   "sin(7)",                    "\\sin{7}"),
//			                  new QADataEntry("sim_53",   "cos(G)",                    "\\cos{G}"),
//			                  new QADataEntry("sim_54",   "sin(gr_al)",                "\\sin{\\alpha}"),
//			                  new QADataEntry("sim_55",   "ln(2.7)",                   "\\ln{2.7}"),
//			                  new QADataEntry("sim_56",   "sin((5 / 3))",              "\\sin{\\frac{5}{3}}"),
//			                  new QADataEntry("sim_57",   "cos((gr_be ^ 2))",          "\\cos{{\\beta}^{2}}"),
//			                  new QADataEntry("sim_58",   "ln((sqrt(2)))",             "\\ln{\\sqrt{2}}"), 
//			                  new QADataEntry("sim_59",   "(sin(1) + 8)",              "{\\sin{1}}+{8}"),
//							  new QADataEntry("sim_60",   "(A + sin(3))",              "{A}+{\\sin{3}}"),
//							  new QADataEntry("sim_61",   "cos(sin(72))",              "\\cos{\\sin{72}}"),      // Nested function, 1 level
//							  new QADataEntry("sim_62",   "ln(sin(cos(0)))",           "\\ln{\\sin{\\cos{0}}}"), // Nested function, 2 levels
//							  new QADataEntry("sim_63",   "(sin(8) / 4)",              "\\frac{\\sin{8}}{4}"),
//							  new QADataEntry("sim_64",   "(sin(A) / cos(A))",         "\\frac{\\sin{A}}{\\cos{A}}"),
//							  new QADataEntry("sim_65",   "(sqrt(sin(A)))",             "\\sqrt{\\sin{A}}"), 
//							  new QADataEntry("sim_66",   "((sqrt(sin(A))) / cos(A))",  "\\frac{\\sqrt{\\sin{A}}}{\\cos{A}}"),
						      new QADataEntry("sim_67",   "cos(gr_al)",                 "\\cos{\\alpha}"),
			                  };
	/* ~Member variables */
}

