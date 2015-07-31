/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;

/**
 * @author Kristof Szabados
 * */
public final class AtNotations extends ASTNode {

	private ArrayList<AtNotation> atnotations;

	public AtNotations() {
		atnotations = new ArrayList<AtNotation>();
	}

	public void addAtNotation(final AtNotation notation) {
		if (null != notation) {
			atnotations.add(notation);
		}
	}

	public int getNofAtNotations() {
		return atnotations.size();
	}

	public AtNotation getAtNotationByIndex(final int i) {
		return atnotations.get(i);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (atnotations != null) {
			for (AtNotation an : atnotations) {
				if (!an.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
