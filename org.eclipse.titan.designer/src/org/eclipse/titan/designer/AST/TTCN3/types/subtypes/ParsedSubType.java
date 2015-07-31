/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import org.eclipse.titan.designer.AST.IIdentifierContainer;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;

/**
 * Represents a sub-type restriction as it was parsed.
 * 
 * @author Adam Delic
 * */
public abstract class ParsedSubType implements IIncrementallyUpdateable, IIdentifierContainer, IVisitableNode {
	public enum ParsedSubType_type {
		SINGLE_PARSEDSUBTYPE, RANGE_PARSEDSUBTYPE, LENGTH_PARSEDSUBTYPE, PATTERN_PARSEDSUBTYPE
	}

	public abstract ParsedSubType_type getSubTypetype();

	public abstract Location getLocation();
}
