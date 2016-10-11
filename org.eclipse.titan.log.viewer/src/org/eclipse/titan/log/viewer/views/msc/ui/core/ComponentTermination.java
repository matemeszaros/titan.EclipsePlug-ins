/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.core;

import org.eclipse.swt.graphics.Color;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Representation of a component termination in the sequence diagram
 */
public class ComponentTermination extends ComponentEventNode {

	private String verdict;

	/**
	 * Constructor
	 *
	 * @param eventOccurrence the occurrence of the component termination
	 * @param verdict         the final verdict of the component
	 */
	public ComponentTermination(final int eventOccurrence, final Lifeline lifeline, final String verdict) {
		super(eventOccurrence, lifeline);
		this.verdict = verdict;
	}

	@Override
	protected Color getBackgroundColor() {
		return (Color) Activator.getDefault().getCachedResource(MSCConstants.getVerdictColor(this.verdict));
	}

	@Override
	public Type getType() {
		return Type.COMPONENT_TERMINATION;
	}

}
