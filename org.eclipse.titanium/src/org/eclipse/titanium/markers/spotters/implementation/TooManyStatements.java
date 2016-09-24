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

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.preferences.PreferenceConstants;

public class TooManyStatements extends BaseModuleCodeSmellSpotter {
	private final int reportTooManyStatementsSize;
	private static final String TOOMANYSTATEMENTS = "More than {0} statements in a single statementblock: {1}";

	public TooManyStatements() {
		super(CodeSmellType.TOO_MANY_STATEMENTS);
		reportTooManyStatementsSize = Platform.getPreferencesService().getInt(Activator.PLUGIN_ID,
				PreferenceConstants.TOO_MANY_STATEMENTS_SIZE, 150, null);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof StatementBlock) {
			final StatementBlock s = (StatementBlock) node;
			if (s.getSize() > reportTooManyStatementsSize) {
				final String msg = MessageFormat.format(TOOMANYSTATEMENTS, reportTooManyStatementsSize, s.getSize());
				problems.report(s.getLocation(), msg);
			}
		}

	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(StatementBlock.class);
		return ret;
	}
}
