/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.visibility;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This class represents the 'Minimize visibility modifiers' refactoring operation.
 * <p>
 * This refactoring operation minimizes all visibility modifiers in the given
 *   files/folders/projects, which are contained in a {@link IStructuredSelection} object.
 * The operation can be executed using the mechanisms in the superclass, through a wizard for example
 * 
 * @author Viktor Varga
 */
public class MinimizeVisibilityRefactoring extends Refactoring {
	public static final String PROJECTCONTAINSERRORS = "The project `{0}'' contains errors, which might corrupt the result of the refactoring";
	public static final String PROJECTCONTAINSTTCNPPFILES = "The project `{0}'' contains .ttcnpp files, which might corrupt the result of the refactoring";
	private static final String ONTHEFLYANALAYSISDISABLED = "The on-the-fly analysis is disabled, there is semantic information present to work on";
	private static final String MINIMISEWARNING = "Minimise memory usage is enabled, which can cause unexpected behaviour in the refactoring process!\n"
			+ "Refactoring is not supported with the memory minimise option turned on, "
			+ "we do not take any responsibility for it.";

	private IStructuredSelection selection;
	private Set<IProject> projects = new HashSet<IProject>();

	private Object[] affectedObjects;		//the list of objects affected by the change
	
	/*
	 * TODO dev:
	 *
	 * 
	 * TODO fix:
	 * 
	 * */
	
	public MinimizeVisibilityRefactoring(IStructuredSelection selection) {
		this.selection = selection;
		
		Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof IResource) {
				IProject temp = ((IResource) o).getProject();
				projects.add(temp);
			}
		}
	}

	Object[] getAffectedObjects() {
		return affectedObjects;
	}
	
	//METHODS FROM REFACTORING

	@Override
	public String getName() {
		return "Minimize visibility modifiers";
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		try {
			pm.beginTask("Checking preconditions...", 3);
			
			final IPreferencesService prefs = Platform.getPreferencesService();//PreferenceConstants.USEONTHEFLYPARSING
			if (! prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, false, null)) {
				result.addError(ONTHEFLYANALAYSISDISABLED);
			}

			// check that there are no ttcnpp files in the
			// project
			for (IProject project : projects) {
				if (hasTtcnppFiles(project)) {//FIXME actually all referencing and referenced projects need to be checked too !
					result.addError(MessageFormat.format(PROJECTCONTAINSTTCNPPFILES, project));
				}
			}
			
			pm.worked(1);
			// check that there are no error markers in the
			// project
			for (IProject project : projects) {
				IMarker[] markers = project.findMarkers(null, true, IResource.DEPTH_INFINITE);
				for (IMarker marker : markers) {
					if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
						result.addError(MessageFormat.format(PROJECTCONTAINSERRORS, project));
						break;
					}
				}
			}
			pm.worked(1);

			
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
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		return result;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (selection == null) {
			return null;
		}
		CompositeChange cchange = new CompositeChange("MinimizeVisibilityRefactoring");
		Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (!(o instanceof IResource)) {
				continue;
			}
			IResource res = (IResource)o;
			ResourceVisitor vis = new ResourceVisitor(cchange);
			res.accept(vis);
		}
		affectedObjects = cchange.getAffectedObjects();
		return cchange;
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
	//METHODS FROM REFACTORING END
	

	
	/**
	 * Visits all the files of a folder or project (any {@link IResource}).
	 * Creates the {@link Change} for all files and then merges them into a single
	 * {@link CompositeChange}.
	 * <p>
	 * Call on any {@link IResource} object.
	 *  */
	private class ResourceVisitor implements IResourceVisitor {

		private final CompositeChange change;
		
		public ResourceVisitor(CompositeChange change) {
			this.change = change;
		}
		
		CompositeChange getChange() {
			return change;
		}
		
		@Override
		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				ChangeCreator chCreator = new ChangeCreator((IFile)resource);
				chCreator.perform();
				Change ch = chCreator.getChange();
				if (ch != null) {
					change.add(ch);
				}
				//SKIP
				return false;
			}
			//CONTINUE
			return true;
		}
		
	}
	
}
