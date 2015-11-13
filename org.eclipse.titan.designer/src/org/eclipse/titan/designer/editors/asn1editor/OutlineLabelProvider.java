/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.designer.AST.IOutlineElement;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public final class OutlineLabelProvider extends LabelProvider {
	@Override
	public Image getImage(final Object element) {
		String iconName = "titan.gif";
		if (element instanceof IOutlineElement) {
			IOutlineElement e = (IOutlineElement) element;
			iconName = e.getOutlineIcon();
		}

		return ImageCache.getImage(iconName);
	}

	@Override
	public String getText(final Object element) {
		Identifier identifier = null;
		if (element instanceof IOutlineElement) {
			IOutlineElement e = (IOutlineElement) element;
			String outlineText = e.getOutlineText();
			if (outlineText.length() != 0) {
				return outlineText;
			}
			identifier = e.getIdentifier();
		}

		if (identifier == null) {
			return "unknown";
		}

		return identifier.getDisplayName();
	}
}
