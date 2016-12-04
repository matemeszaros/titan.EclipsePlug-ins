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
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;

/**
 * Context class representing {@link Def_Function} nodes.
 * 
 * @author Viktor Varga
 */
class FunctionContext extends Context {
	
	private List<Identifier> paramIds;

	public FunctionContext(final Def_Function func, final Settings settings) {
		super(func, settings);
	}
	
	@Override
	public Def_Function getNode() {
		return (Def_Function)super.getNode();
	}

	@Override
	protected void process_internal() {
		Def_Function func = getNode();
		FormalParameterList fpl = func.getFormalParameterList();
		ParameterListVisitor vis = new ParameterListVisitor();
		fpl.accept(vis);
		paramIds = vis.getResult();
	}

	@Override
	protected List<String> createLogParts_internal(final Set<String> idsAlreadyHandled) {
		List<String> ret = new ArrayList<String>();
		if (paramIds == null) {
			return ret;
		}
		for (Identifier id: paramIds) {
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
	 * Collects all the necessary variable identifiers from an {@link FormalParameterList}.
	 * <p>
	 * Call on {@link FormalParameterList}.
	 * */
	private static class ParameterListVisitor extends ASTVisitor {

		private final List<Identifier> result = new ArrayList<Identifier>();
		
		private List<Identifier> getResult() {
			return result;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Identifier) {
				result.add((Identifier)node);
				return V_SKIP;
			}
			//skip parameter types
			if (node instanceof Referenced_Type) {
				return V_SKIP;
			}
			//skip default values
			if (node instanceof TemplateInstance) {
				return V_SKIP;
			}
			return V_CONTINUE;
		}
		
	}

}
