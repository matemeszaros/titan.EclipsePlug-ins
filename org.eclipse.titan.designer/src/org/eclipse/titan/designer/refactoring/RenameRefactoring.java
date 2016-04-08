/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.refactoring;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTLocationChainVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NamedBridgeScope;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.SubScopeVisitor;
import org.eclipse.titan.designer.AST.ASN1.definitions.ASN1Module;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Enumerated_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Enumerated_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.declarationsearch.IdentifierFinderVisitor;
import org.eclipse.titan.designer.editors.IEditorWithCarretOffset;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IEditorPart;

/**
 * FIXME component variables are not handled correctly
 * @author Adam Delic
 * */
public class RenameRefactoring extends Refactoring {
	public static final String FILENOTIDENTIFIABLE = "The file related to the editor could not be identified";
	public static final String NORECOGNISABLEMODULENAME = "The name of the module in the file `{0}'' could not be identified";
	public static final String EXCLUDEDFROMBUILD = "The name of the module in the file `{0}'' could not be identified, the file is excluded from build";
	public static final String NOTFOUNDMODULE = "The module `{0}'' could not be found";
	public static final String PROJECTCONTAINSERRORS = "The project `{0}'' contains errors, which might corrupt the result of the refactoring";
	public static final String PROJECTCONTAINSTTCNPPFILES = "The project `{0}'' contains .ttcnpp files, which might corrupt the result of the refactoring";
	public static final String FIELDALREADYEXISTS = "Field with name `{0}'' already exists in type `{1}''";
	public static final String DEFINITIONALREADYEXISTS = "Name conflict:"
			+ " definition with name `{0}'' already exists in the scope of the selected definition or in one of its parent scopes";
	public static final String DEFINITIONALREADYEXISTS2 = "Name conflict:"
			+ " definition with name `{0}'' already exists in module `{1}'' at line {2}";

	private static final String MINIMISEWARNING = "Minimise memory usage is enabled, which can cause unexpected behaviour in the refactoring process!\n"
			+ "Refactoring is not supported with the memory minimise option turned on, "
			+ "we do not take any responsibility for it.";

	final IFile file;
	final Module module;
	Map<Module, List<Hit>> idsMap = null;
	// found identifiers will be renamed to this
	String newIdentifierName;
	ReferenceFinder rf;

	public RenameRefactoring(final IFile file, final Module module, final ReferenceFinder rf) {
		super();
		this.file = file;
		this.module = module;
		this.rf = rf;
	}

	@Override
	public String getName() {
		return "Rename " + rf.getSearchName();
	}

	public Module getModule() {
		return module;
	}

	public Identifier getRefdIdentifier() {
		return rf.getReferredIdentifier();
	}

