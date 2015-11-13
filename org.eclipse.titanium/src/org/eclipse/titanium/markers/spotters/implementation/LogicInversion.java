/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clause;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clauses;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class LogicInversion extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Negation is not necessary in this condition";

	public LogicInversion() {
		super(CodeSmellType.LOGIC_INVERSION);
	}

	@Override
	public void process(IVisitableNode node, Problems problems) {
		if (node instanceof If_Statement) {
			final If_Statement s = (If_Statement) node;
			if (s.getStatementBlock() == null) {
				return;
			}
			final If_Clauses ifClauses = s.getIfClauses();
			if (ifClauses == null) {
				return;
			}
			final List<If_Clause> clauses = ifClauses.getClauses();
			if (clauses.size() != 1) {
				return;
			}
			final Value expression = clauses.get(0).getExpression();
			if (expression != null && Value_type.EXPRESSION_VALUE.equals(expression.getValuetype())
					&& Operation_type.NOT_OPERATION.equals(((Expression_Value) expression).getOperationType())) {
				problems.report(s.getLocation(), ERROR_MESSAGE);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(If_Statement.class);
		return ret;
	}
}
