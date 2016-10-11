/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * @author Kristof Szabados
 * */
public final class KeywordLessIdentifier extends Identifier {

	public KeywordLessIdentifier(final Identifier_type idType, final String name) {
		this(idType, name, NULL_Location.INSTANCE);
	}

	public KeywordLessIdentifier(final Identifier_type idType, final String name, final Location location) {
		this(idType, 	name, location, true);
	}

	protected KeywordLessIdentifier(final Identifier_type idType, final String name,
			final Location location, final boolean dontregister) {
		type = idType;
		this.location = location;

		String realName;
		switch(idType) {
		case ID_ASN:
			if (name.length() > 0 && name.charAt(0) == '&') {
				realName = Identifier_Internal_Data.asnToName(name.substring(1));
			} else {
				realName = Identifier_Internal_Data.asnToName(name);
			}

			idData = new Identifier_Internal_Data(realName, name, null);
			break;
		case ID_TTCN:
			realName = Identifier_Internal_Data.ttcnToName(name);
			idData = new Identifier_Internal_Data(realName, null, name);
			break;
		case ID_NAME:
		default:
			idData = new Identifier_Internal_Data(name, null, null);
			break;
		}
	}


}
