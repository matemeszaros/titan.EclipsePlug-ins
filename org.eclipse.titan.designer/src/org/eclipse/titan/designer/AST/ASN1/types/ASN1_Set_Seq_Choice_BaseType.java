/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.IReferenceableElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public abstract class ASN1_Set_Seq_Choice_BaseType extends ASN1Type implements ITypeWithComponents, IReferenceableElement {

	protected Block mBlock;
	protected CTs_EE_CTs components;

	@Override
	public String getTypename() {
		return getFullName();
	}

	/**
	 * Returns the element with the specified name.
	 * 
	 * @param identifier
	 *                the name of the element to return
	 * @return the element with the specified name in this list, or null if
	 *         none exists.
	 */
	public CompField getComponentByName(final Identifier identifier) {
		if (null == components) {
			return null;
		}

		return components.getCompByName(identifier);
	}

	/** @return the number of components */
	public int getNofComponents(final CompilationTimeStamp timestamp) {
		if (null == components || lastTimeChecked == null) {
			check(timestamp);
		}

		return components.getNofComps();
	}

	/**
	 * Returns whether an element is stored with the specified name.
	 * 
	 * @param identifier
	 *                the name of the element to return
	 * @return true if an element with the provided name exists in the list,
	 *         false otherwise
	 */
	public boolean hasComponentWithName(final Identifier identifier) {
		if (null == components) {
			return false;
		}

		return components.hasCompWithName(identifier);
	}

	/**
	 * Returns the element at the specified position.
	 * 
	 * @param index
	 *                index of the element to return
	 * @return the element at the specified position in this list
	 */
	public CompField getComponentByIndex(final int index) {
		return components.getCompByIndex(index);
	}

	/**
	 * Returns the identifier of the element at the specified position.
	 * 
	 * @param index
	 *                index of the element to return
	 * @return the identifier of the element at the specified position in
	 *         this list
	 */
	public Identifier getComponentIdentifierByIndex(final int index) {
		return components.getCompByIndex(index).getIdentifier();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != components) {
			components.setMyScope(scope);
		}
	}

	// TODO: remove this when the location is properly set
	@Override
	public Location getLikelyLocation() {
		if (mBlock != null) {
			return mBlock.getLocation();
		} else {
			return location;
		}
	}

	@Override
	public void getEnclosingField(final int offset, final ReferenceFinder rf) {
		if (components == null) {
			return;
		}

		components.getEnclosingField(offset, rf);
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (components != null) {
			components.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (components != null && !components.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	public Identifier getComponentIdentifierByName(final Identifier identifier) {
		final CompField cf = getComponentByName(identifier);
		return cf == null ? null : cf.getIdentifier();
	}

	@Override
	public Declaration resolveReference(final Reference reference, int subRefIdx, final ISubReference lastSubreference) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		while (subRefIdx < subreferences.size() && subreferences.get(subRefIdx) instanceof ArraySubReference) {
			++subRefIdx;
		}

		if (subRefIdx == subreferences.size()) {
			return null;
		}

		final CompField compField = getComponentByName(subreferences.get(subRefIdx).getId());
		final IType compFieldType = compField.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

		if (subreferences.get(subRefIdx) == lastSubreference) {
			return Declaration.createInstance(getDefiningAssignment(), compField.getIdentifier());
		}

		if (compFieldType instanceof IReferenceableElement) {
			Declaration decl = ((IReferenceableElement) compFieldType).resolveReference(reference, subRefIdx + 1, lastSubreference);
			return decl != null ? decl : Declaration.createInstance(getDefiningAssignment(), compField.getIdentifier());
		}

		return null;
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
				CompField compField = components.getCompByName(subreference.getId());
				if (compField == null) {
					return;
				}
				IType type = compField.getType();
				if (type != null) {
					type.addDeclaration(declarationCollector, i + 1);
				}
			} else {
				// final part of the reference
				List<CompField> compFields = components.getComponentsWithPrefix(subreference.getId().getName());
				for (CompField compField : compFields) {
					declarationCollector.addDeclaration(compField.getIdentifier().getDisplayName(),
							compField.getIdentifier().getLocation(), this);
				}
			}
		}
	}

	@Override
	public Object[] getOutlineChildren() {
		if (components == null) {
			return new Object[] {};
		}

		return components.getOutlineChildren();
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
		if (subreferences.size() <= i || components == null) {
			return;
		}

		ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on

				CompField compField = components.getCompByName(subreference.getId());
				if (compField == null) {
					return;
				}
				IType type = compField.getType();
				if (type != null) {
					type.addProposal(propCollector, i + 1);
				}
			} else {
				// final part of the reference
				List<CompField> compFields = components.getComponentsWithPrefix(subreference.getId().getName());
				for (CompField compField : compFields) {
					String proposalKind = compField.getType().getProposalDescription(new StringBuilder()).toString();
					propCollector.addProposal(compField.getIdentifier(), " - " + proposalKind,
							ImageCache.getImage(getOutlineIcon()), proposalKind);
				}
			}
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
		case fieldSubReference: {
			Identifier id = subreference.getId();
			CompField compField = components.getCompByName(id);
			if (compField == null) {
				return false;
			}
			IType fieldType = compField.getType();
			if (fieldType == null) {
				return false;
			}
			typeArray.add(this);
			return fieldType.getFieldTypesAsArray(reference, actualSubReference + 1, typeArray);
		}
		case parameterisedSubReference:
			return false;
		default:
			return false;
		}
	}
}
