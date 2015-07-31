/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.IType;

/**
 * An extension attribute representing the component types that a given
 * component type extends.
 * 
 * @author Kristof Szabados
 * */
public final class ExtensionsAttribute extends ExtensionAttribute {

	private final Types types;

	public ExtensionsAttribute(final Types types) {
		this.types = types;
	}

	@Override
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.EXTENDS;
	}

	public int getNofTypes() {
		return types.getNofTypes();
	}

	public IType getType(final int index) {
		return types.getType(index);
	}
}
