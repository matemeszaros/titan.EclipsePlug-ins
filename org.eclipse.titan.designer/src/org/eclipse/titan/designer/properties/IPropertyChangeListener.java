/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties;

import org.eclipse.core.resources.IResource;

/**
 * This interface is implemented by objects that listen to changes of TITAN related properties.
 * 
 * @author Kristof Szabados
 * */
public interface IPropertyChangeListener {

	/**
	 * Notifies that the resource has changed its properties.
	 * 
	 * @param resouce the resource.
	 * */
	public void propertyChanged(final IResource resouce);
}
