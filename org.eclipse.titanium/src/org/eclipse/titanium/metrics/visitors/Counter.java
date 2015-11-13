/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.visitors;

/**
 * This class implements a mutable integer type.
 * 
 * @author poroszd
 * 
 */
public class Counter {
	private int val;

	public Counter(final int init) {
		val = init;
	}

	public void set(final int val) {
		this.val = val;
	}

	public void inc() {
		++val;
	}

	public void increase(final int inc) {
		val += inc;
	}

	public void dec() {
		--val;
	}

	public void decrease(final int dec) {
		val -= dec;
	}

	public int val() {
		return val;
	}
}
