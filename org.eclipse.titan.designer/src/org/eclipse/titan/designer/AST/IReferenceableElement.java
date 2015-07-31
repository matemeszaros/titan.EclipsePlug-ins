/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.declarationsearch.Declaration;

/**
 * @author Szabolcs Beres
 * */
public interface IReferenceableElement {

	/**
	 * Resolves the given reference with this type.
	 * 
	 * @param reference The reference to resolve.
	 * @param subRefIdx The index of the sub-reference which belongs to this type.
	 * @param lastSubreference The last sub-reference to resolve.
	 * @return The resolved declaration or <code>null</code>, if the reference can not be resolved by this type.
	 */
	Declaration resolveReference(final Reference reference, final int subRefIdx, final ISubReference lastSubreference);

}
