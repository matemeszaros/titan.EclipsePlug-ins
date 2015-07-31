/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a generic type mapping target.
 * 
 * @author Kristof Szabados
 * */
public abstract class TypeMappingTarget extends ASTNode implements ILocateableNode {
	public enum TypeMapping_type {
		SIMPLE, DISCARD, FUNCTION, ENCODE, DECODE
	}

	/** the time when this attribute was checked the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	/**
	 * The location of the whole mapping. This location encloses the mapping
	 * fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	public abstract TypeMapping_type getTypeMappingType();

	public abstract String getMappingName();

	@Override
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public final Location getLocation() {
		return location;
	}

	public abstract Type getTargetType();

	/**
	 * Does the semantic checking of the type mapping target.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public abstract void check(final CompilationTimeStamp timestamp, final Type source);
}
