/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * ExtensionAddition (abstract class).
 * 
 * @author Kristof Szabados
 */
public abstract class ExtensionAddition extends ASTNode {

	public abstract int getNofComps();

	public abstract CompField getCompByIndex(int index);

	public abstract boolean hasCompWithName(final Identifier identifier);

	public abstract CompField getCompByName(final Identifier identifier);

	public abstract void trCompsof(final CompilationTimeStamp timestamp, IReferenceChain referenceChain, boolean isSet);
}
