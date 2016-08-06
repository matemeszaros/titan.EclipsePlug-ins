/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

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
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.TableConstraint;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_Definition;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.CompFieldMap;
import org.eclipse.titan.designer.AST.TTCN3.values.Choice_Value;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a hole type.
 *
 * Please note, that all instances of Open_Type type are always erroneous to
 * stop some error messages, which are produced as we are not yet able to fully
 * handle the constraints.
 * 
 * @author Kristof Szabados
 * */
public final class Open_Type extends ASN1Type {
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for union type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for union type `{0}''";
	private static final String ONEFIELDEXPECTED = "A template for union type must contain exactly one selected field";
	private static final String REFERENCETONONEXISTENTFIELD = "Reference to non-existent field `{0}'' in union template for type `{1}''";
	private static final String CHOICEEXPECTED = "CHOICE value was expected for type `{0}''";
	private static final String UNIONEXPECTED = "Union value was expected for type `{0}''";
	private static final String NONEXISTENTCHOICE = "Reference to a non-existent alternative `{0}'' in CHOICE value for type `{1}''";
	private static final String NONEXISTENTUNION = "Reference to a non-existent field `{0}'' in union value for type `{1}''";

	private CompFieldMap compFieldMap;
	private final ObjectClass_Definition objectClass;
	private final Identifier fieldName;

	private TableConstraint myTableConstraint;

	public Open_Type(final ObjectClass_Definition objectClass, final Identifier identifier) {
		compFieldMap = new CompFieldMap();
		this.objectClass = objectClass;
		fieldName = identifier;

		compFieldMap.setMyType(this);
		compFieldMap.setFullNameParent(this);
		if (null != objectClass) {
			objectClass.setFullNameParent(this);
		}

		isErroneous = true;
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_OPENTYPE;
	}

	@Override
	public IASN1Type newInstance() {
		return new Open_Type(objectClass, fieldName);
	}

