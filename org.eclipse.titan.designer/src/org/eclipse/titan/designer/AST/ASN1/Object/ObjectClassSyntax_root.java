/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

/**
 * Class to represent the root of an OCS.
 * 
 * @author Kristof Szabados
 */
public final class ObjectClassSyntax_root extends ObjectClassSyntax_Node {

	private final ObjectClassSyntax_sequence sequence;

	public ObjectClassSyntax_root() {
		sequence = new ObjectClassSyntax_sequence(false, false);
	}

	@Override
	public void accept(final ObjectClassSyntax_Visitor visitor) {
		visitor.visitRoot(this);
	}

	@Override
	public String getDisplayName() {
		return sequence.getDisplayName();
	}

	public ObjectClassSyntax_sequence getSequence() {
		return sequence;
	}
}
