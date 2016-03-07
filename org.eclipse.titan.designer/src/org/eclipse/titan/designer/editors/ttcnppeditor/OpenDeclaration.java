/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcnppeditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.declarationsearch.IdentifierFinderVisitor;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @author Kristof Szabados
 * */
public final class OpenDeclaration extends AbstractHandler implements IEditorActionDelegate {
	public static final String FILENOTIDENTIFIABLE = "The file related to the editor could not be identified";
	public static final String NOTTTCN3DECLARATION = "Current text selection does not resolve to a TTCN3 declaration";
	public static final String TTCNPPEDITORNOTFOUND = "The TTCNPP editor could not be found";

	private IEditorPart targetEditor = null;
	private ISelection selection = TextSelection.emptySelection();

	/**
	 * TODO This function is not needed
	 */
	@Override
	public void run(final IAction action) {
		doOpenDeclaration();
	}

	/**
	 * Opens an editor for the provided declaration, and in this editor the
	 * location of the declaration is revealed and selected.
	 * 
	 * @param declaration
	 *                the declaration to reveal
	 * */
	private void selectAndRevealDeclaration(final Location location) {
		IWorkbenchPage page = targetEditor.getSite().getPage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(location.getFile().getName());
		if (desc == null) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(TTCNPPEDITORNOTFOUND);
			return;
		}
		try {
			IEditorPart editorPart = page.openEditor(new FileEditorInput((IFile) location.getFile()), desc.getId());
			if (editorPart != null && (editorPart instanceof AbstractTextEditor)) {
				((AbstractTextEditor) editorPart).selectAndReveal(location.getOffset(),
						location.getEndOffset() - location.getOffset());
			}

		} catch (PartInitException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		doOpenDeclaration();

		return null;
	}

	private final void doOpenDeclaration() {
		if (targetEditor == null || !(targetEditor instanceof TTCNPPEditor)) {
			return;
		}

		IPreferencesService prefs = Platform.getPreferencesService();
		boolean reportDebugInformation = prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
				true, null);

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

		if (ResourceExclusionHelper.isExcluded(file)) {
			MessageDialog.openError(null, "Open Declaration does not work within excluded resources",
					"This module is excluded from build. To use the Open Declaration "
							+ "feature please click on the 'Toggle exclude from build state' in the context menu of the Project Explorer. ");
			return;
		}

		int offset;
		if (!selection.isEmpty() && selection instanceof TextSelection && !"".equals(((TextSelection) selection).getText())) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("text selected: " + ((TextSelection) selection).getText());
			}
			TextSelection tSelection = (TextSelection) selection;
			offset = tSelection.getOffset() + tSelection.getLength();
		} else {
			offset = ((TTCNPPEditor) targetEditor).getCarretOffset();
		}

		ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());

		final String ttcn3ModuleName = projectSourceParser.containedModule(file);

		final Module module = projectSourceParser.getModuleByName(ttcn3ModuleName);

		if (module == null) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("Can not find the module.");
			}
			return;
		}

		IdentifierFinderVisitor visitor = new IdentifierFinderVisitor(offset);
		module.accept(visitor);
		final Declaration decl = visitor.getReferencedDeclaration();
		if (decl == null) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("No visible elements found");
			}
			return;
		}

		selectAndRevealDeclaration(decl.getIdentifier().getLocation());
	}
}
