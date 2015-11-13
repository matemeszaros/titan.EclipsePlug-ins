/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import org.eclipse.titan.common.parsers.IntervalDetector;
import org.eclipse.titan.common.parsers.Interval.interval_type;
import org.eclipse.titan.common.parsers.cfg.CfgInterval.section_type;

/**
 * @author eferkov
 * */
public final class CfgIntervalDetector extends IntervalDetector {
	@Override
	public void initRootInterval(final int length){
		actualInterval = new CfgInterval(null, interval_type.NORMAL, section_type.UNKNOWN);
		rootInterval = actualInterval;
		actualInterval.setStartOffset(0);
		actualInterval.setStartLine(0);
		actualInterval.setEndOffset(length);
		setMaxLength(length);
	}
	
	public void pushInterval(final int offset, final int line, final interval_type type, final section_type sectionType) {
		CfgInterval tempInterval = new CfgInterval(actualInterval, type, sectionType);
		actualInterval =  tempInterval;
		if (rootInterval == null) {
			rootInterval =  tempInterval;
		}
		tempInterval.setStartOffset(offset);
		tempInterval.setStartLine(line);
	}

	@Override
	public final void handleFinalCorrection() {
		if (rootInterval == null) {
			return;
		}

		while (actualInterval != null && !actualInterval.equals(rootInterval)) {
			if (!(actualInterval instanceof CfgInterval && !section_type.UNKNOWN.equals(((CfgInterval) actualInterval).getSectionType()))) {
				actualInterval.setErroneous();
			}

			// TODO the ending line should be set correctly.
			popInterval(getMaxLength() - 1, -1);
		}
	}
}
