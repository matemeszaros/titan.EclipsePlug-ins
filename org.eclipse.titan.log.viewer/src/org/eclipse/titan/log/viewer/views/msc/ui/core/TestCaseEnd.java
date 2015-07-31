/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.core;

import org.eclipse.swt.graphics.Color;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Represents a test case start
 *
 */
public class TestCaseEnd extends TestCaseEventNode {
	private static final String PREFIX = Messages.getString("TestCaseEnd.0"); //$NON-NLS-1$
	private String verdict;

	/**
	 * Constructor
	 *
	 * @param eventOccurrence the occurrence of this event
	 * @param verdict the verdict
	 * @param lifelines the number of lifelines
	 */
	public TestCaseEnd(final int eventOccurrence, final String verdict, final int lifelines) {
		super(eventOccurrence, lifelines);
		this.verdict = verdict;
	}

	@Override
	public int getHeight() {
		return MSCConstants.ROW_HEIGHT - MSCConstants.ROW_SPACING + MSCConstants.TESTCASEEND_SHADOW_SIZE;
	}

	@Override
	protected Color getBackgroundColor() {
		return (Color) Activator.getDefault().getCachedResource(MSCConstants.getVerdictColor(this.verdict));
	}

	@Override
	protected String getNodeText() {
		return PREFIX + getName();
	}

	@Override
	public Type getType() {
		return Type.TESTCASE_END;
	}
}
