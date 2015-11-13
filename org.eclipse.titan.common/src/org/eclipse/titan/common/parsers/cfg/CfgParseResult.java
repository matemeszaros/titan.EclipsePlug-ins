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

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.titan.common.parsers.TITANMarker;

/**
 * Record to store extracted data from cfg file parsing
 * @author Arpad Lovassy
 */
public class CfgParseResult {

	private ParserRuleContext mParseTreeRoot = null;
	
	private List<TITANMarker> mWarnings = new ArrayList<TITANMarker>();
	
	private List<String> mIncludeFiles = new ArrayList<String>();

	private boolean mLogFileDefined = false;
	
	private Integer mTcpPort = null;
	
	private String mLocalAddress = null;
	
	private Double mKillTimer = null;
	
	private Integer mNumHcs = null;
	
	private Boolean mUnixDomainSocket = null;
	
	private Map<String, String> mComponents = new HashMap<String, String>();
	
	private Map<String , String[]> mGroups = new HashMap<String, String[]>();
	
	private List<String> mExecuteElements = new ArrayList<String>();
	
	public ParserRuleContext getParseTreeRoot() {
		return mParseTreeRoot;
	}

	public void setParseTreeRoot(ParserRuleContext aParseTreeRoot) {
		this.mParseTreeRoot = aParseTreeRoot;
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
	
	public Boolean getUnixDomainSocket() {
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
