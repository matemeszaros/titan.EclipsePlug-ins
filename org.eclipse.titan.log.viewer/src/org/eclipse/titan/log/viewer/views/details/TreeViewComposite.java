/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.details;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewSite;

public class TreeViewComposite extends Composite {

	private TreeViewer viewer;
	private DetailData currentData;
	

	public TreeViewComposite(final Composite parent, final IViewSite site) {
		super(parent, SWT.NO_BACKGROUND);
		this.viewer = new TreeViewer(this, SWT.H_SCROLL | SWT.V_SCROLL);
		this.viewer.setContentProvider(new DetailsViewContentProvider());
		this.viewer.setLabelProvider(new DetailsViewLabelProvider());
		this.viewer.setInput(site);
		this.currentData = null;		
	}
	
	/**
	 * Returns the viewer
	 * @return the viewer
	 */
	public TreeViewer getViewer() {
		return this.viewer;
	}

	/**
	 * Sets new input in the viewer
	 * @param oldInput the old input
	 * @param newInput the new input
	 */
	public void inputChanged(final DetailData newInput) {
	    String oldMessage = null;
		if (newInput == null) {
			// Clear tree view
			this.viewer.getContentProvider().inputChanged(this.viewer, null, null);
			this.currentData = null;
			return;
		}
		if ((this.currentData != null) && this.currentData.isEqualTo(newInput)) {
			return;
		}
	   
		this.currentData = newInput;
		String message = this.currentData.getLine();
		if (message.contains("{") && (message.indexOf("{") != 0)) {
			oldMessage = message;
			message = "{ " + message + " }";
			this.currentData.setLine(message);
		}
			
		this.viewer.getContentProvider().inputChanged(this.viewer, newInput, newInput);
		
		Tree tree = this.viewer.getTree();
		 tree.setVisible(false);
		if (tree.getItemCount() > 0) {
			this.viewer.setSelection(new StructuredSelection(tree.getItem(0).getData()));
		}
		if (oldMessage != null) {
			this.currentData.setLine(oldMessage);
		}
		
		this.viewer.expandAll();
		 tree.setVisible(true);
	}
}
