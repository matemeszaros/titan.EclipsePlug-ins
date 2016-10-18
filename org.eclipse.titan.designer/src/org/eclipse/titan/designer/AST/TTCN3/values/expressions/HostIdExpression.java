/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Expression type for
 *   hostid(in charstring idkind := "Ipv4orIPv6") return charstring
 * @author Arpad Lovassy
 */
public final class HostIdExpression extends Expression_Value {
	private static final String OPERAND1_ERROR1 = "The 1st operand of the `hostid' operation should be a charstring value";

	private final Value value1;

	public HostIdExpression(final Value value1) {
		this.value1 = value1;

		if (value1 != null) {
			value1.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.HOSTID_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("hostid(");
		builder.append(value1 == null ? "null" : value1.createStringRepresentation());
		builder.append(')');
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value1 != null) {
			value1.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (value1 == child) {
			return builder.append(OPERAND1);
		}
		
		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_CHARSTRING;
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
	 */
	private void checkExpressionOperands( final CompilationTimeStamp timestamp,
										  final Expected_Value_type expectedValue,
										  final IReferenceChain referenceChain) {
		//check value1
		checkExpressionOperand1(timestamp, expectedValue, referenceChain);
	}

	/** 
	 * Checks the 1st operand
	 * in charstring (optional)
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperand1( final CompilationTimeStamp timestamp,
										  final Expected_Value_type expectedValue,
										  final IReferenceChain referenceChain ) {
		if (value1 == null) {
			return;
		}

		value1.setLoweridToReference(timestamp);
		final Type_type tempType = value1.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_CHARSTRING:
			IValue last = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (!last.isUnfoldable(timestamp)) {
				final String originalString = ((Charstring_Value) last).getValue();
				CharstringExtractor cs = new CharstringExtractor( originalString );
				if ( cs.isErrorneous() ) {
					value1.getLocation().reportSemanticError( cs.getErrorMessage() );
					setIsErroneous(true);
				}
			}

			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERAND1_ERROR1);
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

		if (value1 != null) {
			checkExpressionOperands(timestamp, expectedValue, referenceChain);
		}

		return lastValue;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			if (value1 != null) {
				referenceChain.markState();
				value1.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value1 != null) {
			value1.updateSyntax(reparser, false);
			reparser.updateLocation(value1.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value1 != null) {
			value1.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (value1 != null && !value1.accept(v)) {
			return false;
		}
		return true;
	}
}
