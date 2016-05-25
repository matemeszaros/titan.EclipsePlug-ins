/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Anytype_Value;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * anytype type.
 * 
 * @author Kristof Szabados
 * */
public final class Anytype_Type extends Type {
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for anytype type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for anytype type `{0}''";
	private static final String ONEFIELDEXPECTED = "A template for anytype type must contain exactly one selected field";
	private static final String ANYTYPEEXPECTED = "Anytype value was expected for type `{0}''";
	private static final String NONEXISTENTUNION = "Reference to a non-existent field `{0}'' in anytype value for type `{1}''";

	private static final String NOCOMPATIBLEFIELD = "Type anytype `{0}'' doesn''t have any field compatible with `{1}''";
	private static final String NOTCOMPATIBLEANYTYPE = "Type anytype is compatible only with other anytype types";

	private CompFieldMap compFieldMap;

	public Anytype_Type() {
		clear();
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_ANYTYPE;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		compFieldMap.setMyScope(scope);
	}

	/** @return the number of components */
	public int getNofComponents() {
		if (compFieldMap == null) {
			return 0;
		}

		return compFieldMap.fields.size();
	}

	/**
	 * Returns the element at the specified position.
	 *
	 * @param index index of the element to return
	 * @return the element at the specified position in this list
	 */
	public CompField getComponentByIndex(final int index) {
		return compFieldMap.fields.get(index);
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
		case TYPE_ANYTYPE: {
			Anytype_Type tempType = (Anytype_Type) temp;
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
			for (int i = 0; i < getNofComponents(); i++) {
				CompField cf = getComponentByIndex(i);
				IType cfType = cf.getType().getTypeRefdLast(timestamp);
				for (int j = 0; j < tempType.getNofComponents(); j++) {
					CompField tempComponentField = tempType.getComponentByIndex(j);
					IType tempTypeCompFieldType = tempComponentField.getType().getTypeRefdLast(timestamp);
					if (!cf.getIdentifier().getDisplayName().equals(tempComponentField.getIdentifier().getDisplayName())
							|| !cfType.getMyScope().getModuleScope().equals(tempTypeCompFieldType.getMyScope().getModuleScope())) {
						continue;
					}
					lChain.markState();
					rChain.markState();
					lChain.add(cfType);
					rChain.add(tempTypeCompFieldType);
					if (cfType == tempTypeCompFieldType
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
		case TYPE_ASN1_CHOICE:
		case TYPE_TTCN3_CHOICE:
			info.setErrorStr(NOTCOMPATIBLEANYTYPE);
			return false;
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
		return getFullName();
	}

	@Override
	public String getOutlineIcon() {
		return "anytype.gif";
	}

	/**
	 * Clears the fields of this anytype.
	 * <p>
	 * The fields of the anytype have to be recollected every time the module is checked,
	 * as definitions might have changed.
	 * */
	public void clear() {
		compFieldMap = new CompFieldMap();
		compFieldMap.setMyType(this);
		compFieldMap.setFullNameParent(this);
	}

	/**
	 * Adds a component to the list of components.
	 *
	 * @param field the component to be added.
	 * */
	public void addComp(final CompField field) {
		compFieldMap.addComp(field);
	}

	/**
	 * Returns whether a component with the name exists or not..
	 *
	 * @param name the name of the element to check
	 * @return true if there is an element with that name, false otherwise.
	 */
	public boolean hasComponentWithName(final String name) {
		if (compFieldMap.componentFieldMap == null) {
			return false;
		}

		return compFieldMap.componentFieldMap.containsKey(name);
	}

	/**
	 * Returns the element with the specified name.
	 *
	 * @param name the name of the element to return
	 * @return the element with the specified name in this list, or null if none was found
	 */
	public CompField getComponentByName(final String name) {
		if (compFieldMap.componentFieldMap == null) {
			return null;
		}

		return compFieldMap.componentFieldMap.get(name);
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		parseAttributes(timestamp);

		compFieldMap.check(timestamp);
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
			last = last.setValuetype(timestamp, Value_type.ANYTYPE_VALUE);
			if (!last.getIsErroneous(timestamp)) {
				checkThisValueAnytype(timestamp, (Anytype_Value) last, valueCheckingOptions.expected_value,
						valueCheckingOptions.incomplete_allowed, valueCheckingOptions.str_elem);
			}
			break;
		case ANYTYPE_VALUE:
			checkThisValueAnytype(timestamp, (Anytype_Value) last, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(MessageFormat.format(ANYTYPEEXPECTED, getFullName()));
			value.setIsErroneous(true);
		}
	}

	private void checkThisValueAnytype(final CompilationTimeStamp timestamp, final Anytype_Value value, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean strElem) {
		Identifier name = value.getName();
		if (!hasComponentWithName(name.getName())) {
			value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTUNION, name.getDisplayName(), getFullName()));
			setIsErroneous(true);
			return;
		}

		Type alternativeType = getComponentByName(name.getName()).getType();
		IValue alternativeValue = value.getValue();
		alternativeValue.setMyGovernor(alternativeType);
		alternativeValue = alternativeType.checkThisValueRef(timestamp, alternativeValue);
		alternativeType.checkThisValue(
				timestamp, alternativeValue, new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true, false, strElem));
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
				if (field != null) {
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
			Identifier id = subreference.getId();
			CompField compField = compFieldMap.getCompWithName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(), getTypename()));
				return null;
			}

