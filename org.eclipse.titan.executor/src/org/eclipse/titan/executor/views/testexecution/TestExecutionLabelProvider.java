/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.testexecution;

import static org.eclipse.titan.executor.GeneralConstants.ERROR;
import static org.eclipse.titan.executor.GeneralConstants.FAIL;
import static org.eclipse.titan.executor.GeneralConstants.INCONC;
import static org.eclipse.titan.executor.GeneralConstants.NONE;
import static org.eclipse.titan.executor.GeneralConstants.PASS;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.executor.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public final class TestExecutionLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		if (element instanceof ExecutedTestcase
				&& 2 == columnIndex) {
			final String verdict = ((ExecutedTestcase) element).getVerdict();
			if (PASS.equals(verdict)) {
				return ImageCache.getImage("pass.gif");
			} else if (INCONC.equals(verdict)) {
				return ImageCache.getImage("inconc.gif");
			} else if (FAIL.equals(verdict)) {
				return ImageCache.getImage("fail.gif");
			} else if (ERROR.equals(verdict)) {
				return ImageCache.getImage("error.gif");
			} else if (NONE.equals(verdict)) {
				return ImageCache.getImage("none.gif");
			}
		}
		return super.getImage(element);
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if (element instanceof ExecutedTestcase) {
			final ExecutedTestcase executed = (ExecutedTestcase) element;
			switch (columnIndex) {
			case 0:
				return executed.getTimestamp();
			case 1:
				return executed.getTestCaseName();
			case 2:
				return executed.getVerdict();
			case 3:
				return executed.getReason();
			default:
				break;
			}
		}
		return null;
	}

}
