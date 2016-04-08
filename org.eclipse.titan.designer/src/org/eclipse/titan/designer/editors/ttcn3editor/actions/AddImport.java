/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor.actions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.editors.actions.DeclarationCollectionHelper;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.actions.OpenDeclarationHelper;
import org.eclipse.titan.designer.editors.actions.OpenDeclarationLabelProvider;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3ReferenceParser;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * @author Kristof Szabados
 * */
public final class AddImport implements IEditorActionDelegate {
	public static final String FILENOTIDENTIFIABLE = "The file related to the editor could not be identified";
	public static final String NOTTTCN3DECLARATION = "Current text selection does not resolve to a TTCN3 declaration";
	public static final String TTCN3EDITORNOTFOUND = "The TTCN3 editor could not be found";

	private IEditorPart targetEditor = null;
	private ISelection selection = TextSelection.emptySelection();

	@Override
	public void run(final IAction action) {
		TITANDebugConsole.println("Add import called: ");
		if (targetEditor == null || !(targetEditor instanceof TTCN3Editor)) {
			return;
		}

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
		if (!selection.isEmpty() && selection instanceof TextSelection && !"".equals(((TextSelection) selection).getText())) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("text selected: " + ((TextSelection) selection).getText());
			}
			TextSelection tSelection = (TextSelection) selection;
			offset = tSelection.getOffset() + tSelection.getLength();
		} else {
			offset = ((TTCN3Editor) targetEditor).getCarretOffset();
		}

		DeclarationCollector declarationCollector = OpenDeclarationHelper.findVisibleDeclarations(targetEditor, new TTCN3ReferenceParser(
				false), ((TTCN3Editor) targetEditor).getDocument(), offset, false);

		if (declarationCollector == null) {
			return;
		}

		List<DeclarationCollectionHelper> collected = declarationCollector.getCollected();
		if (collected.isEmpty()) {
			// FIXME add semantic check guard on project level.
			ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
			if (reportDebugInformation) {
				TITANDebugConsole.println("No visible elements found");
			}
			for (String moduleName2 : projectSourceParser.getKnownModuleNames()) {
				Module module2 = projectSourceParser.getModuleByName(moduleName2);
				if (module2 != null) {
					// Visit each file in the project one by
					// one instead of
					// "module2.getAssignments().addDeclaration(declarationCollector)".
					Assignments assignments = module2.getAssignments();
					for (int i = 0; i < assignments.getNofAssignments(); i++) {
						assignments.getAssignmentByIndex(i).addDeclaration(declarationCollector, 0);
					}
				}
			}

			if (declarationCollector.getCollectionSize() == 0) {
				targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(NOTTTCN3DECLARATION);
				return;
			}

			if (reportDebugInformation) {
				TITANDebugConsole.println("Elements were only found in not visible modules");
			}

			DeclarationCollectionHelper resultToInsert = null;
			if (collected.size() == 1) {
				resultToInsert = collected.get(0);
			} else {
				OpenDeclarationLabelProvider labelProvider = new OpenDeclarationLabelProvider();
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(null, labelProvider);
				dialog.setTitle("Add Import");
				dialog.setMessage("Choose element to generate an import statement for.");
				dialog.setElements(collected.toArray());
				if (dialog.open() == Window.OK) {
					if (reportDebugInformation) {
						TITANDebugConsole.getConsole().newMessageStream().println("Selected: " + dialog.getFirstResult());
					}
					resultToInsert = (DeclarationCollectionHelper) dialog.getFirstResult();
				}
			}

			if (resultToInsert == null) {
				return;
			}

			IFile newfile = (IFile) resultToInsert.location.getFile();
			String moduleName = projectSourceParser.containedModule(newfile);
			Module newModule = projectSourceParser.getModuleByName(moduleName);
			if (newModule == null) {
				targetEditor.getEditorSite().getActionBars().getStatusLineManager()
						.setErrorMessage("Could not identify the module in file " + newfile.getName());
				return;
			}

			String ttcnName = newModule.getIdentifier().getTtcnName();
			TITANDebugConsole.println("the new module to insert: " + ttcnName);

			final IFile actualFile = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
			String actualModuleName = projectSourceParser.containedModule(actualFile);
			Module actualModule = projectSourceParser.getModuleByName(actualModuleName);

			int insertionOffset = ((TTCN3Module) actualModule).getAssignmentsScope().getLocation().getOffset() + 1;

			MultiTextEdit multiEdit = new MultiTextEdit(insertionOffset, 0);
			RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(((TTCN3Editor) targetEditor).getDocument(),
					multiEdit, TextEdit.UPDATE_REGIONS | TextEdit.CREATE_UNDO);
			multiEdit.addChild(new InsertEdit(insertionOffset, "\nimport from " + ttcnName + " all;\n"));

			try {
				processor.performEdits();
			} catch (BadLocationException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		} else {
			if (reportDebugInformation) {
				for (DeclarationCollectionHelper foundDeclaration : collected) {
					TITANDebugConsole.println("declaration:" + foundDeclaration.location.getFile() + ": "
									+ foundDeclaration.location.getOffset() + " - "
									+ foundDeclaration.location.getEndOffset() + " is available");
				}
			}
		}

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openWarning(null, "Study feature",
						"Adding a missing importation is still under study");
			}
		});
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}
}
