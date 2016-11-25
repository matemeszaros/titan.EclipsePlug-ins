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
import java.util.StringJoiner;

import org.eclipse.titan.codegenerator.Scope;
import org.eclipse.titan.codegenerator.SourceCode;
import org.eclipse.titan.codegenerator.myASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;

public class NamedTemplateListParser extends TemplateValueParser {

	private NamedListValue list;
	private String name;

	public NamedTemplateListParser(Scope parent, ValueHolder holder, String type) {
		super(parent, holder);
		this.list = new NamedListValue(type);
		this.value = list;
	}

	@Override
	public Scope process(IVisitableNode node) {
		if (node instanceof NamedTemplate) {
			NamedTemplate nt = (NamedTemplate) node;
			name = nt.getName().toString();
		}
		if (node instanceof TTCN3Template) {
			ValueHolder holder = list.getHolder(name);
			String type = list.getType(name);
			return TemplateValueParser.getScope(this, holder, type, node);
		}
		return this;
	}

	private class NamedListValue extends Value {
		private List<String> names = new ArrayList<>();
		private HashMap<String, String> types = new HashMap<>();
		private HashMap<String, Value> values = new HashMap<>();

		public NamedListValue(String type) {
			super(type);
			String[] names = myASTVisitor.nodeNameChildrenNamesHashMap.get(type);
			String[] types = myASTVisitor.nodeNameChildrenTypesHashMap.get(type);
			for (int i = 0; i < names.length || i < types.length; i++) {
				this.names.add(names[i]);
				this.types.put(names[i], types[i]);
			}
		}

		public String getType(String name) {
			return types.get(name);
		}

		public ValueHolder getHolder(String name) {
			return value -> values.put(name, value);
		}

		@Override
		public String toString() {
			StringJoiner s = new StringJoiner(", ", "new " + type + "(", ")");
			for (String name : names) {
				s.add(String.valueOf(values.get(name)));
			}
			return s.toString();
		}

		@Override
		public SourceCode write(SourceCode code, int indent) {
			code.append("new " + type + "(");
			for (int i = 0; i < names.size(); i++) {
				if (i != 0) {
					code.append(",");
				}
				code.newLine().indent(indent + 2);
				values.get(names.get(i)).write(code, indent + 2);
			}
			code.newLine().indent(indent).append(")");
			return code;
		}
	}
}
