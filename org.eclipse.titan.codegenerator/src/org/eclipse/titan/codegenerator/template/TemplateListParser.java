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
import java.util.Iterator;
import java.util.List;

import org.eclipse.titan.codegenerator.Scope;
import org.eclipse.titan.codegenerator.SourceCode;
import org.eclipse.titan.codegenerator.myASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;

public class TemplateListParser extends TemplateValueParser {

	private final ListValue list;

	public TemplateListParser(Scope parent, ValueHolder holder, String type) {
		super(parent, holder);
		this.value = this.list = new ListValue(type);
	}

	@Override
	public Scope process(IVisitableNode node) {
		// TODO : detect the duplicate ListOfTemplates part in a more sophisticated way
		if (node instanceof TTCN3Template) {
			if (list.isFinished()) {
				return Action.skip(TTCN3Template.class, this);
			}
			ValueHolder holder = list.nextHolder();
			String type = list.nextType();
			return TemplateListParser.getScope(this, holder, type, node);
		}
		return this;
	}

	private class ListValue extends Value {
		private List<String> types = new ArrayList<>();
		private List<Value> values = new ArrayList<>();
		private Iterator<String> iterator;

		public ListValue(String type) {
			super(type);
			String[] types = myASTVisitor.nodeNameChildrenTypesHashMap.get(type);
			for (int i = 0; i < types.length; i++) {
				this.types.add(types[i]);
			}
			iterator = this.types.iterator();
		}

		public boolean isFinished() {
			return !iterator.hasNext();
		}

		public String nextType() {
			return iterator.next();
		}

		public ValueHolder nextHolder() {
			return value -> values.add(value);
		}

		@Override
		public SourceCode write(SourceCode code, int indent) {
			code.append("new " + type + "(");
			for (int i = 0; i < values.size(); i++) {
				if (i != 0) {
					code.append(",");
				}
				code.newLine().indent(indent + 2);
				values.get(i).write(code, indent + 2);
			}
			code.newLine().indent(indent).append(")");
			return code;
		}
	}
}
