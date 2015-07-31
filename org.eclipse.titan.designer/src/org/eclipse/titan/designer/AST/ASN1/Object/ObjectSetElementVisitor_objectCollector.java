/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ObjectClass;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.AST.ASN1.ObjectSetElement_Visitor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * ObjectSetElement Visitor, object collector.
 * 
 * @author Kristof Szabados
 */
public final class ObjectSetElementVisitor_objectCollector extends ObjectSetElement_Visitor {
	private static final String OBJECTOFCLASSEXPECTED = "Objects of objectclass `{0}'' are expected; `{1}'' is object of class `{2}''";

	private final ObjectClass_Definition governor;
	private final Set<Object> visitedElements = new HashSet<Object>();
	private ASN1Objects objects;
	private final CompilationTimeStamp timestamp;

	public ObjectSetElementVisitor_objectCollector(final ObjectSet parent, final CompilationTimeStamp timestamp) {
		super(parent.getLocation());
		governor = parent.getMyGovernor().getRefdLast(timestamp, null);
		objects = new ASN1Objects();
		this.timestamp = timestamp;
	}

	public ObjectSetElementVisitor_objectCollector(final Location location, final ObjectClass governor, final CompilationTimeStamp timestamp) {
		super(location);
		this.governor = governor.getRefdLast(timestamp, null);
		objects = new ASN1Objects();
		this.timestamp = timestamp;
	}

	@Override
	public void visitObject(final ASN1Object p) {
		final Object_Definition object = p.getRefdLast(timestamp, null);
		if (object.getIsErroneous(timestamp)) {
			return;
		}
		if (visitedElements.contains(object)) {
			return;
		}
		if (!governor.equals(object.getMyGovernor().getRefdLast(timestamp, null))) {
			location.reportSemanticError(MessageFormat.format(OBJECTOFCLASSEXPECTED, governor.getFullName(), p.getFullName(), p
					.getMyGovernor().getRefdLast(timestamp, null).getFullName()));
			return;
		}
		visitedElements.add(object);
		objects.addObject(object);
	}

	@Override
	public void visitObjectSetReferenced(final Referenced_ObjectSet p) {
		visitObjectSet(p, false);
	}

	public void visitObjectSet(final ObjectSet p, final boolean force) {
		final ObjectSet_definition os = p.getRefdLast(timestamp, null);
		final ObjectClass tempGovernor = os.getMyGovernor();

		if (null == tempGovernor) {
			return;
		}

		if (!governor.equals(tempGovernor.getRefdLast(timestamp, null))) {
			p.getLocation().reportSemanticError(MessageFormat.format(OBJECTOFCLASSEXPECTED, governor.getFullName(), p.getFullName(),
					tempGovernor.getRefdLast(timestamp, null).getFullName()));
			return;
		}
		if (visitedElements.contains(os)) {
			if (!force) {
				return;
			}
		} else {
			visitedElements.add(os);
		}
		final ASN1Objects otherObjects = os.getObjs();

		otherObjects.trimToSize();
		for (int i = 0; i < otherObjects.getNofObjects(); i++) {
			visitObject(otherObjects.getObjectByIndex(i));
		}
	}

	public ASN1Objects getObjects() {
		return objects;
	}

	public ASN1Objects giveObjects() {
		final ASN1Objects temp = objects;
		objects = null;
		return temp;
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		// TODO
		return true;
	}
}
