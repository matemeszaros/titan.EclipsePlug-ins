/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * Ass_Error::get_Setting() returns this.
 * 
 * @author Kristof Szabados
 */
public final class Error_Setting extends Setting {

	@Override
	public Setting_type getSettingtype() {
		return Setting_type.S_ERROR;
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		// no members
		return true;
	}
}
