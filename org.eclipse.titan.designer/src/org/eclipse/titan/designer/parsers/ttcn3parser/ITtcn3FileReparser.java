package org.eclipse.titan.designer.parsers.ttcn3parser;

public interface ITtcn3FileReparser {
	
	/**
	 * Runs the reparsing process
	 * @return true if syntactically outdated
	 *         false otherwise 
	 */
	public boolean parse();
}
