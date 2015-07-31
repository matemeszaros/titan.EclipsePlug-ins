/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.parsers.ISourceAnalyzer;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.parsers.ProjectSourceSyntacticAnalyzer;

/**
 * Helper class to separate the responsibility of the source parser into smaller
 * parts. This class is responsible for handling the syntactic checking of the
 * source code of the projects
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class ProjectSourceSyntacticAnalyzer_V4 extends ProjectSourceSyntacticAnalyzer {

	public ProjectSourceSyntacticAnalyzer_V4(IProject project, ProjectSourceParser sourceParser) {
		super(project, sourceParser);
	}

	@Override
	protected boolean processParserErrors(final IFile aFile, ISourceAnalyzer aAnalyzer) {
		List<SyntacticErrorStorage> errors = null;

		errors = ((ISourceAnalyzer_V4)aAnalyzer).getErrorStorage();
		
		if (errors != null) {
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlySyntacticMarker(aFile, errors.get(i), IMarker.SEVERITY_ERROR);
			}
		}

		return errors != null && !errors.isEmpty();
	}
}
