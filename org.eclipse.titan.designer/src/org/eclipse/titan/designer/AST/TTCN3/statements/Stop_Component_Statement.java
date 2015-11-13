/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
/**
 * @author Kristof Szabados
 * */
public final class Stop_Component_Statement extends Statement {
	private static final String FULLNAMEPART = ".componentreference";
	private static final String STATEMENT_NAME = "stop test component";

	private final IValue componentReference;

	public Stop_Component_Statement(final IValue componentReference) {
		this.componentReference = componentReference;

		if (componentReference != null) {
			componentReference.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_STOP_COMPONENT;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (componentReference == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		if (componentReference != null) {
			componentReference.setMyScope(scope);
		}
	}

	@Override
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		if (componentReference != null) {
			IValue last = componentReference.getValueRefdLast(timestamp, null);
			if (Value_type.EXPRESSION_VALUE.equals(last.getValuetype())) {
				switch (((Expression_Value) last).getOperationType()) {
				case SELF_COMPONENT_OPERATION:
				case MTC_COMPONENT_OPERATION:
					return true;
				default:
					break;
				}
			}
		}

		return super.isTerminating(timestamp);
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		Port_Utility.checkComponentReference(timestamp, this, componentReference, true, false);

		lastTimeChecked = timestamp;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (componentReference instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) componentReference).updateSyntax(reparser, false);
			reparser.updateLocation(componentReference.getLocation());
		} else if (componentReference != null) {
			throw new ReParseException();
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentReference == null) {
			return;
		}

		componentReference.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (componentReference != null && !componentReference.accept(v)) {
			return false;
		}
		return true;
	}
}
