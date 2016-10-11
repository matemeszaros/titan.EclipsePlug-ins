/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.titan.designer.AST.Location;

/**
 * A node that corresponds to an entity in the TTCN3 code, so it can be opened
 * in an eclipse editor.
 * 
 * @author poroszd
 * 
 */
interface IOpenable {
	/**
	 * @return The Location where its code starts.
	 */
	Location getLocation();
}
