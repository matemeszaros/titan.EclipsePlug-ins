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
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.Type;

public class FormalParameterParser implements Scope {
	private final Scope parent;
	private final Parameterizable parameterizable;
	private String type;
	private String name;

	public FormalParameterParser(Scope parent, Parameterizable p) {
		this.parent = parent;
		parameterizable = p;
	}

	@Override
	public Scope process(IVisitableNode node) {
		if (node instanceof FormalParameter) {
			FormalParameter parameter = (FormalParameter) node;
			name = parameter.getIdentifier().toString();
		}
		if (node instanceof Type) {
			type = Util.getTypeName((Type) node);
		}
		return this;
	}

	@Override
	public Scope finish(IVisitableNode node) {
		if (node instanceof FormalParameter) {
			parameterizable.addParameter(type, name);
		}
		if (node instanceof FormalParameterList) {
			return parent;
		}
		return this;
	}
}
