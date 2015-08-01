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

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.parsers.ttcn3parser.ITTCN3ReparseBase_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Reparser4;

/**
 * Class to represent pr_GroupDef nodes.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Group_V4 extends Group {

	public Group_V4(Identifier identifier) {
		super(identifier);
	}

	@Override
	protected int reparseIdentifier(TTCN3ReparseUpdater aReparser) {
		return ((TTCN3ReparseUpdater_V4)aReparser).parse(new ITTCN3ReparseBase_V4() {
			@Override
			public void reparse(final TTCN3Reparser4 parser) {
				identifier = parser.pr_reparse_Identifier().identifier;
			}
		});
	}

	@Override
	protected int reparseOptionalWithStatement(TTCN3ReparseUpdater aReparser) {
		return ((TTCN3ReparseUpdater_V4)aReparser).parse(new ITTCN3ReparseBase_V4() {
			@Override
			public void reparse(final TTCN3Reparser4 parser) {
				MultipleWithAttributes attributes = parser.pr_reparser_optionalWithStatement().attributes;
				parser.pr_EndOfFile();
				if ( parser.isErrorListEmpty() ) {
					withAttributesPath.setWithAttributes(attributes);
					if (attributes != null) {
						getLocation().setEndOffset(attributes.getLocation().getEndOffset());
					}
				}
			}
		});
	}

	@Override
	protected int reparseModuleDefinitionsList(TTCN3ReparseUpdater aReparser) {
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

				TTCN3Module temp = getModule();

				parser.setModule(temp);
				parser.pr_reparse_ModuleDefinitionsList(null, allDefinitions, localDefinitions, localGroups, allImports,
						localImports, allFriends, localFriends, null);

				if ( parser.isErrorListEmpty() ) {
					temp.addDefinitions(allDefinitions);
					for (ImportModule impmod : allImports) {
						temp.addImportedModule(impmod);
					}

					for (FriendModule friend : allFriends) {
						temp.addFriendModule(friend);
					}

					addDefinitions(localDefinitions);

					for (ImportModule impmod : localImports) {
						addImportedModule(impmod);
					}

					for (Group group : localGroups) {
						addGroup(group);
					}

					for (FriendModule friend : localFriends) {
						addFriendModule(friend);
					}
				}
			}
		});
	}
}
