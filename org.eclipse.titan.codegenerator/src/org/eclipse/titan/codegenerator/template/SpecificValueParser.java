/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.template;

import org.eclipse.titan.codegenerator.Scope;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Value;

public class SpecificValueParser extends TemplateValueParser {
	private final String type;

	public SpecificValueParser(Scope parent, ValueHolder holder, String type) {
		super(parent, holder);
		this.type = type;
	}

	@Override
	public Scope process(IVisitableNode node) {
		if (node instanceof Value) {
			value = Util.extract(type, node);
		}
		return this;
	}
}
