/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.execute;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler.ExecuteItem;

/**
 * @author Kristof Szabados
 * */
public final class ExecuteDataLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if (element != null && element instanceof ExecuteItem) {
			ExecuteItem item = (ExecuteItem) element;
			switch (columnIndex) {
			case 0:
				return item.getModuleName().getText();
			case 1:
				if (item.getTestcaseName() == null) {
					return "";
				}

				return item.getTestcaseName().getText();
			default:
				return "";
			}
		}

		return "";
	}
}
