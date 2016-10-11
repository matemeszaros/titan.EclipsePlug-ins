/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.logging;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler;

/**
 * @author Adam Delic
 * */
public final class LoggerTreeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof LoggingSectionHandler) {
			LoggingSectionHandler lsh = (LoggingSectionHandler) inputElement;
			return lsh.getComponentsTreeElementArray();
		}
		return new Object[] {};
	}

	@Override
	public void dispose() {
		//Do nothing
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		//Do nothing
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof LoggingSectionHandler.LoggerTreeElement) {
			LoggingSectionHandler.LoggerTreeElement compLTE = (LoggingSectionHandler.LoggerTreeElement) parentElement;
			if (compLTE.getPluginName() != null) {
				ErrorReporter.INTERNAL_ERROR("plugin has children");
			}
			return compLTE.getLsh().getPluginsTreeElementArray(compLTE.getComponentName());
		}
		return new Object[] {};
	}

	@Override
	public Object getParent(final Object element) {
		if (element instanceof LoggingSectionHandler.LoggerTreeElement) {
			LoggingSectionHandler.LoggerTreeElement lte = (LoggingSectionHandler.LoggerTreeElement) element;
			if (lte.getPluginName() == null) {
				// this is a component element
				return null;
			}

			// this is a plugin element
			return new LoggingSectionHandler.LoggerTreeElement(lte.getLsh(), lte.getComponentName());
		}
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof LoggingSectionHandler.LoggerTreeElement) {
			LoggingSectionHandler.LoggerTreeElement lte = (LoggingSectionHandler.LoggerTreeElement) element;
			return lte.getPluginName() == null;
		}
		return false;
	}

}
