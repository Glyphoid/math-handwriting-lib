package me.scai.parsetree;

//import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.handwriting.CWrittenTokenSetNoStroke;

public interface ITokenSetParser {
//	Node parse(CAbstractWrittenTokenSet tokenSet); /* Return reference to root node */
	Node parse(CWrittenTokenSetNoStroke tokenSet)
            throws TokenSetParserException, InterruptedException; /* Return reference to root node */
}
