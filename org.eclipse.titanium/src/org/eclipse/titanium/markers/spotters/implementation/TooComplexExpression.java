/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.DoWhile_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clause;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.preferences.PreferenceConstants;

public class TooComplexExpression {

	private TooComplexExpression() {
		throw new AssertionError("Noninstantiable");
	}

	private abstract static class Base extends BaseModuleCodeSmellSpotter {
		private static final String COMPLEXITY = "The complexity of this expression `{0}'' is bigger than allowed `{1}''";
		private final int reportTooComplexExpressionSize;

		public Base() {
			super(CodeSmellType.TOO_COMPLEX_EXPRESSIONS);
			reportTooComplexExpressionSize = Platform.getPreferencesService().getInt(Activator.PLUGIN_ID,
					PreferenceConstants.TOO_COMPLEX_EXPRESSIONS_SIZE, 7, null);
		}

		protected static class ExpressionVisitor extends ASTVisitor {
			private int count = 0;

			public int getCount() {
				return count;
			}

			@Override
			public int visit(IVisitableNode node) {
				if (node instanceof Expression_Value) {
					count++;
				}

				return V_CONTINUE;
			}
		}

		protected void check(IValue expression, Problems problems) {
			if (expression instanceof Expression_Value) {
				ExpressionVisitor visitor = new ExpressionVisitor();
				expression.accept(visitor);
				if (visitor.getCount() > reportTooComplexExpressionSize) {
					String msg = MessageFormat.format(COMPLEXITY, visitor.getCount(), reportTooComplexExpressionSize);
					problems.report(expression.getLocation(), msg);
				}
			}
		}

	}

	public static class For extends Base {

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (node instanceof For_Statement) {
				For_Statement s = (For_Statement) node;
				Value expression = s.getFinalExpression();
				check(expression, problems);
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(For_Statement.class);
			return ret;
		}
	}

	public static class While extends Base {

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (node instanceof While_Statement) {
				While_Statement s = (While_Statement) node;
				Value expression = s.getExpression();
				check(expression, problems);
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(While_Statement.class);
			return ret;
		}
	}

	public static class DoWhile extends Base {

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (node instanceof DoWhile_Statement) {
				DoWhile_Statement s = (DoWhile_Statement) node;
				Value expression = s.getExpression();
				check(expression, problems);
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(DoWhile_Statement.class);
			return ret;
		}
	}

	public static class If extends Base {

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (node instanceof If_Clause) {
				If_Clause s = (If_Clause) node;
				Value expression = s.getExpression();
				check(expression, problems);
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(If_Clause.class);
			return ret;
		}
	}

	public static class Assignments extends Base {

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (node instanceof Assignment_Statement) {
				Assignment_Statement s = (Assignment_Statement) node;
				CompilationTimeStamp ct = CompilationTimeStamp.getBaseTimestamp();
				Assignment assignment = s.getReference().getRefdAssignment(ct, false);
				TTCN3Template template = s.getTemplate();
				if (assignment == null || template == null) {
					// semantic error in the code.
					return;
				}
				switch (assignment.getAssignmentType()) {
				case A_PAR_VAL_IN:
				case A_PAR_VAL_OUT:
				case A_PAR_VAL_INOUT:
				case A_PAR_VAL:
				case A_VAR:
					if (template.isValue(ct)) {
						IValue tempValue = template.getValue();
						check(tempValue, problems);
					}
					break;
				default:
					break;
				}
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(Assignment_Statement.class);
			return ret;
		}
	}
}
