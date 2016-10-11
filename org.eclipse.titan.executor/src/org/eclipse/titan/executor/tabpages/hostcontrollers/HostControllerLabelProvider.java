/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.hostcontrollers;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.executor.HostController;

/**
 * @author Kristof Szabados
 * */
public final class HostControllerLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if (element instanceof HostController) {
			HostController controller = (HostController) element;
			switch (columnIndex) {
			case 0:
				return controller.host();
			case 1:
				return controller.workingdirectory();
			case 2:
				return controller.executable();
			case 3:
				return controller.command();
			default:
				break;
			}
		}
		return null;
	}

}
