/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IOutlineElement;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Type assignment.
 * 
 * @author Kristof Szabados
 */
public final class Type_Assignment extends ASN1Assignment {
	//private static final String PARAMETERISEDTYPE = "`{0}'' is a parameterized type assignment"; //FIXME: see next occurence!
	private static final String UNKNOWNTYPE = "unknown type assignment";

	/** right. */
	private final IASN1Type type;

	public Type_Assignment(final Identifier id, final Ass_pard ass_pard, final IASN1Type type) {
		super(id, ass_pard);
		this.type = type;

		if (null != type) {
			type.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_TYPE;
	}

	@Override
	protected ASN1Assignment internalNewInstance(final Identifier identifier) {
		return new Type_Assignment(identifier, null, type.newInstance());
	}

	@Override
	public void setRightScope(final Scope right_scope) {
		if (null != type) {
			type.setMyScope(right_scope);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != type) {
			type.setMyScope(scope);
		}
	}

	@Override
	public IASN1Type getType(final CompilationTimeStamp timestamp) {
		if (null != ass_pard) {
			//FIXME: This is commented out because Mark Occurences gives a faulty error marker
			//It has more types...
			//location.reportSemanticError(MessageFormat.format(PARAMETERISEDTYPE, getFullName()));
			return null;
		}

		check(timestamp);

		return type;
	}

	@Override
	public IASN1Type getSetting(final CompilationTimeStamp timestamp) {
		return getType(timestamp);
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (null != ass_pard) {
			ass_pard.check(timestamp);
			return;
		}

		checkTTCNIdentifier();
		if (null != type) {
			type.check(timestamp);

			final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			type.checkRecursions(timestamp, referenceChain);
			referenceChain.release();
		}
	}

	@Override
	public Object[] getOutlineChildren() {
		if (type instanceof IOutlineElement) {
			((IOutlineElement) type).getOutlineChildren();
		}

		return super.getOutlineChildren();
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() > i && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			if (subrefs.size() > i + 1 && null != type) {
				type.addDeclaration(declarationCollector, i + 1);
			} else if (subrefs.size() == i + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
				declarationCollector.addDeclaration(identifier.getDisplayName(), identifier.getLocation(), this);
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
			String proposalKind;
			if (null != type) {
				proposalKind = type.getProposalDescription(new StringBuilder()).toString();
			} else {
				proposalKind = UNKNOWNTYPE;

			}
			propCollector.addProposal(identifier, " - " + proposalKind, ImageCache.getImage(getOutlineIcon()), proposalKind);
		} else if (subrefs.size() > i + 1 && null != type && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			type.addProposal(propCollector, i + 1);
		}
	}

	@Override
	public String getAssignmentName() {
		return "type";
	}

	@Override
	public String getOutlineIcon() {
		if (type instanceof IOutlineElement) {
			return ((IOutlineElement) type).getOutlineIcon();
		}

		return "type.gif";
	}

	// TODO: remove when location is fixed
	@Override
	public Location getLikelyLocation() {
		if (type != null) {
			return Location.interval(super.getLikelyLocation(), type.getLikelyLocation());
		}

		return super.getLikelyLocation();
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (ass_pard != null && !ass_pard.accept(v)) {
			return false;
		}
		if (type != null && !type.accept(v)) {
			return false;
		}
		return true;
	}
}
