/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

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
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.values.Altstep_Reference_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Function_Reference_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Testcase_Reference_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a refers expression.
 * <p>
 * In the compiler this is a direct value.
 * 
 * @author Kristof Szabados
 * */
public final class RefersExpression extends Expression_Value {
	private static final String OPERANDERROR = "Reference to a function, external function, altstep or testcase was expected.";

	private Reference refered;
	private Assignment referedAssignment;

	public RefersExpression(final Reference refered) {
		this.refered = refered;

		if (refered != null) {
			refered.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.REFERS_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		if (refered == null) {
			return "<erroneous value>";
		}

		StringBuilder builder = new StringBuilder();
		builder.append("refers(").append(refered.getDisplayName()).append(')');
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (refered != null) {
			refered.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (refered == child) {
			return builder.append(OPERAND);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (referedAssignment == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		switch (referedAssignment.getAssignmentType()) {
		case A_FUNCTION:
		case A_FUNCTION_RTEMP:
		case A_FUNCTION_RVAL:
		case A_EXT_FUNCTION:
		case A_EXT_FUNCTION_RTEMP:
		case A_EXT_FUNCTION_RVAL:
			return Type_type.TYPE_FUNCTION;
		case A_ALTSTEP:
			return Type_type.TYPE_ALTSTEP;
		case A_TESTCASE:
			return Type_type.TYPE_TESTCASE;
		default:
			return Type_type.TYPE_UNDEFINED;
		}
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return false;
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
		if (refered == null) {
			return;
		}

		referedAssignment = refered.getRefdAssignment(timestamp, false);
		if (referedAssignment == null) {
			return;
		}

		switch (referedAssignment.getAssignmentType()) {
		case A_FUNCTION:
		case A_FUNCTION_RTEMP:
		case A_FUNCTION_RVAL:
		case A_EXT_FUNCTION:
		case A_EXT_FUNCTION_RTEMP:
		case A_EXT_FUNCTION_RVAL:
		case A_ALTSTEP:
		case A_TESTCASE:
			break;
		default:
			location.reportSemanticError(OPERANDERROR);
			setIsErroneous(true);
			break;
		}
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

		if (refered == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		if (getIsErroneous(timestamp) || referedAssignment == null) {
			return lastValue;
		}

		if (isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		switch (referedAssignment.getAssignmentType()) {
		case A_FUNCTION:
		case A_FUNCTION_RTEMP:
		case A_FUNCTION_RVAL:
			lastValue = new Function_Reference_Value((Def_Function) referedAssignment);
			lastValue.copyGeneralProperties(this);
			break;
		case A_EXT_FUNCTION:
		case A_EXT_FUNCTION_RTEMP:
		case A_EXT_FUNCTION_RVAL:
			lastValue = new Function_Reference_Value((Def_Extfunction) referedAssignment);
			lastValue.copyGeneralProperties(this);
			break;
		case A_ALTSTEP:
			lastValue = new Altstep_Reference_Value((Def_Altstep) referedAssignment);
			lastValue.copyGeneralProperties(this);
			break;
		case A_TESTCASE:
			lastValue = new Testcase_Reference_Value((Def_Testcase) referedAssignment);
			lastValue.copyGeneralProperties(this);
			break;
		default:
			setIsErroneous(true);
			break;
		}
		// transform

		return lastValue;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (refered != null) {
			refered.updateSyntax(reparser, false);
			reparser.updateLocation(refered.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (refered == null) {
			return;
		}

		refered.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (refered != null && !refered.accept(v)) {
			return false;
		}
		return true;
	}
}
