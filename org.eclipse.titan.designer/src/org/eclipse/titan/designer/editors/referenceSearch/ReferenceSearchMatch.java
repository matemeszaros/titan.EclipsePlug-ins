/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.referenceSearch;

import org.eclipse.search.ui.text.Match;
import org.eclipse.titan.designer.AST.Identifier;

/**
 * @author Szabolcs Beres
 * */
public class ReferenceSearchMatch extends Match {

	private Identifier id;

	public ReferenceSearchMatch(final Identifier id) {
		super(id.getLocation().getFile(), id.getLocation().getOffset(), id.getLocation().getEndOffset() - id.getLocation().getOffset());
		this.id = id;
	}

	public Identifier getId() {
		return id;
	}
}
