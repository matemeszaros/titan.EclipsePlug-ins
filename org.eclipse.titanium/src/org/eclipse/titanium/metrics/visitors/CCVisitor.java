/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.visitors;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.statements.DoWhile_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clause;
import org.eclipse.titan.designer.AST.TTCN3.statements.Operation_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstances;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AndExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.NotExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.OrExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.XorExpression;

/**
 * Helper visitor class, used by the metrics.
 * <p>
 * Calculates the cyclomatic complexity of the code, starting at a node (given
 * in the constructor).
 * 
 * @author poroszd
 * 
 */
public class CCVisitor extends CounterVisitor {
	private static class ExpressionComplexityVisitor extends CounterVisitor {
		public ExpressionComplexityVisitor(final Counter n) {
			super(n);
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Expression_Value) {
				if (node instanceof AndExpression) {
					count.inc();
					return V_CONTINUE;
				} else if (node instanceof OrExpression) {
					count.inc();
					return V_CONTINUE;
				} else if (node instanceof XorExpression) {
					count.inc();
					return V_CONTINUE;
				} else if (node instanceof NotExpression) {
					return V_CONTINUE;
				} else {
					return V_SKIP;
				}
			} else {
				return V_SKIP;
			}
		}
	}

	private static class OpAltGuardExpVisitor extends CounterVisitor {
		public OpAltGuardExpVisitor(final Counter n) {
			super(n);
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Operation_Altguard) {
				return V_CONTINUE;
			} else if (node instanceof Value) {
				count.inc();
				node.accept(new ExpressionComplexityVisitor(count));
			}
			return V_SKIP;
		}
	}

	private static class SelectCaseVisitor extends CounterVisitor {
		public SelectCaseVisitor(final Counter n) {
			super(n);
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof SelectCase) {
				return V_CONTINUE;
			} else if (node instanceof TemplateInstances) {
				count.inc();
			}
			return V_SKIP;
		}
	}

	public CCVisitor(final Counter n) {
		super(n);
		count.inc();
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof Operation_Altguard) {
			count.inc();
			node.accept(new OpAltGuardExpVisitor(count));
		} else if (node instanceof DoWhile_Statement) {
			count.inc();
			node.accept(new ExpressionComplexityVisitor(count));
		} else if (node instanceof For_Statement) {
			count.inc();
			node.accept(new ExpressionComplexityVisitor(count));
		} else if (node instanceof If_Clause) {
			count.inc();
			node.accept(new ExpressionComplexityVisitor(count));
		} else if (node instanceof SelectCase) {
			node.accept(new SelectCaseVisitor(count));
		} else if (node instanceof While_Statement) {
			count.inc();
			node.accept(new ExpressionComplexityVisitor(count));
		}
		return V_CONTINUE;
	}
}
