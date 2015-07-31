/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Keywords;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public abstract class TTCN3Scope extends Scope {

	@Override
	public abstract Assignment getAssBySRef(final CompilationTimeStamp timestamp, Reference reference);

	@Override
	public void addKeywordProposal(final ProposalCollector propCollector) {
		propCollector.addProposal(TTCN3Keywords.GENERALLY_USABLE, null, TTCN3Keywords.KEYWORD);
		propCollector.addProposal(TTCN3Keywords.MACROS, null, TTCN3Keywords.MACRO);
	}
}
