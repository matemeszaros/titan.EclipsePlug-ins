/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * 
 * @author Peter Olah
 */
public final class AssignmentHandlerFactory {

	private AssignmentHandlerFactory() {
		//private constructor to disable instantiation.
	}

	public static AssignmentHandler getDefinitionHandler(final Assignment assignment) {

		switch (assignment.getAssignmentType()) {
			case A_ALTSTEP:
			case A_FUNCTION:
			case A_FUNCTION_RTEMP:
			case A_FUNCTION_RVAL:
			case A_TESTCASE:
				return new AssignmentHandlerAFTRerences(assignment);
			case A_TYPE:
				IType type = assignment.getType(CompilationTimeStamp.getBaseTimestamp());
				if (type instanceof Component_Type) {
					return new AssignmentHandlerComponent(assignment);
				} else {
					return new AssignmentHandlerAFTRerences(assignment);
				}
			default:
				return new AssignmentHandlerAFTRerences(assignment);
		}
	}
}
