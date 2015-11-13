/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.preferences;

/**
 * The method for classifying risk levels
 * 
 * @author poroszd
 * 
 */
public enum RiskMethod {
	/** This risk is 'weak' to say to some code that 'this is wrong' */
	NEVER("Never warn"),
	/** Above a threshold the metric will sign LOW risk level */
	NO_LOW("Low risk"),
	/** Above a threshold the metric will sign HIGH risk level */
	NO_HIGH("High risk"),
	/** Metric can sign both LOW and HIGH risk levels. */
	NO_LOW_HIGH("Tri-state");

	// a short, human-understandable description
	private String text;

	RiskMethod(final String text) {
		this.text = text;
	}

	/**
	 * return the text of the risk method.
	 * */
	public String getText() {
		return text;
	}
	/*
	 * Convert from the ordinal to enum. While this is somehow ugly, we need it
	 * e.g. when reading saved values back from the preference store.
	 */
	public static RiskMethod myMethod(final int i) {
		switch (i) {
		case 0:
			return NEVER;
		case 1:
			return NO_LOW;
		case 2:
			return NO_HIGH;
		case 3:
			return NO_LOW_HIGH;
		default:
			return NEVER;
		}
	}
}