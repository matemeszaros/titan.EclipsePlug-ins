/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests.library.JUnitXMLListener;

import org.eclipse.titanium.regressiontests.library.JUnitXMLListener.JUnitXMLRunListener.ITest;
import org.eclipse.titanium.regressiontests.library.JUnitXMLListener.JUnitXMLRunListener.ResultEnum;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class TestCase implements ITest {
	private String system_out;
	private String system_err;
	private String name;
	private long starTime;

	private long endTime;
	private String className;
	private ResultEnum result;
	private String message;
	private String trace;

	public TestCase(final Description desc) {
		this.name = desc.getMethodName();
		this.starTime = System.currentTimeMillis();
		this.className = desc.getClassName();
	}

	public void pass() {
		this.result = ResultEnum.PASS;
		this.endTime = System.currentTimeMillis();
	}

	public void fail(final Failure failure) {
		this.result = ResultEnum.FAILURE;
		this.message = failure.getMessage();
		this.trace = failure.getTrace();
		this.endTime = System.currentTimeMillis();
	}

	public void ignore() {
		result = ResultEnum.IGNORED;
		this.endTime = System.currentTimeMillis();
	}

	@Override
	public Element writeXml(final Document doc, final Element parent) {
		Element tcElem = doc.createElement("testcase");
		parent.appendChild(tcElem);
		tcElem.setAttribute("classname", className);
		tcElem.setAttribute("name", name);
		tcElem.setAttribute("time", String.valueOf((endTime - starTime) / 1000.0));
		if (system_out != null && !system_out.isEmpty()) {
			tcElem.setAttribute("system-out", system_out);
		}
		if (system_err != null && !system_err.isEmpty()) {
			tcElem.setAttribute("system-err", system_err);
		}

		switch (result) {
		case PASS: break;
		case FAILURE: {
			Element failNode = doc.createElement("failure");
			failNode.setAttribute("message", message + "###" + trace);
			tcElem.appendChild(failNode);
			break;
		}
		case IGNORED: {
			Element ignoredNode = doc.createElement("skipped");
			tcElem.appendChild(ignoredNode);
			break;
		}
		default:
			break;
		}
		return tcElem;

	}

	@Override
	public ResultEnum getResult() {
		return result;
	}
}