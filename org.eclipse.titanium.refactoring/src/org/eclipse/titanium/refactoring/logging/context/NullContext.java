/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;

/**
 * Context class representing uninteresting nodes.
 * All the nodes from which no log arguments are created are represented by this class. 
 * 
 * @author Viktor Varga
 */
class NullContext extends Context {

	NullContext(final IVisitableNode node, final Settings settings) {
		super(node, settings);
	}
	
	@Override
	protected void process_internal() {}
	
	@Override
	protected List<String> createLogParts_internal(final Set<String> idsAlreadyHandled) {
		return new ArrayList<String>();
	}

}
