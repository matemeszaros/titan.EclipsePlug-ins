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
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class ReadOnlyLocal {
	private ReadOnlyLocal() {
		throw new AssertionError("Noninstantiable");
	}

	public static class Var extends BaseModuleCodeSmellSpotter {
		public static final String READONLY = "The {0} seems to be never written, maybe it could be a constant";

		public Var() {
			super(CodeSmellType.READONLY_LOC_VARIABLE);
		}

		@Override
		public void process(final IVisitableNode node, final Problems problems) {
			if (node instanceof Def_Var) {
				Def_Var s = (Def_Var) node;
				if (!(s.getMyScope() instanceof ComponentTypeBody) && !s.getWritten()) {
					Value initialValue = s.getInitialValue();
					CompilationTimeStamp ct = CompilationTimeStamp.getBaseTimestamp();
					if (initialValue != null && !initialValue.getIsErroneous(ct) && !initialValue.isUnfoldable(ct)) {
						String msg = MessageFormat.format(READONLY, s.getDescription());
						problems.report(s.getIdentifier().getLocation(), msg);
					}
				}
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(Def_Var.class);
			return ret;
		}
	}

	public static class VarTemplate extends BaseModuleCodeSmellSpotter {
		public static final String READONLY = "The {0} seems to be never written, maybe it could be a template";

		public VarTemplate() {
			super(CodeSmellType.READONLY_LOC_VARIABLE);
		}

		@Override
		public void process(final IVisitableNode node, final Problems problems) {
			if (node instanceof Def_Var_Template) {
				Def_Var_Template s = (Def_Var_Template) node;
				if (!s.getWritten()) {
					String msg = MessageFormat.format(READONLY, s.getDescription());
					problems.report(s.getIdentifier().getLocation(), msg);
				}
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(Def_Var_Template.class);
			return ret;
		}
	}
}