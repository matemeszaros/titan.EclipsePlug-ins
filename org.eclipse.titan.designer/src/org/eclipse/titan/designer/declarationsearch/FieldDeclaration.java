/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.declarationsearch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;

/**
 * @author Szabolcs Beres
 * */
class FieldDeclaration extends Declaration {
	private Assignment ass;
	private Identifier fieldId;

	public FieldDeclaration(final Assignment ass, final Identifier id) {
		this.ass = ass;
		this.fieldId = id;
	}

	@Override
	public List<Hit> getReferences(final Module module) {
		try {
			ReferenceFinder referenceFinder = new ReferenceFinder(ass);
			referenceFinder.fieldId = fieldId;
			final List<Hit> result = referenceFinder.findReferencesInModule(module);
			if (ass.getMyScope().getModuleScope() == module) {
				result.add(new Hit(fieldId));
			}
			return result;
		} catch (final IllegalArgumentException e) {
			return new ArrayList<ReferenceFinder.Hit>();
		}
	}

	@Override
	public boolean shouldMarkOccurrences() {
		return ass.shouldMarkOccurrences();
	}

	@Override
	public Identifier getIdentifier() {
		return fieldId;
	}

	@Override
	public ReferenceFinder getReferenceFinder(Module module) {
		try {
			return new ReferenceFinder(ass);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public Assignment getAssignment() {
		return ass;
	}
}
