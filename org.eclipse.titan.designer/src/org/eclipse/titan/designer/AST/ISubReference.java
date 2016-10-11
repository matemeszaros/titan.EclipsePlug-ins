/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Defines the interface of TTCN3 or ASN.1 sub-references (reference parts).
 * 
 * @author Kristof Szabados
 * */
public interface ISubReference extends INamedNode, IIncrementallyUpdateable, IIdentifierContainer, IVisitableNode {
	String INVALIDSUBREFERENCE = "Unsupported subreference kind.";

	public enum Subreference_type {
		fieldSubReference,
		arraySubReference,
		parameterisedSubReference
	}

	/**
	 * @return the type of the sub-reference.
	 * */
	Subreference_type getReferenceType();

	/**
	 * @return the identifier of the sub-reference
	 * */
	Identifier getId();

	/**
	 * @return the location, the sub-reference was declared at
	 * */
	Location getLocation();

	/**
	 * Sets the actual scope of this node.
	 *
	 * @param scope the scope to be set
	 * */
	void setMyScope(Scope scope);

	/**
	 * Appends the description to be displayed of the sub-reference to the parameter, and returns it.
	 * Or if the parameter is <code>null</code> creates a new <code>StringBuilder</code> and returns that.
	 * <p>
	 * If the parameter is not <code>null</code> and contains elements, prefixes might also be inserted.
	 *
	 * @param builder the stringBuilder to append the result to, or null
	 * */
	void appendDisplayName(final StringBuilder builder);

	/**
	 *  Handles the incremental parsing of this reference part.
	 *
	 *  @param reparser the parser doing the incremental parsing.
	 *  @param isDamaged true if the location contains the damaged area,
	 *    false if only its' location needs to be updated.
	 * */
	@Override
	void updateSyntax(TTCN3ReparseUpdater reparser, boolean isDamaged) throws ReParseException;
}
