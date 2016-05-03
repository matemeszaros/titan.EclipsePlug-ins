/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;

/**
 * Singleton class storing the list of files being saved at any point in time.
 * 
 * Can be used by change listeners to know if a change in the file system
 *  is triggered by external factors or the saving of our editors.
 *  In the first case the file needs to be re-analyzed to know its contents.
 *  
 *  @author Kristof Szabados
 * */
public final class FileSaveTracker {
	/**
	 * The set of files that were reported to be saved by the supported
	 * editors, but not yet had their modification event processed
	 */
	private static final Set<IFile> FILES_BEING_SAVED = new HashSet<IFile>();


	// Disabled constructor
	private FileSaveTracker() {
		// Do nothing
	}

	/**
	 * Adds a file to the set of files, that will be saved from our editors.
	 * So that we can handle them specially in event handling.
	 * 
	 * @param file
	 *                the file being saved.
	 * */
	public static void fileBeingSaved(final IFile file) {
		FILES_BEING_SAVED.add(file);
	}

	/**
	 * Checks whether the provided file is being saved.
	 * 
	 * @param file
	 *                the file to check.
	 * 
	 * @return true if the file is being saved, false otherwise.
	 * */
	public static boolean isFileBeingSaved(final IFile file) {
		return FILES_BEING_SAVED.contains(file);
	}

	/**
	 * Reports that the provided file has been saved.
	 * 
	 * @param file
	 *                the file saved.
	 * */
	public static void fileSaved(final IFile file) {
		if (FILES_BEING_SAVED.contains(file)) {
			FILES_BEING_SAVED.remove(file);
		}
	}
}
