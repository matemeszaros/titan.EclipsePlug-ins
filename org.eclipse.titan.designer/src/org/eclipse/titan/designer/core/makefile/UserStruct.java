/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core.makefile;

/**
 * @author Szabolcs Beres
 * */
final class UserStruct implements Comparable<UserStruct> {
	/** if null the file is in the current working directory */
	private String directory;
	private String originalSourceLocation;
	private String originalHeaderLocation;
	private String fileName;
	private String filePrefix;
	private String headerName;
	private String sourceName;
	private boolean hasHHSuffix;
	private boolean hasCCSuffix;

	public StringBuilder headerName() {
		if (getHeaderName() == null) {
			return new StringBuilder();
		}

		StringBuilder result = new StringBuilder();
		if (getDirectory() != null) {
			result.append(getDirectory()).append('/');
		}
		result.append(getHeaderName());
		return result;
	}

	public StringBuilder sourceName() {
		if (getSourceName() == null) {
			return new StringBuilder();
		}

		StringBuilder result = new StringBuilder();
		if (getDirectory() != null) {
			result.append(getDirectory()).append('/');
		}
		result.append(getSourceName());
		return result;
	}

	public StringBuilder objectName() {
		if (getSourceName() == null) {
			return null;
		}

		StringBuilder result = new StringBuilder();
		if (getDirectory() != null) {
			result.append(getDirectory()).append('/');
		}
		result.append(getFilePrefix()).append(".o");
		return result;
	}

	public StringBuilder specialName(final String extension) {
		if (getSourceName() == null) {
			return null;
		}

		StringBuilder result = new StringBuilder();
		if (getDirectory() != null) {
			result.append(getDirectory()).append('/');
		}
		result.append(getFilePrefix());
		if (extension != null) {
			result.append('.').append(extension);
		}
		return result;
	}

	@Override
	public int compareTo(final UserStruct other) {
		return getFileName().compareTo(other.getFileName());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof UserStruct) {
			return getFileName().equals(((UserStruct) obj).getFileName());
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

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getOriginalSourceLocation() {
		return originalSourceLocation;
	}

	public void setOriginalSourceLocation(String originalSourceLocation) {
		this.originalSourceLocation = originalSourceLocation;
	}

	public String getOriginalHeaderLocation() {
		return originalHeaderLocation;
	}

	public void setOriginalHeaderLocation(String originalHeaderLocation) {
		this.originalHeaderLocation = originalHeaderLocation;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public boolean isHasHHSuffix() {
		return hasHHSuffix;
	}

	public void setHasHHSuffix(boolean hasHHSuffix) {
		this.hasHHSuffix = hasHHSuffix;
	}

	public boolean isHasCCSuffix() {
		return hasCCSuffix;
	}

	public void setHasCCSuffix(boolean hasCCSuffix) {
		this.hasCCSuffix = hasCCSuffix;
	}
}
