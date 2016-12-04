/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.definition;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

/**
 * This class is only instantiated by {@link ExtractDefinitionRefactoring} once per
 * each refactoring operation. By calling {@link #perform()}, the currently
 * selected project and piece of code ({@link Definition}) is determined.
 * 
 * @author Viktor Varga
 */
class SelectionFinder {

	private static final String ERR_MSG_NO_SELECTION = "No definition to extract was found! ";
	
	/** the project from which we extract the definition */
	private IProject sourceProj;
	/** the definition which is being extracted */
	private Definition selection;
	
	public IProject getSourceProj() {
		return sourceProj;
	}

	public Definition getSelection() {
		return selection;
	}
	
	public void perform() {
		selection = findSelection();
	}

	private Definition findSelection() {
		//getting the active editor
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		TTCN3Editor targetEditor;
		if (editor == null || !(editor instanceof TTCN3Editor)) {
			return null;
		} else {
			targetEditor = (TTCN3Editor) editor;
		}

		//iterating through part of the module
		IResource selectedRes = extractResource(targetEditor);
		if (!(selectedRes instanceof IFile)) {
			ErrorReporter.logError("SelectionFinder.findSelection(): Selected resource `" + selectedRes.getName() + "' is not a file.");
			return null;
		}
		IFile selectedFile = (IFile)selectedRes;
		sourceProj = selectedFile.getProject();
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(sourceProj);
		final Module selectedModule = projectSourceParser.containedModule(selectedFile);

		//getting current selection
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		TextSelection textSelection = extractSelection(selectionService.getSelection());
		//getting current selection nodes
		int selectionOffset = textSelection.getOffset() + textSelection.getLength();
		SelectionFinderVisitor selVisitor = new SelectionFinderVisitor(selectionOffset);
		selectedModule.accept(selVisitor);
		Definition selectedDef = selVisitor.getSelection();
		if (selectedDef == null) {
			ErrorReporter.logWarning("SelectionFinder.findSelection(): Visitor did not find a definition in the selection.");
			final IStatusLineManager statusLineManager = targetEditor.getEditorSite().getActionBars().getStatusLineManager();
			statusLineManager.setErrorMessage(ERR_MSG_NO_SELECTION);
			return null;
		}
		return selectedDef;
	}

	/**
	 * Searches for a {@link Def_Function} node that contains the current selection offset
	 * (selection is inside the node).
	 */
	private static class SelectionFinderVisitor extends ASTVisitor {
		
		private Definition def;
		private final int offset;
		
		SelectionFinderVisitor(final int selectionOffset) {
			offset = selectionOffset;
		}
		
		private Definition getSelection() {
			return def;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (!(node instanceof ILocateableNode)) {
				return V_CONTINUE;
			}
			final Location loc = ((ILocateableNode) node).getLocation();
			if (loc == null) {
				return V_ABORT;
			}
			if (!loc.containsOffset(offset)) {
				// skip the children, the offset is not inside this node
				return V_SKIP;
			}
			if (node instanceof Definition) {
				if (isGoodType(node)) {
					def = (Definition)node;
				} else {
					def = null;
				}
			}
			if (node instanceof Undefined_LowerIdentifier_Value) {
				((Undefined_LowerIdentifier_Value)node).getAsReference();
				return V_CONTINUE;
			}
			if (node instanceof Reference) {
				Reference ref = (Reference)node;
				Assignment as = ref.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				if (as instanceof Definition) {
					if (isGoodType(as)) {
						def = (Definition)as;
					} else {
						def = null;
					}
				}
			}
			return V_CONTINUE;
		}
		
		private static boolean isGoodType(final IVisitableNode node) {
			if (node instanceof Definition &&
					!(node instanceof Def_Var) &&
					!(node instanceof Def_Var_Template) &&
					!(node instanceof FormalParameter)) {
				return true;
			}
			return false;
		}
		
	}
	
	
	private IResource extractResource(final IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (!(input instanceof IFileEditorInput)) {
			return null;
		}
		return ((IFileEditorInput)input).getFile();
	}
	private TextSelection extractSelection(final ISelection sel) {
		if (!(sel instanceof TextSelection)) {
			ErrorReporter.logError("Selection is not a TextSelection.");
			return null;
		}
		return (TextSelection)sel;
	}
	
}
