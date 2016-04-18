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
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class SwitchOnBoolean extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "For checking boolean values 'if' conditions are more efficient";

	public SwitchOnBoolean() {
		super(CodeSmellType.SWITCH_ON_BOOLEAN);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof SelectCase_Statement) {
			SelectCase_Statement s = (SelectCase_Statement) node;
			Value expression = s.getExpression();
			CompilationTimeStamp ct = CompilationTimeStamp.getBaseTimestamp();

			if (expression != null
					&& Type_type.TYPE_BOOL.equals(expression.getExpressionReturntype(ct,
							Expected_Value_type.EXPECTED_DYNAMIC_VALUE))) {
				problems.report(expression.getLocation(), ERROR_MESSAGE);
			}
		}

	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(SelectCase_Statement.class);
		return ret;
	}
}
