/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.details;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the TreeParent class 
 *  
 */
public class TreeParent extends TreeObject {
	private final List<TreeObject> children;

	/**
	 * @param name
	 */
	public TreeParent(final String name) {
		super(name);
		this.children = new ArrayList<TreeObject>();
	}

	/**
	 * Add TreeObject child
	 * @param child
	 */
	public void addChild(final TreeObject child) {
		this.children.add(child);
		child.setParent(this);
	}

	/**
	 * Remove TreeObject child 
	 * @param child
	 */
	public void removeChild(final TreeObject child) {
		this.children.remove(child);
		child.setParent(null);
	}

	/**
	 * Get the children
	 * @return
	 */
	public TreeObject [] getChildren() {
		return this.children.toArray(new TreeObject[this.children.size()]);
	}

	/**
	 * Has the parent any children
	 * @return
	 */
	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	public TreeObject getChild(final String name) {
		for (TreeObject child : children) {
			if (child.getName().trim().equals(name)) {
				return child;
			}
		}
		return null;
	}

	public String asString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getName()).append("\n").append("[");
		for (TreeObject obj : children) {
			builder.append(obj.toString()).append("\n");
		}
		builder.append("]\n");

		return builder.toString();
	}
}
