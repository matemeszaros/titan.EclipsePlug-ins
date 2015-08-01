/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.parsers.ttcn3parser.ITTCN3ReparseBase_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Reparser4;

/**
 * Represents a Module.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class TTCN3Module_V4 extends TTCN3Module {

	public TTCN3Module_V4(Identifier identifier, IProject project) {
		super(identifier, project);
		definitions = new Definitions_V4();
		definitions.setParentScope(this);
		definitions.setFullNameParent(this);
	}

	@Override
	protected int reparseAfterModule(final TTCN3ReparseUpdater aReparser) {
		return ((TTCN3ReparseUpdater_V4)aReparser).parse(new ITTCN3ReparseBase_V4() {
			@Override
			public void reparse(final TTCN3Reparser4 parser) {
				aReparser.fullAnalysysNeeded = true;
				MultipleWithAttributes attributes = parser.pr_reparser_optionalWithStatement().attributes;
				parser.pr_EndOfFile();
				if ( parser.isErrorListEmpty() ) {
					withAttributesPath = new WithAttributesPath();
					withAttributesPath.setWithAttributes(attributes);
					if (attributes != null) {
						getLocation().setEndOffset(attributes.getLocation().getEndOffset());
					}
				}
			}
		});
	}

	@Override
	protected int reparseInsideAttributelist(TTCN3ReparseUpdater aReparser) {
		return ((TTCN3ReparseUpdater_V4)aReparser).parse(new ITTCN3ReparseBase_V4() {
			@Override
			public void reparse(final TTCN3Reparser4 parser) {
				MultipleWithAttributes attributes = parser.pr_reparser_optionalWithStatement().attributes;
				parser.pr_EndOfFile();
				if ( parser.isErrorListEmpty() ) {
					withAttributesPath.setWithAttributes(attributes);
					getLocation().setEndOffset(attributes.getLocation().getEndOffset());
				}
			}
		});
	}
}
