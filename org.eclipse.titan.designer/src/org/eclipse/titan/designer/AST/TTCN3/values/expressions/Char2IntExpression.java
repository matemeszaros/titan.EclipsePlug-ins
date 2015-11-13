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
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.CharstringExtractor;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Char2IntExpression extends Expression_Value {
	private static final String OPERANDERROR1 = "The operand of the `char2int' operation should be a charstring value";
	private static final String OPERANDERROR2 = "The operand of the `char2int' operation should be of length 1";

	private final Value value;

	public Char2IntExpression(final Value value) {
		this.value = value;

		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.CHAR2STR_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append("char2int(").append(value.createStringRepresentation()).append(')');
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (value == child) {
			return builder.append(OPERAND);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_INTEGER;
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (value == null) {
			return true;
		}

		return value.isUnfoldable(timestamp, expectedValue, referenceChain);
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
		if (value == null) {
			return;
		}

		value.setLoweridToReference(timestamp);
		Type_type tempType = value.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_CHARSTRING:
			IValue last = value.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (!last.isUnfoldable(timestamp)) {
				final String originalString = ((Charstring_Value) last).getValue();
				CharstringExtractor cs = new CharstringExtractor( originalString );
				if ( cs.isErrorneous() ) {
					value.getLocation().reportSemanticError( cs.getErrorMessage() );
					setIsErroneous(true);
				}
				else {
					final String string = cs.getExtractedString();
					if (string != null && string.length() != 1) {
						value.getLocation().reportSemanticError(OPERANDERROR2);
						setIsErroneous(true);
					}
				}
			}

			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERANDERROR1);
				setIsErroneous(true);
			}
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

		if (value == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		if (getIsErroneous(timestamp)) {
			return lastValue;
		}

		if (isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		IValue last = value.getValueRefdLast(timestamp, referenceChain);
		if (last.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return lastValue;
		}

		switch (last.getValuetype()) {
		case CHARSTRING_VALUE:
			String string = ((Charstring_Value) last).getValue();
			lastValue = new Integer_Value(string.charAt(0));
			lastValue.copyGeneralProperties(this);
			break;
		default:
			setIsErroneous(true);
			break;
		}

		return lastValue;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this) && value != null) {
			referenceChain.markState();
			value.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value == null) {
			return;
		}

		value.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (value != null && !value.accept(v)) {
			return false;
		}
		return true;
	}
}
