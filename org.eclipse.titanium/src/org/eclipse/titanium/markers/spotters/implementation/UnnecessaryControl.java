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
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuards;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.DoWhile_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clause;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clauses;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCases;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard.altguard_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock.ReturnStatus_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class UnnecessaryControl {
	private static final String ALWAYS_TRUE = "This control is unnecessary because the conditional expression evaluates to true";
	private static final String ALWAYS_FALSE = "This control is unnecessary because the conditional expression evaluates to false";
	private static final String UNREACHEBLE = "Control never reaches this code because the final condition evaluates to false";

	private UnnecessaryControl() {
		// disabled constructor
	}

	public abstract static class Base extends BaseModuleCodeSmellSpotter {
		protected final CompilationTimeStamp timestamp;

		public Base() {
			super(CodeSmellType.UNNECESSARY_CONTROLS);
			timestamp = CompilationTimeStamp.getBaseTimestamp();
		}
	}

	public static class For extends Base {
		private static final String ONE_SHOT = "This loop is executed at most once, because the body always returns";

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (!(node instanceof For_Statement)) {
				return;
			}

			For_Statement s = (For_Statement) node;
			Value finalExpression = s.getFinalExpression();
			if (finalExpression == null) {
				return;
			}

			IValue lastValue = finalExpression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			Type_type temp = lastValue.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			
			if (Type_type.TYPE_BOOL == temp && !lastValue.isUnfoldable(timestamp)) {
				if (((Boolean_Value) lastValue).getValue()) {
					problems.report(finalExpression.getLocation(), ALWAYS_TRUE);
				} else {
					problems.report(finalExpression.getLocation(), UNREACHEBLE);
				}
			}

			StatementBlock statementblock = s.getStatementBlock();
			if (statementblock != null && ReturnStatus_type.RS_YES.equals(statementblock.hasReturn(timestamp))) {
				problems.report(s.getLocation(), ONE_SHOT);
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
			if (!(node instanceof While_Statement)) {
				return;
			}

			While_Statement s = (While_Statement) node;
			Value expression = s.getExpression();
			if (expression == null) {
				return;
			}

			IValue last = expression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			Type_type tempType = last.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (!last.getIsErroneous(timestamp)) {
				if (!Type_type.TYPE_BOOL.equals(tempType)) {
					return;
				} else if (!expression.isUnfoldable(timestamp) && !((Boolean_Value) last).getValue()) {
					problems.report(expression.getLocation(), UNREACHEBLE);
				}

			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>();
			ret.add(While_Statement.class);
			return ret;
		}
	}

	public static class DoWhile extends Base {

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (!(node instanceof DoWhile_Statement)) {
				return;
			}

			DoWhile_Statement s = (DoWhile_Statement) node;
			Value expression = s.getExpression();
			IValue last = expression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			Type_type tempType = last.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (!last.getIsErroneous(timestamp)) {
				if (!Type_type.TYPE_BOOL.equals(tempType)) {
					return;
				} else if (!expression.isUnfoldable(timestamp) && !((Boolean_Value) last).getValue()) {
					problems.report(expression.getLocation(), ALWAYS_FALSE);
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

	public static class Select extends Base {
		private static final String UNREACHABLE = "Control never reaches this code because of previous effective cases(s)";

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (!(node instanceof SelectCase_Statement)) {
				return;
			}

			SelectCase_Statement s = (SelectCase_Statement) node;
			SelectCases cases = s.getSelectCases();
			if (cases == null) {
				return;
			}
			List<SelectCase> selectcases = cases.getSelectCaseArray();
			int size = selectcases.size();
			boolean unreachable = false;
			for (int i = 0; i < size; ++i) {
				SelectCase sc = selectcases.get(i);
				if (unreachable) {
					problems.report(sc.getLocation(), UNREACHABLE);
				}
				if (sc.hasElse()) {
					unreachable = true;
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

	public static class If extends Base {
		private static final String HANDLED = "Control never reaches this code because of previous effective condition(s)";
		private static final String UNREACHABLE = "Control never reaches this code because the conditional expression evaluates to false";

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (!(node instanceof If_Statement)) {
				return;
			}

			If_Statement s = (If_Statement) node;
			If_Clauses clauses = s.getIfClauses();
			if (clauses == null) {
				return;
			}
			List<If_Clause> ifclauses = clauses.getClauses();
			int size = ifclauses.size();
			boolean unreachable = false;
			for (int i = 0; i < size; ++i) {
				If_Clause ic = ifclauses.get(i);
				if (unreachable) {
					problems.report(ic.getLocation(), HANDLED);
				}
				Value expression = ic.getExpression();
				if (expression != null) {
					IValue last = expression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
					Type_type tempType = last.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
					if (!last.getIsErroneous(timestamp) && !Type_type.TYPE_UNDEFINED.equals(tempType)) {
						if (!Type_type.TYPE_BOOL.equals(tempType)) {
							continue;
						} else if (!expression.isUnfoldable(timestamp)) {
							if (((Boolean_Value) last).getValue()) {
								problems.report(expression.getLocation(), ALWAYS_TRUE);
								unreachable = true;
							} else {
								problems.report(expression.getLocation(), ALWAYS_FALSE);
								problems.report(ic.getStatementBlock().getLocation(), UNREACHABLE);
							}
						}
					}
				}
			}
			if (s.getStatementBlock() != null && unreachable) {
				problems.report(s.getStatementBlock().getLocation(), HANDLED);
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(If_Statement.class);
			return ret;
		}
	}

	public static class Alt extends Base {
		private static final String AFTER_ELSE = "Control never reaches this branch of alternative because of a previous [else] branch";

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (!(node instanceof Alt_Statement)) {
				return;
			}

			Alt_Statement s = (Alt_Statement) node;
			AltGuards altGuards = s.getAltGuards();
			if (altGuards == null) {
				return;
			}
			int size = altGuards.getNofAltguards();
			boolean unreachable = false;
			for (int i = 0; i < size; ++i) {
				AltGuard guard = altGuards.getAltguardByIndex(i);
				if (unreachable) {
					problems.report(guard.getLocation(), AFTER_ELSE);
				}
				if (altguard_type.AG_ELSE.equals(guard.getType())) {
					unreachable = true;
				}
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(Alt_Statement.class);
			return ret;
		}
	}
}
