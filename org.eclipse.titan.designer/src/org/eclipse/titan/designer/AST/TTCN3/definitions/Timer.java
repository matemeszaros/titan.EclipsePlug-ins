/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.util.List;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.graphics.ImageCache;

/**
 * The Timer class is a helper class for TTCN3 timer definitions.
 * <p>
 * Timers in TTCN3 just look like types, but they are not ones. This was just
 * created to ease working with timers.
 * 
 * @author Kristof Szabados
 * */
public final class Timer {
	private static final String START_OPERATION = "start";
	private static final String STOP_OPERATION = "stop";
	private static final String READ_OPERATION = "read";
	private static final String RUNNING_OPERATION = "running";
	private static final String TIMEOUT_OPERATION = "timeout";
	private static final String START_TEMPLATE = "start( ${timer} )";
	private static final String START_TEMPLATE_NAME = "start( timer )";

	private Timer() {

	}

	/**
	 * Adds the completion possibilities of timers to the proposal
	 * collector..
	 * <p>
	 * handles the following proposals:
	 * <ul>
	 * <li>start, start(timer)
	 * <li>stop
	 * <li>read
	 * <li>running
	 * <li>timeout
	 * </ul>
	 * 
	 * @param propCollector
	 *                the proposal collector.
	 * @param i
	 *                the index of a part of the full reference, for which
	 *                we wish to find completions.
	 * */
	public static void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() != i + 1 || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		propCollector.addProposal(START_OPERATION, START_OPERATION, ImageCache.getImage("timer.gif"));
		propCollector.addTemplateProposal(START_OPERATION, new Template(START_TEMPLATE_NAME, "", propCollector.getContextIdentifier(),
				START_TEMPLATE, false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addProposal(STOP_OPERATION, STOP_OPERATION, ImageCache.getImage("timer.gif"));
		propCollector.addProposal(READ_OPERATION, READ_OPERATION, ImageCache.getImage("timer.gif"));
		propCollector.addProposal(RUNNING_OPERATION, RUNNING_OPERATION, ImageCache.getImage("timer.gif"));
		propCollector.addProposal(TIMEOUT_OPERATION, TIMEOUT_OPERATION, ImageCache.getImage("timer.gif"));
	}

	public static void addAnyorAllProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (i != 0 || subrefs.isEmpty() || Subreference_type.arraySubReference.equals(subrefs.get(0).getReferenceType())) {
			return;
		}

		String fakeModuleName = propCollector.getReference().getModuleIdentifier().getName();

		if ("any timer".equals(fakeModuleName)) {
			propCollector.addProposal(RUNNING_OPERATION, RUNNING_OPERATION, ImageCache.getImage("timer.gif"));
			propCollector.addProposal(TIMEOUT_OPERATION, TIMEOUT_OPERATION, ImageCache.getImage("timer.gif"));
		} else if ("all timer".equals(fakeModuleName)) {
			propCollector.addProposal(STOP_OPERATION, STOP_OPERATION, ImageCache.getImage("timer.gif"));
		}
	}
}
