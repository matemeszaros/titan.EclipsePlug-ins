/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests.library.JUnitXMLListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.titan.common.utils.StringUtils;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JUnitXMLRunListener extends RunListener {

	private final File xmlFile;

	private TestCase activeTc;
	private ITestContainer activeTs;

	private ITestContainer root = null;

	enum ResultEnum {
		PASS, FAILURE, IGNORED
	}

	interface ITest {
		Element writeXml(Document doc, Element parent);
		ResultEnum getResult();
	}

	interface ITestContainer {
		void addTest(ITest test);
		void finish(Result result);
		Element writeXml(Document doc, Element parent);
	}
	
	public JUnitXMLRunListener(final File xmlFile) {
		this.xmlFile = xmlFile;
	}

	@Override
	public void testRunStarted(final Description description) throws Exception {
		if (root == null) {
			root = new TestSuite(description);
			activeTs = root;
		}
	}

	@Override
	public void testRunFinished(final Result result) throws Exception {
		activeTs.finish(result);
		writeXml();
	}

	@Override
	public void testStarted(final Description description) throws Exception {
		activeTc = new TestCase(description);
		activeTs.addTest(activeTc);
	}

	@Override
	public void testFinished(final Description description) throws Exception {
		if (activeTc != null) {
			activeTc.pass();
			activeTc = null;
		}
	}

	@Override
	public void testFailure(final Failure failure) throws Exception {
		if (activeTc != null) {
			activeTc.fail(failure);
		} else {
			activeTc = new TestCase(failure.getDescription());
			activeTs.addTest(activeTc);
			activeTc.fail(failure);
		}
		activeTc = null;
	}

	@Override
	public void testAssumptionFailure(final Failure failure) {
		activeTc.fail(failure);
	}

	@Override
	public void testIgnored(final Description description) throws Exception {
		activeTc = new TestCase(description);
		activeTc.ignore();
		activeTc = null;
	}

	public void writeXml() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			doc.appendChild(root.writeXml(doc, null));

			TransformerFactory tFactory = TransformerFactory.newInstance();
			tFactory.setAttribute("indent-number", 2);
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
			FileOutputStream fos = new FileOutputStream(xmlFile);
			StreamResult result = new StreamResult(fos);

			transformer.transform(source, result);
			fos.close();
			System.out.println("Result xml file created: " + xmlFile.getAbsolutePath());
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static String descToString(final Description desc) {
		if (desc == null) {
			return "DESC is NULL";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("className: ").append(desc.getClassName()).append(StringUtils.lineSeparator());
		builder.append("displayName: ").append(desc.getDisplayName()).append(StringUtils.lineSeparator());
		builder.append("methodName: ").append(desc.getMethodName()).append(StringUtils.lineSeparator());
		builder.append("isSuite: ").append(desc.isSuite()).append(StringUtils.lineSeparator());
		builder.append("isTest: ").append(desc.isTest()).append(StringUtils.lineSeparator());
		builder.append("testCount: ").append(desc.testCount()).append(StringUtils.lineSeparator());
		Class<?> testClass = desc.getTestClass();
		builder.append("testClass: ").append(testClass == null ? "null" : testClass.getName()).append(StringUtils.lineSeparator());

		ArrayList<Description> children = desc.getChildren();
		if (children != null) {
			builder.append("numChildren: ").append(children.size()).append(StringUtils.lineSeparator());
		}
		return builder.toString();
	}

}
