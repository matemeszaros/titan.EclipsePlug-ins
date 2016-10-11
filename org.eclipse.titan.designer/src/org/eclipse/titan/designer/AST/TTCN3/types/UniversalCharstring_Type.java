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
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.CharString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString;
import org.eclipse.titan.designer.AST.TTCN3.templates.UnivCharString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueRange;
import org.eclipse.titan.designer.AST.TTCN3.templates.Value_Range_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString.PatternType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class UniversalCharstring_Type extends Type {
	private static final String CHARSTRINGVALUEEXPECTED = "Universal character string value was expected";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for type `{1}''";
	private static final String INCORRECTBOUNDARIES = "The lower boundary is higher than the upper boundary";
	private static final String INFINITEBOUNDARYERROR = "The {0} boundary must be a universalcharstring value";
	private static final String TOOLONGBOUNDARYERROR = "The {0} boundary must be a universalcharstring value containing a single character.";

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_UCHARSTRING;
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("universalcharstring");
	}

	@Override
	public String getOutlineIcon() {
		return "universal_charstring.gif";
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

		switch (temp.getTypetype()) {
		case TYPE_UCHARSTRING:
		case TYPE_UTF8STRING:
		case TYPE_BMPSTRING:
		case TYPE_UNIVERSALSTRING:
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
			return true;
		default:
			return false;
		}
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
		return "universal charstring";
	}

	@Override
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_UNIVERSAL_CHARSTRING;
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

		switch (last.getValuetype()) {
		case UNDEFINED_BLOCK:
			last = last.setValuetype(timestamp, Value_type.CHARSYMBOLS_VALUE);
			if (last.getIsErroneous(timestamp)) {
				return;
			}

			last = last.setValuetype(timestamp, Value_type.UNIVERSALCHARSTRING_VALUE);
			break;
		case CHARSYMBOLS_VALUE:
		case CHARSTRING_VALUE:
			last = last.setValuetype(timestamp, Value_type.UNIVERSALCHARSTRING_VALUE);
			break;
		case ISO2022STRING_VALUE:
			location.reportSemanticError(UniversalCharstring_Value.ISOCONVERTION);
			setIsErroneous(true);
			break;
		case UNIVERSALCHARSTRING_VALUE:
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(CHARSTRINGVALUEEXPECTED);
			value.setIsErroneous(true);
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
		checkThisTemplateString(timestamp, this, template, isModified);
	}

	/**
	 * Checks if the provided template is valid for the provided type.
	 * <p>
	 * The type must be equivalent with the TTCN-3 universal charstring type
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param type the universal charstring type used for the check.
	 * @param template the template to be checked by the type.
	 * @param isModified true if the template is a modified template
	 * */
	public static void checkThisTemplateString(final CompilationTimeStamp timestamp, final Type type,
			final ITTCN3Template template, final boolean isModified) {
		template.setMyGovernor(type);

		PatternString ps = null;

		switch (template.getTemplatetype()) {
		case VALUE_RANGE: {
			ValueRange range = ((Value_Range_Template) template).getValueRange();
			IValue lower = checkBoundary(timestamp, type, range.getMin(), template, "lower");
			IValue upper = checkBoundary(timestamp, type, range.getMax(), template, "upper");

			if (lower != null && upper != null) {
				UniversalCharstring value1;
				if (Value_type.CHARSTRING_VALUE.equals(lower.getValuetype())) {
					value1 = new UniversalCharstring(((Charstring_Value) lower).getValue());
				} else {
					value1 = ((UniversalCharstring_Value) lower).getValue();
				}

				UniversalCharstring value2;
				if (Value_type.CHARSTRING_VALUE.equals(upper.getValuetype())) {
					value2 = new UniversalCharstring(((Charstring_Value) upper).getValue());
				} else {
					value2 = ((UniversalCharstring_Value) upper).getValue();
				}

				if (value1.compareWith(value2) > 0) {
					template.getLocation().reportSemanticError(INCORRECTBOUNDARIES);
				}
			}
			break;
		}
		case CSTR_PATTERN: {
			// Change the pattern type
			CharString_Pattern_Template cstrpt = (CharString_Pattern_Template) template;
			ps = cstrpt.getPatternstring();
			ps.setPatterntype(PatternType.UNIVCHARSTRING_PATTERN);

			//FIXME might need some implementation
			break;
		}
		case USTR_PATTERN:
			// FIXME implement as soon as charstring pattern templates become handled
			ps = ((UnivCharString_Pattern_Template) template).getPatternstring();
			break;
		default:
			template.getLocation().reportSemanticError(
					MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), type.getTypename()));
			break;
		}
	}

	private static IValue checkBoundary(final CompilationTimeStamp timestamp, final Type type, final Value value,
			final ITTCN3Template template, final String which) {
		if (value == null) {
			template.getLocation().reportSemanticError(MessageFormat.format(INFINITEBOUNDARYERROR, which));
			return null;
		}

		value.setMyGovernor(type);
		IValue temp = type.checkThisValueRef(timestamp, value);
		type.checkThisValue(timestamp, temp, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, false, true, false, false));
		temp = temp.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
		switch (temp.getValuetype()) {
		case CHARSTRING_VALUE:
			if (((Charstring_Value) temp).getValueLength() != 1) {
				value.getLocation().reportSemanticError(MessageFormat.format(TOOLONGBOUNDARYERROR, which));
			}
			break;
		case UNIVERSALCHARSTRING_VALUE:
			if (((UniversalCharstring_Value) temp).getValueLength() != 1) {
				value.getLocation().reportSemanticError(MessageFormat.format(TOOLONGBOUNDARYERROR, which));
			}
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
			if (subreferences.size() > actualSubReference + 1) {
				subreference.getLocation().reportSemanticError(ArraySubReference.INVALIDSTRINGELEMENTINDEX);
				return null;
			} else if (subreferences.size() == actualSubReference + 1) {
				reference.setStringElementReferencing();
			}
			return this;
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
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		ISubReference subreference = subreferences.get(i);
		if (Subreference_type.arraySubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() == i + 1) {
				declarationCollector.addDeclaration("universalcharstring", location, this);
			}
		}
	}
}
