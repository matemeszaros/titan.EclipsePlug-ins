/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class MCSectionHandler {

	private ParseTree lastSectionRoot = null;

	private ParseTree localAddress = null;
	private ParseTree localAddressRoot = null;
	private ParseTree tcpPort = null;
	private ParseTree tcpPortRoot = null;
	private ParseTree killTimer = null;
	private ParseTree killTimerRoot = null;
	private ParseTree numHCsText = null;
	private ParseTree numHCsTextRoot = null;
	private ParseTree unixDomainSocket = null;
	private ParseTree unixDomainSocketRoot = null;

	public ParseTree getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(final ParseTree lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public ParseTree getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(final ParseTree localAddress) {
		this.localAddress = localAddress;
	}

	public ParseTree getLocalAddressRoot() {
		return localAddressRoot;
	}

	public void setLocalAddressRoot(final ParseTree localAddressRoot) {
		this.localAddressRoot = localAddressRoot;
	}

	public ParseTree getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(final ParseTree tcpPort) {
		this.tcpPort = tcpPort;
	}

	public ParseTree getTcpPortRoot() {
		return tcpPortRoot;
	}

	public void setTcpPortRoot(final ParseTree tcpPortRoot) {
		this.tcpPortRoot = tcpPortRoot;
	}

	public ParseTree getKillTimer() {
		return killTimer;
	}

	public void setKillTimer(final ParseTree killTimer) {
		this.killTimer = killTimer;
	}

	public ParseTree getKillTimerRoot() {
		return killTimerRoot;
	}

	public void setKillTimerRoot(final ParseTree killTimerRoot) {
		this.killTimerRoot = killTimerRoot;
	}

	public ParseTree getNumHCsText() {
		return numHCsText;
	}

	public void setNumHCsText(final ParseTree numHCsText) {
		this.numHCsText = numHCsText;
	}

	public ParseTree getNumHCsTextRoot() {
		return numHCsTextRoot;
	}

	public void setNumHCsTextRoot(final ParseTree numHCsTextRoot) {
		this.numHCsTextRoot = numHCsTextRoot;
	}

	public ParseTree getUnixDomainSocket() {
		return unixDomainSocket;
	}

	public void setUnixDomainSocket(final ParseTree unixDomainSocket) {
		this.unixDomainSocket = unixDomainSocket;
	}

	public ParseTree getUnixDomainSocketRoot() {
		return unixDomainSocketRoot;
	}

	public void setUnixDomainSocketRoot(final ParseTree unixDomainSocketRoot) {
		this.unixDomainSocketRoot = unixDomainSocketRoot;
	}
}
