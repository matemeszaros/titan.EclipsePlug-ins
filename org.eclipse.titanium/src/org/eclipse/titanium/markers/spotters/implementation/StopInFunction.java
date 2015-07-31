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
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.Stop_Execution_Statement;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class StopInFunction extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "The stop execution statement should not be used in functions";

	public StopInFunction() {
		super(CodeSmellType.STOP_IN_FUNCTION);
	}

	@Override
	public void process(IVisitableNode node, Problems problems) {
		if (node instanceof Stop_Execution_Statement) {
			Stop_Execution_Statement s = (Stop_Execution_Statement) node;
			StatementBlock sb = s.getMyStatementBlock();
			Definition d = sb.getMyDefinition();
			if (d instanceof Def_Function) {
				problems.report(s.getLocation(), ERROR_MESSAGE);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Stop_Execution_Statement.class);
		return ret;
	}
}
