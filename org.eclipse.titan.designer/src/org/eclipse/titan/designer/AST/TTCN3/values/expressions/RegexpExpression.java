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
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class RegexpExpression extends Expression_Value {
	private static final String CANNOT_DETERMINE_ARG_TYPE = "Cannot determine the argument type of `regexp' operation";
	private static final String OPERANDERROR1 = "The first operand of the `regexp' operation should be a (universal) charstring value";
	private static final String OPERANDERROR2 = "The second operand of the `regexp' operation should be a charstring value";
	private static final String OPERANDERROR3 = "The third operand of the `regexp' operation should be an integer value";
	private static final String OPERANDERROR4 = "The third operand of operation `regexp' should not be negative";
	private static final String OPERANDERROR5 = "Using a large integer value ({0}) as the third operand of operation `regexp'' is not allowed";

	private final TemplateInstance templateInstance1;
	private final TemplateInstance templateInstance2;
	private final Value value3;

	public RegexpExpression(final TemplateInstance templateInstance1, final TemplateInstance templateInstance2, final Value value3) {
		this.templateInstance1 = templateInstance1;
		this.templateInstance2 = templateInstance2;
		this.value3 = value3;

		if (templateInstance1 != null) {
			templateInstance1.setFullNameParent(this);
		}
		if (templateInstance2 != null) {
			templateInstance2.setFullNameParent(this);
		}
		if (value3 != null) {
			value3.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.REGULAREXPRESSION_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder("regexp(");
		builder.append(templateInstance1.createStringRepresentation());
		builder.append(", ");
		builder.append(templateInstance2.createStringRepresentation());
		builder.append(", ");
		builder.append(value3.createStringRepresentation());
		builder.append(')');
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (templateInstance1 != null) {
			templateInstance1.setMyScope(scope);
		}
		if (templateInstance2 != null) {
			templateInstance2.setMyScope(scope);
		}
		if (value3 != null) {
			value3.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (templateInstance1 == child) {
			return builder.append(OPERAND1);
		} else if (templateInstance2 == child) {
			return builder.append(OPERAND2);
		} else if (value3 == child) {
			return builder.append(OPERAND3);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		IValue last = getValueRefdLast(timestamp, expectedValue, null);

		if (last == null || templateInstance1 == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		if (last.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return Type_type.TYPE_UNDEFINED;
		}

		ITTCN3Template template = templateInstance1.getTemplateBody().setLoweridToReference(timestamp);
		Type_type tempType = template.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
		switch (tempType) {
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
			return tempType;
		case TYPE_UNDEFINED:
			return tempType;
		default:
			setIsErroneous(true);
			return Type_type.TYPE_UNDEFINED;
		}
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		/*
		 * if(value1 == null || value2 == null || value3 == null ||
		 * get_is_erroneous(timestamp)){ return true; }
		 * 
		 * return value1.is_unfoldable(timestamp, referenceChain) ||
		 * value2.is_unfoldable(timestamp, referenceChain) ||
		 * value3.is_unfoldable(timestamp, referenceChain);
		 */
		// FIXME implement regexp evaluation once patterns become
		// supported
		return true;
	}

	@Override
	public IValue setLoweridToReference(final CompilationTimeStamp timestamp) {
		if (templateInstance1 != null && templateInstance1.getType() != null && templateInstance1.getDerivedReference() != null) {
			templateInstance1.getTemplateBody().setLoweridToReference(timestamp);
		}

		if (templateInstance2 != null && templateInstance2.getType() != null && templateInstance2.getDerivedReference() != null) {
			templateInstance2.getTemplateBody().setLoweridToReference(timestamp);
		}

		return this;
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
		Expected_Value_type internalExpectation = Expected_Value_type.EXPECTED_DYNAMIC_VALUE.equals(expectedValue) ? Expected_Value_type.EXPECTED_TEMPLATE
				: expectedValue;

		setIsErroneous(false);

		if (templateInstance1 != null) {
			IType governor1 = templateInstance1.getExpressionGovernor(timestamp, internalExpectation);
			if (governor1 == null) {
				ITTCN3Template temp = templateInstance1.getTemplateBody().setLoweridToReference(timestamp);
				governor1 = temp.getExpressionGovernor(timestamp, internalExpectation);
			}
			if (governor1 == null) {
				templateInstance1.getLocation().reportSemanticError(CANNOT_DETERMINE_ARG_TYPE);
				setIsErroneous(true);
				return;
			}
			IsValueExpression.checkExpressionTemplateInstance(timestamp, this, templateInstance1, governor1, referenceChain,
					expectedValue);

			if (getIsErroneous(timestamp)) {
				return;
			}

			ITTCN3Template temp = templateInstance1.getTemplateBody().setLoweridToReference(timestamp);
			temp.checkSpecificValue(timestamp, false);

			switch (governor1.getTypeRefdLast(timestamp).getTypetypeTtcn3()) {
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
				break;
			case TYPE_UNDEFINED:
				setIsErroneous(true);
				break;
			default:
				location.reportSemanticError(OPERANDERROR1);
				setIsErroneous(true);
				break;
			}
		}

		if (templateInstance2 != null) {
			IType governor2 = templateInstance2.getExpressionGovernor(timestamp, internalExpectation);
			if (governor2 == null) {
				ITTCN3Template temp = templateInstance2.getTemplateBody().setLoweridToReference(timestamp);
				governor2 = temp.getExpressionGovernor(timestamp, internalExpectation);
			}
			if (governor2 == null) {
				templateInstance2.getLocation().reportSemanticError(CANNOT_DETERMINE_ARG_TYPE);
				setIsErroneous(true);
				return;
			}
			IsValueExpression.checkExpressionTemplateInstance(timestamp, this, templateInstance2, governor2, referenceChain,
					expectedValue);

			if (getIsErroneous(timestamp)) {
				return;
			}

			switch (governor2.getTypeRefdLast(timestamp).getTypetype()) {
			case TYPE_CHARSTRING:
				break;
			case TYPE_UNDEFINED:
				setIsErroneous(true);
				break;
			default:
				location.reportSemanticError(OPERANDERROR2);
				setIsErroneous(true);
				break;
			}
		}

		if (value3 != null) {
			IValue temp = value3.setLoweridToReference(timestamp);
			Type_type tempType3 = temp.getExpressionReturntype(timestamp, expectedValue);

			switch (tempType3) {
			case TYPE_INTEGER:
				IValue last3 = temp.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (!last3.isUnfoldable(timestamp) && Value.Value_type.INTEGER_VALUE.equals(last3.getValuetype())) {
					if (!((Integer_Value) last3).isNative()) {
						value3.getLocation().reportSemanticError(MessageFormat.format(OPERANDERROR5, last3));
						setIsErroneous(true);
					} else {
						long i = ((Integer_Value) last3).getValue();
						if (i < 0) {
							value3.getLocation().reportSemanticError(OPERANDERROR4);
							setIsErroneous(true);
						}
					}
				}
				break;
			case TYPE_UNDEFINED:
				setIsErroneous(true);
				break;
			default:
				location.reportSemanticError(OPERANDERROR3);
				setIsErroneous(true);
				break;
			}
		}

		// TODO add regexp specific checks once patterns become
		// supported
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

		if (templateInstance1 == null || templateInstance2 == null || value3 == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		if (getIsErroneous(timestamp)) {
			return lastValue;
		}

		if (isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		// FIXME implement regexp evaluation once patterns become
		// supported
		return lastValue;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			if (templateInstance1 != null) {
				referenceChain.markState();
				templateInstance1.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
			if (templateInstance2 != null) {
				referenceChain.markState();
				templateInstance2.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
			if (value3 != null) {
				referenceChain.markState();
				value3.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (templateInstance1 != null) {
			templateInstance1.updateSyntax(reparser, false);
			reparser.updateLocation(templateInstance1.getLocation());
		}

		if (templateInstance2 != null) {
			templateInstance2.updateSyntax(reparser, false);
			reparser.updateLocation(templateInstance2.getLocation());
		}

		if (value3 != null) {
			value3.updateSyntax(reparser, false);
			reparser.updateLocation(value3.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (templateInstance1 != null) {
			templateInstance1.findReferences(referenceFinder, foundIdentifiers);
		}
		if (templateInstance2 != null) {
			templateInstance2.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value3 != null) {
			value3.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (templateInstance1 != null && !templateInstance1.accept(v)) {
			return false;
		}
		if (templateInstance2 != null && !templateInstance2.accept(v)) {
			return false;
		}
		if (value3 != null && !value3.accept(v)) {
			return false;
		}
		return true;
	}
}
