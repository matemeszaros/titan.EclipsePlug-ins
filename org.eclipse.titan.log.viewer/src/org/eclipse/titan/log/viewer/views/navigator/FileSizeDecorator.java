/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.navigator;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.titan.log.viewer.utils.Constants;


/**
 * Decorates the file name with the size and unit
 * 
 */
public class FileSizeDecorator implements ILightweightLabelDecorator {

	public FileSizeDecorator() {
		// Do nothing
	}
	
	@Override
	public void decorate(final Object element, final IDecoration decoration) {
		if (!(element instanceof IFile)) {
			return;
		}
		IFile aFile = (IFile) element;
		String extension = aFile.getFileExtension();
		if ((extension == null) || !extension.contentEquals(Constants.LOG_EXTENSION)) {
			return;
		}
		try {
			if (!aFile.getProject().hasNature(Constants.NATURE_ID)) {
				return;
			}
		} catch (CoreException e) {
			return;
		}
		
		String formattedSize = getFormattedSize(aFile);
		decoration.addSuffix(" [" + formattedSize + ']'); //$NON-NLS-1$
	} // decorate

	/**
	 * 
	 * Returns a formatted string containing the size of the file (including unit)
	 * 
	 * @param aFile The file to get the size
	 * @return the formatted string
	 * 
	 */
	private String getFormattedSize(final IFile aFile) {
		long size = getSize(aFile);
		return getFormat(size);
	}

	/**
	 * Returns the size of the passed file
	 * 
	 */
	private long getSize(final IFile file) {
		IPath location = file.getLocation();
		if (location == null) {
			return -1L;
		}
		File localFile = location.toFile();
		return localFile.length();
	} // getSize

	/**
	 * Get the formated size
	 * @param unit
	 * @return the formated size including unit (byte, kilo, mega, giga)
	 * 
	 */
	private String getFormat(final long size) {
		String format = ""; //$NON-NLS-1$
		long result;
		// calculate category for the file size
		if (size == -1L) {
			format = "unknown size"; //$NON-NLS-1$
		} else if (size >= Constants.GIGA_FACTOR) {
			result = (size + Constants.GIGA_FACTOR / 2) / Constants.GIGA_FACTOR;
			format = result + " GB"; //$NON-NLS-1$
		} else if (size >= Constants.MEGA_FACTOR) {
			result = (size + Constants.MEGA_FACTOR / 2) / Constants.MEGA_FACTOR;
			format = result + " MB"; //$NON-NLS-1$
		} else if (size >= Constants.KILO_FACTOR) {
			result = (size + Constants.KILO_FACTOR / 2) / Constants.KILO_FACTOR;
			format = result + " KB"; //$NON-NLS-1$
		} else {
			format = size + " B"; //$NON-NLS-1$
		}
		return format;
	} // getFormat

	@Override
	public void addListener(final ILabelProviderListener listener) {
		// Do nothing
	}

	@Override
	public void removeListener(final ILabelProviderListener listener) {
		// Do nothing
	}
	
	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		return false;
	}
}
