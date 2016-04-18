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
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class IfWithoutElse extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Conditional operation without else clause";

	public IfWithoutElse() {
		super(CodeSmellType.IF_WITHOUT_ELSE);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof If_Statement) {
			If_Statement s = (If_Statement) node;
			if (s.getStatementBlock() == null && s.getIfClauses() != null && s.getIfClauses().isExactlyOne()) {
				StatementBlock parentBlock = s.getMyStatementBlock();
				if (parentBlock != null && parentBlock.getSize() == 1) {
					problems.report(s.getLocation(), ERROR_MESSAGE);
				}
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
