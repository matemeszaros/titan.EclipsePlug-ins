/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.ASTLocationChainVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.declarationsearch.IdentifierFinderVisitor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.preferences.SubscribedBoolean;
import org.eclipse.titan.designer.preferences.SubscribedInt;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Szabolcs Beres
 * */
public abstract class OccurencesMarker {
	private static final String ANNOTATION_TYPE = "org.eclipse.titan.occurrences";
	private static SubscribedBoolean reportDebugInformation;
	private static SubscribedBoolean printASTElem;

	private static SubscribedInt delay;
	private static SubscribedBoolean keepMarks;

	private final ITextEditor editor;
	private Annotation[] occurrenceAnnotations = null;
	private MarkerJob markerJob = new MarkerJob();

	private class MarkerJob extends WorkspaceJob {
		private IDocument document;
		private int offset;

		public MarkerJob() {
			super("Marking occurrences");
			setSystem(true);
		}

		public synchronized void setParam(final IDocument document, final int offset) {
			this.document = document;
			this.offset = offset;
		}

		@Override
		public synchronized IStatus runInWorkspace(final IProgressMonitor pMonitor) throws CoreException {
			doMark(document, offset);
			return Status.OK_STATUS;
		}
	}

	static {
		delay = new SubscribedInt(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.MARK_OCCURRENCES_DELAY, 100);
		keepMarks = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.MARK_OCCURRENCES_KEEP_MARKS, false);
		reportDebugInformation = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
				false);
		printASTElem = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DEBUG_CONSOLE_AST_ELEM, false);
	}

	public OccurencesMarker(final ITextEditor editor) {
		this.editor = editor;
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				final String property = event.getProperty();
				if (PreferenceConstants.MARK_OCCURRENCES_ENABLED.equals(property)
						|| PreferenceConstants.MARK_OCCURRENCES_ASN1_ASSIGNMENTS.equals(property)
						|| PreferenceConstants.MARK_OCCURRENCES_TTCN3_ASSIGNMENTS.equals(property)
						|| PreferenceConstants.MARK_OCCURRENCES_KEEP_MARKS.equals(property)) {
					removeOccurences(true);
					return;
				}
			}
		});
	}

	/**
	 * Displays the error message on the debug console if the
	 * "report debug information" switch is on
	 * 
	 * @param document
	 *                The document currently opened in the editor.
	 * @param offset
	 *                The offset of the cursor.
	 * @param reason
	 *                The error message.
	 */
	private void error(final IDocument document, final int offset, final String reason) {
		if (!reportDebugInformation.getValue()) {
			return;
		}

		final String fileName = editor.getEditorInput() == null ? "unknown" : editor.getEditorInput().getName();
		try {
			final int line = document.getLineOfOffset(offset);
			final int offsetInLine = offset - document.getLineOffset(line);

			TITANDebugConsole.println("Mark occurrences failed: " + reason + "\nFile: " + fileName + "\nLine: " + line + "\nOffset: "
					+ offsetInLine);
		} catch (BadLocationException e) {
			TITANDebugConsole.println("Mark occurrences failed: invalid offset" + "\nFile: " + fileName + "\nOffset: " + offset);
		}
	}

	public void markOccurences(final IDocument document, final int offset) {
		markerJob.cancel();
		markerJob.setParam(document, offset);
		markerJob.schedule(delay.getValue());
	}

	public void doMark(final IDocument document, final int offset) {
		IAnnotationModel annotationModel = getAnnotationModel();
		if (annotationModel == null) {
			removeOccurences(false);
			error(document, offset, "AnnotationModel is null");
			return;
		}

		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (file == null) {
			removeOccurences(false);
			error(document, offset, "can not determine the file in the editor.");
			return;
		}

		ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
		if (projectSourceParser == null) {
			removeOccurences(false);
			error(document, offset, "Can not find the projectsourceparser for the project: " + file.getProject());
			return;
		}
		final String moduleName = projectSourceParser.containedModule(file);
		if (moduleName == null) {
			removeOccurences(false);
			error(document, offset, "The module name can not be found in the project.");
			return;
		}
		final Module module = projectSourceParser.getModuleByName(moduleName);
		if (module == null) {
			removeOccurences(false);
			error(document, offset, "The module can not be found in the project.");
			return;
		}

		if (printASTElem.getValue()) {
			ASTLocationChainVisitor locVisitor = new ASTLocationChainVisitor(offset);
			module.accept(locVisitor);
			locVisitor.printChain();
		}

		final Reference reference = getReferenceParser().findReferenceForOpening(file, offset, document);
		if (reference == null) {
			removeOccurences(false);
			// error(document, offset,
			// "The reference can not be parsed.");
			return;
		}

		if (reference.getLocation().getOffset() == reference.getLocation().getEndOffset()) {
			removeOccurences(false);
			return;
		}

		// if (reportDebugInformation) {
		// ASTLocationConsistencyVisitor visitor = new
		// ASTLocationConsistencyVisitor(document,
		// module.get_moduletype()==Module.module_type.TTCN3_MODULE);
		// module.accept(visitor);
		// }

		final List<Hit> result = findOccurrences(document, reference, module, offset);
		if (result.isEmpty()) {
			return;
		}

		Map<Annotation, Position> annotationMap = new HashMap<Annotation, Position>();
		for (Hit hit : result) {
			if (hit.identifier != null) {
				int hitOffset = hit.identifier.getLocation().getOffset();
				int hitEndOffset = hit.identifier.getLocation().getEndOffset();
				if( hitOffset>=0 && hitEndOffset>=0 && hitEndOffset>=hitOffset ) {
					final Annotation annotationToAdd = new Annotation(ANNOTATION_TYPE, false, "Occurence of " + hit.identifier.getDisplayName());
					final Position position = new Position(hitOffset, hitEndOffset - hitOffset);
					annotationMap.put(annotationToAdd, position);
				}
			}
		}

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(occurrenceAnnotations, annotationMap);
			} else {
				if (occurrenceAnnotations != null) {
					for (Annotation annotationToRemove : occurrenceAnnotations) {
						annotationModel.removeAnnotation(annotationToRemove);
					}
				}

				for (Map.Entry<Annotation, Position> entry : annotationMap.entrySet()) {
					annotationModel.addAnnotation(entry.getKey(), entry.getValue());
				}
			}

			occurrenceAnnotations = annotationMap.keySet().toArray(new Annotation[annotationMap.keySet().size()]);
		}
	}

	protected abstract List<Hit> findOccurrences(final IDocument document, final Reference reference, final Module module, final int offset);

	/**
	 * Finds the occurrences of the element located on the given offset.
	 * This search is based on the {@link ASTLocationChainVisitor}.
	 * 
	 * @param module
	 *                The module to search the occurrences in
	 * @param offset
	 *                An offset in the module
	 * @return The found references. Includes the definition of the element.
	 */
	protected List<Hit> findOccurrencesLocationBased(final Module module, final int offset) {
		final IdentifierFinderVisitor visitor = new IdentifierFinderVisitor(offset);
		module.accept(visitor);
		final Declaration def = visitor.getReferencedDeclaration();

		if (def == null || !def.shouldMarkOccurrences()) {
			removeOccurences(false);
			return new ArrayList<Hit>();
		}

		return def.getReferences(module);
	}

	/**
	 * Finds the occurrences of the element located on the given offset.
	 * This solution can be used, when the locations are not correct. (e.g.
	 * in case of an ASN.1 file)
	 * 
	 * @param document
	 * @param reference
	 * @param module
	 *                The module to search the occurrences in
	 * @param offset
	 *                An offset in the module
	 * @return The found references. Includes the definition of the element.
	 */
	protected List<Hit> findOccurrencesReferenceBased(final IDocument document, final Reference reference, final Module module, final int offset) {

		Scope scope = module.getSmallestEnclosingScope(offset);
		if (scope == null) {
			removeOccurences(false);
			error(document, offset, "Can not determine the smallest enclosing scope.");
			return new ArrayList<ReferenceFinder.Hit>();
		}

		reference.setMyScope(scope);
		if (reference.getId() == null) {
			removeOccurences(false);
			error(document, offset, "The identifier of the reference is null.");
			return new ArrayList<ReferenceFinder.Hit>();
		}
		if (reference.getSubreferences().size() > 1) {
			// highlighting the subreferences is not yet supported
			removeOccurences(false);
			return new ArrayList<ReferenceFinder.Hit>();
		}
		ReferenceFinder referenceFinder;
		List<Hit> result = null;
		boolean found = false;
		if (scope.hasAssignmentWithId(CompilationTimeStamp.getBaseTimestamp(), reference.getId())
				|| (scope.getModuleScope().hasImportedAssignmentWithID(CompilationTimeStamp.getBaseTimestamp(), reference.getId()))) {

			Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
			if (assignment == null) {
				error(document, offset, "The assignment could not be determined from the reference: " + reference.getDisplayName());
				removeOccurences(false);
				return new ArrayList<ReferenceFinder.Hit>();
			}
			if (!assignment.shouldMarkOccurrences()) {
				removeOccurences(false);
				return new ArrayList<ReferenceFinder.Hit>();
			}

			try {
				referenceFinder = new ReferenceFinder(assignment);
			} catch (final IllegalArgumentException e) {
				removeOccurences(false);
				return new ArrayList<ReferenceFinder.Hit>();
			}

			result = referenceFinder.findReferencesInModule(module);

			// Hack to eliminate false positive results
			if (assignment.getLocation().containsOffset(offset)) {
				found = true;
			} else {
				for (Hit hit : result) {
					if (hit.identifier.getLocation().containsOffset(offset)) {
						found = true;
						break;
					}
				}
			}

			if (found && assignment.getMyScope().getModuleScope() == module && assignment.getIdentifier() != null) {
				result.add(new Hit(assignment.getIdentifier()));
			}
		}

		if (!found) {
			// Check if the reference points to a field of a type
			// definition
			referenceFinder = new ReferenceFinder();
			referenceFinder.detectAssignmentDataByOffset(module, offset, editor, false, false);

			Assignment assignment = referenceFinder.assignment;
			if (assignment == null) {
				removeOccurences(false);
				error(document, offset, "Could not detect the assignment.");
				return new ArrayList<ReferenceFinder.Hit>();
			}
			if (assignment.getAssignmentType() != null && assignment.getAssignmentType() != Assignment_type.A_TYPE
					|| referenceFinder.fieldId == null || !assignment.shouldMarkOccurrences()) {
				removeOccurences(false);
				return new ArrayList<ReferenceFinder.Hit>();
			}

			result = referenceFinder.findReferencesInModule(module);
			if (referenceFinder.fieldId != null) {
				result.add(new Hit(referenceFinder.fieldId));
			}
		}

		return result;
	}

	/**
	 * Removes the occurrence annotations of this OccurrenceMarker.
	 * 
	 * @param force
	 *                The annotations will be removed even if the keepMarks
	 *                flag is set.
	 */
	public void removeOccurences(final boolean force) {
		if (!force && keepMarks.getValue()) {
			return;
		}
		markerJob.cancel();
		final IAnnotationModel annotationModel = getAnnotationModel();
		if (annotationModel == null) {
			return;
		}
		synchronized (getLockObject(annotationModel)) {
			if (occurrenceAnnotations == null) {
				return;
			}
			for (Annotation annotaion : occurrenceAnnotations) {
				annotationModel.removeAnnotation(annotaion);
			}
			occurrenceAnnotations = null;
		}
	}

	/**
	 * Returns the annotationModel of the editor or null if it can not be
	 * found
	 * 
	 * @return the annotationModel
	 */
	private IAnnotationModel getAnnotationModel() {
		final IEditorInput editorInput = editor.getEditorInput();
		if (editorInput == null) {
			if (reportDebugInformation.getValue()) {
				TITANDebugConsole.println("Mark occurrences failed: Can not find the file associated with the editor.");
			}
			return null;
		}
		final IDocumentProvider documentProvider = editor.getDocumentProvider();
		if (documentProvider == null) {
			// This can be null if the editor was closed
			// while the marker thread is waiting
			if (reportDebugInformation.getValue()) {
				TITANDebugConsole.println("Mark occurrences failed: The editor is closed.");
			}
			return null;
		}
		return documentProvider.getAnnotationModel(editorInput);
	}

	/** Returns the lock object for the given annotation model. */
	private Object getLockObject(final IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null) {
				return lock;
			}
		}
		return annotationModel;
	}

	/**
	 * Returns a reference parser according to the editor.
	 * 
	 * @return the reference parser
	 */
	protected abstract IReferenceParser getReferenceParser();
}
