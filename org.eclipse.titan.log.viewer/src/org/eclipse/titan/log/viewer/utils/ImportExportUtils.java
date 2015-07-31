/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.utils;

import static org.eclipse.titan.common.utils.StringUtils.isNullOrEmpty;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.common.utils.Joiner;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.preferences.data.KeywordColor;

/**
 * Helper class for import and export of preferences via xml files
 */
public final class ImportExportUtils {

	private static final String KEYWORD_ELEMENT_KEY = "keyword"; //$NON-NLS-1$
	private static final String COLOR_ELEMENT_KEY = "color"; //$NON-NLS-1$
	private static final String VERSION_ATTRIBUTE_KEY = "version"; //$NON-NLS-1$
	private static final String CURRENT_LV_VERSION = "1.3"; //$NON-NLS-1$
	private static final String VALUE_ELEMENT_KEY = "value"; //$NON-NLS-1$
	private static final String NEW_LINE = "\n"; //$NON-NLS-1$
	private static final String PARENT_INDENTATION = "   "; //$NON-NLS-1$
	private static final String CHILD_INDENTATION = "      "; //$NON-NLS-1$
	private static final String COLOR_CHILD_INDENTATION = "         "; //$NON-NLS-1$
	private static final String XML_EXTENSION_MASK = "*.xml"; //$NON-NLS-1$
	private static final String XML_EXTENSION = ".xml"; //$NON-NLS-1$

	private ImportExportUtils() {
		// Hide constructor
	}

	/**
	 * Converts a string array to a string separated with the given delimiter
	 *
	 * @param array     the array
	 * @param delimiter the delimiter
	 * @return an string containing all the array elements separated with the given delimiter
	 */
	public static String arrayToString(final String[] array, final String delimiter) {
		return new Joiner(delimiter).join(Arrays.asList(array)).toString();
	}

