/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope.nodes;

/**
 * A class representing an edit operation in the simplified representation of the AST.
 *
 * @author Viktor Varga
 */
public class Edit {

	public final StatementNode declSt;
	public final StatementNode insertionPoint;	//if null -> remove edit

	public Edit(final StatementNode declSt, final StatementNode insertionPoint) {
		this.declSt = declSt;
		this.insertionPoint = insertionPoint;
	}

	public boolean isRemoveEdit() {
		return insertionPoint == null;
	}

}
