/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.Scope;

/**
 * Something that can be in an ObjectSet.
 * 
 * @author Kristof Szabados
 */
public interface IObjectSet_Element {
	IObjectSet_Element newOseInstance();

	void accept(ObjectSetElement_Visitor visitor);

	void setMyScopeOse(Scope scope);
}
