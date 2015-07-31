/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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

import org.eclipse.core.runtime.Path;
import org.eclipse.titan.common.parsers.ILocationAST;

/**
 * This class handles the parsing and resolving of configuration files,
 * and is extracting data from them required by the executors (actually the Main Controller)
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy 
 */
public abstract class ConfigFileHandler {
	private static final String ORIGANLLY_FROM = "//This part was originally found in file: ";

	protected Map<Path, ILocationAST> originalASTs = new HashMap<Path, ILocationAST>();
	protected Map<Path, ILocationAST> resolvedASTs = new HashMap<Path, ILocationAST>();
	protected final Map<String, CfgDefinitionInformation> definesMap = new HashMap<String, CfgDefinitionInformation>();
	private final List<Path> processedFiles = new ArrayList<Path>();
	protected final List<Path> toBeProcessedFiles = new ArrayList<Path>();
	
	protected int tcpPort = 0;
	protected String localAddress = null;
	protected double killTimer = 10.0;
	protected int numHCs = 0;
	private boolean unixDomainSocket = false;
	protected final Map<String , String[]> groups = new HashMap<String, String[]>();
	protected final Map<String, String> components = new HashMap<String, String>();
	protected final List<String> executeElements = new ArrayList<String>();
	
	protected final List<Throwable> exceptions = new ArrayList<Throwable>();
	protected boolean processingErrorsDetected = false;

	protected List<Integer> disallowedNodes;
	
	protected Map<String, String> environmentalVariables;

	protected boolean logFileNameDefined = false;

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
	
	public void setEnvMap(Map<String, String> envMap){
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
			Path actualFile = toBeProcessedFiles.get(0);
			if(!isAlreadyProcessed(actualFile)){
				processFile(actualFile);
				processedFiles.add(actualFile);
			}
			toBeProcessedFiles.remove(0);
		}
	}

	private boolean isAlreadyProcessed(Path actualFile) {
		for (Path processedFile : processedFiles) {
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
	protected abstract void processFile(Path actualFile);

	/**
	 * Processes the parsed tree structure of the configuration file, resolving expressions and extracting executor required data.
	 * This parsed tree already contains all of the data that were found in the configuration file,
	 * or in configuration files that can be reached from it via inclusion.
	 */
	public abstract void processASTs();

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
	private StringBuilder toStringInternal(final Map<Path, ILocationAST> asts, final List<Integer> disallowedNodes){
		if(asts == null || asts.isEmpty()){
			return new StringBuilder();
		}
		
		this.disallowedNodes = disallowedNodes;
		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.setLength(0);
		
		for(Entry<Path, ILocationAST> entry:asts.entrySet()){
			stringbuilder.append(ORIGANLLY_FROM).
				append(entry.getKey().toOSString()).append('\n');
			if(entry.getValue() != null){
				print(entry.getValue(), stringbuilder);
			}
		}

		this.disallowedNodes = null;
		return stringbuilder;
	}

	/**
	 * Prints out the hidden values and the visible value of a tree node, and calls itself recursively on it's children
	 * 
	 * @see #toStringOriginal(List)
	 * @see #toStringResolved(List)
	 * 
	 * @param root the tree root to start at.
	 */
	protected abstract void print(final ILocationAST root, final StringBuilder stringbuilder);
}
