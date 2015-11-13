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
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * ComponentType/regular (Contains only a Component).
 * 
 * @author Kristof Szabados
 */
public final class RegularComponentType extends ComponentType {

	private final CompField componentField;

	public RegularComponentType(final CompField componentField) {
		this.componentField = componentField;

		componentField.setFullNameParent(this);
	}

	@Override
	public void setMyScope(final Scope scope) {
		componentField.setMyScope(scope);
	}

	@Override
	public int getNofComps() {
		return 1;
	}

	@Override
	public CompField getCompByIndex(final int index) {
		if (0 == index) {
			return componentField;
		}

		// FATAL_ERROR
		return null;
	}

	@Override
	public boolean hasCompWithName(final Identifier identifier) {
		if (null == identifier) {
			return false;
		}

		return identifier.getName().equals(componentField.getIdentifier().getName());
	}

	@Override
	public CompField getCompByName(final Identifier identifier) {
		if (null == identifier) {
			return null;
		}

		if (identifier.getName().equals(componentField.getIdentifier().getName())) {
			return componentField;
		}

		// FATAL_ERROR
		return null;
	}

	@Override
	public void trCompsof(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain, final boolean is_set) {
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentField != null) {
			componentField.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (componentField != null && !componentField.accept(v)) {
			return false;
		}
		return true;
	}
}
