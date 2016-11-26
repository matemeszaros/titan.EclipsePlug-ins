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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.titan.codegenerator.Scope;
import org.eclipse.titan.codegenerator.SourceCode;
import org.eclipse.titan.codegenerator.myASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;

public class UnionValueParser extends TemplateValueParser {
	private UnionValue union;
	private String subtype;

	private Map<String, String> map = new HashMap<>();
	private Map<String, String> lookup = new HashMap<>();

	private class UnionValue extends Value implements ValueHolder {

		private final String supertype;
		private Value value;

		public UnionValue(String supertype) {
			super(supertype);
			this.supertype = supertype;
		}

		@Override
		public SourceCode write(SourceCode code, int indent) {
			code.append("new ", getType(), "(");
			value.write(code, indent);
			return code.append(")");
		}

		@Override
		public void setValue(Value value) {
			this.value = value;
		}

		@Override
		public String getType() {
			return String.format("SC_%s_%s", lookup.get(subtype), supertype);
		}
	}

	public UnionValueParser(Scope parent, ValueHolder holder, String type) {
		super(parent, holder);
		this.value = this.union = new UnionValue(type);
		String[] names = myASTVisitor.nodeNameChildrenNamesHashMap.get(type);
		String[] types = myASTVisitor.nodeNameChildrenTypesHashMap.get(type);
		for (int i = 0; i < names.length; i++) {
			map.put(names[i], types[i]);
			lookup.put(types[i], names[i]);
		}
	}

	@Override
	public Scope process(IVisitableNode node) {
		if (node instanceof NamedTemplate) {
			NamedTemplate nt = (NamedTemplate) node;
			String name = nt.getName().toString();
			subtype = map.get(name);
		}
		if (node instanceof TTCN3Template) {
			return TemplateValueParser.getScope(this, union, subtype, node);
		}
		return this;
	}
}
