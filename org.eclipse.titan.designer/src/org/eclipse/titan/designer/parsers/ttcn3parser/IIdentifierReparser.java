package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Identifier;

public interface IIdentifierReparser {
	
	/**
	 * Runs the reparsing process
	 * @return 0 on success
	 *         failure otherwise, where the value indicates the number of tokens we need to read back 
	 */
	public int parse();

	/**
	 * Runs the reparsing process
	 * Also runs default code (reparser.setNameChanged(true);) on success
	 * @return 0 on success
	 *         failure otherwise, where the value indicates the number of tokens we need to read back 
	 */
	public int parseAndSetNameChanged();

	/**
	 * @return the parsed identifierIdentifierReparser_V2.java
	 */
	public Identifier getIdentifier();

}
