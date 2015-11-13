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

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCases;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstances;
import org.eclipse.titan.designer.AST.TTCN3.types.EnumItem;
import org.eclipse.titan.designer.AST.TTCN3.types.EnumerationItems;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Enumerated_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Enumerated_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * A select statement that uses an enumerated type as expression
 * does not have all the enum items covered in its case branches.
 * Those select statements are ignored which have an else branch
 * or contain an unfoldable value in one of their case expressions.
 * 
 * @author Viktor Varga
 */
public class SelectCoverage extends BaseModuleCodeSmellSpotter {
	
	private static final String ERR_MSG = "Missing select branch. The enumeration `{0}'' has {1} enumeration items, but only {2} are covered. Items not covered: {3} ";
	
	private final CompilationTimeStamp timestamp;
	

	public SelectCoverage() {
		super(CodeSmellType.SELECT_COVERAGE);
		timestamp = CompilationTimeStamp.getBaseTimestamp();
	}
	
	@Override
	protected void process(IVisitableNode node, Problems problems) {
		if (!(node instanceof SelectCase_Statement)) {
			return;
		}
		SelectCase_Statement s = (SelectCase_Statement)node;
		Value v = s.getExpression();
		if (v == null || v.getIsErroneous(timestamp)) {
			return;
		}
		//if there is an else branch, no smell will be reported
		SelectCases scs = s.getSelectCases();
		if (scs == null || scs.getSelectCaseArray() == null) {
			return;
		}
		for (SelectCase sc: scs.getSelectCaseArray()) {
			if (sc.hasElse()) {
				return;
			}
		}
		IType itype = v.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
		if (itype instanceof Referenced_Type) {
			itype = itype.getTypeRefdLast(timestamp);
		}
		if (itype == null || !(itype instanceof TTCN3_Enumerated_Type)) {
			return;
		}
		TTCN3_Enumerated_Type enumType = (TTCN3_Enumerated_Type)itype;
		//count number of items in enum, get all items from enum
		EnumItemVisitor enumVisitor = new EnumItemVisitor();
		enumType.accept(enumVisitor);
		int enumSize = enumVisitor.getCount();
		List<Identifier> allEnumItems = enumVisitor.getItemsFound();
		//count number of TemplateInstances in select, get used enum items
		CaseVisitor caseVisitor = new CaseVisitor();
		scs.accept(caseVisitor);
		List<Identifier> usedEnumItems = caseVisitor.getItemsUsed();
		if (caseVisitor.isContainsUnfoldable()) {
			return;
		}
		int casesSize = caseVisitor.getCount();
		if (enumSize > casesSize) {
			String enumName = itype.getTypename();
			String itemsNotCovered = getItemsNotCovered(allEnumItems, usedEnumItems);
			problems.report(v.getLocation(), MessageFormat.format(ERR_MSG, enumName, enumSize, casesSize, itemsNotCovered));
		}
		
	}

	private String getItemsNotCovered(List<Identifier> allEnumItems, List<Identifier> usedEnumItems) {
		if (allEnumItems == null || usedEnumItems == null) {
			return "";
		}

		StringBuilder ret = new StringBuilder();
		boolean start = true;
		for (Identifier id : allEnumItems) {
			if (!usedEnumItems.contains(id)) {
				// id is not used
				if (!start) {
					ret.append(", ");
					start = false;
				}
				
				ret.append(id.toString());
			}
		}
		return ret.toString();
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(SelectCase_Statement.class);
		return ret;
	}
	
	private final class CaseVisitor extends ASTVisitor {

		private List<Identifier> itemsUsed = new ArrayList<Identifier>();
		private int count = 0;
		private boolean containsUnfoldable = false;
		
		public int getCount() {
			return count;
		}
		public List<Identifier> getItemsUsed() {
			return itemsUsed;
		}
		
		public boolean isContainsUnfoldable() {
			return containsUnfoldable;
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
				TemplateInstance ti = (TemplateInstance)node;
				IValue val = ti.getTemplateBody().getValue();
				if (val == null || val.getIsErroneous(timestamp) || val.isUnfoldable(timestamp)) {
					containsUnfoldable = true;
					return V_ABORT;
				}
				if (val instanceof Undefined_LowerIdentifier_Value) {
					val = val.setLoweridToReference(timestamp);
					if (val instanceof Enumerated_Value) {
						Enumerated_Value ev = (Enumerated_Value)val;
						Identifier id = ev.getValue();
						itemsUsed.add(id);
					}
				}
				count++;
				return V_SKIP;
			}
			return V_SKIP;
		}
		
	}
	private static final class EnumItemVisitor extends ASTVisitor {

		private List<Identifier> itemsFound = new ArrayList<Identifier>();
		private int count = 0;
		
		public int getCount() {
			return count;
		}
		public List<Identifier> getItemsFound() {
			return itemsFound;
		}
		
		@Override
		public int visit(IVisitableNode node) {
			if (node instanceof TTCN3_Enumerated_Type) {
				return V_CONTINUE;
			} else if (node instanceof EnumerationItems) {
				return V_CONTINUE;
			} else if (node instanceof EnumItem) {
				itemsFound.add(((EnumItem)node).getId());
				count++;
				return V_SKIP;
			}
			return V_SKIP;
		}
		
	}
	
}
