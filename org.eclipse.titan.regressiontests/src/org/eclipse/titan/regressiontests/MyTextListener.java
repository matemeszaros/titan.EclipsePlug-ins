/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.titan.common.utils.StringUtils;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class MyTextListener extends RunListener {

	private static final Logger LOGGER = Logger.getLogger(MyTextListener.class.getName());

	public MyTextListener() {
		LOGGER.setUseParentHandlers(false);
		for (Handler handler : LOGGER.getHandlers()) {
			LOGGER.removeHandler(handler);
		}
		Handler handler = new ConsoleHandler();
		
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
		java.util.logging.Formatter formatter = new java.util.logging.Formatter() {
			@Override
			public String format(final LogRecord record) {
				return dateFormat.format(new Date(record.getMillis())) + " " 
						+ record.getLevel().getName() + " " 
						+ record.getMessage()
						+ StringUtils.lineSeparator();
			}
			
		};
		handler.setFormatter(formatter);
		LOGGER.addHandler(handler);
		
	}

	@Override
	public void testRunFinished(final Result result) {
		printFailures(result);
		printFooter(result);
	}

	@Override
	public void testStarted(final Description description) {
		LOGGER.log(Level.INFO, "Test started: " + description.getDisplayName());
	}

	@Override
	public void testFailure(final Failure failure) {
		LOGGER.log(Level.SEVERE, "Test failed: " + failure.getTestHeader() + "#####" + failure.getMessage());
	}

	@Override
	public void testFinished(final Description description) throws Exception {
		LOGGER.log(Level.INFO, "Test finished: " + description.getDisplayName());
	}

	@Override
	public void testIgnored(final Description description) {
		LOGGER.log(Level.INFO, "Test ignored: " + description.getDisplayName());
	}

	protected void printFailures(final Result result) {
		List<Failure> failures = result.getFailures();
		if (failures.size() == 0) {
			return;
		}
		if (failures.size() == 1) {
			LOGGER.log(Level.SEVERE, "There was " + failures.size() + " failure");
		} else {
			LOGGER.log(Level.SEVERE, "There were " + failures.size() + " failures");
		}
		int i = 1;
		for (Failure each : failures) {
			printFailure(each, "" + i++);
		}
	}

	protected void printFailure(final Failure each, final String prefix) {
		LOGGER.log(Level.SEVERE, each.getTestHeader());
		LOGGER.log(Level.SEVERE, each.getTrace());
	}

	protected void printFooter(final Result result) {
		if (result.wasSuccessful()) {
			LOGGER.log(Level.SEVERE, "You ran " + result.getRunCount() + " tests. 0 failure.");
		} else {
			LOGGER.log(Level.SEVERE, "Tests run: " + result.getRunCount() + ",  Failures: " + result.getFailureCount());
		}
	}
}
