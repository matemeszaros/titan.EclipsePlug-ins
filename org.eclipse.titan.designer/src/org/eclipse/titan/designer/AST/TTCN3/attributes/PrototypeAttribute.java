/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function.EncodingPrototype_type;

/**
 * Represents an encoding/decoding prototype that is set on a function or
 * external function. Using this attribute the compiler will be able to use the
 * function/external function it was assigned to as a type mapping component.
 * 
 * @author Kristof Szabados
 * */
public final class PrototypeAttribute extends ExtensionAttribute {

	private final EncodingPrototype_type prototypeType;

	public PrototypeAttribute(final EncodingPrototype_type type) {
		prototypeType = type;
	}

	@Override
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.PROTOTYPE;
	}

	public EncodingPrototype_type getPrototypeType() {
		return prototypeType;
	}
}
