/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.experimental;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.titan.codegenerator.AstWalkerJava;

/**
 * Class for logging the AST hierarchy to an XML file.
 */
class HierarchyLogger {
	private final static String TAB = " ";

	private static Logger logger = Logger.getLogger(HierarchyLogger.class.getName());

	private static Formatter formatter = new Formatter() {
		@Override
		public String format(LogRecord record) {
			return String.format("%s%n", record.getMessage());
		}
	};

	static {
		try {
			String path = AstWalkerJava.props.getProperty("ast.log.file");
			if (path != null && !path.isEmpty()) {
				FileHandler handler = new FileHandler(path, true);
				handler.setFormatter(formatter);
				logger.addHandler(handler);
			}
			boolean logToConsole = Boolean.parseBoolean(AstWalkerJava.props.getProperty("ast.log.console"));
			if (logToConsole) {
				ConsoleHandler handler = new ConsoleHandler();
				handler.setFormatter(formatter);
				logger.addHandler(handler);
			}
			logger.setUseParentHandlers(false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private int level;

	HierarchyLogger() {
		level = 0;
	}

	/**
	 * Write XML start tag with the given objects class.
	 * @param o the object whose class determines the XML tag name
	 */
	void visit(Object o) {
		logger.info(indentation() + "<" + o.getClass().getSimpleName() + ">");
		level++;
	}

	/**
	 * Write XML end tag with the given objects class.
	 * @param o the object whose class determines the XML tag name
	 */
	void leave(Object o) {
		level--;
		logger.info(indentation() + "</" + o.getClass().getSimpleName() + ">");
	}

	/**
	 * Log a message into the current xml element.
	 * @param message the message to be logged
	 */
	public void log(String message) {
		logger.info(indentation() + message);
	}

	private String indentation() {
		return new String(new char[level]).replace("\0", TAB);
	}
}
