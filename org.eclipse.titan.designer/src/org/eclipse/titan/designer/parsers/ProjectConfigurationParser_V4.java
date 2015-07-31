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
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.cfg.CfgAnalyzer_V4;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.editors.EditorTracker;
import org.eclipse.titan.designer.editors.ISemanticTITANEditor;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.titan.designer.editors.configeditor.ConfigFoldingSupport;
import org.eclipse.titan.designer.editors.configeditor.ConfigTextEditor;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This is project level root of all parsing related activities. Every data that
 * was extracted from files while parsing them has its root here.
 * <p>
 * In not noted elsewhere all operations that modify the internal states are
 * executed in a parallel WorkspaceJob, which will have scheduling rules
 * required to access related resources.
 * <p>
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ProjectConfigurationParser_V4 extends ProjectConfigurationParser {

	public ProjectConfigurationParser_V4(IProject project) {
		super(project);
	}
	
	@Override
	protected void fileBasedAnalysis(final IFile file) {
		List<TITANMarker> warnings = null;
		List<SyntacticErrorStorage> errorsStored = null;
		IDocument document = null;
		ISemanticTITANEditor tempEditor = null;
		List<ISemanticTITANEditor> editors = null;
		if (EditorTracker.containsKey(file)) {
			editors = EditorTracker.getEditor(file);
			tempEditor = editors.get(0);
			document = tempEditor.getDocument();
		}
		ConfigTextEditor editor = null;
		if (tempEditor instanceof ConfigTextEditor) {
			editor = (ConfigTextEditor) tempEditor;
		}

		String oldConfigFilePath = fileMap.get(file);
		if (oldConfigFilePath != null) {
			fileMap.remove(file);
		}

		CfgAnalyzer_V4 cfgAnalyzer = new CfgAnalyzer_V4();
		cfgAnalyzer.parse(file, document == null ? null : document.get());
		errorsStored = cfgAnalyzer.getErrorStorage();
		warnings = cfgAnalyzer.getWarnings();

		if (editor != null && editor.getDocument() != null) {
			ConfigEditor parentEditor = editor.getParentEditor();
			if ( errorsStored == null || errorsStored.isEmpty() ) {
				//TODO: call setParseTreeRoot() if needed
				//parentEditor.setParseTreeRoot(cfgAnalyzer.getRootNode());
				parentEditor.refresh(cfgAnalyzer);
				parentEditor.setErrorMessage(null);
			}
		}

		fileMap.put(file, file.getFullPath().toOSString());
		uptodateFiles.put(file, file.getFullPath().toOSString());

		if (document != null) {
			GlobalIntervalHandler.putInterval(document, cfgAnalyzer.getRootInterval());
		}

		if (warnings != null) {
			for (TITANMarker marker : warnings) {
				if (file.isAccessible()) {
					Location location = new Location(file, marker.getLine(), marker.getOffset(), marker.getEndOffset());
					location.reportExternalProblem(marker.getMessage(), marker.getSeverity(),
							GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
				}
			}
		}

		if (errorsStored != null && !errorsStored.isEmpty()) {
			String reportLevel = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORTERRORSINEXTENSIONSYNTAX, GeneralConstants.WARNING, null);
			int errorLevel;
			if (GeneralConstants.ERROR.equals(reportLevel)) {
				errorLevel = IMarker.SEVERITY_ERROR;
			} else if (GeneralConstants.WARNING.equals(reportLevel)) {
				errorLevel = IMarker.SEVERITY_WARNING;
			} else {
				return;
			}
			for (int i = 0; i < errorsStored.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlySyntacticMarker(file, errorsStored.get(i), errorLevel);
			}
		}

		if (document != null && editors != null) {
			ConfigFoldingSupport foldingSupport = new ConfigFoldingSupport();
			final IDocument tempDocument = document;
			final List<ISemanticTITANEditor> editors2 = editors;
			final List<Position> positions = foldingSupport.calculatePositions(tempDocument);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (ISemanticTITANEditor editor : editors2) {
						editor.updateFoldingStructure(positions);
						editor.invalidateTextPresentation();
					}
				}
			});
		}
	}
}
