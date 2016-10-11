/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.TTCN3.statements.Function_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Instance_Statement;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class UnusedRetval extends BaseModuleCodeSmellSpotter {
	private static final String UNUSEDRETURN2 = "The template returned by {0} is not used";
	private static final String UNUSEDRETURN1 = "The value returned by {0} is not used";

	public UnusedRetval() {
		super(CodeSmellType.UNUSED_FUNTION_RETURN_VALUES);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof Unknown_Instance_Statement) {
			final Unknown_Instance_Statement u = (Unknown_Instance_Statement) node;
			final Statement s = u.getRealStatement();
			if (s instanceof Function_Instance_Statement) {
				final Function_Instance_Statement f = (Function_Instance_Statement) s;

				final Assignment assignment = f.getReference().getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				if (assignment != null) {
					String msg;
					switch (assignment.getAssignmentType()) {
					case A_FUNCTION_RVAL:
					case A_EXT_FUNCTION_RVAL:
						msg = MessageFormat.format(UNUSEDRETURN1, assignment.getIdentifier().getDisplayName());
						problems.report(f.getLocation(), msg);
						break;
					case A_FUNCTION_RTEMP:
					case A_EXT_FUNCTION_RTEMP:
						msg = MessageFormat.format(UNUSEDRETURN2, assignment.getIdentifier().getDisplayName());
						problems.report(f.getLocation(), msg);
						break;
					default:
						break;
					}
				}

			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Unknown_Instance_Statement.class);
		return ret;
	}
}
