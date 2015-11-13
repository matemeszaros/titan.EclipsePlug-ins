/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;
import java.util.List;

/**
 * This a special reference kind which should only be used temporally, when the sub-references are backed by an other reference.
 * As such it is not setting scope information for the sub-references, to not alter the original information.
 * 
 * @author Kristof Szabados
 * */
public final class TemporalReference extends Reference {

	public TemporalReference(final Identifier modid, final List<ISubReference> subReferences) {
		super(modid);
		this.subReferences = new ArrayList<ISubReference>(subReferences);
	}

	@Override
	public void setMyScope(final Scope scope) {
		myScope = scope;
	}
}
