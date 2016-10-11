/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a single error behavior attribute (which can hold a list of error
 * behaviors) on an external function, used to tell the generator of the
 * external function how to handle errors.
 * 
 * @author Kristof Szabados
 * */
public final class ErrorBehaviorAttribute extends ExtensionAttribute implements IVisitableNode {

	private final ErrorBehaviorList list;

	public ErrorBehaviorAttribute(final ErrorBehaviorList list) {
		this.list = list;
	}

	@Override
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.ERRORBEHAVIOR;
	}

	public ErrorBehaviorList getErrrorBehaviorList() {
		return list;
	}

	/**
	 * Does the semantic checking of the error behavior attribute.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (list != null) {
			list.check(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public boolean accept(final ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (list != null && !list.accept(v)) {
			return false;
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
