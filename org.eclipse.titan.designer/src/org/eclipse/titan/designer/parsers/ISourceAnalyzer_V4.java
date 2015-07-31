/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.util.List;

import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.parsers.ISourceAnalyzer;

/**
 * An interface for source code analyzers.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public interface ISourceAnalyzer_V4 extends ISourceAnalyzer {

	/** @return the errors from ANTLR 4 lexer and parser */
	List<SyntacticErrorStorage> getErrorStorage();
}
