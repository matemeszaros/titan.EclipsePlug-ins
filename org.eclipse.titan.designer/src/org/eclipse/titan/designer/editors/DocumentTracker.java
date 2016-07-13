package org.eclipse.titan.designer.editors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

/**
 * This fully static class is used to track which files are open in a document at
 * a given time.
 * <p>
 * We assume that one file can be open in one document only at a time.
 * 
 * @author Kristof Szabados
 * */
public class DocumentTracker {

	private static final Map<IFile, IDocument> FILE_DOCUMENT_MAP = new ConcurrentHashMap<IFile, IDocument>();

	/** private constructor to disable instantiation */
	private DocumentTracker() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Stores the information that the provided file is opened in the provided document.
	 * 
	 * @param file the provided file
	 * @param document the provided document
	 * */
	public static void put(final IFile file, final IDocument document) {
		FILE_DOCUMENT_MAP.put(file, document);
	}

	/**
	 * Checks if the provided file is open in a document and returns the document.
	 * 
	 * @param file the file to check for
	 * @return the document the file is open in, or null if none
	 * */
	public static IDocument get(final IFile file) {
		return FILE_DOCUMENT_MAP.get(file);
	}
}
