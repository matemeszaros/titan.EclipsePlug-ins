/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

/**
 * Represents the internal attribute, that can be assigned to a port type. If
 * set, the code for the port type will be automatically generated. But than the
 * port can only be used for internal communication (between TTCN-3 components)
 * 
 * @author Kristof Szabados
 * */
public final class InternalPortTypeAttribute extends PortTypeAttribute {

	@Override
	public PortType_type getPortTypeType() {
		return PortType_type.INTERNAL;
	}
}
