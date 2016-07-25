/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

/**
 * USED FOR LOGGING PURPOSES
 * Interface for token name resolving
 * @see TokenNameResolver
 * @author Arpad Lovassy
 */
public interface ILexerLogUtil {

	/**
	 * @param aIndex token type index
	 * @return resolved token name
	 */
	public String getTokenName( int aIndex );
}
