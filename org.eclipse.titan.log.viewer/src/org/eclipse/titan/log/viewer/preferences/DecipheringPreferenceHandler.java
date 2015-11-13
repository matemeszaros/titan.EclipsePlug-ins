/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.common.utils.preferences.PreferenceUtils;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.utils.Messages;

public final class DecipheringPreferenceHandler {

	private static final String VERSION_ATTRIBUTE_KEY = "version";
	private static final String DECIPHERING_XML_VERSION = "1.0";

	private static final String TAG_ROOT = "org.eclipse.titan.log.viewer.properties.MessageDecipheringPreferencePage";
	private static final String TAG_RULE_SET = "ruleset";
	private static final String TAG_NAME = "name";
	private static final String TAG_MSG_TYPE_LIST = "messagetypelist";
	private static final String TAG_MSG_TYPE = "messagetype";
	private static final String TAG_RULE_LIST = "rulelist";
	private static final String TAG_RULE = "rule";

	// The preferences are stored in the following form:
	// RULESETS_PREFIX = ruleset1;ruleset2;...
	// RULESETS_PREFIX.ruleset1.msgtypes = msgtype1;msgtype2;...
	// RULESETS_PREFIX.ruleset1.msgtypes.msgtypes1.rules = rule1;rule2;...

	private static final String RULESETS_PREFIX = "org.eclipse.titan.log.viewer.preferences.deciphering.rulesets";
	private static final String MSG_TYPES_QUALIFIER = ".msgtypes";

	public static final Pattern MSGTPYE_PATTERN = Pattern.compile("@?([a-z]+[a-z0-9_]*)(\\.([a-z]+[a-z0-9_]*))*", Pattern.CASE_INSENSITIVE);
	public static final Pattern RULE_PATTERN = Pattern.compile("([a-z]+[a-z0-9_]*)(\\.([a-z]+[a-z0-9_]*))*", Pattern.CASE_INSENSITIVE);

	@SuppressWarnings("serial")
	public static class ImportFailedException extends Exception {
		public ImportFailedException(final String msg) {
			super(msg);
		}
	}

	private DecipheringPreferenceHandler() {
	}

	public static String getPreferenceKeyForRulesets() {
		return RULESETS_PREFIX;
	}

	public static String getPreferenceKeyForRuleset(final String rulesetName) {
		return RULESETS_PREFIX + "." + rulesetName;
	}

	public static String getPreferenceKeyForMessageTypeList(final String rulesetName) {
		return getPreferenceKeyForRuleset(rulesetName) + MSG_TYPES_QUALIFIER;
	}

	public static String getPreferenceKeyForRuleList(final String rulesetName, final String msgType) {
		return getPreferenceKeyForMessageTypeList(rulesetName) + "." + msgType;
	}

	/**
	 * Returns the currently stored rule sets.
	 *
	 * @return the rule sets
	 */
	public static List<String> getAvailableRuleSets() {
		return PreferenceUtils.deserializeFromString(getPreferenceStore().getString(RULESETS_PREFIX));
	}

	/**
	 * Returns the rule set with the given name
	 *
	 * @param name The name of the rule set
	 * @return the ruleset or null if it does not exist
	 */
	public static Map<String, List<String>> getRuleset(final String name) {
		if (!getAvailableRuleSets().contains(name)) {
			return null;
		}
		List<String> msgTypes = PreferenceUtils.deserializeFromString(getPreferenceStore().getString(getPreferenceKeyForMessageTypeList(name)));
		final Map<String, List<String>> result = new HashMap<String, List<String>>();
		for (String msgType : msgTypes) {
			final List<String> rules = PreferenceUtils.deserializeFromString(getPreferenceStore().getString(getPreferenceKeyForRuleList(name, msgType)));
			result.put(msgType, new ArrayList<String>(rules));
		}

		return result;
	}

