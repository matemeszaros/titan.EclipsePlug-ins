/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core.makefile;

import org.eclipse.core.runtime.IPath;

/**
 * @author Szabolcs Beres
 * */
final class BaseDirectoryStruct implements Comparable<BaseDirectoryStruct> {
	private IPath directory;
	private String directoryName;
	private boolean hasModules;

	public BaseDirectoryStruct(final IPath directory, final String directoryName, final boolean hasModules) {
		this.setDirectory(directory);
		this.setDirectoryName(directoryName);
		this.setHasModules(hasModules);
	}

	public StringBuilder name() {
		final StringBuilder result = new StringBuilder();
		result.append(getDirectoryName());
		return result;
	}

	public StringBuilder originalName() {
		final StringBuilder result = new StringBuilder();
		result.append(getDirectoryName());
		return result;
	}

	@Override
	public int compareTo(final BaseDirectoryStruct other) {
		return getDirectoryName().compareTo(other.getDirectoryName());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof BaseDirectoryStruct) {
			return getDirectoryName().equals(((BaseDirectoryStruct) obj).getDirectoryName());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getDirectoryName().hashCode();
	}

	public IPath getDirectory() {
		return directory;
	}

	public void setDirectory(final IPath directory) {
		this.directory = directory;
	}

	public String getDirectoryName() {
		return directoryName;
	}

	public void setDirectoryName(final String directoryName) {
		this.directoryName = directoryName;
	}

	public boolean isHasModules() {
		return hasModules;
	}

	public void setHasModules(final boolean hasModules) {
		this.hasModules = hasModules;
	}
}
