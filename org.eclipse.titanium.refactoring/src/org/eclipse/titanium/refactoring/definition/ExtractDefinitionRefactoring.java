/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.definition;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;

/**
 * This class represents the 'Extract definition' refactoring operation. The details of the operation in order:
 * <p>
 * <li>INIT
 *  <ul>
 *  <li>instantiate by using the constructor: {@link #ExtractDefinitionRefactoring()};
 *   the constructor calls {@link SelectionFinder#perform()}, 
 *   that determines which statements are selected by the user</li>
 *  <li>create a new project using the ExtractDefinitionWizard</li>
 *  <li>call {@link #setTargetProject(IProject)} with the new project as a parameter</li>
 *  <li>{@link #checkInitialConditions()} checks for the initial conditions</li>
 *  </ul>
 * </li>
 * <li>REFACTORING
 *  <ul>
 *  <li>{@link #perform()} creates the <code>copyMap</code>, which contains the parts to copy
 *   from the source project, and writes its contents into the (new and empty) target project</li>
 *  </ul>
 * </li>
 * <p>
 * Headless mode:
 * <p>
 * Use {@link #ExtractDefinitionRefactoring(IProject, Definition)}
 *  constructor instead and then call {@link #perform()}.
 * <p>
 * 
 * @author Viktor Varga
 */
public class ExtractDefinitionRefactoring {
	
	static final boolean ENABLE_COPY_COMMENTS = false;

	private IProject sourceProj;
	/** the new project to extract function dependencies into */
	private IProject targetProj;
	/** the definition which is being extracted */
	private Definition selection;
	
	/** this contains the copied dependencies during the operation */
	private Map<IPath, StringBuilder> copyMap;
	/** this contains the list of files to be copied completely */
	private List<IFile> filesToCopy;
	
	/** Use this constructor only when a workbench is available. */
	ExtractDefinitionRefactoring() {
		SelectionFinder sf = new SelectionFinder();
		sf.perform();
		selection = sf.getSelection();
		sourceProj = sf.getSourceProj();
	}
	
	/** Use this constructor in headless mode. */
	ExtractDefinitionRefactoring(IProject sourceProj, Definition selection) {
		this.selection = selection;
		this.sourceProj = sourceProj;
	}
	
	String getName() {
		return "Extract definition";
	}
	Definition getSelection() {
		return selection;
	}
	IProject getSourceProject() {
		return sourceProj;
	}

	void setTargetProject(IProject targetProj) {
		this.targetProj = targetProj;
	}

	private RefactoringStatus checkInitialConditions() {
		RefactoringStatus result = new RefactoringStatus();
		if (selection == null) {
			result.addError("Selection is not a definition (selection is null)! ");
			
		}
		if (targetProj == null) {
			result.addError("Target project is null! ");
		}
		return result;
	}
	
	void perform() {
		RefactoringStatus rs = checkInitialConditions();
		if (!rs.hasError()) {
			try {
				DependencyCollector dc = new DependencyCollector(selection);
				WorkspaceJob job1 = dc.readDependencies();
				job1.join();
				copyMap = dc.getCopyMap();
				filesToCopy = dc.getFilesToCopy();
				WorkspaceJob job2 = createChange();
				job2.join();
			} catch (InterruptedException ie) {
				ErrorReporter.logExceptionStackTrace(ie);
			}
		} else {
			ErrorReporter.logError("Initial conditions are not fulfilled for the ExtractDefinition operation");
		}
		
	}

	private WorkspaceJob createChange() {
		WorkspaceJob job = new WorkspaceJob("ExtractDefinition: writing to target project") {
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				if (copyMap == null) {
					ErrorReporter.logError("ExtractDefinition::createChange(): Reading dependencies did not finish.");
					return Status.CANCEL_STATUS;
				}
				if (filesToCopy == null) {
					ErrorReporter.logError("ExtractDefinition::createChange(): Reading dependencies did not finish (2).");
					return Status.CANCEL_STATUS;
				}
				//create files & write dependencies in them
				for (Entry<IPath, StringBuilder> e: copyMap.entrySet()) {
					IFile targetFile = createFile(e.getKey());
					if (targetFile == null) {
						ErrorReporter.logError("Unable to create file `" + e.getKey() + "' while extracting a definition.");
						return Status.CANCEL_STATUS;
					}
					TextFileChange change = new TextFileChange("extract_append", targetFile);
					MultiTextEdit rootEdit = new MultiTextEdit();
					change.setEdit(rootEdit);
					TextEdit edit = new InsertEdit(0, e.getValue().toString());
					rootEdit.addChild(edit);
					change.perform(new NullProgressMonitor());
				}
				//copy files from 'filesToCopy' without opening them
				for (IFile f: filesToCopy) {
					if (copyFile(f) == null) {
						ErrorReporter.logError("Unable to copy file `" + (f == null ? "null" : f.getProjectRelativePath())
								+ "' while extracting a definition.");
						return Status.CANCEL_STATUS;
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return job;
		
	}
	
	private IFile createFile(IPath relativePath) {
		IFile ret = targetProj.getFile(relativePath);

		//create the file
		if (!ret.exists()) {
			try {
				createTargetFolderHierarchy(ret);
				InputStream source = new ByteArrayInputStream(new byte[0]);
				ret.create(source, IResource.NONE, null);
			} catch (CoreException ce) {
				return null;
			}
		}
		return ret;
	}
	private IFile copyFile(IFile toCopy) {
		IFile newFile = targetProj.getFile(toCopy.getProjectRelativePath());

		//copy the file
		try {
			createTargetFolderHierarchy(newFile);
			IPath relPath = toCopy.getProjectRelativePath();
			toCopy.copy(targetProj.getFile(relPath).getFullPath(), true, null);
		} catch (CoreException ce) {
			return null;
		}
		return toCopy;
	}
	
	private void createTargetFolderHierarchy(final IFile file) throws CoreException {
		IContainer parent = file.getParent();
		List<IFolder> folders = new LinkedList<IFolder>();
		while (parent != null) {
			if (parent instanceof IFolder) {
				folders.add(0, (IFolder)parent);
			} else {
				break;
			}
			parent = parent.getParent();
		}

		//create all folders from project level towards file level
		for (IFolder f: folders) {
			if (!f.exists()) {
				f.create(true, true, new NullProgressMonitor());
			}
		}
	}

}
