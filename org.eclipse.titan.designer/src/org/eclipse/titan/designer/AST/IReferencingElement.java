/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.declarationsearch.Declaration;

/**
 * This interface represents an element which can be referenced from other parts of the source code.
 * TODO rename to something like reference -able
 * 
 * @author Szabolcs Beres
 */
public interface IReferencingElement {

	/**
	 * Creates a {@link Declaration} from the referenced element.
	 */
	Declaration getDeclaration();
}
