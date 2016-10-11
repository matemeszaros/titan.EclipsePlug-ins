/*******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.views.msc.ui.core;

/**
 * @author Szabolcs Beres
 * */
public abstract class PortEventNode extends BaseMessage {
	protected String sourcePort;
	protected String targetPort;

	public PortEventNode(final String sourcePort, final String targetPort) {
		super(0);
		this.sourcePort = sourcePort;
		this.targetPort = targetPort;
	}
}
