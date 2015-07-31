/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.ILocationAST;

/**
 * This class handles the parsing and resolving of configuration files,
 * and is extracting data from them required by the executors (actually the Main Controller)
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ConfigFileHandler_V4 extends ConfigFileHandler {
	
	@Override
	protected void processFile(Path actualFile) {
		CfgAnalyzer_V4 analyzerV4 = new CfgAnalyzer_V4();
		IWorkspaceRoot wroot = ResourcesPlugin.getWorkspace().getRoot();
		IFile[] files = wroot.findFilesForLocation(actualFile);

		if (files.length == 0) {
			ErrorReporter.logError("The file " + actualFile.toOSString()
					+ " can not be mapped to a file resource in eclipse, and so is not accessible");
			processingErrorsDetected = true;
		} else if (files[0].isAccessible()){
			parseFile(actualFile, analyzerV4, files[0]);
		} else {
			ErrorReporter.logError("The file " + files[0].getLocationURI() + " can not be found");
			processingErrorsDetected = true;
		}
	}
	
	private void parseFile(Path actualFile, CfgAnalyzer_V4 analyzerV4, IFile file) {
		analyzerV4.directParse(file, actualFile.toOSString(), null);

		if (analyzerV4.isLogFileNameDefined()) {
			logFileNameDefined = true;
		}
		//TODO: implement something similar, which is V4 specific
		/*
		exceptions.addAll(analyzerV4.getExceptions());
		LocationAST rootNode = analyzerV4.getRootNode();
		if(rootNode != null){
			originalASTs.put(actualFile, rootNode);

			List<String> includeFiles = analyzerV4.getIncludeFilePaths();
			for(String filename:includeFiles){
				filename = PathConverter.getAbsolutePath(actualFile.toOSString(), filename);
				if(filename != null){
					toBeProcessedFiles.add(new Path(filename));
				}
			}

			definesMap.putAll(analyzerV4.getDefinitions());
		}
		*/
	}
	
	@Override
	public void processASTs() {
		//TODO: implement CFGResolver4.g4 and use it here
	}
	
	@Override
	protected void print(final ILocationAST root, final StringBuilder stringbuilder) {
		//TODO: implement
	}
}