	/**
	 * Adds the given rule set to the preference store.
	 *
	 * @param rulesetName The name of the rule set
	 * @param ruleset     The Map representing the rule set, where
	 *                    <ul>
	 *                    <li>key - the message type</li>
	 *                    <li>value - the list containing the rules associated with the message type</li>
	 *                    </ul>
	 */
	public static void addRuleSet(final String rulesetName, final Map<String, List<String>> ruleset) {
		final IPreferenceStore prefStore = getPreferenceStore();

		final String rulesetList = prefStore.getString(RULESETS_PREFIX);
		final List<String> rulesets = PreferenceUtils.deserializeFromString(rulesetList);
		if (!rulesets.contains(rulesetName)) {
			rulesets.add(rulesetName);
			prefStore.setValue(RULESETS_PREFIX, PreferenceUtils.serializeToString(rulesets));
		}

		final Set<String> msgTypes = ruleset.keySet();
		prefStore.setValue(getPreferenceKeyForMessageTypeList(rulesetName), PreferenceUtils.serializeToString(msgTypes));

		for (Entry<String, List<String>> entry : ruleset.entrySet()) {
			prefStore.setValue(getPreferenceKeyForRuleList(rulesetName, entry.getKey()),
					PreferenceUtils.serializeToString(entry.getValue()));
		}
	}

	/**
	 * Deletes a rule set and it's message types from the preference store.
	 * The method does nothing if the given rule set does not exists.
	 *
	 * @param rulesetName The name of the rule set.
	 */
	public static void deleteRuleset(final String rulesetName) {
		final IPreferenceStore prefStore = getPreferenceStore();
		final String rulesetList = prefStore.getString(RULESETS_PREFIX);
		final List<String> rulesets = PreferenceUtils.deserializeFromString(rulesetList);

		if (!rulesets.contains(rulesetName)) {
			return;
		}

		rulesets.remove(rulesetName);
		prefStore.setValue(RULESETS_PREFIX, PreferenceUtils.serializeToString(rulesets));

		// Delete the message types of the ruleset
		final List<String> msgTypesList = PreferenceUtils.deserializeFromString(prefStore.getString(getPreferenceKeyForMessageTypeList(rulesetName)));
		prefStore.setToDefault(getPreferenceKeyForMessageTypeList(rulesetName));

		for (String msgType : msgTypesList) {
			prefStore.setToDefault(getPreferenceKeyForRuleList(rulesetName, msgType));
		}
	}

	/**
	 * Deletes the given message type and all of it's rules from the preference store.
	 *
	 * @param rulesetName The name of the ruleset, the message type belongs to.
	 * @param msgType     The name of the message type.
	 */
	public static void deleteMsgType(final String rulesetName, final String msgType) {
		final IPreferenceStore prefStore = getPreferenceStore();
		final List<String> msgTypesList = PreferenceUtils.deserializeFromString(prefStore.getString(getPreferenceKeyForMessageTypeList(rulesetName)));
		msgTypesList.remove(msgType);
		prefStore.setValue(getPreferenceKeyForMessageTypeList(rulesetName), PreferenceUtils.serializeToString(msgTypesList));

		prefStore.setToDefault(getPreferenceKeyForRuleList(rulesetName, msgType));
	}

