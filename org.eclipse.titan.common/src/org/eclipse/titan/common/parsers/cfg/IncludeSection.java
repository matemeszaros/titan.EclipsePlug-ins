/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import java.util.ArrayList;
import java.util.List;

public class IncludeSection implements ISection {

	List<String> mIncludeFileNames = new ArrayList<String>();
	
	public IncludeSection() {
	}
	
	public void addIncludeFileName( String aFileName ) {
		mIncludeFileNames.add(aFileName);
	}

}
