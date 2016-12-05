/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceableElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifier;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public abstract class TTCN3_Set_Seq_Choice_BaseType extends Type implements ITypeWithComponents, IReferenceableElement {
	protected CompFieldMap compFieldMap;

	private boolean componentInternal;

	public TTCN3_Set_Seq_Choice_BaseType(final CompFieldMap compFieldMap) {
		this.compFieldMap = compFieldMap;
		componentInternal = false;
		compFieldMap.setMyType(this);
		compFieldMap.setFullNameParent(this);
	}

	@Override
	public final void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		compFieldMap.setMyScope(scope);
	}

	/** @return the number of components */
	public final int getNofComponents() {
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
	public final CompField getComponentByIndex(final int index) {
		return compFieldMap.fields.get(index);
	}

	/**
	 * Returns whether a component with the name exists or not..
	 *
	 * @param name the name of the element to check
	 * @return true if there is an element with that name, false otherwise.
	 */
	public final boolean hasComponentWithName(final String name) {
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
	public final CompField getComponentByName(final String name) {
		if (compFieldMap.componentFieldMap == null) {
			return null;
		}

		return compFieldMap.componentFieldMap.get(name);
	}

	/**
	 * Returns the identifier of the element at the specified position.
	 *
	 * @param index index of the element to return
	 * @return the identifier of the element at the specified position in this
	 *         list
	 */
	public final Identifier getComponentIdentifierByIndex(final int index) {
		return compFieldMap.fields.get(index).getIdentifier();
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
			Identifier id = subreference.getId();
			CompField compField = compFieldMap.getCompWithName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
								getTypename()));
				return null;
			}

			IType fieldType = compField.getType();
			if (fieldType == null) {
				return null;
			}

			if (interruptIfOptional && compField.isOptional()) {
				return null;
			}

			Expected_Value_type internalExpectation =
					expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : expectedIndex;

			return fieldType.getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain, interruptIfOptional);
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
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
								getTypename()));
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
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),
							getTypename()));
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
	public final boolean isComponentInternal(final CompilationTimeStamp timestamp) {
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
				SingleWithAttribute newAttribute = new SingleWithAttribute(
						temp.getAttributeType(), temp.hasOverride(), null, temp.getAttributeSpecification());
				newSelfAttributes.addAttribute(newAttribute);
			}
		}

		WithAttributesPath encodeAttributePath = null;
		if (newSelfAttributes.getNofElements() > 0) {
			//at least on "encode" was copied; create a context for them.
			encodeAttributePath = new WithAttributesPath();
			encodeAttributePath.setWithAttributes(newSelfAttributes);
			encodeAttributePath.setAttributeParent(withAttributesPath.getAttributeParent());
		}

		for (int i = 0, size = getNofComponents(); i < size; i++) {
			CompField componentField = getComponentByIndex(i);
			IType componentType = componentField.getType();

			componentType.clearWithAttributes();
			if (encodeAttributePath == null) {
				componentType.setAttributeParentPath(withAttributesPath.getAttributeParent());
			} else {
				componentType.setAttributeParentPath(encodeAttributePath);
			}
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
				for (int i = 0, size = getNofComponents(); i < size; i++) {
					CompField componentField = getComponentByIndex(i);
					Identifier componentId = componentField.getIdentifier();

					if (tempSubReference.getReferenceType() == Subreference_type.fieldSubReference
						&& tempSubReference.getId().equals(componentId)) {
						// Found a qualifier whose first identifier matches the component name
						Qualifiers calculatedQualifiers = new Qualifiers();
						calculatedQualifiers.addQualifier(tempQualifier.getQualifierWithoutFirstSubRef());

						SingleWithAttribute tempSingle2 = new SingleWithAttribute(
								tempSingle.getAttributeType(), tempSingle.hasOverride(), calculatedQualifiers, tempSingle.getAttributeSpecification());
						tempSingle2.setLocation(new Location(tempSingle.getLocation()));
						IType componentType = componentField.getType();
						MultipleWithAttributes componentAttributes = componentType.getAttributePath().getAttributes();
						if (componentAttributes == null) {
							componentAttributes = new MultipleWithAttributes();
							componentAttributes.addAttribute(tempSingle2);
							componentType.setWithAttributes(componentAttributes);
						} else {
							componentAttributes.addAttribute(tempSingle2);
						}
						componentFound = true;
					}
				}

				if (!componentFound) {
					if (tempSubReference.getReferenceType() == Subreference_type.arraySubReference) {
						tempQualifier.getLocation().reportSemanticError(Qualifier.INVALID_INDEX_QUALIFIER);
					} else {
						tempQualifier.getLocation().reportSemanticError(MessageFormat.format(
								Qualifier.INVALID_FIELD_QUALIFIER, tempSubReference.getId().getDisplayName()));
					}
				}
			}
		}
	}

	@Override
	public final void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		//MarkerHandler.markAllSemanticMarkersForRemoval(this);//TODO: Check its place!!
		lastTimeChecked = timestamp;
		componentInternal = false;
		isErroneous = false;

		parseAttributes(timestamp);

		compFieldMap.check(timestamp);

		for (int i = 0, size = getNofComponents(); i < size; i++) {
			IType type = getComponentByIndex(i).getType();
			if (type != null && type.isComponentInternal(timestamp)) {
				componentInternal = true;
				break;
			}
		}

		if (constraints != null) {
			constraints.check(timestamp);
		}

		checkSubtypeRestrictions(timestamp);
	}

	@Override
	public final void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		if (typeSet.contains(this)) {
			return;
		}

		typeSet.add(this);
		for (int i = 0, size = getNofComponents(); i < size; i++) {
			IType type = getComponentByIndex(i).getType();
			if (type != null && type.isComponentInternal(timestamp)) {
				type.checkComponentInternal(timestamp, typeSet, operation);
			}
		}
		typeSet.remove(this);
	}

	@Override
	public final Object[] getOutlineChildren() {
		return compFieldMap.getOutlineChildren();
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
	public final void addProposal(final ProposalCollector propCollector, final int i) {
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
					propCollector.addProposal(compField.getIdentifier(), " - " + proposalKind, ImageCache.getImage(getOutlineIcon()), proposalKind);
					IType type = compField.getType();
					if (type != null && compField.getIdentifier().equals(subreference.getId())) {
						type = type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
						type.addProposal(propCollector, i + 1);
					}
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
	public final void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
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
					declarationCollector.addDeclaration(compField.getIdentifier().getDisplayName(), compField.getIdentifier().getLocation(), this);
				}
			}
		}
	}

	@Override
	public final void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean handled = false;

			if (compFieldMap != null
					&& reparser.envelopsDamage(compFieldMap.getLocation())) {
				try {
					compFieldMap.updateSyntax(reparser, true);
				} catch (ReParseException e) {
					e.decreaseDepth();
					throw e;
				}

				reparser.updateLocation(compFieldMap.getLocation());
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

		reparser.updateLocation(compFieldMap.getLocation());
		compFieldMap.updateSyntax(reparser, false);

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
		compFieldMap.getEnclosingField(offset, rf);
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (compFieldMap != null) {
			compFieldMap.findReferences(referenceFinder, foundIdentifiers);
		}
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

	@Override
	public Identifier getComponentIdentifierByName(final Identifier identifier) {
		if(identifier == null){
			return null;
		}
		final CompField cf = getComponentByName(identifier.getName());
		return cf == null ? null : cf.getIdentifier();
	}

	@Override
	public Declaration resolveReference(final Reference reference, final int subRefIdx, final ISubReference lastSubreference) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		int localIndex = subRefIdx;
		while (localIndex < subreferences.size() && subreferences.get(localIndex) instanceof ArraySubReference) {
			++localIndex;
		}

		if (localIndex == subreferences.size()) {
			return null;
		}

		final CompField compField = getComponentByName(subreferences.get(localIndex).getId().getName());
		if (compField == null) {
			return null;
		}
		if (subreferences.get(localIndex) == lastSubreference) {
			return Declaration.createInstance(getDefiningAssignment(), compField.getIdentifier());
		}
		
		final IType compFieldType = compField.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (compFieldType instanceof IReferenceableElement) {
			final Declaration decl = ((IReferenceableElement) compFieldType).resolveReference(reference, localIndex + 1, lastSubreference);
			return decl != null ? decl : Declaration.createInstance(getDefiningAssignment(), compField.getIdentifier());
		}

		return null;
	}
}
