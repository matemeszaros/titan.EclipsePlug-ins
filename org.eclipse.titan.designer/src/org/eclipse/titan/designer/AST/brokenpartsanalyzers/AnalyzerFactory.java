/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Factory class to create instance of IBaseAnalyzer.
 * 
 * @author Peter Olah
 */
public final class AnalyzerFactory {

	private AnalyzerFactory() {
	}

	public static IBaseAnalyzer getAnalyzer(final CompilationTimeStamp timestamp) {
		return new BrokenPartsViaReferences(SelectionAlgorithm.BROKENREFERENCESINVERTED, timestamp);
	}


}
