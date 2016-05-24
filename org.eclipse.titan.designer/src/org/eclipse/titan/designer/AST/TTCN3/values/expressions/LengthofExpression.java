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
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Array_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class LengthofExpression extends Expression_Value {
	private static final String OPERANDERROR = "Cannot determine the argument type of `lengthof()'' operation";
	private static final String UNEXPECTEDOPERAND = "The first operand of operation `lengthof' should be a string,"
			+ " a `record of', a `set of' or an `array' value";

	private final TemplateInstance templateInstance;

	public LengthofExpression(final TemplateInstance templateInstance) {
		this.templateInstance = templateInstance;

		if (templateInstance != null) {
			templateInstance.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.LENGTHOF_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("lengthof(");
		builder.append(templateInstance.createStringRepresentation());
		builder.append(')');
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (templateInstance != null) {
			templateInstance.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (templateInstance == child) {
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
		if (templateInstance == null) {
			return true;
		}

		if (templateInstance.getDerivedReference() != null) {
			return true;
		}

		ITTCN3Template template = templateInstance.getTemplateBody().setLoweridToReference(timestamp);
		if (Template_type.SPECIFIC_VALUE.equals(template.getTemplatetype())) {
			return ((SpecificValue_Template) templateInstance.getTemplateBody()).getSpecificValue().isUnfoldable(timestamp,
					expectedValue, referenceChain);
		}
		// TODO we could unfold way more cases
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
		Expected_Value_type internalExpectation = Expected_Value_type.EXPECTED_DYNAMIC_VALUE.equals(expectedValue) ? Expected_Value_type.EXPECTED_TEMPLATE
				: expectedValue;

		IType governor = templateInstance.getExpressionGovernor(timestamp, internalExpectation);
		if (governor == null) {
			ITTCN3Template template = templateInstance.getTemplateBody().setLoweridToReference(timestamp);
			governor = template.getExpressionGovernor(timestamp, internalExpectation);
		}
		if (governor == null) {
			templateInstance.getLocation().reportSemanticError(OPERANDERROR);
			setIsErroneous(true);
			return;
		}

		Type_type typetype = templateInstance.getExpressionReturntype(timestamp, internalExpectation);
		switch (typetype) {
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
		case TYPE_BITSTRING:
		case TYPE_HEXSTRING:
		case TYPE_OCTETSTRING:
		case TYPE_SEQUENCE_OF:
		case TYPE_SET_OF:
		case TYPE_ARRAY:
			break;
		case TYPE_UNDEFINED:
			break;
		default:
			templateInstance.getLocation().reportSemanticError(UNEXPECTEDOPERAND);
			setIsErroneous(true);
			return;
		}

		IsValueExpression.checkExpressionTemplateInstance(timestamp, this, templateInstance, governor, referenceChain, expectedValue);
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

		if (templateInstance == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		if (getIsErroneous(timestamp)) {
			return lastValue;
		}

		if (isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		ITTCN3Template template = templateInstance.getTemplateBody();
		IValue value = ((SpecificValue_Template) template).getSpecificValue().getValueRefdLast(timestamp, referenceChain);
		int length;
		switch (value.getValuetype()) {
		case CHARSTRING_VALUE:
			length = ((Charstring_Value) value).getValueLength();
			break;
		case UNIVERSALCHARSTRING_VALUE:
			length = ((UniversalCharstring_Value) value).getValueLength();
			break;
		case BITSTRING_VALUE:
			length = ((Bitstring_Value) value).getValueLength();
			break;
		case HEXSTRING_VALUE:
			length = ((Hexstring_Value) value).getValueLength();
			break;
		case OCTETSTRING_VALUE:
			length = ((Octetstring_Value) value).getValueLength();
			break;
		case SEQUENCEOF_VALUE:
			length = ((SequenceOf_Value) value).getNofComponents();
			break;
		case SETOF_VALUE:
			length = ((SetOf_Value) value).getNofComponents();
			break;
		case ARRAY_VALUE:
			length = ((Array_Value) value).getNofComponents();
			break;
		default:
			setIsErroneous(true);
			return lastValue;
		}

		lastValue = new Integer_Value(length);
		lastValue.copyGeneralProperties(this);
		return lastValue;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			if (templateInstance != null) {
				referenceChain.markState();
				templateInstance.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (templateInstance != null) {
			templateInstance.updateSyntax(reparser, false);
			reparser.updateLocation(templateInstance.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (templateInstance == null) {
			return;
		}

		templateInstance.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (templateInstance != null && !templateInstance.accept(v)) {
			return false;
		}
		return true;
	}
}
