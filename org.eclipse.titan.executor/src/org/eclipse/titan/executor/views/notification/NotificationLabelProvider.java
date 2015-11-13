/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.notification;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kristof Szabados
 * */
public final class NotificationLabelProvider extends LabelProvider implements ITableLabelProvider {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if (element instanceof Notification) {
			Notification nElement = (Notification) element;
			switch (columnIndex) {
			case 0:
				return nElement.getTimestamp();
			case 1:
				return nElement.getType();
			case 2:
				return nElement.getComponent();
			case 3:
				return nElement.getInformation();
			default:
				break;
			}
		}
		return null;
	}

}
