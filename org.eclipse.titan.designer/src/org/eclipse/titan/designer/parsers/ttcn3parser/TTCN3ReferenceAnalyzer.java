/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.Reference;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class TTCN3ReferenceAnalyzer {

	public TTCN3ReferenceAnalyzer() {
	}

	/**
	 * @return the parsed reference or null if the text can not form a reference
	 */
	public abstract Reference parse(IFile file, String code, boolean reportErrors, final int aLine, final int aOffset);
	
    /**
	 * Parses the provided elements. If the contents of an editor are to be parsed,
	 *  than the file parameter is only used to report the errors to.
	 * 
	 * @param file the file to parse, and report the errors to
	 * @param code the contents of an editor, or null.
	 */
	public abstract Reference parseForCompletion(IFile file, String code);
}
