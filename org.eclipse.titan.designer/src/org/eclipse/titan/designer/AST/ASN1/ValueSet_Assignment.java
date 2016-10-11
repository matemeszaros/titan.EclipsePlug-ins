/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
// TODO handling of the right value is not implemented in the compiler either
public final class ValueSet_Assignment extends ASN1Assignment {
	private static final String UNKNOWNASSIGNMENT = "unknown value assignment";

	/** left. */
	protected final IASN1Type type;
	/** right */
	private final Block mBlock;

	public ValueSet_Assignment(final Identifier id, final Ass_pard assPard, final IASN1Type type, final Block aBlock) {
		super(id, assPard);
		this.type = type;
		this.mBlock = aBlock;

		if (null != type) {
			type.setFullNameParent(this);
		}
		
		if (null != aBlock) {
			aBlock.setFullNameParent(this);
		}
	}

	@Override
	protected ASN1Assignment internalNewInstance(final Identifier identifier) {
		return new ValueSet_Assignment(identifier, null, type.newInstance(), mBlock);
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_VS;
	}

	@Override
	public void setRightScope(final Scope rightScope) {
		//Do nothing

	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != type) {
			type.setMyScope(scope);
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() > i && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			if (subrefs.size() > i + 1 && null != type) {
				type.addDeclaration(declarationCollector, i + 1);
			} else if (subrefs.size() == i + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
				declarationCollector.addDeclaration(this);
			}
		}
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i) {
			return;
		}

		if (subrefs.size() == i + 1 && identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			propCollector.addProposal(identifier, " - " + UNKNOWNASSIGNMENT, ImageCache.getImage(getOutlineIcon()), UNKNOWNASSIGNMENT);
		} else if (subrefs.size() > i + 1 && null != type && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			type.addProposal(propCollector, i + 1);
		}
	}

	@Override
	public String getAssignmentName() {
		return "value set";
	}

	@Override
	public String getOutlineIcon() {
		return "titan.gif";
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (type != null && !type.accept(v)) {
			return false;
		}
		return true;
	}
}