	/**
	 * Exports the currently stored rulesets to the given file.
	 *
	 * @param file The output file
	 */
	public static void exportToFile(final File file) {

		try {
			Document document = createDocument();
			Element root = document.getDocumentElement();
			root.setAttribute(VERSION_ATTRIBUTE_KEY, DECIPHERING_XML_VERSION);

			final List<String> ruleSets = PreferenceUtils.deserializeFromString(getPreferenceStore().getString(RULESETS_PREFIX));

			for (String ruleSet : ruleSets) {
				final Element ruleSetElem = createXMLElementFromRuleSet(document, ruleSet);
				root.appendChild(ruleSetElem);
			}

			writeToFile(file, document);

		} catch (FileNotFoundException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(e));
		} catch (ParserConfigurationException e) {
			ErrorReporter.logExceptionStackTrace("While exporting into `" + file.getName() + "'", e);
		} catch (TransformerConfigurationException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("LogViewerPreferenceRootPage.2") + e)); //$NON-NLS-1$
		} catch (TransformerException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("LogViewerPreferenceRootPage.2") + e)); //$NON-NLS-1$
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("LogViewerPreferenceRootPage.2") + e)); //$NON-NLS-1$
		}
	}

	private static Document createDocument() throws ParserConfigurationException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation implementation = builder.getDOMImplementation();
		return implementation.createDocument(null, TAG_ROOT, null);
	}

	private static void writeToFile(File file, Document document) throws IOException, TransformerException {
		FileOutputStream stream = null;
		OutputStreamWriter writer = null;
		try {
			file.createNewFile();
			stream = new FileOutputStream(file);
			writer = new OutputStreamWriter(stream, "utf-8");
			Source input = new DOMSource(document);
			StreamResult output = new StreamResult(writer);
			TransformerFactory xFormFactory = TransformerFactory.newInstance();
			xFormFactory.setAttribute("indent-number", 2);
			Transformer idTransform = xFormFactory.newTransformer();
			idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
			idTransform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			idTransform.transform(input, output);
		} finally {
			IOUtils.closeQuietly(writer, stream);
		}
	}

	/**
	 * Imports all of the rulesets from the given file.
	 * If a ruleset with the same name already exists, a dialog will be displayed to the user.
	 *
	 * @param file
	 * @throws ImportFailedException
	 */
	public static void importFromFile(final File file) throws ImportFailedException {
		FileInputStream stream = null;

		try {
			stream = new FileInputStream(file);
			final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource(stream);
			Document document = builder.parse(inputSource);
			Element documentElement = document.getDocumentElement();

			if (!documentElement.getNodeName().contentEquals(TAG_ROOT)) {
				throw new ImportFailedException("The xml file is not valid");
			}

			final List<String> alreadyExistingRulesets = getAvailableRuleSets();

			NodeList rulesets = document.getElementsByTagName(TAG_RULE_SET);
			Boolean overwriteAll = null;
			for (int i = 0; i < rulesets.getLength(); ++i) {
				Element ruleElement = (Element) rulesets.item(i);
				NodeList nameList = ruleElement.getElementsByTagName(TAG_NAME);

				if (nameList.getLength() == 0) {
					throw new ImportFailedException("The ruleset's name is missing.");
				}

				final String rulesetName = nameList.item(0).getTextContent();
				if (rulesetName.length() == 0) {
					throw new ImportFailedException("The ruleset name can not be the empty string.");
				}

				NodeList messageTypeListList = ruleElement.getElementsByTagName(TAG_MSG_TYPE_LIST);
				if (messageTypeListList.getLength() == 0) {
					throw new ImportFailedException("The message type list for the ruleset '" + rulesetName + "' is missing.");
				}

				Element messageTypeListElement = (Element) messageTypeListList.item(0);

				NodeList msgTypeList = messageTypeListElement.getElementsByTagName(TAG_MSG_TYPE);

				Map<String, List<String>> msgTypesMap = importMessageTypes(rulesetName, msgTypeList);

				if (!alreadyExistingRulesets.contains(rulesetName)) {
					alreadyExistingRulesets.add(rulesetName);
					DecipheringPreferenceHandler.addRuleSet(rulesetName, msgTypesMap);
					continue;
				}

				if (overwriteAll == null) {
					final MessageDialog msgdialog = new MessageDialog(
							null,
							"Ruleset exists",
							null,
							"The following ruleset already exists: " + rulesetName + ".\nOverwrite the existing ruleset?",
							MessageDialog.QUESTION, new String[]{"Yes", "No", "Yes to All", "No to All"}, 1);
					final int result = msgdialog.open();
					if (result == 2) {
						overwriteAll = true;
					} else if (result == 3) {
						overwriteAll = false;
					}

					if (result == 2 || result == 0) {
						DecipheringPreferenceHandler.addRuleSet(rulesetName, msgTypesMap);
						alreadyExistingRulesets.add(rulesetName);
					}

				} else if (overwriteAll) {
					DecipheringPreferenceHandler.addRuleSet(rulesetName, msgTypesMap);
					alreadyExistingRulesets.add(rulesetName);
				}
			}
			PreferencesHandler.getInstance().setImportLastDir(file.getParentFile().getPath());
		} catch (ParserConfigurationException e) {
			throw new ImportFailedException("Error while parsing the file: " + e.getMessage());
		} catch (SAXException e) {
			throw new ImportFailedException("Error while parsing the file: " + e.getMessage());
		} catch (IOException e) {
			throw new ImportFailedException("Error while parsing the file: " + e.getMessage());
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private static Map<String, List<String>> importMessageTypes(String rulesetName, NodeList msgTypeList) throws ImportFailedException {
		Map<String, List<String>> msgTypesMap = new HashMap<String, List<String>>();
		for (int j = 0; j < msgTypeList.getLength(); ++j) {
			Element currentMsgType = (Element) msgTypeList.item(j);
			NodeList msgTypeNameList = currentMsgType.getElementsByTagName(TAG_NAME);
			if (msgTypeList.getLength() == 0) {
				throw new ImportFailedException("The messagetype's name is missing in the ruleset '" + rulesetName + "'");
			}

			final String msgTypeName = msgTypeNameList.item(0).getTextContent();
			if (!MSGTPYE_PATTERN.matcher(msgTypeName).matches()) {
				throw new ImportFailedException("Invalid message type in the ruleset '"
						+ rulesetName + "' in the message type '" + msgTypeName + "'.");
			}

			NodeList ruleListList = currentMsgType.getElementsByTagName(TAG_RULE_LIST);
			if (ruleListList.getLength() == 0) {
				throw new ImportFailedException("The ruleList element is missing in the ruleset '"
						+ rulesetName + "' in the message type '" + msgTypeName + "'.");
			}

			List<String> rulesParsed = new ArrayList<String>();
			NodeList ruleList = ((Element) ruleListList.item(0)).getElementsByTagName(TAG_RULE);

			for (int k = 0; k < ruleList.getLength(); ++k) {
				final String rule = ruleList.item(k).getTextContent();
				if (!RULE_PATTERN.matcher(rule).matches()) {
					throw new ImportFailedException("Invalid rule in the ruleset '"
							+ rulesetName + "' in the message type '" + msgTypeName + "'"
							+ " in the rule '" + rule + "'.");
				}

				rulesParsed.add(rule);
			}

			msgTypesMap.put(msgTypeName, rulesParsed);
		}
		return msgTypesMap;
	}

	/**
	 * Creates an XML element representing the given ruleset.
	 * The message types and the rules are loaded from the preference store.
	 *
	 * @param document    the document to create the element
	 * @param ruleSetName the name of the ruleset
	 * @return the created element
	 */
	private static Element createXMLElementFromRuleSet(final Document document, final String ruleSetName) {
		final Element ruleSetElement = document.createElement(TAG_RULE_SET);

		final Element ruleSetNameElement = document.createElement(TAG_NAME);
		ruleSetNameElement.setTextContent(ruleSetName);
		ruleSetElement.appendChild(ruleSetNameElement);

		final Element msgTypeListElement = document.createElement(TAG_MSG_TYPE_LIST);
		ruleSetElement.appendChild(msgTypeListElement);
		final List<String> msgTypes =
				PreferenceUtils.deserializeFromString(getPreferenceStore().getString(getPreferenceKeyForMessageTypeList(ruleSetName)));
		for (String msgType : msgTypes) {
			final Element msgTypeElement = document.createElement(TAG_MSG_TYPE);
			msgTypeListElement.appendChild(msgTypeElement);

			final Element msgTypeNameElement = document.createElement(TAG_NAME);
			msgTypeNameElement.setTextContent(msgType);
			msgTypeElement.appendChild(msgTypeNameElement);

			final List<String> rules =
					PreferenceUtils.deserializeFromString(getPreferenceStore().getString(getPreferenceKeyForRuleList(ruleSetName, msgType)));
			final Element ruleListElement = document.createElement(TAG_RULE_LIST);
			msgTypeElement.appendChild(ruleListElement);

			for (String rule : rules) {
				final Element ruleElement = document.createElement(TAG_RULE);
				ruleElement.setTextContent(rule);
				ruleListElement.appendChild(ruleElement);

			}
		}

		return ruleSetElement;
	}

	private static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
