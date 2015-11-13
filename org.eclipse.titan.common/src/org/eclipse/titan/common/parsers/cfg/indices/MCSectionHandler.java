/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import org.eclipse.titan.common.parsers.LocationAST;

/**
 * @author Kristof Szabados
 * */
public final class MCSectionHandler {

	private LocationAST lastSectionRoot = null;

	private LocationAST localAddress = null;
	private LocationAST localAddressRoot = null;
	private LocationAST tcpPort = null;
	private LocationAST tcpPortRoot = null;
	private LocationAST killTimer = null;
	private LocationAST killTimerRoot = null;
	private LocationAST numHCsText = null;
	private LocationAST numHCsTextRoot = null;
	private LocationAST unixDomainSocket = null;
	private LocationAST unixDomainSocketRoot = null;

	public LocationAST getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(LocationAST lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public LocationAST getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(LocationAST localAddress) {
		this.localAddress = localAddress;
	}

	public LocationAST getLocalAddressRoot() {
		return localAddressRoot;
	}

	public void setLocalAddressRoot(LocationAST localAddressRoot) {
		this.localAddressRoot = localAddressRoot;
	}

	public LocationAST getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(LocationAST tcpPort) {
		this.tcpPort = tcpPort;
	}

	public LocationAST getTcpPortRoot() {
		return tcpPortRoot;
	}

	public void setTcpPortRoot(LocationAST tcpPortRoot) {
		this.tcpPortRoot = tcpPortRoot;
	}

	public LocationAST getKillTimer() {
		return killTimer;
	}

	public void setKillTimer(LocationAST killTimer) {
		this.killTimer = killTimer;
	}

	public LocationAST getKillTimerRoot() {
		return killTimerRoot;
	}

	public void setKillTimerRoot(LocationAST killTimerRoot) {
		this.killTimerRoot = killTimerRoot;
	}

	public LocationAST getNumHCsText() {
		return numHCsText;
	}

	public void setNumHCsText(LocationAST numHCsText) {
		this.numHCsText = numHCsText;
	}

	public LocationAST getNumHCsTextRoot() {
		return numHCsTextRoot;
	}

	public void setNumHCsTextRoot(LocationAST numHCsTextRoot) {
		this.numHCsTextRoot = numHCsTextRoot;
	}

	public LocationAST getUnixDomainSocket() {
		return unixDomainSocket;
	}

	public void setUnixDomainSocket(LocationAST unixDomainSocket) {
		this.unixDomainSocket = unixDomainSocket;
	}

	public LocationAST getUnixDomainSocketRoot() {
		return unixDomainSocketRoot;
	}

	public void setUnixDomainSocketRoot(LocationAST unixDomainSocketRoot) {
		this.unixDomainSocketRoot = unixDomainSocketRoot;
	}
}
