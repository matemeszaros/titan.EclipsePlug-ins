/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.core.resources.IResource;
import org.eclipse.titan.common.parsers.ILocationAST;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class LargeLocation extends Location {
	protected int endLine;

	public LargeLocation(final IResource file, final ILocationAST startToken, final ILocationAST endToken) {
		setLocation(file, startToken, endToken);

		if (endToken == null) {
			endLine = -1;
		} else {
			endLine = endToken.getLine();
		}
	}
	
	protected LargeLocation(final IResource file, final int line, final int offset, final int endOffset) {
		super(file, line, offset, endOffset);
	}

	public final int getEndLine() {
		return endLine;
	}
}
