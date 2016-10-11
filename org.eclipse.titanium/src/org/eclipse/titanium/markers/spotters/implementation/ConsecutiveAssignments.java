/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.preferences.PreferenceConstants;

/**
 * This class marks the following code smell:
 * Consecutive assignment statements to the fields of the same object
 * in the same statement block.
 * Minimum consecutive assignments limit can be set in Preferences.
 * 
 * @author Viktor Varga
 */
public class ConsecutiveAssignments extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Consecutive assignments should be merged into one assignment.";

	/* the minimum number of consecutive assignments to mark as a smell */
	private final int minCountToMark;
	private static final int DEFAULT_MIN_COUNT_TO_MARK = 4;
	
	public ConsecutiveAssignments() {
		super(CodeSmellType.CONSECUTIVE_ASSIGNMENTS);
		minCountToMark = Platform.getPreferencesService().getInt(Activator.PLUGIN_ID, 
				PreferenceConstants.TOO_MANY_CONSECUTIVE_ASSIGNMENTS_SIZE, DEFAULT_MIN_COUNT_TO_MARK, null);
	}

	@Override
	protected void process(final IVisitableNode node, final Problems problems) {
		if (!(node instanceof StatementBlock)) {
			return;
		}
		final CompilationTimeStamp timestamp = CompilationTimeStamp.getBaseTimestamp();
		int count = 0;
		boolean limitReached = false;
		Location smellLoc = null;
		Assignment_Statement lastAs = null;
		Assignment toMatch = null;
		final StatementBlock sb = (StatementBlock)node;
		//iterate statements in block
		for (int i=0;i<sb.getSize();i++) {
			final Statement s = sb.getStatementByIndex(i);
			if (!(s instanceof Assignment_Statement)) {
				if (limitReached) {
					smellLoc.setEndOffset(lastAs.getLocation().getEndOffset());
					problems.report(smellLoc, ERROR_MESSAGE);
					limitReached = false;
				}
				count = 0;
				toMatch = null;
				continue;
			}
			final Assignment_Statement as = (Assignment_Statement)s;
			final Reference ref = as.getReference();
			final Assignment a = ref.getRefdAssignment(timestamp, false);
			if (a == null) {
				if (limitReached) {
					smellLoc.setEndOffset(lastAs.getLocation().getEndOffset());
					problems.report(smellLoc, ERROR_MESSAGE);
					limitReached = false;
				}
				count = 0;
				toMatch = null;
				continue;
			}
			//consecutive assignments: consecutive Assignment_Statements have the same definition
			if (toMatch == null) {
				toMatch = a;
			} else if (toMatch != a) {
				if (limitReached) {
					smellLoc.setEndOffset(lastAs.getLocation().getEndOffset());
					problems.report(smellLoc, ERROR_MESSAGE);
					limitReached = false;
				}
				count = 0;
				toMatch = a;
			}
			if (count == 0) {
				smellLoc = new Location(as.getLocation());
			}
			lastAs = as;
			count++;
			if (count >= minCountToMark) {
				limitReached = true;
			}
		}
		if (limitReached) {
			smellLoc.setEndOffset(lastAs.getLocation().getEndOffset());
			problems.report(smellLoc, ERROR_MESSAGE);
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(StatementBlock.class);
		return ret;
	}

}
