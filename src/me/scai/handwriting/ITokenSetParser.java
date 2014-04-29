package me.scai.handwriting;

import me.scai.parsetree.Node;

public interface ITokenSetParser {
	Node parse(CAbstractWrittenTokenSet tokenSet); /* Return reference to root node */
}
