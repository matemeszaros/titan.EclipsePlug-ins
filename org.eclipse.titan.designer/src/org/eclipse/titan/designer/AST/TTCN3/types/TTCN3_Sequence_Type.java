/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.OmitValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
public final class TTCN3_Sequence_Type extends TTCN3_Set_Seq_Choice_BaseType {
	public static final String INCOMPLETEPRESENTERROR = "Not used symbol `-' is not allowed in this context";
	private static final String UNSUPPERTED_FIELDNAME = "Sorry, but it is not supported for sequence types to have a field with a name (`{0}'')"
			+ " which exactly matches the name of the type definition.";
	private static final String SEQUANCEEPECTED = "sequence value was expected for type `{0}''";

	private static final String NONEXISTENTFIELDERRORASN1 = "Reference to a non-existent component `{0}'' of SEQUENCE type `{1}''";
	private static final String DUPLICATEDFIELDFIRSTASN1 = "Component `{0}'' is already given here";
	private static final String DUPLICATEDFIELDAGAINASN1 = "Duplicated SEQUENCE component `{0}''";
	private static final String WRONGFIELDORDERASN1 = "Component `{0}'' cannot appear after component `{1}'' in SEQUENCE value";
	private static final String UNEXPECTEDFIELDASN1 = "Unexpected component `{0}'' in SEQUENCE value, expecting `{1}''";
	private static final String MISSINGFIELDASN1 = "Mandatory component `{0}'' is missing from SEQUENCE value";

	private static final String NONEXISTENTFIELDERRORTTCN3 = "Reference to a non-existent field `{0}'' in record value for type `{1}''";
	private static final String DUPLICATEDFIELDFIRSTTTCN3 = "Field `{0}'' is already given here";
	private static final String DUPLICATEDFIELDAGAINTTCN3 = "Duplicated record field `{0}''";
	private static final String WRONGFIELDORDERTTCN3 = "Field `{0}'' cannot appear after field `{1}'' in record value";
	private static final String UNEXPECTEDFIELDTTCN3 = "Unexpected field `{0}'' in record value, expecting `{1}''";
	private static final String MISSINGFIELDTTCN3 = "Field `{0}'' is missing from record value";

	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for record type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for record type `{0}''";
	private static final String DUPLICATETEMPLATEFIELDFIRST = "Duplicate field `{0}'' in template";
	private static final String DUPLICATETEMPLATEFIELDAGAIN = "Field `{0}'' is already given here";
	private static final String INCORRECTTEMPLATEFIELDORDER = "Field `{0}'' cannot appear after field `{1}''"
			+ " in a template for record type `{2}''";
	private static final String UNEXPECTEDTEMPLATEFIELD = "Unexpected field `{0}'' in record template, expecting `{1}''";
	private static final String NONEXISTENTTEMPLATEFIELDREFERENCE = "Reference to non-existing field `{0}'' in record template for type `{1}''";
	private static final String MISSINGTEMPLATEFIELD = "Field `{0}'' is missing from template for record type `{1}''";

	private static final String NOFFIELDSDONTMATCH = "The number of fields in record/SEQUENCE types must be the same";
	private static final String NOFFIELDSDIMENSIONDONTMATCH = "The number of fields in record types ({0}) and the size of the array ({1})"
			+ " must be the same";
	private static final String BADOPTIONALITY = "The optionality of fields in record/SEQUENCE types must be the same";
	private static final String NOTCOMPATIBLESETSETOF = "set/SET and set of/SET OF types are compatible only"
			+ " with other set/SET and set of/SET OF types";
	private static final String NOTCOMPATIBLEUNIONANYTYPE = "union/CHOICE/anytype types are compatible only"
			+ " with other union/CHOICE/anytype types";

