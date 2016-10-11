/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.Identifier;

/**
 * Represents the minimum required version the TITAN which is needed to
 * correctly compile the file.
 * 
 * @author Csaba Raduly
 */
public final class TitanVersionAttribute extends ModuleVersionAttribute {
	public TitanVersionAttribute(final Identifier version) {
		super(version, false);
	}

	@Override
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.TITANVERSION;
	}
}
