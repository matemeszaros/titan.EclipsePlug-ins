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
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
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
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.SequenceOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
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
public final class ASN1_Sequence_Type extends ASN1_Set_Seq_Choice_BaseType {
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for record type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for record type `{0}''";
	private static final String SEQUANCEEPECTED = "SEQUENCE value was expected for type `{0}''";

	private static final String NONEXISTENTFIELDERRORASN1 = "Reference to a non-existent component `{0}'' of SEQUENCE type `{1}''";
	private static final String DUPLICATEDFIELDFIRSTASN1 = "Component `{0}'' is already given here";
	private static final String DUBLICATEDFIELDAGAINASN1 = "Duplicated SEQUENCE component `{0}''";
	private static final String WRONGFIELDORDERASN1 = "Component `{0}'' cannot appear after component `{1}'' in SEQUENCE value";
	private static final String UNEXPECTEDFIELDASN1 = "Unexpected component `{0}'' in SEQUENCE value, expecting `{1}''";
	private static final String MISSINGFIELDASN1 = "Mandatory component `{0}'' is missing from SEQUENCE value";

	private static final String NONEXISTENTFIELDERRORTTCN3 = "Reference to a non-existent field `{0}'' in record value for type `{1}''";
	private static final String DUPLICATEDFIELDFIRSTTTCN3 = "Field `{0}'' is already given here";
	private static final String DUBLICATEDFIELDAGAINTTCN3 = "Duplicated record field `{0}''";
	private static final String WRONGFIELDORDERTTCN3 = "Field `{0}'' cannot appear after field `{1}'' in record value";
	private static final String UNEXPECTEDFIELDTTCN3 = "Unexpected field `{0}'' in record value, expecting `{1}''";
	private static final String MISSINGFIELDTTCN3 = "Field `{0}'' is missing from record value";

	private static final String DUPLICATETEMPLATEFIELDFIRST = "Duplicate field `{0}'' in template";
	private static final String DUPLICATETEMPLATEFIELDAGAIN = "Field `{0}'' is already given here";
	private static final String INCORRECTTEMPLATEFIELDORDER = "Field `{0}'' cannot appear after field `{1}''"
			+ " in a template for record type `{2}''";
	private static final String UNEXPECTEDTEMPLATEFIELD = "Unexpected field `{0}'' in record template, expecting `{1}''";
	private static final String NONEXISTENTTEMPLATEFIELDREFERENCE = "Reference to non-existing field `{0}'' in record template for type `{1}''";
	private static final String MISSINGTEMPLATEFIELD = "Field `{0}'' is missing from template for record type `{1}''";

	private static final String NOFFIELDSDONTMATCH = "The number of fields in record/SEQUENCE types must be the same";
	private static final String NOFFIELDSDIMENSIONDONTMATCH = "The number of fields in SEQUENCE types ({0}) and the size of the array ({1})"
			+ " must be the same";
	private static final String BADOPTIONALITY = "The optionality of fields in record/SEQUENCE types must be the same";
	private static final String NOTCOMPATIBLESETSETOF = "set/SET and set of/SET OF types are compatible only"
			+ " with other set/SET and set of/SET OF types";
	private static final String NOTCOMPATIBLEUNIONANYTYPE = "union/CHOICE/anytype types are compatible only"
			+ " with other union/CHOICE/anytype types";

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

	public ASN1_Sequence_Type(final Block aBlock) {
		this.mBlock = aBlock;
	}

	public IASN1Type newInstance() {
		return new ASN1_Sequence_Type(mBlock);
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_ASN1_SEQUENCE;
	}

