package me.scai.parsetree;

import me.scai.parsetree.evaluation.ParseTreeEvaluator;

import java.util.Map;
import java.util.HashMap;

class QADataEntry {
	/* Member variables */
	private String tokenSetFileName;
	private String correctParseRes;
	private String correctMathTex;

	private Object correctEvalRes;
    private double[] correctEvalResRange = null;

	/* ~Member variables */

	/* Constructors */
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
	
	/* Accordion methods */
	public QADataEntry withParseRes(String parseRes) {
	    this.correctParseRes = parseRes;
	    return this;
	}
	
	public QADataEntry withMathTex(String mathTex) {
	    this.correctMathTex = mathTex;
	    return this;
	}
	
	public QADataEntry withEvalRes(Object evalRes) {
	    this.correctEvalRes = evalRes;
	    return this;
	}

    public QADataEntry withEvalResRange(double[] evalResRange) {
        if (evalResRange.length != 2) {
            throw new IllegalArgumentException("Evaluation result range is not a length-2 array: [lowerBound, upperBound]");
        }
        if (evalResRange[1] < evalResRange[0]) {
            throw new IllegalArgumentException("Evaluation result range does not have an non-descending order: [lowerBound, upperBound]");
        }

        this.correctEvalResRange = evalResRange;

        return this;
    }

    public QADataEntry withEvalResRange(double lowerBound, double upperBound) {
        return withEvalResRange(new double[] {lowerBound, upperBound});
    }
	
	/* Getters */
	public String getTokenSetFileName() {
        return tokenSetFileName;
    }

    public String getCorrectParseRes() {
        return correctParseRes;
    }

    public String getCorrectMathTex() {
        return correctMathTex;
    }
	
	public Object getCorrectEvalRes() {
	    return correctEvalRes;
	}

    public double[] getCorrectEvalResRange() {
        return correctEvalResRange;
    }
}

class QADataSuite {
    private QADataEntry [] entries;
    private boolean isStateful;    
    
    /* Constructor */
    public QADataSuite(QADataEntry [] entries, boolean isStateful) {
        this.entries = entries;
        this.isStateful = isStateful;
    }

    public boolean isStateful() {
        return isStateful;
    }

    public QADataEntry [] getEntries() {
        return entries;
    }
}

public class Test_QADataSet {
	/* Member variables */
//    Map<String, QADataEntry []> QADataSuites = new HashMap<String, QADataEntry []>();
    Map<String, QADataSuite> QADataSuites = new HashMap<String, QADataSuite>();
    
    QADataEntry[] entries = {
            new QADataEntry("98", ParseTreeStringizer.STRINGIZATION_FAILED_STRING),
            new QADataEntry("99", ParseTreeStringizer.STRINGIZATION_FAILED_STRING),
    };
    
