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

package org.eclipse.titan.codegenerator.template;

import org.eclipse.titan.codegenerator.Scope;
import org.eclipse.titan.designer.AST.IVisitableNode;

public interface Action {
	void execute();

	static Scope skip(final Class cls, final Scope parent) {
		return new Scope() {
			private int level = 0;
			@Override
			public Scope process(IVisitableNode node) {
				if (cls.isInstance(node)) {
					level++;
				}
				return this;
			}

			@Override
			public Scope finish(IVisitableNode node) {
				if (cls.isInstance(node)) {
					if (level <= 0) {
						return parent;
					}
					level--;
				}
				return this;
			}
		};
	}

	static Scope waitFor(final Class cls, final Scope parent, final Action action) {
		return new Scope() {
			@Override
			public Scope process(IVisitableNode node) {
				return this;
			}

			@Override
			public Scope finish(IVisitableNode node) {
				if (cls.isInstance(node)) {
					action.execute();
					return parent;
				}
				return this;
			}
		};
	}

}
