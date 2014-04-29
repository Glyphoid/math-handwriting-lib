package me.scai.handwriting;

import me.scai.parsetree.Node;

public interface ITokenSetParser {
	Node parse(CAbstractWrittenTokenSet tokenSet, String lhs); /* Return reference to root node */
}
