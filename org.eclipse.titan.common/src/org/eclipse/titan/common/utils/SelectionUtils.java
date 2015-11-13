/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public final class SelectionUtils {

	private SelectionUtils() {
		// Do nothing
	}

	/**
	 * Collects the accessible files from the given selection.
	 * 
	 * @param selection
	 *            the selection to process.
	 * @return The selected files or an empty list if the selection is not a {@link IStructuredSelection} or there is no selected file.
	 */
	public static List<IFile> getAccessibleFilesFromSelection(final ISelection selection) {
		final List<IFile> result = new ArrayList<IFile>();
		for (Object selected : getObjectsFromSelection(selection)) {
			if (selected instanceof IFile) {
				final IFile file = (IFile) selected;
				if (file.isAccessible()) {
					result.add(file);
				}
			}
		}
		return result;
	}

	/**
	 * Collects the resources from the given selection.
	 * 
	 * @param selection
	 *            the selection to process.
	 * @return The selected resources or an empty list if the selection is not a {@link IStructuredSelection} or there is no selected resource.
	 */
	public static List<IResource> getResourcesFromSelection(final ISelection selection) {
		List<IResource> result = new ArrayList<IResource>();
	
		for (Object o : SelectionUtils.getObjectsFromSelection(selection)) {
			if (o instanceof IResource) {
				result.add((IResource) o);
			}
		}
	
		return result;
	}

	/**
	 * Collects the projects from the given selection.
	 * 
	 * @param selection
	 *            the selection to process.
	 * @return The selected projects or an empty list if the selection is not a {@link IStructuredSelection} or there is no selected project
	 */
	public static List<IProject> getProjectsFromSelection(final ISelection selection) {
		List<IProject> result = new ArrayList<IProject>();
	
		for (Object o : SelectionUtils.getObjectsFromSelection(selection)) {
			if (o instanceof IProject) {
				result.add((IProject) o);
			}
		}
	
		return result;
	}

	/**
	 * Converts the given selection to a list of objects.
	 * 
	 * @param selection
	 *            the selection
	 * @return The selected objects or an empty list if the selection is not a {@link IStructuredSelection}
	 */
	public static List<Object> getObjectsFromSelection(final ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return new ArrayList<Object>();
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;
		if (structSelection.isEmpty()) {
			return new ArrayList<Object>();
		}

		return structSelection.toList();
	}

	/**
	 * Converts the given selection to a list.
	 *
	 * @param selection
	 *            the selection
	 * @param type
	 *          the types to include it he result
	 * @return The selected instances of the given type or an empty list if the selection is not a {@link IStructuredSelection}
	 */
	public static<T> List<T> filterSelection(final ISelection selection, Class<T> type) {
		List<T> result = new ArrayList<T>();

		for (Object o : SelectionUtils.getObjectsFromSelection(selection)) {
			if (type.isInstance(o)) {
				result.add(type.cast(o));
			}
		}

		return result;
	}
}
