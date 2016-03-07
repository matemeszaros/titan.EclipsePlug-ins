/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.cfg.CfgDefinitionInformation;
import org.eclipse.titan.common.parsers.cfg.CfgInterval;
import org.eclipse.titan.common.parsers.cfg.CfgLocation;
import org.eclipse.titan.common.parsers.cfg.CfgInterval.section_type;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.editors.DeclarationCollectionHelper;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.parsers.GlobalIntervalHandler;
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
 * @author Ferenc Kovacs
 * */
public final class OpenDeclaration extends AbstractHandler implements IEditorActionDelegate {
	public static final String NOTIDENTIFIABLEFILE = "The file related to the editor could not be identified";
	public static final String NOTDEFINITION = "Selected text does not resolve to a definition";
	public static final String NOTMODULEPARDECLARATION = "Selected text does not resolve to a module parameter declaration";
	public static final String NOTCONSTANTDEFINITION = "Current text selection does not resolve to a constant definition";
	public static final String WRONGSELECTION = "Selected text cannot be mapped to a file name";
	public static final String EDITORNOTFOUND = "The configuration file editor could not be found";
	public static final String FILENOTFOUND = "Could not find included configuration file `{0}'' on include paths";

	private IEditorPart targetEditor = null;
	private ISelection selection = TextSelection.emptySelection();

