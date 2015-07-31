/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * @author Kristof Szabados
 * */
public final class IndentationSupport {

	private static String indentationString = null;

	private IndentationSupport() {
		// Hide constructor
	}

	/** clear the indentation string */
	public static void clearIndentString() {
		indentationString = null;
	}

	/** @return the current indentation string */
	public static String getIndentString() {
		if (indentationString != null) {
			return indentationString;
		}

		String tabPolicy = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.INDENTATION_TAB_POLICY);
		String indentSizeString = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.INDENTATION_SIZE);
		int indentSize;
		// This checking is leaved for sure.
		if (indentSizeString.isEmpty()) {
			indentSize = 2;
		} else {
			indentSize = Integer.parseInt(indentSizeString);
		}
		if (PreferenceConstantValues.TAB_POLICY_1.equals(tabPolicy)) {
			indentationString = "\t";
		} else if (PreferenceConstantValues.TAB_POLICY_2.equals(tabPolicy)) {
			StringBuilder sb = new StringBuilder(8);
			for (int i = 0; i < indentSize; i++) {
				sb.append(" ");
			}
			indentationString = sb.toString();
		} else {
			indentationString = "  ";
		}

		return indentationString;
	}
}
