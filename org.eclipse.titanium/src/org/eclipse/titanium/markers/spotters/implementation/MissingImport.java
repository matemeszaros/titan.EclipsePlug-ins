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
import org.eclipse.titan.designer.AST.TTCN3.definitions.ImportModule;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class MissingImport extends BaseModuleCodeSmellSpotter {
	private static final String MISSING_MODULE = "There is no module with name `{0}''";

	public MissingImport() {
		super(CodeSmellType.MISSING_IMPORT);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof ImportModule) {
			ImportModule s = (ImportModule) node;
			if (s.getReferredModule() == null) {
				String msg = MessageFormat.format(MISSING_MODULE, s.getIdentifier().getDisplayName());
				problems.report(s.getIdentifier().getLocation(), msg);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(ImportModule.class);
		return ret;
	}
}
