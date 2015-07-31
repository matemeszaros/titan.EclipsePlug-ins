/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
public final class Function_Instance_Statement extends Statement {
	private static final String UNUSEDRETURN2 = "The template returned by {0} is not used";
	private static final String UNUSEDRETURN1 = "The value returned by {0} is not used";

	private static final String FULLNAMEPART = ".reference";
	private static final String STATEMENT_NAME = "function instance";

	private final Reference reference;

	public Function_Instance_Statement(final Reference reference) {
		this.reference = reference;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_FUNCTION_INSTANCE;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (reference == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return;
		}

		if (myStatementBlock != null) {
			myStatementBlock.checkRunsOnScope(timestamp, assignment, reference, "call");
		}

		switch (assignment.getAssignmentType()) {
		case A_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RVAL:
			location.reportConfigurableSemanticProblem(
					Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.REPORTUNUSEDFUNCTIONRETURNVALUES, GeneralConstants.WARNING, null),
					MessageFormat.format(UNUSEDRETURN1, assignment.getFullName()));
			break;
		case A_FUNCTION_RTEMP:
		case A_EXT_FUNCTION_RTEMP:
			location.reportConfigurableSemanticProblem(
					Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.REPORTUNUSEDFUNCTIONRETURNVALUES, GeneralConstants.WARNING, null),
					MessageFormat.format(UNUSEDRETURN2, assignment.getFullName()));
			break;
		default:
			break;
		}
	}

	public Reference getReference() {
		return reference;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (reference != null) {
			reference.updateSyntax(reparser, false);
			reparser.updateLocation(reference.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (reference == null) {
			return;
		}

		reference.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		return true;
	}
}
