/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

/**
 * Represents an extension attribute that can be assigned to a port type.
 * 
 * @author Kristof Szabados
 * */
public abstract class PortTypeAttribute extends ExtensionAttribute {

	public enum PortType_type {
		INTERNAL, ADDRESS, PROVIDER, USER
	}

	@Override
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.PORTTYPE;
	}

	public abstract PortType_type getPortTypeType();
}
