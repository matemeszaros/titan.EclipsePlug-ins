/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * Reparser for getting pr_Identifier
 * @author Arpad Lovassy
 */
public class Ttcn3FileReparser implements ITtcn3FileReparser {

	private final TTCN3ReparseUpdater mReparser;
	private final IFile mFile;
	private final ProjectSourceParser mSourceParser;
	private final Map<IFile, String> mFileMap;
	private final Map<IFile, String> mUptodateFiles;
	private final Set<IFile> mHighlySyntaxErroneousFiles;
	 
	private boolean mSyntacticallyOutdated = false;
	
	public Ttcn3FileReparser( final TTCN3ReparseUpdater aReparser,
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
		mReparser.parse(new ITTCN3ReparseBase() {
			@Override
			public void reparse(final Ttcn3Reparser parser) {
				parser.pr_TTCN3File();
				TTCN3Module actualTtcn3Module = parser.getModule();
				if (actualTtcn3Module != null && actualTtcn3Module.getIdentifier() != null) {
					mSourceParser.getSemanticAnalyzer().addModule(actualTtcn3Module);
					mFileMap.put(mFile, actualTtcn3Module.getName());
					mUptodateFiles.put(mFile, actualTtcn3Module.getName());
				} else {
					mSyntacticallyOutdated = true;
					mHighlySyntaxErroneousFiles.add(mFile);
				}

				if (actualTtcn3Module.getLocation().getEndOffset() == -1 && !parser.getErrors().isEmpty()) {
					actualTtcn3Module.getLocation().setEndOffset((int) new File(mFile.getLocationURI()).length());
				}
			}
		});
		return mSyntacticallyOutdated;
	}
}
