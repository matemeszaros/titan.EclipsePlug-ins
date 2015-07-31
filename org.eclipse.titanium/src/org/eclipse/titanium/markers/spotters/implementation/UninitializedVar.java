/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class UninitializedVar extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Variable templates should be initialized";

	public UninitializedVar() {
		super(CodeSmellType.UNINITIALIZED_VARIABLE);
	}
	
	@Override
	protected void process(IVisitableNode node, Problems problems) {
		if (node instanceof Def_Var_Template) {
			Def_Var_Template d = (Def_Var_Template)node;
			if (d.getInitialValue() == null) {
				problems.report(d.getLocation(), ERROR_MESSAGE);
			}
		} else if (node instanceof Def_Var) {
			Def_Var d = (Def_Var)node;
			if (d.getInitialValue() == null) {
				problems.report(d.getLocation(), ERROR_MESSAGE);
			}
		} else {
			return;
		}
	}
	
	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(2);
		ret.add(Def_Var_Template.class);
		ret.add(Def_Var.class);
		return ret;
	}
	
}