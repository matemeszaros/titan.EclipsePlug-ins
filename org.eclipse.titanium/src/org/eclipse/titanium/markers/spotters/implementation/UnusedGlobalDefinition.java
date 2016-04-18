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

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class UnusedGlobalDefinition extends BaseModuleCodeSmellSpotter {
	public UnusedGlobalDefinition() {
		super(CodeSmellType.UNUSED_GLOBAL_DEFINITION);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof Assignment) {
			Assignment s = (Assignment) node;
			if (!s.isUsed() && !s.isLocal() && !(s.getMyScope() instanceof ComponentTypeBody)) {
				String name = s.getIdentifier().getDisplayName();
				String msg = MessageFormat.format("The {0} `{1}'' seems to be never used globally", s.getAssignmentName(), name);
				problems.report(s.getIdentifier().getLocation(), msg);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Assignment.class);
		return ret;
	}
}
