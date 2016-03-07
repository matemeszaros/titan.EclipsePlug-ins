/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.logging;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.product.ProductConstants;
import org.eclipse.ui.IEditorPart;

/**
 * @author Kristof Szabados
 * */
public final class ErrorReporter {
	private static final String EMPTY = "";
	private static final String INTERNAL_ERROR_PREFIX = "INTERNAL_ERROR: ";

	private static ILog log;

	//private constructor to protect from instantiation
	private ErrorReporter() {
	}

	/**
	 * A simple little static function to log an exception in the Eclipse provided error log.
	 * @param e The exception to be logged.
	 */
	public static void logExceptionStackTrace(final Exception e) {
		final String message = e.getMessage();
		getMyLog().log(new Status(IStatus.ERROR, ProductConstants.PRODUCT_ID_COMMON, IStatus.OK, (message != null) ? message : EMPTY, e));
	}

	/**
	 * A simple little static function to log an exception in the Eclipse provided error log.
	 * @param context contextual information
	 * @param e The exception to be logged.
	 */
	public static void logExceptionStackTrace(final String context, final Throwable e) {
		getMyLog().log(new Status(IStatus.ERROR, ProductConstants.PRODUCT_ID_COMMON, IStatus.OK, context, e));
	}

	/**
	 * STRICTLY INTERNAL
	 *
	 * Reports an error in the error log with a description and a full stack trace. Used to report internal (programmer) errors, that are needed to
	 * debug a special error occurrence.
	 *
	 * @param description the description of error to report.
	 * */
	public static void INTERNAL_ERROR(final String description) {
		final Exception e = new Exception(description);
		getMyLog().log(new Status(IStatus.ERROR, ProductConstants.PRODUCT_ID_COMMON, IStatus.OK, INTERNAL_ERROR_PREFIX + description, e));
	}

	/**
	 * STRICTLY INTERNAL
	 *
	 * Reports an error in the error log with a description and a full stack trace. Used to report internal (programmer) errors, that are needed to
	 * debug a special error occurrence.
	 *
	 * SHOULD ONLY BE USED IF THERE IS NO WAY TO PROVIDE MEANINGFUL INFORMATION.
	 * */
	public static void INTERNAL_ERROR() {
		final Exception e = new Exception("An INTERNAL ERROR has occured");
		getMyLog().log(new Status(IStatus.ERROR, ProductConstants.PRODUCT_ID_COMMON, IStatus.OK, "An INTERNAL ERROR has occured", e));
	}

	/**
	 * Reports an error in the error log with a description.
	 *
	 * @param description the description of error to report.
	 * */
	public static void logError(final String description) {
		getMyLog().log(new Status(IStatus.ERROR, ProductConstants.PRODUCT_ID_COMMON, IStatus.OK, description, null));
	}

	/**
	 * Reports a warning in the error log with a description and a full stack trace.
	 *
	 * @param description the description of error to report.
	 * */
	public static void logWarning(final String description) {
		getMyLog().log(new Status(IStatus.WARNING, ProductConstants.PRODUCT_ID_COMMON, IStatus.OK, description, null));
	}

	/**
	 * A simple little static function to log an exception in the Eclipse provided error log as a warning.
	 *
	 * @param context contextual information
	 * @param e The exception to be logged.
	 */
	public static void logWarningExceptionStackTrace(final String context, final Exception e) {
		getMyLog().log(new Status(IStatus.WARNING, ProductConstants.PRODUCT_ID_COMMON, IStatus.OK, context, e));
	}

	/**
	 * A simple little static function to log an exception in the Eclipse provided error log as a warning.
	 *
	 * @param e The exception to be logged.
	 */
	public static void logWarningExceptionStackTrace(final Exception e) {
		final String message = e.getMessage();
		getMyLog().log(new Status(IStatus.WARNING, ProductConstants.PRODUCT_ID_COMMON, IStatus.OK, (message != null) ? message : EMPTY, e));
	}

	/**
	 * A general way to convert a stack trace list into a single string.
	 * Useful for reporting special error messages.
	 *
	 * @param elements the list of stack trace elements.
	 * @return the string form of the stack trace.
	 * */
	public static String stackTraceToString(final StackTraceElement[] elements) {
		StringBuilder builder = new StringBuilder();
		for (StackTraceElement element : elements) {
			builder.append(element).append("\n");
		}
		return builder.toString();
	}

	/** Helper function to return the log of this bundle */
	private static ILog getMyLog() {
		if (log == null) {
			log = Platform.getLog(Platform.getBundle(ProductConstants.PRODUCT_ID_COMMON));
		}
		return log;
	}

	/**
	 * Sets the logger which will be used from this point.
	 * Can be used when the default logger of the platform is not available. (e.g. when unit testing)
	 * @param newLog The logger
	 */
	public static void setLog(ILog newLog) {
		log = newLog;
	}


	/**
	 * Prints the message into the status line in a new thread	
	 * @param targetEditor the editor which the message is sent from
	 * @param errorMessage
	 */
	public static void parallelDisplayInStatusLine(final IEditorPart targetEditor, final String errorMessage) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if(targetEditor != null) {
					targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(errorMessage); 
				}
			}
		});
	}

	/**
	 * Displays the error message in a MessageDialog in a new thread
	 * @param title
	 * @param message
	 */
	public static void parallelErrorDisplayInMessageDialog(final String title, final String message){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(null, title, message); //$NON-NLS-1$
			}
		});
	}
	
	/**
	 * Displays the warning message in a MessageDialog in a new thread
	 * @param title
	 * @param message
	 */
	public static void parallelWarningDisplayInMessageDialog(final String title, final String message){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openWarning(null, title, message);
			}
		});
	}
}
