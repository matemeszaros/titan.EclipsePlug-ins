/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.modulepar;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler;

/**
 * @author Dimitrov Peter
 * */
public final class ModuleParameterDataContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement != null) {
			if (inputElement instanceof ModuleParameterSectionHandler) {
				return ((ModuleParameterSectionHandler) inputElement).getModuleParameters().toArray();
			}
		}

		return new Object[] {};
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

}
