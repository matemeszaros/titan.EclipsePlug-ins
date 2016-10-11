/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.text.templates.Template;

/**
 * Helper class, used to ease working with TemplateProposals.
 * 
 * @author Kristof Szabados
 * */
public final class SkeletonTemplateProposal {
	public static final String SKELETON_IMAGE = "skeleton.gif";

	private final String prefix;
	private final Template proposal;

	public SkeletonTemplateProposal(final String prefix, final Template proposal) {
		this.prefix = prefix;
		this.proposal = proposal;
	}

	public String getPrefix() {
		return prefix;
	}

	public Template getProposal() {
		return proposal;
	}
}
