/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.ASN1.Value_Assignment;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The Type class is the base class for types.
 * 
 * @author Kristof Szabados
 * */
public abstract class Type extends Governor implements IType, IIncrementallyUpdateable, IOutlineElement {
	private static final String INCOMPATIBLEVALUE = "Incompatible value: `{0}'' was expected";

	private static final String TYPECOMPATWARNING = "Type compatibility between `{0}'' and `{1}''";

	/** the parent type of this type */
	private IType parentType;

	/** The constraints assigned to this type. */
	protected Constraints constraints = null;

	/** The with attributes assigned to the definition of this type */
	// TODO as this is only used for TTCN-3 types maybe we could save some
	// memory, by moving it ... but than we waste runtime.
	protected WithAttributesPath withAttributesPath = null;
	private boolean hasDone = false;

	/** The list of parsed sub-type restrictions before they are converted */
	protected List<ParsedSubType> parsedRestrictions = null;

	/** The sub-type restriction created from the parsed restrictions */
	protected SubType subType = null;

	/**
	 * The actual value of the severity level to report type compatibility
	 * on.
	 */
	private static String typeCompatibilitySeverity;
	/**
	 * if typeCompatibilitySeverity is set to Error this is true, in this
	 * case structured types must be nominally compatible
	 */
	protected static boolean noStructuredTypeCompatibility;

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			typeCompatibilitySeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORTTYPECOMPATIBILITY, GeneralConstants.WARNING, null);
			noStructuredTypeCompatibility = GeneralConstants.ERROR.equals(typeCompatibilitySeverity);

			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.REPORTTYPECOMPATIBILITY.equals(property)) {
							typeCompatibilitySeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORTTYPECOMPATIBILITY, GeneralConstants.WARNING, null);
							noStructuredTypeCompatibility = GeneralConstants.ERROR.equals(typeCompatibilitySeverity);
						}
					}
				});
			}
		}
	}

	public Type() {
		parentType = null;
	}

	@Override
	public Setting_type getSettingtype() {
		return Setting_type.S_T;
	}

	@Override
	public abstract Type_type getTypetype();

	@Override
	public final IType getParentType() {
		return parentType;
	}

	@Override
	public final void setParentType(final IType type) {
		parentType = type;
	}

	@Override
	public WithAttributesPath getAttributePath() {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}

		return withAttributesPath;
	}

	@Override
	public void setAttributeParentPath(final WithAttributesPath parent) {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}

		withAttributesPath.setAttributeParent(parent);
	}

	@Override
	public void clearWithAttributes() {
		if (withAttributesPath != null) {
			withAttributesPath.setWithAttributes(null);
		}
	}

	@Override
	public void setWithAttributes(final MultipleWithAttributes attributes) {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}

		withAttributesPath.setWithAttributes(attributes);
	}

	@Override
	public boolean hasDoneAttribute() {
		return hasDone;
	}

	@Override
	public final boolean isConstrained() {
		return constraints != null;
	}

	@Override
	public final void addConstraints(final Constraints constraints) {
		if (constraints == null) {
			return;
		}

		this.constraints = constraints;
		constraints.setMyType(this);
	}

	@Override
	public final Constraints getConstraints() {
		return constraints;
	}

	@Override
	public SubType getSubtype() {
		return subType;
	}

	@Override
	public final void setParsedRestrictions(final List<ParsedSubType> parsedRestrictions) {
		this.parsedRestrictions = parsedRestrictions;
	}

	@Override
	public String chainedDescription() {
		return "type reference: " + getFullName();
	}

	@Override
	public final Location getChainLocation() {
		return getLocation();
	}

	@Override
	public IType getTypeRefdLast(final CompilationTimeStamp timestamp) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IType result = getTypeRefdLast(timestamp, referenceChain);
		referenceChain.release();

		return result;
	}

	/**
	 * Returns the type referred last in case of a referred type, or itself
	 * in any other case.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references
	 * 
	 * @return the actual or the last referred type
	 * */
	protected IType getTypeRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		return this;
	}

	@Override
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final boolean interruptIfOptional) {
		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IType temp = getFieldType(timestamp, reference, actualSubReference, expectedIndex, chain, interruptIfOptional);
		chain.release();

		return temp;
	}

	@Override
	public abstract IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional);

	@Override
	public boolean getSubrefsAsArray(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final List<Integer> subrefsArray, final List<IType> typeArray) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return true;
		}
		ErrorReporter.INTERNAL_ERROR("Type " + getTypename() + " has no fields.");
		return false;
	}

	@Override
	public boolean getFieldTypesAsArray(final Reference reference, final int actualSubReference, final List<IType> typeArray) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasVariantAttributes(final CompilationTimeStamp timestamp) {
		if (withAttributesPath == null) {
			return false;
		}

		List<SingleWithAttribute> realAttributes = withAttributesPath.getRealAttributes(timestamp);
		for (int i = 0; i < realAttributes.size(); i++) {
			if (SingleWithAttribute.Attribute_Type.Variant_Attribute.equals(realAttributes.get(i).getAttributeType())) {
				return true;
			}
		}

		MultipleWithAttributes localAttributes = withAttributesPath.getAttributes();
		if (localAttributes == null) {
			return false;
		}

		for (int i = 0; i < localAttributes.getNofElements(); i++) {
			SingleWithAttribute tempSingle = localAttributes.getAttribute(i);
			if (Attribute_Type.Variant_Attribute.equals(tempSingle.getAttributeType())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void checkDoneAttribute(final CompilationTimeStamp timestamp) {
		hasDone = false;

		if (withAttributesPath == null) {
			return;
		}

		List<SingleWithAttribute> realAttributes = withAttributesPath.getRealAttributes(timestamp);
		for (int i = 0, size = realAttributes.size(); i < size; i++) {
			SingleWithAttribute singleAttribute = realAttributes.get(i);
			if (Attribute_Type.Extension_Attribute.equals(singleAttribute.getAttributeType())
					&& "done".equals(singleAttribute.getAttributeSpecification().getSpecification())) {
				hasDone = true;
			}
		}
	}

	@Override
	public void parseAttributes(final CompilationTimeStamp timestamp) {
		checkDoneAttribute(timestamp);
		// FIXME To support the processing of variant attributes this
		// needs to be implemented properly.
	}

	/**
	 * Does the semantic checking of the type.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * */
	// FIXME could be made abstract
	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void checkConstructorName(final String definitionName) {
		// nothing to be done by default
	}

	@Override
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_NONE;
	}

	protected void checkSubtypeRestrictions(final CompilationTimeStamp timestamp) {
		checkSubtypeRestrictions(timestamp, getSubtypeType(), null);
	}

	/** create and check subtype, called by the check function of the type */
	protected void checkSubtypeRestrictions(final CompilationTimeStamp timestamp, final SubType.SubType_type subtypeType,
			final SubType parentSubtype) {

		if (getIsErroneous(timestamp)) {
			return;
		}

		// if there is no own or parent sub-type then there's nothing to
		// do
		if ((parsedRestrictions == null) && (parentSubtype == null)) {
			return;
		}

		// if the type has no subtype type
		if (subtypeType == SubType.SubType_type.ST_NONE) {
			getLocation().reportSemanticError(
					MessageFormat.format("TTCN-3 subtype constraints are not applicable to type `{0}''", getTypename()));
			return;
		}

		subType = new SubType(subtypeType, this, parsedRestrictions, parentSubtype);

		subType.check(timestamp);
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
	}

	@Override
	public boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		return false;
	}

	@Override
	public void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		// nothing to be done by default
	}

	@Override
	public void checkEmbedded(final CompilationTimeStamp timestamp, final Location errorLocation, final boolean defaultAllowed,
			final String errorMessage) {
		// nothing to be done by default
	}

	@Override
	public IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value) {
		if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(value.getValuetype())) {
			return value.setValuetype(timestamp, Value_type.REFERENCED_VALUE);
		}

		return value;
	}

	@Override
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		value.setIsErroneous(false);

		final Assignment assignment = getDefiningAssignment();
		if (assignment != null && assignment instanceof Definition) {
			final Scope scope = value.getMyScope();
			if (scope != null) {
				final Module module = scope.getModuleScope();
				if (module != null) {
					final String referingModuleName = module.getName();
					if (!((Definition)assignment).referingHere.contains(referingModuleName)) {
						((Definition)assignment).referingHere.add(referingModuleName);
					}
				} else {
					ErrorReporter.logError("The value `" + value.getFullName() + "' does not appear to be in a module");
				}
			} else {
				ErrorReporter.logError("The value `" + value.getFullName() + "' does not appear to be in a scope");
			}
		}

		check(timestamp);
		final IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
		if (last == null || last.getIsErroneous(timestamp) || getIsErroneous(timestamp)) {
			return;
		}

		if (Value_type.OMIT_VALUE.equals(last.getValuetype()) && !valueCheckingOptions.omit_allowed) {
			value.getLocation().reportSemanticError("`omit' value is not allowed in this context");
			value.setIsErroneous(true);
			return;
		}

		switch (value.getValuetype()) {
		case UNDEFINED_LOWERIDENTIFIER_VALUE:
			if (Value_type.REFERENCED_VALUE.equals(last.getValuetype())) {
				IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				checkThisReferencedValue(timestamp, last, valueCheckingOptions.expected_value, chain, valueCheckingOptions.sub_check,
						valueCheckingOptions.str_elem);
				chain.release();
			}
			return;
		case REFERENCED_VALUE: {
			IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			checkThisReferencedValue(timestamp, value, valueCheckingOptions.expected_value, chain, valueCheckingOptions.sub_check,
					valueCheckingOptions.str_elem);
			chain.release();
			return;
		}
		case EXPRESSION_VALUE:
			if (value.isUnfoldable(timestamp, null)) {
				Type_type temporalType = value.getExpressionReturntype(timestamp, valueCheckingOptions.expected_value);
				if (!Type_type.TYPE_UNDEFINED.equals(temporalType)
						&& !isCompatible(timestamp, this.getTypetype(), temporalType, false, value.isAsn())) {
					value.getLocation().reportSemanticError(MessageFormat.format(INCOMPATIBLEVALUE, getTypename()));
					value.setIsErroneous(true);
				}
				return;
			}
			break;
		case MACRO_VALUE:
			if (value.isUnfoldable(timestamp, null)) {
				Type_type temporalType = value.getExpressionReturntype(timestamp, valueCheckingOptions.expected_value);
				if (!Type_type.TYPE_UNDEFINED.equals(temporalType)
						&& !isCompatible(timestamp, this.getTypetype(), temporalType, false, value.isAsn())) {
					value.getLocation().reportSemanticError(MessageFormat.format(INCOMPATIBLEVALUE, getTypename()));
					value.setIsErroneous(true);
				}
				return;
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Checks the provided referenced value.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param value
	 *                the referenced value to be checked.
	 * @param expectedValue
	 *                the expectations we have for the value.
	 * @param referenceChain
	 *                the reference chain to detect circular references.
	 * @param strElem
	 *                true if the value to be checked is an element of a
	 *                string
	 * */
	private void checkThisReferencedValue(final CompilationTimeStamp timestamp, final IValue value, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain, final boolean subCheck, final boolean strElem) {
		final Reference reference = ((Referenced_Value) value).getReference();
		final Assignment assignment = reference.getRefdAssignment(timestamp, true);

		if (assignment == null) {
			value.setIsErroneous(true);
			return;
		}

		final Assignment myAssignment = getDefiningAssignment();
		if (myAssignment != null && myAssignment instanceof Definition) {
			String referingModuleName = value.getMyScope().getModuleScope().getName();
			if (!((Definition)myAssignment).referingHere.contains(referingModuleName)) {
				((Definition)myAssignment).referingHere.add(referingModuleName);
			}
		}

		assignment.check(timestamp);
		boolean isConst = false;
		boolean errorFlag = false;
		boolean checkRunsOn = false;
		IType governor = null;
		if (assignment.getIsErroneous()) {
			value.setIsErroneous(true);
		} else {
			switch (assignment.getAssignmentType()) {
			case A_CONST:
				isConst = true;
				break;
			case A_OBJECT:
			case A_OS:
				ISetting setting = reference.getRefdSetting(timestamp);
				if (setting == null || setting.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return;
				}

				if (!Setting_type.S_V.equals(setting.getSettingtype())) {
					reference.getLocation().reportSemanticError(
							MessageFormat.format("This InformationFromObjects construct does not refer to a value: {0}",
									value.getFullName()));
					value.setIsErroneous(true);
					return;
				}

				governor = ((Value) setting).getMyGovernor();
				if (governor != null) {
					isConst = true;
				}
				break;
			case A_EXT_CONST:
			case A_MODULEPAR:
				if (Expected_Value_type.EXPECTED_CONSTANT.equals(expectedValue)) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(
									"Reference to an (evaluatable) constant value was expected instead of {0}",
									assignment.getDescription()));
					errorFlag = true;
				}
				break;
			case A_VAR:
			case A_PAR_VAL:
			case A_PAR_VAL_IN:
			case A_PAR_VAL_OUT:
			case A_PAR_VAL_INOUT:
				switch (expectedValue) {
				case EXPECTED_CONSTANT:
					value.getLocation().reportSemanticError(
							MessageFormat.format("Reference to a constant value was expected instead of {0}",
									assignment.getDescription()));
					errorFlag = true;
					break;
				case EXPECTED_STATIC_VALUE:
					value.getLocation().reportSemanticError(
							MessageFormat.format("Reference to a static value was expected instead of {0}",
									assignment.getDescription()));
					errorFlag = true;
					break;
				default:
					break;
				}
				break;
			case A_TEMPLATE:
			case A_MODULEPAR_TEMPLATE:
			case A_VAR_TEMPLATE:
			case A_PAR_TEMP_IN:
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				if (!Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue) 
						&& (!Expected_Value_type.EXPECTED_DYNAMIC_VALUE.equals(expectedValue))) {
					value.getLocation().reportSemanticError(
							MessageFormat.format("Reference to a value was expected instead of {0}",
									assignment.getDescription()));
					errorFlag = true;
				}
				break;
			case A_FUNCTION_RVAL:
				checkRunsOn = true;
				switch (expectedValue) {
				case EXPECTED_CONSTANT: {
					final String message = MessageFormat.format(
							"Reference to a constant value was expected instead of the return value of {0}",
							assignment.getDescription());
					value.getLocation().reportSemanticError(message);
					errorFlag = true;
				}
				break;
				case EXPECTED_STATIC_VALUE: {
					final String message = MessageFormat.format(
							"Reference to a static value was expected instead of the return value of {0}",
							assignment.getDescription());
					value.getLocation().reportSemanticError(message);
					errorFlag = true;
				}
				break;
				default:
					break;
				}
				break;
			case A_EXT_FUNCTION_RVAL:
				switch (expectedValue) {
				case EXPECTED_CONSTANT: {
					final String message = MessageFormat.format(
							"Reference to a constant value was expected instead of the return value of {0}",
							assignment.getDescription());
					value.getLocation().reportSemanticError(message);
					errorFlag = true;
				}
				break;
				case EXPECTED_STATIC_VALUE: {
					final String message = MessageFormat.format(
							"Reference to a static value was expected instead of the return value of {0}",
							assignment.getDescription());
					value.getLocation().reportSemanticError(message);
					errorFlag = true;
				}
				break;
				default:
					break;
				}
				break;
			case A_FUNCTION_RTEMP:
				checkRunsOn = true;
				if (!Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue)) {
					value.getLocation()
					.reportSemanticError(
							MessageFormat.format(
									"Reference to a value was expected instead of a call of {0}, which return a template",
									assignment.getDescription()));
					errorFlag = true;
				}
				break;
			case A_EXT_FUNCTION_RTEMP:
				if (!Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue)) {
					value.getLocation()
					.reportSemanticError(
							MessageFormat.format(
									"Reference to a value was expected instead of a call of {0}, which return a template",
									assignment.getDescription()));
					errorFlag = true;
				}
				break;
			case A_FUNCTION:
			case A_EXT_FUNCTION:
				value.getLocation()
				.reportSemanticError(
						MessageFormat.format(
								"Reference to a {0} was expected instead of a call of {1}, which does not have a return type",
								Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue) ? "value or template"
										: "value", assignment.getDescription()));
				value.setIsErroneous(true);
				return;
			default:
				value.getLocation().reportSemanticError(
						MessageFormat.format("Reference to a {0} was expected instead of {1}",
								Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue) ? "value or template"
										: "value", assignment.getDescription()));
				value.setIsErroneous(true);
				return;
			}
		}

		if (checkRunsOn) {
			reference.getMyScope().checkRunsOnScope(timestamp, assignment, reference, "call");
		}
		if (governor == null) {
			final IType type = assignment.getType(timestamp);
			if (type != null) {
				governor = type.getFieldType(timestamp, reference, 1, expectedValue, referenceChain, false);
			}
		}
		if (governor == null) {
			value.setIsErroneous(true);
			return;
		}
		
		final TypeCompatibilityInfo info = new TypeCompatibilityInfo(this, governor, true);
		info.setStr1Elem(strElem);
		info.setStr2Elem(reference.refersToStringElement());
		CompatibilityLevel compatibilityLevel = getCompatibility(timestamp, governor, info, null, null);
		if (compatibilityLevel != CompatibilityLevel.COMPATIBLE) {
			// Port or signature values do not exist at all. These
			// errors are already
			// reported at those definitions. Extra errors should
			// not be reported
			// here.
			final IType type = getTypeRefdLast(timestamp, null);
			switch (type.getTypetype()) {
			case TYPE_PORT:
				// neither port values nor templates exist
				break;
			case TYPE_SIGNATURE:
				if (Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue)) {
					final String message = MessageFormat.format(
							"Type mismatch: a signature template of type `{0}'' was expected instead of `{1}''",
							getTypename(), governor.getTypename());
					value.getLocation().reportSemanticError(message);
				}
				break;
			case TYPE_SEQUENCE_OF:
			case TYPE_ASN1_SEQUENCE:
			case TYPE_TTCN3_SEQUENCE:
			case TYPE_ARRAY:
			case TYPE_ASN1_SET:
			case TYPE_TTCN3_SET:
			case TYPE_SET_OF:
			case TYPE_ASN1_CHOICE:
			case TYPE_TTCN3_CHOICE:
			case TYPE_ANYTYPE:
				if (compatibilityLevel == CompatibilityLevel.INCOMPATIBLE_SUBTYPE) {
					value.getLocation().reportSemanticError(info.getSubtypeError());
				} else {
					value.getLocation().reportSemanticError(info.toString());
				}
				break;
			default:
				if (compatibilityLevel == CompatibilityLevel.INCOMPATIBLE_SUBTYPE) {
					value.getLocation().reportSemanticError(info.getSubtypeError());
				} else {
					final String message = MessageFormat.format(
							"Type mismatch: a {0} of type `{1}'' was expected instead of `{2}''",
							Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue) ? "value or template" : "value",
									getTypename(), governor.getTypename());
					value.getLocation().reportSemanticError(message);
				}
				break;
			}
			errorFlag = true;
		} else {
			if (GeneralConstants.WARNING.equals(typeCompatibilitySeverity)) {
				if (info.getNeedsConversion()) {
					value.getLocation().reportSemanticWarning(
							MessageFormat.format(TYPECOMPATWARNING, this.getTypename(), governor.getTypename()));
				}
			}
		}

		if (errorFlag) {
			value.setIsErroneous(true);
			return;
		}

		// checking for circular references
		IValue last = value.getValueRefdLast(timestamp, expectedValue, referenceChain);
		if (isConst && !last.getIsErroneous(timestamp)) {
			if (subCheck && (subType != null)) {
				subType.checkThisValue(timestamp, value);
			}
		}
	}

	@Override
	public ITTCN3Template checkThisTemplateRef(final CompilationTimeStamp timestamp, final ITTCN3Template t) {
		if (!Template_type.SPECIFIC_VALUE.equals(t.getTemplatetype())) {
			return t;
		}

		ITTCN3Template template = t;

		if( template instanceof TemplateBody) {
			template = ((TemplateBody) template).getTemplate();
		}

		IValue value = ((SpecificValue_Template) template).getSpecificValue();
		if (value == null) {
			return template;
		}

		value = checkThisValueRef(timestamp, value);

		switch (value.getValuetype()) {
		case REFERENCED_VALUE:
			Assignment assignment = ((Referenced_Value) value).getReference().getRefdAssignment(timestamp, false);
			if (assignment == null) {
				template.setIsErroneous(true);
			} else {
				switch (assignment.getAssignmentType()) {
				case A_VAR_TEMPLATE:
					IType type = ((Def_Var_Template) assignment).getType(timestamp);

					switch (type.getTypetype()) {
					case TYPE_BITSTRING:
					case TYPE_BITSTRING_A:
					case TYPE_HEXSTRING:
					case TYPE_OCTETSTRING:
					case TYPE_CHARSTRING:
					case TYPE_UCHARSTRING:
					case TYPE_UTF8STRING:
					case TYPE_NUMERICSTRING:
					case TYPE_PRINTABLESTRING:
					case TYPE_TELETEXSTRING:
					case TYPE_VIDEOTEXSTRING:
					case TYPE_IA5STRING:
					case TYPE_GRAPHICSTRING:
					case TYPE_VISIBLESTRING:
					case TYPE_GENERALSTRING:
					case TYPE_UNIVERSALSTRING:
					case TYPE_BMPSTRING:
					case TYPE_UTCTIME:
					case TYPE_GENERALIZEDTIME:
					case TYPE_OBJECTDESCRIPTOR: {
						List<ISubReference> subReferences = ((Referenced_Value) value).getReference().getSubreferences();
						int nofSubreferences = subReferences.size();
						if (nofSubreferences > 1) {
							ISubReference subreference = subReferences.get(nofSubreferences - 1);
							if (subreference instanceof ArraySubReference) {
								template.getLocation().reportSemanticError(
										MessageFormat.format("Reference to {0} can not be indexed",
												assignment.getDescription()));
								template.setIsErroneous(true);
								return template;
							}
						}
						break;
					}
					default:
						break;
					}
					return template.setTemplatetype(timestamp, Template_type.TEMPLATE_REFD);
				case A_CONST:
					IType type1;
					if( assignment instanceof Value_Assignment){						
						type1 = ((Value_Assignment) assignment).getType(timestamp);
					} else {
						type1 = ((Def_Const) assignment).getType(timestamp);
					}
					switch (type1.getTypetype()) {
					case TYPE_BITSTRING:
					case TYPE_BITSTRING_A:
					case TYPE_HEXSTRING:
					case TYPE_OCTETSTRING:
					case TYPE_CHARSTRING:
					case TYPE_UCHARSTRING:
					case TYPE_UTF8STRING:
					case TYPE_NUMERICSTRING:
					case TYPE_PRINTABLESTRING:
					case TYPE_TELETEXSTRING:
					case TYPE_VIDEOTEXSTRING:
					case TYPE_IA5STRING:
					case TYPE_GRAPHICSTRING:
					case TYPE_VISIBLESTRING:
					case TYPE_GENERALSTRING:
					case TYPE_UNIVERSALSTRING:
					case TYPE_BMPSTRING:
					case TYPE_UTCTIME:
					case TYPE_GENERALIZEDTIME:
					case TYPE_OBJECTDESCRIPTOR: {
						List<ISubReference> subReferences = ((Referenced_Value) value).getReference().getSubreferences();
						int nofSubreferences = subReferences.size();
						if (nofSubreferences > 1) {
							ISubReference subreference = subReferences.get(nofSubreferences - 1);
							if (subreference instanceof ArraySubReference) {
								template.getLocation().reportSemanticError(
										MessageFormat.format("Reference to {0} can not be indexed",
												assignment.getDescription()));
								template.setIsErroneous(true);
								return template;
							}
						}
						break;
					}
					default:
						break;
					}
					break;
				case A_TEMPLATE:
				case A_MODULEPAR_TEMPLATE:
				case A_PAR_TEMP_IN:
				case A_PAR_TEMP_OUT:
				case A_PAR_TEMP_INOUT:
				case A_FUNCTION_RTEMP:
				case A_EXT_FUNCTION_RTEMP:
					return template.setTemplatetype(timestamp, Template_type.TEMPLATE_REFD);
				default:
					break;
				}
			}
			break;
		case EXPRESSION_VALUE: {
			Expression_Value expression = (Expression_Value) value;
			if (Operation_type.APPLY_OPERATION.equals(expression.getOperationType())) {
				IType type = expression.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
				if (type == null) {
					break;
				}

				type = type.getTypeRefdLast(timestamp);
				if (type != null && Type_type.TYPE_FUNCTION.equals(type.getTypetype()) && ((Function_Type) type).returnsTemplate()) {
					return template.setTemplatetype(timestamp, Template_type.TEMPLATE_INVOKE);
				}
			}
			break;
		}
		default:
			break;
		}

		return template;
	}

	/**
	 * Register the usage of this type in the provided template.
	 * 
	 * @param template
	 *                the template to use.
	 * */
	protected void registerUsage(final ITTCN3Template template) {
		final Assignment assignment = getDefiningAssignment();
		if (assignment != null && assignment instanceof Definition) {
			String referingModuleName = template.getMyScope().getModuleScope().getName();
			if (!((Definition)assignment).referingHere.contains(referingModuleName)) {
				((Definition)assignment).referingHere.add(referingModuleName);
			}
		}
	}

	@Override
	public abstract void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit);

	@Override
	public final void checkThisTemplateSubtype(final CompilationTimeStamp timestamp, final ITTCN3Template template) {
		if (ITTCN3Template.Template_type.PERMUTATION_MATCH.equals(template.getTemplatetype())) {
			// a permutation is just a fragment, in itself it has no type
			return;
		}

		if (subType != null) {
			subType.checkThisTemplateGeneric(timestamp, template);
		}
	}

	@Override
	public abstract boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain);
	
	@Override
	public boolean isStronglyCompatible(final CompilationTimeStamp timestamp, final IType otherType, TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {

		check(timestamp);
		otherType.check(timestamp);
		final IType thisTypeLast = this.getTypeRefdLast(timestamp);
		final IType otherTypeLast = otherType.getTypeRefdLast(timestamp);

		if (thisTypeLast == null || otherTypeLast == null || thisTypeLast.getIsErroneous(timestamp)
				|| otherTypeLast.getIsErroneous(timestamp)) {
			return true;
		}

		return thisTypeLast.getTypetype().equals(otherTypeLast.getTypetype());
	}

	public enum CompatibilityLevel {
		INCOMPATIBLE_TYPE, INCOMPATIBLE_SUBTYPE, COMPATIBLE
	}

	@Override
	public CompatibilityLevel getCompatibility(final CompilationTimeStamp timestamp, final IType type, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		if (info == null) {
			ErrorReporter.INTERNAL_ERROR("info==null");
		}

		if (!isCompatible(timestamp, type, info, leftChain, rightChain)) {
			return CompatibilityLevel.INCOMPATIBLE_TYPE;
		}
		
		// if there is noStructuredTypeCompatibility and isCompatible then it should be strong compatibility:
		if( noStructuredTypeCompatibility ) {
			return CompatibilityLevel.COMPATIBLE;
		}

		SubType otherSubType = type.getSubtype();
		if ((info != null) && (subType != null) && (otherSubType != null)) {
			if (info.getStr1Elem()) {
				if (info.getStr2Elem()) {
					// both are string elements -> nothing
					// to do
				} else {
					// char <-> string
					if (!otherSubType.isCompatibleWithElem(timestamp)) {
						info.setSubtypeError("Subtype mismatch: string element has no common value with subtype "
								+ otherSubType.toString());
						return CompatibilityLevel.INCOMPATIBLE_SUBTYPE;
					}
				}
			} else {
				if (info.getStr2Elem()) {
					// string <-> char
					if (!subType.isCompatibleWithElem(timestamp)) {
						info.setSubtypeError("Subtype mismatch: subtype " + subType.toString()
								+ " has no common value with string element");
						return CompatibilityLevel.INCOMPATIBLE_SUBTYPE;
					}
				} else {
					// string <-> string
					if (!subType.isCompatible(timestamp, otherSubType)) {
						info.setSubtypeError("Subtype mismatch: subtype " + subType.toString()
								+ " has no common value with subtype " + otherSubType.toString());
						return CompatibilityLevel.INCOMPATIBLE_SUBTYPE;
					}
				}
			}
		}

		return CompatibilityLevel.COMPATIBLE;
	}

	@Override
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		type.check(timestamp);
		IType temp = type.getTypeRefdLast(timestamp);
		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return getTypetypeTtcn3().equals(temp.getTypetypeTtcn3());
	}

	@Override
	public abstract String getTypename();

	@Override
	public abstract Type_type getTypetypeTtcn3();

	/**
	 * Creates and returns the description of this type, used to describe it
	 * as a completion proposal.
	 * 
	 * @param builder
	 *                the StringBuilder used to create the description.
	 * 
	 * @return the description of this type.
	 * */
	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder;
	}

	@Override
	public Identifier getIdentifier() {
		return null;
	}

	@Override
	public Object[] getOutlineChildren() {
		return new Object[] {};
	}

	@Override
	public String getOutlineText() {
		return "";
	}

	@Override
	public int category() {
		return getTypetype().ordinal();
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * If this type is a simple type, it can never complete any proposals.
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
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * Simple types can not be used as declarations.
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
	}

	/**
	 * Returns whether this type is compatible with type. Used if the other
	 * value is unfoldable, but we can determine its expression return type
	 * <p>
	 * Note: The compatibility relation is asymmetric. The function returns
	 * true if the set of possible values in type is a subset of possible
	 * values in this.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param typeType1
	 *                the type of the first type.
	 * @param typeType2
	 *                the type of the second type.
	 * @param isAsn11
	 *                true if the first type is from ASN.1
	 * @param isAsn12
	 *                true if the second type is from ASN.1
	 * 
	 * @return true if the first type is compatible with the second,
	 *         otherwise false.
	 * */
	public static final boolean isCompatible(final CompilationTimeStamp timestamp, final Type_type typeType1, final Type_type typeType2,
			final boolean isAsn11, final boolean isAsn12) {
		if (Type_type.TYPE_UNDEFINED.equals(typeType1) || Type_type.TYPE_UNDEFINED.equals(typeType2)) {
			return true;
		}

		switch (typeType1) {
		case TYPE_NULL:
		case TYPE_BOOL:
		case TYPE_REAL:
		case TYPE_HEXSTRING:
		case TYPE_SEQUENCE_OF:
		case TYPE_SET_OF:
		case TYPE_VERDICT:
		case TYPE_DEFAULT:
		case TYPE_COMPONENT:
		case TYPE_SIGNATURE:
		case TYPE_PORT:
		case TYPE_ARRAY:
		case TYPE_FUNCTION:
		case TYPE_ALTSTEP:
		case TYPE_TESTCASE:
			return typeType1.equals(typeType2);
		case TYPE_OCTETSTRING:
			return Type_type.TYPE_OCTETSTRING.equals(typeType2) || (!isAsn11 && Type_type.TYPE_ANY.equals(typeType2));
		case TYPE_UCHARSTRING:
			switch (typeType2) {
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
		case TYPE_UTF8STRING:
		case TYPE_BMPSTRING:
		case TYPE_UNIVERSALSTRING:
			switch (typeType2) {
			case TYPE_UCHARSTRING:
			case TYPE_UTF8STRING:
			case TYPE_BMPSTRING:
			case TYPE_UNIVERSALSTRING:
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
		case TYPE_TELETEXSTRING:
		case TYPE_VIDEOTEXSTRING:
		case TYPE_GRAPHICSTRING:
		case TYPE_OBJECTDESCRIPTOR:
		case TYPE_GENERALSTRING:
			switch (typeType2) {
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
			case TYPE_UCHARSTRING:
				return true;
			default:
				return false;
			}
		case TYPE_CHARSTRING:
		case TYPE_NUMERICSTRING:
		case TYPE_PRINTABLESTRING:
		case TYPE_IA5STRING:
		case TYPE_VISIBLESTRING:
		case TYPE_UTCTIME:
		case TYPE_GENERALIZEDTIME:
			switch (typeType2) {
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
		case TYPE_BITSTRING:
		case TYPE_BITSTRING_A:
			return Type_type.TYPE_BITSTRING.equals(typeType2) || Type_type.TYPE_BITSTRING_A.equals(typeType2);
		case TYPE_INTEGER:
		case TYPE_INTEGER_A:
			return Type_type.TYPE_INTEGER.equals(typeType2) || Type_type.TYPE_INTEGER_A.equals(typeType2);
		case TYPE_OBJECTID:
			return Type_type.TYPE_OBJECTID.equals(typeType2) || (!isAsn11 && Type_type.TYPE_ROID.equals(typeType2));
		case TYPE_ROID:
			return Type_type.TYPE_ROID.equals(typeType2) || (!isAsn12 && Type_type.TYPE_OBJECTID.equals(typeType2));
		case TYPE_TTCN3_ENUMERATED:
		case TYPE_ASN1_ENUMERATED:
			return Type_type.TYPE_TTCN3_ENUMERATED.equals(typeType2) || Type_type.TYPE_ASN1_ENUMERATED.equals(typeType2);
		case TYPE_TTCN3_CHOICE:
		case TYPE_ASN1_CHOICE:
		case TYPE_OPENTYPE:
			return Type_type.TYPE_TTCN3_CHOICE.equals(typeType2) || Type_type.TYPE_ASN1_CHOICE.equals(typeType2)
					|| Type_type.TYPE_OPENTYPE.equals(typeType2);
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_ASN1_SEQUENCE:
			return Type_type.TYPE_TTCN3_SEQUENCE.equals(typeType2) || Type_type.TYPE_ASN1_SEQUENCE.equals(typeType2);
		case TYPE_TTCN3_SET:
		case TYPE_ASN1_SET:
			return Type_type.TYPE_TTCN3_SET.equals(typeType2) || Type_type.TYPE_ASN1_SET.equals(typeType2);
		case TYPE_ANY:
			return Type_type.TYPE_ANY.equals(typeType2) || Type_type.TYPE_OCTETSTRING.equals(typeType2);
		case TYPE_REFERENCED:
		case TYPE_OBJECTCLASSFIELDTYPE:
		case TYPE_ADDRESS:
			return false;
		default:
			return false;
		}
	}

	/**
	 * Handles the incremental parsing of this type.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (subType != null) {
			subType.updateSyntax(reparser, false);
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	@Override
	public Assignment getDefiningAssignment() {
		if(getMyScope() == null) {
			return null;
		}

		Module module = getMyScope().getModuleScope();
		Assignment assignment = module.getEnclosingAssignment(getLocation().getOffset());

		return assignment;
	}

	@Override
	public void getEnclosingField(final int offset, final ReferenceFinder rf) {
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (constraints != null) {
			constraints.findReferences(referenceFinder, foundIdentifiers);
		}
		if (withAttributesPath != null) {
			withAttributesPath.findReferences(referenceFinder, foundIdentifiers);
		}
		if (parsedRestrictions != null) {
			for (ParsedSubType parsedSubType : parsedRestrictions) {
				parsedSubType.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (constraints != null && !constraints.accept(v)) {
			return false;
		}
		if (withAttributesPath != null && !withAttributesPath.accept(v)) {
			return false;
		}
		if (parsedRestrictions != null) {
			for (ParsedSubType pst : parsedRestrictions) {
				if (!pst.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
