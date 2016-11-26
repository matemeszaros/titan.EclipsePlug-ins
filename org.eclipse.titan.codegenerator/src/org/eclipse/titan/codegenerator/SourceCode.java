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

package org.eclipse.titan.codegenerator;

import static org.eclipse.titan.codegenerator.SourceCode.Indentation.MAX_LEVEL;
import static org.eclipse.titan.codegenerator.SourceCode.Indentation.TOKEN;

public class SourceCode {

	interface Indentation {
		int MAX_LEVEL = 50;
		String TOKEN = "\t";
	}

	private static final String BASE = new String(new char[MAX_LEVEL]).replace("\0", TOKEN);

	private StringBuilder builder = new StringBuilder();

	public SourceCode indent(int level) {
		builder.append(BASE.substring(0, level));
		return this;
	}

	public SourceCode line(Object... objects) {
		return append(objects).newLine();
	}

	public SourceCode append(Object... objects) {
		for (Object o : objects) {
			builder.append(o);
		}
		return this;
	}

	public SourceCode newLine() {
		builder.append(System.lineSeparator());
		return this;
	}

	public SourceCode write(int indent, Writable writable) {
		return writable.write(this, indent);
	}

	public SourceCode clear() {
		builder.setLength(0);
		return this;
	}

	@Override
	public String toString() {
		return builder.toString();
	}
}
