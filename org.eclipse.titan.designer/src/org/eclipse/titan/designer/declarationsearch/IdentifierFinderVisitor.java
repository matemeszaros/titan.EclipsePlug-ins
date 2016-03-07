/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.declarationsearch;

import org.eclipse.titan.designer.AST.ASTLocationChainVisitor;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IReferencingElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;

/**
 * This class is similar to {@link ASTLocationChainVisitor} except it aborts as
 * soon as an identifier is found.
 * 
 * @author Szabolcs Beres
 */
//FIXME why identifierFinderVisitor if it searches for references ?
public class IdentifierFinderVisitor extends ASTVisitor {
	private IReferencingElement reference = null;
	private ISubReference subReference = null;

	private int offset;

	public IdentifierFinderVisitor(int offset) {
		this.offset = offset;
	}

	@Override
	public int visit(IVisitableNode node) {
		if (node instanceof ASN1Assignment) {
			ASN1Assignment assignment = (ASN1Assignment) node;
			if(assignment.getAssPard() != null) {
				return V_SKIP;
			}
		}

		if (node instanceof ILocateableNode) {
			final Location loc = ((ILocateableNode) node).getLocation();
			if (loc == null) {
				return V_ABORT;
			}
			if (!loc.containsOffset(offset)) {
				// skip the children, the offset is not inside
				// this node
				return V_SKIP;
			}

			if (node instanceof IReferencingElement) {
				reference = (IReferencingElement) node;
			} else if (node instanceof ISubReference) {
				subReference = (ISubReference) node;
			}

			if (node instanceof Identifier) {
				return V_ABORT;
			}

		}
		return V_CONTINUE;
	}

	/**
	 * Identifies the referenced declaration and returns it.
	 * 
	 * @return the referenced declaration or null.
	 */
	public Declaration getReferencedDeclaration() {
		if (reference == null) {
			// nothing found during AST visit
			return null;
		}

		if (subReference == null || !(reference instanceof Reference)) {
			// we have a reference, but nothing more specific
			return reference.getDeclaration();
		}

		return ((Reference) reference).getReferencedDeclaration(subReference);
	}

}
