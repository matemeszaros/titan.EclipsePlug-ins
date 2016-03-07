/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.editors.AnnotationImageProvider;
import org.eclipse.titan.designer.editors.ISemanticTITANEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Handles the marker related operations, and operates as an internal cache.
 * Caching markers like this is beneficial as operations directly done on markers are very costly.
 * <p>
 * All operations are synchronized on the markers object.
 * It is very important to understand that the marking of markers for removal and the actual removal should be done in the same job,
 *  protecting at least its own resources from external access.
 * <p>
 * The internal cache holds data about markers either created by us, or reported to be created as a resource change event.
 * It is also true that markers can be removed by us, or reported to be removed by the system.
 * This way external tools should not be able to corrupt our internal data storage too much or for too long
 *  ... however as markers can be modified perfectly in parallel in the system slight differences between the cache and the real world can appear.
 *  Such differences should be removed by the next on-the-fly analysis.
 *  
 *  @author Kristof Szabados
 * */
public final class MarkerHandler {
	private static final String MARKER_HANDLING_ERROR = "Marker handling error";
	/** The list of all the marker qualifiers, to decrease the code size. */
	private static String[] allMarkerTypes = {
			GeneralConstants.COMPILER_ERRORMARKER,
			GeneralConstants.COMPILER_WARNINGMARKER,
			GeneralConstants.COMPILER_INFOMARKER,
			GeneralConstants.ONTHEFLY_SEMANTIC_MARKER,
			GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER,
			GeneralConstants.ONTHEFLY_TASK_MARKER,
			GeneralConstants.ONTHEFLY_MIXED_MARKER // syntactic marker in semantic time...
	};

	/**
	 * Simple internal structure to store marker related data.
	 * */
	private static final class InternalMarker {
		public int row;
		public int offset;
		public int endoffset;
		public long markerID;
	}

	/** type - resource - line - -offset - markerid. */
	private static final Map<String, Map<IResource, List<InternalMarker>>> MARKERS = new HashMap<String, Map<IResource, List<InternalMarker>>>();
	/** type - resource - markerid. */
	private static final Map<String, Map<IResource, Set<Long>>> MARKERS_TO_BE_REMOVED = new HashMap<String, Map<IResource, Set<Long>>>();

	/** private constructor to disable instantiation */
	private MarkerHandler() {
	}

	/**
	 * Marks the markers on a file to be candidates for removal.
	 * If they will not be refreshed till the end of an operation, they could be removed by the removeMarkedMarkers function.
	 *
	 * @param markerTypeID the id of the marker type
	 * @param file the file whose markers should be marked for removal
	 * */
	public static void markMarkersForRemoval(final String markerTypeID, final IResource file) {
		if (!MARKERS.containsKey(markerTypeID)) {
			return;
		}

		synchronized (MARKERS) {
			Map<IResource, List<InternalMarker>> typeSpecificMarkers = MARKERS.get(markerTypeID);

			if (!typeSpecificMarkers.containsKey(file)) {
				return;
			}

			List<InternalMarker> fileSpecificMarkers = typeSpecificMarkers.get(file);

			if (fileSpecificMarkers == null || fileSpecificMarkers.isEmpty()) {
				return;
			}

			Map<IResource, Set<Long>> typeSpecificRemovable;
			if (MARKERS_TO_BE_REMOVED.containsKey(markerTypeID)) {
				typeSpecificRemovable = MARKERS_TO_BE_REMOVED.get(markerTypeID);
			} else {
				typeSpecificRemovable = new HashMap<IResource, Set<Long>>();
				MARKERS_TO_BE_REMOVED.put(markerTypeID, typeSpecificRemovable);
			}

			Set<Long> markerIdstobeRemoved;

			if (typeSpecificRemovable.containsKey(file)) {
				markerIdstobeRemoved = typeSpecificRemovable.get(file);
			} else {
				markerIdstobeRemoved = new HashSet<Long>();
				typeSpecificRemovable.put(file, markerIdstobeRemoved);
			}

			for (InternalMarker marker : fileSpecificMarkers) {
				markerIdstobeRemoved.add(Long.valueOf(marker.markerID));
			}
		}
	}

