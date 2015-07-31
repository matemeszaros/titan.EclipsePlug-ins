/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import org.eclipse.titan.common.parsers.Interval;

/**
 * @author eferkov
 * */
public final class CfgInterval extends Interval {
	public enum section_type {
		LOGGING,
		EXECUTE,
		TESTPORT_PARAMETERS,
		MODULE_PARAMETERS,
		MAIN_CONTROLLER,
		EXTERNAL_COMMANDS,
		GROUPS,
		COMPONENTS,
		INCLUDE,
		DEFINE,
		PROFILER,
		 // Error indicator.
		UNKNOWN
	}

	private final section_type sectionType;
	
	public CfgInterval(final Interval parent, final interval_type type, final section_type sectionType) {
		super(parent, type);
		this.sectionType = sectionType;
	}
	
	/**
	 * Returns the section type for the interval.
	 */
	public section_type getSectionType(){
		return sectionType;
	}
}
