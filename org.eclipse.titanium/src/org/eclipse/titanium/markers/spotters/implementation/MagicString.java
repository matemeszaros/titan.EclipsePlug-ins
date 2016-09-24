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
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ControlPart;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class MagicString extends BaseModuleCodeSmellSpotter {
	private static final String MAGIC_STRING = "The magic string `{0}'' should be extracted into a local constant";

	public MagicString() {
		super(CodeSmellType.MAGIC_STRINGS);
	}

	@Override
	public void process(final IVisitableNode n, final Problems problems) {
		n.accept(new ASTVisitor() {
			@Override
			public int visit(final IVisitableNode node) {
				if (node instanceof Bitstring_Value || node instanceof Charstring_Value || node instanceof Hexstring_Value
						|| node instanceof Octetstring_Value) {
					final String msg = MessageFormat.format(MAGIC_STRING, ((Value) node).createStringRepresentation());
					problems.report(((Value) node).getLocation(), msg);
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
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(4);
		ret.add(Def_Function.class);
		ret.add(Def_Testcase.class);
		ret.add(Def_Altstep.class);
		ret.add(ControlPart.class);
		return ret;
	}
	
}
