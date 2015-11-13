/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.single;

/**
 * Our internal Boolean implementation, where the internal value can be changed.
 * 
 * @author Kristof Szabados
 * */
public final class MyBoolean {
	private boolean value;

	public MyBoolean(final boolean value) {
		this.value = value;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(final boolean value) {
		this.value = value;
	}
}
