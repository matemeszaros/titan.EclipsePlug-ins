/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.Port_Utility;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.CharstringExtractor;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Expression type for
 * Checkstate port operation
 * ( Port | all port | any port ) "." checkstate "(" SingleExpression ")"
 * @author Arpad Lovassy
 */
public final class CheckStateExpression extends Expression_Value {
	
	private static final String NOINCOMINGQUEUE = "Port type `{0}'' does not have incoming queue"
			+ " because it has neither incoming messages nor incoming or outgoing signatues";
	
	private static final String OPERAND1_ERROR1 = "The operand of the `checkstate' operation should be a charstring";
	
	/** port reference */
	private final Reference mPortReference;

	/**
	 * The parameter of the checkstate operation
	 * 
	 * The parameter of the checkstate operation shall be of type charstring and shall have one of the
	 * following values:
	 * a) "Started"
	 * b) "Halted"
	 * c) "Stopped"
	 * d) "Connected"
	 * e) "Mapped"
	 * f) "Linked" 
	 */
	private final Value mValue;

	public CheckStateExpression(final Reference aPortReference, final Value aValue) {
		this.mPortReference = aPortReference;
		this.mValue = aValue;

		if (mPortReference != null) {
			mPortReference.setFullNameParent(this);
		}
		
		if (mValue != null) {
			mValue.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.CHECKSTATE_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append(mPortReference.getDisplayName());
		builder.append(".checkstate(");
		builder.append(mValue.createStringRepresentation());
		builder.append(")");
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (mPortReference != null) {
			mPortReference.setMyScope(scope);
		}
		if (mValue != null) {
			mValue.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (mPortReference == child) {
			return builder.append(OPERAND1);
		} else if (mValue == child) {
			return builder.append(OPERAND2);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_BOOL;
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
		//check mPortReference
		final Port_Type portType = Port_Utility.checkPortReference(timestamp, mPortReference);
		if (portType != null && !portType.getPortBody().hasQueue(timestamp)) {
			mPortReference.getLocation().reportSemanticError(MessageFormat.format(NOINCOMINGQUEUE, portType.getTypename()));
		}

		
		//check the operand (mValue)
		checkExpressionOperand1(timestamp, expectedValue, referenceChain);
	}

	/** 
	 * Checks the operand
	 * in charstring (mandatory)
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
		if (mValue == null) {
			setIsErroneous(true);
			return;
		}

		mValue.setLoweridToReference(timestamp);
		final Type_type tempType = mValue.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_CHARSTRING:
			IValue last = mValue.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (!last.isUnfoldable(timestamp)) {
				final String originalString = ((Charstring_Value) last).getValue();
				CharstringExtractor cs = new CharstringExtractor( originalString );
				if ( cs.isErrorneous() ) {
					mValue.getLocation().reportSemanticError( cs.getErrorMessage() );
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

		if (mValue == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		if (getIsErroneous(timestamp)) {
			return lastValue;
		}

		if (isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		lastValue = mValue;
		return lastValue;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (mValue != null) {
			mValue.updateSyntax(reparser, false);
			reparser.updateLocation(mValue.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (mValue == null) {
			return;
		}

		mValue.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (mValue != null && !mValue.accept(v)) {
			return false;
		}
		return true;
	}
}
