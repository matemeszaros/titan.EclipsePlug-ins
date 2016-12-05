/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.Log_Statement;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;

/**
 * The abstract parent class for all the context classes.
 * <p>
 * A context object represents a single {@link IVisitableNode} object. A context object
 *  can be linked into a chain of contexts, and the previous ({@link Context#parent}) and
 *  next ({@link Context#child}) node in the chain can be accessed from the context.
 * Besides, the context classes can create new arguments for existing {@link Log_Statement}s so
 *  properties of the node represented by this context can be added to those log arguments.
 *
 * @author Viktor Varga
 */
public abstract class Context {

	protected final Settings settings;

	private final IVisitableNode node;

	private Context parent;
	private Context child;

	public Context(final IVisitableNode node, final Settings settings) {
		this.node = node;
		this.settings = settings;
	}

	public void setParent(final Context parent) {
		this.parent = parent;
	}
	public void setChild(final Context child) {
		this.child = child;
	}
	public Context getParent() {
		return parent;
	}
	public Context getChild() {
		return child;
	}
	public Context getBottom() {
		if (getChild() == null) {
			return this;
		} else {
			return getChild().getBottom();
		}
	}

	public IVisitableNode getNode() {
		return node;
	}

	public int getVarCountLimitOption() {
		return 8;
	}

	/** Calls the {@link #process_internal()} methods in all descendant contexts. */
	public final void process() {
		if (child != null) {
			child.process();
		}
		process_internal();
	}
	/** Processing work can be done in the implementations of this method. */
	protected abstract void process_internal();


	public final List<String> createLogParts() {
		return createLogParts(new HashSet<String>());
	}
	/**
	 * @param idsAlreadyHandled The variable names which are already in the log statements
	 * (they were either present before the refactoring, or added with another context)
	 */
	public final List<String> createLogParts(final Set<String> idsAlreadyHandled) {
		if (child == null) {
			return new ArrayList<String>();
		}

		final List<String> parts = child.createLogParts(idsAlreadyHandled);
		parts.addAll(createLogParts_internal(idsAlreadyHandled));
		return parts;
	}
	/** @return a list of the log arguments to be appended to the current {@link Log_Statement} */
	protected abstract List<String> createLogParts_internal(final Set<String> idsAlreadyHandled);

	protected String formatLogPart(final String varName) {
		return ", \", " + varName + ": \", " + varName;
	}
}
