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

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCases;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstances;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * Select statements used with an expression that is not of 
 * enumerated type.
 * 
 * @author Viktor Varga
 */
public class ConvertToEnum extends BaseModuleCodeSmellSpotter {
	
	private static final String ERROR_MSG_SELECT = "Select should be used with enumerations. Branch coverage can not be calculated on {0} types.";
	private static final String ERROR_MSG_CASE = "Select cases should be described by enumeration items.";
	
	
	private final CompilationTimeStamp timestamp;

	public ConvertToEnum() {
		super(CodeSmellType.CONVERT_TO_ENUM);
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
		final IType.Type_type type = v.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (!type.equals(Type_type.TYPE_TTCN3_ENUMERATED)) {
			final IType governor = v.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
			if (governor != null && !governor.getIsErroneous(timestamp)) {
				problems.report(v.getLocation(), MessageFormat.format(ERROR_MSG_SELECT, governor.getTypename()));
			}
		}
		final SelectCases scs = s.getSelectCases();
		if (scs == null || scs.getSelectCaseArray() == null) {
			return;
		}
		for (final SelectCase sc: scs.getSelectCaseArray()) {
			if (sc.hasElse()) {
				continue;
			}
			final CaseVisitor visitor = new CaseVisitor(problems);
			sc.accept(visitor);
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(SelectCase_Statement.class);
		return ret;
	}
	
	private final class CaseVisitor extends ASTVisitor {
		
		private final Problems problems;
		
		public CaseVisitor(final Problems problems) {
			this.problems = problems;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof SelectCase) {
				return V_CONTINUE;
			} else if (node instanceof TemplateInstances) {
				return V_CONTINUE;
			} else if (node instanceof TemplateInstance) {
				final TemplateInstance ti = (TemplateInstance)node;
				final IType.Type_type type = ti.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
				if (!type.equals(Type_type.TYPE_TTCN3_ENUMERATED)) {
					problems.report(ti.getLocation(), ERROR_MSG_CASE);
				}
				return V_SKIP;
			}
			return V_SKIP;
		}
		
	}
}
