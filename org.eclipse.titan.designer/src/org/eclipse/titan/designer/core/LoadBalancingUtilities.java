/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.preferences.SubscribedBoolean;
import org.eclipse.titan.designer.preferences.SubscribedInt;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Szabolcs Beres
 * */
public final class LoadBalancingUtilities {

	private static SubscribedInt tokensToProcessInARow;
	private static SubscribedInt threadPriority;
	private static SubscribedInt sleepBetweenFiles;
	private static SubscribedBoolean yieldBetweenChecks;

	static {
		tokensToProcessInARow = new SubscribedInt(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DEBUG_LOAD_TOKENS_TO_PROCESS_IN_A_ROW, 100);
		threadPriority = new SubscribedInt(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DEBUG_LOAD_THREAD_PRIORITY,
				Thread.MIN_PRIORITY);
		sleepBetweenFiles = new SubscribedInt(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DEBUG_LOAD_SLEEP_BETWEEN_FILES, 10);
		yieldBetweenChecks = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DEBUG_LOAD_YIELD_BETWEEN_CHECKS,
				true);
	}

	private LoadBalancingUtilities() {
		throw new UnsupportedOperationException();
	}

	public static int getThreadPriority() {
		return threadPriority.getValue();
	}

	public static int getTokensToProcessInARow() {
		return tokensToProcessInARow.getValue();
	}

	public static void syntaxAnalyzerProcessedAFile() {
		if (sleepBetweenFiles.getValue() >= 0) {
			try {
				Thread.sleep(sleepBetweenFiles.getValue());
			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
	}

	public static void astNodeChecked() {
		if (yieldBetweenChecks.getValue()) {
			Thread.yield();
		}
	}

}
