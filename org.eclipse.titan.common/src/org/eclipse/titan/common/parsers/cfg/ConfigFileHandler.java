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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.path.PathConverter;

/**
 * This class handles the parsing and resolving of configuration files,
 * and is extracting data from them required by the executors (actually the Main Controller)
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy 
 */
public final class ConfigFileHandler {
	private static final String ORIGINALLY_FROM = "//This part was originally found in file: ";

	private Map<Path, LocationAST> originalASTs = new HashMap<Path, LocationAST>();
	private Map<Path, LocationAST> resolvedASTs = new HashMap<Path, LocationAST>();
	private final Map<String, CfgDefinitionInformation> definesMap = new HashMap<String, CfgDefinitionInformation>();
	private final List<Path> processedFiles = new ArrayList<Path>();
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
		resolvedASTs.clear();
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
		final IFile[] files = wroot.findFilesForLocation(actualFile);

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
		//FIXME implement
	}

	/**
	 * Creates the String representation of the parsed tree of the root configuration file.
	 * Can also be used to filter out  some of its nodes before printing it to a file.
	 * 
	 * @see #print(org.eclipse.titan.common.parsers.LocationAST, StringBuilder)
	 * @param disallowedNodes the list of nodes that should be left out of the process.
	 * */
	public StringBuilder toStringOriginal(final List<Integer> disallowedNodes){
		return toStringInternal(originalASTs, disallowedNodes);
	}

	/**
	 * Creates the String representation of the parsed tree of all of the parsed files.
	 * Can be used to create a single configuration file instead of the hierarchy already exisiting.
	 * 
	 * @see #print(org.eclipse.titan.common.parsers.LocationAST, StringBuilder)
	 * @param disallowedNodes the list of nodes that should be left out of the process.
	 * */
	public StringBuilder toStringResolved(final List<Integer> disallowedNodes){
		return toStringInternal(resolvedASTs, disallowedNodes);
	}

	/**
	 * Creates the String representation of the parsed tree starting from the provided root node.
	 * 
	 * @see #print(org.eclipse.titan.common.parsers.LocationAST, StringBuilder)
	 * @param asts the root node of the parse tree to start from.
	 * @param disallowedNodes the list of nodes that should be left out of the process.
	 * */
	private StringBuilder toStringInternal(final Map<Path, LocationAST> asts, final List<Integer> disallowedNodes){
		if(asts == null || asts.isEmpty()){
			return new StringBuilder();
		}
		
		this.disallowedNodes = disallowedNodes;
		final StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.setLength(0);
		
		for(final Entry<Path, LocationAST> entry:asts.entrySet()){
			stringbuilder.append(ORIGINALLY_FROM).
				append(entry.getKey().toOSString()).append('\n');
			if(entry.getValue() != null){
				print(entry.getValue(), stringbuilder);
			}
		}

		this.disallowedNodes = null;
		return stringbuilder;
	}

	private void parseFile(final Path actualFile, final CfgAnalyzer analyzer, final IFile file) {
		analyzer.directParse(file, actualFile.toOSString(), null);

		if (analyzer.isLogFileNameDefined()) {
			logFileNameDefined = true;
			mLogFileName  = analyzer.getLogFileName();
			localAddress = analyzer.getLocalAddress();
		}

		final LocationAST rootNode = new LocationAST( analyzer.getParseTreeRoot() );
		if ( rootNode != null ) {
			originalASTs.put( actualFile, rootNode );

			final List<String> includeFiles = analyzer.getIncludeFilePaths();
			for ( String filename:includeFiles ) {
				filename = PathConverter.getAbsolutePath( actualFile.toOSString(), filename );
				if ( filename != null ) {
					toBeProcessedFiles.add( new Path( filename ) );
				}
			}

			definesMap.putAll( analyzer.getDefinitions() );
			executeElements.addAll( analyzer.getExecuteElements() );

		}
	}
	
	/**
	 * Prints out the hidden values and the visible value of a tree node, and calls itself recursively on it's children
	 * 
	 * @see #toStringOriginal(List)
	 * @see #toStringResolved(List)
	 * 
	 * @param root the tree root to start at.
	 */
	private void print(final LocationAST root, final StringBuilder stringbuilder) {
		CommonHiddenStreamToken hidden = root.getHiddenBefore();
		if(hidden != null){
			while(hidden.getHiddenBefore() != null){
				hidden = hidden.getHiddenBefore();
			}
			while(hidden != null){
				stringbuilder.append(hidden.getText());
				hidden = hidden.getHiddenAfter();
			}
		}
		stringbuilder.append(root.getText());
		LocationAST child = root.getFirstChild();
		while(child != null){
			final Integer tempType = child.getType();
			if(disallowedNodes != null && !disallowedNodes.contains(tempType)){
				print(child, stringbuilder);
			}
			child = child.getNextSibling();
		}
	}
	
}
