/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.search;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.models.LogRecordIndex;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.ui.model.WorkbenchLabelProvider;


public class SearchLabelProvider extends LabelProvider {
	private static final int MAX_TEXT_LENGTH = 500;
	private final Image matchIcon;
	
	public SearchLabelProvider() {
		super();
		matchIcon = Activator.getImageDescriptor(Constants.ICONS_CHILD_OBJ).createImage();
	}
	
	@Override
	public Image getImage(final Object element) {
		if (element instanceof Match) {
			return matchIcon;
		}
		
		return WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider().getImage(element);
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof IResource) {
			return WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider().getText(element);
		}
		
		if (element instanceof Match) {
			Match match = (Match) element;
			IFile logFile = (IFile) match.getElement();
			RandomAccessFile file = null;
			String result = "";
			try {
				// TODO: This should be cached in some way
				File indexFile = LogFileCacheHandler.getLogRecordIndexFileForLogFile(logFile);
				LogRecordIndex[] indexes = LogFileCacheHandler.readLogRecordIndexFile(indexFile, match.getOffset(), 1);
				file = new RandomAccessFile(new File(logFile.getLocationURI()), "r");
				int length = indexes[0].getRecordLength() > MAX_TEXT_LENGTH ? MAX_TEXT_LENGTH : indexes[0].getRecordLength();
				byte[] record = new byte[length];
				file.seek(indexes[0].getFileOffset());
				file.read(record);
				result = new String(record);
			} catch (FileNotFoundException e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(new TechnicalException(logFile.getName() + ": File not found " + e.getMessage()));  //$NON-NLS-1$
				return logFile.getName() + ": File not found";
			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(new TechnicalException("Error while reading the log file." + e.getMessage()));  //$NON-NLS-1$
				return "Error while reading the log file.";
			} finally {
				IOUtils.closeQuietly(file);
			}
			return result;
		}
		
		return "Unexpected element";
	}

}
