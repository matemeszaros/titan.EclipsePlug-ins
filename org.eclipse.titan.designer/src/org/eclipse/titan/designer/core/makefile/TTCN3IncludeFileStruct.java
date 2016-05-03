/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core.makefile;

/**
 * @author Laszlo Baji
 * */
public final class TTCN3IncludeFileStruct implements Comparable<TTCN3IncludeFileStruct> {
	private String directory;
	private String workspaceDirectory;
	private String originalLocation;
	private String workspaceLocation;
	private String fileName;

	public TTCN3IncludeFileStruct(final String directory, final String workspaceDirectory, final String originalLocation,
			final String workspaceLocation, final String fileName) {
		setDirectory(directory);
		setWorkspaceDirectory(workspaceDirectory);
		setOriginalLocation(originalLocation);
		setWorkspaceLocation(workspaceLocation);
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
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof TTCN3IncludeFileStruct) {
			return getFileName().equals(((TTCN3IncludeFileStruct) obj).getFileName());
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

	public String getWorkspaceDirectory() {
		return workspaceDirectory;
	}

	public void setWorkspaceDirectory(final String workspaceDirectory) {
		this.workspaceDirectory = workspaceDirectory;
	}

	public String getWorkspaceLocation() {
		return workspaceLocation;
	}

	public void setWorkspaceLocation(final String workspaceLocation) {
		this.workspaceLocation = workspaceLocation;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	public int compareTo(final TTCN3IncludeFileStruct other) {
		return getFileName().compareTo(other.getFileName());
	}

}
