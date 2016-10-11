/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors;

/**
 * @author Kristof Szabados
 * */
public class TreeLeaf implements ITreeLeaf {
	private String name;

	protected ITreeBranch parent;

	public TreeLeaf(final String name) {
		this.name = name;
	}

	@Override
	public void dispose() {
		name = null;
		if (null != parent) {
			parent = null;
		}
	}

	@Override
	public final String name() {
		return name;
	}

	public final void name(final String name) {
		this.name = name;
	}

	@Override
	public final ITreeBranch parent() {
		return parent;
	}

	@Override
	public final void parent(final ITreeBranch element) {
		parent = element;
	}
}
