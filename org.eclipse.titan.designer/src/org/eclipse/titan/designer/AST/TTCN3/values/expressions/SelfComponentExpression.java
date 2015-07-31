/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.RunsOnScope;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the self component.
 * 
 * @author Kristof Szabados
 * */
public final class SelfComponentExpression extends Expression_Value {
	private static final String OPERATIONNAME = "self";

	@Override
	public Operation_type getOperationType() {
		return Operation_type.SELF_COMPONENT_OPERATION;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_COMPONENT;
	}

	@Override
	public String createStringRepresentation() {
		return "self";
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		if (myScope == null) {
			return null;
		}

		RunsOnScope runsOnScope = myScope.getScopeRunsOn();
		if (runsOnScope == null) {
			return null;
		}

		return runsOnScope.getComponentType();
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
		if (myGovernor == null || myScope == null) {
			return;
		}

		IType governorLast = myGovernor.getTypeRefdLast(timestamp);
		if (!Type_type.TYPE_COMPONENT.equals(governorLast.getTypetype())) {
			return;
		}

		RunsOnScope runsOnScope = myScope.getScopeRunsOn();
		if (runsOnScope == null) {
			return;
		}

		Type componentType = runsOnScope.getComponentType();
		if (componentType != null && !governorLast.isCompatible(timestamp, componentType, null, null, null)) {
			getLocation().reportSemanticError(
					MessageFormat.format(
							"Incompatible component types: a component reference of type `{0}'' was expected, but `self'' has type `{1}''",
							governorLast.getTypename(), componentType.getTypename()));
			setIsErroneous(true);
		}

		checkExpressionDynamicPart(expectedValue, OPERATIONNAME, false, true, false);
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

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		return lastValue;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		// no members
		return true;
	}
}
