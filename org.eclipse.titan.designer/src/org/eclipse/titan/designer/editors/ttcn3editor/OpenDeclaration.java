/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import java.util.List;

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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ASN1.definitions.ASN1Module;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.declarationsearch.IdentifierFinderVisitor;
import org.eclipse.titan.designer.editors.DeclarationCollectionHelper;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.OpenDeclarationHelper;
import org.eclipse.titan.designer.editors.OpenDeclarationLabelProvider;
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
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * TODO extract common parts from
 * org.eclipse.titan.designer.editors.ttcn3editor.OpenDeclaration
 * org.eclipse.titan.designer.editors.asn1editor.OpenDeclaration
 * org.eclipse.titan.designer.editors.asn1editor.OpenDeclaration
 * 
 * @author Kristof Szabados
 */
public final class OpenDeclaration extends AbstractHandler implements IEditorActionDelegate {
	public static final String FILENOTIDENTIFIABLE = "The file related to the editor could not be identified";
	public static final String NOTTTCN3DECLARATION = "Current text selection does not resolve to a TTCN3 declaration";
	public static final String TTCN3EDITORNOTFOUND = "The TTCN3 editor could not be found";

	private IEditorPart targetEditor = null;
	private ISelection selection = TextSelection.emptySelection();

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
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(TTCN3EDITORNOTFOUND);
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

	private void getDeclarationWithoutVisitor(final int offset, final boolean reportDebugInformation, final IFile file) {
		DeclarationCollector declarationCollector = OpenDeclarationHelper.findVisibleDeclarations(targetEditor, new TTCN3ReferenceParser(
				false), ((TTCN3Editor) targetEditor).getDocument(), offset, false);

		if (declarationCollector == null) {
			return;
		}

		DeclarationCollectionHelper declaration = null;
		List<DeclarationCollectionHelper> collected = declarationCollector.getCollected();
		if (collected.isEmpty()) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("No visible elements found");
			}
			ProjectSourceParser projectSourceParser1 = GlobalParser.getProjectSourceParser(file.getProject());
			for (String moduleName2 : projectSourceParser1.getKnownModuleNames()) {
				Module module2 = projectSourceParser1.getModuleByName(moduleName2);
				if (module2 != null && module2 instanceof ASN1Module) {
					module2.getAssignments().addDeclaration(declarationCollector);
				}
			}

			if (declarationCollector.getCollectionSize() == 0) {
				targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(NOTTTCN3DECLARATION);
				return;
			}

			if (reportDebugInformation) {
				TITANDebugConsole.println("Elements were only found in not visible modules");
			}

			OpenDeclarationLabelProvider labelProvider = new OpenDeclarationLabelProvider();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(new Shell(Display.getDefault()), labelProvider);
			dialog.setTitle("Open");
			dialog.setMessage("Select the element to open");
			dialog.setElements(collected.toArray());
			if (dialog.open() == Window.OK) {
				if (reportDebugInformation) {
					TITANDebugConsole.println("Selected: " + dialog.getFirstResult());
				}
				declaration = (DeclarationCollectionHelper) dialog.getFirstResult();
			}
		} else {
			declaration = collected.get(0);
		}
		if (reportDebugInformation) {
			for (DeclarationCollectionHelper foundDeclaration : collected) {
				TITANDebugConsole.println("Assignment:" + foundDeclaration.location.getFile() + ": "
						+ foundDeclaration.location.getOffset() + " - "
						+ foundDeclaration.location.getEndOffset());
			}
		}

		if (declaration != null) {
			selectAndRevealDeclaration(declaration.location);
		}
	}

	private void getDeclarationVisitor(final Module module, final IdentifierFinderVisitor visitor, final boolean reportDebugInformation) {

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

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		doOpenDeclaration();

		return null;
	}

	private final void doOpenDeclaration() {
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

		if (ResourceExclusionHelper.isExcluded(file)) {
			MessageDialog.openError(new Shell(Display.getDefault()), "Open Declaration does not work within excluded resources",
					"This module is excluded from build. To use the Open Declaration "
							+ "feature please click on the 'Toggle exclude from build state' in the context menu of the Project Explorer. ");
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
			offset = ((TTCN3Editor) targetEditor).getCarretOffset();
		}

		IdentifierFinderVisitor visitor = new IdentifierFinderVisitor(offset);

		ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());

		final String ttcn3ModuleName = projectSourceParser.containedModule(file);

		final Module module = projectSourceParser.getModuleByName(ttcn3ModuleName);

		if (module == null) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("Can not find the module.");
			}
			return;
		}

		// ToDo: In case of minimize memory usage, the code reverts to
		// non visitor type of get declaration.
		// Think it over.
		// if
		// (Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
		// PreferenceConstants.MINIMISEMEMORYUSAGE, true, null)) {
		//
		// getDeclarationWithoutVisitor(offset, reportDebugInformation,
		// file);

		// } else {
		getDeclarationVisitor(module, visitor, reportDebugInformation);
		// }
		return;
	}
}
