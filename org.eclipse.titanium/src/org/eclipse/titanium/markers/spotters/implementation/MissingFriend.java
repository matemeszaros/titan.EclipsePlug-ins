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
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FriendModule;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class MissingFriend extends BaseModuleCodeSmellSpotter {
	public static final String MISSING_MODULE = "There is no module with name `{0}''";

	public MissingFriend() {
		super(CodeSmellType.MISSING_FRIEND);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof FriendModule) {
			FriendModule s = (FriendModule) node;
			Identifier identifier = s.getIdentifier();
			ProjectSourceParser parser = GlobalParser.getProjectSourceParser(s.getProject());
			if (parser != null && identifier != null) {
				Module referredModule = parser.getModuleByName(identifier.getName());
				if (referredModule == null) {
					String msg = MessageFormat.format(MISSING_MODULE, identifier.getDisplayName());
					problems.report(identifier.getLocation(), msg);
				}
			}
		}

	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(FriendModule.class);
		return ret;
	}
}
