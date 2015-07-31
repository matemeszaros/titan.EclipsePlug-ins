/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests.library.JUnitXMLListener;

import java.util.ArrayList;

import org.eclipse.titanium.regressiontests.library.JUnitXMLListener.JUnitXMLRunListener.ITest;
import org.eclipse.titanium.regressiontests.library.JUnitXMLListener.JUnitXMLRunListener.ITestContainer;
import org.eclipse.titanium.regressiontests.library.JUnitXMLListener.JUnitXMLRunListener.ResultEnum;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class TestSuite implements ITest, ITestContainer {
	private String name;
	private long runTime;
	private ArrayList<ITest> tests;
	private int failures;
	private int skipped;

	public TestSuite(final Description description) {
		name = description.getClassName();
		tests = new ArrayList<JUnitXMLRunListener.ITest>();
	}

	@Override
	public void addTest(final ITest test) {
		tests.add(test);
	}

	@Override
	public Element writeXml(final Document doc, final Element parent) {
		Element tsElem = doc.createElement("testsuite");
		if (parent != null) {
			parent.appendChild(tsElem);
		}
		tsElem.setAttribute("name",		name);
		tsElem.setAttribute("tests",	String.valueOf(tests.size()));
		tsElem.setAttribute("failures",	String.valueOf(failures));
		tsElem.setAttribute("skipped",	String.valueOf(skipped));
		tsElem.setAttribute("time",		String.valueOf((runTime / 1000.0)));
		
		for (ITest test : tests) {
			test.writeXml(doc, tsElem);
		}
		return tsElem;
	}

	@Override
	public ResultEnum getResult() {
		return ResultEnum.PASS; // TODO
	}

	static String getElapsedTime(final long start, final long end) {
		return String.valueOf(end - start);
	}

	@Override
	public void finish(final Result result) {
		runTime = result.getRunTime();
		skipped = result.getIgnoreCount();
		failures = result.getFailureCount();
	}

}