	// The actual value of the severity level to report stricter constant
	// checking on.
	private static boolean strictConstantCheckingSeverity;

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			strictConstantCheckingSeverity = ps.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORT_STRICT_CONSTANTS, false, null);

			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.REPORT_STRICT_CONSTANTS.equals(property)) {
							strictConstantCheckingSeverity = ps.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORT_STRICT_CONSTANTS, false, null);
						}
					}
				});
			}
		}
	}

	public TTCN3_Sequence_Type(final CompFieldMap compFieldMap) {
		super(compFieldMap);
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_TTCN3_SEQUENCE;
	}

	@Override
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_RECORD;
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
		case TYPE_ASN1_SEQUENCE: {
			ASN1_Sequence_Type tempType = (ASN1_Sequence_Type) temp;
			if (getNofComponents() != tempType.getNofComponents(timestamp)) {
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
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				CompField cf = getComponentByIndex(i);
				CompField tempTypeCf = tempType.getComponentByIndex(i);
				IType cfType = cf.getType().getTypeRefdLast(timestamp);
				IType tempTypeCfType = tempTypeCf.getType().getTypeRefdLast(timestamp);
				if (cf.isOptional() != tempTypeCf.isOptional()) {
					String cfName = cf.getIdentifier().getDisplayName();
					String tempTypeCfName = tempTypeCf.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + cfName);
					info.appendOp2Ref("." + tempTypeCfName);
					info.setOp1Type(cfType);
					info.setOp2Type(tempTypeCfType);
					info.setErrorStr(BADOPTIONALITY);
					return false;
				}
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(cfType, tempTypeCfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(cfType);
				rChain.add(tempTypeCfType);
				if (!cfType.equals(tempTypeCfType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !cfType.isCompatible(timestamp, tempTypeCfType, infoTemp, lChain, rChain)) {
					String cfName = cf.getIdentifier().getDisplayName();
					String tempTypeCfName = tempTypeCf.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + cfName + infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCfName + infoTemp.getOp2RefStr());
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
		case TYPE_TTCN3_SEQUENCE: {
			TTCN3_Sequence_Type tempType = (TTCN3_Sequence_Type) temp;
			if (this == tempType) {
				return true;
			}
			if (getNofComponents() != tempType.getNofComponents()) {
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
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				CompField cf = getComponentByIndex(i);
				CompField tempTypeCf = tempType.getComponentByIndex(i);
				IType cfType = cf.getType().getTypeRefdLast(timestamp);
				IType tempTypeCfType = tempTypeCf.getType().getTypeRefdLast(timestamp);
				if (cf.isOptional() != tempTypeCf.isOptional()) {
					String cfName = cf.getIdentifier().getDisplayName();
					String tempTypeCfName = tempTypeCf.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + cfName);
					info.appendOp2Ref("." + tempTypeCfName);
					info.setOp1Type(cfType);
					info.setOp2Type(tempTypeCfType);
					info.setErrorStr(BADOPTIONALITY);
					return false;
				}
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(cfType, tempTypeCfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(cfType);
				rChain.add(tempTypeCfType);
				if (!cfType.equals(tempTypeCfType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !cfType.isCompatible(timestamp, tempTypeCfType, infoTemp, lChain, rChain)) {
					String cfName = cf.getIdentifier().getDisplayName();
					String tempTypeCfName = tempTypeCf.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + cfName + infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCfName + infoTemp.getOp2RefStr());
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
		case TYPE_SEQUENCE_OF: {
			SequenceOf_Type tempType = (SequenceOf_Type) temp;
			if (!tempType.isSubtypeCompatible(timestamp, this)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}

			int nofComps = getNofComponents();
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
				CompField cf = getComponentByIndex(i);
				IType cfType = cf.getType().getTypeRefdLast(timestamp);
				IType tempTypeOfType = tempType.getOfType().getTypeRefdLast(timestamp);
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(cfType, tempTypeOfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(cfType);
				rChain.add(tempTypeOfType);
				if (!cfType.equals(tempTypeOfType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !cfType.isCompatible(timestamp, tempTypeOfType, infoTemp, lChain, rChain)) {
					info.appendOp1Ref("." + cf.getIdentifier().getDisplayName() + infoTemp.getOp1RefStr());
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
		case TYPE_ARRAY: {
			Array_Type tempType = (Array_Type) temp;
			int nofComps = getNofComponents();
			if (nofComps == 0) {
				return false;
			}
			long tempTypeNOfComps = tempType.getDimension().getSize();
			if (nofComps != tempTypeNOfComps) {
				info.setErrorStr(MessageFormat.format(NOFFIELDSDIMENSIONDONTMATCH, nofComps, tempTypeNOfComps));
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
				CompField cf = getComponentByIndex(i);
				IType cfType = cf.getType().getTypeRefdLast(timestamp);
				IType tempTypeElementType = tempType.getElementType().getTypeRefdLast(timestamp);
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(cfType, tempTypeElementType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(cfType);
				rChain.add(tempTypeElementType);
				if (!cfType.equals(tempTypeElementType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !cfType.isCompatible(timestamp, tempTypeElementType, infoTemp, lChain, rChain)) {
					info.appendOp1Ref("." + cf.getIdentifier().getDisplayName() + infoTemp.getOp1RefStr());
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
		case TYPE_ASN1_SET:
		case TYPE_TTCN3_SET:
		case TYPE_SET_OF:
			info.setErrorStr(NOTCOMPATIBLESETSETOF);
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
		return "record.gif";
	}

	@Override
	public void checkConstructorName(final String definitionName) {
		if (hasComponentWithName(definitionName)) {
			CompField field = getComponentByName(definitionName);
			field.getIdentifier().getLocation()
					.reportSemanticError(MessageFormat.format(UNSUPPERTED_FIELDNAME, field.getIdentifier().getDisplayName()));
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				CompField field = getComponentByIndex(i);
				IType type = field.getType();
				if (!field.isOptional() && type != null) {
					referenceChain.markState();
					type.checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
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
			if (last.isAsn()) {
				checkThisValueSeq(timestamp, (Sequence_Value) last, valueCheckingOptions.expected_value, false,
						valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			} else {
				checkThisValueSeq(timestamp, (Sequence_Value) last, valueCheckingOptions.expected_value,
						valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit,
						valueCheckingOptions.str_elem);
			}
			break;
		case SEQUENCEOF_VALUE:
			if (((SequenceOf_Value) last).isIndexed()) {
				value.getLocation().reportSemanticError(
						MessageFormat.format("Indexed assignment notation cannot be used for record type `{0}''",
								getFullName()));
				value.setIsErroneous(true);
			} else {
				last = last.setValuetype(timestamp, Value_type.SEQUENCE_VALUE);
				if (last.isAsn()) {
					checkThisValueSeq(timestamp, (Sequence_Value) last, valueCheckingOptions.expected_value, false,
							valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
				} else {
					checkThisValueSeq(timestamp, (Sequence_Value) last, valueCheckingOptions.expected_value,
							valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit,
							valueCheckingOptions.str_elem);
				}
			}
			break;
		case UNDEFINED_BLOCK:
			last = last.setValuetype(timestamp, Value_type.SEQUENCE_VALUE);
			checkThisValueSeq(timestamp, (Sequence_Value) last, valueCheckingOptions.expected_value, false,
					valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(MessageFormat.format(SEQUANCEEPECTED, getFullName()));
			value.setIsErroneous(true);
		}

		if (valueCheckingOptions.sub_check) {
			// there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, last);
			}
		}

		value.setLastTimeChecked(timestamp);
	}

	/**
	 * Checks the Sequence_Value kind value against this type.
	 * <p>
	 * Please note, that this function can only be called once we know for
	 * sure that the value is of sequence type.
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
	private void checkThisValueSeq(final CompilationTimeStamp timestamp, final Sequence_Value value, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean implicitOmit, final boolean strElem) {
		CompilationTimeStamp valueTimeStamp = value.getLastTimeChecked();
		if (valueTimeStamp == null || valueTimeStamp.isLess(timestamp)) {
			value.removeGeneratedValues();
		}

		Map<String, NamedValue> componentMap = new HashMap<String, NamedValue>();
		Map<String, CompField> realComponents = compFieldMap.getComponentFieldMap(timestamp);

		final boolean isAsn = value.isAsn();
		boolean inSnyc = true;
		final int nofTypeComponents = realComponents.size();
		final int nofValueComponents = value.getNofComponents();
		int nextIndex = 0;
		CompField lastCompField = null;
		int sequenceIndex = 0;
		for (int i = 0; i < nofValueComponents; i++, sequenceIndex++) {
			NamedValue namedValue = value.getSeqValueByIndex(i);
			Identifier valueId = namedValue.getName();

			if (!realComponents.containsKey(valueId.getName())) {
				namedValue.getLocation().reportSemanticError(
						MessageFormat.format(isAsn ? NONEXISTENTFIELDERRORASN1 : NONEXISTENTFIELDERRORTTCN3, namedValue
								.getName().getDisplayName(), getTypename()));
				inSnyc = false;
			} else {
				if (componentMap.containsKey(valueId.getName())) {
					final String duplicateAgain = MessageFormat.format(isAsn ? DUPLICATEDFIELDAGAINASN1
							: DUPLICATEDFIELDAGAINTTCN3, valueId.getDisplayName());
					namedValue.getLocation().reportSemanticError(duplicateAgain);
					final String duplicateFirst = MessageFormat.format(isAsn ? DUPLICATEDFIELDFIRSTASN1
							: DUPLICATEDFIELDFIRSTTTCN3, valueId.getDisplayName());
					componentMap.get(valueId.getName()).getLocation().reportSingularSemanticError(duplicateFirst);
					inSnyc = false;
				} else {
					componentMap.put(valueId.getName(), namedValue);
				}

				CompField componentField = realComponents.get(valueId.getName());
				if (inSnyc) {
					if (incompleteAllowed) {
						boolean found = false;

						for (int j = nextIndex; j < nofTypeComponents && !found; j++) {
							CompField field2 = getComponentByIndex(j);
							if (valueId.getName().equals(field2.getIdentifier().getName())) {
								lastCompField = field2;
								nextIndex = j + 1;
								found = true;
							}
						}

						if (lastCompField != null && !found) {
							namedValue.getLocation().reportSemanticError(
									MessageFormat.format(isAsn ? WRONGFIELDORDERASN1 : WRONGFIELDORDERTTCN3,
											valueId.getDisplayName(), lastCompField.getIdentifier()
													.getDisplayName()));
							inSnyc = false;
						}
					} else {
						CompField field2 = getComponentByIndex(sequenceIndex);
						CompField field2Original = field2;
						while (implicitOmit && sequenceIndex < getNofComponents() && componentField != field2
								&& field2.isOptional()) {
							field2 = getComponentByIndex(sequenceIndex);
						}
						if (sequenceIndex >= getNofComponents() || componentField != field2) {
							namedValue.getLocation().reportSemanticError(
									MessageFormat.format(isAsn ? UNEXPECTEDFIELDASN1 : UNEXPECTEDFIELDTTCN3,
											valueId.getDisplayName(), field2Original.getIdentifier()
													.getDisplayName()));
						}
					}
				}

				Type type = componentField.getType();
				IValue componentValue = namedValue.getValue();

				if (componentValue != null) {
					componentValue.setMyGovernor(type);
					if (Value_type.NOTUSED_VALUE.equals(componentValue.getValuetype())) {
						if (!incompleteAllowed) {
							componentValue.getLocation().reportSemanticError(INCOMPLETEPRESENTERROR);
						}
					} else {
						IValue tempValue = type.checkThisValueRef(timestamp, componentValue);
						type.checkThisValue(timestamp, tempValue, new ValueCheckingOptions(expectedValue, incompleteAllowed,
								componentField.isOptional(), true, implicitOmit, strElem));
					}
				}
			}
		}

		if (!incompleteAllowed || strictConstantCheckingSeverity) {
			for (int i = 0; i < nofTypeComponents; i++) {
				Identifier id = compFieldMap.fields.get(i).getIdentifier();
				if (!componentMap.containsKey(id.getName())) {
					if (getComponentByIndex(i).isOptional() && implicitOmit) {
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

		value.setLastTimeChecked(timestamp);
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit) {
		registerUsage(template);
		template.setMyGovernor(this);

		switch (template.getTemplatetype()) {
		case TEMPLATE_LIST:
			ITTCN3Template transformed = template.setTemplatetype(timestamp, Template_type.NAMED_TEMPLATE_LIST);
			checkThisNamedTemplateList(timestamp, (Named_Template_List) transformed, isModified, implicitOmit);
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
	 *                is the template modified or not ?
	 * @param implicitOmit
	 *                true if the implicit omit optional attribute was set
	 *                for the template, false otherwise
	 * */
	private void checkThisNamedTemplateList(final CompilationTimeStamp timestamp, final Named_Template_List templateList,
			final boolean isModified, final boolean implicitOmit) {
		templateList.removeGeneratedValues();

		Map<String, NamedTemplate> componentMap = new HashMap<String, NamedTemplate>();
		int nofTypeComponents = getNofComponents();
		int nofTemplateComponents = templateList.getNofTemplates();
		boolean inSync = true;

		Map<String, CompField> realComponents = compFieldMap.getComponentFieldMap(timestamp);
		CompField lastComponentField = null;
		int nextIndex = 0;
		for (int i = 0; i < nofTemplateComponents; i++) {
			NamedTemplate namedTemplate = templateList.getTemplateByIndex(i);
			Identifier identifier = namedTemplate.getName();
			String templateName = identifier.getName();

			if (realComponents.containsKey(templateName)) {
				if (componentMap.containsKey(templateName)) {
					namedTemplate.getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATETEMPLATEFIELDFIRST, identifier.getDisplayName()));
					componentMap.get(templateName)
							.getLocation()
							.reportSemanticError(
									MessageFormat.format(DUPLICATETEMPLATEFIELDAGAIN, identifier.getDisplayName()));
				} else {
					componentMap.put(templateName, namedTemplate);
				}

				CompField componentField = getComponentByName(identifier.getName());

				if (inSync) {
					if (isModified) {
						boolean found = false;
						for (int j = nextIndex; j < nofTypeComponents && !found; j++) {
							CompField componentField2 = getComponentByIndex(j);
							if (templateName.equals(componentField2.getIdentifier().getName())) {
								lastComponentField = componentField2;
								nextIndex = j + 1;
								found = true;
							}
						}
						if (!found && lastComponentField != null) {
							final String message = MessageFormat.format(INCORRECTTEMPLATEFIELDORDER, identifier
									.getDisplayName(), lastComponentField.getIdentifier().getDisplayName(),
									getFullName());
							namedTemplate.getLocation().reportSemanticError(message);
							inSync = false;
						}
					} else if (strictConstantCheckingSeverity) {
						CompField componentField2 = getComponentByIndex(i);
						if (componentField2 != componentField) {
							if (!componentField2.isOptional() || (componentField2.isOptional() && !implicitOmit)) {
								final String message = MessageFormat.format(UNEXPECTEDTEMPLATEFIELD, identifier
										.getDisplayName(), componentField2.getIdentifier().getDisplayName());
								namedTemplate.getLocation().reportSemanticError(message);
								inSync = false;
							}
						}
					}
				}

				Type type = componentField.getType();
				ITTCN3Template componentTemplate = namedTemplate.getTemplate();
				componentTemplate.setMyGovernor(type);
				componentTemplate = type.checkThisTemplateRef(timestamp, componentTemplate);
				boolean isOptional = componentField.isOptional();
				componentTemplate.checkThisTemplateGeneric(timestamp, type, isModified, isOptional, isOptional, true, implicitOmit);
			} else {
				namedTemplate.getLocation().reportSemanticError(
						MessageFormat.format(NONEXISTENTTEMPLATEFIELDREFERENCE, identifier.getDisplayName(), getTypename()));
				inSync = false;
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
						templateList.getLocation()
								.reportSemanticError(
										MessageFormat.format(MISSINGTEMPLATEFIELD,
												identifier.getDisplayName(), getTypename()));
					}
				}
			}
		}
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("sequence");
	}
}