	/**
	 * Marks the markers on a file in an interval, to be candidates for removal.
	 * If they will not be refreshed till the end of an operation, they could be removed by the removeMarkedMarkers function.
	 *
	 * @param markerTypeID the id of the marker type
	 * @param file the file whose markers should be marked for removal
	 * @param startOffset the start of the interval in which the markers to be marked can be
	 * @param endOffset the start of the interval in which the markers to be marked can be
	 * */
	public static void markMarkersForRemoval(final String markerTypeID, final IResource file,
			final int startOffset, final int endOffset) {

		if (!MARKERS.containsKey(markerTypeID)) {
			return;
		}

		synchronized (MARKERS) {
			Map<IResource, List<InternalMarker>> typeSpecificMarkers = MARKERS.get(markerTypeID);

			if (!typeSpecificMarkers.containsKey(file)) {
				return;
			}

			List<InternalMarker> fileSpecificMarkers = typeSpecificMarkers.get(file);

			if (fileSpecificMarkers == null || fileSpecificMarkers.isEmpty()) {
				return;
			}

			Map<IResource, Set<Long>> typeSpecificRemovable;
			if (MARKERS_TO_BE_REMOVED.containsKey(markerTypeID)) {
				typeSpecificRemovable = MARKERS_TO_BE_REMOVED.get(markerTypeID);
			} else {
				typeSpecificRemovable = new HashMap<IResource, Set<Long>>();
				MARKERS_TO_BE_REMOVED.put(markerTypeID, typeSpecificRemovable);
			}

			Set<Long> markerIds;

			if (typeSpecificRemovable.containsKey(file)) {
				markerIds = typeSpecificRemovable.get(file);
			} else {
				markerIds = new HashSet<Long>();
				typeSpecificRemovable.put(file, markerIds);
			}

			for (InternalMarker marker : fileSpecificMarkers) {
				if (marker.offset >= startOffset && marker.endoffset <= endOffset) {
					markerIds.add(Long.valueOf(marker.markerID));
				}
			}
		}
	}

