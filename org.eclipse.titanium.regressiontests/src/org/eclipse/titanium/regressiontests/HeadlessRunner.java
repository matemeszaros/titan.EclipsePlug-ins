/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.titanium.regressiontests.library.JUnitXMLListener.JUnitXMLRunListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class HeadlessRunner implements IApplication {
	private static final Logger LOGGER = Logger.getLogger(HeadlessRunner.class.getName());
	private static final String ARG_WORKSPACE = "workspace";
	private static final String ARG_LICESNSE =  "license";
	private static final String ARG_XML_OUT =  "xml_out";

	public HeadlessRunner() {
	}
	
	private class ArgumentException extends Exception {
		private static final long serialVersionUID = 1L;

		public ArgumentException(final String message) {
			super(message);
		}
	}

	@Override
	public Object start(final IApplicationContext context) throws Exception {

		final String[] cmdArguments = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		Map<String, String> args = null;
		try {
			args = processArgs(cmdArguments);
		} catch (final ArgumentException e) {
			System.err.println(e.getMessage());
			System.err.println(usage());
			return -1;
		}

		JUnitCore junit = new JUnitCore();
		junit.addListener(new MyTextListener());

		if (args.containsKey(ARG_XML_OUT)) {
			final File xmlResultFile = new File(args.get(ARG_XML_OUT));
			JUnitXMLRunListener listener = new JUnitXMLRunListener(xmlResultFile);
			junit.addListener(listener);
		}

		LOGGER.info("Initialization done. Running tests.");
		Result result = junit.run(MainTestSuite.class);
		printResult(result);
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// Do nothing
	}

	private static void printResult(final Result res) {
		System.out.println("Nof testcases: " + res.getRunCount());
		System.out.println("Nof fail: " + res.getFailureCount());
		System.out.println("failed: ");
		for (Failure fail : res.getFailures()) {
			System.out.println("   " + fail.toString());
		}
		System.out.println("runtime: " + res.getRunCount());
	}

	private Map<String, String> processArgs(final String[] args) throws ArgumentException {
		if (args.length % 2 == 1) {
			throw new ArgumentException("Invalid number of arguments.");
		}

		final HashMap<String, String> arguments = new HashMap<String, String>(args.length);
		for (int i = 0; i < args.length; i += 2) {
			String key = args[i].startsWith("-") ? args[i].substring(1) : args[i];
			arguments.put(key.trim(), args[i + 1].trim());
		}

		if (!arguments.containsKey(ARG_WORKSPACE)) {
			throw new ArgumentException("No workspace argument.");
		}

		try {
			CustomConfigurable.setProjectFolder(new URI(arguments.get(ARG_WORKSPACE)).toString());
		} catch (URISyntaxException e) {
			throw new ArgumentException("Invalid URI: " + arguments.get(ARG_WORKSPACE));
		}

		if (arguments.containsKey(ARG_LICESNSE)) {
			CustomConfigurable.setLicenseFile(arguments.get(ARG_LICESNSE));
		}
		
		System.out.println("ws location: " + CustomConfigurable.getProjectFolder());
		System.out.println("license file location: " + CustomConfigurable.getLicenseFile());

		return arguments;
	}
	
	private String usage() {
		return "usage: ./eclipse -application org.eclipse.titan.regressiontests.HeadlessRunner -workspace ${WORKSPACE} -license ${TTCN3_LICENSE_FILE}";
	}

}
