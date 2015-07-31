package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Identifier;

/**
 * Reparser for getting pr_Identifier
 * ANTLR 4 version
 * @author Arpad Lovassy
 */
public class IdentifierReparser_V4 implements IIdentifierReparser {

	private final TTCN3ReparseUpdater mReparser;
	
	private Identifier mIdentifier;
	
	public IdentifierReparser_V4( final TTCN3ReparseUpdater aReparser ) {
		mReparser = aReparser;
	}
	
	@Override
	public int parse() {
		//TODO: implement
		return 0;
	}
	
	@Override
	public int parseAndSetNameChanged() {
		//TODO: implement
		return 0;
	}
	
	@Override
	public final Identifier getIdentifier() {
		return mIdentifier;
	}

}
