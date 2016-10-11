/*******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.views.msc.ui.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;

public interface DelayedSelectable extends ISelectionChangedListener {
	ISelection getDelayedSelection();

	void setDelayedSelection(ISelection delayedSelection);

	void selectionChanged(ISelection selection);

	public void run();
}
