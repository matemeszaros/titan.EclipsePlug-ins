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
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class ReadOnlyOutPar extends BaseModuleCodeSmellSpotter {
	public static final String READONLY = "The {0} seems to be never written, maybe it could be an `in'' parameter";

	public ReadOnlyOutPar() {
		super(CodeSmellType.READONLY_OUT_PARAM);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof FormalParameter) {
			FormalParameter s = (FormalParameter) node;
			if (s.getMyParameterList().getMyDefinition() instanceof Def_Type) {
				return;
			}
			if (!s.getWritten() && !(s.getMyParameterList().getMyDefinition() instanceof Def_Extfunction)) {
				switch (s.getAssignmentType()) {
				case A_PAR_VAL_OUT:
				case A_PAR_TEMP_OUT:
					String msg = MessageFormat.format(READONLY, s.getDescription());
					problems.report(s.getLocation(), msg);
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(FormalParameter.class);
		return ret;
	}
}
