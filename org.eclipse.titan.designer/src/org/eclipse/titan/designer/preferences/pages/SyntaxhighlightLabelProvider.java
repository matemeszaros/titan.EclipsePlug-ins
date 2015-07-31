/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * @author Kristof Szabados
 * */
final class SyntaxhighlightLabelProvider extends LabelProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(final Object element) {
		if (element instanceof SyntaxHighlightColoringGroup) {
			return ((SyntaxHighlightColoringGroup) element).name;
		} else if (element instanceof SyntaxHighlightColoringElement) {
			return ((SyntaxHighlightColoringElement) element).getName();
		}
		return super.getText(element);
	}

}
