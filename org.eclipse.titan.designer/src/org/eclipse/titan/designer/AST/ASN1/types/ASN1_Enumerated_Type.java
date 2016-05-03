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
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.types.EnumItem;
import org.eclipse.titan.designer.AST.TTCN3.values.Enumerated_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTracker;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ASN1_Enumerated_Type extends ASN1Type implements ITypeWithComponents {
	private static final String DUPLICATEENUMERATEDREPEATED = "Duplicate ENUMERATE identifier: `{0}'' was declared here again";
	private static final String TTCN3ENUMERATEDVALUEEXPECTED = "Enumerated value was expected";
	private static final String ASN1ENUMERATEDVALUEEXPECTED = "ENUMERATED value was expected";
	public static final String DUPLICATEDENUMERATIONVALUEFIRST = "Value {0} is already assigned to `{1}''";
	public static final String DUPLICATEDENUMERATIONVALUEREPEATED = "Duplicate numeric value {0} for enumeration `{1}''";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for enumerated type";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for enumerated type";

	private final Block mBlock;
	protected ASN1_Enumeration enumerations;
	private Map<String, EnumItem> nameMap;
	private Integer firstUnused;

	public ASN1_Enumerated_Type(final Block aBlock) {
		this.mBlock = aBlock;
	}

	public IASN1Type newInstance() {
		return new ASN1_Enumerated_Type(mBlock);
	}

	@Override
	public final Type_type getTypetype() {
		return Type_type.TYPE_ASN1_ENUMERATED;
	}

	@Override
	public final Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_TTCN3_ENUMERATED;
	}

	@Override
	public final StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("enumerated");
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (enumerations != null) {
			if(enumerations.enumItems1 != null) {
				enumerations.enumItems1.setMyScope(scope);
			}
			if(enumerations.enumItems2 != null) {
				enumerations.enumItems2.setMyScope(scope);
			}
		}
	}

	@Override
	public final boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		IType temp = otherType.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return this == temp;
	}

	@Override
	public final boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		return isCompatible(timestamp, type, null, null, null);
	}

	@Override
	public final String getTypename() {
		return getFullName();
	}

	@Override
	public final String getOutlineIcon() {
		return "enumeration.gif";
	}

	// TODO: remove this when the location is properly set
	@Override
	public Location getLikelyLocation() {
		return location;
	}

	/**
	 * Check if an enumeration item exists with the provided name.
	 * 
	 * @param identifier
	 *                the name to look for
	 * 
	 * @return true it there is an item with that name, false otherwise.
	 * */
	public final boolean hasEnumItemWithName(final Identifier identifier) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return nameMap.containsKey(identifier.getName());
	}

	/**
	 * Returns an enumeration item with the provided name.
	 * 
	 * @param identifier
	 *                the name to look for
	 * 
	 * @return the enumeration item with the provided name, or null.
	 * */
	public final EnumItem getEnumItemWithName(final Identifier identifier) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return nameMap.get(identifier.getName());
	}

	@Override
	public final void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		if (null != myScope) {
			Module module = myScope.getModuleScope();
			if (null != module) {
				if (module.getSkippedFromSemanticChecking()) {
					return;
				}
			}
		}
		isErroneous = false;

		if (null == enumerations) {
			parseBlockEnumeration();
		}

		if (isErroneous || null == enumerations) {
			return;
		}

		/* check duplications and set missing values */
		firstUnused = Integer.valueOf(0);
		nameMap = new HashMap<String, EnumItem>();
		Map<Integer, EnumItem> valueMap = new HashMap<Integer, EnumItem>();
		if (null != enumerations.enumItems1) {
			List<EnumItem> enumItems = enumerations.enumItems1.getItems();
			for (EnumItem item : enumItems) {
				checkEnumItem(timestamp, item, false, valueMap);
			}

			// set the default values
			while (valueMap.containsKey(firstUnused)) {
				firstUnused++;
			}
			for (EnumItem item : enumItems) {
				if (null == item.getValue() || !item.isOriginal()) {
					Integer_Value tempValue = new Integer_Value(firstUnused.longValue());
					tempValue.setLocation(item.getLocation());
					item.setValue(tempValue);
					valueMap.put(firstUnused, item);
					while (valueMap.containsKey(firstUnused)) {
						firstUnused++;
					}
				}
			}
		}

		if (null != enumerations.enumItems2) {
			List<EnumItem> enumItems = enumerations.enumItems2.getItems();
			for (EnumItem item : enumItems) {
				checkEnumItem(timestamp, item, true, valueMap);
			}
		}

		if (null != constraints) {
			constraints.check(timestamp);
		}
	}

	/**
	 * Helper function for checking a single enumeration item. Checks if the
	 * name of the item is not a duplicate, and its value is in correct
	 * order. Also for items after the ellipsis if the value is missing a
	 * new one is assigned.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param item
	 *                the enumeration item to work on.
	 * @param afterEllipsis
	 *                true if the enumeration item is after the ellipsis.
	 * @param valueMap
	 *                a value map so that the correctness of the item's
	 *                value can be checked.
	 * */
	private final void checkEnumItem(final CompilationTimeStamp timestamp, final EnumItem item, final boolean afterEllipsis,
			final Map<Integer, EnumItem> valueMap) {
		Identifier itemID = item.getId();
		if (nameMap.containsKey(itemID.getName())) {
			nameMap.get(itemID.getName())
					.getLocation()
					.reportSingularSemanticError(
							MessageFormat.format(Assignments.DUPLICATEDEFINITIONFIRST, itemID.getDisplayName()));
			itemID.getLocation().reportSemanticError(MessageFormat.format(DUPLICATEENUMERATEDREPEATED, itemID.getDisplayName()));
		} else {
			nameMap.put(itemID.getName(), item);
		}

		if (!itemID.getHasValid(Identifier_type.ID_TTCN)) {
			itemID.getLocation().reportSemanticWarning(MessageFormat.format(ASN1Assignment.UNREACHABLE, itemID.getDisplayName()));
		}

		Value value = item.getValue();
		if (!item.isOriginal()) {
			if (afterEllipsis) {
				while (valueMap.containsKey(firstUnused)) {
					firstUnused++;
				}

				valueMap.put(firstUnused, item);
				// optimization: if the same value was already
				// assigned, there is no need to create it
				// again.
				if (null == value || ((Integer_Value) value).getValue() != firstUnused) {
					Integer_Value tempValue = new Integer_Value(firstUnused.longValue());
					tempValue.setLocation(item.getLocation());
					item.setValue(tempValue);
				}
			}
			return;
		}

		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = value.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (last.getIsErroneous(timestamp)) {
			return;
		}

		if (!Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
			value.getLocation().reportSemanticError(
					MessageFormat.format("INTEGER value was expected for enumeration `{0}''", itemID.getDisplayName()));
			return;
		}

		Integer_Value temp = (Integer_Value) last;
		if (!temp.isNative()) {
			value.getLocation().reportSemanticError(
					MessageFormat.format(
							"The numeric value of enumeration `{0}'' ({1}) is too large for being represented in memory",
							itemID.getDisplayName(), temp.getValueValue()));
			return;
		}

		Integer enumValue = Integer.valueOf(temp.intValue());
		if (afterEllipsis) {
			if (enumValue >= firstUnused) {
				valueMap.put(enumValue, item);
				while (valueMap.containsKey(firstUnused)) {
					firstUnused++;
				}
			} else {
				value.getLocation().reportSemanticError(
					MessageFormat.format(
						"ENUMERATED values shall be monotonically growing after the ellipsis: the value of `{0}'' must be at least {1} instead of {2}",
										itemID.getDisplayName(), firstUnused, enumValue));
			}
		} else {
			if (valueMap.containsKey(enumValue)) {
				final Location tempLocation = valueMap.get(enumValue).getLocation();
				tempLocation.reportSingularSemanticError(MessageFormat.format(DUPLICATEDENUMERATIONVALUEFIRST, enumValue, valueMap
						.get(enumValue).getId().getDisplayName()));
				value.getLocation().reportSemanticError(
						MessageFormat.format(DUPLICATEDENUMERATIONVALUEREPEATED, enumValue, itemID.getDisplayName()));
				setIsErroneous(true);
			} else {
				valueMap.put(enumValue, item);
			}
		}
	}

	@Override
	public final IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value) {
		IValue temp = value;
		if (Value_type.REFERENCED_VALUE.equals(value.getValuetype())) {
			// we are not able to parse lower identifier values as default values
			// so the parsed reference needs to be converted.
			Reference reference = ((Referenced_Value)value).getReference();
			if (reference.getModuleIdentifier() == null && reference.getSubreferences().size() == 1) {
				Identifier identifier = reference.getId();
				temp = new Enumerated_Value(identifier);
				temp.setMyGovernor(this);
				temp.setFullNameParent(this);
				temp.setMyScope(value.getMyScope());
				return temp;
			}
		}

		if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(temp.getValuetype())) {
			if (nameMap != null && nameMap.containsKey(((Undefined_LowerIdentifier_Value) temp).getIdentifier().getName())) {
				temp = temp.setValuetype(timestamp, Value_type.ENUMERATED_VALUE);
				temp.setMyGovernor(this);
				temp.setFullNameParent(this);
				temp.setMyScope(value.getMyScope());
				return temp;
			}
		}

		return super.checkThisValueRef(timestamp, value);
	}

	@Override
	public final void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
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
		case ENUMERATED_VALUE:
			// if it is an enumerated value, than it was already
			// checked to be categorized.
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(value.isAsn() ? ASN1ENUMERATEDVALUEEXPECTED : TTCN3ENUMERATEDVALUEEXPECTED);
			value.setIsErroneous(true);
		}
	}

	@Override
	public final void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit) {
		registerUsage(template);
		template.setMyGovernor(this);

		template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName()));

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(LENGTHRESTRICTIONNOTALLOWED);
		}
	}

	@Override
	public final IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
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

	private void parseBlockEnumeration() {
		if(null == mBlock) {
			return;
		}

		final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mBlock);
		if (null == parser) {
			return;
		}

		enumerations = parser.pr_special_Enumerations().enumeration;
		final List<SyntacticErrorStorage> errors = parser.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			enumerations = null;
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}

		if (enumerations != null) {
			if(enumerations.enumItems1 != null) {
				enumerations.enumItems1.setFullNameParent(this);
				enumerations.enumItems1.setMyScope(getMyScope());
			}
			if(enumerations.enumItems2 != null) {
				enumerations.enumItems2.setFullNameParent(this);
				enumerations.enumItems2.setMyScope(getMyScope());
			}
		}
	}
	
	@Override
	public final void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subreferences = propCollector.getReference().getSubreferences();
		if (subreferences.size() <= i || enumerations == null) {
			return;
		}

		ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() <= i + 1) {
				String referenceName = subreference.getId().getName();
				if (enumerations.enumItems1 != null) {
					List<EnumItem> enumItems = enumerations.enumItems1.getItems();
					for (EnumItem item : enumItems) {
						Identifier itemID = item.getId();
						if (itemID.getName().startsWith(referenceName)) {
							propCollector.addProposal(itemID, " - " + "named integer",
									ImageCache.getImage(getOutlineIcon()), "named integer");
						}
					}
				}
				if (enumerations.enumItems2 != null) {
					List<EnumItem> enumItems = enumerations.enumItems2.getItems();
					for (EnumItem item : enumItems) {
						Identifier itemID = item.getId();
						if (itemID.getName().startsWith(referenceName)) {
							propCollector.addProposal(itemID, " - " + "named integer",
									ImageCache.getImage(getOutlineIcon()), "named integer");
						}
					}
				}
			}
		}
	}

	@Override
	public final void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() <= i + 1) {
				String referenceName = subreference.getId().getName();
				if (enumerations.enumItems1 != null) {
					List<EnumItem> enumItems = enumerations.enumItems1.getItems();
					for (EnumItem item : enumItems) {
						Identifier itemID = item.getId();
						if (itemID.getName().startsWith(referenceName)) {
							declarationCollector.addDeclaration(itemID.getDisplayName(), itemID.getLocation(), this);
						}
					}
				}
				if (enumerations.enumItems2 != null) {
					List<EnumItem> enumItems = enumerations.enumItems2.getItems();
					for (EnumItem item : enumItems) {
						Identifier itemID = item.getId();
						if (itemID.getName().startsWith(referenceName)) {
							declarationCollector.addDeclaration(itemID.getDisplayName(), itemID.getLocation(), this);
						}
					}
				}
			}
		}
	}

	@Override
	public final void getEnclosingField(final int offset, final ReferenceFinder rf) {
		if (enumerations == null) {
			return;
		}

		if (enumerations.enumItems1 != null) {
			for (EnumItem enumItem : enumerations.enumItems1.getItems()) {
				if (enumItem.getLocation().containsOffset(offset)) {
					rf.type = this;
					rf.fieldId = enumItem.getId();
					return;
				}
			}
		}
		if (enumerations.enumItems2 != null) {
			for (EnumItem enumItem : enumerations.enumItems2.getItems()) {
				if (enumItem.getLocation().containsOffset(offset)) {
					rf.type = this;
					rf.fieldId = enumItem.getId();
					return;
				}
			}
		}
	}

	@Override
	public final void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (enumerations != null) {
			if (enumerations.enumItems1 != null) {
				enumerations.enumItems1.findReferences(referenceFinder, foundIdentifiers);
			}
			if (enumerations.enumItems2 != null) {
				enumerations.enumItems2.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	protected final boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (enumerations != null) {
			if (enumerations.enumItems1 != null && !enumerations.enumItems1.accept(v)) {
				return false;
			}
			if (enumerations.enumItems2 != null && !enumerations.enumItems2.accept(v)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public final Identifier getComponentIdentifierByName(final Identifier identifier) {
		final EnumItem enumItem = getEnumItemWithName(identifier);
		return enumItem == null ? null : enumItem.getId();
	}
}
