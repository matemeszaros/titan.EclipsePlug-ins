/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.Goto_statement;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class Goto extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Usage of goto and label statements is not recommended "
			+ "as they usually break the structure of the code";

	public Goto() {
		super(CodeSmellType.GOTO);
	}

	@Override
	public void process(IVisitableNode node, Problems problems) {
		if (node instanceof Goto_statement) {
			Goto_statement s = (Goto_statement) node;
			problems.report(s.getLocation(), ERROR_MESSAGE);
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Goto_statement.class);
		return ret;
	}
}
