/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;


import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.titan.designer.editors.DocumentTracker;
import org.eclipse.titan.designer.editors.GlobalIntervalHandler;


/**
 * @author Kristof Szabados
 * */
public final class DocumentSetupParticipant implements IDocumentSetupParticipant {
	private final ASN1Editor editor;

	public DocumentSetupParticipant(final ASN1Editor editor) {
		this.editor = editor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.
	 * eclipse.jface.text.IDocument)
	 */
	@Override
	public void setup(final IDocument document) {
		DocumentTracker.put((IFile) editor.getEditorInput().getAdapter(IFile.class), document);

		IDocumentPartitioner partitioner = new FastPartitioner(new PartitionScanner(), PartitionScanner.PARTITION_TYPES);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(PartitionScanner.ASN1_PARTITIONING, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
		partitioner.connect(document);

		document.addDocumentListener(new IDocumentListener() {

			@Override
			public void documentAboutToBeChanged(final DocumentEvent event) {
				GlobalIntervalHandler.putInterval(event.getDocument(), null);
			}

			@Override
			public void documentChanged(final DocumentEvent event) {
				//Do nothing
			}

		});
	}
}