	@Override
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_TTCN3_CHOICE;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != compFieldMap) {
			compFieldMap.setMyScope(scope);
		}
	}

	public ObjectClass_Definition getMyObjectClass() {
		return objectClass;
	}

	public Identifier getObjectClassFieldName() {
		return fieldName;
	}

	public void setMyTableConstraint(final TableConstraint constraint) {
		myTableConstraint = constraint;
	}

	public void addComponent(final CompField field) {
		if (null != field && null != compFieldMap) {
			compFieldMap.addComp(field);
		}
	}

	public boolean hasComponentWithName(final Identifier identifier) {
		if (null != compFieldMap && null != compFieldMap.getCompWithName(identifier)) {
			return true;
		}

		return false;
	}

	public CompField getComponentByName(final Identifier identifier) {
		if (null != compFieldMap) {
			return compFieldMap.getCompWithName(identifier);
		}
		return null;
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

		return this == otherType;
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
	public String getTypename() {
		return "open type";
	}

	@Override
	public String getFullName() {
		if (null == getNameParent()) {
			return getTypename();
		}

		return super.getFullName();
	}

	@Override
	public String getOutlineIcon() {
		return "asn1_opentype.gif";
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		Map<String, CompField> map = compFieldMap.getComponentFieldMap(timestamp);

		if (referenceChain.add(this) && 1 == map.size()) {
			for (CompField compField : map.values()) {
				IType type = compField.getType();
				if (null != type) {
					referenceChain.markState();
					type.checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	public void clear() {
		lastTimeChecked = null;
		compFieldMap = new CompFieldMap();
		compFieldMap.setMyType(this);
		compFieldMap.setFullNameParent(this);
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		// isErroneous = false;
		isErroneous = true;

		compFieldMap.check(timestamp);

		if (null != constraints) {
			constraints.check(timestamp);
		}
	}

	// FIXME add tests
	@Override
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		if (getIsErroneous(timestamp)) {
			return;
		}

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
		default:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(CHOICEEXPECTED, getFullName()));
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(UNIONEXPECTED, getFullName()));
			}
			value.setIsErroneous(true);
		}

		value.setLastTimeChecked(timestamp);
	}

	private void checkThisValueChoice(final CompilationTimeStamp timestamp, final Choice_Value value, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean strElem) {
		Identifier name = value.getName();
		if (!hasComponentWithName(name)) {
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(
						MessageFormat.format(NONEXISTENTCHOICE, name.getDisplayName(), getFullName()));
				setIsErroneous(true);
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTUNION, name.getDisplayName(), getFullName()));
				setIsErroneous(true);
			}
		}

		CompField field = getComponentByName(name);
		if (null != field) {
			Type alternativeType = field.getType();
			IValue alternativeValue = value.getValue();
			if (null == alternativeValue) {
				return;
			}

			alternativeValue.setMyGovernor(alternativeType);
			alternativeValue = alternativeType.checkThisValueRef(timestamp, alternativeValue);
			alternativeType.checkThisValue(timestamp, alternativeValue, new ValueCheckingOptions(expectedValue, incompleteAllowed,
					false, true, false, strElem));
		}

		value.setLastTimeChecked(timestamp);
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit) {
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
					// named_template.getLocation().reportSemanticError(MessageFormat.format(REFERENCETONONEXISTENTFIELD,
					// name.get_displayName(),
					// getFullName()));
				} else {
					Type fieldType = field.getType();
					if (fieldType != null && !fieldType.getIsErroneous(timestamp)) {
						ITTCN3Template namedTemplateTemplate = namedTemplate.getTemplate();

						namedTemplateTemplate.setMyGovernor(fieldType);
						namedTemplateTemplate = fieldType.checkThisTemplateRef(timestamp, namedTemplateTemplate);
						Completeness_type completeness = namedTemplateList.getCompletenessConditionChoice(timestamp,
								isModified, name);
						namedTemplateTemplate.checkThisTemplateGeneric(timestamp, fieldType,
								Completeness_type.MAY_INCOMPLETE.equals(completeness), false, false, true,
								implicitOmit);
					}
				}
			}
		} else {
			template.getLocation().reportSemanticError(
					MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
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
				// reference.getLocation().reportSemanticError(MessageFormat.format(TTCN3_Set_Seq_Choice_BaseType.NONEXISTENTFIELDREFERENCE,
				// id.get_displayName(), getFullName()));
				reference.setIsErroneous(true);
				return this;
			}

			Expected_Value_type internalExpectation = (expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE) ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE
					: expectedIndex;

			if (interruptIfOptional && compField.isOptional()) {
				return null;
			}
			return compField.getType().getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain,
					interruptIfOptional);
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
		return builder.append("open type");
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * In case of structural types, the member fields are checked if they
	 * could complete the proposal.
	 *
	 * @param propCollector
	 *                the proposal collector to add the proposal to, and
	 *                used to get more information
	 * @param i
	 *                index, used to identify which element of the reference
	 *                (used by the proposal collector) should be checked for
	 *                completions.
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
				List<CompField> compFields = compFieldMap.getComponentsWithPrefix(subreference.getId().getName());
				for (CompField compField : compFields) {
					String proposalKind = compField.getType().getProposalDescription(new StringBuilder()).toString();
					propCollector.addProposal(compField.getIdentifier(), " - " + proposalKind,
							ImageCache.getImage(compField.getOutlineIcon()), proposalKind);
				}
			}
		}
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * In case of structural types, the member fields are checked if they
	 * could be the declaration searched for.
	 *
	 * @param declarationCollector
	 *                the declaration collector to add the declaration to,
	 *                and used to get more information.
	 * @param i
	 *                index, used to identify which element of the reference
	 *                (used by the declaration collector) should be checked.
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
				List<CompField> compFields = compFieldMap.getComponentsWithPrefix(subreference.getId().getName());
				for (CompField compField : compFields) {
					declarationCollector.addDeclaration(compField.getIdentifier().getDisplayName(), compField.getIdentifier()
							.getLocation(), this);
				}
			}
		}
	}

	@Override
	public void getEnclosingField(final int offset, final ReferenceFinder rf) {
		if (compFieldMap == null) {
			return;
		}

		compFieldMap.getEnclosingField(offset, rf);
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (compFieldMap != null) {
			compFieldMap.findReferences(referenceFinder, foundIdentifiers);
		}
		if (objectClass != null) {
			// TODO
		}
		if (myTableConstraint != null) {
			// TODO
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (compFieldMap != null && !compFieldMap.accept(v)) {
			return false;
		}
		if (objectClass != null && !objectClass.accept(v)) {
			return false;
		}
		if (fieldName != null && !fieldName.accept(v)) {
			return false;
		}
		if (myTableConstraint != null && !myTableConstraint.accept(v)) {
			return false;
		}
		return true;
	}
}
