/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clause;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clauses;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsBoundExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsChoosenExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsPresentExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.NotExpression;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * An isBound/isPresent/isChosen statement is inside an if condition
 * and the else branch of the if statement is not present.
 * If the isBound/... statement is inside an odd number of Not_Expressions,
 * the statement will not be marked as a smell.
 * 
 * @author Viktor Varga
 */
public class IsBoundWithoutElse extends BaseModuleCodeSmellSpotter {
	
	private static final String ERR_MSG = "Only the positive branch of `{0}'' check is used." +
			" In tests it is advised to log the reason of failure in the else branch.";
	
	private static final Map<Class<?>, String> NAMES;
	
	static {
		NAMES = new HashMap<Class<?>, String>();
		NAMES.put(IsBoundExpression.class, "isBound");
		NAMES.put(IsPresentExpression.class, "isPresent");
		NAMES.put(IsChoosenExpression.class, "isChosen");
	}
	
	protected IsBoundWithoutElse() {
		super(CodeSmellType.ISBOUND_WITHOUT_ELSE);
	}
		
	@Override
	protected void process(final IVisitableNode node, final Problems problems) {
		if (!(node instanceof If_Statement)) {
			return;
		}
		final If_Statement ifs = (If_Statement)node;
		final StatementBlock elseClause = ifs.getStatementBlock();
		if (elseClause != null) {
			return;
		}
		//there is no else clause present
		final If_Clauses ifcs = ifs.getIfClauses();
		if (ifcs == null) {
			return;
		}
		final List<If_Clause> ifcL = ifcs.getClauses();
		if (ifcL == null || ifcL.isEmpty()) {
			return;
		}
		for (final If_Clause ifc: ifcL) {
			final IfConditionVisitor visitor = new IfConditionVisitor(problems);
			ifc.accept(visitor);
		}
	}
	
	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(3);
		ret.add(If_Statement.class);
		return ret;
	}
	
	
	//call on IfClause (if condition); recursively calls itself on NotExpressions
	private static class IfConditionVisitor extends ASTVisitor {
		
		private final int negationsNumber;
		private final Problems problems;
		private boolean insideNotExpr = false;	//true if last visited node was a NotExpression
		
		public IfConditionVisitor(final Problems problems) {
			this(0, problems);
		}
		private IfConditionVisitor(final int negationsNumber, final Problems problems) {
			this.negationsNumber = negationsNumber;
			this.problems = problems;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			//do not enter another if statements: it would cause duplicate visits
			if (node instanceof If_Statement) {
				return V_SKIP;
			}
			//true if the visitor is at the expression node of a NotExpression
			if (insideNotExpr) {
				final IfConditionVisitor visitor = new IfConditionVisitor(negationsNumber+1, problems);
				node.accept(visitor);
				insideNotExpr = false;
				return V_SKIP;
			}
			if (node instanceof NotExpression) {
				insideNotExpr = true;
			} else if (node instanceof IsBoundExpression || 
					node instanceof IsPresentExpression ||
					node instanceof IsChoosenExpression) {
				final Expression_Value ev = (Expression_Value)node;
				if (negationsNumber%2 == 0) {
					problems.report(ev.getLocation(), MessageFormat.format(ERR_MSG, NAMES.get(ev.getClass())));
				}
				return V_SKIP;
			}
			return V_CONTINUE;
		}
		
		
	}

}