	/**
	 * Help method for exporting properties/preferences to an xml file
	 *
	 * @param pageID         the page id of the preference/property page (which is used as main xml tag)
	 * @param settings       a hash map with all property/preference keys/values to store
	 * @param useIndentation a flag which indicates if indentation (of the xml file) should be used or not
	 */
	public static void exportSettings(final String pageID, final Map<String, String[]> settings, final boolean useIndentation) {
		FileOutputStream stream = null;
		try {
			stream = createOutputStream();
			if (stream != null) {
				exportToXml(pageID, settings, useIndentation, stream);
			}
		} catch (TechnicalException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private static FileOutputStream createOutputStream() throws TechnicalException {
		String resultFile = getTargetFileFromWithDialog();
		if (resultFile == null
				|| resultFile.compareTo(File.separator) == 0) {
			return null;
		}
		//for GTK versions older than 2.4.10 file dialog filters does not work
		resultFile = addXmlExtension(resultFile);
		File file = new File(resultFile);
		// Set last dir
		PreferencesHandler.getInstance().setExportLastDir(file.getParentFile().getPath());

		try {
			file.createNewFile();
			return new FileOutputStream(file);
		} catch (IOException e) {
			throw new TechnicalException("Cannot open file: " + file.getAbsolutePath());
		}
	}

	private static String addXmlExtension(String resultFile) {
		if (!resultFile.endsWith(XML_EXTENSION)) {
			return resultFile.concat(XML_EXTENSION);
		}
		return resultFile;
	}

	private static String getTargetFileFromWithDialog() {
		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		String exportLastDir = PreferencesHandler.getInstance().getExportLastDir();
		boolean pathValid = new Path(exportLastDir).isValidPath(exportLastDir);
		if (!exportLastDir.isEmpty() && pathValid) {
			dialog.setFilterPath(exportLastDir);
		}
		dialog.setFilterExtensions(new String[]{XML_EXTENSION_MASK});
		dialog.setText(Messages.getString("ImportExportUtils.0"));
		String dialogResult = dialog.open();
		if (dialogResult == null) {
			return null;
		}
		return dialog.getFilterPath() + File.separator + dialog.getFileName();
	}

	protected static void exportToXml(String pageID, Map<String, String[]> settings, boolean useIndentation, OutputStream stream)
			throws TechnicalException {
		try {
			Document document = createDocument(pageID);

			Element root = document.getDocumentElement();
			root.setAttribute(VERSION_ATTRIBUTE_KEY, CURRENT_LV_VERSION);
			// Add all keys/values
			SortedMap<String, String[]> sortedMap = new TreeMap<String, String[]>(settings);
			for (Map.Entry<String, String[]> entry : sortedMap.entrySet()) {
				String currentKey = entry.getKey();
				String[] values = entry.getValue();
				Element parent = document.createElement(currentKey);
				if (useIndentation) {
					root.appendChild(document.createTextNode(NEW_LINE + PARENT_INDENTATION));
				}
				root.appendChild(parent);
				for (int i = 0; i < values.length; i++) {
					addStringElement(useIndentation, document, parent, values[i]);
					addNewLineIfLast(document, parent, values, i);
				}
			}
			// if last parent
			if (useIndentation) {
				root.appendChild(document.createTextNode(NEW_LINE));
			}
			writeDocument(stream, document);
		} catch (ParserConfigurationException e) {
			throw new TechnicalException(e);
		} catch (TransformerException e) {
			throw new TechnicalException(e);
		}

	}

	private static Document createDocument(String pageID) throws ParserConfigurationException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation implementation = builder.getDOMImplementation();
		// Create an xml document
		return implementation.createDocument(null, pageID, null);
	}

	/**
	 * Help method for exporting properties/preferences to an xml file
	 *
	 * @param pageID         the page id of the preference/property page (which is used as main xml tag)
	 * @param settings       a hash map with all property/preference keys/values to store
	 * @param useIndentation a flag which indicates if indentation (of the xml file) should be used or not
	 */
	public static void exportColorSettings(final String pageID, final Map<String, Object[]> settings, final boolean useIndentation) {
		FileOutputStream stream = null;
		try {
			stream = createOutputStream();
			if (stream != null) {
				exportKeywordColorsToXml(pageID, settings, useIndentation, stream);
			}
		} catch (TechnicalException e) {
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("LogViewerPreferenceRootPage.2") + e)); //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	protected static void exportKeywordColorsToXml(String pageID, Map<String, Object[]> settings, boolean useIndentation, OutputStream stream)
			throws TechnicalException {
		try {
			Document document = createDocument(pageID);
			Element root = document.getDocumentElement();
			root.setAttribute(VERSION_ATTRIBUTE_KEY, CURRENT_LV_VERSION);
			SortedMap<String, Object[]> sortedMap = new TreeMap<String, Object[]>(settings);
			for (Map.Entry<String, Object[]> entry : sortedMap.entrySet()) {
				String currentKey = entry.getKey();
				Element parent = document.createElement(currentKey);
				if (useIndentation) {
					root.appendChild(document.createTextNode(NEW_LINE + PARENT_INDENTATION));
				}
				root.appendChild(parent);

				Object[] values = entry.getValue();
				if (values instanceof String[]) {
					String[] stringValues = (String[]) values;
					for (int i = 0; i < stringValues.length; i++) {
						addStringElement(useIndentation, document, parent, stringValues[i]);
						addNewLineIfLast(document, parent, values, i);
					}
				} else if (values instanceof KeywordColor[]) {
					KeywordColor[] colorValues = (KeywordColor[]) values;
					for (int i = 0; i < colorValues.length; i++) {
						addKeywordColor(useIndentation, document, parent, colorValues[i]);
						addNewLineIfLast(document, parent, values, i);
					}
				}
			}
			// if last parent
			if (useIndentation) {
				root.appendChild(document.createTextNode(NEW_LINE));
			}
			writeDocument(stream, document);
		} catch (ParserConfigurationException e) {
			throw new TechnicalException(e);
		} catch (TransformerException e) {
			throw new TechnicalException(e);
		}
	}

	private static void addNewLineIfLast(Document document, Element parent, Object[] values, int i) {
		// if last child
		if (i == (values.length - 1)) {
			parent.appendChild(document.createTextNode(NEW_LINE + PARENT_INDENTATION));
		}
	}

	private static void addKeywordColor(boolean useIndentation, Document document, Element parent, KeywordColor keywordColor) {
		Element child = document.createElement(VALUE_ELEMENT_KEY);
		if (useIndentation) {
			parent.appendChild(document.createTextNode(NEW_LINE + CHILD_INDENTATION));
		}
		parent.appendChild(child);

		Element keywordChild = document.createElement(KEYWORD_ELEMENT_KEY);
		keywordChild.setTextContent(keywordColor.getKeyword());
		if (useIndentation) {
			child.appendChild(document.createTextNode(NEW_LINE + COLOR_CHILD_INDENTATION));
		}
		child.appendChild(keywordChild);
		Element colorChild = document.createElement(COLOR_ELEMENT_KEY);
		if (keywordColor.getColor() != null) {
			String color = keywordColor.getColor().red
					+ PreferenceConstants.RGB_COLOR_SEPARATOR + keywordColor.getColor().green
					+ PreferenceConstants.RGB_COLOR_SEPARATOR + keywordColor.getColor().blue;
			colorChild.setTextContent(color);
		} else {
			colorChild.setTextContent("");
		}
		if (useIndentation) {
			child.appendChild(document.createTextNode(NEW_LINE + COLOR_CHILD_INDENTATION));
		}
		child.appendChild(colorChild);
		child.appendChild(document.createTextNode(NEW_LINE + CHILD_INDENTATION));
	}

	private static void addStringElement(boolean useIndentation, Document document, Element parent, String stringValue) {
		Element child = document.createElement(VALUE_ELEMENT_KEY);
		child.setTextContent(stringValue);
		if (useIndentation) {
			parent.appendChild(document.createTextNode(NEW_LINE + CHILD_INDENTATION));
		}
		parent.appendChild(child);
	}

	private static void writeDocument(OutputStream stream, Document document) throws TransformerException {
		Source input = new DOMSource(document);
		StreamResult output = new StreamResult(stream);
		TransformerFactory xFormFactory = TransformerFactory.newInstance();
		Transformer idTransform = xFormFactory.newTransformer();
		idTransform.transform(input, output);
	}

	/**
	 * @param pageID property/preference xml tag
	 * @return the properties / preferences of the given xml tag (if it exists)
	 */
	public static Map<String, String> importSettings(final String pageID) {
		String resultFile = getImportSourceFileWithDialog();
		if (resultFile == null
				|| resultFile.compareTo(File.separator) == 0) {
			return null;
		}

		File file = new File(resultFile);
		if (!file.exists() || file.length() == 0) {
			// Empty file selected
			final Display display = Display.getDefault();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(new Shell(Display.getDefault()),
							Messages.getString("ImportExportUtils.2"), Messages.getString("ImportExportUtils.4"));
				}
			});
			return null;
		}

		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			Map<String, String> result = importFromStream(pageID, stream);
			if (result != null) {
				PreferencesHandler.getInstance().setImportLastDir(file.getParentFile().getPath());
			}
			return result;
		} catch (ParserConfigurationException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("LogViewerPreferenceRootPage.2") + e)); //$NON-NLS-1$
		} catch (SAXException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("LogViewerPreferenceRootPage.2") + e)); //$NON-NLS-1$
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("LogViewerPreferenceRootPage.2") + e)); //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(stream);
		}
		return null;
	}

	protected static Map<String, String> importFromStream(String pageID, InputStream stream)
			throws ParserConfigurationException, IOException, SAXException {
		InputSource inputSource = new InputSource(stream);
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(inputSource);
		Element documentElement = document.getDocumentElement();

		if (!documentElement.getNodeName().contentEquals(pageID)) {
			// Nothing to import
			final Display display = Display.getDefault();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(new Shell(Display.getDefault()),
							Messages.getString("ImportExportUtils.2"), Messages.getString("ImportExportUtils.3"));
				}
			});
			return null;
		}

		String version = documentElement.getAttribute(VERSION_ATTRIBUTE_KEY);
		if (version.contentEquals(CURRENT_LV_VERSION)) {
			return importNewSettings(documentElement);
		} else {
			return importOldSettings(pageID, documentElement);
		}
	}

	public static String getImportSourceFileWithDialog() {
		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);

		dialog.setFilterExtensions(new String[]{XML_EXTENSION_MASK});
		dialog.setText(Messages.getString("ImportExportUtils.1"));
		String importLastDir = PreferencesHandler.getInstance().getImportLastDir();

		boolean pathValid = new Path(importLastDir).isValidPath(importLastDir);
		if (!isNullOrEmpty(importLastDir) && pathValid) {
			dialog.setFilterPath(importLastDir);
		}
		String dialogResult = dialog.open();
		if (dialogResult == null || isNullOrEmpty(dialog.getFileName())) {
			return null;
		}
		return dialog.getFilterPath() + File.separator + dialog.getFileName();
	}

	/**
	 * Retrieves the properties of a given element in an xml file
	 *
	 * @param documentElement the xml document root element
	 * @return The key and values of the element
	 */
	private static Map<String, String> importNewSettings(final Element documentElement) {
		Map<String, String> result = new HashMap<String, String>();
		NodeList parents = documentElement.getChildNodes();
		for (int i = 0; i < parents.getLength(); i++) {
			Node currentNode = parents.item(i);
			NodeList values = currentNode.getChildNodes();
			for (int j = 0; j < values.getLength(); j++) {
				Node currentValue = values.item(j);

				if (currentValue.getNodeName().contentEquals(VALUE_ELEMENT_KEY)) {
					importValueElement(result, currentNode, currentValue);
				}
			}
		}
		return result;
	}

	private static void importValueElement(Map<String, String> result, Node currentNode, Node currentValue) {
		String storedValue = result.get(currentNode.getNodeName());
		String fetchedValue;

		NodeList keywords = currentValue.getChildNodes();
		if (keywords.getLength() > 1) {
			StringBuilder keywordColor = new StringBuilder();
			for (int k = 0; k < keywords.getLength(); k++) {
				Node currentKeyword = keywords.item(k);
				if (currentKeyword.getNodeName().contentEquals(KEYWORD_ELEMENT_KEY)) {
					keywordColor.append(currentKeyword.getTextContent());
				}
				if (currentKeyword.getNodeName().contentEquals(COLOR_ELEMENT_KEY)) {
					keywordColor.append(PreferenceConstants.KEYWORD_COLOR_SEPARATOR + currentKeyword.getTextContent());
				}
			}
			fetchedValue = keywordColor.toString();
		} else {
			fetchedValue = currentValue.getTextContent();
		}
		if ((storedValue != null) && !storedValue.contentEquals("")) { //$NON-NLS-1$
			result.put(currentNode.getNodeName(), storedValue.concat(File.pathSeparator.concat(fetchedValue)));
		} else {
			result.put(currentNode.getNodeName(), fetchedValue);
		}
	}

	/**
	 * Retrieves the properties of a given element in an xml file
	 *
	 * @param pageID          the id of the preference page
	 * @param documentElement the xml document root element
	 * @return The key and values of the element
	 */
	private static Map<String, String> importOldSettings(final String pageID, final Element documentElement) {
		Map<String, String> result = new HashMap<String, String>();
		NodeList childNodes = documentElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			String nodeName = childNodes.item(i).getNodeName();
			if (nodeName.compareTo(pageID) == 0) {
				Element e = (Element) childNodes.item(i);
				NamedNodeMap attributes = e.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					// Add all keys and values from the xml
					result.put(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue());
				}
			}
		}
		return result;
	}
}
