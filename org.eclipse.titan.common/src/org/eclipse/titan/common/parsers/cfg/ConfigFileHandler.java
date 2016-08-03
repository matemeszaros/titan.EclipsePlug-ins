/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.CfgParseTreePrinter.ResolveMode;
import org.eclipse.titan.common.path.PathConverter;

/**
 * This class handles the parsing and resolving of configuration files,
 * and is extracting data from them required by the executors (actually the Main Controller)
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ConfigFileHandler {
	private LinkedHashMap<Path, CfgParseResult> originalASTs = new LinkedHashMap<Path, CfgParseResult>();
	private final Map<String, CfgDefinitionInformation> definesMap = new HashMap<String, CfgDefinitionInformation>();

	/**
	 * List of files, which are already parsed, no need to parse it again
	 */
	private final List<Path> processedFiles = new ArrayList<Path>();

	/**
	 * List of files, which are needed to be parsed.
	 * It contains 1 item at the beginning, this is the root configuration file,
	 * others are included by the root or others recursively.
	 * Parsing is done when this list becomes empty.
	 */
	private final List<Path> toBeProcessedFiles = new ArrayList<Path>();

	private int tcpPort = 0;
	private String localAddress = null;
	private double killTimer = 10.0;
	private int numHCs = 0;
	private boolean unixDomainSocket = false;
	private final Map<String , String[]> groups = new HashMap<String, String[]>();
	private final Map<String, String> components = new HashMap<String, String>();
	private final List<String> executeElements = new ArrayList<String>();
	
	private final List<Throwable> exceptions = new ArrayList<Throwable>();
	private boolean processingErrorsDetected = false;

	private List<Integer> disallowedNodes;
	
	private Map<String, String> environmentalVariables;

	private boolean logFileNameDefined = false;

	private String mLogFileName = null;
	
	public ConfigFileHandler(){
		// Do nothing
	}

	/** 
	 * Returns true if the log file name was defined in the configuration file.
	 * @return true if the log file name was defined in the configuration file.
	 */
	public boolean isLogFileNameDefined() {
		return logFileNameDefined;
	}
	
	public String getLogFileName() {
		return mLogFileName;
	}
	
	public int getTcpPort(){
		return tcpPort;
	}
	
	public String getLocalAddress(){
		return localAddress;
	} 
	
	public int getNumHCs(){
		return numHCs;
	}
	
	public boolean unixDomainSocketEnabled(){
		return unixDomainSocket;
	}
	
	public double getKillTimer(){
		return killTimer;
	}
	
	public Map<String , String[]> getGroups(){
		return groups;
	}
	
	public Map<String, String> getComponents(){
		return components;
	}
	
	public List<String> getExecuteElements(){
		return executeElements;
	}
	
	public void setEnvMap(final Map<String, String> envMap){
		environmentalVariables = envMap;
	}
	
	public boolean isErroneous() {
		return processingErrorsDetected || !exceptions.isEmpty();
	}
	
	public List<Throwable> parseExceptions(){
		return exceptions;
	}

	/**
	 * Initializes the structure's data (only the parse tree), by processing the provided configuration file.
	 * <p>
	 * If this file includes other configuration files, than those files are also parsed here.
	 *
	 * @see #processASTs()
	 *
	 * @param first the absolute path of the root configuration file
	 * */
	public void readFromFile(final String first){
		originalASTs.clear();
		toBeProcessedFiles.add(new Path(first));
		while(!toBeProcessedFiles.isEmpty()){
			final Path actualFile = toBeProcessedFiles.get(0);
			if(!isAlreadyProcessed(actualFile)){
				processFile(actualFile);
				processedFiles.add(actualFile);
			}
			toBeProcessedFiles.remove(0);
		}
	}

	/**
	 * @param actualFile file name to check
	 * @return true if file is already processed (parsed)
	 */
	private boolean isAlreadyProcessed(final Path actualFile) {
		for (final Path processedFile : processedFiles) {
			if (processedFile.equals(actualFile)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Parsing, collecting includes and defines
	 * @param actualFile the file to process
	 */
	private void processFile(final Path actualFile) {
		final CfgAnalyzer analyzer = new CfgAnalyzer();
		final IWorkspaceRoot wroot = ResourcesPlugin.getWorkspace().getRoot();
		final IFile[] files = wroot.findFilesForLocationURI(URIUtil.toURI(actualFile));

		if (files.length == 0) {
			ErrorReporter.logError("The file " + actualFile.toOSString()
					+ " can not be mapped to a file resource in eclipse, and so is not accessible");
			processingErrorsDetected = true;
		} else if (files[0].isAccessible()){
			parseFile(actualFile, analyzer, files[0]);
		} else {
			ErrorReporter.logError("The file " + files[0].getLocationURI() + " can not be found");
			processingErrorsDetected = true;
		}
	}
	
	/**
	 * Processes the parsed tree structure of the configuration file, resolving expressions and extracting executor required data.
	 * This parsed tree already contains all of the data that were found in the configuration file,
	 * or in configuration files that can be reached from it via inclusion.
	 */
	public void processASTs() {
		// nothing to do, resolving is done in toStringResolved() on the fly
	}

	/**
	 * Creates the String representation of the parsed tree of all of the parsed files.
	 * Can be used to create a single configuration file instead of the hierarchy already existing.
	 * 
	 * @see #print(ParseTree, StringBuilder)
	 * @param disallowedNodes the list of nodes that should be left out of the process.
	 */
	public StringBuilder toStringResolved(final List<Integer> disallowedNodes){
		StringBuilder sb = new StringBuilder();
		// Creates the String representation of the parsed tree starting from the provided root node.
		CfgParseTreePrinter.printResolved( originalASTs, sb, disallowedNodes,
										   ResolveMode.IN_ROW, definesMap, environmentalVariables );
		return sb;
	}

	private void parseFile(final Path actualFile, final CfgAnalyzer analyzer, final IFile file) {
		analyzer.directParse(file, actualFile.toOSString(), null);

		final CfgParseResult cfgParseResult = analyzer.getCfgParseResult();
		if (cfgParseResult.isLogFileDefined()) {
			logFileNameDefined = true;
			mLogFileName  = cfgParseResult.getLogFileName();
			localAddress = cfgParseResult.getLocalAddress();
		}
		
		if (cfgParseResult.getTcpPort() != null) {
			tcpPort = cfgParseResult.getTcpPort();
		}
		if (cfgParseResult.getLocalAddress() != null) {
			localAddress = cfgParseResult.getLocalAddress();
		}
		if (cfgParseResult.getKillTimer() != null) {
			killTimer = cfgParseResult.getKillTimer();
		}
		if (cfgParseResult.getNumHcs() != null) {
			numHCs = cfgParseResult.getNumHcs();
		}
		if (cfgParseResult.isUnixDomainSocket() != null) {
			unixDomainSocket = cfgParseResult.isUnixDomainSocket();
		}
		
		final ParseTree rootNode = cfgParseResult.getParseTreeRoot();
		if ( rootNode != null ) {
			originalASTs.put( actualFile, cfgParseResult );

			final List<String> includeFiles = cfgParseResult.getIncludeFiles();
			for ( String filename:includeFiles ) {
				filename = PathConverter.getAbsolutePath( actualFile.toOSString(), filename );
				if ( filename != null ) {
					final Path filePath = new Path( filename );
					if ( !processedFiles.contains( filePath ) &&
						 !toBeProcessedFiles.contains( filePath ) ) {
						// make sure, that file is added only once
						toBeProcessedFiles.add( filePath );
					}
				}
			}

			definesMap.putAll( cfgParseResult.getDefinitions() );
			groups.putAll( cfgParseResult.getGroups() );
			components.putAll( cfgParseResult.getComponents() );
			executeElements.addAll( cfgParseResult.getExecuteElements() );

		}
	}
}
