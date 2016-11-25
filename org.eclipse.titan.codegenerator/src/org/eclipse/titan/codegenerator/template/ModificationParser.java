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
import org.eclipse.titan.codegenerator.myASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.templates.AnyOrOmit_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Any_Value_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;

public class ModificationParser {

	public static Scope getScope(Scope parent, Modifiable modifiable, String path, String type, IVisitableNode node) {
		if (node instanceof Named_Template_List) {
			return new NamedModificationParser(parent, modifiable, path, type);
		}
		if (node instanceof SpecificValue_Template) {
			return new ModificationValueParser(parent, modifiable, path, type);
		}
		if (node instanceof Any_Value_Template) {
			return Action.waitFor(
					Any_Value_Template.class, parent,
					() -> modifiable.addModification(new Modification(path, Value.ANY(type)))
			);
		}
		if (node instanceof AnyOrOmit_Template) {
			return Action.waitFor(
					AnyOrOmit_Template.class, parent,
					() -> modifiable.addModification(new Modification(path, Value.ANY_OR_OMIT(type)))
			);
		}
		return parent;
	}

	private ModificationParser() {
	}

	private static class NamedModificationParser implements Scope {
		private final Scope parent;
		private final Modifiable modifiable;
		private final String path;
		private final Map<String, String> types = new HashMap<>();
		private String name;

		public NamedModificationParser(Scope parent, Modifiable modifiable, String path, String type) {
			this.parent = parent;
			this.modifiable = modifiable;
			this.path = path;
			String[] names = myASTVisitor.nodeNameChildrenNamesHashMap.get(type);
			String[] types = myASTVisitor.nodeNameChildrenTypesHashMap.get(type);
			for (int i = 0; i < names.length || i < types.length; i++) {
				this.types.put(names[i], types[i]);
			}
		}

		@Override
		public Scope process(IVisitableNode node) {
			if (node instanceof NamedTemplate) {
				NamedTemplate template = (NamedTemplate) node;
				name = template.getName().toString();
			}
			if (node instanceof TTCN3Template) {
				return getScope(this, modifiable, path + "." + name, types.get(name), node);
			}
			return this;
		}

		@Override
		public Scope finish(IVisitableNode node) {
			if (node instanceof TTCN3Template) {
				return parent;
			}
			return this;
		}
	}

	private static class ModificationValueParser extends SpecificValueParser {
		public ModificationValueParser(Scope parent, Modifiable modifiable, String path, String type) {
			super(parent, value -> modifiable.addModification(new Modification(path, value)), type);
		}
	}

}
