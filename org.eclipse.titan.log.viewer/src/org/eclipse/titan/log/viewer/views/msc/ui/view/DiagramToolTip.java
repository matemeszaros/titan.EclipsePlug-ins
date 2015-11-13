/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * DiagramToolTip - custom tooltip for Message Sequence Chart
 *
 */
public class DiagramToolTip implements PaintListener  {

	private Control parent = null;
	private Shell toolTipShell = null;
	private String text = null;

	/**
	 * Constructor
	 * 
	 * @param parent the parent control
	 */
	public DiagramToolTip(final Control parent) {
		this.parent = parent;
		this.toolTipShell = new Shell(this.parent.getShell(), SWT.ON_TOP | SWT.NO_FOCUS);
		this.toolTipShell.setLayout(new RowLayout());
		this.toolTipShell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		this.toolTipShell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		this.toolTipShell.addPaintListener(this);
		this.toolTipShell.setSize(10, 10);
	}
	
	/**
	 * Display the tool tip using the given text
	 * The tool tip will stay on screen until it is told otherwise
	 * 
	 * @param value the text to display
	 */
	public void showToolTip(final String value) {
		if ((value == null) || value.isEmpty()) { //$NON-NLS-1$
			this.toolTipShell.setVisible(false);
			return;
		}
		this.text = value;
		int w = this.toolTipShell.getBounds().width;
		Point hr = Display.getDefault().getCursorLocation();
		int cursorH = 32;
		for (int i = 0; i < Display.getDefault().getCursorSizes().length; i++) {
			if (Display.getDefault().getCursorSizes()[i].y < cursorH) {
				cursorH = Display.getDefault().getCursorSizes()[i].y;
			}
		}	
		if (hr.x + w > Display.getDefault().getBounds().width) {
			int tempX = (hr.x + w) - Display.getDefault().getBounds().width;
			if (tempX > Display.getDefault().getBounds().width) {
				hr.x = 0;
			}
			hr.x = hr.x - tempX;
		}
		this.toolTipShell.setLocation(hr.x, hr.y + cursorH);
		this.toolTipShell.setVisible(true);
	}
	
	
	/**
	 * Hide the tool tip
	 */
	public void hideToolTip() {
		this.toolTipShell.setVisible(false);
	}
	
	/**
	 * Draw the tool tip text on the control widget when a paint event is received
	 */
	@Override
	public void paintControl(final PaintEvent event) {
		Point size = event.gc.textExtent(this.text);
		event.gc.drawText(this.text, 2, 0, true);
		this.toolTipShell.setSize(size.x + 6, size.y + 2);
	}
}
