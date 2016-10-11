/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

/**
 * Helper class, used by the matching brackets functionality.
 * 
 * @author Kristof Szabados
 */
public final class Pair {
	public final char start;
	public final char end;

	public Pair(final char start, final char end) {
		this.start = start;
		this.end = end;
	}
}
