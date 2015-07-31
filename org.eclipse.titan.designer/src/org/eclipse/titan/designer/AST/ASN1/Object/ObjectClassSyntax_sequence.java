/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.util.ArrayList;

/**
 * Class to represent a (perhaps optional) sequence of OCS_Nodes.
 * 
 * @author Kristof Szabados
 */
public final class ObjectClassSyntax_sequence extends ObjectClassSyntax_Node {

	private final ArrayList<ObjectClassSyntax_Node> objectClassSyntaxes;
	private final boolean isOptional;
	/**
	 * This is used when default syntax is active. Then, if there is at
	 * least one parsed setting in the object, then the sequence must begin
	 * with a comma (',').
	 * */
	private final boolean optionalFirstComma;

	public ObjectClassSyntax_sequence(final boolean isOptional, final boolean optionalFirstComma) {
		objectClassSyntaxes = new ArrayList<ObjectClassSyntax_Node>();
		this.isOptional = isOptional;
		this.optionalFirstComma = optionalFirstComma;
	}

	@Override
	public void accept(final ObjectClassSyntax_Visitor visitor) {
		visitor.visitSequence(this);
	}

	public void addNode(final ObjectClassSyntax_Node node) {
		if (null != node) {
			objectClassSyntaxes.add(node);
		}
	}

	public void trimToSize() {
		objectClassSyntaxes.trimToSize();
	}

	public int getNofNodes() {
		return objectClassSyntaxes.size();
	}

	public ObjectClassSyntax_Node getNthNode(final int index) {
		return objectClassSyntaxes.get(index);
	}

	public boolean getIsOptional() {
		return isOptional;
	}

	public boolean getOptionalFirstComma() {
		return optionalFirstComma;
	}

	@Override
	public String getDisplayName() {
		final StringBuilder builder = new StringBuilder();

		if (isOptional) {
			builder.append('[');
		}

		for (int i = 0; i < objectClassSyntaxes.size(); i++) {
			if (i > 0) {
				builder.append(' ');
			}
			builder.append(objectClassSyntaxes.get(i).getDisplayName());
		}

		if (isOptional) {
			builder.append(']');
		}

		return builder.toString();
	}
}
