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

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clause;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Operation_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.MatchExpression;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class IfInsteadReceiveTemplate extends BaseModuleCodeSmellSpotter {
	private static final String SHOULD_BRANCH = "The 'match' expression should be turned to a receive template";

	public IfInsteadReceiveTemplate() {
		super(CodeSmellType.IF_INSTEAD_RECEIVE_TEMPLATE);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof Operation_Altguard) {
			final Operation_Altguard ag = (Operation_Altguard) node;
			final Statement action = ag.getGuardStatement();
			if (action instanceof Receive_Port_Statement) {
				final Receive_Port_Statement receive = (Receive_Port_Statement) action;
				final SuspiciouslyUsedIf susp = new SuspiciouslyUsedIf(receive);
				ag.accept(susp);
				if (susp.doesSmell()) {
					problems.report(susp.getSuspicious().getLocation(), SHOULD_BRANCH);
				}
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(AltGuard.class);
		return ret;
	}
}

/**
 * This visitor helps to find a branch of an if statement, where a matching
 * operation is performed on the reference, that is obtained by value
 * redirection. This effect should be achieved by template matching in the
 * receive statement.
 * 
 * @author poroszd
 * 
 */
final class SuspiciouslyUsedIf extends ASTVisitor {
	private Reference redirectValue;
	private boolean smells;
	private If_Clause suspicious;

	public SuspiciouslyUsedIf(final Receive_Port_Statement rec) {
		redirectValue = rec.getRedirectValue();
		smells = false;
		suspicious = null;
	}

	public boolean doesSmell() {
		return smells;
	}

	public If_Clause getSuspicious() {
		return suspicious;
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof If_Statement) {
			final List<If_Clause> ifs = ((If_Statement) node).getIfClauses().getClauses();
			for (final If_Clause clause : ifs) {
				final Value cond = clause.getExpression();
				if (cond != null) {
					final RefUsedInMatching mv = new RefUsedInMatching(redirectValue);
					cond.accept(mv);
					if (mv.getUsed()) {
						smells = true;
						suspicious = clause;
					}
				}
			}
			return V_SKIP;
		}
		return V_CONTINUE;
	}
}

/**
 * This class helps to tell whether the condition of the if clause uses a given
 * reference in a matching expression.
 * 
 * @author poroszd
 * 
 */
final class RefUsedInMatching extends ASTVisitor {
	private Reference ref;
	private boolean used;

	public RefUsedInMatching(final Reference ref) {
		this.ref = ref;
		used = false;
	}

	/**
	 * return whether the reference is used in matching.
	 * */
	public boolean getUsed() {
		return used;
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof MatchExpression) {
			final ContainsRef cv = new ContainsRef(ref);
			node.accept(cv);
			used = used || cv.contains;
			return V_SKIP;
		}
		return V_CONTINUE;
	}
}

final class ContainsRef extends ASTVisitor {
	private Identifier id;
	public boolean contains;

	public ContainsRef(final Reference ref) {
		id = ref.getId();
		contains = false;
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof Reference) {
			if (id.equals(((Reference) node).getId())) {
				contains = true;
			}
			return V_SKIP;
		}
		return V_CONTINUE;
	}
}
