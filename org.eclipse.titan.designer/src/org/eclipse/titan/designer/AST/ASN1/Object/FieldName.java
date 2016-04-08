/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Identifier;

/**
 * Class to represent FieldName.
 * FieldName is a sequence of PrimitiveFieldNames.
 * 
 * @author Kristof Szabados
 */
public final class FieldName extends ASTNode {
	private final List<Identifier> fields;

	public FieldName() {
		fields = new ArrayList<Identifier>(1);
	}

	public FieldName newInstance() {
		final FieldName temp = new FieldName();

		for (int i = 0, size = fields.size(); i < size; i++) {
			temp.addField(fields.get(i).newInstance());
		}

		return temp;
	}

	public String getDisplayName() {
		final StringBuilder builder = new StringBuilder();

		for (int i = 0, size = fields.size(); i < size; i++) {
			builder.append('.').append(fields.get(i).getDisplayName());
		}

		return builder.toString();
	}

	public void addField(final Identifier identifier) {
		if (null == identifier) {
			return;
		}

		fields.add(identifier);
	}

	public int getNofFields() {
		return fields.size();
	}

	public Identifier getFieldByIndex(final int index) {
		return fields.get(index);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (fields != null) {
			for (final Identifier id : fields) {
				if (!id.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
