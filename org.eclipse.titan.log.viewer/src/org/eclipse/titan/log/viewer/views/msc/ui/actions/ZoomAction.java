/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.views.MSCView;
import org.eclipse.titan.log.viewer.views.msc.ui.view.MSCWidget;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;

/**
 * Zoom action
 *
 */
public class ZoomAction extends Action {

	private MSCWidget viewer = null;
	private MSCView view = null;
	private boolean lastZoomIn = false;
	private Cursor zoomIn = null;
	private Cursor zoomOut = null;

	public ZoomAction(final IViewPart view) {
		super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		this.view = (MSCView) view;
		this.viewer = (this.view).getMSCWidget();
		this.zoomIn = new Cursor(Display.getCurrent(), Activator.getImageDescriptor(MSCConstants.ICON_ZOOM_IN_SOURCE).getImageData(),
				Activator.getImageDescriptor(MSCConstants.ICON_ZOOM_MASK).getImageData(), 0, 0);
		this.zoomOut = new Cursor(Display.getCurrent(), Activator.getImageDescriptor(MSCConstants.ICON_ZOOM_OUT_SOURCE).getImageData(),
				Activator.getImageDescriptor(MSCConstants.ICON_ZOOM_MASK).getImageData(), 0, 0);
	}

	@Override
	public void run() {
		
		// Zoom in
		if (getId().equals(MSCConstants.ID_ZOOM_OUT)) {
			this.viewer.setZoomOutMode(isChecked());
			if (isChecked()) {
				this.viewer.setCursor(this.zoomOut);
				setActionChecked(MSCConstants.ID_NO_ZOOM, false);
			} else {
				this.viewer.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_ARROW));
				setActionChecked(MSCConstants.ID_NO_ZOOM, true);
			}
		// Zoom out
		} else if (getId().equals(MSCConstants.ID_ZOOM_IN)) {
			if (this.lastZoomIn == isChecked()) {
				setChecked(!isChecked());
			}
			this.viewer.setZoomInMode(isChecked());
			this.lastZoomIn = isChecked();
			if (isChecked())  {
				this.viewer.setCursor(this.zoomIn);
				setActionChecked(MSCConstants.ID_NO_ZOOM, false);
			} else {
				this.viewer.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_ARROW));
				setActionChecked(MSCConstants.ID_NO_ZOOM, true);
			}
		// Reset zoom
		} else if (getId().equals(MSCConstants.ID_RESET_ZOOM)) {
			this.viewer.resetZoomFactor();
			// The reset action is a radio button only to un-check the zoom in and out button
			// when it is clicked. This avoid adding code to do it manually
			// We only have to force it to false every time
			setChecked(false);
			setActionChecked(MSCConstants.ID_NO_ZOOM, true);
		// No zoom
		} else if (getId().equals(MSCConstants.ID_NO_ZOOM)) {
			setChecked(true);
			this.viewer.setZoomInMode(false);
			this.viewer.setZoomOutMode(false);
			this.viewer.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_ARROW));
		}
	}
	
	public void setActionChecked(final String id, final boolean checked)	{
		IActionBars bar = this.view.getViewSite().getActionBars();
		if (bar == null) {
			return;
		}
		IToolBarManager barManager = bar.getToolBarManager();
		if (barManager == null) {
			return;
		}
		IContributionItem nextPage = barManager.find(id);
		if ((nextPage != null) && (nextPage instanceof ActionContributionItem)) {
			IAction action = ((ActionContributionItem) nextPage).getAction();
			if (action != null) {
				action.setChecked(checked);
			}
		}
	}
}
