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

import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.DoWhile_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class InfiniteLoop {

	private InfiniteLoop() {
		throw new AssertionError("Noninstantiable");
	}

	public static class While extends BaseModuleCodeSmellSpotter {
		private static final String ERROR_MESSAGE = "Inifinite loop detected: the program can not escape from this while statement";

		public While() {
			super(CodeSmellType.INFINITE_LOOP);
		}

		@Override
		public void process(final IVisitableNode node, final Problems problems) {
			if (node instanceof While_Statement) {
				While_Statement s = (While_Statement) node;
				if (s.isTerminating(null)) {
					problems.report(s.getLocation(), ERROR_MESSAGE);
				}
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(While_Statement.class);
			return ret;
		}
	}

	public static class For extends BaseModuleCodeSmellSpotter {
		private static final String ERROR_MESSAGE = "Inifinite loop detected: the program can not escape from this for statement";

		public For() {
			super(CodeSmellType.INFINITE_LOOP);
		}

		@Override
		public void process(final IVisitableNode node, final Problems problems) {
			if (!(node instanceof For_Statement)) {
				return;
			}

			For_Statement s = (For_Statement) node;
			StatementBlock sb = s.getStatementBlock();
			if (sb == null) {
				return;
			}

			Value finalExpression = s.getFinalExpression();
			if (finalExpression == null) {
				return;
			}
			CompilationTimeStamp timestamp = CompilationTimeStamp.getBaseTimestamp();
			finalExpression.setLoweridToReference(timestamp);
			IValue lastValue = finalExpression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			Type_type temp = lastValue.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

			if (temp == Type_type.TYPE_BOOL && !lastValue.isUnfoldable(timestamp)
					&& ((Boolean_Value) lastValue).getValue()) {
						problems.report(s.getLocation(), ERROR_MESSAGE);
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(For_Statement.class);
			return ret;
		}
	}

	public static class DoWhile extends BaseModuleCodeSmellSpotter {
		private static final String ERROR_MESSAGE = "Inifinite loop detected: the program can not escape from this do-while statement";

		public DoWhile() {
			super(CodeSmellType.INFINITE_LOOP);
		}

		@Override
		public void process(final IVisitableNode node, final Problems problems) {
			if (node instanceof DoWhile_Statement) {
				DoWhile_Statement s = (DoWhile_Statement) node;
				if (s.isTerminating(null)) {
					problems.report(s.getLocation(), ERROR_MESSAGE);
				}
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(DoWhile_Statement.class);
			return ret;
		}
	}
}
