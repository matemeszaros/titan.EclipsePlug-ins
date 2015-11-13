/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core.makefile;

/**
 * @author Szabolcs Beres
 * */
class ModuleStruct implements Comparable<ModuleStruct> {
	/** if null the file is in the current working directory */
	private String directory;
	private String originalLocation;
	private String fileName;

	private String moduleName;
	private boolean isRegular = true;

	public ModuleStruct(final String directory, final String originalLocation, final String fileName, final String moduleName) {
		this.setDirectory(directory);
		this.setOriginalLocation(originalLocation);
		this.setFileName(fileName);
		this.setModuleName(moduleName);
	}

	public StringBuilder fileName() {
		StringBuilder result = new StringBuilder();
		if (getDirectory() != null) {
			result.append(getDirectory()).append('/');
		}

		result.append(getFileName());
		return result;
	}

	public StringBuilder generatedName(final boolean addDirectory, final String suffix) {
		return generatedName(addDirectory, suffix, null);
	}

	public StringBuilder generatedName(final boolean addDirectory, final String suffix, final String fileNameSuffix) {
		StringBuilder result = new StringBuilder();
		if (addDirectory) {
			if (getDirectory() != null) {
				result.append(getDirectory()).append('/');
			}
		}
		result.append(getModuleName().replace('-', '_'));

		if (fileNameSuffix != null) {
			result.append(fileNameSuffix);
		}

		if (suffix != null) {
			result.append('.').append(suffix);
		}

		return result;
	}

	public StringBuilder preprocessedName(final boolean addDirectory) {
		StringBuilder result = new StringBuilder();
		if (addDirectory) {
			if (getDirectory() != null) {
				result.append(getDirectory()).append('/');
			}
		}
		result.append(getModuleName());
		result.append(".ttcn");

		return result;
	}

	@Override
	public int compareTo(final ModuleStruct other) {
		return getFileName().compareTo(other.getFileName());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof ModuleStruct) {
			return getFileName().equals(((ModuleStruct) obj).getFileName());
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

	public String getOriginalLocation() {
		return originalLocation;
	}

	public void setOriginalLocation(String originalLocation) {
		this.originalLocation = originalLocation;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/** The module name in original format */
	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public boolean isRegular() {
		return isRegular;
	}

	public void setRegular(boolean isRegular) {
		this.isRegular = isRegular;
	}
}
