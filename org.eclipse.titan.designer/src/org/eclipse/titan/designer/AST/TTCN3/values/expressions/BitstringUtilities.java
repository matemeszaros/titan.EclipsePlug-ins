/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

/**
 * Common bitstring utilities.
 * 
 * @author Kristof Szabados
 * */
public final class BitstringUtilities {
	protected static final byte[] DIGITS = new byte[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/** private constructor to disable instantiation */
	private BitstringUtilities() {
	}

	/**
	 * Converts the provided character into a hexadecimal value.
	 * 
	 * @param c
	 *                the character to be converted
	 * 
	 * @return the hexadecimal value.
	 * */
	public static int charToHexdigit(final byte c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		} else if (c >= 'A' && c <= 'F') {
			return c - 'A' + 10;
		} else {
			return c - 'a' + 10;
		}
	}

	/**
	 * Converts the provided hexadecimal value into a character.
	 * 
	 * @param value
	 *                the value to be converted
	 * 
	 * @return the resulting character
	 * */
	public static byte hexdigitToChar(final int value) {
		if (value < 10) {
			return (byte) ('0' + value);
		}

		return (byte) ('A' + value - 10);
	}
}
