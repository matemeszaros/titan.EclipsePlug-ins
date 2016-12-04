/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;

/**
 * A class representing a variable.
 * 
 * @author Viktor Varga
 */
public class Variable {
	
	public final Assignment assmnt;	//the def_var, or FormalParameter object
	
	public final StatementNode declaration;
	
	/**
	 * StatementNodes in which this variable is referred to.
	 * The second argument is true if the reference is a left hand side reference (write occurrence). 
	 * */
	public final List<Reference> references;
	
	/** 
	 * True if this variable is a formal parameter of a function 
	 * (in this case there is no declaration statement).
	 * If true, the variable cannot be refactored. */
	public final boolean isParameter;

	public Variable(final Assignment assmnt, final StatementNode declaration, final boolean isParameter) {
		this.assmnt = assmnt;
		this.declaration = declaration;
		this.isParameter = isParameter;
		this.references = new ArrayList<Reference>();
	}
	
	public Assignment getAssmnt() {
		return assmnt;
	}
	public StatementNode getDeclaration() {
		return declaration;
	}
	public List<Reference> getReferences() {
		return references;
	}
	public boolean isParameter() {
		return isParameter;
	}
	
	public void addReference(final StatementNode st, final boolean leftHandSideRef) {
		//avoid multiple instances of the same SN in the list
		if (!references.isEmpty()) {
			if (references.get(references.size()-1).getRef().equals(st)) {
				if (leftHandSideRef) {
					references.get(references.size()-1).setLeftHandSide();
				}
				return;
			}
		}
		references.add(new Reference(st, leftHandSideRef));
	}
	public void removeReference(final StatementNode st) {
		ListIterator<Reference> it = references.listIterator();
		while (it.hasNext()) {
			if (it.next().getRef().equals(st)) {
				it.remove();
				return;
			}
		}
		String fname = assmnt.getLocation().getFile().toString();
		ErrorReporter.logError("Variable.removeReference(): Could not remove reference for variable: " + toString() + "; in file " + fname);
	}
	
	@Override
	public String toString() {
		return assmnt == null ? "null" : assmnt.getIdentifier().toString();
	}

	public String toStringRecursive(final boolean includeRefs, final int prefixLen) {
		String prefix = new String(new char[prefixLen]).replace('\0', ' ');
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("VAR: ").append(assmnt == null ? "null" : assmnt.getIdentifier().toString());
		if (includeRefs) {
			sb.append('\n').append(prefix).append("  refs:\n");
			for (Reference r: references) {
				sb.append(" LHS:" + r.isLeftHandSide() + " " + r.getRef().toStringRecursive(false, 8)).append('\n');
			}
		}
		return sb.toString();
	}
	
}
