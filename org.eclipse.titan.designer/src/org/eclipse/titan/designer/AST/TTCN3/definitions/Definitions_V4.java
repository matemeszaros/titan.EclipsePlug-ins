/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.parsers.ttcn3parser.ITTCN3ReparseBase_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Reparser4;

/**
 * The Definitions class represents the scope of module level definitions inside
 * Modules.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Definitions_V4 extends Definitions {

	@Override
	protected int reparse( final TTCN3ReparseUpdater aReparser, final boolean aTempIsControlPossible ) {
		return ((TTCN3ReparseUpdater_V4)aReparser).parse(new ITTCN3ReparseBase_V4() {
			@Override
			public void reparse(final TTCN3Reparser4 parser) {
				List<Definition> allDefinitions = new ArrayList<Definition>();
				List<Definition> localDefinitions = new ArrayList<Definition>();
				List<Group> localGroups = new ArrayList<Group>();
				List<ImportModule> allImports = new ArrayList<ImportModule>();
				List<ImportModule> localImports = new ArrayList<ImportModule>();
				List<FriendModule> allFriends = new ArrayList<FriendModule>();
				List<FriendModule> localFriends = new ArrayList<FriendModule>();
				List<ControlPart> controlParts = null;
				if (aTempIsControlPossible) {
					controlParts = new ArrayList<ControlPart>();
				}

				TTCN3Module module = (TTCN3Module) parentScope;
				parser.setModule((TTCN3Module) parentScope);
				parser.pr_reparse_ModuleDefinitionsList(null, allDefinitions, localDefinitions, localGroups, allImports,
						localImports, allFriends, localFriends, controlParts);

				if ( parser.isErrorListEmpty() ) {
					if (!allDefinitions.isEmpty()) {
						aReparser.fullAnalysysNeeded = true;
					}
					addDefinitions(allDefinitions);
					if (doubleDefinitions != null) {
						doubleDefinitions.clear();
					}
					lastUniquenessCheckTimeStamp = null;

					for (ImportModule impmod : allImports) {
						aReparser.fullAnalysysNeeded = true;
						module.addImportedModule(impmod);
					}

					for (Group group : localGroups) {
						aReparser.fullAnalysysNeeded = true;
						addGroup(group);
					}

					for (FriendModule friend : allFriends) {
						aReparser.fullAnalysysNeeded = true;
						module.addFriendModule(friend);
					}
					if (controlParts != null && controlParts.size() == 1) {
						aReparser.fullAnalysysNeeded = true;
						((TTCN3Module) parentScope).addControlpart(controlParts.get(0));
					}
				}
			}
		});
	}
}
