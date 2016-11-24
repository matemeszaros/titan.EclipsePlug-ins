/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope.nodes;

import java.util.List;

/**
 * This class represents a reference for a variable.
 * 
 * @author Viktor Varga
 */
class Reference {
	
	private final StatementNode ref;	// the SN containing the ASTNode which contains the reference
	private boolean leftHandSide;		// whether the reference is lhs
	
	Reference(StatementNode ref, boolean leftHandSide) {
		this.ref = ref;
		this.leftHandSide = leftHandSide;
	}
	
	public StatementNode getRef() {
		return ref;
	}
	public boolean isLeftHandSide() {
		return leftHandSide;
	}
	public void setLeftHandSide() {
		this.leftHandSide = true;
	}
	
	//helpers
	public static int indexOf(StatementNode sn, List<Reference> refs) {
		for (int i=0;i<refs.size();i++) {
			if (refs.get(i).ref.equals(sn)) {
				return i;
			}
		}
		return -1;
	}
	
}