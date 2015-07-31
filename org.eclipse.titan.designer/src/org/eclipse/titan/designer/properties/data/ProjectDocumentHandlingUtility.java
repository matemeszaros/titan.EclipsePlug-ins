/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Handles the storeage of the internal document related to each project, and
 * projected to/from the .TITAN_properties file.
 * 
 * @author Kristof Szabados
 * */
// FIXME configuration handling, document handling and project file handling has
// to be separated.
public final class ProjectDocumentHandlingUtility {
	private static Map<IProject, Document> documents = new HashMap<IProject, Document>();

	private ProjectDocumentHandlingUtility() {
		// Do nothing
	}

	public static Document getDocument(final IProject project) {
		if (!documents.containsKey(project)) {
			loadDocument(project);
		}

		return documents.get(project);
	}

	public static Document createDocument(final IProject project) {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			ErrorReporter.logExceptionStackTrace("While creating document for `" + project.getName() + "'", e);
			return null;
		}
		final DOMImplementation impl = builder.getDOMImplementation();

		final Document document = impl.createDocument(null, "TITAN_Designer_Properties", null);
		ProjectDocumentHandlingUtility.addDocument(project, document);
		return document;
	}

	public static void addDocument(final IProject project, final Document document) {
		documents.put(project, document);
	}

	private static void loadDocument(final IProject project) {
		final ProjectFileHandler fileHandler = new ProjectFileHandler(project);
		final Document document = fileHandler.getDocumentFromFile(project.getFile('/' + ProjectFileHandler.XML_TITAN_PROPERTIES_FILE));

		if (document != null) {
			documents.put(project, document);
		}
	}

	public static void clearDocument(final IProject project) {
		documents.remove(project);
	}

	public static void saveDocument(final IProject project) {
		if (!documents.containsKey(project)) {
			return;
		}

		final Document document = documents.get(project);
		final ProjectFileHandler fileHandler = new ProjectFileHandler(project);
		ProjectFileHandler.clearNode(document.getDocumentElement());
		ProjectFileHandler.indentNode(document, document.getDocumentElement(), 1);

		fileHandler.saveDocumentToFile(project.getFile('/' + ProjectFileHandler.XML_TITAN_PROPERTIES_FILE), document);
	}
}
