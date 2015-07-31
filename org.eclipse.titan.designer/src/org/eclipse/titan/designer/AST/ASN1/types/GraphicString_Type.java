/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class GraphicString_Type extends ASN1Type {

	private static final String GRAPHICSTRINGVALUEEXPECTED = "Graphicl string value was expected";

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_GRAPHICSTRING;
	}

	@Override
	public IASN1Type newInstance() {
		return new GraphicString_Type();
	}

	@Override
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_CHARSTRING;
	}

	@Override
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		final IType temp = otherType.getTypeRefdLast(timestamp);
		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		switch (temp.getTypetype()) {
		case TYPE_TELETEXSTRING:
		case TYPE_VIDEOTEXSTRING:
		case TYPE_GRAPHICSTRING:
		case TYPE_OBJECTDESCRIPTOR:
		case TYPE_GENERALSTRING:
		case TYPE_CHARSTRING:
		case TYPE_NUMERICSTRING:
		case TYPE_PRINTABLESTRING:
		case TYPE_IA5STRING:
		case TYPE_VISIBLESTRING:
		case TYPE_UTCTIME:
		case TYPE_GENERALIZEDTIME:
		case TYPE_UNIVERSALSTRING:
			return true;
		default:
			return false;
		}
	}

	@Override
	public String getTypename() {
		return "GraphicString";
	}

	@Override
	public String getOutlineIcon() {
		return "charstring.gif";
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != constraints) {
			constraints.check(timestamp);
		}
	}

	@Override
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		super.checkThisValue(timestamp, value, valueCheckingOptions);

		IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
		if (null == last || last.getIsErroneous(timestamp)) {
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

		switch (last.getValuetype()) {
		case UNDEFINED_BLOCK:
			last = last.setValuetype(timestamp, Value_type.CHARSYMBOLS_VALUE);
			if (last.getIsErroneous(timestamp)) {
				return;
			}

			last.setValuetype(timestamp, Value_type.ISO2022STRING_VALUE);
			break;
		case CHARSYMBOLS_VALUE:
		case CHARSTRING_VALUE:
			last.setValuetype(timestamp, Value_type.ISO2022STRING_VALUE);
			break;
		case ISO2022STRING_VALUE:
		case UNIVERSALCHARSTRING_VALUE:
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(GRAPHICSTRINGVALUEEXPECTED);
			value.setIsErroneous(true);
		}
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit) {
		registerUsage(template);
		CharString_Type.checkThisTemplateString(timestamp, this, template, isModified);
	}

	@Override
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			if (subreferences.size() > actualSubReference + 1) {
				subreference.getLocation().reportSemanticError(ArraySubReference.INVALIDSTRINGELEMENTINDEX);
				return null;
			} else if (subreferences.size() == actualSubReference + 1) {
				reference.setStringElementReferencing();
			}
			return this;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId()
							.getDisplayName(), getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference)
							.getId().getDisplayName(), getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("graphic string");
	}
}
