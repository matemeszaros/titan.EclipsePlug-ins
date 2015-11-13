/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3;

import java.util.List;

import org.eclipse.titan.designer.AST.Location;

/**
 * This interface represents an AST node whose syntax ends on optional element.
 * In this case an already correct AST node might be extended by a change directly following itself.
 *
 * This interface should only be used by the incremental parser.
 * 
 * @author Kristof Szabados
 * */
public interface IAppendableSyntax {
	/**
	 * Returns the list of tokens of optional elements that might follow the actual node, to complete it.
	 * (for example a with attribute for a type definition, if it does not have one already).
	 *
	 * Might return null, if no optional element can follow.
	 *
	 * @return the list of tokens that can immediately follow.
	 * */
	List<Integer> getPossibleExtensionStarterTokens();

	/**
	 * Returns the list of tokens of optional elements that might prefix the actual node.
	 * (for example a private visibility attribute for a definition, if it does not have one already).
	 *
	 * Might return null, if no optional element can follow.
	 *
	 * @return the list of tokens that can immediately follow.
	 * */
	List<Integer> getPossiblePrefixTokens();

	/**
	 * @return the location of this element.
	 * */
	Location getLocation();
}
