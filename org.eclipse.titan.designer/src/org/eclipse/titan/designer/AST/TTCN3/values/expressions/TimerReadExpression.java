/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class TimerReadExpression extends Expression_Value {
	private static final String OPERANDERROR = "The operand of operation `timer read'':"
			+ " Reference to a single timer `{0}'' cannot have field or array sub-references";
	private static final String OPERANDERROR2 = "The operand of operation `timer read'' should be a timer instead of `{0}''";
	private static final String OPERATIONNAME = "timer read";

	private Reference reference;

	public TimerReadExpression(final Reference reference) {
		this.reference = reference;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.TIMER_READ_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append(reference.getDisplayName()).append(".read");
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (reference == child) {
			return builder.append(OPERAND);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_REAL;
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return true;
	}

	/**
	 * Checks the parameters of the expression and if they are valid in
	 * their position in the expression or not.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 * */
	private void checkExpressionOperands(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (reference == null) {
			return;
		}

		Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			setIsErroneous(true);
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_TIMER: {
			ArrayDimensions dimensions = ((Def_Timer) assignment).getDimensions();
			if (dimensions != null) {
				dimensions.checkIndices(timestamp, reference, "timer", false, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			} else if (reference.getSubreferences().size() > 1) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format(OPERANDERROR, assignment.getIdentifier().getDisplayName()));
			}
			break;
		}
		case A_PAR_TIMER:
			if (reference.getSubreferences().size() > 1) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format(OPERANDERROR, assignment.getIdentifier().getDisplayName()));
			}
			break;
		default:
			reference.getLocation().reportSemanticError(MessageFormat.format(OPERANDERROR2, assignment.getDescription()));
			setIsErroneous(true);
			break;
		}

		checkExpressionDynamicPart(expectedValue, OPERATIONNAME, true, true, false);
	}

	@Override
	public IValue evaluateValue(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return lastValue;
		}

		isErroneous = false;
		lastTimeChecked = timestamp;
		lastValue = this;

		if (reference == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		return lastValue;
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
