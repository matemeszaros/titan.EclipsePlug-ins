/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Reference;

/**
 * @author Peter Olah
 */
public final class ReferenceCollector extends ASTVisitor {

	private final Set<Reference> references;

	public ReferenceCollector() {
		references = new HashSet<Reference>();
	}

	public Set<Reference> getReferences() {
		return references;
	}

	public Set<String> getReferencesAsString() {
		Set<String> result = new HashSet<String>();
		for (Reference reference : references) {
			result.add(reference.getId().getDisplayName());
		}
		return result;
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof Reference) {
			references.add(((Reference) node));
		}
		return V_CONTINUE;
	}
}