	/**
	 * Removes those markers from a file which were already marked for removal.
	 *
	 * @param markerTypeID the id of the marker type
	 * @param file the file whose markers should be marked for removal
	 * */
	public static void removeMarkedMarkers(final String markerTypeID, final IResource file) {
		if (!MARKERS_TO_BE_REMOVED.containsKey(markerTypeID)) {
			return;
		}

		List<Long> markersTobeDeleted = new ArrayList<Long>();

		synchronized (MARKERS) {
			Map<IResource, Set<Long>> typeSpecificRemovable = MARKERS_TO_BE_REMOVED.get(markerTypeID);

			if (!typeSpecificRemovable.containsKey(file)) {
				return;
			}

			markersTobeDeleted.addAll(typeSpecificRemovable.get(file));

			typeSpecificRemovable.remove(file);
		}

		for (long markerID : markersTobeDeleted) {
			try {
				IMarker externalMarker = file.findMarker(markerID);
				if (externalMarker != null) {
					externalMarker.delete();
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(MARKER_HANDLING_ERROR, e);
			}
		}
	}

	/**
	 * Checks whether a marker is marked for removal or not.
	 *
	 * @param markerTypeID the id of the marker type.
	 * @param file the file on which the marker can be found.
	 * @param markerID the id of the marker.
	 *
	 * @return true if the marker is marked for removal, false otherwise (even if it does not exists)
	 * */
	public static boolean isMarkerdForRemoval(final String markerTypeID, final IResource file, final long markerID) {
		if (!MarkerHandler.MARKERS_TO_BE_REMOVED.containsKey(markerTypeID)) {
			return false;
		}

		synchronized (MARKERS) {
			Map<IResource, Set<Long>> specificRemovable = MarkerHandler.MARKERS_TO_BE_REMOVED.get(markerTypeID);
			if (!specificRemovable.containsKey(file)) {
				return false;
			}

			Set<Long> markerIds = specificRemovable.get(file);

			if (markerIds.contains(markerID)) {
				return true;
			}

			return false;
		}
	}

	/**
	 * Marks a marker to be used out of those markers that were marked to be removable.
	 *
	 * @param markerTypeID the id of the marker type
	 * @param file the file whose markers should be marked for removal
	 * @param markerId the identifier of the marker
	 * */
	public static void markUsed(final String markerTypeID, final IResource file, final long markerId) {
		if (!MARKERS_TO_BE_REMOVED.containsKey(markerTypeID)) {
			return;
		}

		synchronized (MARKERS) {
			Map<IResource, Set<Long>> typeSpecificRemovable = MARKERS_TO_BE_REMOVED.get(markerTypeID);

			if (!typeSpecificRemovable.containsKey(file)) {
				return;
			}

			Set<Long> markerIds = typeSpecificRemovable.get(file);
			markerIds.remove(markerId);

			if (markerIds.isEmpty()) {
				typeSpecificRemovable.remove(file);

				if (typeSpecificRemovable.isEmpty()) {
					MARKERS_TO_BE_REMOVED.remove(markerTypeID);
				}
			}
		}
	}

	/**
	 * Adds a new marker to a resource (actually the identifier of the marker is registered).
	 *
	 * @param markerTypeID the id of the marker type
	 * @param file the file whose markers should be marked for removal
	 * @param lineNumber the line in which the marker marked region starts
	 * @param offset the offset on which the marked region starts
	 * @param endoffset the offset on which the marked region ends
	 * @param markerId the identifier of the marker
	 * */
	public static void addMarker(final String markerTypeID, final IResource file, final int lineNumber, final int offset, final int endoffset,
			final long markerId) {
		Map<IResource, List<InternalMarker>> typeSpecificMarkers;

		synchronized (MARKERS) {
			if (MARKERS.containsKey(markerTypeID)) {
				typeSpecificMarkers = MARKERS.get(markerTypeID);
			} else {
				typeSpecificMarkers = new HashMap<IResource, List<InternalMarker>>();
				MARKERS.put(markerTypeID, typeSpecificMarkers);
			}

			List<InternalMarker> fileSpecificMarkers;
			if (typeSpecificMarkers.containsKey(file)) {
				fileSpecificMarkers = typeSpecificMarkers.get(file);
			} else {
				fileSpecificMarkers = new ArrayList<InternalMarker>();
				typeSpecificMarkers.put(file, fileSpecificMarkers);
			}

			InternalMarker temp = new InternalMarker();
			temp.row = lineNumber;
			temp.offset = offset;
			temp.endoffset = endoffset;
			temp.markerID = markerId;
			fileSpecificMarkers.add(temp);

			markUsed(markerTypeID, file, markerId);
		}
	}

	/**
	 *Checks if a resource has a marker with the provided text on the provided position.
	 *
	 * @param markerTypeID the id of the marker type
	 * @param file the file whose markers should be marked for removal
	 * @param lineNumber the line at which the marker is expected to be
	 * @param offset the offset at which the marker marked region is expected to start
	 * @param endoffset the offset at which the marker marked region is expected to end
	 * @param severity the expected severity of the marker
	 * @param text the error message to be expected
	 *
	 * @return the marker if one was found, otherwise null
	 * */
	public static IMarker hasMarker(final String markerTypeID, final IResource file, final int lineNumber, final int offset, final int endoffset,
			final int severity, final String text) {
		if (!MARKERS.containsKey(markerTypeID)) {
			return null;
		}

		synchronized (MARKERS) {
			Map<IResource, List<InternalMarker>> typeSpecificMarkers = MARKERS.get(markerTypeID);

			if (!typeSpecificMarkers.containsKey(file)) {
				return null;
			}

			List<InternalMarker> fileSpecificMarkers = typeSpecificMarkers.get(file);

			for (InternalMarker internalMarker : fileSpecificMarkers) {
				if (internalMarker.row == lineNumber && internalMarker.offset == offset && internalMarker.endoffset == endoffset) {
					try {
						IMarker externalMarker = file.findMarker(internalMarker.markerID);
						if (externalMarker != null && severity == externalMarker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)
								&& externalMarker.getAttribute(IMarker.MESSAGE, "").equals(text)) {
							return externalMarker;
						}
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
			}

			return null;
		}
	}

	/**
	 * Updates the stored markers, should be run only by incremental syntax checking.
	 * As such it needs to know where the change started in the file, and how much has it shifted the contents.
	 *
	 * @param file the file whose markers should be updated
	 * @param lineOffset the index of the first line where the change happened.
	 * @param lineShift the number of lines all line following the line offset have to be shifted with.
	 * @param offset the offset where the change happened
	 * @param shift the amount to shift the markers offset.
	 * */
	public static void updateMarkers(final IResource file, final int lineOffset, final int lineShift, final int offset, final int shift) {
		for (String markerTypeID : MARKERS.keySet()) {
			updateMarkers(markerTypeID, file, lineOffset, lineShift, offset, shift);
		}
	}

	/**
	 * Updates the stored markers, should be run only by incremental syntax checking.
	 * As such it needs to know where the change started in the file, and how much has it shifted the contents.
	 *
	 * @param markerTypeID the type of markers to be updated
	 * @param file the file whose markers should be updated
	 * @param lineOffset the index of the first line where the change happened.
	 * @param lineShift the number of lines all line following the line offset have to be shifted with.
	 * @param offset the offset where the change happened
	 * @param shift the amount to shift the markers offset.
	 * */
	private static void updateMarkers(final String markerTypeID, final IResource file,
			final int lineOffset, final int lineShift, final int offset, final int shift) {
		if (!MARKERS.containsKey(markerTypeID)) {
			return;
		}

		synchronized (MARKERS) {
			Map<IResource, List<InternalMarker>> typeSpecificMarkers = MARKERS.get(markerTypeID);

			if (!typeSpecificMarkers.containsKey(file)) {
				return;
			}

			List<InternalMarker> fileSpecificMarkers = typeSpecificMarkers.get(file);

			if (fileSpecificMarkers == null || fileSpecificMarkers.isEmpty()) {
				return;
			}

			for (Iterator<InternalMarker> iterator = fileSpecificMarkers.iterator(); iterator.hasNext();) {
				InternalMarker marker = iterator.next();

				if (marker.row >= lineOffset && marker.offset > offset) {
					marker.row += lineShift;
					if (marker.offset != -1) {
						marker.offset += shift;
					}
					if (marker.endoffset != -1) {
						marker.endoffset += shift;
					}
				}
			}
		}
	}

	/**
	 * Updates the stored markers, should be run only by incremental syntax checking.
	 * As such it needs to know where the change started in the file, and how much has it shifted the contents.
	 *
	 * @param file the file whose markers should be updated
	 * @param lineOffset the index of the first line where the change happened.
	 * @param lineShift the number of lines all line following the line offset have to be shifted with.
	 * @param offset the offset where the change happened
	 * @param shift the amount to shift the markers offset.
	 * */
	public static void updateInsertMarkers(final IResource file, final int lineOffset, final int lineShift, final int offset, final int shift) {
		for (String markerTypeID : MARKERS.keySet()) {
			updateInsertMarkers(markerTypeID, file, lineOffset, lineShift, offset, shift);
		}
	}

	/**
	 * Updates the stored markers, should be run only by incremental syntax checking.
	 * As such it needs to know where the change started in the file, and how much has it shifted the contents.
	 *
	 * @param markerTypeID the type of markers to be updated
	 * @param file the file whose markers should be updated
	 * @param lineOffset the index of the first line where the change happened.
	 * @param lineShift the number of lines all line following the line offset have to be shifted with.
	 * @param offset the offset where the change happened
	 * @param shift the amount to shift the markers offset.
	 * */
	private static void updateInsertMarkers(final String markerTypeID, final IResource file,
			final int lineOffset, final int lineShift, final int offset, final int shift) {
		if (!MARKERS.containsKey(markerTypeID)) {
			return;
		}

		synchronized (MARKERS) {
			Map<IResource, List<InternalMarker>> typeSpecificMarkers = MARKERS.get(markerTypeID);

			if (!typeSpecificMarkers.containsKey(file)) {
				return;
			}

			List<InternalMarker> fileSpecificMarkers = typeSpecificMarkers.get(file);

			if (fileSpecificMarkers == null || fileSpecificMarkers.isEmpty()) {
				return;
			}

			for (Iterator<InternalMarker> iterator = fileSpecificMarkers.iterator(); iterator.hasNext();) {
				InternalMarker marker = iterator.next();

				if (marker.row >= lineOffset && marker.offset > offset) {
					marker.row += lineShift;
					if (marker.offset != -1) {
						marker.offset += shift;
					}
					if (marker.endoffset != -1) {
						marker.endoffset += shift;
					}

					try {
						IMarker externalMarker = file.findMarker(marker.markerID);
						if (externalMarker == null) {
							iterator.remove();
						}
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
			}
		}
	}

	/**
	 * Updates the stored markers, should be run only by incremental syntax checking.
	 * As such it needs to know where the change started in the file, and how much has it shifted the contents.
	 * <p>
	 * When a marker starts in an interval that was removed it will be marked for removal.
	 *
	 * @param file the file whose markers should be updated
	 * @param lineOffset the index of the first line where the change happened.
	 * @param lineShift the number of lines all line following the line offset have to be shifted with (the absolute value).
	 * @param offset the offset where the change happened
	 * @param shift the amount to shift the markers offset.
	 * */
	public static void updateRemoveMarkers(final IResource file, final int lineOffset, final int lineShift, final int offset, final int shift) {
		for (String markerTypeID : MARKERS.keySet()) {
			updateRemoveMarkers(markerTypeID, file, lineOffset, lineShift, offset, shift);
		}
	}

	/**
	 * Updates the stored markers, should be run only by incremental syntax checking.
	 * As such it needs to know where the change started in the file, and how much has it shifted the contents.
	 * <p>
	 * When a marker starts in an interval that was removed it will be marked for removal.
	 *
	 * @param markerTypeID the type of markers to be updated
	 * @param file the file whose markers should be updated
	 * @param lineOffset the index of the first line where the change happened.
	 * @param lineShift the number of lines all line following the line offset have to be shifted with (the absolute value).
	 * @param offset the offset where the change happened
	 * @param shift the amount to shift the markers offset (negative value).
	 * */
	private static void updateRemoveMarkers(final String markerTypeID, final IResource file, final int lineOffset,
			final int lineShift, final int offset, final int shift) {
		if (!MARKERS.containsKey(markerTypeID)) {
			return;
		}

		List<Long> markersTobeDeleted = new ArrayList<Long>();

		synchronized (MARKERS) {
			Map<IResource, List<InternalMarker>> typeSpecificMarkers = MARKERS.get(markerTypeID);

			if (!typeSpecificMarkers.containsKey(file)) {
				return;
			}

			List<InternalMarker> fileSpecificMarkers = typeSpecificMarkers.get(file);

			if (fileSpecificMarkers == null || fileSpecificMarkers.isEmpty()) {
				return;
			}

			for (Iterator<InternalMarker> iterator = fileSpecificMarkers.iterator(); iterator.hasNext();) {
				InternalMarker marker = iterator.next();

				if (marker.offset >= offset && marker.offset + shift <= offset) {
					iterator.remove();
					markersTobeDeleted.add(marker.markerID);
				} else {
					if (marker.row != -1 && marker.row > lineOffset) {
						marker.row -= lineShift;
					}
					if (marker.offset != -1 && marker.offset > offset - shift) {
						marker.offset += shift;
					}
					if (marker.endoffset != -1 && marker.endoffset > offset - shift) {
						marker.endoffset += shift;
					}

					try {
						IMarker externalMarker = file.findMarker(marker.markerID);
						if (externalMarker == null) {
							iterator.remove();
						}
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(MARKER_HANDLING_ERROR, e);
					}
				}
			}
		}

		for (long markerID : markersTobeDeleted) {
			try {
				IMarker externalMarker = file.findMarker(markerID);
				if (externalMarker != null) {
					externalMarker.delete();
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(MARKER_HANDLING_ERROR, e);
			}
		}
	}

	/**
	 * Collects all files that are under the starting resource.
	 * */
	public static final class FileFinder implements IResourceVisitor {
		private List<IFile> files = new ArrayList<IFile>();

		@Override
		public boolean visit(final IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				files.add((IFile) resource);
				return false;
			}

			return true;
		}

		public List<IFile> getFiles() {
			return files;
		}
	}

	/**
	 * Marks all markers on the provided resource to be ready for removal.
	 * Removes the markers also from the children of the resource.
	 *
	 * @param resource the resource whose markers can be removed.
	 * */
	public static void markAllMarkersForRemoval(final IResource resource) {
		markAllMarkersForRemoval(resource, true);
	}

	/**
	 * Marks all markers on the provided resource to be ready for removal.
	 * <p>
	 * todo markers are excluded as they can not be invalidated by the compiler.
	 *
	 * @param resource the resource whose markers can be removed.
	 * @param effectsChildren true if the children of the resource should be processed.
	 * */
	public static void markAllMarkersForRemoval(final IResource resource, final boolean effectsChildren) {
		List<IResource> resources = new ArrayList<IResource>();
		resources.add(resource);

		if (effectsChildren && !(resource instanceof IFile)) {
			FileFinder finder = new FileFinder();
			try {
				resource.accept(finder);
				for (IFile file : finder.getFiles()) {
					resources.add(file);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(MARKER_HANDLING_ERROR, e);
			}
		}

		IResource tempResource;
		for (int i = 0, size = resources.size(); i < size; i++) {
			tempResource = resources.get(i);
			markMarkersForRemoval(GeneralConstants.COMPILER_ERRORMARKER, tempResource);
			markMarkersForRemoval(GeneralConstants.COMPILER_WARNINGMARKER, tempResource);
			markMarkersForRemoval(GeneralConstants.COMPILER_INFOMARKER, tempResource);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, tempResource);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, tempResource);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_MIXED_MARKER, tempResource);
		}
	}

	/**
	 * Marks all markers on the provided resource to be ready for removal.
	 *
	 * @param resource the resource whose markers can be removed.
	 * @param markerTypeID the type of markers to be marked for removal.
	 * */
	public static void markAllMarkersForRemoval(final IResource resource, final String markerTypeID) {
		List<IResource> resources = new ArrayList<IResource>();
		resources.add(resource);

		if (!(resource instanceof IFile)) {
			FileFinder finder = new FileFinder();
			try {
				resource.accept(finder);
				for (IFile file : finder.getFiles()) {
					resources.add(file);
				}
			} catch (CoreException e) {
				//be silent, perhaps it already has been removed
			}
		}

		IResource tempResource;
		for (int i = 0, size = resources.size(); i < size; i++) {
			tempResource = resources.get(i);
			markMarkersForRemoval(markerTypeID, tempResource);
		}
	}

	/**
	 * Marks all on-the-fly markers on the provided resource to be ready for removal.
	 *
	 * @param resource the resource whose markers can be removed.
	 * */
	public static void markAllOnTheFlyMarkersForRemoval(final IResource resource) {
		List<IResource> resources = new ArrayList<IResource>();
		resources.add(resource);

		if (!(resource instanceof IFile)) {
			FileFinder finder = new FileFinder();
			try {
				resource.accept(finder);
				for (IFile file : finder.getFiles()) {
					resources.add(file);
				}
			} catch (CoreException e) {
				//be silent, perhaps it already has been removed
			}
		}

		IResource tempResource;
		for (int i = 0, size = resources.size(); i < size; i++) {
			tempResource = resources.get(i);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, tempResource);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, tempResource);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_TASK_MARKER, tempResource);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_MIXED_MARKER, tempResource);
		}
	}

	/**
	 * Marks all syntactic on-the-fly markers on the provided resource to be ready for removal.
	 *
	 * @param resource the resource whose markers can be removed.
	 * @param startOffset the start of the interval in which the markers to be marked can be
	 * @param endOffset the start of the interval in which the markers to be marked can be
	 * */
	public static void markAllOnTheFlyMarkersForRemoval(final IResource resource, final int startOffset, final int endOffset) {
		List<IResource> resources = new ArrayList<IResource>();
		resources.add(resource);

		if (!(resource instanceof IFile)) {
			FileFinder finder = new FileFinder();
			try {
				resource.accept(finder);
				for (IFile file : finder.getFiles()) {
					resources.add(file);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(MARKER_HANDLING_ERROR, e);
			}
		}

		IResource tempResource;
		for (int i = 0, size = resources.size(); i < size; i++) {
			tempResource = resources.get(i);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, tempResource, startOffset, endOffset);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, tempResource, startOffset, endOffset);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_TASK_MARKER, tempResource);
			markMarkersForRemoval(GeneralConstants.ONTHEFLY_MIXED_MARKER, tempResource);
		}
	}

	/**
	 * Marks all task markers on the provided resource to be ready for removal.
	 *
	 * @param resource the resource whose markers can be removed.
	 * */
	public static void markAllTaskMarkersForRemoval(final IFile resource) {
		markMarkersForRemoval(GeneralConstants.ONTHEFLY_TASK_MARKER, resource);
	}

	/**
	 * Marks all task markers on the provided resource to be ready for removal.
	 *
	 * @param resource the resource whose markers can be removed.
	 * */
	public static void markAllSemanticMarkersForRemoval(final IFile resource) {
		markMarkersForRemoval(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, resource);
		markMarkersForRemoval(GeneralConstants.ONTHEFLY_MIXED_MARKER, resource); // this is for ASN.1 Blocks and extension attributes
	}

	/**
	 * Re-enable all of the On-the-fly markers on the provided file.
	 *
	 * @param file the file to work on.
	 * */
	public static void reEnableAllMarkers(final IFile file) {
		synchronized (MARKERS) {
			Map<IResource, Set<Long>> typeSpecificRemovable;
			for (String qualifier : allMarkerTypes) {
				typeSpecificRemovable = MARKERS_TO_BE_REMOVED.get(qualifier);
				if (typeSpecificRemovable != null && typeSpecificRemovable.containsKey(file)) {
					typeSpecificRemovable.remove(file);
				}
			}
		}
	}

	/**
	 * Re-enable all of the On-the-fly markers on the provided file inside the selected interval.
	 *
	 * @param file the file to work on.
	 * @param startOffset the start of the interval.
	 * @param endOffset the end of the interval.
     *	
	 * */
	public static void reEnableAllSemanticMarkers(final IFile file, final int startOffset, final int endOffset) {
		//
		synchronized (MARKERS) {
			if (!MARKERS_TO_BE_REMOVED.containsKey(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER)) {
				return;
			}

			Map<IResource, Set<Long>> typeSpecificRemovable = MARKERS_TO_BE_REMOVED.get(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
			if (typeSpecificRemovable != null && typeSpecificRemovable.containsKey(file)) {
				Set<Long> markerIds = typeSpecificRemovable.get(file);

				List<InternalMarker> fileSpecificMarkers = MARKERS.get(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER).get(file);
				if (fileSpecificMarkers == null || fileSpecificMarkers.isEmpty()) {
					markerIds.clear();
					return;
				}

				for (InternalMarker marker : fileSpecificMarkers) {
					if (marker.offset >= startOffset && marker.endoffset <= endOffset) {
						// there should be no need to check if it is inside or not.
						markerIds.remove(Long.valueOf(marker.markerID));
					}
				}
			}
		}
	}

	/**
	 * Removes all marked markers from the provided resource and all the children of the resource.
	 *
	 * @param resource the resource whose markers can be removed.
	 * */
	public static void removeAllMarkedMarkers(final IResource resource) {
		List<IResource> resources = new ArrayList<IResource>();
		resources.add(resource);

		if (!(resource instanceof IFile)) {
			FileFinder finder = new FileFinder();
			try {
				resource.accept(finder);
				for (IFile file : finder.getFiles()) {
					resources.add(file);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(MARKER_HANDLING_ERROR, e);
			}
		}

		IResource tempResource;
		for (int i = 0, size = resources.size(); i < size; i++) {
			tempResource = resources.get(i);
			for (String qualifier : allMarkerTypes) {
				removeMarkedMarkers(qualifier, tempResource);
			}
		}
	}

	/**
	 * Removes all marked on-the-fly syntactic markers from the provided resource and all the children of the resource.
	 *
	 * @param resource the resource whose markers can be removed.
	 * */
	public static void removeAllOnTheFlySyntacticMarkedMarkers(final IResource resource) {
		List<IResource> resources = new ArrayList<IResource>();
		resources.add(resource);

		if (!(resource instanceof IFile)) {
			FileFinder finder = new FileFinder();
			try {
				resource.accept(finder);
				for (IFile file : finder.getFiles()) {
					resources.add(file);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(MARKER_HANDLING_ERROR, e);
			}
		}

		IResource tempResource;
		for (int i = 0, size = resources.size(); i < size; i++) {
			tempResource = resources.get(i);
			removeMarkedMarkers(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, tempResource);
			removeMarkedMarkers(GeneralConstants.ONTHEFLY_TASK_MARKER, tempResource);
			removeMarkedMarkers(GeneralConstants.ONTHEFLY_MIXED_MARKER, tempResource);
		}
	}

	/**
	 * Removes all marked on-the-fly markers from the provided resource and all the children of the resource.
	 *
	 * @param resource the resource whose markers can be removed.
	 * */
	public static void removeAllOnTheFlyMarkedMarkers(final IResource resource) {
		List<IResource> resources = new ArrayList<IResource>();
		resources.add(resource);

		if (!(resource instanceof IFile)) {
			FileFinder finder = new FileFinder();
			try {
				resource.accept(finder);
				for (IFile file : finder.getFiles()) {
					resources.add(file);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(MARKER_HANDLING_ERROR, e);
			}
		}

		IResource tempResource;
		for (int i = 0, size = resources.size(); i < size; i++) {
			tempResource = resources.get(i);
			removeMarkedMarkers(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, tempResource);
			removeMarkedMarkers(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, tempResource);
			removeMarkedMarkers(GeneralConstants.ONTHEFLY_TASK_MARKER, tempResource);
			removeMarkedMarkers(GeneralConstants.ONTHEFLY_MIXED_MARKER, tempResource);
		}
	}

	/**
	 * Mark the provided markers as deprecated.
	 * <p>
	 * Those that are in an open editor, can be manipulated directly,
	 *  the others will receive their value later by the AnnotationImageProvider
	 *
	 *  @param editor the editor being open/edited triggering the change.
	 *  @param markers the markers to mark deprecated.
	 * */
	public static void deprecateMarkers(final ISemanticTITANEditor editor, final IMarker[] markers) {
		if (editor == null || markers == null) {
			return;
		}

		try {
			AbstractDecoratedTextEditor textEditor = null;
			IFile fileInput = null;
			if (editor instanceof AbstractDecoratedTextEditor) {
				textEditor = (AbstractDecoratedTextEditor) editor;
				if (textEditor.getEditorInput() instanceof FileEditorInput) {
					FileEditorInput input = (FileEditorInput) textEditor.getEditorInput();
					fileInput = input.getFile();
				}
			}

			for (IMarker marker : markers) {
				if (fileInput != null && textEditor != null && fileInput.equals(marker.getResource())) {
					IEditorInput editorInput = textEditor.getEditorInput();
					IDocumentProvider provider = textEditor.getDocumentProvider();
					if (editorInput != null && provider != null) {
						IAnnotationModel model = provider.getAnnotationModel(editorInput);
						Iterator<?> e = model.getAnnotationIterator();
						while (e.hasNext()) {
							Annotation annotation = (Annotation) e.next();
							if (annotation instanceof MarkerAnnotation && marker.equals(((MarkerAnnotation) annotation).getMarker())) {
								annotation.markDeleted(true);
							}
						}
					} else {
						if (!marker.getAttribute(AnnotationImageProvider.DEPRECATED, false)) {
							marker.setAttribute(AnnotationImageProvider.DEPRECATED, true);
						}
					}
				} else {
					if (!marker.getAttribute(AnnotationImageProvider.DEPRECATED, false)) {
						marker.setAttribute(AnnotationImageProvider.DEPRECATED, true);
					}
				}
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(MARKER_HANDLING_ERROR, e);
		}
	}

	/**
	 * Handle a resource change event in hope to detect and book marker changes.
	 * The changes reported can be because of internal or external operations.
	 *
	 * @param event the event to process.
	 * */
	public static void handleResourceChanges(final IResourceChangeEvent event) {
		for (String qualifier : allMarkerTypes) {
			IMarkerDelta[] markerDeltas = event.findMarkerDeltas(qualifier, false);
			if (markerDeltas == null) {
				continue;
			}

			for (int i = 0; i < markerDeltas.length; i++) {
				IMarkerDelta markerDelta = markerDeltas[i];
				if (markerDelta == null) {
					continue;
				}

				IResource resource = markerDelta.getResource();
				if (resource == null) {
					continue;
				}

				IMarker marker = markerDelta.getMarker();
				if (marker == null) {
					continue;
				}

				switch (markerDelta.getKind()) {
				case IResourceDelta.ADDED:
					handleMarkerAddition(marker, qualifier);
					break;
				case IResourceDelta.REMOVED:
					handleMarkerRemoval(marker, qualifier);
					break;
				case IResourceDelta.CHANGED:
					handleMarkerChange(marker, qualifier);
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * Handle the event of a new marker being created.
	 * If it was created by us, leave it. If it was created externally book it in the cache.
	 *
	 * @param marker the marker just added to the system.
	 * @param type the type of the marker.
	 * */
	private static void handleMarkerAddition(final  IMarker marker, final String type) {
		final IResource resource = marker.getResource();
		final int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
		final int offset = marker.getAttribute(IMarker.CHAR_START, -1);
		final int endoffset = marker.getAttribute(IMarker.CHAR_END, -1);
		final long markerId = marker.getId();

		Map<IResource, List<InternalMarker>> typeSpecificMarkers;

		synchronized (MARKERS) {
			if (MARKERS.containsKey(type)) {
				typeSpecificMarkers = MARKERS.get(type);
			} else {
				typeSpecificMarkers = new HashMap<IResource, List<InternalMarker>>();
				MARKERS.put(type, typeSpecificMarkers);
			}

			List<InternalMarker> fileSpecificMarkers;
			if (typeSpecificMarkers.containsKey(resource)) {
				fileSpecificMarkers = typeSpecificMarkers.get(resource);
			} else {
				fileSpecificMarkers = new ArrayList<InternalMarker>();
				typeSpecificMarkers.put(resource, fileSpecificMarkers);
			}

			boolean found = false;
			for (int i = 0; !found && i < fileSpecificMarkers.size(); i++) {
				if (fileSpecificMarkers.get(i).markerID == markerId) {
					found = true;
				}
			}

			if (!found) {
				InternalMarker temp = new InternalMarker();
				temp.row = lineNumber;
				temp.offset = offset;
				temp.endoffset = endoffset;
				temp.markerID = markerId;
				fileSpecificMarkers.add(temp);
			}

			markUsed(type, resource, markerId);
		}
	}

	/**
	 * Handle the event of a marker was removed.
	 * If it is mentioned in our cache, remove it from there.
	 *
	 * @param marker the marker just removed from the system.
	 * @param type the type of the marker.
	 * */
	private static void handleMarkerRemoval(final  IMarker marker, final String type) {
		final IResource resource = marker.getResource();
		final long markerId = marker.getId();

		synchronized (MARKERS) {
			Map<IResource, List<InternalMarker>> typeSpecificMarkers = MARKERS.get(type);
			if (typeSpecificMarkers == null) {
				return;
			}

			List<InternalMarker> resourceSpecificMarkers = typeSpecificMarkers.get(resource);
			if (resourceSpecificMarkers == null) {
				return;
			}

			boolean found = false;
			for (int i = 0; !found && i < resourceSpecificMarkers.size(); i++) {
				if (resourceSpecificMarkers.get(i).markerID == markerId) {
					resourceSpecificMarkers.remove(i);
					found = true;
				}
			}

			if (resourceSpecificMarkers.isEmpty()) {
				typeSpecificMarkers.remove(resource);
			}
		}
	}

	/**
	 * Handle the event when a marker changes.
	 * <p>
	 * Right now we simply remove the old data, and overwrite it with the new one.
	 *
	 * @param marker the marker that changed.
	 * @param type the type of the marker.
	 * */
	private static void handleMarkerChange(final  IMarker marker, final String type) {
		handleMarkerRemoval(marker, type);
		handleMarkerAddition(marker, type);
	}

	/**
	 * Checks if there is  marker of a specific kind on a file.
	 *
	 * @param markerTypeID the type identifier of the marker kind.
	 * @param file the file to search on.
	 *
	 * @return true if there is at least one marker of the provided kind on the provided file, false otherwise.
	 * */
	public static boolean hasMarker(final String markerTypeID, final IResource file) {
		if (!MARKERS.containsKey(markerTypeID)) {
			return false;
		}

		synchronized (MARKERS) {
			Map<IResource, List<InternalMarker>> typeSpecificMarkers = MARKERS.get(markerTypeID);

			List<InternalMarker> fileSpecificMarkers = typeSpecificMarkers.get(file);
			if (fileSpecificMarkers == null || fileSpecificMarkers.isEmpty()) {
				return false;
			}

			return true;
		}
	}
}
