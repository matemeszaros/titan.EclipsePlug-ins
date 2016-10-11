/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.details;

import org.eclipse.core.runtime.IAdaptable;

/**
 * This is the TreeObject class
 *
 */
public class TreeObject implements IAdaptable {
	private String name;
	private TreeParent parent;

	/**
	 * Set TreeObject name
	 * @param name
	 */
	public TreeObject(final String name) {
		this.name = name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Get TreeObject name
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set Parent
	 * @param parent
	 */
	public void setParent(final TreeParent parent) {
		this.parent = parent;
	}

	/**
	 * Get Parent
	 * @return 
	 */
	public TreeParent getParent() {
		return this.parent;
	}

	@Override
	public String toString() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class key) {
		return null;
	}
}
