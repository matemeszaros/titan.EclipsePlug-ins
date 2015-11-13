/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.modulepar;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler.ModuleParameter;

/**
 * @author Dimitrov Peter
 * */
public final class ModuleParameterDataLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if (element != null && element instanceof ModuleParameter) {
			ModuleParameter parameter = (ModuleParameter) element;
			switch (columnIndex) {
			case 0:
				if (parameter.getModuleName() == null) {
					return "";
				}

				return parameter.getModuleName().getText();
			case 1:
				return parameter.getParameterName().getText();
			default:
				return "";
			}
		}

		return "";
	}

}
