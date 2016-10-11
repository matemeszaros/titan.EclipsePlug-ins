/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.factories;

import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.MessageAnalyser;
import org.eclipse.titan.log.viewer.parsers.MessageAnalyserParallel1;
import org.eclipse.titan.log.viewer.parsers.MessageAnalyserParallel2;
import org.eclipse.titan.log.viewer.parsers.MessageAnalyserSingle1;
import org.eclipse.titan.log.viewer.parsers.MessageAnalyserSingle2;
import org.eclipse.titan.log.viewer.utils.Constants;

/**
 * Singleton for handling selection of log parser
 */
public final class MessageAnalyserFactory {
	
	private MessageAnalyserFactory messageAnalyserFactory;

	private MessageAnalyserFactory() {
		// Hide constructor
	}
	
	/**
	 * Returns the correct parser according to the logFileMetaData.
	 * @param logFileMetaData 
	 * @return <the parser>
	 * throws ParseExecption
	 */
	public static MessageAnalyser createMessageAnalyser(final LogFileMetaData logFileMetaData)
			throws TechnicalException {
		if (logFileMetaData == null) {
			throw new TechnicalException("logFileMetaData missing");	 //$NON-NLS-1$
		}
		//Check file format
		if (logFileMetaData.getFileFormat() == Constants.FILEFORMAT_2) {
			if (logFileMetaData.getExecutionMode().equals(Constants.EXECUTION_MODE_SINGLE)) {
				return new MessageAnalyserSingle2();
			} else if (logFileMetaData.getExecutionMode().equals(Constants.EXECUTION_MODE_PARALLEL)
					|| logFileMetaData.getExecutionMode().equals(Constants.EXECUTION_MODE_PARALLEL_MERGED)) {
				return new MessageAnalyserParallel2();
			} else {
				throw new TechnicalException("Wrong execution mode");		 //$NON-NLS-1$
			}
		}

		if (logFileMetaData.getExecutionMode().equals(Constants.EXECUTION_MODE_SINGLE)) {
			return new MessageAnalyserSingle1();
		} else if (logFileMetaData.getExecutionMode().equals(Constants.EXECUTION_MODE_PARALLEL)
				|| logFileMetaData.getExecutionMode().equals(Constants.EXECUTION_MODE_PARALLEL_MERGED)) {
			return new MessageAnalyserParallel1();
		} else {
			throw new TechnicalException("Wrong execution mode");		 //$NON-NLS-1$
		}
	}

	/**
	 * Returns the instance of the LogParserFactory
	 * @return the instance of the LogParserFactory
	 */
	public MessageAnalyserFactory getInstance() {
		if (this.messageAnalyserFactory == null) {
			this.messageAnalyserFactory = new MessageAnalyserFactory();
		}
		return this.messageAnalyserFactory;
	}
}
