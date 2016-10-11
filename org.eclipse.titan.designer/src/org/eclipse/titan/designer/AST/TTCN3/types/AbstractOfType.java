/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifier;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.templates.SingleLenghtRestriction;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.Length_ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public abstract class AbstractOfType extends ASN1Type {

	public static final String INCOMPLETEPRESENTERROR = "Not used symbol `-' is not allowed in this context";

	private static final String FULLNAMEPART = ".oftype";

	private final IType ofType;
	private boolean componentInternal;

	public AbstractOfType(final IType ofType) {
		this.ofType = ofType;

		if (ofType != null) {
			ofType.setFullNameParent(this);
		}
		componentInternal = false;
	}

	public IType getOfType() {
		return ofType;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (ofType == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (ofType != null) {
			ofType.setMyScope(scope);
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

	/**
	 * Checks that the provided type is sub-type compatible with the actual
	 * set of type.
	 * <p>
	 * In case of sequence/set/array this means that the number of their
	 * fields fulfills the length restriction of the set of type.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle
	 * @param other
	 *                the type to check against.
	 * 
	 * @return true if they are sub-type compatible, false otherwise.
	 * */
	public boolean isSubtypeCompatible(final CompilationTimeStamp timestamp, final IType other) {
		if (subType == null || other == null) {
			return true;
		}

		long nofComponents;
		switch (other.getTypetype()) {
		case TYPE_ASN1_SEQUENCE:
			nofComponents = ((ASN1_Sequence_Type) other).getNofComponents(timestamp);
			break;
		case TYPE_TTCN3_SEQUENCE:
			nofComponents = ((TTCN3_Sequence_Type) other).getNofComponents();
			break;
		case TYPE_ASN1_SET:
			nofComponents = ((ASN1_Set_Type) other).getNofComponents(timestamp);
			break;
		case TYPE_TTCN3_SET:
			nofComponents = ((TTCN3_Set_Type) other).getNofComponents();
			break;
		case TYPE_SEQUENCE_OF:
		case TYPE_SET_OF:
			if (other.getSubtype() == null) {
				return true;
			}

			return subType.isCompatible(timestamp, other.getSubtype());
		case TYPE_ARRAY: {
			ArrayDimension dimension = ((Array_Type) other).getDimension();
			if (dimension.getIsErroneous(timestamp)) {
				return false;
			}

			nofComponents = dimension.getSize();
			break;
		}
		default:
			return false;
		}

		List<ParsedSubType> tempRestrictions = new ArrayList<ParsedSubType>(1);
		Integer_Value length = new Integer_Value(nofComponents);
		tempRestrictions.add(new Length_ParsedSubType(new SingleLenghtRestriction(length)));
		SubType tempSubtype = new SubType(getSubtypeType(), this, tempRestrictions, null);
		tempSubtype.check(timestamp);
		return subType.isCompatible(timestamp, tempSubtype);
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
	public boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return componentInternal;
	}

	@Override
	public void parseAttributes(final CompilationTimeStamp timestamp) {
		checkDoneAttribute(timestamp);

		if (!hasVariantAttributes(timestamp)) {
			return;
		}

		/* This will be useful when processing of the variant attributes assigned to the whole type is implemented
		ArrayList<SingleWithAttribute> realAttributes = withAttributesPath.getRealAttributes(timestamp);
		for (int i = 0; i < realAttributes.size(); i++) {
			SingleWithAttribute tempSingle = realAttributes.get(i);
			if (Attribute_Type.Variant_Attribute.equals(tempSingle.getAttributeType())
					&& (tempSingle.getQualifiers() == null || tempSingle.getQualifiers().getNofQualifiers() == 0)) {
				...
			}
		}*/

		MultipleWithAttributes selfAttributes = withAttributesPath.getAttributes();
		if (selfAttributes == null) {
			return;
		}

		MultipleWithAttributes newSelfAttributes = new MultipleWithAttributes();
		for (int i = 0; i < selfAttributes.getNofElements(); i++) {
			SingleWithAttribute temp = selfAttributes.getAttribute(i);
			if (Attribute_Type.Encode_Attribute.equals(temp.getAttributeType())) {
				SingleWithAttribute newAttribute = new SingleWithAttribute(temp.getAttributeType(), temp.hasOverride(), null,
						temp.getAttributeSpecification());
				newSelfAttributes.addAttribute(newAttribute);
			}
		}

		WithAttributesPath encodeAttributePath = null;
		if (newSelfAttributes.getNofElements() > 0) {
			// at least on "encode" was copied; create a context for
			// them.
			encodeAttributePath = new WithAttributesPath();
			encodeAttributePath.setWithAttributes(newSelfAttributes);
			encodeAttributePath.setAttributeParent(withAttributesPath.getAttributeParent());
		}

		ofType.clearWithAttributes();
		if (encodeAttributePath == null) {
			ofType.setAttributeParentPath(withAttributesPath.getAttributeParent());
		} else {
			ofType.setAttributeParentPath(encodeAttributePath);
		}

		// Distribute the attributes with qualifiers to the components
		for (int j = 0; j < selfAttributes.getNofElements(); j++) {
			SingleWithAttribute tempSingle = selfAttributes.getAttribute(j);
			Qualifiers tempQualifiers = tempSingle.getQualifiers();
			if (tempQualifiers == null || tempQualifiers.getNofQualifiers() == 0) {
				continue;
			}

			for (int k = 0, kmax = tempQualifiers.getNofQualifiers(); k < kmax; k++) {
				Qualifier tempQualifier = tempQualifiers.getQualifierByIndex(k);
				if (tempQualifier.getNofSubReferences() == 0) {
					continue;
				}

				ISubReference tempSubReference = tempQualifier.getSubReferenceByIndex(0);
				boolean componentFound = false;

				if (tempSubReference.getReferenceType() == Subreference_type.arraySubReference) {
					// Found a qualifier whose first
					// identifier matches the component name
					Qualifiers calculatedQualifiers = new Qualifiers();
					calculatedQualifiers.addQualifier(tempQualifier.getQualifierWithoutFirstSubRef());

					SingleWithAttribute tempSingle2 = new SingleWithAttribute(tempSingle.getAttributeType(),
							tempSingle.hasOverride(), calculatedQualifiers, tempSingle.getAttributeSpecification());
					tempSingle2.setLocation(new Location(tempSingle.getLocation()));
					MultipleWithAttributes componentAttributes = ofType.getAttributePath().getAttributes();
					if (componentAttributes == null) {
						componentAttributes = new MultipleWithAttributes();
						componentAttributes.addAttribute(tempSingle2);
						ofType.setWithAttributes(componentAttributes);
					} else {
						componentAttributes.addAttribute(tempSingle2);
					}
					componentFound = true;
				}

				if (!componentFound) {
					if (tempSubReference.getReferenceType() == Subreference_type.arraySubReference) {
						tempQualifier.getLocation().reportSemanticError(Qualifier.INVALID_INDEX_QUALIFIER);
					} else {
						tempQualifier.getLocation().reportSemanticError(
								MessageFormat.format(Qualifier.INVALID_FIELD_QUALIFIER, tempSubReference.getId()
										.getDisplayName()));
					}
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
		if (myScope != null) {
			Module module = myScope.getModuleScope();
			if (module != null && module.getSkippedFromSemanticChecking()) {
				lastTimeChecked = timestamp;
				return;
			}
		}
		componentInternal = false;
		isErroneous = false;

		parseAttributes(timestamp);

		if (ofType == null) {
			setIsErroneous(true);
		} else {
			ofType.setParentType(this);
			ofType.check(timestamp);
			if (!isAsn()) {
				ofType.checkEmbedded(timestamp, ofType.getLocation(), true, "embedded into another type");
			}
			componentInternal = ofType.isComponentInternal(timestamp);
		}

		if (constraints != null) {
			constraints.check(timestamp);
		}

		checkSubtypeRestrictions(timestamp);
	}

	@Override
	public void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		if (typeSet.contains(this)) {
			return;
		}

		if (ofType != null && ofType.isComponentInternal(timestamp)) {
			typeSet.add(this);
			ofType.checkComponentInternal(timestamp, typeSet, operation);
			typeSet.remove(this);
		}
	}

	/**
	 * Checks the SequenceOf_value kind value against this type.
	 * SequenceOf_value kinds have to be converted before calling this
	 * function.
	 * <p>
	 * Please note, that this function can only be called once we know for
	 * sure that the value is of set-of type.
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
	public void checkThisValueSetOf(final CompilationTimeStamp timestamp, final SetOf_Value value, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean implicitOmit, final boolean strElem) {
		if (value.isIndexed()) {
			boolean checkHoles = Expected_Value_type.EXPECTED_CONSTANT.equals(expectedValue);
			BigInteger maxIndex = BigInteger.valueOf(-1);
			Map<BigInteger, Integer> indexMap = new HashMap<BigInteger, Integer>(value.getNofComponents());
			for (int i = 0, size = value.getNofComponents(); i < size; i++) {
				IValue component = value.getValueByIndex(i);
				Value index = value.getIndexByIndex(i);
				IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				IValue indexLast = index.getValueRefdLast(timestamp, referenceChain);
				referenceChain.release();

				if (indexLast.getIsErroneous(timestamp) || !Value_type.INTEGER_VALUE.equals(indexLast.getValuetype())) {
					checkHoles = false;
				} else {
					BigInteger tempIndex = ((Integer_Value) indexLast).getValueValue();
					if (tempIndex.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
						index.getLocation()
								.reportSemanticError(
										MessageFormat.format(
												"A integer value less than `{0}'' was expected for indexing type `{1}'' instead of `{2}''",
												Integer.MAX_VALUE, getTypename(), tempIndex));
						checkHoles = false;
					} else if (tempIndex.compareTo(BigInteger.ZERO) == -1) {
						index.getLocation().reportSemanticError(MessageFormat.format(
								"A non-negative integer value was expected for indexing type `{0}'' instead of `{1}''", getTypename(), tempIndex));
						checkHoles = false;
					} else if (indexMap.containsKey(tempIndex)) {
						index.getLocation().reportSemanticError(
								MessageFormat.format("Duplicate index value `{0}'' for components {1} and {2}",
										tempIndex, indexMap.get(tempIndex), i + 1));
						checkHoles = false;
					} else {
						indexMap.put(tempIndex, Integer.valueOf(i + 1));
						if (maxIndex.compareTo(tempIndex) == -1) {
							maxIndex = tempIndex;
						}
					}
				}

				component.setMyGovernor(getOfType());
				IValue tempValue2 = getOfType().checkThisValueRef(timestamp, component);
				getOfType().checkThisValue(timestamp, tempValue2,
						new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true, implicitOmit, strElem));
			}
			if (checkHoles && maxIndex.compareTo(BigInteger.valueOf(indexMap.size() - 1)) != 0) {
				value.getLocation().reportSemanticError("It's not allowed to create hole(s) in constant values");
			}
		} else {
			for (int i = 0, size = value.getNofComponents(); i < size; i++) {
				IValue component = value.getValueByIndex(i);
				component.setMyGovernor(getOfType());
				if (Value_type.NOTUSED_VALUE.equals(component.getValuetype()) && !incompleteAllowed) {
					component.getLocation().reportSemanticError(INCOMPLETEPRESENTERROR);
				} else {
					IValue tempValue2 = getOfType().checkThisValueRef(timestamp, component);
					getOfType().checkThisValue(timestamp, tempValue2,
							new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true, implicitOmit, strElem));
				}
			}
		}

		value.setLastTimeChecked(timestamp);
	}

	@Override
	public boolean getSubrefsAsArray(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final List<Integer> subrefsArray, final List<IType> typeArray) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return true;
		}
		ISubReference subreference = subreferences.get(actualSubReference);
		if (subreference.getReferenceType() != Subreference_type.arraySubReference) {
			ErrorReporter.INTERNAL_ERROR();
			return false;
		}

		Value indexValue = ((ArraySubReference) subreference).getValue();
		if (indexValue == null) {
			ErrorReporter.INTERNAL_ERROR();
			return false;
		}
		IValue last = indexValue.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_CONSTANT, null);
		if (last == null) {
			ErrorReporter.INTERNAL_ERROR();
			return false;
		}
		if (last.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_CONSTANT) != Type_type.TYPE_INTEGER) {
			return false;
		}
		if (!Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
			return false;
		}
		Integer_Value lastInteger = (Integer_Value) last;
		if (lastInteger.isNative()) {
			int fieldIndex = (int) lastInteger.getValue();
			if (fieldIndex < 0) {
				return false;
			}
			subrefsArray.add(fieldIndex);
			typeArray.add(this);
		} else {
			return false;
		}
		if (ofType == null) {
			ErrorReporter.INTERNAL_ERROR();
			return false;
		}
		return ofType.getSubrefsAsArray(timestamp, reference, actualSubReference + 1, subrefsArray, typeArray);
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
								indexValue.getLocation().reportSemanticError(
										MessageFormat.format(SequenceOf_Type.NONNEGATIVINDEXEXPECTED, last));
								indexValue.setIsErroneous(true);
							}
						} else {
							indexValue.getLocation().reportSemanticError(
									MessageFormat.format(SequenceOf_Type.TOOBIGINDEX, indexValue, getTypename()));
							indexValue.setIsErroneous(true);
						}
					}
					break;
				case TYPE_UNDEFINED:
					indexValue.setIsErroneous(true);
					break;
				default:
					indexValue.getLocation().reportSemanticError(SequenceOf_Type.INTEGERINDEXEXPECTED);
					indexValue.setIsErroneous(true);
					break;
				}
			}

			if (getOfType() != null) {
				return getOfType().getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain,
						interruptIfOptional);
			}

			return null;
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
	public boolean getFieldTypesAsArray(final Reference reference, final int actualSubReference, final List<IType> typeArray) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return true;
		}
		ISubReference subreference = subreferences.get(actualSubReference);
		if (subreference.getReferenceType() != Subreference_type.arraySubReference) {
			return false;
		}
		typeArray.add(this);
		if (ofType == null) {
			return false;
		}
		return ofType.getFieldTypesAsArray(reference, actualSubReference + 1, typeArray);
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subreferences = propCollector.getReference().getSubreferences();
		if (subreferences.size() < i) {
			return;
		} else if (subreferences.size() == i) {
			ISubReference subreference = subreferences.get(i - 1);
			if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
				String candidate = ((FieldSubReference) subreference).getId().getDisplayName();
				propCollector.addTemplateProposal(candidate, new Template(candidate + "[index]", candidate + " with index",
						propCollector.getContextIdentifier(), candidate + "[${index}]", false),
						TTCN3CodeSkeletons.SKELETON_IMAGE);
			}
			return;
		}

		ISubReference subreference = subreferences.get(i);
		if (Subreference_type.arraySubReference.equals(subreference.getReferenceType()) && subreferences.size() > i + 1 && ofType != null) {
			ofType.addProposal(propCollector, i + 1);
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		ISubReference subreference = subreferences.get(i);
		if (Subreference_type.arraySubReference.equals(subreference.getReferenceType()) && subreferences.size() > i + 1 && ofType != null) {
			ofType.addDeclaration(declarationCollector, i + 1);
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean handled = false;

			if (ofType instanceof IIncrementallyUpdateable && reparser.envelopsDamage(ofType.getLocation())) {
				((IIncrementallyUpdateable) ofType).updateSyntax(reparser, true);
				reparser.updateLocation(ofType.getLocation());
				handled = true;
			}

			if (subType != null) {
				subType.updateSyntax(reparser, false);
				handled = true;
			}

			if (handled) {
				return;
			}

			throw new ReParseException();
		}

		if (ofType instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) ofType).updateSyntax(reparser, false);
			reparser.updateLocation(ofType.getLocation());
		} else if (ofType != null) {
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
	public void getEnclosingField(final int offset, final ReferenceFinder rf) {
		if (ofType == null) {
			return;
		}

		ofType.getEnclosingField(offset, rf);
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (ofType != null) {
			ofType.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (ofType != null && !ofType.accept(v)) {
			return false;
		}
		return true;
	}
}
