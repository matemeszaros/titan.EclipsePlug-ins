/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.console.TITANDebugConsole;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.utils.ActionUtils;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.utils.SelectionUtils;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * ResourceChangeListener class responsible for closing open MSC views
 * and to clean up cache entries. The listener is invoked when a resource
 * changed has occurred.
 * 
 * This class is the sole responsible for making sure that the opened views
 * are closed.
 * 
 */
public class ResourceListener implements IResourceChangeListener {

	private void closeMSCWindows(final IResource resource) {

		Display display = Display.getDefault();

		// most join the UI thread to be find activeWorkbench pages
		display.asyncExec(new Runnable() {

			@Override
			public void run() {

				IWorkbench workbench = PlatformUI.getWorkbench();
				if (workbench == null) {
					return;
				}

				IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
				if (activeWorkbenchWindow == null) {
					return;
				}

				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
				if (activePage != null) {
					IViewReference[] viewReferences = activePage.getViewReferences();
					ActionUtils.closeAssociatedViews(activePage, viewReferences, resource);
				}

			}

		});
	}

	
	
	/**
	 * This is the callback method as defined by the IResourceChange listener interface
	 * 
	 * The processing requires knowledge about the event model.
	 * <li>A POST_CHANGE event does *NOT* include any resource object in the event object!
	 * <li>A PRE_DELETE and PRE_CLOSE is *ONLY* issued for project events
	 * 
	 */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {

		
		
		// note: these are the only events that we subscribe upon
		switch (event.getType()) {
		case IResourceChangeEvent.POST_CHANGE: // handles the delete of files and folder
			handlePostChange(event);
			break;

		case IResourceChangeEvent.PRE_DELETE: // only called *before* delete projects!
			IResource delResource = event.getResource();
			handlePreDeleteProject(delResource);
			break;

		case IResourceChangeEvent.PRE_CLOSE: // only called *before* close project
			IResource closeResource = event.getResource();
			handlePreCloseProject(closeResource);
			break;
			
		default:
			break;

		}
	}

	/**
	 * Helper method that checks if the project is of log viewer nature 
	 * 
	 * @param resource The resource to check
	 * @return true if the project has TITAN log viewer nature otherwise false
	 */
	private boolean changeInLogViewerProject(final IResource resource) {
		try {
			if ((resource != null) && resource.getProject().hasNature(Constants.NATURE_ID)) {
				return true;
			}
		} catch (CoreException e) {
			// Do Nothing
		}

		return false;
	}

	private void handlePreDeleteProject(final IResource resource) {

		if (!changeInLogViewerProject(resource)) {
			return;
		}
		
		IProject project = resource.getProject();

		if (Constants.DEBUG) {
			TITANDebugConsole.getConsole().newMessageStream().println("Project delete " + project.getName()); //$NON-NLS-1$
		}
		
		closeMSCWindows(project);
	}

	private void handlePreCloseProject(final IResource resource) {
		if (!changeInLogViewerProject(resource)) {
			return;
		}
		
		IProject project = resource.getProject();
		
		if (Constants.DEBUG) {
			TITANDebugConsole.getConsole().newMessageStream().println("Project close " + project.getName()); //$NON-NLS-1$
		}
		
		closeMSCWindows(project);
	}

	
	
	/**
	 * Callback method that handles all post changes
	 * Implemented using the design pattern visitor that traverses
	 * through the changed resources
	 * 
	 * @param event The change event to handle
	 */
	private void handlePostChange(final IResourceChangeEvent event) {
		
		

		IResourceDelta rootDelta = event.getDelta();

		if (rootDelta == null) {
			return;
		}

		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

			@Override
			public boolean visit(final IResourceDelta delta) {

				if (delta.getKind() != IResourceDelta.REMOVED) {
					return true;
				}

				IResource resource = delta.getResource();

				if (!changeInLogViewerProject(resource)) {
					return true;
				}

				switch (resource.getType()) {
					case IResource.FILE:
						IFile file = (IFile) resource;
						if (Constants.DEBUG) {
							TITANDebugConsole.getConsole().newMessageStream().println("File delete " + file.getName()); //$NON-NLS-1$
						}
						
						if (SelectionUtils.hasLogFileExtension(file)) {
							closeMSCWindows(file);
						}
						return false;
					case IResource.FOLDER:
						IFolder folder = (IFolder) resource;
						
						if (Constants.DEBUG) {
							TITANDebugConsole.getConsole().newMessageStream().println("Folder delete " + folder.getName()); //$NON-NLS-1$
						}
						
						closeMSCWindows(folder);
						return false;
					default:
						return true;
				}
			}

		};

		try {
			rootDelta.accept(visitor);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			String messageText = Messages.getString("ResourceListener.0") + ' ' + e.getMessage();  //$NON-NLS-1$
			TitanLogExceptionHandler.handleException(new TechnicalException(messageText));
		}

	}

}
