package org.eclipse.titanium.preferences;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.titanium.markers.types.CodeSmellType;

public class CodeSmellTypeToPreferenceTypeMapper {

	private static final Map<CodeSmellType, ProblemTypePreference> MAPPING;
	static {
		Map<CodeSmellType, ProblemTypePreference> m = new EnumMap<CodeSmellType, ProblemTypePreference>(CodeSmellType.class);
		for (ProblemTypePreference p : ProblemTypePreference.values()) {
			for (CodeSmellType s : p.getRelatedProblems()) {
				m.put(s, p);
			}
		}
		MAPPING = Collections.unmodifiableMap(m);
	}

	public static ProblemTypePreference getPreferenceType(CodeSmellType problemType) {
		return MAPPING.get(problemType);
	}

	/** Private constructor */
	private CodeSmellTypeToPreferenceTypeMapper() {
		//Do nothing
	}
}
