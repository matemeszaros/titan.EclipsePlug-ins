/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

/**
 * Abstract class for OCS visitors.
 * 
 * @author Kristof Szabados
 */
public abstract class ObjectClassSyntax_Visitor {
	public abstract void visitRoot(ObjectClassSyntax_root parameter);

	public abstract void visitSequence(ObjectClassSyntax_sequence parameter);

	public abstract void visitLiteral(ObjectClassSyntax_literal parameter);

	public abstract void visitSetting(ObjectClassSyntax_setting parameter);
}
