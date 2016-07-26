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
 * @author Arpad Lovassy
 */
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
	
	/**
	 * Creates and pushes a new interval onto the stack of intervals. This new interval becomes the actual one.
	 * <p>
	 * The ending offset of this interval is not yet set. @see #popInterval(int)
	 *
	 * @param offset the offset at which the new interval should start
	 * @param line the line at which the new interval should start
	 * @param type the type of the interval
	 * @param sectionType the section type of the interval. This is used only for section intervals,
	 *                    in this case type == interval_type.NORMAL && sectionType != section_type.UNKNOWN 
	 */
	public void pushInterval(final int offset, final int line, final interval_type type, final section_type sectionType) {
		final CfgInterval tempInterval = new CfgInterval(actualInterval, type, sectionType);
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
