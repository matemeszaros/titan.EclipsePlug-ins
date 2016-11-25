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

import org.eclipse.titan.codegenerator.Scope;
import org.eclipse.titan.codegenerator.myASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.Type;

public class TemplateParser implements Scope {

	private final Scope parent;
	private final ModuleTemplates registry;
	private String name = null;
	private String type = null;
	private Template template = null;

	private boolean isModification = false;

	public TemplateParser(Scope parent, ModuleTemplates registry) {
		this.parent = parent;
		this.registry = registry;
	}

	@Override
	public Scope process(IVisitableNode node) {
		if (node instanceof Identifier) {
			name = node.toString();
			// TODO : find a more sophisticated way of storing symbols (e.g. SymbolTable)
			myASTVisitor.nodeNameNodeTypeHashMap.put(name, "template");
		}
		if (node instanceof Type) {
			type = Util.getTypeName((Type) node);
			template = new Template(name, type);
			return Action.skip(Type.class, this);
		}
		if (node instanceof FormalParameterList) {
			return new FormalParameterParser(this, template);
		}
		// is a modification
		if (node instanceof Reference) {
			Reference reference = (Reference) node;
			String basename = reference.getId().toString();
			Template base = registry.find(basename);
			// TODO : templates need a base value as a fallback
			// TODO : this base value should be used as a reference, to diff against
			// TODO : this was a hotfix for the union types to work
			String type = base.getValue().getType();
			template.setValue(new Value(type,
					(code, indent) -> code.append("(", type,") ").append(basename, "()")
			));
			isModification = true;
			return Action.skip(Reference.class, this);
		}
		if (node instanceof TTCN3Template) {
			if (isModification) {
				return ModificationParser.getScope(this, template, "", type, node);
			}
			return TemplateValueParser.getScope(this, template, type, node);
		}
		return this;
	}

	@Override
	public Scope finish(IVisitableNode node) {
		if (node instanceof Def_Template) {
			registry.add(template);
			return parent;
		}
		return this;
	}
}
