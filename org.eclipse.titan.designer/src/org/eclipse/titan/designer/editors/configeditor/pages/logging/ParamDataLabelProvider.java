/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.logging;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.PluginSpecificParam;

/**
 * @author Balazs Andor Zalanyi
 * */
public class ParamDataLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if (element != null && element instanceof PluginSpecificParam) {
			PluginSpecificParam item = (PluginSpecificParam) element;
			switch (columnIndex) {
			case 0:
				String param = item.getParamName();
				if (param == null || param.length() == 0) {
					return "";
				}
				return param;
			case 1:
				String value = item.getValue().getText();
				if (value == null || value.length() == 0) {
					return "";
				}
				return value;
			default:
				return "";
			}
		}
		return "";
	}

}
