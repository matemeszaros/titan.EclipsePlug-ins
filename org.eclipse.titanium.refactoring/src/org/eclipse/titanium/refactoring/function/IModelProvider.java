/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import java.util.List;

/**
 * Classes implementing this interface should be able to provide a model for a
 * table view. <code>T</code> is a class representing a line of the table.
 * 
 * @author Viktor Varga
 */
interface IModelProvider<T> {

	/** returns the list of items */
	List<T> getItems();

}
