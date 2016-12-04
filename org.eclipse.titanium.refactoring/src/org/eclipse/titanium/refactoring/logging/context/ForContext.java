/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.TTCN3.definitions.For_Loop_Definitions;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;

/**
 * Context class representing {@link For_Statement} nodes.
 * 
 * @author Viktor Varga
 */
class ForContext extends Context {
	
	private List<Identifier> initialAssignmentIds;

	ForContext(final For_Statement st, final Settings settings) {
		super(st, settings);
	}
	
	@Override
	public For_Statement getNode() {
		return (For_Statement)super.getNode();
	}

	protected void process_internal() {
		For_Statement st = getNode();
		ForVisitor vis = new ForVisitor();
		st.accept(vis);
		initialAssignmentIds = vis.getResult();
	}
	
	@Override
	protected List<String> createLogParts_internal(final Set<String> idsAlreadyHandled) {
		List<String> ret = new ArrayList<String>();
		if (initialAssignmentIds == null) {
			return ret;
		}
		for (Identifier id: initialAssignmentIds) {
			String idS = id.toString();
			if (idsAlreadyHandled.contains(idS)) {
				continue;
			}
			idsAlreadyHandled.add(idS);
			ret.add(formatLogPart(idS));
		}
		return ret;
	}

	/** 
	 * Collects all the necessary variable identifiers from a {@link For_Statement}.
	 * <p>
	 * Call on {@link For_Statement} 
	 * */
	private static class ForVisitor extends ASTVisitor {

		private final List<Identifier> result = new ArrayList<Identifier>();
		
		private List<Identifier> getResult() {
			return result;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof For_Statement) {
				return V_CONTINUE;
			}
			if (node instanceof For_Loop_Definitions) {
				For_Loop_Definitions flds = (For_Loop_Definitions)node;
				int count = flds.getNofAssignments();
				for (int i=0;i<count;i++) {
					result.add(flds.getAssignmentByIndex(i).getIdentifier());
				}
			}
			return V_SKIP;
		}
		
		
		
	}
}
