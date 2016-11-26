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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.titan.codegenerator.Scope;
import org.eclipse.titan.codegenerator.SourceCode;
import org.eclipse.titan.codegenerator.Writable;
import org.eclipse.titan.codegenerator.myASTVisitor;
import org.eclipse.titan.codegenerator.template.Action;
import org.eclipse.titan.codegenerator.template.Util;
import org.eclipse.titan.codegenerator.template.Value;
import org.eclipse.titan.codegenerator.template.ValueHolder;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.AST.Type;

public class ConstantParser implements Scope {
	private final ModuleConstants repository;
	private Scope parent;
	private String name;
	private String type;
	private Value value;

	public ConstantParser(Scope parent, ModuleConstants repository) {
		this.parent = parent;
		this.repository = repository;
	}

	@Override
	public Scope process(IVisitableNode node) {
		if (node instanceof Identifier) {
			name = node.toString();
		}
		if (node instanceof Type) {
			type = Util.getTypeName((Type) node);
			value = new Value(type, Writable.NULL);
			return Action.skip(Type.class, this);
		}
		if (node instanceof org.eclipse.titan.designer.AST.Value) {
			ValueHolder holder = v -> value = v;
			return dispatch(this, type, holder, node);
		}
		return this;
	}

	@Override
	public Scope finish(IVisitableNode node) {
		if (node instanceof Def_Const) {
			myASTVisitor.nodeNameNodeTypeHashMap.put(name, "constant");
			repository.add(new Constant(type, name, value));
			return parent;
		}
		return this;
	}

	private static Scope dispatch(Scope parent, String type, ValueHolder holder, IVisitableNode node) {
		if (node instanceof Sequence_Value) {
			if (Util.isUnion(type)) {
				return new UnionValueParser(parent, holder, type);
			}
			return new NamedSequenceParser(parent, holder, type);
		}
		if (node instanceof SequenceOf_Value) {
			if (Util.isRecordOf(type)) {
				String subtype = myASTVisitor.nodeNameRecordOfTypesHashMap.get(type);
				return new SequenceOfParser(parent, holder, type, subtype);
			}
			if (Util.isSetOf(type)) {
				String subtype = myASTVisitor.nodeNameSetOfTypesHashMap.get(type);
				return new SequenceOfParser(parent, holder, type, subtype);
			}
			return new SequenceParser(parent, holder, type);
		}
		holder.setValue(Util.extract(type, node));
		return parent;
//		Debug.println("skip " + node.getClass().getSimpleName());
//		return Action.skip(node.getClass(), parent);
	}

	private static class NamedSequenceParser implements Scope {

		private final Scope parent;
		private String name;
		private final SequenceValue record;

		public NamedSequenceParser(Scope parent, ValueHolder holder, String type) {
			this.parent = parent;
			record = new SequenceValue(type);
			holder.setValue(record);
		}

		@Override
		public Scope process(IVisitableNode node) {
			if (node instanceof NamedValue) {
				NamedValue namedValue = (NamedValue) node;
				name = namedValue.getName().toString();
			}
			if (node instanceof org.eclipse.titan.designer.AST.Value) {
				String type = record.getType(name);
				ValueHolder holder = record.getHolder(name);
				return dispatch(this, type, holder, node);
			}
			return this;
		}

		@Override
		public Scope finish(IVisitableNode node) {
			if (node instanceof Sequence_Value) {
				return parent;
			}
			return this;
		}
	}

	private static class SequenceParser implements Scope {
		private final Scope parent;
		private final SequenceValue record;

		public SequenceParser(Scope parent, ValueHolder holder, String type) {
			this.parent = parent;
			this.record = new SequenceValue(type);
			holder.setValue(record);
		}

		@Override
		public Scope process(IVisitableNode node) {
			if (node instanceof org.eclipse.titan.designer.AST.Value) {
				String name = record.nextName();
				String type = record.getType(name);
				ValueHolder holder = record.getHolder(name);
				return dispatch(this, type, holder, node);
			}
			return this;
		}

		@Override
		public Scope finish(IVisitableNode node) {
			if (node instanceof SequenceOf_Value) {
				return parent;
			}
			return this;
		}
	}

