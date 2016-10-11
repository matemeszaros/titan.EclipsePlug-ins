/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.titan.designer.editors.BaseTextHover;
import org.eclipse.titan.designer.editors.IReferenceParser;
import org.eclipse.ui.IEditorPart;

/**
 * @author Kristof Szabados
 * */
public final class TextHover extends BaseTextHover {
	private final ISourceViewer sourceViewer;
	private final IEditorPart editor;

	public TextHover(final ISourceViewer sourceViewer, final IEditorPart editor) {
		this.sourceViewer = sourceViewer;
		this.editor = editor;
	}

	@Override
	protected ISourceViewer getSourceViewer() {
		return sourceViewer;
	}

	@Override
	protected IEditorPart getTargetEditor() {
		return editor;
	}

	@Override
	protected IReferenceParser getReferenceParser() {
		return new TTCN3ReferenceParser(false);
	}
}
