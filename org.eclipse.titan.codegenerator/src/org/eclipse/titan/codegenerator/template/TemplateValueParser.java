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
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.templates.AnyOrOmit_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Any_Value_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;

public abstract class TemplateValueParser implements Scope {

	public static Scope getScope(Scope parent, ValueHolder holder, String type, IVisitableNode node) {
		if (node instanceof SpecificValue_Template) {
			return new SpecificValueParser(parent, holder, type);
		} else if (node instanceof Named_Template_List) {
			if (Util.isUnion(type)) {
				return new UnionValueParser(parent, holder, type);
			} else {
				return new NamedTemplateListParser(parent, holder, type);
			}
		} else if (node instanceof Template_List) {
			if (Util.isRecordOf(type)) {
				return new RecordOfValueParser(parent, holder, type);
			}
			return new TemplateListParser(parent, holder, type);
		} else if (node instanceof Any_Value_Template) {
			return Action.waitFor(
					Any_Value_Template.class, parent,
					() -> holder.setValue(Value.ANY(type))
			);
		} else if (node instanceof AnyOrOmit_Template) {
			return Action.waitFor(
					AnyOrOmit_Template.class, parent,
					() -> holder.setValue(Value.ANY_OR_OMIT(type))
			);
		} else {
			return new UnknownValueParser(parent, holder, type);
		}
	}

	protected final Scope parent;
	protected final ValueHolder holder;
	protected Value value;

	public TemplateValueParser(Scope parent, ValueHolder holder) {
		this.parent = parent;
		this.holder = holder;
	}

	@Override
	public Scope finish(IVisitableNode node) {
		if (node instanceof TTCN3Template) {
			holder.setValue(value);
			return parent;
		}
		return this;
	}
}