	@Override
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_TTCN3_SEQUENCE;
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
			ASN1_Sequence_Type temporalType = (ASN1_Sequence_Type) temp;
			if (this == temporalType) {
				return true;
			}
			if (getNofComponents(timestamp) != temporalType.getNofComponents(timestamp)) {
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
				rChain.add(temporalType);
			}
			for (int i = 0, size = getNofComponents(timestamp); i < size; i++) {
				CompField compField = getComponentByIndex(i);
				CompField temporalTypeCompField = temporalType.getComponentByIndex(i);
				IType compFieldType = compField.getType().getTypeRefdLast(timestamp);
				IType temporalTypeCompFieldType = temporalTypeCompField.getType().getTypeRefdLast(timestamp);
				if (compField.isOptional() != temporalTypeCompField.isOptional()) {
					String compFieldName = compField.getIdentifier().getDisplayName();
					String temporalTypeCompFieldName = temporalTypeCompField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldName);
					info.appendOp2Ref("." + temporalTypeCompFieldName);
					info.setOp1Type(compFieldType);
					info.setOp2Type(temporalTypeCompFieldType);
					info.setErrorStr(BADOPTIONALITY);
					return false;
				}
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(compFieldType, temporalTypeCompFieldType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(compFieldType);
				rChain.add(temporalTypeCompFieldType);
				if (!compFieldType.equals(temporalTypeCompFieldType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !compFieldType.isCompatible(timestamp, temporalTypeCompFieldType, infoTemp, lChain, rChain)) {
					String compFieldame = compField.getIdentifier().getDisplayName();
					String temporalTypeCompFieldName = temporalTypeCompField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldame + infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + temporalTypeCompFieldName + infoTemp.getOp2RefStr());
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
				CompField tempTypeComponentField = tempType.getComponentByIndex(i);
				IType compFieldType = compField.getType().getTypeRefdLast(timestamp);
				IType temporalTypeCompFieldType = tempTypeComponentField.getType().getTypeRefdLast(timestamp);
				if (compField.isOptional() != tempTypeComponentField.isOptional()) {
					String compFieldName = compField.getIdentifier().getDisplayName();
					String temporalTypeCompFieldName = tempTypeComponentField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldName);
					info.appendOp2Ref("." + temporalTypeCompFieldName);
					info.setOp1Type(compFieldType);
					info.setOp2Type(temporalTypeCompFieldType);
					info.setErrorStr(BADOPTIONALITY);
					return false;
				}
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(compFieldType, temporalTypeCompFieldType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(compFieldType);
				rChain.add(temporalTypeCompFieldType);
				if (!compFieldType.equals(temporalTypeCompFieldType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !compFieldType.isCompatible(timestamp, temporalTypeCompFieldType, infoTemp, lChain, rChain)) {
					String compFieldName = compField.getIdentifier().getDisplayName();
					String tempTypeCompFieldName = tempTypeComponentField.getIdentifier().getDisplayName();
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
		case TYPE_SEQUENCE_OF: {
			SequenceOf_Type temporalType = (SequenceOf_Type) temp;
			if (!temporalType.isSubtypeCompatible(timestamp, this)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}

			int thisNofComps = getNofComponents(timestamp);
			if (thisNofComps == 0) {
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
				rChain.add(temporalType);
			}
			for (int i = 0; i < thisNofComps; i++) {
				CompField compField = getComponentByIndex(i);
				IType compFieldType = compField.getType().getTypeRefdLast(timestamp);
				IType temporalTypeOfType = temporalType.getOfType().getTypeRefdLast(timestamp);
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(compFieldType, temporalTypeOfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(compFieldType);
				rChain.add(temporalTypeOfType);
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
		case TYPE_ARRAY: {
			Array_Type temporalType = (Array_Type) temp;
			int nofComps = getNofComponents(timestamp);
			if (nofComps == 0) {
				return false;
			}
			long temporalTypeNofComps = temporalType.getDimension().getSize();
			if (nofComps != temporalTypeNofComps) {
				info.setErrorStr(MessageFormat.format(NOFFIELDSDIMENSIONDONTMATCH, nofComps, temporalTypeNofComps));
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
				rChain.add(temporalType);
			}
			for (int i = 0; i < nofComps; i++) {
				CompField compField = getComponentByIndex(i);
				IType compFieldType = compField.getType().getTypeRefdLast(timestamp);
				IType tempTypeElementType = temporalType.getElementType().getTypeRefdLast(timestamp);
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(compFieldType, tempTypeElementType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(compFieldType);
				rChain.add(tempTypeElementType);
				if (!compFieldType.equals(tempTypeElementType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !compFieldType.isCompatible(timestamp, tempTypeElementType, infoTemp, lChain, rChain)) {
					info.appendOp1Ref("." + compField.getIdentifier().getDisplayName() + infoTemp.getOp1RefStr());
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
	public String getOutlineIcon() {
		return "sequence.gif";
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("sequence");
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (components == null) {
			return;
		}

		if (referenceChain.add(this)) {
			for (int i = 0, size = components.getNofComps(); i < size; i++) {
				final CompField field = components.getCompByIndex(i);
				final IType type = field.getType();
				if (!field.isOptional() && type != null) {
					referenceChain.markState();
					type.checkRecursions(timestamp, referenceChain);
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
					return;
				}
			}
		}

		if (components == null) {
			parseBlockSequence();
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
						MessageFormat.format("Indexed assignment notation cannot be used for SEQUENCE type `{0}''",
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

		last.setLastTimeChecked(timestamp);
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
	 *                the expected kind of the value.
	 * @param incompleteAllowed
	 *                wheather incomplete value is allowed or not.
	 * @param implicitOmit
	 *                true if the implicit omit optional attribute was set
	 *                for the value, false otherwise
	 * */
	private void checkThisValueSeq(final CompilationTimeStamp timestamp, final Sequence_Value value, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean implicitOmit, final boolean strElem) {
		Map<String, NamedValue> componentMap = new HashMap<String, NamedValue>();

		CompilationTimeStamp valueTimeStamp = value.getLastTimeChecked();
		if (valueTimeStamp == null || valueTimeStamp.isLess(timestamp)) {
			value.removeGeneratedValues();
		}

		boolean isAsn = value.isAsn();
		boolean inSnyc = true;
		int nofTypeComponents = getNofComponents(timestamp);
		int nofValueComponents = value.getNofComponents();
		int nextIndex = 0;
		CompField lastCompField = null;
		int sequenceIndex = 0;
		for (int i = 0; i < nofValueComponents; i++, sequenceIndex++) {
			NamedValue namedValue = value.getSeqValueByIndex(i);
			Identifier valueId = namedValue.getName();

			if (!hasComponentWithName(valueId)) {
				namedValue.getLocation().reportSemanticError(
						MessageFormat.format(isAsn ? NONEXISTENTFIELDERRORASN1 : NONEXISTENTFIELDERRORTTCN3, namedValue
								.getName().getDisplayName(), getTypename()));
				inSnyc = false;
			} else {
				if (componentMap.containsKey(valueId.getName())) {
					namedValue.getLocation().reportSemanticError(
							MessageFormat.format(isAsn ? DUBLICATEDFIELDAGAINASN1 : DUBLICATEDFIELDAGAINTTCN3,
									valueId.getDisplayName()));
					final Location tempLocation = componentMap.get(valueId.getName()).getLocation();
					tempLocation.reportSingularSemanticError(MessageFormat.format(isAsn ? DUPLICATEDFIELDFIRSTASN1
							: DUPLICATEDFIELDFIRSTTTCN3, valueId.getDisplayName()));
					inSnyc = false;
				} else {
					componentMap.put(valueId.getName(), namedValue);
				}

				CompField componentField = getComponentByName(valueId);
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
						}
					} else {
						CompField field2 = getComponentByIndex(sequenceIndex);
						CompField field2Original = field2;
						boolean isOptional = field2.isOptional();
						if (!isOptional && field2.hasDefault() && defaultAsOptional) {
							isOptional = true;
						}
						while (implicitOmit && sequenceIndex < getNofComponents(timestamp) && componentField != field2
								&& isOptional) {
							++sequenceIndex;
							field2 = getComponentByIndex(sequenceIndex);
						}
						if (sequenceIndex >= getNofComponents(timestamp) || componentField != field2) {
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
			for (int i = 0; i < nofTypeComponents; i++) {
				Identifier id = getComponentByIndex(i).getIdentifier();
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

	private void checkThisNamedTemplateList(final CompilationTimeStamp timestamp, final Named_Template_List templateList,
			final boolean isModified, final boolean implicitOmit) {
		templateList.removeGeneratedValues();

		Map<String, NamedTemplate> componentMap = new HashMap<String, NamedTemplate>();
		int nofTypeComponents = getNofComponents(timestamp);
		int nofTemplateComponents = templateList.getNofTemplates();
		boolean inSync = true;

		CompField lastComponentField = null;
		int nextIndex = 0;
		for (int i = 0; i < nofTemplateComponents; i++) {
			NamedTemplate namedTemplate = templateList.getTemplateByIndex(i);
			Identifier identifier = namedTemplate.getName();
			String templateName = identifier.getName();

			if (hasComponentWithName(identifier)) {
				if (componentMap.containsKey(templateName)) {
					namedTemplate.getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATETEMPLATEFIELDFIRST, identifier.getDisplayName()));
					final Location tempLocation = componentMap.get(templateName).getLocation();
					tempLocation.reportSemanticError(MessageFormat.format(DUPLICATETEMPLATEFIELDAGAIN,
							identifier.getDisplayName()));
					inSync = false;
				} else {
					componentMap.put(templateName, namedTemplate);
				}

				CompField componentField = getComponentByName(identifier);

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
							namedTemplate.getLocation().reportSemanticError(
									MessageFormat.format(INCORRECTTEMPLATEFIELDORDER,
											identifier.getDisplayName(),
											lastComponentField.getIdentifier().getDisplayName(),
											getFullName()));
							inSync = false;
						}
					} else if (strictConstantCheckingSeverity) {
						CompField componentField2 = getComponentByIndex(i);
						if (componentField2 != componentField) {
							if (!componentField2.isOptional() || (componentField2.isOptional() && !implicitOmit)) {
								namedTemplate.getLocation().reportSemanticError(
										MessageFormat.format(UNEXPECTEDTEMPLATEFIELD, identifier
												.getDisplayName(), componentField2.getIdentifier()
												.getDisplayName()));
								inSync = false;
							}
						}
					}
				}

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
						templateList.getLocation().reportSemanticError(
							MessageFormat.format(MISSINGTEMPLATEFIELD,
								identifier.getDisplayName(), getTypename()));
					}
				}
			}
		}
	}

	/** Parses the block as if it were the block of a sequence. */
	private void parseBlockSequence() {
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
			//isErroneous = true;
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

	// This is the same as in ASN1_Set_type
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
			if (components == null) {
				return null;
			}
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

			Expected_Value_type internalExpectation = expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE
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

	// This is the same as in ASN1_Set_type
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
			if (components == null) {
				return false;
			}
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
