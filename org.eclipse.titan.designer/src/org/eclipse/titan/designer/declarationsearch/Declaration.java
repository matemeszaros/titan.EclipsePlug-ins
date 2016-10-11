/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.declarationsearch;

import java.util.List;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;

/**
 * This class represents a declaration which can be referenced. This class is
 * responsible for creating instances of the subclasses. The subclasses are not
 * accessible outside this package.
 * 
 * @author Szabolcs Beres
 */
public abstract class Declaration {

	/**
	 * Factory method to create a {@link Declaration} from an
	 * {@link Assignment}.
	 */
	public static Declaration createInstance(final Assignment assignment) {
		return new DefinitionDeclaration(assignment);
	}

	/**
	 * Factory method to create a {@link Declaration} from an
	 * {@link Assignment} and an {@link Identifier}. Returns null, if the
	 * assignment or the identifier is null This declaration represents a
	 * field of a structured type.
	 */
	public static Declaration createInstance(final Assignment ass, final Identifier id) {
		if (ass == null || id == null) {
			return null;
		}
		return new FieldDeclaration(ass, id);
	}

	/**
	 * Factory method to create a {@link Declaration} from an {@link Module}
	 * .
	 */
	public static Declaration createInstance(final Module module) {
		if (module == null) {
			return null;
		}
		return new ModuleDeclaration(module);
	}

	/**
	 * Returns the references of this declaration.
	 * 
	 * @param module
	 *                The module to find the references.
	 * @return The found references. This list includes the definition
	 *         itself as well if it is in the same module.
	 */
	public abstract List<Hit> getReferences(final Module module);

	/**
	 * Returns the references of this declaration.
	 * 
	 * @param module
	 *                The module to find the references.
	 * @return The Reference Finder of given module.
	 */
	public abstract ReferenceFinder getReferenceFinder(final Module module);

	/**
	 * Returns true if the references of this declaration should be marked.
	 * This can be configured on the preference pages.
	 * 
	 * @return <code>true</code> if the occurrences should be marked,
	 *         <code>false</code> otherwise
	 */
	public abstract boolean shouldMarkOccurrences();

	/**
	 * Returns the identifier of this declaration.
	 * 
	 * @return The identifier of the declaration.
	 */
	public abstract Identifier getIdentifier();
	
	public abstract Assignment getAssignment();

}