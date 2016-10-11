/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Type_Assignment;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueRange;
import org.eclipse.titan.designer.AST.TTCN3.templates.Value_Range_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class Float_Type extends ASN1Type {
	private static final String REALVALUEEXPECTED = "REAL value was expected";
	private static final String FLOATVALUEEXPECTED = "float value was expected";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for type `float''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for type `float''";
	private static final String INCORRECTBOUNDARIES = "The lower boundary is higher than the upper boundary";

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_REAL;
	}

	@Override
	public IASN1Type newInstance() {
		return new Float_Type();
	}

	@Override
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		IType temp = otherType.getTypeRefdLast(timestamp);
		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return Type_type.TYPE_REAL.equals(temp.getTypetype());
	}

	@Override
	public Type_type getTypetypeTtcn3() {
		if (isErroneous) {
			return Type_type.TYPE_UNDEFINED;
		}

		return getTypetype();
	}

	@Override
	public String getTypename() {
		return "float";
	}

	@Override
	public String getOutlineIcon() {
		return "float.gif";
	}
	
	@Override
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_FLOAT;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		parseAttributes(timestamp);

		if (constraints != null) {
			constraints.check(timestamp);
		}
		
		checkSubtypeRestrictions(timestamp);
	}

	@Override
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		super.checkThisValue(timestamp, value, valueCheckingOptions);

		IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
		if (last == null || last.getIsErroneous(timestamp)) {
			return;
		}

		// already handled ones
		switch (value.getValuetype()) {
		case OMIT_VALUE:
		case REFERENCED_VALUE:
			return;
		case UNDEFINED_LOWERIDENTIFIER_VALUE:
			if (Value_type.REFERENCED_VALUE.equals(last.getValuetype())) {
				return;
			}
			break;
		default:
			break;
		}

		if (value.isAsn()) {
			if (Value_type.REFERENCED_VALUE.equals(value.getValuetype())) {
				IType lastType = last.getMyGovernor().getTypeRefdLast(timestamp);
				if (!lastType.getIsErroneous(timestamp) && !Type_type.TYPE_REAL.equals(lastType.getTypetype())) {
					value.getLocation().reportSemanticError(REALVALUEEXPECTED);
					value.setIsErroneous(true);
					return;
				}
			}
			switch (last.getValuetype()) {
			case REAL_VALUE:
				break;
			case UNDEFINED_BLOCK: {
				last = last.setValuetype(timestamp, Value_type.SEQUENCE_VALUE);
				Identifier identifier = new Identifier(Identifier_type.ID_ASN, "REAL");
				Assignment assignment = getMyScope().getAssignmentsScope().getLocalAssignmentByID(timestamp, identifier);
				((Type_Assignment) assignment).getType(timestamp).checkThisValue(
						timestamp, last, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT,
								false, false, true, false, valueCheckingOptions.str_elem));
				last = last.setValuetype(timestamp, Value_type.REAL_VALUE);
				break; }
			case INTEGER_VALUE:
				last.setValuetype(timestamp, Value_type.REAL_VALUE);
				break;
			case EXPRESSION_VALUE:
			case MACRO_VALUE:
				// already checked
				break;
			default:
				last.getLocation().reportSemanticError(REALVALUEEXPECTED);
				last.setIsErroneous(true);
				break;
			}
		} else {
			switch (last.getValuetype()) {
			case REAL_VALUE:
				break;
			case EXPRESSION_VALUE:
			case MACRO_VALUE:
				// already checked
				break;
			default:
				value.getLocation().reportSemanticError(FLOATVALUEEXPECTED);
				value.setIsErroneous(true);
			}
		}

		if (valueCheckingOptions.sub_check) {
			//there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, last);
			}
		}

		value.setLastTimeChecked(timestamp);
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit) {
		registerUsage(template);
		template.setMyGovernor(this);

		if (getIsErroneous(timestamp)) {
			return;
		}

		if (Template_type.VALUE_RANGE.equals(template.getTemplatetype())) {
			ValueRange range = ((Value_Range_Template) template).getValueRange();
			IValue lower = checkBoundary(timestamp, range.getMin());
			IValue upper = checkBoundary(timestamp, range.getMax());

			if (lower != null && upper != null) {
				if (((Real_Value) lower).getValue() > ((Real_Value) upper).getValue()) {
					template.getLocation().reportSemanticError(INCORRECTBOUNDARIES);
				}
			}
		} else {
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName()));
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(LENGTHRESTRICTIONNOTALLOWED);
		}
	}

	private IValue checkBoundary(final CompilationTimeStamp timestamp, final Value value) {
		if (value == null) {
			return null;
		}

		value.setMyGovernor(this);
		IValue temp = checkThisValueRef(timestamp, value);
		checkThisValue(timestamp, temp, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, false, true, false, false));
		temp = temp.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
		switch (temp.getValuetype()) {
		case REAL_VALUE:
			break;
		default:
			temp = null;
			break;
		}

		return temp;
	}

	@Override
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDSUBREFERENCE, getTypename()));
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("float");
	}

}
