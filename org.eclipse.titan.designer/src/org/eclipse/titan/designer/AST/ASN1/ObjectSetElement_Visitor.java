/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ASN1.Object.Referenced_ObjectSet;

/**
 * ObjectSetElement visitor.
 * 
 * @author Kristof Szabados
 */
public abstract class ObjectSetElement_Visitor extends ASTNode {

	protected Location location;

	public ObjectSetElement_Visitor(final Location location) {
		this.location = location;
	}

	public abstract void visitObject(ASN1Object p);

	public abstract void visitObjectSetReferenced(Referenced_ObjectSet p);

}
