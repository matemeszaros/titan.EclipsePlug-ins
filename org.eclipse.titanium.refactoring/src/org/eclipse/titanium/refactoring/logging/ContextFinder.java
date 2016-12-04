/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.statements.Log_Statement;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;
import org.eclipse.titanium.refactoring.logging.context.Context;
import org.eclipse.titanium.refactoring.logging.context.ContextFactory;

/**
 * Visits all the subnodes of an {@link IVisitableNode}.
 * Collects all the {@link Log_Statement}s and creates a {@link Context} chain for each.
 * The context chain is a list of {@link Context}s, each created from a single {@link IVisitableNode}
 *  in the ancestor chain (in the AST) of the {@link Log_Statement}.
 * <p>
 * After the visitor ran, the {@link ContextFinder#result} contains the root {@link Context} for each
 *  {@link Log_Statement}. The root {@link Context} is mostly the {@link Context} object derived from
 *  a {@link Module} node. Its child context is derived from the {@link IVisitableNode} child of the
 *  module in which the current {@link Log_Statement} is located. The end of the context chain (the last
 *  descendant) is the context derived from the current {@link Log_Statement}.
 *
 * @author Viktor Varga
 */
class ContextFinder extends ASTVisitor {

	private final Settings settings;

	private final ContextFactory factory;
	private final Deque<IVisitableNode> ancestorStack;

	private final Map<Log_Statement, Context> result;

	ContextFinder(final Settings settings) {
		this.settings = settings;
		factory = new ContextFactory();
		ancestorStack = new ArrayDeque<IVisitableNode>();
		result = new HashMap<Log_Statement, Context>();
	}

	public Map<Log_Statement, Context> getResult() {
		return result;
	}

	/** Iteration order when creating contexts is: from bottom (log statement) to root (module). */
	protected Context createContextChain() {
		final Iterator<IVisitableNode> it = ancestorStack.iterator();
		Context prev = null;
		Context curr = null;
		if (it.hasNext()) {
			prev = factory.createContext(it.next(), null, settings);
		}
		while (it.hasNext()) {
			curr = factory.createContext(it.next(), prev, settings);
			curr.setChild(prev);
			prev.setParent(curr);
			prev = curr;
		}

		return curr;
	}

	@Override
	public int visit(final IVisitableNode node) {
		ancestorStack.addFirst(node);
		if (node instanceof Log_Statement) {
			if (settings.getSetting(Settings.SETTING_MODIFY_LOG_STATEMENTS)) {
				result.put((Log_Statement)node, createContextChain());
			} else {
				final LogStatementVisitor vis = new LogStatementVisitor();
				node.accept(vis);
				if (!vis.getResult()) {
					result.put((Log_Statement)node, createContextChain());
				}
			}
		}
		return V_CONTINUE;
	}
	@Override
	public int leave(final IVisitableNode node) {
		if (ancestorStack.getFirst() == node) {
			ancestorStack.removeFirst();
		}
		return V_CONTINUE;
	}

	/**
	 * Searches a log statement for arguments that are variables (and not text literals).
	 * <p>
	 * Call on a {@link Log_Statement}}
	 * */
	private static class LogStatementVisitor extends ASTVisitor {

		private boolean result = false;

		private boolean getResult() {
			return result;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Reference) {
				result = true;
				return V_ABORT;
			}
			return V_CONTINUE;
		}

	}

}
