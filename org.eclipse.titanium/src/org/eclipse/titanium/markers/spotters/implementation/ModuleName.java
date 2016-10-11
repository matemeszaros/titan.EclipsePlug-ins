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
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Group;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class ModuleName {
	private ModuleName() {
		throw new AssertionError("Noninstantiable");
	}

	public abstract static class Base extends BaseModuleCodeSmellSpotter {
		private static final String REPORT = "The name {1} of the {0} contains the module name {2} it is located in";

		public Base() {
			super(CodeSmellType.MODULENAME_IN_DEFINITION);
		}

		@Override
		public abstract List<Class<? extends IVisitableNode>> getStartNode();

		protected void check(final Identifier identifier, final Identifier moduleID, final String description, final Problems problems) {
			final String displayName = identifier.getDisplayName();
			if (displayName.contains(moduleID.getDisplayName())) {
				final String msg = MessageFormat.format(REPORT, description, displayName, moduleID.getDisplayName());
				problems.report(identifier.getLocation(), msg);
			}
		}
	}

	public static class InDef extends Base {

		@Override
		public void process(final IVisitableNode node, final Problems problems) {
			if ((node instanceof Definition) && !(node instanceof FormalParameter)) {
				final Definition s = (Definition) node;
				final Identifier identifier = s.getIdentifier();
				final Identifier moduleID = s.getMyScope().getModuleScope().getIdentifier();
				check(identifier, moduleID, s.getDescription(), problems);
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(Definition.class);
			return ret;
		}
	}

	public static class InGroup extends Base {

		@Override
		public void process(final IVisitableNode node, final Problems problems) {
			if (node instanceof Group) {
				final Group s = (Group) node;
				final Identifier identifier = s.getIdentifier();
				final Identifier moduleID = s.getMyScope().getModuleScope().getIdentifier();
				check(identifier, moduleID, "group", problems);
			}
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(Group.class);
			return ret;
		}
	}
}
