/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

import org.eclipse.titan.log.viewer.views.MSCView;
import org.eclipse.titan.log.viewer.views.msc.model.ExecutionModel;
import org.eclipse.titan.log.viewer.views.msc.ui.view.MSCWidget;

/**
 * Jumps to previous setverdict
 *
 */
public class JumpToNextSetverdictAction extends SelectionProviderAction {

	private IStructuredSelection selection;
	private MSCView view = null;
	private MSCWidget widget = null;

	public JumpToNextSetverdictAction(final MSCView view) {
		super(view.getMSCWidget(), "");
		this.view = view;
		this.widget = view.getMSCWidget();
	}
	
	@Override
	public void run() {
		if (this.widget == null) {
			return;
		}
		ExecutionModel model = this.view.getModel();
		if (model == null) {
			return;
		}
		int[] setverdictPlaces = model.getSetverdict(); 
		int selectedLine = (Integer) this.selection.getFirstElement();
		selectSetVerdict(setverdictPlaces, selectedLine);
	}

	private void selectSetVerdict(int[] setverdictPlaces, int selectedLine) {
		for (int setverdictPlace : setverdictPlaces) {
			if (setverdictPlace > selectedLine) {
				this.widget.setSelection(new StructuredSelection(setverdictPlace));
				return;
			}
		}
	}

	@Override
	public void selectionChanged(final IStructuredSelection selection) {
		this.selection = selection;
		int selectedLine = (Integer) this.selection.getFirstElement();
		
		boolean enable = false;

		ExecutionModel model = this.view.getModel();
		if (model == null) {
			setEnabled(false);
			return;
		}
		
		for (int j = 0; j < model.getSetverdict().length; j++) {
			if (model.getSetverdict()[j] > selectedLine) {
				enable = true;
				break;
			}
		}
		setEnabled(enable);
	}
}
