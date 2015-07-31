/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.details;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.utils.Constants;

/**
 * This class manage the tree icons
 *  
 */
public class DetailsViewLabelProvider extends LabelProvider {

	@Override
	public String getText(final Object obj) {
		return obj.toString();
	}

	@Override
	public Image getImage(final Object obj) {
		String imageKey = Constants.ICONS_CHILD_OBJ;
		if (obj instanceof TreeParent) {
			imageKey = Constants.ICONS_PARENT_OBJ;
		}

		if (obj.toString().length() == 0) {
			imageKey = Constants.ICONS_EMPTY_PARENT;
		}
		return Activator.getDefault().getIcon(imageKey); 
	}
}
