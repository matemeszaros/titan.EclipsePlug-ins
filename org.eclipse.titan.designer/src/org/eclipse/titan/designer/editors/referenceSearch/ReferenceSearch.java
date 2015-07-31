/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.referenceSearch;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.editors.IEditorWithCarretOffset;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IEditorPart;

/**
 * @author Szabolcs Beres
 * */
public final class ReferenceSearch {
	public static final String FILENOTIDENTIFIABLE = "The file related to the editor could not be identified";
	public static final String NORECOGNISABLEMODULENAME = "The name of the module in the file `{0}'' could not be identified";
	public static final String EXCLUDEDFROMBUILD = "The name of the module in the file `{0}'' could not be identified, the file is excluded from build";
	public static final String NOTFOUNDMODULE = "The module `{0}'' could not be found";

	private ReferenceSearch() {
		// Hide constructor
	}

	/**
	 * Helper function used by FindReferences classes for TTCN-3, ASN.1 and
	 * TTCNPP editors
	 */
	public static void runAction(final IEditorPart targetEditor, final ISelection selection) {
		targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);

		IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		if (file == null) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(FILENOTIDENTIFIABLE);
			return;
		}

		if (!TITANNature.hasTITANNature(file.getProject())) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(TITANNature.NO_TITAN_FILE_NATURE_FOUND);
			return;
		}

		IPreferencesService prefs = Platform.getPreferencesService();
		boolean reportDebugInformation = prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
				true, null);

		int offset;
		if (selection instanceof TextSelection && !selection.isEmpty() && !"".equals(((TextSelection) selection).getText())) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("text selected: " + ((TextSelection) selection).getText());
			}
			TextSelection tSelection = (TextSelection) selection;
			offset = tSelection.getOffset() + tSelection.getLength();
		} else {
			offset = ((IEditorWithCarretOffset) targetEditor).getCarretOffset();
		}

		// find the module
		ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
		final String moduleName = projectSourceParser.containedModule(file);
		if (moduleName == null) {
			if (ResourceExclusionHelper.isExcluded(file)) {
				targetEditor.getEditorSite().getActionBars().getStatusLineManager()
						.setErrorMessage(MessageFormat.format(EXCLUDEDFROMBUILD, file.getFullPath()));
			}

			targetEditor.getEditorSite().getActionBars().getStatusLineManager()
					.setErrorMessage(MessageFormat.format(NORECOGNISABLEMODULENAME, file.getFullPath()));
			return;
		}
		final Module module = projectSourceParser.getModuleByName(moduleName);
		if (module == null) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager()
					.setErrorMessage(MessageFormat.format(NOTFOUNDMODULE, moduleName));
			return;
		}

		final ReferenceFinder rf = new ReferenceFinder();
		boolean isDetected = rf.detectAssignmentDataByOffset(module, offset, targetEditor, true, reportDebugInformation);
		if (!isDetected) {
			return;
		}

		final ReferenceSearchQuery query = new ReferenceSearchQuery(rf, module, projectSourceParser);
		for (ISearchQuery runningQuery : NewSearchUI.getQueries()) {
			NewSearchUI.cancelQuery(runningQuery);
		}
		NewSearchUI.runQueryInBackground(query);
	}

}
