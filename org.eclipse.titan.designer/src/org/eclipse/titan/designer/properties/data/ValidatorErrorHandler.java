/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * @author Kristof Szabados
 * */
public final class ValidatorErrorHandler implements org.xml.sax.ErrorHandler {
	private static final String ERROR = "ERROR: ";
	private static final String FATAL_ERROR = "FATAL ERROR: ";
	private static final String WARNING = "WARNING: ";

	/** private constructor to disable instantiation. */
	private ValidatorErrorHandler() {
		// Do nothing
	}

	@Override
	public void error(final org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
		ErrorReporter.logError(ERROR + sAXParseException.toString());
	}

	@Override
	public void fatalError(final org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
		ErrorReporter.logError(FATAL_ERROR + sAXParseException.toString());
	}

	@Override
	public void warning(final org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
		ErrorReporter.logWarning(WARNING + sAXParseException.toString());
	}
}
