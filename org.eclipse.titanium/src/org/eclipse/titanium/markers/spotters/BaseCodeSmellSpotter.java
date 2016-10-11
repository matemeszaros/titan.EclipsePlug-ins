/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.handler.Marker;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.preferences.ProblemTypePreference;
import org.eclipse.titanium.preferences.CodeSmellTypeToPreferenceTypeMapper;

/**
 * Abstract base class of code smell spotters.
 * <p>
 * Code smell spotters are used to analyze a ttcn3 module and project. They
 * extend either {@link BaseModuleCodeSmellSpotter} or
 * {@link BaseProjectCodeSmellSpotter}, both of which are descendants of this
 * class.
 * <p>
 * Their general purpose is to analyze a module or a project searching for
 * patterns of a specific problem, and when found, report it, that is, create a
 * {@link Marker}, which later can be used for example to show up in eclipse.
 * <p>
 * This class extracts what is common in every spotter:
 * <ul>
 * <li>can report a single code smell type</li>
 * <li>during the analysis collects multiple reports</li>
 * <li>can be initialized with some spotter-specific data</li>
 * </ul>
 * Code smell spotters should be immutable; any required user preference setting
 * should be read at construction time.
 * 
 * @author poroszd
 * 
 */
abstract class BaseCodeSmellSpotter {
	/** The code smell that this spotter can spot */
	private final CodeSmellType type;
	/** The severity of the code smell to spot */
	private final int severity;

	protected BaseCodeSmellSpotter(final CodeSmellType type) {
		this.type = type;

		final IPreferencesService prefs = Platform.getPreferencesService();
		final ProblemTypePreference pref = CodeSmellTypeToPreferenceTypeMapper.getPreferenceType(type);
		if (prefs == null) {
			severity = IMarker.SEVERITY_WARNING;
		} else {
			final String prefName = pref.getPreferenceName();
			final String warnLevel = prefs.getString(Activator.PLUGIN_ID, prefName, GeneralConstants.IGNORE, null);
			// Validating and parsing warnLevel
			if (warnLevel.equals(GeneralConstants.IGNORE)) {
				severity = IMarker.SEVERITY_INFO;
			} else if (warnLevel.equals(GeneralConstants.WARNING)) {
				severity = IMarker.SEVERITY_WARNING;
			} else if (warnLevel.equals(GeneralConstants.ERROR)) {
				severity = IMarker.SEVERITY_ERROR;
			} else {
				throw new IllegalStateException("warnLevel should be one of IGNORE, WARNING or ERROR, not " + warnLevel);
			}
		}
	}

	/**
	 * A utility class to make code smell reporting in the spotter subclasses
	 * more straightforward.
	 * 
	 * @author poroszd
	 * 
	 */
	protected class Problems {
		private final List<Marker> reports = new ArrayList<Marker>();

		/**
		 * Create a problem marker at a specific location.
		 * 
		 * @param loc
		 *            The location to marker
		 * @param message
		 *            the message of the marker
		 */
		public void report(final Location loc, final String message) {
			reports.add(new Marker(loc, message, severity, type));
		}

		/**
		 * Create a problem marker without specific location.
		 * 
		 * @param res
		 *            the resource the marker belongs to
		 * @param message
		 *            the message of the marker
		 */
		public void report(final IResource res, final String message) {
			reports.add(new Marker(res, message, severity, type));
		}

		public List<Marker> getMarkers() {
			return reports;
		}
	}
}
