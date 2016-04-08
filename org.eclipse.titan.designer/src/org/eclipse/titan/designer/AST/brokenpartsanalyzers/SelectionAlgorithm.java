/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import org.eclipse.titan.designer.preferences.PreferenceConstantValues;

/**
 * This is a helper class to map string of selection algorithm to enum value.
 * 
 * @author Peter Olah
 */
public enum SelectionAlgorithm {
	
	MODULESELECTIONORIGINAL(PreferenceConstantValues.MODULESELECTIONORIGINAL),
	BROKENREFERENCESINVERTED(PreferenceConstantValues.BROKENPARTSVIAREFERENCES);
	
	private String readableName;
	
	private SelectionAlgorithm(final String name) {
		readableName = name;
	}
	
	public static SelectionAlgorithm fromString(final String name) {
		if (name != null) {
			for (SelectionAlgorithm s : SelectionAlgorithm.values()) {
				if (name.equalsIgnoreCase(s.readableName)) {
					return s;
				}
			}
		}
		return null;
	}
	
	public static SelectionAlgorithm getDefaultValue() {
		return SelectionAlgorithm.MODULESELECTIONORIGINAL;
	}
}