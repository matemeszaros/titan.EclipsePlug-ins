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

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ExternalConst;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.VisibilityModifier;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class NonprivatePrivate extends BaseModuleCodeSmellSpotter {
	private static final String SHOULD_BE_PRIVATE = "{0} is referenced only locally, it should be private";

	public NonprivatePrivate() {
		super(CodeSmellType.NONPRIVATE_PRIVATE);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (!(node instanceof Definition)) {
			return;
		}

		final Definition d = (Definition) node;
		if (d.isUsed() && d.referingHere.size() == 1 &&
				!VisibilityModifier.Private.equals(d.getVisibilityModifier()) && !d.isLocal()) {
			final String moduleName = d.getMyScope().getModuleScope().getName();
			if (d.referingHere.get(0).equals(moduleName)) {
				final String msg = MessageFormat.format(SHOULD_BE_PRIVATE, d.getIdentifier().getDisplayName());
				problems.report(d.getIdentifier().getLocation(), msg);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(8);
		ret.add(Def_Altstep.class);
		ret.add(Def_Const.class);
		ret.add(Def_ExternalConst.class);
		ret.add(Def_Extfunction.class);
		ret.add(Def_Function.class);
		ret.add(Def_ModulePar.class);
		ret.add(Def_Template.class);
		ret.add(Def_Type.class);
		return ret;
	}
}
