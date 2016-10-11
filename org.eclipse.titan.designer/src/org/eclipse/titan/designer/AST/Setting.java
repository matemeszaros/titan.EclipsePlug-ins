/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.AST.Module.module_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public abstract class Setting extends ASTNode implements ISetting {
	/** indicates if this setting has already been found erroneous in the actual checking cycle. */
	protected boolean isErroneous;

	/** the time when this setting was check the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	/**
	 * The location of the whole setting.
	 * This location encloses the setting fully, as it is used to report errors to.
	 **/
	protected Location location;

	public Setting() {
		isErroneous = false;
		location = NULL_Location.INSTANCE;
	}

	@Override
	public final boolean getIsErroneous(final CompilationTimeStamp timestamp) {
		return isErroneous;
	}

	@Override
	public final void setIsErroneous(final boolean isErroneous) {
		this.isErroneous = isErroneous;
	}

	@Override
	public abstract Setting_type getSettingtype();

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public final boolean isAsn() {
		if (myScope == null) {
			return false;
		}

		return module_type.ASN_MODULE.equals(myScope.getModuleScope().getModuletype());
	}
}
