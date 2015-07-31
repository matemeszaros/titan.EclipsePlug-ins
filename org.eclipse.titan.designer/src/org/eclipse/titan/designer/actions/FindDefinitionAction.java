/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IOutlineElement;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.finddefinition.DefinitionListSelectionDialog;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;


/**
 * @author Szabolcs Beres
 * */
public class FindDefinitionAction extends AbstractHandler implements IEditorActionDelegate {
	private IEditorPart targetEditor = null;
	private ISelection selection = TextSelection.emptySelection();

	public FindDefinitionAction() {
	}

	private static class DefinitionLabelProvider extends LabelProvider {
		@Override
		public String getText(final Object element) {
			if (element instanceof IOutlineElement) {
				String text = ((IOutlineElement) element).getOutlineText();
				if (text == null || text.isEmpty()) {
					return ((IOutlineElement) element).getIdentifier().getDisplayName();
				}
				return text;
			}
			return "";
		}

		@Override
		public Image getImage(final Object element) {
			if (element instanceof IOutlineElement) {
				String icon = ((IOutlineElement) element).getOutlineIcon();
				return ImageCache.getImage(icon);
			}
			return null;
		}
	}

	@Override
	public void run(final IAction action) {
		doFindDefinition();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		
		doFindDefinition();

		return null;
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

	private void doFindDefinition() {
		DefinitionListSelectionDialog dialog = new DefinitionListSelectionDialog(
				Display.getDefault().getActiveShell(),
				new DefinitionLabelProvider(),
				getCurrentProject());

		dialog.setTitle("Find Definition");
		dialog.setMessage("Type the name of a definition");
		dialog.setHelpAvailable(false);
		
		if (targetEditor instanceof TTCN3Editor) {
			selection = ((TTCN3Editor) targetEditor).getSelectionProvider().getSelection();
		}
		if (selection instanceof TextSelection && !selection.isEmpty()) {
			IPreferencesService prefs = Platform.getPreferencesService();
			boolean reportDebugInformation = prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
					true, null);
			
			if (reportDebugInformation) {
				TITANDebugConsole.println("text selected: " + ((TextSelection) selection).getText());
			}
			TextSelection tSelection = (TextSelection) selection;
			dialog.setFilter(tSelection.getText());
		}
		
		
		dialog.init();

		if (dialog.open() == Window.CANCEL || dialog.getFirstResult() == null) {
			return;
		}
		Object result = dialog.getFirstResult();
		if (!(result instanceof ILocateableNode)) {
			return;
		}
		showInEditor((ILocateableNode) result);
	}

	private IProject getCurrentProject() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPart activePart = activePage.getActivePart();
		if (activePart instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart) activePart;
			IResource resource = (IResource )editorPart.getEditorInput().getAdapter(IResource.class);
			return resource.getProject();
		}
		return null;
	}

	private void showInEditor(final ILocateableNode node) {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		Location location = node.getLocation();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(location.getFile().getName());
		if (desc == null) {
			TITANDebugConsole.println("Cannot find the editor");
			return;
		}
		try {
			IEditorPart editorPart = activePage.openEditor(new FileEditorInput((IFile) location.getFile()), desc.getId());
			if (editorPart != null && (editorPart instanceof AbstractTextEditor)) {
				((AbstractTextEditor) editorPart).selectAndReveal(location.getOffset(), location.getEndOffset()
						- location.getOffset());
			}
		} catch (PartInitException e) {
			ErrorReporter.logExceptionStackTrace("Error while opening the editor", e);
		}
	}

}
