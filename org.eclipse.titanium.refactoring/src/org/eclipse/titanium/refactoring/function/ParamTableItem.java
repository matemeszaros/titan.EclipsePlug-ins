/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

/**
 * This class represents a line of the params' table on wizard page#2
 * 
 * @author Viktor Varga
 */
class ParamTableItem {

	private final ArgumentPassingType passType;
	private final String type;
	/**
	 * content editable by the user through the wizard ui
	 */
	private final StringBuilder name;

	ParamTableItem(ArgumentPassingType passType, String type, StringBuilder name) {
		this.passType = passType;
		this.type = type;
		this.name = name;
	}

	String getPassType() {
		return passType.toString();
	}

	String getType() {
		return type;
	}

	String getName() {
		return name.toString();
	}

	void setName(String newName) {
		name.setLength(0);
		name.append(newName);
	}

}
