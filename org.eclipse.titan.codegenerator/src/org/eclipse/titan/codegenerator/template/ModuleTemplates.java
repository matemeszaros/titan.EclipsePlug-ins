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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.eclipse.titan.codegenerator.JavaSourceProvider;
import org.eclipse.titan.codegenerator.SourceCode;

public class ModuleTemplates implements JavaSourceProvider {

	private List<Template> templates = new ArrayList<>();

	private Map<String, Template> lookup = new HashMap<>();

	public String getClassName() {
		return "Templates";
	}

	@Override
	public String getJavaSource() {
		SourceCode code = new SourceCode();
		code.line("public class ", getClassName(), " {");
		for (Template t : templates) {
			code.newLine();
			StringJoiner params = new StringJoiner(", ");
			t.getParameters().forEach(field -> params.add(field.toString()));
			code.indent(1).line("public static ", t.getType(), " ", t.getName(), "(", params, ") {");
			Value value = t.getValue();
			if (value == null) {
				code.append(null, " /* Unexpected null value! */");
			} else {
				code.indent(2).append(value.getType(), " value = ");
				value.write(code, 2);
			}
			code.line(";");
			for (Modification m : t.getModifications()) {
				code.indent(2).append("value", m.path(), " = ");
				m.write(code, 2);
				code.line(";");
			}
			code.indent(2).line("return value;");
			code.indent(1).line("}");
		}
		code.line("}");
		return code.toString();
	}

	public void add(Template template) {
		templates.add(template);
		lookup.put(template.getName(), template);
	}

	public Template find(String name) {
		return lookup.get(name);
	}
}
