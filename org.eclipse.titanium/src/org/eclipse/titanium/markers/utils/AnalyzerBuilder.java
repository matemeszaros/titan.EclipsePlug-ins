/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.utils;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.spotters.BaseProjectCodeSmellSpotter;
import org.eclipse.titanium.markers.spotters.implementation.StaticData;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.preferences.ProblemTypePreference;

/**
 * Builder class for {@link Analyzer} instances.
 * <p>
 * This class is used to configure and construct an immutable
 * <code>Analyzer</code>. Instances of this class are acquired via
 * {@link Analyzer#builder()}.
 * 
 * @author poroszd
 * 
 */
public class AnalyzerBuilder {

	private final Map<Class<? extends IVisitableNode>, Set<BaseModuleCodeSmellSpotter>> actions;
	private final Set<BaseProjectCodeSmellSpotter> projectActions;
	private final Map<CodeSmellType, BaseModuleCodeSmellSpotter[]> spotters;
	private final Map<CodeSmellType, BaseProjectCodeSmellSpotter[]> projectSpotters;
	private Boolean exhausted;

	AnalyzerBuilder() {
		actions = new HashMap<Class<? extends IVisitableNode>, Set<BaseModuleCodeSmellSpotter>>();
		projectActions = new HashSet<BaseProjectCodeSmellSpotter>();
		spotters = StaticData.newSpotters();
		projectSpotters = StaticData.newProjectSpotters();
		exhausted = false;
	}

	/**
	 * Create the <code>Analyzer</code>.
	 * <p>
	 * Constructing the product consumes <code>AnalyzerBuilder</code>
	 * instance, and no further methods are permitted to be called. Calling
	 * any modifier method of an exhausted <code>AnalyzerBuilder</code> will
	 * throw an <code>IllegalStateException</code>.
	 * 
	 * @return
	 */
	public Analyzer build() {
		if (exhausted) {
			throw new IllegalStateException("One must not use the builder after build() method is called");
		}

		exhausted = true;
		return new Analyzer(actions, projectActions);
	}

	public AnalyzerBuilder adaptPreferences() {
		if (exhausted) {
			throw new IllegalStateException("One must not use the builder after build() method is called");
		}

		actions.clear();
		projectActions.clear();
		IPreferencesService prefs = Platform.getPreferencesService();

		for (ProblemTypePreference prefType : ProblemTypePreference.values()) {
			String prefName = prefType.getPreferenceName();
			String warnLevel = prefs.getString(Activator.PLUGIN_ID, prefName, GeneralConstants.IGNORE, null);
			if (!GeneralConstants.IGNORE.equals(warnLevel)) {
				addPreferenceProblem(prefType);
			}
		}
		return this;
	}

	/**
	 * Add the set of code smells to the under-construction
	 * <code>Analyzer</code>, that is associated with this preference
	 * problem type.
	 * 
	 * @param preferenceProblem
	 *            the analyzer will use its code smells
	 * @return this for method chaining
	 */
	public AnalyzerBuilder addPreferenceProblem(ProblemTypePreference preferenceProblem) {
		if (exhausted) {
			throw new IllegalStateException("One must not use the builder after build() method is called");
		}

		for (CodeSmellType problemType : preferenceProblem.getRelatedProblems()) {
			addProblem(problemType);
		}
		return this;
	}

	/**
	 * @param problemType
	 *            the analyzer will use this code smell
	 * @return this for method chaining
	 */
	public AnalyzerBuilder addProblem(CodeSmellType problemType) {
		if (exhausted) {
			throw new IllegalStateException("One must not use the builder after build() method is called");
		}

		if (spotters.get(problemType) != null) {
			for (BaseModuleCodeSmellSpotter spotter : spotters.get(problemType)) {
				Queue<Class<? extends IVisitableNode>> subtypes = new ArrayDeque<Class<? extends IVisitableNode>>();
				subtypes.addAll(spotter.getStartNode());
				while (!subtypes.isEmpty()) {
					Class<? extends IVisitableNode> sub = subtypes.poll();
					if (StaticData.TYPE_HIERARCHY.get(sub) != null) {
						Collections.addAll(subtypes, StaticData.TYPE_HIERARCHY.get(sub));
					}
					if (actions.get(sub) == null) {
						actions.put(sub, new HashSet<BaseModuleCodeSmellSpotter>());
					}
					actions.get(sub).add(spotter);
				}
			}
		} else if (projectSpotters.get(problemType) != null) {
			for (BaseProjectCodeSmellSpotter spotter : projectSpotters.get(problemType)) {
				projectActions.add(spotter);
			}
		}
		return this;
	}
}
