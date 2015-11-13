/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.exceptions;

/**
 * Base class for TITAN exceptions
 *
 */
public class TitanLogException extends Exception {

	private static final long serialVersionUID = -5960457089587294859L;

	/**
	 * Constructor for throwable
	 * @param t the throwable
	 */
	public TitanLogException(final Throwable t) {
		super(t);
	}
	
	/**
	 * Constructor for messages
	 * @param msg the message
	 */
	public TitanLogException(final String msg) {
		super(msg);
	}

	public TitanLogException(String message, Throwable cause) {
		super(message, cause);
	}
}
