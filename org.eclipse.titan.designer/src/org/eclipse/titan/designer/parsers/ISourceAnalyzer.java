/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.io.FileNotFoundException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.designer.AST.Module;

/**
 * An interface for source code analyzers.
 * 
 * @author Kristof Szabados
 * */
public interface ISourceAnalyzer {

	/** @return the errors from ANTLR 4 lexer and parser */
	List<SyntacticErrorStorage> getErrorStorage();
	
	/**
	 * @return the list of markers created for the parse time found
	 *         unsupported features and bad practices
	 */
	List<TITANMarker> getWarnings();

	/**
	 * @return the list of markers created for the parse time found
	 *         unsupported features
	 */
	List<TITANMarker> getUnsupportedConstructs();

	/** @return the module that was created from the parsed source code */
	Module getModule();

	/** @return the root interval of the interval created for the source code */
	Interval getRootInterval();

	/**
	 * Parses the provided elements. If the contents of an editor are to be
	 * parsed, than the file parameter is only used to report the errors to.
	 *
	 * @param file
	 *                the file to parse, and report the errors to
	 * @param code
	 *                the contents of an editor, or null.
	 *
	 * @exception FileNotFoundException
	 *                    if this method fails, the file was not found.
	 * */
	void parse(IFile file, String code) throws FileNotFoundException;
}
