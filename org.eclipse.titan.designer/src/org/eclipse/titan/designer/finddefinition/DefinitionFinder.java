/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.finddefinition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * @author Szabolcs Beres
 * */
public class DefinitionFinder {

	private final StoredDefinitionFilter filter;

	public DefinitionFinder(final StoredDefinitionFilter filter) {
		this.filter = filter;
	}

	public List<Object> findDefinitions() {
		if (filter.getWorkspaceScope()) {
			return getDefinitionsOfWorkspace();
		} else {
			return getDefinitionsOfProject(filter.getCurrentProject());
		}
	}

	private List<Object> getDefinitionsOfWorkspace() {
		final List<Object> result = new ArrayList<Object>();
		for (IProject project : GlobalParser.getAllAnalyzedProjects()) {
			result.addAll(getDefinitionsOfProject(project));
		}
		return result;
	}

	private List<Object> getDefinitionsOfProject(final IProject project) {
		final List<Object> result = new ArrayList<Object>();
		ProjectSourceParser parser = GlobalParser.getProjectSourceParser(project);
		if (filter.showOnlyModules()) {
			result.addAll(parser.getModules());
		} else {
			for (Module module : parser.getModules()) {
				if (filter.filter(module)) {
					result.add(module);
				}
				for (Assignment ass : module.getAssignments()) {
					if (filter.filter(ass)) {
						result.add(ass);
					}
				}
			}
		}
		return result;
	}
}