    /* No-arg constructor */
    public Test_QADataSet() {
        /* Basic decimal numbers */
        QADataEntry[] entries_basicDecimalNumber = {
            new QADataEntry("1", "12").withMathTex("12").withEvalRes(12.0),
            new QADataEntry("2", "236").withMathTex("236").withEvalRes(236.0),
            new QADataEntry("4", "77").withMathTex("77").withEvalRes(77.0),
            new QADataEntry("6", "36").withMathTex("36").withEvalRes(36.0),
            new QADataEntry("9", "-28").withMathTex("-{28}").withEvalRes(-28.0),
            new QADataEntry("13", "009").withMathTex("009").withEvalRes(9.0),
            new QADataEntry("14", "900").withMathTex("900").withEvalRes(900.0),
            new QADataEntry("15", "100").withMathTex("100").withEvalRes(100.0),
            new QADataEntry("48", "8.3").withMathTex("8.3").withEvalRes(8.3),
            new QADataEntry("49", "4.0").withMathTex("4.0").withEvalRes(4.0),
            new QADataEntry("50", "0.01").withMathTex("0.01").withEvalRes(0.01),
            new QADataEntry("51", "-53").withMathTex("-{53}").withEvalRes(-53.0),
            new QADataEntry("52", "-7.4").withMathTex("-{7.4}").withEvalRes(-7.4),
            new QADataEntry("67", "2").withMathTex("2").withEvalRes(2.0),
            new QADataEntry("68", "0").withMathTex("0").withEvalRes(0.0),
            new QADataEntry("69", "1.20").withMathTex("1.20").withEvalRes(1.2),
            new QADataEntry("70", "0.02").withMathTex("0.02").withEvalRes(0.02),
            new QADataEntry("72", "-1").withMathTex("-{1}").withEvalRes(-1.0),
            new QADataEntry("73", "-1.2").withMathTex("-{1.2}").withEvalRes(-1.2),
            new QADataEntry("74", "-0.11").withMathTex("-{0.11}").withEvalRes(-0.11),
            new QADataEntry("75", "-12").withMathTex("-{12}").withEvalRes(-12.0),
            new QADataEntry("76", "-13.9").withMathTex("-{13.9}").withEvalRes(-13.9),
            new QADataEntry("100", "-3").withMathTex("-{3}").withEvalRes(-3.0),
        };
        QADataSuites.put("basicDecimalNumber", new QADataSuite(entries_basicDecimalNumber, false));
        
        /* Negative sign */
        QADataEntry[] entries_negativePositiveSigns = {
            new QADataEntry("22", "--3").withMathTex("-{-{3}}").withEvalRes(3.0),
            new QADataEntry("101", "+3").withMathTex("+{3}").withEvalRes(3.0),
        };
        QADataSuites.put("negativePositiveSigns", new QADataSuite(entries_negativePositiveSigns, false));
        
        /* Basic number algebra */
        QADataEntry[] entries_basicNumberAlgebra = {
            new QADataEntry("10", "(21 - 3)").withMathTex("{21}-{3}").withEvalRes(18.0),
            new QADataEntry("11", "(21 + 3)").withMathTex("{21}+{3}").withEvalRes(24.0),
            new QADataEntry("12", "(21 - 5)").withMathTex("{21}-{5}").withEvalRes(16.0),            
            new QADataEntry("18", "(56 - 3)").withMathTex("{56}-{3}").withEvalRes(53.0),
            new QADataEntry("56", "(5 * 3)").withMathTex("{5}\\ast{3}").withEvalRes(15.0),
            new QADataEntry("57", "(3 * 4)").withMathTex("{3}\\ast{4}").withEvalRes(12.0),
            new QADataEntry("58", "(-2 * 8)").withMathTex("{-{2}}\\ast{8}").withEvalRes(-16.0),
            new QADataEntry("59", "(2 * -3)").withMathTex("{2}\\ast{-{3}}").withEvalRes(-6.0),
            new QADataEntry("60", "(2 * +3)").withMathTex("{2}\\ast{+{3}}").withEvalRes(6.0),
            new QADataEntry("83", "(0 + 0)").withMathTex("{0}+{0}").withEvalRes(0.0),
            new QADataEntry("84", "(1.3 + 4)").withMathTex("{1.3}+{4}").withEvalRes(5.3),
            new QADataEntry("85", "(4 + 2.1)").withMathTex("{4}+{2.1}").withEvalRes(6.1),
            new QADataEntry("86", "(2.0 + 1.1)").withMathTex("{2.0}+{1.1}").withEvalRes(3.1),
            new QADataEntry("88", "(-1 + -3)").withMathTex("{-{1}}+{-{3}}").withEvalRes(-4.0),
            new QADataEntry("89", "(-3.0 + -1)").withMathTex("{-{3.0}}+{-{1}}").withEvalRes(-4.0),
            new QADataEntry("90", "((1 + 2) + 3)").withMathTex("{{1}+{2}}+{3}").withEvalRes(6.0),
            new QADataEntry("91", "((2 - 3) - 4)").withMathTex("{{2}-{3}}-{4}").withEvalRes(-5.0),
            new QADataEntry("110", "((4 + 3) + 8)").withMathTex("{{4}+{3}}+{8}").withEvalRes(15.0), /* AssocLeft3B */
            new QADataEntry("111", "((8 - 5) + 2)").withMathTex("{{8}-{5}}+{2}").withEvalRes(5.0), /* AssocLeft3B */
            new QADataEntry("112", "((4 + 48) - 5)").withMathTex("{{4}+{48}}-{5}").withEvalRes(47.0), /* AssocLeft3B */
            new QADataEntry("113", "((4 - 8) + 5)").withMathTex("{{4}-{8}}+{5}").withEvalRes(1.0), /* AssocLeft3B */
            new QADataEntry("sim_20", "((11 * 22) * 33)").withMathTex("{{11}\\ast{22}}\\ast{33}").withEvalRes(7986.0),
            new QADataEntry("sim_33", "(12 * 34)").withMathTex("{12}\\ast{34}").withEvalRes(408.0),
        };
        QADataSuites.put("basicNumberAlgebra", new QADataSuite(entries_basicNumberAlgebra, false));
        
        /* Composite number algebra */
        QADataEntry[] entries_compositeNumberAlgebra = {
            new QADataEntry("sim_1", "((1 * 2) + (3 * 4))").withMathTex("{{1}\\ast{2}}+{{3}\\ast{4}}").withEvalRes(14.0), /* Add - multiplication precedence */
            new QADataEntry("sim_6", "((1 * 10) + (2 * 3))").withMathTex("{{1}\\ast{10}}+{{2}\\ast{3}}").withEvalRes(16.0),
            new QADataEntry("sim_7", "((1 + (2 * 3)) - 4)").withMathTex("{{1}+{{2}\\ast{3}}}-{4}").withEvalRes(3.0),
            new QADataEntry("sim_8", "((5 / 8) * (4 / 7))").withMathTex("{\\frac{5}{8}}\\ast{\\frac{4}{7}}").withEvalRes(0.35714285714), /* Multiplication of two fractions */
            new QADataEntry("sim_9", "((4 + ((2 * 3) * 5)) + 8)").withMathTex("{{4}+{{{2}\\ast{3}}\\ast{5}}}+{8}").withEvalRes(42.0),
            //new QADataEntry("sim_10", "((9 - (4 * 8)) + 2)"), /* Why does this token set cause error? */
//            new QADataEntry("sim_11", "((1 / 2) ^ 3)").withEvalRes("{\\frac{1}{2}}^{3}").withEvalRes(0.125), /* Exponentiation of a fraction */ //TODO: Confirm that this is not valid grammar
            new QADataEntry("sim_12", "((1 + (10 * 20)) + 3)").withEvalRes("{{1}+{{10}\\ast{20}}}+{3}").withEvalRes(204.0),
            //new QADataEntry("sim_13", "(1 * (2 ^ 3))").withEvalRes("{1}\\ast{{2}^{3}}").withEvalRes(8.0), // TODO: New AlignBottomNorthPastMiddle leads to failure here
            new QADataEntry("sim_14", "((4 ^ 5) * (2 ^ 3))").withEvalRes("{{4}^{5}}\\ast{{2}^{3}}").withEvalRes(8192.0),
            new QADataEntry("sim_15", "((2 + ((3 ^ 4) * (2 ^ 3))) - 5)").withMathTex("{{2}+{{{3}^{4}}\\ast{{2}^{3}}}}-{5}").withEvalRes(645.0),
            new QADataEntry("sim_16", "((1 + ((2 * 3) * 4)) + 5)").withMathTex("{{1}+{{{2}\\ast{3}}\\ast{4}}}+{5}").withEvalRes(30.0),
            new QADataEntry("sim_17", "(1 + (((2 * 3) * 4) * 5))").withMathTex("{1}+{{{{2}\\ast{3}}\\ast{4}}\\ast{5}}").withEvalRes(121.0),
            new QADataEntry("sim_18", "(((2 * 3) * 4) * 5)").withMathTex("{{{2}\\ast{3}}\\ast{4}}\\ast{5}").withEvalRes(120.0),
            new QADataEntry("sim_19", "(2 * (3 / 4))").withMathTex("{2}\\ast{\\frac{3}{4}}").withEvalRes(1.5),
            new QADataEntry("sim_30", "((2 * 3))").withMathTex("\\left({2}\\times{3}\\right)").withEvalRes(6.0),
            new QADataEntry("sim_31", "((3 * 45))").withMathTex("\\left({3}\\ast{45}\\right)").withEvalRes(135.0),
            new QADataEntry("sim_32", "(((2 * 4)) ^ 6)").withMathTex("{\\left({2}\\times{4}\\right)}^{6}").withEvalRes(262144.0),
            new QADataEntry("sim_21", "((3 * (4 / 5)) * 2)").withMathTex("{{3}\\ast{\\frac{4}{5}}}\\ast{2}").withEvalRes(4.8),
            // new QADataEntry("sim_22", "((((23 / 45) * 7) * (15 / 26)) * 4) + (2 * 5))"), /* Causes hanging. TODO: Debug. */
            new QADataEntry("sim_23", "(((12 / 13) * 5) + (28 * 3))").withMathTex("{{\\frac{12}{13}}\\ast{5}}+{{28}\\ast{3}}").withEvalRes(88.6153846154),
            new QADataEntry("sim_24", "((1 + 2) / (3 * 4))").withMathTex("\\frac{{1}+{2}}{{3}\\ast{4}}").withEvalRes(0.25),
            new QADataEntry("sim_68", "((a ^ 2)*b)").withMathTex("{{a}^{2}}{b}"), /* Variable exponent and multiplication */
            //new QADataEntry("sim_69", "(a*(b ^ 2))", "{a}{{b}^{2}}"), //TODO
            
        };
        QADataSuites.put("compositeNumberAlgebra", new QADataSuite(entries_compositeNumberAlgebra, false));
        
        /* Parentheses */
        QADataEntry[] entries_algebraWithParenthses = {
            new QADataEntry("sim_25", "((1 + 2))").withMathTex("\\left({1}+{2}\\right)").withEvalRes(3.0),
            new QADataEntry("sim_26", "((2 + 3))").withMathTex("\\left({2}+{3}\\right)").withEvalRes(5.0),
            new QADataEntry("sim_27", "((20 + 3))").withMathTex("\\left({20}+{3}\\right)").withEvalRes(23.0),
            new QADataEntry("sim_28", "(((1 + 2) + 3))").withMathTex("\\left({{1}+{2}}+{3}\\right)").withEvalRes(6.0),
            new QADataEntry("sim_29", "((1 - 2))").withMathTex("\\left({1}-{2}\\right)").withEvalRes(-1.0),
            
        };
        QADataSuites.put("algebraWithParentheses", new QADataSuite(entries_algebraWithParenthses, false));
        
        /* Fraction */
        QADataEntry[] entries_fraction = {
            new QADataEntry("45", "((8 - 3) / 4)").withMathTex("\\frac{{8}-{3}}{4}").withEvalRes(1.25),
            new QADataEntry("21", "(29 / 3)").withMathTex("\\frac{29}{3}").withEvalRes(29.0 / 3.0),
            new QADataEntry("27", "(5 / 8)").withMathTex("\\frac{5}{8}").withEvalRes(0.625),
            new QADataEntry("28", "((5 / 8) / 9)").withMathTex("\\frac{\\frac{5}{8}}{9}").withEvalRes(0.06944444444),
            new QADataEntry("29", "(3 / (2 / 7))").withMathTex("\\frac{3}{\\frac{2}{7}}").withEvalRes(10.5),
            new QADataEntry("32", "(1 - (2 / 3))").withMathTex("{1}-{\\frac{2}{3}}").withEvalRes(0.33333333333),
            new QADataEntry("34", "(4 / (5 + (2 / 3)))").withMathTex("\\frac{4}{{5}+{\\frac{2}{3}}}").withEvalRes(0.70588235294),
            new QADataEntry("36", "(23 / 4)").withMathTex("\\frac{23}{4}").withEvalRes(5.75),
            new QADataEntry("37", "((5 + 9) / ((3 / 2) - 1))").withMathTex("\\frac{{5}+{9}}{{\\frac{3}{2}}-{1}}").withEvalRes(28.0),
            new QADataEntry("41", "((4 - 2) / 3)").withMathTex("\\frac{{4}-{2}}{3}").withEvalRes(0.666666666667),
            new QADataEntry("42", "((7 - 8) / 10)").withMathTex("\\frac{{7}-{8}}{10}").withEvalRes(-0.1),
            new QADataEntry("43", "((3 + 1) / 4)").withMathTex("\\frac{{3}+{1}}{4}").withEvalRes(1.0),
            new QADataEntry("44", "(72 / 3)").withMathTex("\\frac{72}{3}").withEvalRes(24.0),
            new QADataEntry("53", "(8.1 / 0.9)").withMathTex("\\frac{8.1}{0.9}").withEvalRes(9.0),
//            new QADataEntry("54", "(-1 / -3.2)").withMathTex("\\frac{-{1}}{-{3.2}}").withEvalRes(0.3125), // TODO: Regressed after checkIllegalOverlap
            new QADataEntry("55", "(-4.2 / (7 + 3))").withMathTex("\\frac{-{4.2}}{{7}+{3}}").withEvalRes(-0.42),
            new QADataEntry("sim_2", "-(1 / 2)").withMathTex("-\\frac{1}{2}").withEvalRes(-0.5), /* Negative of high-level expressions */
            new QADataEntry("sim_3", "-(23 / 4)").withMathTex("-\\frac{23}{4}").withEvalRes(-5.75), /* Negative of high-level expressions */
            new QADataEntry("sim_4", "((1 / 2) + (3 / 4))").withMathTex("{\\frac{1}{2}}+{\\frac{3}{4}}").withEvalRes(1.25),
            new QADataEntry("sim_5", "((1 / 2) * (3 / 4))", "{\\frac{1}{2}}\\ast{\\frac{3}{4}}"), /* Multiplication of two fractions */
        };
        QADataSuites.put("fraction", new QADataSuite(entries_fraction, false));
        
        /* Exponentiation */
        QADataEntry[] entries_exponentiation = {
            new QADataEntry("108", "(289 ^ 643)").withMathTex("{289}^{643}"),
            new QADataEntry("23", "(9 ^ 3)").withMathTex("{9}^{3}").withEvalRes(729.0),
            new QADataEntry("24", "(2 ^ -3)").withMathTex("{2}^{-{3}}").withEvalRes(0.125), /* Error due to geometric imprecision? */
            new QADataEntry("103", "(68 ^ 75)").withMathTex("{68}^{75}"),
            new QADataEntry("104", "(2 ^ 34)").withMathTex("{2}^{34}").withEvalRes(17179869184.0),
            new QADataEntry("106", "(258 ^ 76)").withMathTex("{258}^{76}"),
            new QADataEntry("107", "(256 ^ 481)").withMathTex("{256}^{481}"),
            new QADataEntry("114", "(2 ^ (3 ^ 4))").withMathTex("{2}^{{3}^{4}}"),
            new QADataEntry("115", "(0.5 ^ (2 ^ 3))").withMathTex("{0.5}^{{2}^{3}}").withEvalRes(0.00390625),
            new QADataEntry("sim_170", "(1 + (2 ^ 3))").withMathTex("{1}+{{2}^{3}}").withEvalRes(9.0),
            new QADataEntry("sim_171", "((1 + 2) + (B ^ 4))").withMathTex("{{1}+{2}}+{{B}^{4}}").withEvalRes(3.0),
            new QADataEntry("sim_172", "(a*(b ^ 2))").withMathTex("{a}{{b}^{2}}").withEvalRes(0.0),
            new QADataEntry("sim_173", "((2 ^ 7) / 8)").withMathTex("\\frac{{2}^{7}}{8}").withEvalRes(16.0),
            new QADataEntry("sim_174", "((x ^ 2)*(y ^ 3))").withMathTex("{{x}^{2}}{{y}^{3}}").withEvalRes(0.0),
        };
        QADataSuites.put("exponentiation", new QADataSuite(entries_exponentiation, false));
        
        /* Sqrt */
        QADataEntry[] entries_sqrt = {
            new QADataEntry("sim_34", "(sqrt(16))").withMathTex("\\sqrt{16}").withEvalRes(4.0),
            new QADataEntry("sim_35", "(sqrt(4))").withMathTex("\\sqrt{4}").withEvalRes(2.0),
            new QADataEntry("sim_36", "(1 + (sqrt(243)))").withMathTex("{1}+{\\sqrt{243}}").withEvalRes(16.5884572681),
            new QADataEntry("sim_37", "(sqrt((5 - 3.8)))").withMathTex("\\sqrt{{5}-{3.8}}").withEvalRes(1.09544511501),
            new QADataEntry("sim_38", "(sqrt((3 / 5)))").withMathTex("\\sqrt{\\frac{3}{5}}").withEvalRes(0.77459666924),
            new QADataEntry("sim_39", "(2 + (sqrt((1 / 6))))").withMathTex("{2}+{\\sqrt{\\frac{1}{6}}}").withEvalRes(2.40824829046),
            new QADataEntry("sim_40", "(sqrt(((11 / 22) / 33)))").withMathTex("\\sqrt{\\frac{\\frac{11}{22}}{33}}").withEvalRes(0.12309149097),
            new QADataEntry("sim_41", "(sqrt((7 + (1 / 12))))").withMathTex("\\sqrt{{7}+{\\frac{1}{12}}}").withEvalRes(2.66145323711),
            new QADataEntry("sim_42", "(sqrt(((3 - 4) + 5)))").withMathTex("\\sqrt{{{3}-{4}}+{5}}").withEvalRes(2.0),
            new QADataEntry("sim_43", "((sqrt(2)) / 3)").withMathTex("\\frac{\\sqrt{2}}{3}").withEvalRes(0.47140452079),
            new QADataEntry("sim_44", "(sqrt((sqrt(2))))").withMathTex("\\sqrt{\\sqrt{2}}").withEvalRes(1.189207115),
            new QADataEntry("sim_45", "((sqrt((sqrt(21)))) / 8)").withMathTex("\\frac{\\sqrt{\\sqrt{21}}}{8}").withEvalRes(0.26758689286),
            new QADataEntry("sim_46", "(sqrt((1 + (sqrt(4)))))").withMathTex("\\sqrt{{1}+{\\sqrt{4}}}").withEvalRes(1.73205080757),
            new QADataEntry("sim_47", "(sqrt(((sqrt(4)) / ((sqrt(9)) + (sqrt(16))))))").withMathTex("\\sqrt{\\frac{\\sqrt{4}}{{\\sqrt{9}}+{\\sqrt{16}}}}").withEvalRes(0.53452248382),
        };
        QADataSuites.put("sqrt", new QADataSuite(entries_sqrt, false));
        
        /* Symbols */
        QADataEntry[] entries_symbols = {
            new QADataEntry("sim_48", "gr_al", "\\alpha"),
            new QADataEntry("sim_49", "(gr_al*gr_be)", "{\\alpha}{\\beta}"),
            new QADataEntry("sim_50", "(A ^ B)", "{A}^{B}"),
            /* Subscript */
            new QADataEntry("sim_137", "A_1", "A_1"),
            new QADataEntry("sim_138", "gr_al_0", "\\alpha_0"),
            new QADataEntry("sim_139", "(A*B_2)", "{A}{B_2}"),
            new QADataEntry("sim_140", "(A_1*B_2)", "{A_1}{B_2}"),
            new QADataEntry("sim_141", "A_12", "A_12"),
            new QADataEntry("sim_142", "M_310", "M_310"),
            new QADataEntry("sim_143", "(A_7 + 8)", "{A_7}+{8}"),
            new QADataEntry("sim_144", "((A_1 - B_2) + c_3)", "{{A_1}-{B_2}}+{c_3}"),
            new QADataEntry("sim_145", "(A_3 ^ 2)", "{A_3}^{2}"), /* Subscript and index in the same expression */
            new QADataEntry("sim_146", "((A_23 ^ 4) + 5)", "{{A_23}^{4}}+{5}"),
            new QADataEntry("sim_147", "(A_9 / B_10)", "\\frac{A_9}{B_10}"),  /* Subscripts in a fraction */
            new QADataEntry("sim_148", "(sqrt(B_7))", "\\sqrt{B_7}"),         /* Subscripts in a sqrt */
            new QADataEntry("sim_149", "(((A_1 + B_2)) * c_3)", "{\\left({A_1}+{B_2}\\right)}\\ast{c_3}"), /* Subscripts in a pair of parentheses */
            new QADataEntry("sim_150", "(sqrt(((A_1 ^ 2) + ((B_3 ^ 4) / D_5))))", "\\sqrt{{{A_1}^{2}}+{\\frac{{B_3}^{4}}{D_5}}}")
        };
        QADataSuites.put("symbols", new QADataSuite(entries_symbols, false));

        /* Symbols stateful */
        QADataEntry[] entries_symbolsStateful = {
            new QADataEntry("sim_153", "(A = 2)", "{A}={2}").withEvalRes(2.0),
            new QADataEntry("sim_151", "(A_1 = 3)", "{A_1}={3}").withEvalRes(3.0),
            new QADataEntry("sim_155", "(A_21 = 4)", "{A_21}={4}").withEvalRes(4.0),
            new QADataEntry("sim_154", "A", "A").withEvalRes(2.0),
            new QADataEntry("sim_152", "A_1", "A_1").withEvalRes(3.0),
            new QADataEntry("sim_156", "(1 / (sqrt(A_21)))", "\\frac{1}{\\sqrt{A_21}}").withEvalRes(0.5)
        };
        QADataSuites.put("symbolsStateful", new QADataSuite(entries_symbolsStateful, true));
        
        /* Function */
        QADataEntry[] entries_function = {
            new QADataEntry("sim_51", "ln(3)").withMathTex("\\ln{3}").withEvalRes(1.09861228867),
            new QADataEntry("sim_52", "sin(7)").withMathTex("\\sin{7}").withEvalRes(0.65698659871),
            new QADataEntry("sim_53", "cos(G)").withMathTex("\\cos{G}"),
            new QADataEntry("sim_54", "sin(gr_al)").withMathTex("\\sin{\\alpha}"),
            new QADataEntry("sim_55", "ln(2.7)").withMathTex("\\ln{2.7}").withEvalRes(0.99325177301),
            new QADataEntry("sim_56", "sin((5 / 3))").withMathTex("\\sin{\\frac{5}{3}}").withEvalRes(0.99540795775),
            // new QADataEntry("sim_57", "cos((gr_be ^ 2))", "\\cos{{\\beta}^{2}}"), // TODO: New AlignBottomNorthPastMiddle leads to failure here
            new QADataEntry("sim_58", "ln((sqrt(2)))").withMathTex("\\ln{\\sqrt{2}}").withEvalRes(0.34657359028),
            new QADataEntry("sim_59", "(sin(1) + 8)").withMathTex("{\\sin{1}}+{8}").withEvalRes(8.84147098481),
            new QADataEntry("sim_60", "(A + sin(3))").withMathTex("{A}+{\\sin{3}}"),
            new QADataEntry("sim_61", "cos(sin(72))").withMathTex("\\cos{\\sin{72}}").withEvalRes(0.9679594271), // Nested function, 1 level
            new QADataEntry("sim_62", "ln(sin(cos(0)))").withMathTex("\\ln{\\sin{\\cos{0}}}").withEvalRes(-0.17260374626), // Nested function, 2 levels
            new QADataEntry("sim_63", "(sin(8) / 4)").withMathTex("\\frac{\\sin{8}}{4}").withEvalRes(0.24733956165),
            new QADataEntry("sim_64", "(sin(A) / cos(A))").withMathTex("\\frac{\\sin{A}}{\\cos{A}}"),
            new QADataEntry("sim_65", "(sqrt(sin(A)))").withMathTex("\\sqrt{\\sin{A}}"),
            new QADataEntry("sim_66", "((sqrt(sin(A))) / cos(A))").withMathTex("\\frac{\\sqrt{\\sin{A}}}{\\cos{A}}"),
            new QADataEntry("sim_67", "cos(gr_al)").withMathTex("\\cos{\\alpha}"),
            new QADataEntry("sim_87", "sin(((2 + B)))").withMathTex("\\sin{\\left({2}+{B}\\right)}"),
            new QADataEntry("sim_88", "sin((sqrt(A)))").withMathTex("\\sin{\\left(\\sqrt{A}\\right)}"),
            new QADataEntry("sim_89", "(sin(A) + cos(B))").withMathTex("{\\sin{\\left(A\\right)}}+{\\cos{\\left(B\\right)}}"),
            new QADataEntry("sim_90", "(2*sin(gr_al))").withMathTex("{2}{\\sin{\\left(\\alpha\\right)}}"),
            new QADataEntry("sim_86", "ln(88)").withMathTex("\\ln{\\left(88\\right)}").withEvalRes(4.47733681448),
//              new QADataEntry("sim_91", "(sin(A)*cos(B))", "{\\sin{\\left(A\\right)}}{\\cos{\\left(B\\right)}}"), //TODO
            new QADataEntry("sim_183", "cos((0*sin(1)))").withMathTex("\\cos{{0}{\\sin{1}}}").withEvalRes(1.0),
            new QADataEntry("sim_184", "((cos(a))*(sin(b)))").withMathTex("{\\left(\\cos{a}\\right)}{\\left(\\sin{b}\\right)}").withEvalRes(0.0),
            new QADataEntry("sim_185", "((sin(A))*(cos(B)))").withMathTex("{\\left(\\sin{A}\\right)}{\\left(\\cos{B}\\right)}").withEvalRes(0.0)
        };
        QADataSuites.put("function", new QADataSuite(entries_function, false));
        
        /* Basic Matrix */
        QADataEntry[] entries_basicMatrix = {
            new QADataEntry("sim_70", "[3; 4]").withMathTex("\\begin{bmatrix}3\\\\4\\end{bmatrix}"),                         // Column vector
            new QADataEntry("sim_71", "[sin(A); cos(A)]").withMathTex("\\begin{bmatrix}\\sin{A}\\\\\\cos{A}\\end{bmatrix}"), // Column vector
            new QADataEntry("sim_72", "[2, 3]").withMathTex("\\begin{bmatrix}2&3\\end{bmatrix}"),     // Row vector
            new QADataEntry("sim_73", "[1, 2, 3]").withMathTex("\\begin{bmatrix}1&2&3\\end{bmatrix}"),   // Row vector
            new QADataEntry("sim_74", "[12, 34]",  "\\begin{bmatrix}12&34\\end{bmatrix}"),   // Row vector
            new QADataEntry("sim_75", "[(1 / 2), (3 / 5)]").withMathTex("\\begin{bmatrix}\\frac{1}{2}&\\frac{3}{5}\\end{bmatrix}"), // Row vector with fractions
            new QADataEntry("sim_77", "[a, b]").withMathTex("\\begin{bmatrix}a&b\\end{bmatrix}"),                       // Row vector with symbols
            new QADataEntry("sim_78", "[(a*b)]").withMathTex("\\begin{bmatrix}{a}{b}\\end{bmatrix}"),                    // Row vector with symbols
            new QADataEntry("sim_79", "[(a*b), c, (x*y)]").withMathTex("\\begin{bmatrix}{a}{b}&c&{x}{y}\\end{bmatrix}"),           // Row vector with symbols
            new QADataEntry("sim_80", "[1, 2; 3, 4]").withMathTex("\\begin{bmatrix}1&2\\\\3&4\\end{bmatrix}"),                // Matrix: 2x2
            new QADataEntry("sim_81", "[(a*b), c; x, (y*z)]").withMathTex("\\begin{bmatrix}{a}{b}&c\\\\x&{y}{z}\\end{bmatrix}"),      // Matrix: 2x2, with symbols
            new QADataEntry("sim_82", "[(a*b), c; x, (y*u)]").withMathTex("\\begin{bmatrix}{a}{b}&c\\\\x&{y}{u}\\end{bmatrix}"),      // Matrix: 2x2, with symbols
            new QADataEntry("sim_84", "[(a ^ 2), b; 0, (a ^ 3)]").withMathTex("\\begin{bmatrix}{a}^{2}&b\\\\0&{a}^{3}\\end{bmatrix}"),  // Matrix: 2x2, with symbols and fraction
            new QADataEntry("sim_85", "[2, 4, 6; 1, 3, 5; 0, 7, 9]").withMathTex("\\begin{bmatrix}2&4&6\\\\1&3&5\\\\0&7&9\\end{bmatrix}"), // Matrix: 3x3, with symbols and fraction
            new QADataEntry("sim_92", "[(2 ^ 3); (3 ^ 4)]").withMathTex("\\begin{bmatrix}{2}^{3}\\\\{3}^{4}\\end{bmatrix}"),
            new QADataEntry("sim_93", "[(A ^ 2); (B ^ 3)]").withMathTex("\\begin{bmatrix}{A}^{2}\\\\{B}^{3}\\end{bmatrix}"),
            new QADataEntry("sim_94", "[(x ^ 2); (y ^ 3)]"),
            new QADataEntry("sim_83", "[(1 / 2), 3; 4, 9]").withMathTex("\\begin{bmatrix}\\frac{1}{2}&3\\\\4&9\\end{bmatrix}"),
            new QADataEntry("sim_190", "[(x ^ 2), (1 / y); 3, 4]").withMathTex("\\begin{bmatrix}{x}^{2}&\\frac{1}{y}\\\\3&4\\end{bmatrix}"),
            new QADataEntry("sim_191", "[(sqrt((1 + 2))), (sqrt(3)); 4, (sqrt(5))]").withMathTex("\\begin{bmatrix}\\sqrt{{1}+{2}}&\\sqrt{3}\\\\4&\\sqrt{5}\\end{bmatrix}"),
            new QADataEntry("sim_193", "[(1 / 2), (3 / 4); (5 / 6), (7 / 8)]").withMathTex("\\begin{bmatrix}\\frac{1}{2}&\\frac{3}{4}\\\\\\frac{5}{6}&\\frac{7}{8}\\end{bmatrix}"),
//            new QADataEntry("sim_192", "[(sqrt((1 + 2))), (sqrt(3)); 4, (sqrt(5))]").withMathTex("\\begin{bmatrix}\\sqrt{{1}+{2}}&\\sqrt{3}\\\\4&\\sqrt{5}\\end{bmatrix}") //TODO: This takes too long to parse.
        };
        QADataSuites.put("basicMatrix", new QADataSuite(entries_basicMatrix, false));

        /* Matrix with holes */
        QADataEntry[] entries_matrixWithHoles = {
            new QADataEntry("sim_76", "[0, 4, 0; 3, 0, 5]").withMathTex("\\begin{bmatrix}0&4&0\\\\3&0&5\\end{bmatrix}"),
            new QADataEntry("sim_194", "[2, 0; 0, 3]").withMathTex("\\begin{bmatrix}2&0\\\\0&3\\end{bmatrix}"),
            new QADataEntry("sim_196", "[1, 0, 0; 2, 3, 4]").withMathTex("\\begin{bmatrix}1&0&0\\\\2&3&4\\end{bmatrix}"),
            new QADataEntry("sim_197", "[0, 0, 1; 2, 3, 4]").withMathTex("\\begin{bmatrix}0&0&1\\\\2&3&4\\end{bmatrix}"),
            new QADataEntry("sim_198", "[1, 2, 3; 4, 0, 0]").withMathTex("\\begin{bmatrix}1&2&3\\\\4&0&0\\end{bmatrix}"),
            new QADataEntry("sim_199", "[1, 2, 3; 0, 4, 0]").withMathTex("\\begin{bmatrix}1&2&3\\\\0&4&0\\end{bmatrix}"),
            new QADataEntry("sim_200", "[1, 2, 3; 0, 0, 4]").withMathTex("\\begin{bmatrix}1&2&3\\\\0&0&4\\end{bmatrix}"),
            new QADataEntry("sim_201", "[1, 0, 0, 0; 2, 3, 4, 5]").withMathTex("\\begin{bmatrix}1&0&0&0\\\\2&3&4&5\\end{bmatrix}"),
            new QADataEntry("sim_202", "[0, 1, 0, 0; 2, 3, 4, 5]").withMathTex("\\begin{bmatrix}0&1&0&0\\\\2&3&4&5\\end{bmatrix}"),
            new QADataEntry("sim_203", "[0, 0, 1, 0; 2, 3, 4, 5]").withMathTex("\\begin{bmatrix}0&0&1&0\\\\2&3&4&5\\end{bmatrix}"),
            new QADataEntry("sim_204", "[0, 0, 0, 1; 2, 3, 4, 5]").withMathTex("\\begin{bmatrix}0&0&0&1\\\\2&3&4&5\\end{bmatrix}"),
            new QADataEntry("sim_205", "[0, 1, 2; 3, 4, 5; 6, 7, 8]").withMathTex("\\begin{bmatrix}0&1&2\\\\3&4&5\\\\6&7&8\\end{bmatrix}"),
            new QADataEntry("sim_206", "[0, 1, 0; 2, 0, 3; 0, 4, 0]").withMathTex("\\begin{bmatrix}0&1&0\\\\2&0&3\\\\0&4&0\\end{bmatrix}"),
            new QADataEntry("sim_207", "[1, 2; 0, 3; 0, 4; 0, 5]").withMathTex("\\begin{bmatrix}1&2\\\\0&3\\\\0&4\\\\0&5\\end{bmatrix}"),
            new QADataEntry("sim_208", "[11, 2; 3, 0; 4, 0; 5, 0]").withMathTex("\\begin{bmatrix}11&2\\\\3&0\\\\4&0\\\\5&0\\end{bmatrix}"),
//                new QADataEntry("sim_195", "[0, 0; 0, 2; 4, 0]")                                           //TODO: With empty (zero) elments // Row vector with exponentiation
        };
        QADataSuites.put("matrixWithHoles", new QADataSuite(entries_matrixWithHoles, false));

        /* Basic Matrix: Stateful */
        QADataEntry[] entries_basicMatrixStateful = {
            new QADataEntry("sim_95", "(A = [1, 2; 3, 4])").withMathTex("{A}={\\begin{bmatrix}1&2\\\\3&4\\end{bmatrix}}"),  // Assignment of matrix value
            new QADataEntry("sim_96", "(B = 2)").withMathTex("{B}={2}"), // Assignment of double value
            new QADataEntry("sim_97", "A").withMathTex("A"),    // STATEFUL EVAL
            new QADataEntry("sim_98", "(B + A)").withMathTex("{B}+{A}"),   // STATEFUL EVAL
            new QADataEntry("sim_99", "(A + B)").withMathTex("{A}+{B}"),   // STATEFUL EVAL
            new QADataEntry("sim_100", "((A + B) + A)").withMathTex("{{A}+{B}}+{A}"),   // STATEFUL EVAL
            new QADataEntry("sim_101", "(3*A)").withMathTex("{3}{A}"),    // STATEFUL EVAL
            new QADataEntry("sim_102", "(A - B)").withMathTex("{A}-{B}"),  // STATEFUL EVAL
            new QADataEntry("sim_103", "(B - A)").withMathTex("{B}-{A}"),  // STATEFUL EVAL
            new QADataEntry("sim_104", "(A*B)").withMathTex("{A}{B}"),    // STATEFUL EVAL
            new QADataEntry("sim_105", "det(A)").withMathTex("\\det{A}"),   // STATEFUL EVAL
            new QADataEntry("sim_106", "det(A)").withMathTex("\\det{\\left(A\\right)}"),   // STATEFUL EVAL
            new QADataEntry("sim_107", "det(((A + B)))").withMathTex("\\det{\\left({A}+{B}\\right)}"),   // STATEFUL EVAL
            new QADataEntry("sim_108", "rank(A)").withMathTex("\\rank{A}"),   // STATEFUL EVAL
        };
        QADataSuites.put("basicMatrixStateful", new QADataSuite(entries_basicMatrixStateful, true));
        
        /* Defined functions: Stateful */
        QADataEntry[] entries_definedFunctions = {
            new QADataEntry("sim_109", "(f(x) = (2*x))").withMathTex("{f{\\left(x\\right)}}={{2}{x}}"), // Function definition
            new QADataEntry("sim_112", "f(3)").withMathTex("f{\\left(3\\right)}").withEvalRes(6.0),             // Evaluation of custom function
            new QADataEntry("sim_110", "(g(y) = 1)").withMathTex("{g{\\left(y\\right)}}={1}"),     // Function definition
            new QADataEntry("sim_111", "(h(x, y) = (x*y))").withMathTex("{h{\\left(x,y\\right)}}={{x}{y}}"), // Function definition
            new QADataEntry("sim_115", "f(27)").withMathTex("f{\\left(27\\right)}"),             // Evaluation of custom function
            new QADataEntry("sim_113", "f((3 + 2))").withMathTex("f{\\left({3}+{2}\\right)}"), // Evaluation of custom function
            new QADataEntry("sim_114", "f(-0.5)").withMathTex("f{\\left(-{0.5}\\right)}"),      // Evaluation of custom function
            new QADataEntry("sim_116", "f((sqrt(3)))").withMathTex("f{\\left(\\sqrt{3}\\right)}"),             // Evaluation of custom function
            new QADataEntry("sim_117", "(f(4) + 2)").withMathTex("{f{\\left(4\\right)}}+{2}"),  // Custom function term as level-1 expression
            new QADataEntry("sim_118", "(1 / f((2 + 3)))").withMathTex("\\frac{1}{f{\\left({2}+{3}\\right)}}"),  // Custom function term as level-1 expression
            new QADataEntry("sim_119", "(3*f(9))").withMathTex("{3}{f{\\left(9\\right)}}"),                      // Custom function term as level-1 expression
            new QADataEntry("sim_120", "(f(1)*g(0))").withMathTex("{f{\\left(1\\right)}}{g{\\left(0\\right)}}"), // Custom function term as level-1 expression
            new QADataEntry("sim_121", "((2*f(3)) + (4*g(5)))").withMathTex("{{2}{f{\\left(3\\right)}}}+{{4}{g{\\left(5\\right)}}}"), // Custom function term as level-1 expression
            new QADataEntry("sim_175", "(p(x) = (3*(x ^ 2)))").withMathTex("{p{\\left(x\\right)}}={{3}{{x}^{2}}}"),
            new QADataEntry("sim_176", "p(4)").withEvalRes(48.0),
            new QADataEntry("sim_182", "p(Sum((b = 1) : (2))(b))").withMathTex("p{\\left(\\sum\\limits_{{b}={1}}^{2}{b}\\right)}").withEvalRes(27.0),
            new QADataEntry("sim_177", "(q(x) = (((3*x) + (x ^ 2)) / (sqrt(x))))").withMathTex("{q{\\left(x\\right)}}={\\frac{{{3}{x}}+{{x}^{2}}}{\\sqrt{x}}}"),
            new QADataEntry("sim_178", "q((3 + 1))").withEvalRes(14.0),
            new QADataEntry("sim_179", "q((p(1) + 1))").withMathTex("q{\\left({p{\\left(1\\right)}}+{1}\\right)}").withEvalRes(14.0)
        };
        QADataSuites.put("definedFunctions", new QADataSuite(entries_definedFunctions, true));
        
        /* Function evaluation context cluster */
        QADataEntry[] entries_functionEvaluationContextClosure = {
            new QADataEntry("sim_126", "(y = 9)").withMathTex("{y}={9}").withEvalRes(9.0),
            new QADataEntry("sim_129", "(h = 0.1)").withMathTex("{h}={0.1}").withEvalRes(0.1),
            new QADataEntry("sim_127", "(f(h) = (y*h))").withMathTex("{f{\\left(h\\right)}}={{y}{h}}"),
            new QADataEntry("sim_128", "f(4)").withMathTex("f{\\left(4\\right)}").withEvalRes(36.0),
            new QADataEntry("sim_130", "h").withMathTex("h").withEvalRes(0.1),
            new QADataEntry("sim_131", "(y = -8)").withMathTex("{y}={-{8}}").withEvalRes(-8.0),
            new QADataEntry("sim_128", "f(4)").withMathTex("f{\\left(4\\right)}").withEvalRes(-32.0),
        };
        QADataSuites.put("functionEvaluationContextClosure", new QADataSuite(entries_functionEvaluationContextClosure, true));
        
        /* Sigma-pi term: Stateful */
        QADataEntry[] entries_sigmaPiTerm = {
            /* Sigma sum term */
            new QADataEntry("sim_124", "Sum((i = 1) : (8))(i)").withMathTex("\\sum\\limits_{{i}={1}}^{8}{i}").withEvalRes(36.0),
            new QADataEntry("sim_125", "Sum((n = -2) : ((sqrt(4))))((n ^ 2))").withMathTex("\\sum\\limits_{{n}={-{2}}}^{\\sqrt{4}}{{n}^{2}}").withEvalRes(10.0),     // Sigma sum term
            new QADataEntry("sim_180", "(3*Sum((a = 1) : (2))(a))").withMathTex("{3}{\\sum\\limits_{{a}={1}}^{2}{a}}").withEvalRes(9.0),
            new QADataEntry("sim_181", "(1 + Sum((a = 2) : (3))(a))").withMathTex("{1}+{\\sum\\limits_{{a}={2}}^{3}{a}}").withEvalRes(6.0),
            new QADataEntry("sim_187", "Sum((a = 1) : (2))(Sum((b = 1) : (3))((a*b)))").withMathTex("\\sum\\limits_{{a}={1}}^{2}{\\sum\\limits_{{b}={1}}^{3}{{a}{b}}}"), // TODO: Parsing of nested doesn't work with the sigma/pi-style shortcut. Eval of nested has problems. .withEvalRes(18.0), not 28.0
            new QADataEntry("sim_188", "(1 / Sum((x = 6) : (7))(x))").withMathTex("\\frac{1}{\\sum\\limits_{{x}={6}}^{7}{x}}").withEvalRes(1.0 / 13.0),
            new QADataEntry("sim_189", "(1 / Sum((x = 2) : (3))((x ^ 2)))").withMathTex("\\frac{1}{\\sum\\limits_{{x}={2}}^{3}{{x}^{2}}}").withEvalRes(1.0 / 13.0),

            /* Pi product term */
            new QADataEntry("sim_186", "Prod((a = 1) : (3))(a)").withMathTex("\\prod\\limits_{{a}={1}}^{3}{a}").withEvalRes(6.0)
        };
        QADataSuites.put("sigmaPiTerm", new QADataSuite(entries_sigmaPiTerm, true));

        /* Sigma-pi term evaluation context closure */
        QADataEntry[] entries_sigmaPiTermEvaluationContextClosure = {
            new QADataEntry("sim_133", "(a = 2)").withMathTex("{a}={2}").withEvalRes(2.0),
            new QADataEntry("sim_135", "(i = 7)").withMathTex("{i}={7}").withEvalRes(7.0),
            new QADataEntry("sim_136", "i").withMathTex("i").withEvalRes(7.0),
            new QADataEntry("sim_134", "Sum((i = 1) : (5))((i ^ a))").withMathTex("\\sum\\limits_{{i}={1}}^{5}{{i}^{a}}").withEvalRes(55.0),
            new QADataEntry("sim_136", "i").withMathTex("i").withEvalRes(7.0),
            new QADataEntry("sim_134", "Sum((i = 1) : (5))((i ^ a))").withMathTex("\\sum\\limits_{{i}={1}}^{5}{{i}^{a}}").withEvalRes(55.0),
        };
        QADataSuites.put("sigmaPiTermEvaluationContextClosure", new QADataSuite(entries_sigmaPiTermEvaluationContextClosure, true));


         /* Definite integral terms */
        QADataEntry[] entries_defIntegTerm = {
            new QADataEntry("sim_209", "Integ(x=0 : 1)((x ^ 2))").withMathTex("\\int_{0}^{1}{{x}^{2}}d{x}").withEvalResRange(0.3332, 0.3334),
            new QADataEntry("sim_210", "Integ(x=0 : 1)(x)").withMathTex("\\int_{0}^{1}{x}d{x}").withEvalResRange(0.499999, 0.500001),
            new QADataEntry("sim_211", "Integ(x=0 : gr_pi)(sin(x))").withMathTex("\\int_{0}^{\\pi}{\\sin{x}}d{x}").withEvalResRange(1.9998, 2.0002),
            new QADataEntry("sim_212", "Integ(x=1 : 2)((1 / x))").withMathTex("\\int_{1}^{2}{\\frac{1}{x}}d{x}").withEvalResRange(0.6931, 0.6932),
            new QADataEntry("sim_212", "Integ(x=1 : 2)((1 / x))").withMathTex("\\int_{1}^{2}{\\frac{1}{x}}d{x}").withEvalResRange(0.6931, 0.6932),
            new QADataEntry("sim_213", "Integ(y=1 : 4)((sqrt(y)))").withMathTex("\\int_{1}^{4}{\\sqrt{y}}d{y}").withEvalResRange(4.6666, 4.6667),
        };
        QADataSuites.put("defIntegTerm", new QADataSuite(entries_defIntegTerm, false));

        /* Performance test: Relatively more complex token sets */
        QADataEntry[] entries_performance = {
            new QADataEntry("sim_157", "((((1 / 2) + (1 / 3)) + (1 / 4)) + (1 / 5))").withEvalRes(1.2833333333333332)
        };
        QADataSuites.put("performance", new QADataSuite(entries_performance, false));

        /* Predefined constants */
        QADataEntry[] entries_predefinedConstants = {
            new QADataEntry("sim_158", "c").withEvalRes(299792458.0), // c: speed of light. It might be okay to hard-code the value in this case
            new QADataEntry("sim_159", "((1 / 2)*c)").withEvalRes(0.5 * 299792458.0),
            new QADataEntry("sim_160", "(((1 / 2)*c) + ((1 / 3)*c))").withEvalRes(299792458.0 * 5.0 / 6.0),
            new QADataEntry("sim_161", "(c / 4)").withEvalRes(299792458.0 / 4.0),
            new QADataEntry("sim_162", "(gr_pi ^ 2)").withEvalRes(Math.PI * Math.PI),
            new QADataEntry("sim_163", "(sqrt(e))").withEvalRes(Math.sqrt(Math.E)),
            new QADataEntry("sim_164", "((1 / 6)*h)").withEvalRes(1.1043448833333333E-34), // h: Planck constant
            new QADataEntry("sim_165", "N_A").withEvalRes(6.0221415E23), // Avogadro constant
            new QADataEntry("sim_166", "(sqrt((N_A - 1)))").withEvalRes(7.760245807962529E11)
        };
        QADataSuites.put("predefinedConstants", new QADataSuite(entries_predefinedConstants, false));

        /* Token sets with incorrect syntax */
        QADataEntry[] entries_incorrectSyntax = {
           new QADataEntry("sim_167", ParseTreeStringizer.STRINGIZATION_FAILED_STRING).withEvalRes(ParseTreeEvaluator.EVAL_FAILED_STRING).withMathTex(ParseTreeMathTexifier.MATH_TEXIFICATION_FAILED_STRING),
           new QADataEntry("sim_168", ParseTreeStringizer.STRINGIZATION_FAILED_STRING).withEvalRes(ParseTreeEvaluator.EVAL_FAILED_STRING).withMathTex(ParseTreeMathTexifier.MATH_TEXIFICATION_FAILED_STRING),
           new QADataEntry("sim_169", ParseTreeStringizer.STRINGIZATION_FAILED_STRING).withEvalRes(ParseTreeEvaluator.EVAL_FAILED_STRING).withMathTex(ParseTreeMathTexifier.MATH_TEXIFICATION_FAILED_STRING)
        };
        QADataSuites.put("incorrectSyntax", new QADataSuite(entries_incorrectSyntax, false));
    }
	
	/* ~Member variables */
}
