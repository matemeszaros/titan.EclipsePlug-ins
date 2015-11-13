/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.common.parsers.TitanListener;

public class PPListener extends TitanListener {

	public PPListener() {
		super();
	}

	public PPListener(PreprocessorDirectiveParser parser) {
		super.errorsStored = parser.getErrorStorage(); 	
	}

}
