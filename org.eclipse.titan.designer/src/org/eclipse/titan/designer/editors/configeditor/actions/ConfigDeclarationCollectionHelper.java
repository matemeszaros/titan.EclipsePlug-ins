/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.actions;

import org.eclipse.titan.common.parsers.cfg.CfgLocation;

/**
 * @author Kristof Szabados
 * */
public final class ConfigDeclarationCollectionHelper {
	public String description;
	public CfgLocation location;

	public ConfigDeclarationCollectionHelper(final String description, final CfgLocation location) {
		this.description = description;
		this.location = location;
	}
}
