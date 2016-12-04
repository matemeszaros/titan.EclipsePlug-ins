/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.modulepar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * This class represents the 'Extract modulepar' refactoring operation. The details of the operation in order:
 * <p>
 * <li>INIT
 *  <ul>
 *  <li>instantiate by using the constructor: {@link #ExtractModuleParRefactoring(IProject)}</li>
 *  <li>create a new project using the ExtractDefinitionWizard</li>
 *  <li>call {@link #setTargetProject(IProject)} with the new project as a parameter</li>
 *  </ul>
 * </li>
 * <li>REFACTORING
 *  <ul>
 *  <li>{@link #perform()} collects the module parameter statements in the source project, using the
 *   {@link SelectionFinder} class; after that {@link DependencyCollector} creates the <code>copyMap</code>,
 *   which contains the parts to copy from the source project, and writes its contents 
 *   into the (new and empty) target project</li>
 *  </ul>
 * </li>
 * <p>
 * Headless mode:
 * <p>
 * Use {@link #ExtractModuleParRefactoring(IProject)}
 *  constructor, then optionally use {@link #setOption_saveModuleParList(boolean)} 
 *  and then call {@link #perform()}.
 * <p>
 * 
 * @author Viktor Varga
 */
public class ExtractModuleParRefactoring  {

	static final boolean ENABLE_COPY_COMMENTS = false;

	private static final IPath PATH_MODULEPAR_LIST_FILE_OUTPUT = Path.fromOSString("modulepars.txt");
	
	private final IProject sourceProj;
	/** the new project to extract function dependencies into */
	private IProject targetProj;
	
	private boolean option_saveModuleParList = false;

	/** this contains the copied dependencies during the operation */
	private Map<IPath, StringBuilder> copyMap;
	/** this contains the list of files to be copied completely */
	private List<IFile> filesToCopy;
	
	/*
	 * TODO
	 * 
	 * 
	 * */
	

	/** Use this constructor only when a workbench is available. */
	ExtractModuleParRefactoring(final IProject sourceProj) {
		this.sourceProj = sourceProj;
	}

	void setTargetProject(final IProject targetProj) {
		this.targetProj = targetProj;
	}
	void setOption_saveModuleParList(final boolean option_saveModuleParList) {
		this.option_saveModuleParList = option_saveModuleParList;
	}
	public IProject getSourceProj() {
		return sourceProj;
	}

	void perform() {
		RefactoringStatus rs = checkInitialConditions();
		if (!rs.hasError()) {
			try {
				//TODO make a workspacejob from selection finder too?
				SelectionFinder selFinder = new SelectionFinder(sourceProj);
				selFinder.perform();
				if (option_saveModuleParList) {
					saveModuleParListToFile(selFinder.createModuleParListForSaving());
				}
				DependencyCollector dc = new DependencyCollector(selFinder.getModulePars(), sourceProj);
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

	private RefactoringStatus checkInitialConditions() {
		RefactoringStatus result = new RefactoringStatus();
		if (targetProj == null) {
			result.addError("Target project is null! ");
		}
		return result;
	}

	private WorkspaceJob createChange() {
		WorkspaceJob job = new WorkspaceJob("ExtractModulePar: writing to target project") {
			
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				if (copyMap == null) {
					ErrorReporter.logError("ExtractModuleParRefactoring::createChange(): Reading dependencies did not finish.");
					return Status.CANCEL_STATUS;
				}
				if (filesToCopy == null) {
					ErrorReporter.logError("ExtractModuleParRefactoring::createChange(): Reading dependencies did not finish (2).");
					return Status.CANCEL_STATUS;
				}
				//create files & write dependencies in them
				for (Entry<IPath, StringBuilder> e: copyMap.entrySet()) {
					IFile targetFile = createFile(e.getKey());
					if (targetFile == null) {
						ErrorReporter.logError("Unable to create file `" + e.getKey() + "' while extracting module parameters.");
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
								+ "' while extracting module aprameters.");
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
	
	private void saveModuleParListToFile(final String content) {
		IFile newFile = targetProj.getFile(PATH_MODULEPAR_LIST_FILE_OUTPUT);
		if (!newFile.exists()) {
			try {
				createTargetFolderHierarchy(newFile);
				InputStream source = new ByteArrayInputStream(new byte[0]);
				newFile.create(source, IResource.NONE, null);
			} catch (CoreException ce) {
				ErrorReporter.logError("ExtractModuleParRefactoring.saveModuleParListToFile(): CoreException while creating file: " + ce.getLocalizedMessage());
				return;
			}
		}
		InputStream is = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))); //TODO update with Java 1.7 to StandardCharsets.UTF_8

		try {
			newFile.setContents(is, false, false, null);
		} catch (CoreException e) {
			ErrorReporter.logError("ExtractModuleParRefactoring.saveModuleParListToFile(): CoreException while writing to file: " + e.getLocalizedMessage());
		}
	}
	

	private IFile createFile(final IPath relativePath) {
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
	private IFile copyFile(final IFile toCopy) {
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
