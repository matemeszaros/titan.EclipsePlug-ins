/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * Stores and handles {@link Marker}s.
 * <p>
 * This class is immutable and non-instantiable. Instances are obtained by
 * analyzing a ttcn3 resource (project or module) with an {@link Analyzer}. The
 * returned <code>MarkerHandler</code> is capable of handling the markers on the
 * related resources.
 * 
 * @author poroszd
 * 
 */
public final class MarkerHandler {
	private final Map<IResource, List<Marker>> markersByResource;
	private final Map<CodeSmellType, List<Marker>> markersByType;

	public MarkerHandler(final Map<IResource, List<Marker>> markers) {
		markersByResource = new HashMap<IResource, List<Marker>>();
		for (final Entry<IResource, List<Marker>> entry : markers.entrySet()) {
			for (final Marker m : entry.getValue()) {
				IResource belongsTo = m.getResource();
				if (belongsTo == null) {
					belongsTo = entry.getKey();
				}
				if (markersByResource.get(belongsTo) == null) {
					markersByResource.put(belongsTo, new ArrayList<Marker>());
				}
				markersByResource.get(belongsTo).add(m);
			}
		}

		markersByType = new HashMap<CodeSmellType, List<Marker>>();
	}

	/**
	 * Query the markers of an {@link IResource}, popped up during the analysis
	 * that created this <code>MarkerHandler</code> instance.
	 * 
	 * @param res
	 *            the resource
	 * @return the list of markers (never <code>null</code>)
	 */
	public List<Marker> get(final IResource res) {
		return (markersByResource.get(res) == null) ? Collections.<Marker> emptyList() : Collections.unmodifiableList(markersByResource
				.get(res));
	}

	/**
	 * Query all the markers of a given code smell type, popped up during the
	 * analysis that created this <code>MarkerHandler</code> instance.
	 * 
	 * @param type
	 *            the code smell type
	 * @return the list of markers (never <code>null</code>)
	 */
	public List<Marker> get(final CodeSmellType type) {
		if (markersByType.isEmpty()) {
			lazyInit();
		}

		return Collections.unmodifiableList(markersByType.get(type));
	}
	
	/**
	 * Query the number of occurrences of a given code smell type.
	 * @param type
	 *            the code smell type
	 * @return the number of occurrences
	 */
	public int numberOfOccurrences(final CodeSmellType type) {
		if (markersByType.isEmpty()) {
			lazyInit();
		}
		
		return markersByType.get(type).size();
	}

	// the markersByType field is lazy initialized, as it is usually not
	// used (for showing the markers in eclipse), but nevertheless
	// expensive to produce
	private synchronized void lazyInit() {
		for (final CodeSmellType type : CodeSmellType.values()) {
			markersByType.put(type, new ArrayList<Marker>());
		}
		for (final IResource res : markersByResource.keySet()) {
			for (final Marker m : markersByResource.get(res)) {
				markersByType.get(m.getProblemType()).add(m);
			}
		}
	}

	/**
	 * Asynchronously refresh the code smell markers of a resource in eclipse.
	 * <p>
	 * This method starts a new {@link WorkspaceJob} to delete the current
	 * markers of the given resource, and create the new ones. Note that only
	 * code smell markers are deleted (those of type
	 * {@link CodeSmellType#MARKER_ID}).
	 * 
	 * @param res
	 *            the resource where code smell markers are refreshed
	 */
	public void show(final IResource res) {
		refresh(res);
	}

	/**
	 * Asynchronously create and show the all markers known to this
	 * <code>MarkerHandler</code> in eclipse.
	 * <p>
	 * This method starts a new {@link WorkspaceJob} to delete the current
	 * markers, and create the new ones. Note that only code smell markers are
	 * deleted (those of type {@link CodeSmellType#MARKER_ID}).
	 */
	public void showAll() {
		for (final IResource res : markersByResource.keySet()) {
			refresh(res);
		}
	}

	private void refresh(final IResource res) {
		org.eclipse.titan.designer.AST.MarkerHandler.markMarkersForRemoval(CodeSmellType.MARKER_ID, res);
		final List<Marker> markers = markersByResource.get(res);
		if (markers != null) {
			for (final Marker m : markers) {
				m.show();
			}
		}
		org.eclipse.titan.designer.AST.MarkerHandler.removeMarkedMarkers(CodeSmellType.MARKER_ID, res);
	}

	public Map<IResource, List<Marker>> getMarkersByResource() {
		return markersByResource;
	}
}