			IType fieldType = compField.getType();
			if (fieldType == null) {
				return null;
			}

			Expected_Value_type internalExpectation =
					(expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE) ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : expectedIndex;

			return fieldType.getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain, false);
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),	getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	public boolean getSubrefsAsArray(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final List<Integer> subrefsArray, final List<IType> typeArray) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return true;
		}
		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDSUBREFERENCE, getTypename()));
			return false;
		case fieldSubReference:
			Identifier id = subreference.getId();
			CompField compField = compFieldMap.getCompWithName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(), getTypename()));
				return false;
			}

			int fieldIndex = compFieldMap.fields.indexOf(compField);
			IType fieldType = compField.getType();
			if (fieldType == null) {
				return false;
			}
			subrefsArray.add(fieldIndex);
			typeArray.add(this);
			return fieldType.getSubrefsAsArray(timestamp, reference, actualSubReference + 1, subrefsArray, typeArray);
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),	getTypename()));
			return false;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return false;
		}
	}

	@Override
	public boolean getFieldTypesAsArray(final Reference reference, final int actualSubReference, final List<IType> typeArray) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return true;
		}
		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			return false;
		case fieldSubReference:
			Identifier id = subreference.getId();
			if (compFieldMap == null) {
				return false;
			}
			CompField compField = compFieldMap.getCompWithName(id);
			if (compField == null) {
				return false;
			}
			IType fieldType = compField.getType();
			if (fieldType == null) {
				return false;
			}
			typeArray.add(this);
			return fieldType.getFieldTypesAsArray(reference, actualSubReference + 1, typeArray);
		case parameterisedSubReference:
			return false;
		default:
			return false;
		}
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("anytype");
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * In case of structural types, the member fields are checked if they could
	 * complete the proposal.
	 *
	 * @param propCollector the proposal collector to add the proposal to, and
	 *            used to get more information
	 * @param i index, used to identify which element of the reference (used by
	 *            the proposal collector) should be checked for completions.
	 * */
	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subreferences = propCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on
				CompField compField = compFieldMap.getCompWithName(subreference.getId());
				if (compField == null) {
					return;
				}
				IType type = compField.getType();
				if (type != null) {
					type.addProposal(propCollector, i + 1);
				}
			} else {
				// final part of the reference
				List<CompField> compFields = compFieldMap.getComponentsWithPrefixCaseInsensitive(subreference.getId().getName());
				for (CompField compField : compFields) {
					String proposalKind = compField.getType().getProposalDescription(new StringBuilder()).toString();
					propCollector.addProposal(compField.getIdentifier(), " - " + proposalKind, ImageCache.getImage(getOutlineIcon()), proposalKind);
				}
			}
		}
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * In case of structural types, the member fields are checked if they could
	 * be the declaration searched for.
	 *
	 * @param declarationCollector the declaration collector to add the
	 *            declaration to, and used to get more information.
	 * @param i index, used to identify which element of the reference (used by
	 *            the declaration collector) should be checked.
	 * */
	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on
				CompField compField = compFieldMap.getCompWithName(subreference.getId());
				if (compField == null) {
					return;
				}
				IType type = compField.getType();
				if (type != null) {
					type.addDeclaration(declarationCollector, i + 1);
				}
			} else {
				// final part of the reference
				List<CompField> compFields = compFieldMap.getComponentsWithPrefixCaseInsensitive(subreference.getId().getName());
				for (CompField compField : compFields) {
					declarationCollector.addDeclaration(compField.getIdentifier().getDisplayName(), compField.getIdentifier().getLocation(), this);
				}
			}
		}
	}

	@Override
	public void getEnclosingField(final int offset, final ReferenceFinder rf) {
		compFieldMap.getEnclosingField(offset, rf);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (compFieldMap!=null && !compFieldMap.accept(v)) {
			return false;
		}
		return true;
	}
}
