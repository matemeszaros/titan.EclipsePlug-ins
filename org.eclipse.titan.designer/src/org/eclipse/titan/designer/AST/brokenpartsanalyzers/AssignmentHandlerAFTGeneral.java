/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;

/**
 * @author Peter Olah
 */
public final class AssignmentHandlerAFTGeneral extends AssignmentHandler {

	protected AssignmentHandlerAFTGeneral(final Definition definition) {
		super(definition);
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof StatementBlock) {
			ReferenceCollector referenceCollector = new ReferenceCollector();
			node.accept(referenceCollector);
			addNonContagiousReferences(referenceCollector.getReferencesAsString());
			return V_SKIP;
		}

		if (node instanceof Reference) {
			addContagiousReference(((Reference) node).getId().getDisplayName());
		}

		return V_CONTINUE;
	}
}
