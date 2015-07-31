package org.eclipse.titan.designer.parsers.ttcn3parser;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * Reparser for getting pr_Identifier
 * ANTLR 4 version
 * @author Arpad Lovassy
 */
public class Ttcn3FileReparser_V4 implements ITtcn3FileReparser {

	private final TTCN3ReparseUpdater mReparser;
	private final IFile mFile;
	private final ProjectSourceParser mSourceParser;
	private final Map<IFile, String> mFileMap;
	private final Map<IFile, String> mUptodateFiles;
	private final Set<IFile> mHighlySyntaxErroneousFiles;
	 
	private boolean mSyntacticallyOutdated = false;
	
	public Ttcn3FileReparser_V4( final TTCN3ReparseUpdater aReparser,
							     final IFile aFile,
							     final ProjectSourceParser aSourceParser,
							     final Map<IFile, String> aFileMap,
							     final Map<IFile, String> aUptodateFiles,
							     final Set<IFile> aHighlySyntaxErroneousFiles ) {
		mReparser = aReparser;
		mFile = aFile;
		mSourceParser = aSourceParser;
		mFileMap = aFileMap;
		mUptodateFiles = aUptodateFiles;
		mHighlySyntaxErroneousFiles = aHighlySyntaxErroneousFiles;
	}
	
	@Override
	public boolean parse() {
		//TODO: implement
		return false;
	}
}
