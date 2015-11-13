/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

/**
 * Parse a message single mode and file format 1
 */
public class MessageAnalyserSingle1 extends MessageAnalyserFormat1 {

	@Override
	public String getType() {
		return "MessageAnalyserSingle1"; //$NON-NLS-1$
	}
	
	@Override
	protected boolean isSystemCreation() {
		return this.message.contains(ETS_STARTUP);
	}

	@Override
	protected boolean isSystemTermination() {
		return this.message.contains(ETS_TERMINATION);
	}

}

