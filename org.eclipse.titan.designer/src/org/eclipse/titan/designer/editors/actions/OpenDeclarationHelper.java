/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.actions;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.editors.IReferenceParser;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.ui.IEditorPart;

/**
 * @author Kristof Szabados
 * */
public final class OpenDeclarationHelper {
	public static final String NORECOGNISABLEFILE = "The file related to the editor could not be identified";
	public static final String NORECOGNISABLEREFERENCE = "Current text selection does not resolve to a TTCN3 reference";
	public static final String NORECOGNISABLESCOPE = "The reference does not seem to be in a valid scope";
	public static final String NORECOGNISABLEMODULENAME = "The name of the module in the file `{0}'' could not be identified";
	public static final String EXCLUDEDFROMBUILD = "The name of the module in the file `{0}'' could not be identified, the file is excluded from build";
	public static final String NOTFOUNDMODULE = "The module `{0}'' could not be found";

	/** private constructor to disable instantiation */
	private OpenDeclarationHelper() {
	}

	public static DeclarationCollector findVisibleDeclarations(final IEditorPart targetEditor, final IReferenceParser referenceParser,
			final IDocument document, final int offset) {
		return findVisibleDeclarations(targetEditor, referenceParser, document, offset, true);
	}

	public static DeclarationCollector findVisibleDeclarations(final IEditorPart targetEditor, final IReferenceParser referenceParser,
			final IDocument document, final int offset, final boolean reportErrors) {
		final IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		if (file == null) {
			if (reportErrors) {
				ErrorReporter.parallelDisplayInStatusLine(targetEditor, NORECOGNISABLEFILE);
			}
			return null;
		}

		Reference reference = null;

		referenceParser.setErrorReporting(reportErrors);
		reference = referenceParser.findReferenceForOpening(file, offset, document);
		if (reference == null) {
			if (reportErrors) {
				ErrorReporter.parallelDisplayInStatusLine(targetEditor, NORECOGNISABLEREFERENCE);
			}
			return null;
		}

		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
		if (ResourceExclusionHelper.isExcluded(file)) {
			ErrorReporter.parallelDisplayInStatusLine(targetEditor, MessageFormat.format(EXCLUDEDFROMBUILD, file.getFullPath()));
			return null;
		}

		final Module tempModule = projectSourceParser.containedModule(file);
		if (tempModule == null) {
			if (reportErrors) {
				ErrorReporter.parallelDisplayInStatusLine(targetEditor, MessageFormat.format(NOTFOUNDMODULE, file.getFullPath()));
			}
			return null;
		}

		final Scope scope = tempModule.getSmallestEnclosingScope(offset);
		if (scope == null) {
			if (reportErrors) {
				ErrorReporter.parallelDisplayInStatusLine(targetEditor,NORECOGNISABLESCOPE);
			}
			return null;
		}
		reference.setMyScope(scope);
		reference.detectModid();
		if (reference.getId() == null) {
			if (reportErrors) {
				ErrorReporter.parallelDisplayInStatusLine(targetEditor, NORECOGNISABLEREFERENCE);
			}
			return null;
		}

		final DeclarationCollector declarationCollector = new DeclarationCollector(reference);
		scope.addDeclaration(declarationCollector);

		return declarationCollector;
	}
}
