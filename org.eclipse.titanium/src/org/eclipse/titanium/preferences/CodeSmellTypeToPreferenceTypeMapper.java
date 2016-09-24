/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.titanium.markers.types.CodeSmellType;

public final class CodeSmellTypeToPreferenceTypeMapper {

	private static final Map<CodeSmellType, ProblemTypePreference> MAPPING;
	static {
		final Map<CodeSmellType, ProblemTypePreference> m = new EnumMap<CodeSmellType, ProblemTypePreference>(CodeSmellType.class);
		for (final ProblemTypePreference p : ProblemTypePreference.values()) {
			for (final CodeSmellType s : p.getRelatedProblems()) {
				m.put(s, p);
			}
		}
		MAPPING = Collections.unmodifiableMap(m);
	}

	public static ProblemTypePreference getPreferenceType(final CodeSmellType problemType) {
		return MAPPING.get(problemType);
	}

	/** Private constructor */
	private CodeSmellTypeToPreferenceTypeMapper() {
		//Do nothing
	}
}
