/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.template;

import org.eclipse.titan.codegenerator.SourceCode;
import org.eclipse.titan.codegenerator.Writable;

public class Modification implements Writable {

	private final Writable writable;
	private final String path;

	public Modification(String path, Writable writable) {
		this.writable = writable;
		this.path = path;
	}

	public String path() {
		return path;
	}

	@Override
	public SourceCode write(SourceCode code, int indent) {
		return writable.write(code, indent);
	}
}
