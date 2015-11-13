/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

/**
 * @author Adam Delic
 * */
public abstract class StringSubtypeTreeElement extends SubtypeConstraint {
	public enum StringType {
		CHARSTRING, UNIVERSAL_CHARSTRING
	}

	public enum ElementType {
		// EmptyStringSet
		NONE,
		// FullStringSet
		ALL,
		// StringSetOperation
		OPERATION,
		// StringSetConstraint
		CONSTRAINT
	}

	protected final StringType string_type;

	protected StringSubtypeTreeElement(final StringType string_type) {
		this.string_type = string_type;
	}

	public StringSubtypeTreeElement evaluate() {
		return this;
	}

	public abstract ElementType getElementType();
}
