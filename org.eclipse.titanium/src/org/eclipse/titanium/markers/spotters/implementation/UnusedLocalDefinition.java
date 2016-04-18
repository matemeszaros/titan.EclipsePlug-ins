/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class UnusedLocalDefinition extends BaseModuleCodeSmellSpotter {
	public UnusedLocalDefinition() {
		super(CodeSmellType.UNUSED_LOCAL_DEFINITION);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (!(node instanceof Definition)) {
			// we do nothing and return
			return;
		}
		// since we assume node is a Definition
		Definition s = (Definition) node;
		if (s instanceof FormalParameter) {
			Definition enclose = ((FormalParameter) s).getMyParameterList().getMyDefinition();
			// do not bother formal parameters in external functions and
			// template definitions
			if (enclose instanceof Def_Extfunction || enclose instanceof Def_Template || enclose instanceof Def_Type) {
				return;
			}
		}
		// report if not used local or not used formal parameter
		final boolean report = !s.isUsed() && (s.isLocal() || s instanceof FormalParameter);
		if (report) {
			String name = s.getIdentifier().getDisplayName();
			String msg = MessageFormat.format("The {0} `{1}'' seems to be never used locally", s.getAssignmentName(), name);
			problems.report(s.getIdentifier().getLocation(), msg);
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Definition.class);
		return ret;
	}
}
