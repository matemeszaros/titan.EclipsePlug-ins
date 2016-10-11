/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

/**
 * Represents the address attribute that can be assigned to port types, in order
 * to enable the support of the address type inside the port too.
 * 
 * @author Kristof Szabados
 * */
public final class AddressPortTypeAttribute extends PortTypeAttribute {

	@Override
	public PortType_type getPortTypeType() {
		return PortType_type.ADDRESS;
	}
}
