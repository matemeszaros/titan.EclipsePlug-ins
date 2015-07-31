/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.lang.ref.WeakReference;

/**
 * A node to work as a bridge between two naming nodes.
 * This is useful when we don't wish to store the child locally (and so it can not be compared)
 * 
 * @author Kristof Szabados
 * */
public final class BridgingNamedNode implements INamedNode {
	private final String text;

	/** the naming parent of the node. */
	private WeakReference<INamedNode> nameParent;

	public BridgingNamedNode(final String text) {
		this.text = text;
	}

	public BridgingNamedNode(final INamedNode nameParent, final String text) {
		this.nameParent = new WeakReference<INamedNode>(nameParent);
		this.text = text;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder;

		if (null != nameParent) {
			final INamedNode tempParent = nameParent.get();
			if (null != tempParent) {
				builder = tempParent.getFullName(this);
			} else {
				builder = new StringBuilder();
			}
		} else {
			builder = new StringBuilder();
		}

		return builder.append(text);
	}

	@Override
	public String getFullName() {
		return getFullName(null).toString();
	}

	@Override
	public void setFullNameParent(final INamedNode nameParent) {
		this.nameParent = new WeakReference<INamedNode>(nameParent);
	}

	@Override
	public INamedNode getNameParent() {
		return nameParent.get();
	}
}
