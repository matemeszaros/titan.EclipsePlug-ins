/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;

/**
 * This class is responsible to hold information about the module importation
 * structure.
 * <p>
 * It stores the known modules, and for each of them, all the modules they
 * import + a list of missing modules, if they are imported, but not known by
 * the project itself.
 * 
 * @author Kristof Szabados
 * */
public final class ProjectStructureDataCollector {
	public Map<String, Identifier> knownModules = new HashMap<String, Identifier>();
	public Map<String, Identifier> missingModules = new HashMap<String, Identifier>();
	public Map<String, List<String>> importations = new HashMap<String, List<String>>();

	public void addKnownModule(final Identifier name) {
		knownModules.put(name.getName(), name);

		if (importations.containsKey(name.getName())) {
			importations.remove(name.getName());
		}
	}

	public void removeKnownModule(final String name) {
		knownModules.remove(name);

		if (importations.containsKey(name)) {
			importations.remove(name);
		}
	}

	public void addImportation(final Identifier from, final Identifier where) {
		List<String> importedModules;

		if (importations.containsKey(from.getName())) {
			importedModules = importations.get(from.getName());
			importedModules.add(where.getName());
		} else {
			importedModules = new ArrayList<String>();
			importedModules.add(where.getName());
			importations.put(from.getName(), importedModules);
		}
	}

	public void evaulateMissingModules() {
		missingModules.clear();

		for (List<String> importation : importations.values()) {
			for (String name : importation) {
				if (!knownModules.containsKey(name)) {
					missingModules.put(name, new Identifier(Identifier_type.ID_NAME, name));
				}
			}
		}
	}
}
