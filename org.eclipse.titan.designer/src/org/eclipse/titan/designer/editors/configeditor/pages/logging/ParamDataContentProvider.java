/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.logging;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.LogParamEntry;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.PluginSpecificParam;

/**
 * @author Balazs Andor Zalanyi
 * */
public class ParamDataContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement != null) {
			if (inputElement instanceof LogParamEntry) {
				List<PluginSpecificParam> list = ((LogParamEntry) inputElement).getPluginSpecificParam();
				if (list == null) {
					return new Object[] {};
				}
				return list.toArray();
			}
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

}
