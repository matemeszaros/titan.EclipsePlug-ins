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

import java.util.ArrayList;
import java.util.List;

public class ModuleParameters implements JavaSourceProvider {

	private SourceCode code = new SourceCode();

	private List<Parameter> parameters = new ArrayList<>();

	public String getClassName() {
		return "Parameters";
	}

	@Override
	public String getJavaSource() {
		code.clear();
		code.line("class ", getClassName(), " {");
		for (Parameter p : parameters) {
			code.indent(1).line("public static ", p.type, " ", p.name, " = ", p.value, ";");
		}
		code.line("}");
		return code.toString();
	}

	public void add(String type, String name, String value) {
		AstWalkerJava.logToConsole("	Registered modulepar: " + type + " " + name);
		parameters.add(new Parameter(type, name, value));
	}

	private class Parameter {
		private final String type;
		private final String name;
		private final String value;

		Parameter(String type, String name, String value) {
			this.type = type;
			this.name = name;
			this.value = value;
		}
	}
}