	public void setNewIdentifierName(final String newIdentifierName) {
		this.newIdentifierName = newIdentifierName;
	}

	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException {

		// for debugging
		// ScopeHierarchyVisitor v = new ScopeHierarchyVisitor();
		// module.accept(v);
		// TITANDebugConsole.getConsole().newMessageStream().println(v.getScopeTreeAsHTMLPage());

		RefactoringStatus result = new RefactoringStatus();
		try {
			pm.beginTask("Checking preconditions...", 3);
			// check that there are no ttcnpp files in the
			// project
			if (hasTtcnppFiles(file.getProject())) {//FIXME actually all referencing and referenced projects need to be checked too !
				result.addError(MessageFormat.format(PROJECTCONTAINSTTCNPPFILES, file.getProject()));
			}
			pm.worked(1);
			// check that there are no error markers in the
			// project
			IMarker[] markers = file.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			for (IMarker marker : markers) {
				if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
					result.addError(MessageFormat.format(PROJECTCONTAINSERRORS, file.getProject()));
					break;
				}
			}
			pm.worked(1);

			final IPreferencesService prefs = Platform.getPreferencesService();
			if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.MINIMISEMEMORYUSAGE, false, null)) {
				result.addError(MINIMISEWARNING);
			}
			pm.worked(1);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			result.addFatalError(e.getMessage());
		} finally {
			pm.done();
		}
		return result;
	}

	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor pm) throws CoreException {
		RefactoringStatus result = new RefactoringStatus();
		final boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);
		// search
		idsMap = rf.findAllReferences(module, file.getProject(), pm, reportDebugInformation);
		// add the referred identifier to the map of found identifiers
		Identifier refdIdentifier = rf.getReferredIdentifier();
		Module refdModule = rf.assignment.getMyScope().getModuleScope();
		if (idsMap.containsKey(refdModule)) {
			idsMap.get(refdModule).add(new Hit(refdIdentifier));
		} else {
			ArrayList<Hit> identifierList = new ArrayList<Hit>();
			identifierList.add(new Hit(refdIdentifier));
			idsMap.put(refdModule, identifierList);
		}

		// check if there are name collisions in any of the affected
		// scopes
		if (rf.fieldId == null) {
			// check that in all affected scopes there is no
			// definition with the new name
			Identifier.Identifier_type idType = Identifier_type.ID_TTCN;
			if (rf.scope.getModuleScope() instanceof ASN1Module) {
				idType = Identifier_type.ID_ASN;
			}
			Identifier newId = new Identifier(idType, newIdentifierName);
			// check for assignment with given id in all sub-scopes
			// of the assignment's scope
			// TODO: this does not detect runs on <-> component
			// member conflicts because the RunsOnScope is not a
			// sub-scope of the ComponentTypeBody scope,
			// also it does not go into other modules
			Scope rootScope = rf.assignment.getMyScope();
			if (rootScope instanceof NamedBridgeScope && rootScope.getParentScope() != null) {
				rootScope = rootScope.getParentScope();
			}
			SubScopeVisitor subScopeVisitor = new SubScopeVisitor(rootScope);
			module.accept(subScopeVisitor);
			List<Scope> subScopes = subScopeVisitor.getSubScopes();
			subScopes.add(rootScope);
			for (Scope ss : subScopes) {
				if (ss.hasAssignmentWithId(CompilationTimeStamp.getBaseTimestamp(), newId)) {
					List<ISubReference> subReferences = new ArrayList<ISubReference>();
					subReferences.add(new FieldSubReference(newId));
					Reference reference = new Reference(null, subReferences);
					Assignment assignment = ss.getAssBySRef(CompilationTimeStamp.getBaseTimestamp(), reference);
					if (assignment != null && assignment.getLocation() != null) {
						result.addError(MessageFormat.format(DEFINITIONALREADYEXISTS2, newId.getDisplayName(),
								module.getName(), assignment.getLocation().getLine()));
					} else {
						result.addError(MessageFormat.format(DEFINITIONALREADYEXISTS, newId.getDisplayName()));
					}
					// to avoid spam and multiple messages for the same conflict
					return result;
				}
			}
		} else {
			boolean alreadyExists = false;
			// check if the type has already a field with the new
			// name
			if (rf.type instanceof TTCN3_Set_Seq_Choice_BaseType) {
				alreadyExists = ((TTCN3_Set_Seq_Choice_BaseType) rf.type).hasComponentWithName(newIdentifierName);
			} else if (rf.type instanceof TTCN3_Enumerated_Type) {
				alreadyExists = ((TTCN3_Enumerated_Type) rf.type).hasEnumItemWithName(new Identifier(Identifier_type.ID_TTCN,
						newIdentifierName));
			} else if (rf.type instanceof ASN1_Choice_Type) {
				alreadyExists = ((ASN1_Choice_Type) rf.type).hasComponentWithName(new Identifier(Identifier_type.ID_ASN,
						newIdentifierName));
			} else if (rf.type instanceof ASN1_Enumerated_Type) {
				alreadyExists = ((ASN1_Enumerated_Type) rf.type).hasEnumItemWithName(new Identifier(Identifier_type.ID_ASN,
						newIdentifierName));
			} else if (rf.type instanceof ASN1_Sequence_Type) {
				alreadyExists = ((ASN1_Sequence_Type) rf.type).hasComponentWithName(new Identifier(Identifier_type.ID_ASN,
						newIdentifierName));
			} else if (rf.type instanceof ASN1_Set_Type) {
				alreadyExists = ((ASN1_Set_Type) rf.type).hasComponentWithName(new Identifier(Identifier_type.ID_ASN,
						newIdentifierName));
			}
			if (alreadyExists) {
				result.addError(MessageFormat.format(FIELDALREADYEXISTS, newIdentifierName, rf.type.getTypename()));
			}
		}

		return result;
	}

	@Override
	public Change createChange(final IProgressMonitor pm) throws CoreException {
		CompositeChange result = new CompositeChange(getName());
		// add the change of all found identifiers grouped by module
		boolean isAsnRename = module.getModuletype() == Module.module_type.ASN_MODULE;
		String newTtcnIdentifierName = newIdentifierName;
		if (isAsnRename) {
			newTtcnIdentifierName = Identifier.getTtcnNameFromAsnName(newIdentifierName);
		}

		List<IFile> filesToProcess = new ArrayList<IFile>(idsMap.size());

		for (Module m : idsMap.keySet()) {
			List<Hit> hitList = idsMap.get(m);
			boolean isTtcnModule = m.getModuletype() == Module.module_type.TTCN3_MODULE;
			IFile file = (IFile) hitList.get(0).identifier.getLocation().getFile();
			TextFileChange tfc = new TextFileChange(file.getName(), file);
			result.add(tfc);
			MultiTextEdit rootEdit = new MultiTextEdit();
			tfc.setEdit(rootEdit);
			for (Hit hit : hitList) {
				int offset = hit.identifier.getLocation().getOffset();
				int length = hit.identifier.getLocation().getEndOffset() - offset;
				String newName = isTtcnModule ? newTtcnIdentifierName : newIdentifierName;
				// special case: referencing the definition from
				// another module without a module name prefix
				// and there is a definition with the new name
				// in the scope of the reference.
				// The module name must be added to the
				// reference.
				if (rf.fieldId == null
						&& hit.reference != null
						&& hit.reference.getModuleIdentifier() == null
						&& rf.assignment.getMyScope().getModuleScope() != hit.reference.getMyScope().getModuleScope()
						&& hit.reference.getMyScope().hasAssignmentWithId(
								CompilationTimeStamp.getBaseTimestamp(),
								new Identifier(isTtcnModule ? Identifier_type.ID_TTCN : Identifier_type.ID_ASN,
										newIdentifierName))) {
					newName = rf.assignment.getMyScope().getModuleScope().getName() + "." + newName;
				}
				rootEdit.addChild(new ReplaceEdit(offset, length, newName));
			}

			filesToProcess.add((IFile) m.getLocation().getFile());
		}

		return result;
	}

	public static boolean hasTtcnppFiles(final IResource resource) throws CoreException {
		if (resource instanceof IProject || resource instanceof IFolder) {
			IResource[] children = resource instanceof IFolder ? ((IFolder) resource).members() : ((IProject) resource).members();
			for (IResource res : children) {
				if (hasTtcnppFiles(res)) {
					return true;
				}
			}
		} else if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			return "ttcnpp".equals(file.getFileExtension());
		}
		return false;
	}

	/**
	 * Helper function used by RenameRefactoringAction classes for TTCN-3,
	 * ASN.1 and TTCNPP editors
	 */
	public static void runAction(final IEditorPart targetEditor, final ISelection selection) {
		final IStatusLineManager statusLineManager = targetEditor.getEditorSite().getActionBars().getStatusLineManager();
		statusLineManager.setErrorMessage(null);

		final IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		if (file == null) {
			statusLineManager.setErrorMessage(FILENOTIDENTIFIABLE);
			return;
		}

		if (!TITANNature.hasTITANNature(file.getProject())) {
			statusLineManager.setErrorMessage(TITANNature.NO_TITAN_FILE_NATURE_FOUND);
			return;
		}

		final IPreferencesService prefs = Platform.getPreferencesService();
		final boolean reportDebugInformation = prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);

		int offset;
		if (selection instanceof TextSelection && !selection.isEmpty() && !"".equals(((TextSelection) selection).getText())) {
			if (reportDebugInformation) {
				TITANDebugConsole.getConsole().newMessageStream().println("text selected: " + ((TextSelection) selection).getText());
			}
			TextSelection tSelection = (TextSelection) selection;
			offset = tSelection.getOffset() + tSelection.getLength();
		} else {
			offset = ((IEditorWithCarretOffset) targetEditor).getCarretOffset();
		}

		// run semantic analysis to have up-to-date AST
		// FIXME: this doesn't work if Delay semantic .... is checked
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
		final WorkspaceJob job = projectSourceParser.analyzeAll();
		if (job == null) {
			TITANDebugConsole.getConsole().newMessageStream()
					.println("Rename refactoring: WorkspaceJob to analyze project could not be created.");
			return;
		}
		try {
			job.join();
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return;
		}

		// find the module
		final String moduleName = projectSourceParser.containedModule(file);
		if (moduleName == null) {
			if (ResourceExclusionHelper.isExcluded(file)) {
				targetEditor.getEditorSite().getActionBars().getStatusLineManager()
						.setErrorMessage(MessageFormat.format(EXCLUDEDFROMBUILD, file.getFullPath()));
			}
			statusLineManager.setErrorMessage(MessageFormat.format(NORECOGNISABLEMODULENAME, file.getFullPath()));
			return;
		}
		final Module module = projectSourceParser.getModuleByName(moduleName);
		if (module == null) {
			statusLineManager.setErrorMessage(MessageFormat.format(NOTFOUNDMODULE, moduleName));
			return;
		}

		ReferenceFinder rf = findOccurrencesLocationBased(module, offset);

		if (rf == null) {
			rf = new ReferenceFinder();
			boolean isDetected = rf.detectAssignmentDataByOffset(module, offset, targetEditor, true, reportDebugInformation);
			if (!isDetected) {
				return;
			}
		}

		RenameRefactoring renameRefactoring = new RenameRefactoring(file, module, rf);
		RenameRefactoringWizard renameWizard = new RenameRefactoringWizard(renameRefactoring);
		RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(renameWizard);
		try {
			operation.run(targetEditor.getEditorSite().getShell(), "");
		} catch (InterruptedException irex) {
			// operation was canceled
		} finally {
			Map<Module, List<Hit>> changed = rf.findAllReferences(module, file.getProject(), null, reportDebugInformation);
			for(Module tempModule : changed.keySet()) {
				ProjectSourceParser tempSourceParser = GlobalParser.getProjectSourceParser(tempModule.getProject());
				tempSourceParser.reportOutdating((IFile)tempModule.getLocation().getFile());
			}
			projectSourceParser.reportOutdating(file);
			projectSourceParser.analyzeAll();
		}
	}

	/**
	 * Finds the occurrences of the element located on the given offset.
	 * This search is based on the {@link ASTLocationChainVisitor}.
	 *
	 * @param module
	 *                The module to search the occurrences in
	 * @param offset
	 *                An offset in the module
	 * @return The referencefinder
	 */
	protected static ReferenceFinder findOccurrencesLocationBased(final Module module, final int offset) {
		final IdentifierFinderVisitor visitor = new IdentifierFinderVisitor(offset);
		module.accept(visitor);
		//FIXME the thing to be refactored might be a field ... not a definition
		final Declaration def = visitor.getReferencedDeclaration();

		if (def == null || !def.shouldMarkOccurrences()) {
			return null;
		}

		return def.getReferenceFinder(module);
	}

}
