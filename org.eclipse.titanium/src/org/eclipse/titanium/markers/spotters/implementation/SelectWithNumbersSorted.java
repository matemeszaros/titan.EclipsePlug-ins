/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCases;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstances;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * In a select statement, where the expression is type Integer and
 * the succeeding cases are not incremental ordered.
 * Those select statements are ignored which have an else branch
 * or contain an unfoldable value in one of their case expressions.
 * 
 * @author Ilyes Eniko 
 */
public class SelectWithNumbersSorted extends  BaseModuleCodeSmellSpotter {
	private static final String ERR_MSG = "Select cases are not listed in incremental order.";
	
	private final CompilationTimeStamp timestamp;
	

	public SelectWithNumbersSorted() {
		super(CodeSmellType.SELECT_WITH_NUMBERS_SORTED);
		timestamp = CompilationTimeStamp.getBaseTimestamp();
	}


	@Override
	protected void process(final IVisitableNode node, final Problems problems) {
		if (!(node instanceof SelectCase_Statement)) {
			return;
		}

		final SelectCase_Statement s = (SelectCase_Statement)node;
		final Value v = s.getExpression();
		if (v == null || v.getIsErroneous(timestamp)) {
			return;
		}
		
		final SelectCases scs = s.getSelectCases();
		if (scs == null || scs.getSelectCaseArray() == null) {
			return;
		}
		
		//if there is an else branch, no smell will be reported
		for (final SelectCase sc: scs.getSelectCaseArray()) {
			if (sc.hasElse()) {
				return;
			}
		}
		
		IType itype = v.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
		// TODO Kristof: az ellenorzes folosleges.
		if (itype instanceof Referenced_Type) {
			itype = itype.getTypeRefdLast(timestamp);
		}
		
		if (itype == null || !(itype instanceof Integer_Type)) {
			return;
		}
		
		//count number of cases in select, get used integer type case-items
		final CaseVisitorInteger caseVisitorInteger = new CaseVisitorInteger();
		scs.accept(caseVisitorInteger);
		if (caseVisitorInteger.containsUnfoldable()) {
			return;
		}

		final List<Long> usedIntegerItems = caseVisitorInteger.getItemsUsed();
		if (!checkIfIntegerCasesSorted(usedIntegerItems)) {
			problems.report(s.getLocation(), ERR_MSG);	
			
		}

	}

	private Boolean checkIfIntegerCasesSorted(final List<Long> usedIntegerItems){
		for (int i=0; i < usedIntegerItems.size()-1; ++i) {	
			if (usedIntegerItems.get(i) > usedIntegerItems.get(i+1)) {
				return false;
			} 
		} 
		return true;
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(SelectCase_Statement.class);
		return ret;
	}
	
	private final class CaseVisitorInteger extends ASTVisitor {

		private final List<Long> itemsUsed = new ArrayList<Long>();
		private boolean foundUnfoldable = false;
		
		public List<Long> getItemsUsed() {
			return itemsUsed;
		}

		public boolean containsUnfoldable() {
			return foundUnfoldable;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof SelectCases) {
				return V_CONTINUE;
			} else if (node instanceof SelectCase) {
				return V_CONTINUE;
			} else if (node instanceof TemplateInstances) {
				return V_CONTINUE;
			} else if (node instanceof TemplateInstance) {
				final TemplateInstance ti = (TemplateInstance)node;
				// TODO Kristof: isvalue ?
				final IValue val = ti.getTemplateBody().getValue();
				if (val == null || val.getIsErroneous(timestamp) || val.isUnfoldable(timestamp)) {
					foundUnfoldable = true;
					return V_ABORT;
				}
				if (val instanceof Integer_Value) {
					final Long id = ((Integer_Value)val).getValue();
					itemsUsed.add(id);
				}
				
				return V_SKIP;
			}
			return V_SKIP;
		}
		
	}

	
}
