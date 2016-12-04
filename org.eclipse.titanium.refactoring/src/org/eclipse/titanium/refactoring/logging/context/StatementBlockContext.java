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

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.Definition_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Log_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;

/**
 * 
 * @author Viktor Varga
 */
public class StatementBlockContext extends Context {
	
	private List<Identifier> localVarIds;

	StatementBlockContext(final StatementBlock st, final Settings settings) {
		super(st, settings);
	}
	
	public StatementBlock getNode() {
		return (StatementBlock)super.getNode();
	}
	
	@Override
	protected void process_internal() {
		localVarIds = new ArrayList<Identifier>();
		final Context bottom = getBottom();
		final IVisitableNode bottomNode = bottom.getNode();
		if (!(bottomNode instanceof Log_Statement)) {
			ErrorReporter.logError("StatementBlockContext.process_internal(): Warning! Context chain bottom node is not a Log_Statement! ");
			return;
		}
		
		final Log_Statement logst = (Log_Statement)bottomNode;
		final Location logLoc = logst.getLocation();
		//
		final StatementBlock sb = getNode();
		final StatementBlockVisitor vis = new StatementBlockVisitor(logLoc);
		sb.accept(vis);
		localVarIds.addAll(vis.getIdsFound());
	}

	@Override
	protected List<String> createLogParts_internal(final Set<String> idsAlreadyHandled) {
		final List<String> ret = new ArrayList<String>();
		if (localVarIds == null) {
			return ret;
		}
		for (Identifier id: localVarIds) {
			final String idS = id.toString();
			if (idsAlreadyHandled.contains(idS)) {
				continue;
			}
			idsAlreadyHandled.add(idS);
			ret.add(formatLogPart(idS));
		}
		return ret;
	}
	
	/** @return true if the end offset of l1 is <= than the offset of l2 */
	private static boolean isLocationBefore(final Location l1, final Location l2) {
		return l1.getEndOffset() <= l2.getOffset();
	}

	/** 
	 * Collects all variable identifiers from a {@link StatementBlock} which are 
	 *  located before the given location.
	 * <p>
	 * Call on {@link StatementBlock}
	 * */
	private static class StatementBlockVisitor extends ASTVisitor {
		
		private final Location beforeLoc;
		private final List<Identifier> idsFound;
		
		public StatementBlockVisitor(final Location beforeLoc) {
			this.beforeLoc = beforeLoc;
			idsFound = new ArrayList<Identifier>();
		}
		
		public List<Identifier> getIdsFound() {
			return idsFound;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Statement) {
				final Statement st = (Statement)node;
				if (!isLocationBefore(st.getLocation(), beforeLoc)) {
					return V_SKIP;
				}
				if (st instanceof Definition_Statement) {
					final Definition_Statement defst = (Definition_Statement)st;
					final Definition def = defst.getDefinition();
					if (def != null && def instanceof Def_Var) {
						idsFound.add(((Def_Var)def).getIdentifier());
					}
				}
				return V_SKIP;
			}
			return V_CONTINUE;
		}
		
	}

}
