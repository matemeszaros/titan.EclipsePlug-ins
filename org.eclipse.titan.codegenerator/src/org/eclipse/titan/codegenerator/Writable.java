/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

/**
 * Writable interface
 * A hook for the SourceCode class.
 * @see SourceCode
 */
public interface Writable {
	// TODO : remove, as no null value is allowed to be a template value
	Writable NULL = (code, indent) -> code.append("null /* Foobar! */");

	/**
	 * Write self to the given source, with given base indentation level.
	 * @param code the SourceCode this object should write itself to
	 * @param indent the base indentation level
	 * @return the modified SourceCode
	 */
	SourceCode write(SourceCode code, int indent);
}