	private static class SequenceOfParser implements Scope {

		private final Scope parent;
		private final String type;
		private final SequenceOfValue value;

		public SequenceOfParser(Scope parent, ValueHolder holder, String type, String subtype) {
			this.parent = parent;
			this.type = subtype;
			this.value = new SequenceOfValue(type);
			holder.setValue(value);
		}

		@Override
		public Scope process(IVisitableNode node) {
			if (node instanceof org.eclipse.titan.designer.AST.Value) {
				return dispatch(this, type, value.nextHolder(), node);
			}
			return this;
		}

		@Override
		public Scope finish(IVisitableNode node) {
			if (node instanceof SequenceOf_Value) {
				return parent;
			}
			return this;
		}
	}

	private static class SequenceValue extends Value {
		private List<String> names = new ArrayList<>();
		private HashMap<String, String> types = new HashMap<>();
		private HashMap<String, Value> values = new HashMap<>();
		private Iterator<String> iterator;

		public SequenceValue(String type) {
			super(type);
			String[] names = myASTVisitor.nodeNameChildrenNamesHashMap.get(type);
			String[] types = myASTVisitor.nodeNameChildrenTypesHashMap.get(type);
			for (int i = 0; i < names.length || i < types.length; i++) {
				this.names.add(names[i]);
				this.types.put(names[i], types[i]);
			}
			iterator = this.names.iterator();
		}

		public String getType(String name) {
			return types.get(name);
		}

		public ValueHolder getHolder(String name) {
			return value -> values.put(name, value);
		}

		@Override
		public SourceCode write(SourceCode code, int indent) {
			code.append("new ", type, "(");
			for (int i = 0; i < names.size(); i++) {
				if (i != 0) {
					code.append(",");
				}
				code.newLine().indent(indent + 2);
				code.write(indent + 2, values.get(names.get(i)));
			}
			code.newLine().indent(indent).append(")");
			return code;
		}

		public String nextName() {
			return iterator.next();
		}
	}

	private static class SequenceOfValue extends Value {

		private List<Value> values = new ArrayList<>();

		public SequenceOfValue(String type) {
			super(type);
		}

		public ValueHolder nextHolder() {
			int i = values.size();
			return v -> values.add(i, v);
		}

		@Override
		public SourceCode write(SourceCode code, int indent) {
			code.append("new ", type, "(");
			for (int i = 0; i < values.size(); i++) {
				if (i != 0) {
					code.append(",");
				}
				code.newLine().indent(indent + 2);
				code.write(indent + 2, values.get(i));
			}
			code.newLine().indent(indent).append(")");
			return code;
		}
	}

	private static class UnionValueParser implements Scope {

		private final Scope parent;
		private final UnionValue union;

		public UnionValueParser(Scope parent, ValueHolder holder, String type) {
			this.parent = parent;
			this.union = new UnionValue(type);
			holder.setValue(union);
		}

		@Override
		public Scope process(IVisitableNode node) {
			if (node instanceof Identifier) {
				union.setField(node.toString());
			}
			if (node instanceof org.eclipse.titan.designer.AST.Value) {
				return dispatch(this, union.getFieldType(), union, node);
			}
			return this;
		}

		@Override
		public Scope finish(IVisitableNode node) {
			if (node instanceof Sequence_Value) {
				return parent;
			}
			return this;
		}
	}

	private static class UnionValue extends Value implements ValueHolder {
		private HashMap<String, String> types = new HashMap<>();
		private String field;
		private Value value;

		public UnionValue(String type) {
			super(type);
			String[] names = myASTVisitor.nodeNameChildrenNamesHashMap.get(type);
			String[] types = myASTVisitor.nodeNameChildrenTypesHashMap.get(type);
			for (int i = 0; i < names.length || i < types.length; i++) {
				this.types.put(names[i], types[i]);
			}
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getFieldType() {
			return types.get(field);
		}

		@Override
		public SourceCode write(SourceCode code, int indent) {
			code.append("new ", "SC_", field, "_", type, "(");
			code.write(indent, value);
			code.append(")");
			return code;
		}

		@Override
		public void setValue(Value value) {
			this.value = value;
		}
	}
}
