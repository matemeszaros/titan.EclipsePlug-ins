/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.navigator;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;

/**
 * This class is used to control which listeners that are
 * added to the projects viewer used in the navigator view
 *
 */
public class ProjectsViewerMenuManager extends MenuManager { 
	
	/**
	 * Constructor 
	 */
	public ProjectsViewerMenuManager() {
		super("#PopupMenu"); //$NON-NLS-1$
	}
	
	@Override
	public void addMenuListener(final IMenuListener listener) {
		// Prevent other listeners then our own to remove additions

		if (listener instanceof ProjectsViewerMenuListener) {
			super.addMenuListener(listener);
		}
	}
}
