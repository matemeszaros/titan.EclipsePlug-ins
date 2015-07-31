/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;


/**
 * Things that have a governor. Object, Value...
 * 
 * @author Kristof Szabados
 */
public abstract class Governed extends Setting implements IGoverned {

	@Override
	public abstract IGovernor getMyGovernor();
}
