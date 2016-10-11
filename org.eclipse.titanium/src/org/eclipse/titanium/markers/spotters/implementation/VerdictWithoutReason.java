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

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.LogArguments;
import org.eclipse.titan.designer.AST.TTCN3.statements.Setverdict_Statement;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value.Verdict_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class VerdictWithoutReason extends BaseModuleCodeSmellSpotter {
	private static final String WITHOUT_REASON = "{0} verdict should not be set without telling the reason";

	public VerdictWithoutReason() {
		super(CodeSmellType.SETVERDICT_WITHOUT_REASON);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (!(node instanceof Setverdict_Statement)) {
			return;
		}

		final Setverdict_Statement s = (Setverdict_Statement) node;
		final Value verdictValue = s.getVerdictValue();

		if (verdictValue == null) {
			return;
		}

		final CompilationTimeStamp ct = CompilationTimeStamp.getBaseTimestamp();
		final Type_type temp = verdictValue.getExpressionReturntype(ct, Expected_Value_type.EXPECTED_TEMPLATE);
		if(Type_type.TYPE_VERDICT != temp) {
			return;
		}

		final LogArguments verdictReason = s.getVerdictReason();
		if (Value_type.VERDICT_VALUE.equals(verdictValue.getValuetype())
				&& !Verdict_type.PASS.equals(((Verdict_Value) verdictValue).getValue()) && verdictReason == null) {
			final String msg = MessageFormat.format(WITHOUT_REASON, ((Verdict_Value) verdictValue).getValue());
			problems.report(s.getLocation(), msg);
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Setverdict_Statement.class);
		return ret;
	}
}
