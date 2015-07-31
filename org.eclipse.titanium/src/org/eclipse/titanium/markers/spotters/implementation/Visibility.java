/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Group;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class Visibility extends BaseModuleCodeSmellSpotter {
	private static final Pattern VISIBILITY_PATTERN = Pattern.compile(".*(?:public|private|friend).*");
	private static final String REPORT = "The name {1} of the {0} contains visibility attributes";

	public Visibility() {
		super(CodeSmellType.VISIBILITY_IN_DEFINITION);
	}
	
	@Override
	protected void process(IVisitableNode node, Problems problems) {
		if (node instanceof FormalParameter) {
			return;
		} else if (node instanceof Definition) {
			Definition s = (Definition) node;
			Identifier identifier = s.getIdentifier();
			check(identifier, s.getDescription(), problems);
		} else if (node instanceof Group) {
			Group s = (Group) node;
			Identifier identifier = s.getIdentifier();
			check(identifier, "group", problems);
		} else {
			return;
		}
	}
	
	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(2);
		ret.add(Definition.class);
		ret.add(Group.class);
		return ret;
	}
	

	protected void check(final Identifier identifier, final String description, Problems problems) {
		String displayName = identifier.getDisplayName();
		if (VISIBILITY_PATTERN.matcher(displayName).matches()) {
			String msg = MessageFormat.format(REPORT, description, displayName);
			problems.report(identifier.getLocation(), msg);
		}
	}
}
