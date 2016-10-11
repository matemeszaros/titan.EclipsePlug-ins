/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.Reference;

/**
 * Represents "user" extension attribute. It is used on dual-faced ports to mark
 * the external interface of the port type.
 * 
 * @author Kristof Szabados
 * */
public final class UserPortTypeAttribute extends PortTypeAttribute {

	private final Reference reference;

	/** The in-mappings, can be null */
	private TypeMappings inMappings;

	/** The out-mappings, can be null */
	private TypeMappings outMappings;

	public UserPortTypeAttribute(final Reference reference) {
		this.reference = reference;
	}

	@Override
	public PortType_type getPortTypeType() {
		return PortType_type.USER;
	}

	public Reference getReference() {
		return reference;
	}

	public TypeMappings getInMappings() {
		return inMappings;
	}

	public void setInMappings(final TypeMappings mappings) {
		if (inMappings == null) {
			inMappings = mappings;
			return;
		}

		inMappings.copyMappings(mappings);
	}

	public TypeMappings getOutMappings() {
		return outMappings;
	}

	public void setOutMappings(final TypeMappings mappings) {
		if (outMappings == null) {
			outMappings = mappings;
			return;
		}

		outMappings.copyMappings(mappings);
	}
}
