/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.text.table;

import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.utils.Constants;

class IconHandler {

	private static final String SET_VERDICT = "setverdict(";

	public Image getIcon(LogRecord logRecord, SetVerdictFilter setVerdictFilter) {
		final String message = logRecord.getMessage();
		if (!message.startsWith(SET_VERDICT)) {
			return null;
		}

		final int startPos = message.indexOf("(");
		final int stopPos = message.indexOf(")");
		final String type = message.substring(startPos + 1, stopPos);

		return filter(setVerdictFilter, type);
	}

	private Image filter(SetVerdictFilter setVerdictFilter, String type) {
		if (type.equals(Constants.TEST_CASE_VERDICT_ERROR)) {
			if (setVerdictFilter.isSetverdictError()) {
				return Activator.getDefault().getIcon(Constants.ICONS_ERROR);
			}
		} else if (type.equals(Constants.TEST_CASE_VERDICT_FAIL)) {
			if (setVerdictFilter.isSetverdictFail()) {
				return Activator.getDefault().getIcon(Constants.ICONS_FAIL);
			}
		} else if (type.equals(Constants.TEST_CASE_VERDICT_INCONCLUSIVE)) {
			if (setVerdictFilter.isSetverdictInconc()) {
				return Activator.getDefault().getIcon(Constants.ICONS_INCONCLUSIVE);
			}
		} else if (type.equals(Constants.TEST_CASE_VERDICT_NONE)) {
			if (setVerdictFilter.isSetverdictNone()) {
				return Activator.getDefault().getIcon(Constants.ICONS_NONE);
			}
		} else if (type.equals(Constants.TEST_CASE_VERDICT_PASS)
				&& setVerdictFilter.isSetverdictPass()) {
			return Activator.getDefault().getIcon(Constants.ICONS_PASS);
		}

		return null;
	}
}
