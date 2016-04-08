/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.text.MessageFormat;
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
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * There is no support for Object Assignments in TITAN right now.
 * 
 * @author Kristof Szabados
 * 
 * */
public final class Object_Assignment extends ASN1Assignment {
	public static final String PARAMETERISEDOBJECT = "`{0}'' is a parameterized object assignment";

	/** left. */
	private final ObjectClass objectClass;
	private final ASN1Object object;

	public Object_Assignment(final Identifier id, final Ass_pard assPard, final ObjectClass objectClass, final ASN1Object object) {
		super(id, assPard);
		this.objectClass = objectClass;
		this.object = object;

		if (null != objectClass) {
			objectClass.setFullNameParent(this);
		}
		if (null != object) {
			object.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_OC;
	}

	@Override
	protected ASN1Assignment internalNewInstance(final Identifier identifier) {
		return new Object_Assignment(identifier, null, objectClass.newInstance(), object.newInstance());
	}

	@Override
	public void setRightScope(final Scope rightScope) {
		if (null != object) {
			object.setMyScope(rightScope);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != objectClass) {
			objectClass.setMyScope(scope);
		}
		if (null != object) {
			object.setMyScope(scope);
		}
	}

	/**
	 * Checks and returns the object of this object assignment.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @return the object if this object assignment.
	 * */
	public ASN1Object getObject(final CompilationTimeStamp timestamp) {
		if (null != assPard) {
			location.reportSemanticError(MessageFormat.format(PARAMETERISEDOBJECT, getFullName()));
			return null;
		}

		check(timestamp);

		return object;
	}

	@Override
	public ASN1Object getSetting(final CompilationTimeStamp timestamp) {
		return getObject(timestamp);
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (null != assPard) {
			assPard.check(timestamp);
			// lastTimeChecked = timestamp;
			return;
		}

		if (null != objectClass) {
			objectClass.check(timestamp);
		}

		if (null != object) {
			object.setMyGovernor(objectClass);
			object.check(timestamp);
		}

		// lastTimeChecked = timestamp;
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() > i && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			if (subrefs.size() > i + 1 && null != object) {
				object.addDeclaration(declarationCollector, i + 1);
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
			propCollector.addProposal(identifier, " - " + "Object assignment", ImageCache.getImage(getOutlineIcon()), "Object assignment");
		} else if (subrefs.size() > i + 1 && null != objectClass && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			objectClass.addProposal(propCollector, i + 1);
		}
	}

	@Override
	public String getAssignmentName() {
		return "information object";
	}

	@Override
	public String getOutlineIcon() {
		return "titan.gif";
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		// TODO
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (objectClass != null && !objectClass.accept(v)) {
			return false;
		}
		if (object != null && !object.accept(v)) {
			return false;
		}
		return true;
	}
}
