package me.scai.parsetree;

import me.scai.handwriting.CAbstractWrittenTokenSet;

public interface ITokenSetParser {
	Node parse(CAbstractWrittenTokenSet tokenSet); /* Return reference to root node */
}
