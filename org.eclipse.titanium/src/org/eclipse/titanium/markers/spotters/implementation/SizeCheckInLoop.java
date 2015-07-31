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

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.DoWhile_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.LengthofExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.SizeOfExpression;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class SizeCheckInLoop extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Length check operation in loop condition";
	
	public SizeCheckInLoop() {
		super(CodeSmellType.SIZECHECK_IN_LOOP);
	}
	
	@Override
	protected void process(IVisitableNode node, Problems problems) {
		if (node instanceof For_Statement) {
			For_Statement s = (For_Statement)node;
			s.getFinalExpression().accept(new LoopVisitor(problems));
		} else if (node instanceof While_Statement) {
			While_Statement s = (While_Statement)node;
			s.getExpression().accept(new LoopVisitor(problems));
		} else if (node instanceof DoWhile_Statement) {
			DoWhile_Statement s = (DoWhile_Statement)node;
			s.getExpression().accept(new LoopVisitor(problems));
		} else {
			return;
		}
	}
	
	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(3);
		ret.add(For_Statement.class);
		ret.add(While_Statement.class);
		ret.add(DoWhile_Statement.class);
		return ret;
	}
	
	
	protected static class LoopVisitor extends ASTVisitor {
		private final Problems problems;

		public LoopVisitor(Problems problems) {
			this.problems = problems;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof LengthofExpression) {
				final LengthofExpression temp = (LengthofExpression) node;
				problems.report(temp.getLocation(), ERROR_MESSAGE);
			} else if (node instanceof SizeOfExpression) {
				final SizeOfExpression temp = (SizeOfExpression) node;
				problems.report(temp.getLocation(), ERROR_MESSAGE);
			} else if (node instanceof Expression_Value) {
				return V_CONTINUE;
			}
			return V_SKIP;
		}
	}
	
}
