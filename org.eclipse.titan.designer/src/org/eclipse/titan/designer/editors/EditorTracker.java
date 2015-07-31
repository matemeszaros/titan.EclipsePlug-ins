/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IFile;

/**
 * This fully static class is used to track which files are open in an editor at
 * a given time.
 * <p>
 * The same file can be open in several editors at the same time.
 * 
 * @author Kristof Szabados
 * */
// TODO check out workbench.getEditorRegistry()
public final class EditorTracker {
	/**
	 * A map of files that are open in any number of editors, assigned the
	 * list of editors they appear in.
	 * */
	private static final Map<IFile, CopyOnWriteArrayList<ISemanticTITANEditor>> FILE_EDITOR_MAP =
			new ConcurrentHashMap<IFile, CopyOnWriteArrayList<ISemanticTITANEditor>>();

	/** private constructor to disable instantiation */
	private EditorTracker() {
	}

	/**
	 * Returns whether the file is opened in an editor.
	 * 
	 * @param file
	 *                the file to search for
	 * @return true if the file is opened in a supported editor.
	 */
	public static boolean containsKey(final IFile file) {
		return FILE_EDITOR_MAP.containsKey(file);
	}

	/**
	 * Returns one of the editors the file is opened in right now, or null
	 * if none.
	 * 
	 * @param file
	 *                the file to search for
	 * @return the editor in which the provided file is opened or null if
	 *         none.
	 */
	public static List<ISemanticTITANEditor> getEditor(final IFile file) {
		if (FILE_EDITOR_MAP.containsKey(file)) {
			return FILE_EDITOR_MAP.get(file);
		}

		return null;
	}

	/**
	 * Associates the specified file with the specified editor.
	 * 
	 * @param file
	 *                the file that was just opened.
	 * @param editor
	 *                the editor in which the file was opened
	 * */
	public static void put(final IFile file, final ISemanticTITANEditor editor) {
		CopyOnWriteArrayList<ISemanticTITANEditor> editors;

		if (FILE_EDITOR_MAP.containsKey(file)) {
			editors = FILE_EDITOR_MAP.get(file);
			editors.add(editor);
		} else {
			editors = new CopyOnWriteArrayList<ISemanticTITANEditor>();
			editors.add(editor);
			FILE_EDITOR_MAP.put(file, editors);
		}
	}

	/**
	 * Removes the specified file - editor association.
	 * 
	 * @param file
	 *                the file that was just closed.
	 * @param editor
	 *                the editor in which the file was closed
	 * */
	public static void remove(final IFile file, final ISemanticTITANEditor editor) {
		if (!FILE_EDITOR_MAP.containsKey(file)) {
			return;
		}

		List<ISemanticTITANEditor> editors = FILE_EDITOR_MAP.get(file);
		editors.remove(editor);

		if (editors.isEmpty()) {
			FILE_EDITOR_MAP.remove(file);
		}
	}

	/**
	 * Collects and returns a set of the files that are handled here.
	 * 
	 * @return the set of files collected.
	 * */
	public static Set<IFile> keyset() {
		return FILE_EDITOR_MAP.keySet();
	}
}
