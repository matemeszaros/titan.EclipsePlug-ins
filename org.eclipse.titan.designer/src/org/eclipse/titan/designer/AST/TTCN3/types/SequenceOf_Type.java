/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceableElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.IndexedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Indexed_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.PermutationMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SubsetMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SupersetMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class SequenceOf_Type extends AbstractOfType implements IReferenceableElement {
	public static final String SEQOFVALUEEXPECTED = "SEQUENCE OF value was expected";
	public static final String RECORDOFVALUEEXPECTED = "record of value was expected";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for recordof type `{1}''";
	private static final String REDUNDANTLENGTHRESTRICTION = "Redundant usage of length restriction with `omit''";
	public static final String NOTUSEDNOTALLOWED1 = "Not used symbol `-' is not allowed in this context";
	public static final String NOTUSEDNOTALLOWED2 = "Not used symbol `-' cannot be used here"
			+ " because there is no corresponding element in the base template";
	public static final String TOOBIGINDEXTEMPLATE = "An integer value less than `{0}'' was expected for indexing type `{1}''"
			+ " instead of `{2}''";
	public static final String NONNEGATIVEINDEXEXPECTEDTEMPLATE = "A non-negative integer value was expected for indexing type `{0}''"
			+ " instead of `{1}''";
	public static final String DUPLICATEINDEX = "Duplicate index value `{0}'' for component `{1}'' and `{2}''";
	public static final String NONNEGATIVINDEXEXPECTED = "A non-negative integer value was expected as index instead of `{0}''";
	public static final String TOOBIGINDEX = "Integer value `{0}'' is too big for indexing type `{1}''";
	public static final String INTEGERINDEXEXPECTED = "The index should be an integer value";

	private static final String NOTCOMPATIBLESETSETOF = "set/SET and set of/SET OF types are compatible only"
			+ " with other set/SET and set of/SET OF types";
	private static final String NOTCOMPATIBLEUNIONANYTYPE = "union/CHOICE/anytype types are compatible only"
			+ " with other union/CHOICE/anytype types";

	public SequenceOf_Type(final IType ofType) {
		super(ofType);
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_SEQUENCE_OF;
	}

	@Override
	public IASN1Type newInstance() {
		if (getOfType() instanceof ASN1Type) {
			return new SequenceOf_Type(((IASN1Type) getOfType()).newInstance());
		}

		return this;
	}

	@Override
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		IType lastOtherType = otherType.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || lastOtherType.getIsErroneous(timestamp) || this == lastOtherType) {
			return true;
		}

		if (info == null || noStructuredTypeCompatibility) {
			return this == lastOtherType;
		}

		switch (lastOtherType.getTypetype()) {
		case TYPE_ASN1_SEQUENCE: {
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}
			ASN1_Sequence_Type tempType = (ASN1_Sequence_Type) lastOtherType;
			int tempTypeNofComps = tempType.getNofComponents(timestamp);
			if (tempTypeNofComps == 0) {
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
			for (int i = 0; i < tempTypeNofComps; i++) {
				CompField tempTypeCf = tempType.getComponentByIndex(i);
				IType tempTypeCfType = tempTypeCf.getType().getTypeRefdLast(timestamp);
				IType ofType = getOfType().getTypeRefdLast(timestamp);
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(ofType, tempTypeCfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(ofType);
				rChain.add(tempTypeCfType);
				if (!ofType.equals(tempTypeCfType)
						&& !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !ofType.isCompatible(timestamp, tempTypeCfType, infoTemp, lChain, rChain)) {
					if (infoTemp.getOp1RefStr().length() > 0) {
						info.appendOp1Ref("[]");
					}
					info.appendOp1Ref(infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCf.getIdentifier().getDisplayName() + infoTemp.getOp2RefStr());
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
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}
			TTCN3_Sequence_Type tempType = (TTCN3_Sequence_Type) lastOtherType;
			int tempTypeNofComps = tempType.getNofComponents();
			if (tempTypeNofComps == 0) {
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
			for (int i = 0; i < tempTypeNofComps; i++) {
				CompField tempTypeCf = tempType.getComponentByIndex(i);
				IType tempTypeCfType = tempTypeCf.getType().getTypeRefdLast(timestamp);
				IType ofType = getOfType().getTypeRefdLast(timestamp);
				TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(ofType, tempTypeCfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(ofType);
				rChain.add(tempTypeCfType);
				if (!ofType.equals(tempTypeCfType)
						&& !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !ofType.isCompatible(timestamp, tempTypeCfType, infoTemp, lChain, rChain)) {
					if (infoTemp.getOp1RefStr().length() > 0) {
						info.appendOp1Ref("[]");
					}
					info.appendOp1Ref(infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCf.getIdentifier().getDisplayName() + infoTemp.getOp2RefStr());
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
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}
			SequenceOf_Type tempType = (SequenceOf_Type) lastOtherType;
			if (this == tempType) {
				return true;
			}
			IType tempTypeOfType = tempType.getOfType().getTypeRefdLast(timestamp);
			IType ofType = getOfType().getTypeRefdLast(timestamp);
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
			lChain.markState();
			rChain.markState();
			lChain.add(ofType);
			rChain.add(tempTypeOfType);
			TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(ofType, tempTypeOfType, false);
			if (!ofType.equals(tempTypeOfType)
					&& !(lChain.hasRecursion() && rChain.hasRecursion())
					&& !ofType.isCompatible(timestamp, tempTypeOfType, infoTemp, lChain, rChain)) {
				// Record of types can't do anything to check if they're
				// compatible with other record of types in compile-time since
				// we don't have length restrictions here.  No compile-time
				// checks, only add the "[]" to indicate that it's a record of
				// type.
				if (info.getOp1RefStr().length() > 0) {
					info.appendOp1Ref("[]");
				}
				if (info.getOp2RefStr().length() > 0) {
					info.appendOp2Ref("[]");
				}
				info.appendOp1Ref(infoTemp.getOp1RefStr());
				info.appendOp2Ref(infoTemp.getOp2RefStr());
				info.setOp1Type(infoTemp.getOp1Type());
				info.setOp2Type(infoTemp.getOp2Type());
				info.setErrorStr(infoTemp.getErrorStr());
				lChain.previousState();
				rChain.previousState();
				return false;
			}
			info.setNeedsConversion(true);
			lChain.previousState();
			rChain.previousState();
			return true;
		}
		case TYPE_ARRAY: {
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}
			Array_Type tempType = (Array_Type) lastOtherType;
			IType tempTypeElementType = tempType.getElementType().getTypeRefdLast(timestamp);
			IType ofType = getOfType().getTypeRefdLast(timestamp);
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
			lChain.markState();
			rChain.markState();
			lChain.add(ofType);
			rChain.add(tempTypeElementType);
			TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(ofType, tempTypeElementType, false);
			if (!ofType.equals(tempTypeElementType)
					&& !(lChain.hasRecursion() && rChain.hasRecursion())
					&& !ofType.isCompatible(timestamp, tempTypeElementType, infoTemp, lChain, rChain)) {
				if (infoTemp.getOp1RefStr().length() > 0) {
					info.appendOp1Ref("[]");
				}
				info.appendOp1Ref(infoTemp.getOp1RefStr());
				info.appendOp2Ref(infoTemp.getOp2RefStr());
				info.setOp1Type(infoTemp.getOp1Type());
				info.setOp2Type(infoTemp.getOp2Type());
				info.setErrorStr(infoTemp.getErrorStr());
				lChain.previousState();
				rChain.previousState();
				return false;
			}
			info.setNeedsConversion(true);
			lChain.previousState();
			rChain.previousState();
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
		return "record_of.gif";
	}

	@Override
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_RECORDOF;
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

		if (Value_type.UNDEFINED_BLOCK.equals(last.getValuetype())) {
			last = last.setValuetype(timestamp, Value_type.SEQUENCEOF_VALUE);
		}
		if (last.getIsErroneous(timestamp)) {
			return;
		}

		switch (last.getValuetype()) {
		case SEQUENCEOF_VALUE: {
			checkThisValueSequenceOf(timestamp, (SequenceOf_Value) last, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		}
		case SETOF_VALUE: {
			checkThisValueSetOf(timestamp, (SetOf_Value) last, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		}
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(SEQOFVALUEEXPECTED);
			} else {
				value.getLocation().reportSemanticError(RECORDOFVALUEEXPECTED);
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

	/**
	 * Checks the SequenceOf_value kind value against this type.
	 * <p>
	 * Please note, that this function can only be called once we know for sure
	 * that the value is of set-of type.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param value the value to be checked
	 * @param expectedValue the kind of value expected here.
	 * @param incompleteAllowed wheather incomplete value is allowed or not.
	 * @param implicit_omit true if the implicit omit optional attribute was set
	 *            for the value, false otherwise
	 * */
	public void checkThisValueSequenceOf(final CompilationTimeStamp timestamp, final SequenceOf_Value value,
			final Expected_Value_type expectedValue, final boolean incompleteAllowed , final boolean implicit_omit, final boolean strElem) {
		if (value.isIndexed()) {
			boolean checkHoles = Expected_Value_type.EXPECTED_CONSTANT.equals(expectedValue);
			BigInteger maxIndex = BigInteger.valueOf(-1);
			Map<BigInteger, Integer> indexMap = new HashMap<BigInteger, Integer>(value.getNofComponents());
			for (int i = 0, size = value.getNofComponents(); i < size; i++) {
				IValue component = value.getValueByIndex(i);
				IValue index = value.getIndexByIndex(i);
				IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				IValue indexLast = index.getValueRefdLast(timestamp, referenceChain);
				referenceChain.release();

				if (indexLast.getIsErroneous(timestamp) || !Value_type.INTEGER_VALUE.equals(indexLast.getValuetype())) {
					checkHoles = false;
				} else {
					BigInteger tempIndex = ((Integer_Value) indexLast).getValueValue();
					if (tempIndex.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
						index.getLocation().reportSemanticError(MessageFormat.format(
								"A integer value less than `{0}'' was expected for indexing type `{1}'' instead of `{2}''",
								Integer.MAX_VALUE, getTypename(), tempIndex));
						checkHoles = false;
					} else if (tempIndex.compareTo(BigInteger.ZERO) == -1) {
						index.getLocation().reportSemanticError(MessageFormat.format(
								"A non-negative integer value was expected for indexing type `{0}'' instead of `{1}''", getTypename(), tempIndex));
						checkHoles = false;
					} else if (indexMap.containsKey(tempIndex)) {
						index.getLocation().reportSemanticError(MessageFormat.format(
								"Duplicate index value `{0}'' for components {1} and {2}", tempIndex, indexMap.get(tempIndex),  i + 1));
						checkHoles = false;
					} else {
						indexMap.put(tempIndex,  Integer.valueOf(i + 1));
						if (maxIndex.compareTo(tempIndex) == -1) {
							maxIndex = tempIndex;
						}
					}
				}

				component.setMyGovernor(getOfType());
				IValue tempValue2 = getOfType().checkThisValueRef(timestamp, component);
				getOfType().checkThisValue(timestamp, tempValue2,
						new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true, implicit_omit, strElem));
			}
			if (checkHoles) {
				if (maxIndex.compareTo(BigInteger.valueOf(indexMap.size() - 1)) != 0) {
					value.getLocation().reportSemanticError("It's not allowed to create hole(s) in constant values");
				}
			}
		} else {
			for (int i = 0, size = value.getNofComponents(); i < size; i++) {
				IValue component = value.getValueByIndex(i);
				component.setMyGovernor(getOfType());
				if (Value_type.NOTUSED_VALUE.equals(component.getValuetype())) {
					if (!incompleteAllowed) {
						component.getLocation().reportSemanticError(INCOMPLETEPRESENTERROR);
					}
				} else {
					IValue tempValue2 = getOfType().checkThisValueRef(timestamp, component);
					getOfType().checkThisValue(timestamp, tempValue2,
							new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true, implicit_omit, strElem));
				}
			}
		}
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit) {
		registerUsage(template);
		template.setMyGovernor(this);

		switch (template.getTemplatetype()) {
		case OMIT_VALUE:
			if (template.getLengthRestriction() != null) {
				template.getLocation().reportSemanticWarning(REDUNDANTLENGTHRESTRICTION);
			}
			break;
		case PERMUTATION_MATCH: {
			PermutationMatch_Template permutationTemplate = (PermutationMatch_Template) template;
			int nofComponents = permutationTemplate.getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template templateComponent = permutationTemplate.getTemplateByIndex(i); //FIXME: type is ok? It should be ITemplateListItem!
				templateComponent.setMyGovernor(getOfType()); 
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent); //It does not do anything for AllElementsFrom, it is ok
				templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), false, false, true, true, implicitOmit); //it is a special for AllElementsFrom, it is the usual for TemplateBody
			}
			break;
		}
		case SUPERSET_MATCH: {
			SupersetMatch_Template supersetTemplate = (SupersetMatch_Template) template;
			int nofComponents = supersetTemplate.getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template templateComponent = supersetTemplate.getTemplateByIndex(i); //FIXME: type is ok? It should be ITemplateListItem!
				templateComponent.setMyGovernor(getOfType()); 
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent); //It does not do anything for AllElementsFrom, it is ok
				templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), false, false, true, true, implicitOmit); //it is a special for AllElementsFrom, it is the usual for TemplateBody
			}
			break;
		}
		case SUBSET_MATCH: {
			SubsetMatch_Template subsetTemplate = (SubsetMatch_Template) template;
			int nofComponents = subsetTemplate.getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template templateComponent = subsetTemplate.getTemplateByIndex(i); //FIXME: type is ok? It should be ITemplateListItem!
				templateComponent.setMyGovernor(getOfType()); 
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent); //It does not do anything for AllElementsFrom, it is ok
				templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), false, false, true, true, implicitOmit); //it is a special for AllElementsFrom, it is the usual for TemplateBody
			}
			break;
		}
		case TEMPLATE_LIST: {
			Completeness_type completeness = template.getCompletenessConditionSeof(timestamp, isModified);
			Template_List base = null;
			int nofBaseComps = 0;
			if (Completeness_type.PARTIAL.equals(completeness)) {
				ITTCN3Template tempBase = template.getBaseTemplate();
				if (tempBase != null) {
					tempBase = tempBase.getTemplateReferencedLast(timestamp);
				}

				if (tempBase == null) {
					setIsErroneous(true);
					return;
				}

				base = ((Template_List) tempBase);
				nofBaseComps = base.getNofTemplates();
			}

			Template_List templateList = (Template_List) template;
			int nofComponents = templateList.getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template component = templateList.getTemplateByIndex(i);
				component.setMyGovernor(getOfType());
				if (base != null && nofBaseComps > i) {
					component.setBaseTemplate(base.getTemplateByIndex(i));
				} else {
					component.setBaseTemplate(null);
				}

				component = getOfType().checkThisTemplateRef(timestamp, component);

				switch (component.getTemplatetype()) {
				case PERMUTATION_MATCH:
				case SUPERSET_MATCH:
				case SUBSET_MATCH:
				//FIXME: for Complement??? case COMPLEMENTED_LIST: ???
					// the elements of permutation has to be checked by u.seof.ofType
					// the templates within the permutation always have to be complete
					component.checkThisTemplateGeneric(timestamp, this, false, false, true, true, implicitOmit);
					break;
				case TEMPLATE_NOTUSED:
					if (Completeness_type.MUST_COMPLETE.equals(completeness)) {
						component.getLocation().reportSemanticError(NOTUSEDNOTALLOWED1);
					} else if (Completeness_type.PARTIAL.equals(completeness) && i >= nofBaseComps) {
						component.getLocation().reportSemanticError(NOTUSEDNOTALLOWED2);
					}
					break;
				default:
					boolean embeddedModified = (completeness == Completeness_type.MAY_INCOMPLETE)
					|| (completeness == Completeness_type.PARTIAL && i < nofBaseComps);
					component.checkThisTemplateGeneric(timestamp, getOfType(), embeddedModified, false, true, true, implicitOmit);
					break;
				}
			}
			break;
		}
		case INDEXED_TEMPLATE_LIST:	{
			Map<Long, Integer> indexMap = new HashMap<Long, Integer>();
			Indexed_Template_List indexedTemplateList = (Indexed_Template_List) template;
			for (int i = 0, size = indexedTemplateList.getNofTemplates(); i < size; i++) {
				IndexedTemplate indexedTemplate = indexedTemplateList.getIndexedTemplateByIndex(i);
				Value indexValue = indexedTemplate.getIndex().getValue();
				ITTCN3Template templateComponent = indexedTemplate.getTemplate();

				IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				IValue lastValue = indexValue.getValueRefdLast(timestamp, chain);
				chain.release();
				if (Value_type.INTEGER_VALUE.equals(lastValue.getValuetype())) {
					long index = ((Integer_Value) lastValue).getValue();
					if (index > Integer.MAX_VALUE) {
						indexValue.getLocation().reportSemanticError(
								MessageFormat.format(TOOBIGINDEXTEMPLATE, Integer.MAX_VALUE, getTypename(), index));
						indexValue.setIsErroneous(true);
					} else if (index < 0) {
						indexValue.getLocation().reportSemanticError(MessageFormat.format(NONNEGATIVEINDEXEXPECTEDTEMPLATE, getTypename(), index));
						indexValue.setIsErroneous(true);
					} else {
						if (indexMap.containsKey(index)) {
							indexValue.getLocation().reportSemanticError(MessageFormat.format(DUPLICATEINDEX, index, i + 1, indexMap.get(index)));
							indexValue.setIsErroneous(true);
						} else {
							indexMap.put(index, i);
						}
					}
				} else {
					indexValue.getLocation().reportSemanticError(INTEGERINDEXEXPECTED);
					indexValue.setIsErroneous(true);
				}

				templateComponent.setMyGovernor(getOfType());
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent);
				templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), true, false, true, true, implicitOmit);
			}
			break;
		}
		default:
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
			break;
		}
	}

	@Override
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		Expected_Value_type internalExpectation = expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE
				: expectedIndex;

		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			Value indexValue = ((ArraySubReference) subreference).getValue();
			if (indexValue != null) {
				indexValue.setLoweridToReference(timestamp);
				Type_type tempType = indexValue.getExpressionReturntype(timestamp, expectedIndex);

				switch (tempType) {
				case TYPE_INTEGER:
					IValue last = indexValue.getValueRefdLast(timestamp, expectedIndex, refChain);
					if (Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
						Integer_Value lastInteger = (Integer_Value) last;
						if (lastInteger.isNative()) {
							long temp = lastInteger.getValue();
							if (temp < 0) {
								indexValue.getLocation().reportSemanticError(MessageFormat.format(NONNEGATIVINDEXEXPECTED, last));
								indexValue.setIsErroneous(true);
							}
						} else {
							indexValue.getLocation().reportSemanticError(MessageFormat.format(TOOBIGINDEX, indexValue, getTypename()));
							indexValue.setIsErroneous(true);
						}
					}
					break;
				case TYPE_UNDEFINED:
					indexValue.setIsErroneous(true);
					break;
				default:
					indexValue.getLocation().reportSemanticError(INTEGERINDEXEXPECTED);
					indexValue.setIsErroneous(true);
					break;
				}
			}

			if (getOfType() != null) {
				return getOfType().getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain, interruptIfOptional);
			}

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
		builder.append("sequence of ");
		if (getOfType() != null) {
			getOfType().getProposalDescription(builder);
		}
		return builder;
	}

	@Override
	public Declaration resolveReference(final Reference reference, final int subRefIdx, final ISubReference lastSubreference) {
		if (getOfType() == null) {
			return null;
		}

		final IType refdLastOfType = getOfType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (refdLastOfType instanceof IReferenceableElement) {
			return ((IReferenceableElement) refdLastOfType).resolveReference(reference, subRefIdx + 1, lastSubreference);
		}

		return null;
	}
}
