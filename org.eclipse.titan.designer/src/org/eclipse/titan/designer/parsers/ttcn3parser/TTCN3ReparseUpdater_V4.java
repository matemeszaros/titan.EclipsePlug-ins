/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import java.util.List;

import org.eclipse.core.resources.IFile;

/**
 * This class directs the incremental parsing. Stores all information about the nature and size of the damage done to the system, helps in reparsing
 * only the needed part of the file. And also takes care of cleaning and reporting errors inside the damaged area.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class TTCN3ReparseUpdater_V4 extends TTCN3ReparseUpdater {
	
	/** Errors from the parser (indicating syntax errors). */
	//TODO: implement
	//private List<SyntacticErrorStorage> mErrors;

	public TTCN3ReparseUpdater_V4(IFile file, String code, int firstLine, int lineShift, int startOffset, int endOffset, int shift) {
		super(file, code, firstLine, lineShift, startOffset, endOffset, shift);
	}

	//TODO: implement parse()

	@Override
	public boolean startsWithFollow(List<Integer> followSet) {
		//TODO: implement
		return false;
	}

	@Override
	public boolean endsWithToken(List<Integer> followSet) {
		//TODO: implement
		return false;
	}

	@Override
	protected void reportSpecificSyntaxErrors() {
		//TODO: implement
	}


}