	@Override
	public void run(final IAction action) {
		if (targetEditor instanceof ConfigEditor) {
			targetEditor = ((ConfigEditor) targetEditor).getEditor();
		}

		if (targetEditor == null || !(targetEditor instanceof ConfigTextEditor)) {
			return;
		}

		targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);
		IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		if (file == null) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(NOTIDENTIFIABLEFILE);
			return;
		}

		if (!TITANNature.hasTITANNature(file.getProject())) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(TITANNature.NO_TITAN_FILE_NATURE_FOUND);
			return;
		}

		int offset;
		if (!selection.isEmpty() && selection instanceof TextSelection && !"".equals(((TextSelection) selection).getText())) {
			IPreferencesService prefs = Platform.getPreferencesService();
			if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null)) {
				TITANDebugConsole.println("Selected: " + ((TextSelection) selection).getText());
			}
			TextSelection textSelection = (TextSelection) selection;
			offset = textSelection.getOffset() + textSelection.getLength();
		} else {
			offset = ((ConfigTextEditor) targetEditor).getCarretOffset();
		}

		IDocument document = ((ConfigTextEditor) targetEditor).getDocument();
		section_type section = getSection(document, offset);

		if (section_type.UNKNOWN.equals(section)) {
			return;
		} else if (section_type.INCLUDE.equals(section)) {
			handleIncludes(file, offset, document);
			return;
		} else if (section_type.MODULE_PARAMETERS.equals(section)) {
			// Module parameters are always defined in
			// [MODULE_PARAMETERS]
			// section. Don't go further if the selected text can be
			// identifiable as a module parameter.
			if (handleModuleParameters(file, offset, document)) {
				return;
			}
		}

		// Fall back.
		handleDefinitions(file, offset, document);
	}

	/**
	 * Opens an editor for the provided declaration, and in this editor the
	 * location of the declaration is revealed and highlighted.
	 * 
	 * @param file
	 *                The file to open.
	 * @param offset
	 *                The start position of the declaration to select.
	 * @param endOffset
	 *                The end position of the declaration to select.
	 * @param select
	 *                Select the given region if true.
	 */
	private void selectAndRevealRegion(final IFile file, final int offset, final int endOffset, final boolean select) {
		IWorkbenchPage page = targetEditor.getSite().getPage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		if (desc == null) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(EDITORNOTFOUND);
			return;
		}
		try {
			IEditorPart editorPart = page.openEditor(new FileEditorInput(file), desc.getId());
			if (!select) {
				return;
			}
			if (editorPart != null) {
				// Check the editor instance. It's usually a
				// ConfigEditor and
				// not AbstractTextEditor.
				if (editorPart instanceof ConfigEditor) {
					((AbstractTextEditor) ((ConfigEditor) editorPart).getEditor()).selectAndReveal(offset, endOffset - offset);
				} else if (editorPart instanceof AbstractTextEditor) {
					((AbstractTextEditor) editorPart).selectAndReveal(offset, endOffset - offset);
				}
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

	/**
	 * Display a list of items in a dialog and return the selected item as
	 * an object.
	 * 
	 * @param collected
	 *                The list of items to be displayed in the dialog.
	 * @return The object selected from the dialog.
	 */
	public Object openCollectionListDialog(final List<?> collected) {
		OpenDeclarationLabelProvider labelProvider = new OpenDeclarationLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(null, labelProvider);
		dialog.setTitle("Open");
		dialog.setMessage("Select the element to open");
		dialog.setElements(collected.toArray());
		if (dialog.open() == Window.OK) {
			return dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Provides position dependent section information.
	 * 
	 * @param document
	 *                The document where the check takes place.
	 * @param offset
	 *                The current position of the cursor.
	 * @return The type of the section for the current position of the
	 *         cursor.
	 */
	public section_type getSection(final IDocument document, final int offset) {
		Interval interval = GlobalIntervalHandler.getInterval(document);

		if (interval == null) {
			return section_type.UNKNOWN;
		}

		for (Interval subInterval : interval.getSubIntervals()) {
			int startOffset = subInterval.getStartOffset();
			int endOffset = subInterval.getEndOffset();
			if (subInterval instanceof CfgInterval && startOffset <= offset && endOffset >= offset) {
				return ((CfgInterval) subInterval).getSectionType();
			}
		}

		return section_type.UNKNOWN;
	}

	/**
	 * Opens the included configuration file selected by the user.
	 * 
	 * @param file
	 *                The current file.
	 * @param offset
	 *                The position of the cursor.
	 * @param document
	 *                The document opened in the configuration file editor.
	 */
	public void handleIncludes(final IFile file, final int offset, final IDocument document) {
		ConfigReferenceParser refParser = new ConfigReferenceParser(false);
		String include = refParser.findIncludedFileForOpening(offset, document);

		if (include == null || include.length() == 0) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(WRONGSELECTION);
			return;
		}

		IFile fileToOpen = file.getProject().getFile(include);
		if (fileToOpen == null || !fileToOpen.exists()) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager()
					.setErrorMessage(MessageFormat.format(FILENOTFOUND, include));
			return;
		}

		selectAndRevealRegion(fileToOpen, -1, -1, false);
	}

	/**
	 * Selects and reveals the selected module parameter.
	 * 
	 * @param file
	 *                The current file.
	 * @param offset
	 *                The position of the cursor.
	 * @param document
	 *                The document opened in the configuration file editor.
	 * @return True
	 */
	public boolean handleModuleParameters(final IFile file, final int offset, final IDocument document) {
		ConfigReferenceParser refParser = new ConfigReferenceParser(false);
		Reference reference = refParser.findReferenceForOpening(file, offset, document);
		if (refParser.isModuleParameter()) {
			if (reference == null) {
				return false;
			}
			DeclarationCollector declarationCollector = new DeclarationCollector(reference);
			ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
			String exactModuleName = refParser.getExactModuleName();

			if (exactModuleName != null) {
				Module module = projectSourceParser.getModuleByName(exactModuleName);
				if (module != null) {
					Assignments assignments = module.getAssignments();
					for (int i = 0; i < assignments.getNofAssignments(); i++) {
						assignments.getAssignmentByIndex(i).addDeclaration(declarationCollector, 0);
					}
				}
			} else {
				for (String moduleName : projectSourceParser.getKnownModuleNames()) {
					Module module = projectSourceParser.getModuleByName(moduleName);
					if (module != null) {
						Assignments assignments = module.getAssignments();
						for (int i = 0; i < assignments.getNofAssignments(); i++) {
							assignments.getAssignmentByIndex(i).addDeclaration(declarationCollector, 0);
						}
					}
				}
			}

			if (declarationCollector.getCollectionSize() == 0) {
				targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(NOTMODULEPARDECLARATION);
				return false;
			}

			List<DeclarationCollectionHelper> collected = declarationCollector.getCollected();
			DeclarationCollectionHelper declaration = null;

			if (exactModuleName != null && collected.size() == 1) {
				declaration = collected.get(0);
			} else {
				Object result = openCollectionListDialog(collected);
				if (result != null) {
					declaration = (DeclarationCollectionHelper) result;
				}
			}
			IPreferencesService prefs = Platform.getPreferencesService();
			if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null)) {
				for (DeclarationCollectionHelper foundDeclaration : collected) {
					TITANDebugConsole.println("Module parameter: " + foundDeclaration.location.getFile() + ":"
									+ foundDeclaration.location.getOffset() + "-"
									+ foundDeclaration.location.getEndOffset());
				}
			}

			if (declaration != null) {
				selectAndRevealRegion((IFile) declaration.location.getFile(), declaration.location.getOffset(),
						declaration.location.getEndOffset(), true);
			}

			return true;
		}

		return false;
	}

	/**
	 * Jumps to the definition of the constant selected by the user.
	 * 
	 * @param file
	 *                The current file.
	 * @param offset
	 *                The position of the cursor.
	 * @param document
	 *                The document opened in the configuration file editor.
	 */
	public void handleDefinitions(final IFile file, final int offset, final IDocument document) {
		ConfigReferenceParser refParser = new ConfigReferenceParser(false);
		refParser.findReferenceForOpening(file, offset, document);
		List<ConfigDeclarationCollectionHelper> collected = new ArrayList<ConfigDeclarationCollectionHelper>();
		Map<String, CfgDefinitionInformation> definitions = GlobalParser.getConfigSourceParser(file.getProject()).getAllDefinitions();
		String definitionName = refParser.getDefinitionName();

		if (definitionName == null) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(NOTDEFINITION);
			return;
		}

		CfgDefinitionInformation definition = definitions.get(definitionName);
		ConfigDeclarationCollectionHelper declaration = null;

		if (definition != null) {
			List<CfgLocation> locations = definition.getLocations();
			for (CfgLocation location : locations) {
				collected.add(new ConfigDeclarationCollectionHelper(definitionName, location));
			}
		} else {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(NOTCONSTANTDEFINITION);
			return;
		}

		if (collected.isEmpty()) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(NOTCONSTANTDEFINITION);
			return;
		}

		if (collected.size() == 1) {
			declaration = collected.get(0);
		} else {
			Object result = openCollectionListDialog(collected);
			if (result != null) {
				declaration = (ConfigDeclarationCollectionHelper) result;
			}
		}

		if (declaration != null) {
			selectAndRevealRegion(declaration.location.getFile(), declaration.location.getOffset(), declaration.location.getEndOffset(),
					true);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (targetEditor instanceof ConfigEditor) {
			targetEditor = ((ConfigEditor) targetEditor).getEditor();
		}

		if (targetEditor == null || !(targetEditor instanceof ConfigTextEditor)) {
			return null;
		}

		targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);
		IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		if (file == null) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(NOTIDENTIFIABLEFILE);
			return null;
		}

		if (!TITANNature.hasTITANNature(file.getProject())) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(TITANNature.NO_TITAN_FILE_NATURE_FOUND);
			return null;
		}

		int offset;
		if (!selection.isEmpty() && selection instanceof TextSelection && !"".equals(((TextSelection) selection).getText())) {
			IPreferencesService prefs = Platform.getPreferencesService();
			if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null)) {
				TITANDebugConsole.println("Selected: " + ((TextSelection) selection).getText());
			}
			TextSelection textSelection = (TextSelection) selection;
			offset = textSelection.getOffset() + textSelection.getLength();
		} else {
			offset = ((ConfigTextEditor) targetEditor).getCarretOffset();
		}

		IDocument document = ((ConfigTextEditor) targetEditor).getDocument();
		section_type section = getSection(document, offset);

		if (section_type.UNKNOWN.equals(section)) {
			return null;
		} else if (section_type.INCLUDE.equals(section)) {
			handleIncludes(file, offset, document);
			return null;
		} else if (section_type.MODULE_PARAMETERS.equals(section)) {
			// Module parameters are always defined in
			// [MODULE_PARAMETERS]
			// section. Don't go further if the selected text can be
			// identifiable as a module parameter.
			if (handleModuleParameters(file, offset, document)) {
				return null;
			}
		}

		// Fall back.
		handleDefinitions(file, offset, document);
		return null;
	}
}
