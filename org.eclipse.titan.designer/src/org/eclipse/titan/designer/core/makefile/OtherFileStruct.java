/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core.makefile;

/**
 * @author Szabolcs Beres
 * */
final class OtherFileStruct implements Comparable<OtherFileStruct> {
	/** if null the file is in the current working directory */
	private String directory;
	private String originalLocation;
	private String fileName;

	public OtherFileStruct(final String directory, final String originalLocation, final String fileName) {
		setDirectory(directory);
		setOriginalLocation(originalLocation);
		setFileName(fileName);
	}

	public StringBuilder name(final String workingDirectory, final boolean useAbsolutePathNames) {
		final StringBuilder result = new StringBuilder();

		if (getDirectory() == null || getDirectory().equals(workingDirectory)) {
			result.append(getFileName());
			return result;
		}

		if (useAbsolutePathNames) {
			result.append(getDirectory()).append('/');
		}
		result.append(getFileName());
		return result;
	}

	@Override
	public int compareTo(final OtherFileStruct other) {
		return getFileName().compareTo(other.getFileName());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof OtherFileStruct) {
			return getFileName().equals(((OtherFileStruct) obj).getFileName());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getFileName().hashCode();
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(final String directory) {
		this.directory = directory;
	}

	public String getOriginalLocation() {
		return originalLocation;
	}

	public void setOriginalLocation(final String originalLocation) {
		this.originalLocation = originalLocation;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}
}
