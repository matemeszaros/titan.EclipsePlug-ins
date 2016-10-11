/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ImportModule;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.markers.spotters.BaseProjectCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class CircularImportation extends BaseProjectCodeSmellSpotter {
	public CircularImportation() {
		super(CodeSmellType.CIRCULAR_IMPORTATION);
	}

	@Override
	public void process(final IProject project, final Problems problems) {
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		final Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
		final List<Module> modules = new ArrayList<Module>();
		for (final String moduleName : new TreeSet<String>(knownModuleNames)) {
			modules.add(projectSourceParser.getModuleByName(moduleName));
		}

		Collections.sort(modules, new Comparator<Module>() {
			@Override
			public int compare(final Module o1, final Module o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		final CycleCheck check = new CycleCheck(modules);
		for (final List<Module> c : check.cycles) {
			final StringBuilder sb = new StringBuilder("Importation cycle detected: ");
			for (final Module m : c) {
				sb.append(m.getName());
				sb.append(" -> ");
			}
			sb.append(c.get(0).getName());
			final String errMsg = sb.toString();

			// Try to find the locations to report, i.e. the import statements.
			// We handle only the case of TTCN3Module-s.
			final Iterator<Module> it = c.iterator();
			Module imported = it.next();
			Module importer;
			while (it.hasNext()) {
				importer = it.next();
				if (importer instanceof TTCN3Module) {
					for (final ImportModule im : ((TTCN3Module) importer).getImports()) {
						if (im.getName().equals(imported.getName())) {
							problems.report(im.getLocation(), errMsg);
						}
					}
				}
				imported = importer;
			}
			importer = c.get(0);
			if (importer instanceof TTCN3Module) {
				for (final ImportModule im : ((TTCN3Module) importer).getImports()) {
					if (im.getName().equals(imported.getName())) {
						problems.report(im.getLocation(), errMsg);
					}
				}
			}

		}
	}
}

/**
 * Utility class to find cycles in the module graph
 * <p>
 * This class is used here internally to find cycles of the module importation
 * graph. Note that current implementation does not find all cycles, but it does
 * find at least one cycle if the graph contains any. It uses a simple
 * depth-first search to traverse the nodes and detect some of the cycles.
 * </p>
 */
class CycleCheck {
	private final Map<Module, Node> map;
	List<List<Module>> cycles;

	/**
	 * Constructs and initializes a <code>CycleCheck</code> object
	 * <p>
	 * After construction, the <code>cycles</code> field is available,
	 * initialized with (some) cycles in the graph.
	 * </p>
	 * 
	 * @param modules
	 *            The modules of a project.
	 */
	public CycleCheck(final List<Module> modules) {
		map = new HashMap<Module, Node>();
		for (final Module module : modules) {
			map.put(module, new Node(module));
		}
		cycles = new ArrayList<List<Module>>();

		for (final Module module : modules) {
			final Node node = map.get(module);
			if (node.state == State.WHITE) {
				dfs(node);
			}
		}
	}

	private void dfs(final Node n) {
		n.state = State.GRAY;
		for (final Module m : n.module.getImportedModules()) {
			final Node child = map.get(m);
			if (child != null) {
				switch (child.state) {
				case WHITE:
					child.parent = n;
					dfs(child);
					break;
				case GRAY:
					newCycle(child, n);
					break;
				case BLACK:
					break;
				}
			}
		}
		n.state = State.BLACK;
	}

	private void newCycle(final Node knot, final Node n) {
		final List<Module> cycle = new ArrayList<Module>();
		if (knot.module != n.module) {
			Node p = n;
			do {
				cycle.add(p.module);
				p = p.parent;
			} while (p != knot);
		}
		cycle.add(knot.module);
		cycles.add(cycle);
	}

	/**
	 * This class provides data for the modules during traversal.
	 * 
	 * @author poroszd
	 */
	static class Node {
		/** The module represented by this node */
		Module module;
		/** The parent during traversal */
		Node parent;
		/** The actual state during traversal */
		State state;

		public Node(final Module module) {
			this.module = module;
			parent = null;
			state = State.WHITE;
		}
	}

	/** The state of a node during traversal */
	enum State {
		/** Not reached */
		WHITE,
		/**
		 * Under process (reached, but the dfs started from here is not
		 * finished)
		 */
		GRAY,
		/** Processed */
		BLACK
	}
}
