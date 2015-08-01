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
		return ((TTCN3ReparseUpdater_V4)mReparser).parse(new ITTCN3ReparseBase_V4() {
			@Override
			public void reparse(final TTCN3Reparser4 parser) {
				Identifier identifier2 = parser.pr_Identifier().identifier;
				parser.pr_EndOfFile();
				if ( parser.isErrorListEmpty() ) {
					mIdentifier = identifier2;
				}
			}
		});
	}
	
	@Override
	public int parseAndSetNameChanged() {
		return ((TTCN3ReparseUpdater_V4)mReparser).parse(new ITTCN3ReparseBase_V4() {
			@Override
			public void reparse(final TTCN3Reparser4 parser) {
				Identifier identifier2 = parser.pr_Identifier().identifier;
				parser.pr_EndOfFile();
				if ( parser.isErrorListEmpty() ) {
					mIdentifier = identifier2;
					// default behaviour
					mReparser.setNameChanged(true);
				}
			}
		});
	}
	
	@Override
	public final Identifier getIdentifier() {
		return mIdentifier;
	}

}
