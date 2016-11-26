/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import org.eclipse.titan.codegenerator.TTCN3JavaAPI.BITSTRING;
import org.eclipse.titan.codegenerator.TTCN3JavaAPI.BOOLEAN;
import org.eclipse.titan.codegenerator.TTCN3JavaAPI.CHARSTRING;
import org.eclipse.titan.codegenerator.TTCN3JavaAPI.FLOAT;
import org.eclipse.titan.codegenerator.TTCN3JavaAPI.INTEGER;
import org.eclipse.titan.codegenerator.TTCN3JavaAPI.OCTETSTRING;

public enum TypeMapper {
	BitString("bitstring", BITSTRING.class),
	CharString("charstring", CHARSTRING.class),
	OctetString("octetstring", OCTETSTRING.class),
	Boolean("boolean", BOOLEAN.class),
	Float("float", FLOAT.class),
	Integer("integer", INTEGER.class);

	private final String type;
	private final Class cls;

	TypeMapper(String type, Class cls) {
		this.type = type;
		this.cls = cls;
	}

	/**
	 * Mapper function that maps a typename to its respective class.
	 * If not supported, returns the input string
	 * @param type the typename to map
	 * @return the simple name of the respective class, or the input string if not supported.
	 */
	public static String map(String type) {
		for (TypeMapper value : values()) {
			if (value.type.equals(type)) {
				return value.cls.getSimpleName();
			}
		}
		// TODO : log a warning, that the type is not supported
		System.out.println("Unknown type: " + type);
		return type;
	}
}
