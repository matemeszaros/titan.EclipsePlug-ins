/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IReferencingElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * A member of an enumeration.
 * 
 * @author Kristof Szabados
 * */
public final class EnumItem extends ASTNode implements ILocateableNode, IIncrementallyUpdateable, IReferencingElement {
	private static final String PROPOSAL_KIND = "enumeration";

	private final Identifier identifier;
	private Value value;
	private final boolean originalValue;

	/**
	 * The location of the whole item. This location encloses the item fully, as
	 * it is used to report errors to.
	 **/
	private Location location;
	
	private Location commentLocation = null;

	public EnumItem(final Identifier identifier, final Value value) {
		this.identifier = identifier;
		this.value = value;
		this.originalValue = value != null;
		location = NULL_Location.INSTANCE;

		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	/**
	 * @return The location of the comment assigned to this definition.
	 *  Or null if none.
	 * */
	@Override
	public Location getCommentLocation() {
		return commentLocation;
	}

	/**
	 * Sets the location of the comment that belongs to this definition.
	 *
	 * @param commentLocation the location of the comment
	 * */
	public void setCommentLocation(final Location commentLocation) {
		this.commentLocation = commentLocation;
	}
	/**
	 * @return the identifier of this enumeration item
	 * */
	public Identifier getId() {
		return identifier;
	}

	/**
	 * @return whether the value was originally assigned in the enumeration
	 * 	or later at semantic checking.
	 * */
	public boolean isOriginal() {
		return originalValue;
	}
	
	/**
	 * @return the value of this enumeration item
	 * */
	public Value getValue() {
		return value;
	}

	/**
	 * Sets the default value of the enumeration item.
	 *
	 * @param value the value to be set
	 * */
	public void setValue(final Value value) {
		this.value = value;
	}

	/**
	 * Does the semantic checking of the enumerations.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		// Do nothing
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * Adds this enumeration item if it is valid.
	 *
	 * @param propCollector the proposal collector to add the proposal to, and
	 *            used to get more information
	 * */
	public void addProposal(final ProposalCollector propCollector) {
		propCollector.addProposal(identifier, " - " + PROPOSAL_KIND, ImageCache.getImage("enumeration.gif"), PROPOSAL_KIND);
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * Adds this enumeration element if possible
	 *
	 * @param declarationCollector the declaration collector to add the
	 *            declaration to, and used to get more information.
	 * @param i index, used to indentify which element of the reference (used by
	 *            the declaration collector) should be checked.
	 * */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() == i + 1 && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			declarationCollector.addDeclaration(identifier.getDisplayName(), identifier.getLocation(), this);
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (identifier!=null && !identifier.accept(v)) {
			return false;
		}
		if (value!=null && !value.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	public Declaration getDeclaration() {
		if (getMyScope() == null) {
			return null;
		}
		Module module = getMyScope().getModuleScope();
		Assignment assignment = module.getEnclosingAssignment(getLocation().getOffset());
		IType type = assignment.getType(CompilationTimeStamp.getBaseTimestamp());
		
		if(type instanceof ITypeWithComponents) {
			Identifier id = ((ITypeWithComponents) type).getComponentIdentifierByName(getId());
			return Declaration.createInstance(assignment, id);
		}

		return null;
	}
}
