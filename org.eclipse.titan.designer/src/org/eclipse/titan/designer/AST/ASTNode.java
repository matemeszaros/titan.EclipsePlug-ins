/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.List;

import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definitions;
import org.eclipse.titan.designer.editors.T3Doc;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Base class for AST-classes.
 * 
 * @author Kristof Szabados
 */
public abstract class ASTNode implements IASTNode, IIdentifierContainer, IVisitableNode {

	/** the scope of the declaration of this node. */
	protected Scope myScope;
	/** the naming parent of the node. */
	private INamedNode nameParent;

	public Location getCommentLocation() {
		return null;
	}

	@Override
	public String getFullName() {
		return getFullName(this).toString();
	}

	// FIXME: this does not belong here, ASTNodes in general don't have comments
	public T3Doc getT3Doc(final Location location) {

		if (this instanceof Def_ModulePar) {
			String st1 = this.getFullName().toString();
			String st = st1.substring(st1.lastIndexOf(".") + 1);

			return new T3Doc(this.getCommentLocation(), st);
		}

		if (this.getCommentLocation() != null) {
			return new T3Doc(this.getCommentLocation());
		}

		if (this instanceof ILocateableNode) {
			ILocateableNode iloc = (ILocateableNode) this;

			//ToDo check scopes that do not matter
			Scope scope = this.getMyScope();

			Assignment assignment = scope.getModuleScope().getEnclosingAssignment(iloc.getLocation().getOffset());

			if (assignment == null || assignment == this || assignment.getMyScope() instanceof Definitions) {
				return null;
			}

			final T3Doc parent_t3doc = assignment.getT3Doc(location);
			if (parent_t3doc != null) {
				// if it is a type assignment/definition then detect if we are in a field
				if (assignment.getAssignmentType() == Assignment_type.A_TYPE) {
					IType type = assignment.getType(CompilationTimeStamp.getBaseTimestamp());
					if (type == null) {
						return null;
					}
					// Reference finder - wonderful
					ReferenceFinder rf = new ReferenceFinder(assignment);
					rf.scope = this.getMyScope().getModuleScope().getSmallestEnclosingScope(iloc.getLocation().getOffset());
					rf.type = assignment.getType(CompilationTimeStamp.getBaseTimestamp());
					type.getEnclosingField(location.getOffset(), rf);

					String st = null;
					if (rf.fieldId != null) {
						st = rf.fieldId.getDisplayName();
					} else {
						String st1 = this.getFullName().toString();
						st = st1.substring(st1.lastIndexOf(".") + 1);
					}

					//Get member information if available
					if (parent_t3doc.getMembers() != null) {
						final String desc = parent_t3doc.getMembers().get(st);
						if (desc != null) {
							return new T3Doc(desc);
						}
					}
				} else if (assignment.getAssignmentType() == Assignment_type.A_TEMPLATE) {
					String st1 = this.getFullName().toString();
					String st = st1.substring(st1.lastIndexOf(".") + 1);

					String desc = null;
					if (parent_t3doc.getMembers() != null) {
					  desc = parent_t3doc.getMembers().get(st);
					}
					
					if (parent_t3doc.getParams() != null) {
					  desc = parent_t3doc.getParams().get(st);
					}

					if (desc != null) {
						return new T3Doc(desc);
					}

				} else if (assignment.getAssignmentType() == Assignment_type.A_FUNCTION 
					||
					assignment.getAssignmentType() == Assignment_type.A_FUNCTION_RTEMP 
					||
					assignment.getAssignmentType() == Assignment_type.A_FUNCTION_RVAL) {
					
					String st1 = this.getFullName().toString();
					String st = st1.substring(st1.lastIndexOf(".") + 1);

					if (parent_t3doc.getParams() != null) {
						final String desc = parent_t3doc.getParams().get(st);
						if (desc != null) {
							return new T3Doc(desc);
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		if (null != nameParent) {
			return nameParent.getFullName(this);
		}

		return new StringBuilder();
	}

	@Override
	public final void setFullNameParent(final INamedNode nameParent) {
		this.nameParent = nameParent;
	}

	@Override
	public INamedNode getNameParent() {
		return nameParent;
	}

	@Override
	public void setMyScope(final Scope scope) {
		myScope = scope;
	}

	@Override
	public final Scope getMyScope() {
		return myScope;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
	}
	
	/**
	 * Called by accept(), AST objects have to call accept() of their members in this function
	 * @param v the visitor object
	 * @return false to abort, will be returned by accept()
	 */
	protected abstract boolean memberAccept(ASTVisitor v);
	
	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT: return false;
		case ASTVisitor.V_SKIP: return true;
		}
		if (!memberAccept(v)) {
			return false;
		}

		if (v.leave(this)==ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
