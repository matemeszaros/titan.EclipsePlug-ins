/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.testportpar;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler.TestportParameter;

/**
 * @author Dimitrov Peter
 * */
public final class TestportParameterDataLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if (element != null && element instanceof TestportParameter) {
			TestportParameter parameter = (TestportParameter) element;
			String text;
			switch (columnIndex) {
			case 0:
				if (parameter.getComponentName() == null) {
					return "";
				}

				text = parameter.getComponentName().getText();
				if (text == null || text.length() == 0) {
					text = ConfigTreeNodeUtilities.toString(parameter.getComponentName());
				}

				return text;
			case 1:
				if (parameter.getTestportName() == null) {
					return "";
				}

				text = parameter.getTestportName().getText();
				if (text == null || text.length() == 0) {
					text = ConfigTreeNodeUtilities.toString(parameter.getTestportName());
				}

				return text;
			case 2:
				return parameter.getParameterName().getText();
			default:
				return "";
			}
		}

		return "";
	}
}
