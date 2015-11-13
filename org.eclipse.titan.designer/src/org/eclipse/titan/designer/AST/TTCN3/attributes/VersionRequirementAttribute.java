/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.Identifier;

/**
 * Represents a version requirement on an imported module
 * 
 * @author Csaba Raduly
 */
public final class VersionRequirementAttribute extends ModuleVersionAttribute {
	private final Identifier requiredModule;

	public VersionRequirementAttribute(final Identifier modid, final Identifier version) {
		super(version, false);
		requiredModule = modid;
	}

	public Identifier getRequiredModule() {
		return requiredModule;
	}

	@Override
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.REQUIRES;
	}
}
