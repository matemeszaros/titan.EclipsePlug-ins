/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.OmitValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Set_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTracker;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ASN1_Set_Type extends ASN1_Set_Seq_Choice_BaseType {
	private static final String NONEMPTYEXPECTED = "A non-empty value was expected for type `{0}''";

	// TODO these are duplicates,
	//  try to find a way to remove them without too much pain.
	private static final String VALUELISTNOTATIONERRORASN1 = "Value list notation cannot be used for SET type `{0}''";
	private static final String SETVALUEXPECTEDASN1 = "SET value was expected for type `{0}''";
	private static final String NONEXISTENTFIELDASN1 = "Reference to a non-existent component `{0}'' of SET type `{1}''";
	private static final String DUPLICATEFIELDFIRSTASN1 = "Component `{0}'' is already given here";
	private static final String DUPLICATEFIELDAGAINASN1 = "Duplicated SET component `{0}''";
	private static final String MISSINGFIELDASN1 = "Mandatory component `{0}'' is missing from SET value";

	private static final String VALUELISTNOTATIONERRORTTCN3 = "Value list notation cannot be used for set type `{0}''";
	private static final String SETVALUEXPECTEDTTCN3 = "set value was expected for type `{0}''";
	private static final String NONEXISTENTFIELDTTCN3 = "Reference to a non-existent field `{0}'' in set value for type `{1}''";
	private static final String DUPLICATEFIELDFIRSTTTCN3 = "Field `{0}'' is already given here";
	private static final String DUPLICATEFIELDAGAINTTCN3 = "Duplicated set field `{0}''";
	private static final String MISSINGFIELDTTCN3 = "Field `{0}'' is missing from set value";

	private static final String VALUELISTNOTATIONNOTALLOWED = "Value list notation is not allowed for set type `{0}''";
	private static final String NONEMPTYSETTEMPLATEEXPECTED = "A non-empty set template was expected for type `{0}''";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for record type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for record type `{0}''";
	private static final String DUPLICATETEMPLATEFIELDFIRST = "Duplicate field `{0}'' in template";
	private static final String DUPLICATETEMPLATEFIELDAGAIN = "Field `{0}'' is already given here";
	private static final String NONEXISTENTTEMPLATEFIELDREFERENCE = "Reference to non-existing field `{0}'' in record template for type `{1}''";
	private static final String MISSINGTEMPLATEFIELD = "Field `{0}'' is missing from template for record type `{1}''";

	private static final String NOFFIELDSDONTMATCH = "The number of fields in set/SET types must be the same";
	private static final String BADOPTIONALITY = "The optionality of fields in set/SET types must be the same";
	private static final String NOTCOMPATIBLESETSETOF = "set/SET and set of/SET OF types are compatible only with other set/SET and set of/SET OF types";
	private static final String NOTCOMPATIBLEUNIONANYTYPE = "union/CHOICE/anytype types are compatible only with other union/CHOICE/anytype types";

	private CompilationTimeStamp trCompsofTimestamp;

	// The actual value of having the default as optional setting on..
	private static boolean defaultAsOptional;

	static {
		defaultAsOptional = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DEFAULTASOPTIONAL, false, null);

		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (PreferenceConstants.DEFAULTASOPTIONAL.equals(property)) {
						defaultAsOptional = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.DEFAULTASOPTIONAL, false, null);
					}
				}
			});
		}
	}

	// The actual value of the severity level to report stricter constant
	// checking on.
	private static boolean strictConstantCheckingSeverity;

	static {
		strictConstantCheckingSeverity = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.REPORT_STRICT_CONSTANTS, false, null);

		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (PreferenceConstants.REPORT_STRICT_CONSTANTS.equals(property)) {
						strictConstantCheckingSeverity = Platform.getPreferencesService().getBoolean(
								ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORT_STRICT_CONSTANTS,
								false, null);
					}
				}
			});
		}
	}

	public ASN1_Set_Type(final Block aBlock) {
		this.mBlock = aBlock;
	}

	public IASN1Type newInstance() {
		return new ASN1_Set_Type(mBlock);
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_ASN1_SET;
	}

	@Override
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_TTCN3_SET;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != components) {
			components.setMyScope(scope);
		}
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
		case TYPE_ASN1_SET: {
			ASN1_Set_Type tempType = (ASN1_Set_Type) temp;
			if (this == tempType) {
				return true;
			}
			if (getNofComponents(timestamp) != tempType.getNofComponents(timestamp)) {
				info.setErrorStr(NOFFIELDSDONTMATCH);
				return false;
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
			for (int i = 0, size = getNofComponents(timestamp); i < size; i++) {
				CompField cf = getComponentByIndex(i);
				CompField tempTypeCompField = tempType.getComponentByIndex(i);
				IType compFieldType = cf.getType().getTypeRefdLast(timestamp);
				IType tempTypeCompFieldType = tempTypeCompField.getType().getTypeRefdLast(timestamp);
				if (cf.isOptional() != tempTypeCompField.isOptional()) {
					String compFieldName = cf.getIdentifier().getDisplayName();
					String tempTypeCompFieldName = tempTypeCompField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldName);
					info.appendOp2Ref("." + tempTypeCompFieldName);
					info.setOp1Type(compFieldType);
					info.setOp2Type(tempTypeCompFieldType);
					info.setErrorStr(BADOPTIONALITY);
					return false;
				}
				lChain.markState();
				rChain.markState();
				lChain.add(compFieldType);
				rChain.add(tempTypeCompFieldType);
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(compFieldType, tempTypeCompFieldType, false);
				if (!compFieldType.equals(tempTypeCompFieldType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !compFieldType.isCompatible(timestamp, tempTypeCompFieldType, infoTemp, lChain, rChain)) {
					String compFieldName = cf.getIdentifier().getDisplayName();
					String tempTypeCompFieldName = tempTypeCompField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldName + infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCompFieldName + infoTemp.getOp2RefStr());
					info.setOp1Type(infoTemp.getOp1Type());
					info.setOp2Type(infoTemp.getOp2Type());
					info.setErrorStr(infoTemp.getErrorStr());
					lChain.previousState();
					rChain.previousState();
					return false;
				}
				lChain.previousState();
				rChain.previousState();
			}
			info.setNeedsConversion(true);
			return true;
		}
		case TYPE_TTCN3_SET: {
			TTCN3_Set_Type tempType = (TTCN3_Set_Type) temp;
			if (getNofComponents(timestamp) != tempType.getNofComponents()) {
				info.setErrorStr(NOFFIELDSDONTMATCH);
				return false;
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
			for (int i = 0, size = getNofComponents(timestamp); i < size; i++) {
				CompField compField = getComponentByIndex(i);
				CompField tempTypeCompField = tempType.getComponentByIndex(i);
				IType compFieldType = compField.getType().getTypeRefdLast(timestamp);
				IType tempTypeCompFieldType = tempTypeCompField.getType().getTypeRefdLast(timestamp);
				if (compField.isOptional() != tempTypeCompField.isOptional()) {
					String compFieldName = compField.getIdentifier().getDisplayName();
					String tempTypeCompFieldName = tempTypeCompField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldName);
					info.appendOp2Ref("." + tempTypeCompFieldName);
					info.setOp1Type(compFieldType);
					info.setOp2Type(tempTypeCompFieldType);
					info.setErrorStr(BADOPTIONALITY);
					return false;
				}
				lChain.markState();
				rChain.markState();
				lChain.add(compFieldType);
				rChain.add(tempTypeCompFieldType);
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(compFieldType, tempTypeCompFieldType, false);
				if (!compFieldType.equals(tempTypeCompFieldType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !compFieldType.isCompatible(timestamp, tempTypeCompFieldType, infoTemp, lChain, rChain)) {
					String compFieldName = compField.getIdentifier().getDisplayName();
					String tempTypeCompFieldName = tempTypeCompField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldName + infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCompFieldName + infoTemp.getOp2RefStr());
					info.setOp1Type(infoTemp.getOp1Type());
					info.setOp2Type(infoTemp.getOp2Type());
					info.setErrorStr(infoTemp.getErrorStr());
					lChain.previousState();
					rChain.previousState();
					return false;
				}
				lChain.previousState();
				rChain.previousState();
			}
			info.setNeedsConversion(true);
			return true;
		}
		case TYPE_SET_OF: {
			SetOf_Type tempType = (SetOf_Type) temp;
			if (!tempType.isSubtypeCompatible(timestamp, this)) {
				info.setErrorStr("Incompatible set of/SET OF subtypes");
				return false;
			}

			int nofComps = getNofComponents(timestamp);
			if (nofComps == 0) {
				return false;
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
			for (int i = 0; i < nofComps; i++) {
				CompField compField = getComponentByIndex(i);
				IType compFieldType = compField.getType().getTypeRefdLast(timestamp);
				IType temporalTypeOfType = tempType.getOfType().getTypeRefdLast(timestamp);
				lChain.markState();
				rChain.markState();
				lChain.add(compFieldType);
				rChain.add(temporalTypeOfType);
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(compFieldType, temporalTypeOfType, false);
				if (!compFieldType.equals(temporalTypeOfType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !compFieldType.isCompatible(timestamp, temporalTypeOfType, infoTemp, lChain, rChain)) {
					info.appendOp1Ref("." + compField.getIdentifier().getDisplayName() + infoTemp.getOp1RefStr());
					if (infoTemp.getOp2RefStr().length() > 0) {
						info.appendOp2Ref("[]");
					}
					info.appendOp2Ref(infoTemp.getOp2RefStr());
					info.setOp1Type(infoTemp.getOp1Type());
					info.setOp2Type(infoTemp.getOp2Type());
					info.setErrorStr(infoTemp.getErrorStr());
					lChain.previousState();
					rChain.previousState();
					return false;
				}
				lChain.previousState();
				rChain.previousState();
			}
			info.setNeedsConversion(true);
			return true;
		}
		case TYPE_ASN1_CHOICE:
		case TYPE_TTCN3_CHOICE:
		case TYPE_ANYTYPE:
			info.setErrorStr(NOTCOMPATIBLEUNIONANYTYPE);
			return false;
		case TYPE_ASN1_SEQUENCE:
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_SEQUENCE_OF:
		case TYPE_ARRAY:
			info.setErrorStr(NOTCOMPATIBLESETSETOF);
			return false;
		default:
			return false;
		}
	}

	@Override
	public String getOutlineIcon() {
		return "set.gif";
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("set");
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (components == null) {
			return;
		}

		if (referenceChain.add(this)) {
			CompField field;
			IType t;
			for (int i = 0, size = components.getNofComps(); i < size; i++) {
				field = components.getCompByIndex(i);
				t = field.getType();
				if (!field.isOptional() && t != null) {
					referenceChain.markState();
					t.checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		if (components != null && myScope != null) {
			Module module = myScope.getModuleScope();
			if (module != null) {
				if (module.getSkippedFromSemanticChecking()) {
					lastTimeChecked = timestamp;
					return;
				}
			}
		}
		isErroneous = false;

		if (components == null) {
			parseBlockSet();
		}

		if (isErroneous || components == null) {
			return;
		}

		trCompsof(timestamp, null);
		components.check(timestamp);
		// ctss.chk_tags()

		if (constraints != null) {
			constraints.check(timestamp);
		}
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
			last = last.setValuetype(timestamp, Value_type.SET_VALUE);
			if (last.isAsn()) {
				checkThisValueSet(timestamp, (Set_Value) last, valueCheckingOptions.expected_value, false,
						valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			} else {
				checkThisValueSet(timestamp, (Set_Value) last, valueCheckingOptions.expected_value,
						valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit,
						valueCheckingOptions.str_elem);
			}
			break;
		case SEQUENCEOF_VALUE:
			if (((SequenceOf_Value) last).isIndexed()) {
				value.getLocation().reportSemanticError(
					MessageFormat.format(
						"Indexed assignment notation cannot be used for SET type `{0}''",
						getFullName()));
				value.setIsErroneous(true);
			} else {
				SequenceOf_Value temporalValue = (SequenceOf_Value) last;
				if (temporalValue.getNofComponents() == 0) {
					if (getNofComponents(timestamp) == 0) {
						last = last.setValuetype(timestamp, Value_type.SET_VALUE);
					} else {
						value.getLocation().reportSemanticError(MessageFormat.format(NONEMPTYEXPECTED, getFullName()));
						value.setIsErroneous(true);
					}
				} else {
					value.getLocation().reportSemanticError(
							MessageFormat.format(last.isAsn() ? VALUELISTNOTATIONERRORASN1 : VALUELISTNOTATIONERRORTTCN3,
									getFullName()));
					value.setIsErroneous(true);
				}
			}
			break;
		case SET_VALUE:
			if (last.isAsn()) {
				checkThisValueSet(timestamp, (Set_Value) last, valueCheckingOptions.expected_value, false,
						valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			} else {
				checkThisValueSet(timestamp, (Set_Value) last, valueCheckingOptions.expected_value,
						valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit,
						valueCheckingOptions.str_elem);
			}
			break;
		case UNDEFINED_BLOCK:
			last = last.setValuetype(timestamp, Value_type.SET_VALUE);
			checkThisValueSet(timestamp, (Set_Value) last, valueCheckingOptions.expected_value, false,
					valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(
					MessageFormat.format(last.isAsn() ? SETVALUEXPECTEDASN1 : SETVALUEXPECTEDTTCN3, getFullName()));
			value.setIsErroneous(true);
		}
	}

	/**
	 * Checks the Set_Value kind value against this type.
	 * <p>
	 * Please note, that this function can only be called once we know for
	 * sure that the value is of set type.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param value
	 *                the value to be checked
	 * @param expectedValue
	 *                the kind of value expected here.
	 * @param incompleteAllowed
	 *                wheather incomplete value is allowed or not.
	 * @param implicitOmit
	 *                true if the implicit omit optional attribute was set
	 *                for the value, false otherwise
	 * */
	private void checkThisValueSet(final CompilationTimeStamp timestamp, final Set_Value value, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean implicitOmit, final boolean strElem) {
		Map<String, NamedValue> componentMap = new HashMap<String, NamedValue>();

		value.removeGeneratedValues();

		boolean isAsn = value.isAsn();
		int nofValueComponents = value.getNofComponents();
		for (int i = 0; i < nofValueComponents; i++) {
			NamedValue namedValue = value.getSequenceValueByIndex(i);
			Identifier valueId = namedValue.getName();
			if (!hasComponentWithName(valueId)) {
				namedValue.getLocation().reportSemanticError(
						MessageFormat.format(isAsn ? NONEXISTENTFIELDASN1 : NONEXISTENTFIELDTTCN3, namedValue.getName()
								.getDisplayName(), getTypename()));
			} else {
				if (componentMap.containsKey(valueId.getName())) {
					namedValue.getLocation().reportSemanticError(
							MessageFormat.format(isAsn ? DUPLICATEFIELDAGAINASN1 : DUPLICATEFIELDAGAINTTCN3,
									valueId.getDisplayName()));
					componentMap.get(valueId.getName()).getLocation().reportSingularSemanticError(
						MessageFormat.format(isAsn ? DUPLICATEFIELDFIRSTASN1
						: DUPLICATEFIELDFIRSTTTCN3, valueId.getDisplayName()));
				} else {
					componentMap.put(valueId.getName(), namedValue);
				}

				CompField componentField = getComponentByName(valueId);
				Type type = componentField.getType();
				IValue componentValue = namedValue.getValue();

				if (componentValue != null) {
					componentValue.setMyGovernor(type);
					IValue temporalValue = type.checkThisValueRef(timestamp, componentValue);
					boolean isOptional = componentField.isOptional();
					if (!isOptional && componentField.hasDefault() && defaultAsOptional) {
						isOptional = true;
					}
					type.checkThisValue(timestamp, temporalValue, new ValueCheckingOptions(expectedValue, incompleteAllowed,
							isOptional, true, implicitOmit, strElem));
				}
			}
		}

		if (!incompleteAllowed || strictConstantCheckingSeverity) {
			int nofTypeComponents = getNofComponents(timestamp);
			CompField field;
			for (int i = 0; i < nofTypeComponents; i++) {
				field = getComponentByIndex(i);
				Identifier id = field.getIdentifier();
				if (!componentMap.containsKey(id.getName())) {
					if (field.isOptional() && implicitOmit) {
						value.addNamedValue(new NamedValue(new Identifier(Identifier_type.ID_TTCN, id.getDisplayName()),
								new Omit_Value(), false));
					} else {
						value.getLocation().reportSemanticError(
								MessageFormat.format(isAsn ? MISSINGFIELDASN1 : MISSINGFIELDTTCN3,
										id.getDisplayName()));
					}
				}
			}
		}
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit) {
		registerUsage(template);
		template.setMyGovernor(this);

		switch (template.getTemplatetype()) {
		case TEMPLATE_LIST:
			if (((Template_List) template).getNofTemplates() > 0) {
				template.getLocation().reportSemanticError(MessageFormat.format(VALUELISTNOTATIONNOTALLOWED, getFullName()));
				break;
			} else if (getNofComponents(timestamp) > 0) {
				template.getLocation().reportSemanticError(MessageFormat.format(NONEMPTYSETTEMPLATEEXPECTED, getFullName()));
			} else {
				ITTCN3Template transformed = template.setTemplatetype(timestamp, Template_type.NAMED_TEMPLATE_LIST);
				checkThisNamedTemplateList(timestamp, (Named_Template_List) transformed, isModified, implicitOmit);
			}
			break;
		case NAMED_TEMPLATE_LIST:
			checkThisNamedTemplateList(timestamp, (Named_Template_List) template, isModified, implicitOmit);
			break;
		default:
			template.getLocation().reportSemanticError(
					MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
			break;
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(LENGTHRESTRICTIONNOTALLOWED);
		}
	}

	/**
	 * Checks the provided named template list against this type.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param templateList
	 *                the template list to check
	 * @param isModified
	 *                true if the template is modified otherwise false.
	 * @param implicitOmit
	 *                true it the template has implicit omit attribute set,
	 *                false otherwise.
	 * */
	private void checkThisNamedTemplateList(final CompilationTimeStamp timestamp, final Named_Template_List templateList,
			final boolean isModified, final boolean implicitOmit) {
		templateList.removeGeneratedValues();

		Map<String, NamedTemplate> componentMap = new HashMap<String, NamedTemplate>();
		int nofTypeComponents = getNofComponents(timestamp);
		int nofTemplateComponents = templateList.getNofTemplates();

		for (int i = 0; i < nofTemplateComponents; i++) {
			NamedTemplate namedTemplate = templateList.getTemplateByIndex(i);
			Identifier identifier = namedTemplate.getName();
			String templateName = identifier.getName();

			if (hasComponentWithName(identifier)) {
				if (componentMap.containsKey(templateName)) {
					namedTemplate.getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATETEMPLATEFIELDFIRST, identifier.getDisplayName()));
					componentMap.get(templateName).getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATETEMPLATEFIELDAGAIN, identifier.getDisplayName()));
				} else {
					componentMap.put(templateName, namedTemplate);
				}

				CompField componentField = getComponentByName(identifier);
				Type type = componentField.getType();
				if (type != null && !type.getIsErroneous(timestamp)) {
					ITTCN3Template componentTemplate = namedTemplate.getTemplate();
					componentTemplate.setMyGovernor(type);
					componentTemplate = type.checkThisTemplateRef(timestamp, componentTemplate);
					boolean isOptional = componentField.isOptional();
					if (!isOptional && componentField.hasDefault() && defaultAsOptional) {
						isOptional = true;
					}
					componentTemplate.checkThisTemplateGeneric(timestamp, type, isModified, isOptional, isOptional, true,
							implicitOmit);
				}
			} else {
				namedTemplate.getLocation().reportSemanticError(
						MessageFormat.format(NONEXISTENTTEMPLATEFIELDREFERENCE, identifier.getDisplayName(), getTypename()));
			}
		}

		if (!isModified && strictConstantCheckingSeverity) {
			// check missing fields
			for (int i = 0; i < nofTypeComponents; i++) {
				Identifier identifier = getComponentIdentifierByIndex(i);
				if (!componentMap.containsKey(identifier.getName())) {
					if (getComponentByIndex(i).isOptional() && implicitOmit) {
						templateList.addNamedValue(new NamedTemplate(new Identifier(Identifier_type.ID_TTCN, identifier
								.getDisplayName()), new OmitValue_Template(), false));
					} else {
						templateList.getLocation().reportSemanticError(
								MessageFormat.format(MISSINGTEMPLATEFIELD,
								identifier.getDisplayName(), getTypename()));
					}
				}
			}
		}
	}

	/** Parses the block as if it were the block of a set. */
	private void parseBlockSet() {
		if (null == mBlock) {
			return;
		}

		final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mBlock);
		if (null == parser) {
			return;
		}

		components = parser.pr_special_ComponentTypeLists().list;
		final List<SyntacticErrorStorage> errors = parser.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			components = null;
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}

		if (components == null) {
			isErroneous = true;
			return;
		}

		components.setFullNameParent(this);
		components.setMyScope(getMyScope());
		components.setMyType(this);
	}
	
	/**
	 * Check the components of member to reveal possible recursive
	 * referencing.
	 * 
	 * @param timestamp
	 *                the actual compilation cycle.
	 * @param referenceChain
	 *                the reference chain used to detect recursive
	 *                referencing
	 * */
	public void trCompsof(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (trCompsofTimestamp != null && !trCompsofTimestamp.isLess(timestamp)) {
			return;
		}

		if (referenceChain != null) {
			components.trCompsof(timestamp, referenceChain, false);
		} else {
			IReferenceChain temporalReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);

			components.trCompsof(timestamp, temporalReferenceChain, false);

			temporalReferenceChain.release();
		}

		trCompsofTimestamp = timestamp;
		components.trCompsof(timestamp, null, true);
	}

	// This is the same as in ASN1_Sequence_Type
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
			CompField compField = components.getCompByName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference)
								.getId().getDisplayName(), getTypename()));
				return null;
			}

			if (interruptIfOptional && compField.isOptional()) {
				return null;
			}

			Expected_Value_type internalExpectation = (expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE) ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE
					: expectedIndex;

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

	// This is the same as in ASN1_Sequence_type
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
		case fieldSubReference: {
			Identifier id = subreference.getId();
			CompField compField = components.getCompByName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference)
								.getId().getDisplayName(), getTypename()));
				return false;
			}

			int fieldIndex = components.components.indexOf(compField);
			IType fieldType = compField.getType();
			if (fieldType == null) {
				return false;
			}
			subrefsArray.add(fieldIndex);
			typeArray.add(this);
			return fieldType.getSubrefsAsArray(timestamp, reference, actualSubReference + 1, subrefsArray, typeArray);
		}
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference)
							.getId().getDisplayName(), getTypename()));
			return false;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return false;
		}
	}
}
