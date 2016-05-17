package org.eclipse.titan.designer.editors.ttcn3editor;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.editors.IReferenceParser;
import org.eclipse.titan.designer.editors.OccurencesMarker;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Kristof Szabados
 * */
public class TTCN3OccurrenceMarker extends OccurencesMarker {
	private final IReferenceParser referenceParser = new TTCN3ReferenceParser(false);

	public TTCN3OccurrenceMarker(final ITextEditor editor) {
		super(editor);
	}

	@Override
	protected IReferenceParser getReferenceParser() {
		return referenceParser;
	}

	@Override
	protected List<Hit> findOccurrences(final IDocument document, final Reference reference, final Module module, final int offset) {
		return findOccurrencesLocationBased(module, offset);
	}
}
