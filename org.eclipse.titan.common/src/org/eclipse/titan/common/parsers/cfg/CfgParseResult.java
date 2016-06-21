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

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.titan.common.parsers.TITANMarker;

/**
 * Record to store extracted data from cfg file parsing
 * @author Arpad Lovassy
 */
public class CfgParseResult {

	/** the result parse tree, which was built during the parsing */
	private ParseTree mParseTreeRoot = null;
	
	/**
	 * the result of the tokenizing, done by the lexer.
	 * It contains all the tokens from all the channels, so it contains also the hidden tokens as well.
	 * A token is hidden, if token channel > 0
	 */
	private CommonTokenStream mTokenStream = null;
	
	/**
	 * list of syntactic warnings collected during parsing
	 */
	private List<TITANMarker> mWarnings = new ArrayList<TITANMarker>();
	
	/** included file names from [INCLUDE] section */
	private List<String> mIncludeFiles = new ArrayList<String>();

	/**
	 * true if and only if LogFile parameter is defined in [LOGGING] section 
	 */
	private boolean mLogFileDefined = false;
	
	/**
	 * Format string of the log file. <br>
	 * This value is read from the [LOGGING] section with this parameter:
	 * <pre>
	 * LogFile := "&lt;log_file_name&gt;"
	 * </pre>
	 * where log_file_name is a relative file name to the bin/ directory of the TTCN-3 project,
	 * it must contain "%n" (node), which will be for example "hc" or "mtc". <br>
	 * Example:
	 * <pre>
	 * LogFile := "../log/MyExample-%n.log"
	 * </pre>
	 * This will create the following log files if there is 1 HC: <br>
	 * &lt;project_dir&gt;/log/MyExample-hc.log <br>
	 * &lt;project_dir&gt;/log/MyExample-mtc.log
	 */
	private String mLogFileName = null;
	
	private Integer mTcpPort = null;
	
	private String mLocalAddress = null;
	
	/**
	 * Setting for kill timer (in seconds).
	 * The executing process is killed, if there is no answer form the Main Controller. <br>
	 * NOTE: this value has effect only for the Titan Eclipse Executor plug-in, works only in JNI Executor mode <br>
	 * This value is read from the [MAIN_CONTROLLER] section with this parameter:
	 * <pre>
	 * KillTimer := &lt;float&gt;;
	 * </pre>
	 */
	private Double mKillTimer = null;
	
	/**
	 * Number of Host Controllers. <br>
	 * This value is read from the [MAIN_CONTROLLER] section with this parameter:
	 * <pre>
	 * NumHCs := &lt;integer&gt;;
	 * </pre>
	 */
	private Integer mNumHcs = null;
	
	private Boolean mUnixDomainSocket = null;
	
	private Map<String, String> mComponents = new HashMap<String, String>();
	
	private Map<String, String[]> mGroups = new HashMap<String, String[]>();
	
	private List<String> mExecuteElements = new ArrayList<String>();
	
	public ParseTree getParseTreeRoot() {
		return mParseTreeRoot;
	}

	public void setParseTreeRoot(ParseTree aParseTreeRoot) {
		this.mParseTreeRoot = aParseTreeRoot;
	}

	public CommonTokenStream getTokenStream() {
		return mTokenStream;
	}

	public void setTokenStream(CommonTokenStream aTokenStream) {
		this.mTokenStream = aTokenStream;
	}

	public List<TITANMarker> getWarnings() {
		return mWarnings;
	}
	
	public void setWarnings(List<TITANMarker> aWarnings) {
		this.mWarnings = aWarnings;
	}
	
	public List<String> getIncludeFiles() {
		return mIncludeFiles;
	}
	
	public void setIncludeFiles(List<String> aIncludeFiles) {
		this.mIncludeFiles = aIncludeFiles;
	}
	
	public boolean isLogFileDefined() {
		return mLogFileDefined;
	}
	
	public void setLogFileDefined(boolean aLogFileDefined) {
		this.mLogFileDefined = aLogFileDefined;
	}
	
	public String getLogFileName() {
		return mLogFileName;
	}
	
	public void setLogFileName(String aLogFileName) {
		this.mLogFileName = aLogFileName;
	}
	
	public Integer getTcpPort() {
		return mTcpPort;
	}
	
	public void setTcpPort(Integer aTcpPort) {
		this.mTcpPort = aTcpPort;
	}
	
	public String getLocalAddress() {
		return mLocalAddress;
	}
	
	public void setLocalAddress(String aLocalAddress) {
		this.mLocalAddress = aLocalAddress;
	}
	
	public Double getKillTimer() {
		return mKillTimer;
	}
	
	public void setKillTimer(Double aKillTimer) {
		this.mKillTimer = aKillTimer;
	}
	
	public Integer getNumHcs() {
		return mNumHcs;
	}
	
	public void setNumHcs(Integer aNumHcs) {
		this.mNumHcs = aNumHcs;
	}
	
	public Boolean isUnixDomainSocket() {
		return mUnixDomainSocket;
	}
	
	public void setUnixDomainSocket(Boolean aUnixDomainSocket) {
		this.mUnixDomainSocket = aUnixDomainSocket;
	}
	
	public Map<String, String> getComponents() {
		return mComponents;
	}
	
	public void setComponents(Map<String, String> aComponents) {
		this.mComponents = aComponents;
	}
	
	public Map<String, String[]> getGroups() {
		return mGroups;
	}
	
	public void setGroups(Map<String, String[]> aGroups) {
		this.mGroups = aGroups;
	}
	
	public List<String> getExecuteElements() {
		return mExecuteElements;
	}
	
	public void setExecuteElements(List<String> aExecuteElements) {
		this.mExecuteElements = aExecuteElements;
	}
}
