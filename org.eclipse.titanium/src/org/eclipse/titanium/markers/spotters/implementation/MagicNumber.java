/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ControlPart;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class MagicNumber extends BaseModuleCodeSmellSpotter {
	private static final String MAGIC_NUMBER = "The magic number `{0}'' should be extracted into a local constant";

	public MagicNumber() {
		super(CodeSmellType.MAGIC_NUMBERS);
	}

	@Override
	public void process(IVisitableNode n, final Problems problems) {
		n.accept(new ASTVisitor() {
			@Override
			public int visit(IVisitableNode node) {
				if (node instanceof Integer_Value) {
					Integer_Value value = (Integer_Value) node;
					BigInteger bigNumber = value.getValueValue();
					if (bigNumber.compareTo(BigInteger.valueOf(-5)) < 0 || bigNumber.compareTo(BigInteger.valueOf(5)) > 0) {
						String msg = MessageFormat.format(MAGIC_NUMBER, ((Integer_Value) node).createStringRepresentation());
						problems.report(((Integer_Value) node).getLocation(), msg);
					}
				} else if (node instanceof Def_Const) {
					// should not reach there
					return V_SKIP;
				}
				return V_CONTINUE;
			}
		});
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(4);
		ret.add(Def_Function.class);
		ret.add(Def_Testcase.class);
		ret.add(Def_Altstep.class);
		ret.add(ControlPart.class);
		return ret;
	}
	
}
