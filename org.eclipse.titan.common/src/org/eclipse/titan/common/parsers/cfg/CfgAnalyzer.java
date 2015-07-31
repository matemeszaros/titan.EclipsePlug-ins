/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.titan.common.parsers.TITANMarker;

/**
 * @author eptedim
 * @author Arpad Lovassy
 */
public abstract class CfgAnalyzer {
	protected List<TITANMarker> warnings;
	protected CfgInterval rootInterval;
	protected Map<String, CfgDefinitionInformation> definitions;
	protected boolean logFileNameDefined = false;
	protected List<String> includeFiles;
	
	public List<TITANMarker> getWarnings() {
		return warnings;
	}

	/** 
	 * Returns true if the log file name was defined in the configuration file.
	 * @return true if the log file name was defined in the configuration file.
	 */
	public boolean isLogFileNameDefined() {
		return logFileNameDefined;
	}

	public Map<String, CfgDefinitionInformation> getDefinitions(){
		return definitions;
	}

	public List<String> getIncludeFilePaths(){
		return includeFiles;
	}
	
	public CfgInterval getRootInterval(){
		return rootInterval;
	}

    /**
     * Parses the provided elements.
     * If the contents of an editor are to be parsed, than the file parameter is only used to report the errors to.
     * 
     * @param file the file to parse, and report the errors to
     * @param code the contents of an editor, or null.
     */
	public abstract void parse(final IFile file, final String code);
	
    /**
     * Parses the provided elements.
     * If the contents of an editor are to be parsed, than the file parameter is only used to report the errors to.
     * 
     * @param file the file to parse
     * @param fileName the name of the file, to refer to.
     * @param code the contents of an editor, or null.
     */
	public abstract void directParse(final IFile file, final String fileName, final String code);
}
