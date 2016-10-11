/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.exceptions;

/**
 * Base class for user exceptions
 *
 */
public class UserException extends TitanLogException {

	private static final long serialVersionUID = 4564418820643754603L;

	/**
	 * Constructor for throwable
	 * @param t the throwable
	 */
	public UserException(final Throwable t) {
		super(t);
	}
	
	/**
	 * Constructor for messages
	 * @param msg the message
	 */
	public UserException(final String msg) {
		super(msg);
	}
}
