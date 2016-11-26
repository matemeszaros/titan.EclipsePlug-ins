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

package org.eclipse.titan.codegenerator.constant;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.codegenerator.JavaSourceProvider;
import org.eclipse.titan.codegenerator.SourceCode;

public class ModuleConstants implements JavaSourceProvider {
	private List<Constant> constants = new ArrayList<>();

	public void add(Constant c) {
		constants.add(c);
	}

	public String getClassName() {
		return "Constants";
	}

	@Override
	public String getJavaSource() {
		SourceCode code = new SourceCode();
		code.line("public class ", getClassName(), " {");
		for (Constant c : constants) {
			code.newLine();
			code.indent(1).line("public static ", c.type, " ", c.name, "() {");
			code.indent(2).append("return ").write(2, c.value).line(";");
			code.indent(1).line("}");
		}
		code.line("}");
		return code.toString();
	}
}
