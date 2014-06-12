package me.scai.parsetree;

class QADataEntry {
	/* Member variables */
	public String tokenSetFileName;
	public String correctParseRes;
	/* ~Member variables */
	
	/* Constructor */
	public QADataEntry(final String t_tokenSetFileName, 
			           final String t_correctParseRes) {
		tokenSetFileName = t_tokenSetFileName;
		correctParseRes = t_correctParseRes;
	}
}

public class QADataSet {
	/* Member variables */
	QADataEntry [] entries = {new QADataEntry("1",     "12"), 
			                  new QADataEntry("2",     "236"), 
			                  new QADataEntry("4",     "77"), 
			                  new QADataEntry("6", 	   "36"), 
			                  new QADataEntry("9",     "-28"), 
			                  new QADataEntry("10",    "(21 - 3)"), 
			                  new QADataEntry("11",    "(21 + 3)"), 
			                  new QADataEntry("12",    "(21 - 5)"), 
			                  new QADataEntry("13",    "009"), 
			                  new QADataEntry("14",    "900"), 
			                  new QADataEntry("15",    "100"), 
			                  new QADataEntry("18",    "(56 - 3)"), 
			                  new QADataEntry("21",    "(29 / 3)"), 
			                  new QADataEntry("22",    "--3"), 
			                  new QADataEntry("23",    "(9 ^ 3)"), 
			                  new QADataEntry("24",    "(2 ^ -3)"), 
			                  new QADataEntry("103",   "(68 ^ 75)"), 
			                  new QADataEntry("104",      "(2 ^ 34)"), 
			                  new QADataEntry("106",      "(258 ^ 76)"), 
			                  new QADataEntry("107",      "(256 ^ 481)"), 
			                  new QADataEntry("108",      "(289 ^ 643)"), 
			                  new QADataEntry("27",      "(5 / 8)"), 
			                  new QADataEntry("28",      "((5 / 8) / 9)"), 
			                  new QADataEntry("29",      "(3 / (2 / 7))"), 
			                  new QADataEntry("32",      "(1 - (2 / 3))"), 
			                  new QADataEntry("34",      "(4 / (5 + (2 / 3)))"), 
			                  new QADataEntry("36",      "(23 / 4)"), 
			                  new QADataEntry("37",      "((5 + 9) / ((3 / 2) - 1))"), 
			                  new QADataEntry("41",      "((4 - 2) / 3)"),
			                  new QADataEntry("42",       "((7 - 8) / 10)"),
			                  new QADataEntry("43",       "((3 + 1) / 4)"), 
			                  new QADataEntry("44",       "(72 / 3)"), 
			                  new QADataEntry("45",       "((8 - 3) / 4)"), 
			                  new QADataEntry("48",       "8.3"), 
			                  new QADataEntry("49",       "4.0"), 
			                  new QADataEntry("50",       "0.01"), 
			                  new QADataEntry("51",       "-53"), 
			                  new QADataEntry("52",       "-7.4"), 
			                  new QADataEntry("53",       "(8.1 / 0.9)"), 
			                  new QADataEntry("54",       "(-1 / -3.2)"), 
			                  new QADataEntry("55",       "(-4.2 / (7 + 3))"), 
			                  new QADataEntry("56",       "(5 * 3)"), 
			                  new QADataEntry("57",       "(3 * 4)"), 
			                  new QADataEntry("58",       "(-2 * 8)"), 
			                  new QADataEntry("59",       "(2 * -3)"), 
			                  new QADataEntry("60",       "(2 * +3)"), 
			                  new QADataEntry("67",       "2"), 
			                  new QADataEntry("68",        "0"), 
							  new QADataEntry("69",         "1.20"), 
							  new QADataEntry("70",         "0.02"), 
							  new QADataEntry("72",         "-1"), 
							  new QADataEntry("73",         "-1.2"), 
							  new QADataEntry("74",         "-0.11"), 
							  new QADataEntry("75",         "-12"), 
							  new QADataEntry("76",         "-13.9"), 
							  new QADataEntry("83",         "(0 + 0)"), 
							  new QADataEntry("84",         "(1.3 + 4)"), 
							  new QADataEntry("85",         "(4 + 2.1)"), 
							  new QADataEntry("86",         "(2.0 + 1.1)"), 
							  new QADataEntry("88",      "(-1 + -3)"),  
						      new QADataEntry("89",       "(-3.0 + -1)"), 
						      new QADataEntry("90",       "((1 + 2) + 3)"), 
						      new QADataEntry("91",       "((2 - 3) - 4)"), 
						      new QADataEntry("100",       "-3"), 
						      new QADataEntry("101",       "+3"), 
						      new QADataEntry("98",       TokenSetParser.errStr), 
						      new QADataEntry("99",       TokenSetParser.errStr), 
			                  };
	/* ~Member variables */
}
