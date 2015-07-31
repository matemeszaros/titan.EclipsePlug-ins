/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.titan.designer.AST.CachedReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Choice_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class TTCN3_Choice_Type extends TTCN3_Set_Seq_Choice_BaseType {
	private static final String UNSUPPERTED_FIELDNAME =
			"Sorry, but it is not supported for sequence types to have a field with a name (`{0}'') which exactly matches the name of the type definition.";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for union type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for union type `{0}''";
	private static final String ONEFIELDEXPECTED = "A template for union type must contain exactly one selected field";
	private static final String REFERENCETONONEXISTENTFIELD = "Reference to non-existent field `{0}'' in union template for type `{1}''";
	private static final String CHOICEEXPECTED = "CHOICE value was expected for type `{0}''";
	private static final String UNIONEXPECTED = "Union value was expected for type `{0}''";
	private static final String NONEXISTENTCHOICE = "Reference to a non-existent alternative `{0}'' in CHOICE value for type `{1}''";
	private static final String NONEXISTENTUNION = "Reference to a non-existent field `{0}'' in union value for type `{1}''";

	private static final String NOCOMPATIBLEFIELD = "union/CHOICE type `{0}'' doesn''t have any field compatible with `{1}''";
	private static final String NOTCOMPATIBLEUNION = "union/CHOICE types are compatible only with other union/CHOICE types";

	public TTCN3_Choice_Type(final CompFieldMap compFieldMap) {
		super(compFieldMap);
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_TTCN3_CHOICE;
	}
	
	@Override
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_UNION;
	}

	@Override
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		IType temp = otherType.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp) || this == temp) {
			return true;
		}

		if (info == null || noStructuredTypeCompatibility) {
			return this == temp;
		}

		switch (temp.getTypetype()) {
		case TYPE_ASN1_CHOICE: {
			ASN1_Choice_Type tempType = (ASN1_Choice_Type) temp;
			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (lChain == null) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (rChain == null) {
				rChain = info.getChain();
				rChain.add(tempType);
			}
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				CompField cf = getComponentByIndex(i);
				IType cfType = cf.getType().getTypeRefdLast(timestamp);
				for (int j = 0, size2 = tempType.getNofComponents(timestamp); j < size2; j++) {
					CompField tempComponentField = tempType.getComponentByIndex(j);
					IType tempTypeCompFieldType = tempComponentField.getType().getTypeRefdLast(timestamp);
					if (!cf.getIdentifier().getDisplayName().equals(tempComponentField.getIdentifier().getDisplayName())) {
						continue;
					}
					lChain.markState();
					rChain.markState();
					lChain.add(cfType);
					rChain.add(tempTypeCompFieldType);
					if (cfType.equals(tempTypeCompFieldType)
						|| (lChain.hasRecursion() && rChain.hasRecursion())
						|| cfType.isCompatible(timestamp, tempTypeCompFieldType, info, lChain, rChain)) {
						info.setNeedsConversion(true);
						lChain.previousState();
						rChain.previousState();
						return true;
					}
					lChain.previousState();
					rChain.previousState();
				}
			}
			info.setErrorStr(MessageFormat.format(NOCOMPATIBLEFIELD, temp.getTypename(), getTypename()));
			return false;
		}
		case TYPE_TTCN3_CHOICE: {
			TTCN3_Choice_Type tempType = (TTCN3_Choice_Type) temp;
			if (this == tempType) {
				return true;
			}
			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (lChain == null) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (rChain == null) {
				rChain = info.getChain();
				rChain.add(tempType);
			}
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				CompField cf = getComponentByIndex(i);
				IType cfType = cf.getType().getTypeRefdLast(timestamp);
				for (int j = 0, size2 = tempType.getNofComponents(); j < size2; j++) {
					CompField tempComponentField = tempType.getComponentByIndex(j);
					IType tempTypeCompFieldType = tempComponentField.getType().getTypeRefdLast(timestamp);
					if (!cf.getIdentifier().getDisplayName().equals(tempComponentField.getIdentifier().getDisplayName())) {
						continue;
					}
					lChain.markState();
					rChain.markState();
					lChain.add(cfType);
					rChain.add(tempTypeCompFieldType);
					if (cfType.equals(tempTypeCompFieldType)
						|| (lChain.hasRecursion() && rChain.hasRecursion())
						|| cfType.isCompatible(timestamp, tempTypeCompFieldType, info, lChain, rChain)) {
						info.setNeedsConversion(true);
						lChain.previousState();
						rChain.previousState();
						return true;
					}
					lChain.previousState();
					rChain.previousState();
				}
			}
			info.setErrorStr(MessageFormat.format(NOCOMPATIBLEFIELD, temp.getTypename(), getTypename()));
			return false;
		}
		case TYPE_ASN1_SEQUENCE:
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_SEQUENCE_OF:
		case TYPE_ARRAY:
		case TYPE_ASN1_SET:
		case TYPE_TTCN3_SET:
		case TYPE_SET_OF:
		case TYPE_ANYTYPE:
			info.setErrorStr(NOTCOMPATIBLEUNION);
			return false;
		default:
			return false;
		}
	}


	@Override
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		type.check(timestamp);
		IType temp = type.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return this == temp;
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
		return getFullName();
	}

	@Override
	public String getOutlineIcon() {
		return "asn1_choice.gif";
	}

	@Override
	public void checkConstructorName(final String definitionName) {
		if (hasComponentWithName(definitionName)) {
			CompField field = getComponentByName(definitionName);
			field.getIdentifier().getLocation().reportSemanticError(MessageFormat.format(UNSUPPERTED_FIELDNAME, field.getIdentifier().getDisplayName()));
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (!referenceChain.add(this)) {
			return;
		}

		// FIXME there should be a better way than checking for all possible IReferenceChain implementation
		CachedReferenceChain cachedChain;
		if (referenceChain instanceof CachedReferenceChain) {
			cachedChain = (CachedReferenceChain) referenceChain;
		} else {
			if (!(referenceChain instanceof ReferenceChain)) {
				return;
			}
			cachedChain = ((ReferenceChain) referenceChain).toCachedReferenceChain();
		}

		Map<String, CompField> map = compFieldMap.getComponentFieldMap(timestamp);
		cachedChain.markErrorState();
		int i = 1;
		for (CompField compField : map.values()) {
			IType type = compField.getType();
			if (type != null) {
				cachedChain.markState();
				type.checkRecursions(timestamp, cachedChain);
				cachedChain.previousState();
			}
			
			if (cachedChain.getNofErrors() < i) {
				break;
			}
			++i;
		}

		if (cachedChain.getNofErrors() == map.size()) {
			cachedChain.reportAllTheErrors();
		}
		cachedChain.prevErrorState();
	}

	@Override
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		if (getIsErroneous(timestamp)) {
			return;
		}

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
		case SEQUENCE_VALUE:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(CHOICEEXPECTED, getFullName()));
				value.setIsErroneous(true);
			} else {
				last = last.setValuetype(timestamp, Value_type.CHOICE_VALUE);
				if (!last.getIsErroneous(timestamp)) {
					checkThisValueChoice(timestamp, (Choice_Value) last, valueCheckingOptions.expected_value,
							valueCheckingOptions.incomplete_allowed, valueCheckingOptions.str_elem);
				}
			}
			break;
		case CHOICE_VALUE:
			checkThisValueChoice(timestamp, (Choice_Value) last, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(CHOICEEXPECTED, getFullName()));
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(UNIONEXPECTED, getFullName()));
			}
			value.setIsErroneous(true);
		}

		if (valueCheckingOptions.sub_check) {
			//there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, last);
			}
		}
	}

	private void checkThisValueChoice(final CompilationTimeStamp timestamp, final Choice_Value value, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean strElem) {
		Identifier name = value.getName();
		if (!hasComponentWithName(name.getName())) {
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTCHOICE, name.getDisplayName(), getFullName()));
				value.setIsErroneous(true);
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTUNION, name.getDisplayName(), getFullName()));
				value.setIsErroneous(true);
			}
			return;
		}

		Type alternativeType = getComponentByName(name.getName()).getType();
		IValue alternativeValue = value.getValue();
		if (alternativeValue == null) {
			return;
		}

		alternativeValue.setMyGovernor(alternativeType);
		alternativeValue = alternativeType.checkThisValueRef(timestamp, alternativeValue);
		alternativeType.checkThisValue(timestamp, alternativeValue, new ValueCheckingOptions(expectedValue,
				incompleteAllowed, false, true, false, strElem));
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit) {
		registerUsage(template);
		template.setMyGovernor(this);

		if (getIsErroneous(timestamp)) {
			return;
		}

		if (Template_type.NAMED_TEMPLATE_LIST.equals(template.getTemplatetype())) {
			Named_Template_List namedTemplateList = (Named_Template_List) template;
			int nofTemplates = namedTemplateList.getNofTemplates();
			if (nofTemplates != 1) {
				template.getLocation().reportSemanticError(ONEFIELDEXPECTED);
			}

			for (int i = 0; i < nofTemplates; i++) {
				NamedTemplate namedTemplate = namedTemplateList.getTemplateByIndex(i);
				Identifier name = namedTemplate.getName();

				CompField field = compFieldMap.getCompWithName(name);
				if (field == null) {
					namedTemplate.getLocation().reportSemanticError(MessageFormat.format(REFERENCETONONEXISTENTFIELD, name.getDisplayName(), getFullName()));
				} else {
					Type fieldType = field.getType();
					ITTCN3Template namedTemplateTemplate = namedTemplate.getTemplate();

					namedTemplateTemplate.setMyGovernor(fieldType);
					namedTemplateTemplate = fieldType.checkThisTemplateRef(timestamp, namedTemplateTemplate);
					Completeness_type completeness = namedTemplateList.getCompletenessConditionChoice(timestamp, isModified, name);
					namedTemplateTemplate.checkThisTemplateGeneric(
							timestamp, fieldType, Completeness_type.MAY_INCOMPLETE.equals(completeness), false, false, true, implicitOmit);
				}
			}
		} else {
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(MessageFormat.format(LENGTHRESTRICTIONNOTALLOWED, getTypename()));
		}
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("union");
	}
}
