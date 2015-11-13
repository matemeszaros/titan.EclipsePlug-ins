/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
class DefinitionDeclaration extends Declaration {

	private Assignment ass;

	public DefinitionDeclaration(final Assignment ass) {
		this.ass = ass;
	}

	@Override
	public List<Hit> getReferences(final Module module) {
		try {
			ReferenceFinder referenceFinder = new ReferenceFinder(ass);
			final List<Hit> result = referenceFinder.findReferencesInModule(module);
			if (ass.getMyScope().getModuleScope() == module) {
				result.add(new Hit(ass.getIdentifier()));
			}

			return result;
		} catch (final IllegalArgumentException e) {
			return new ArrayList<ReferenceFinder.Hit>();
		}
	}

	@Override
	public ReferenceFinder getReferenceFinder(final Module module) {
		try {
			ReferenceFinder referenceFinder = new ReferenceFinder(ass);
			return referenceFinder;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public boolean shouldMarkOccurrences() {
		return ass.shouldMarkOccurrences();
	}

	@Override
	public Identifier getIdentifier() {
		return ass.getIdentifier();
	}
	
	@Override
	public Assignment getAssignment() {
		return ass;
	}
}
