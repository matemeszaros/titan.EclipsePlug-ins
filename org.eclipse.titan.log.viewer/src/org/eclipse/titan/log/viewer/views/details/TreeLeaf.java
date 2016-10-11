/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.details;

public class TreeLeaf extends TreeObject {

	private String value;
	
	public TreeLeaf(final String name, final String value) {
		super(name);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	
	@Override
	public String toString() {
		return getName() + " := " + value;
	}
	
}
