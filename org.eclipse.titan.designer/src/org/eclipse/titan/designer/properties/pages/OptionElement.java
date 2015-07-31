/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kristof Szabados
 * */
public class OptionElement {

	public String name = null;
	public IOptionsPage page = null;

	public OptionElement parent = null;
	public List<OptionElement> children = null;

	public OptionElement(final String name) {
		this.name = name;
	}

	public OptionElement(final String name, final IOptionsPage page) {
		this.name = name;
		this.page = page;
	}

	public void addChild(final OptionElement child) {
		if (children == null) {
			children = new ArrayList<OptionElement>();
		}

		children.add(child);
		child.parent = this;
	}
}
