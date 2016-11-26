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

public class Field {
	final String type;
	final String name;

	public Field(String type, String name) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {
		return type + " " + name;
	}
}
