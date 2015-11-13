/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IIdentifierContainer;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a single attribute qualifier.
 * 
 * @author Kristof Szabados
 * */
public final class Qualifier implements ILocateableNode, IIncrementallyUpdateable, IIdentifierContainer, IVisitableNode {
	public static final String INVALID_INDEX_QUALIFIER = "Invalid field qualifier [-]";
	public static final String INVALID_FIELD_QUALIFIER = "Invalid field qualifier {0}";

	/**
	 * The location of the whole qualifier. This location encloses the
	 * qualifier fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	private List<ISubReference> subReferences;

	/** Definition is set during semantic check or null */
	private Definition definition = null;

	private Qualifier() {
		subReferences = new ArrayList<ISubReference>(1);
	}

	public Qualifier(final ISubReference subReference) {
		subReferences = new ArrayList<ISubReference>(1);
		if (subReference != null) {
			subReferences.add(subReference);
		}
	}

	public void setDefinition(final Definition definition) {
		this.definition = definition;
	}

	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		for (ISubReference subref : subReferences) {
			subref.appendDisplayName(sb);
		}
		return sb.toString();
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
	 * Adds a subreference to the end of the list of stored subreferences.
	 * 
	 * @param subReference
	 *                the subreference to be added
	 * */
	public void addSubReference(final ISubReference subReference) {
		if (subReference != null) {
			subReferences.add(subReference);
		}
	}

	/**
	 * Adds a subreference to the beginning of the list of stored
	 * subreferences.
	 * 
	 * @param subReference
	 *                the subreference to be added
	 * */
	public void addSubReferenceFront(final ISubReference subReference) {
		if (subReference != null) {
			subReferences.add(0, subReference);
		}
	}

	/** @return the number of subreferences handled here */
	public int getNofSubReferences() {
		return subReferences.size();
	}

	/**
	 * Returns the subreference at the specified position.
	 * 
	 * @param index
	 *                the index of the subreference to return.
	 * @return the subreference at the given index.
	 * */
	public ISubReference getSubReferenceByIndex(final int index) {
		return subReferences.get(index);
	}

	/**
	 * Creates a qualifier which is almost a copy of the actual one, but
	 * does not have it's first subreference.
	 * 
	 * @return the created qualifier
	 * */
	public Qualifier getQualifierWithoutFirstSubRef() {
		Qualifier temp = new Qualifier();
		temp.setLocation(new Location(getLocation()));
		for (int i = 1; i < subReferences.size(); i++) {
			temp.addSubReference(subReferences.get(i));
		}
		return temp;
	}

	/**
	 * Handles the incremental parsing of this list of qualifiers.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (subReferences != null) {
			for (int i = 0, size = subReferences.size(); i < size; i++) {
				reparser.updateLocation(subReferences.get(i).getLocation());
			}
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (definition != null && subReferences != null) {
			// TODO: the following for() does not work because
			// qualifiers were not semantically analyzed
			for (ISubReference sr : subReferences) {
				sr.findReferences(referenceFinder, foundIdentifiers);
			}
			if (referenceFinder.fieldId != null) {
				// we are searching for a field of a type
				IType t = definition.getType(CompilationTimeStamp.getBaseTimestamp());
				if (t == null) {
					return;
				}
				List<IType> typeArray = new ArrayList<IType>();
				Reference reference = new Reference(null);
				reference.addSubReference(new FieldSubReference(definition.getIdentifier()));
				for (ISubReference sr : subReferences) {
					reference.addSubReference(sr);
				}
				reference.setLocation(location);
				reference.setMyScope(definition.getMyScope());
				boolean success = t.getFieldTypesAsArray(reference, 1, typeArray);
				if (!success) {
					// TODO: maybe a partially erroneous reference could be searched too
					return;
				}
				if (subReferences.size() != typeArray.size()) {
					ErrorReporter.INTERNAL_ERROR();
					return;
				}
				for (int i = 0; i < subReferences.size(); i++) {
					if (typeArray.get(i) == referenceFinder.type && !(subReferences.get(i) instanceof ArraySubReference)
							&& subReferences.get(i).getId().equals(referenceFinder.fieldId)) {
						foundIdentifiers.add(new Hit(subReferences.get(i).getId()));
					}
				}
			}
		}
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (subReferences != null) {
			for (ISubReference sr : subReferences) {
				if (!sr.accept(v)) {
					return false;
				}
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
