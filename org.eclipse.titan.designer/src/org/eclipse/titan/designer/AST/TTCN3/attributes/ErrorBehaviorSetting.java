/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;

/**
 * Represent an error behavior setting in the codec API of the run-time
 * environment. The setting contains the error type identifier and the way of
 * error handling.
 * 
 * @author Kristof Szabados
 */
public final class ErrorBehaviorSetting extends ASTNode {
	private String errorType;
	private String errorHandling;

	/**
	 * The location of the whole setting. This location encloses the setting
	 * fully, as it is used to report errors to.
	 **/
	private Location location;

	public ErrorBehaviorSetting(final String errorType, final String errorHandling) {
		this.errorType = errorType;
		this.errorHandling = errorHandling;
		location = NULL_Location.INSTANCE;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	public String getErrorType() {
		return errorType;
	}

	public String getErrorHandling() {
		return errorHandling;
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		// no members
		return true;
	}
}
