/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.experimental;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;

public class LoggerVisitor extends ASTVisitor {

	private HierarchyLogger logger = new HierarchyLogger();

	@Override
	public int visit(IVisitableNode node) {
		logger.visit(node);
		return V_CONTINUE;
	}

	@Override
	public int leave(IVisitableNode node) {
		logger.leave(node);
		return V_CONTINUE;
	}
}
