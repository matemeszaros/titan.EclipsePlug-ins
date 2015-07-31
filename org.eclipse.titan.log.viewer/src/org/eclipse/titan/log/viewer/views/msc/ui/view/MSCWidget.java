/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.views.msc.ui.core.Frame;
import org.eclipse.titan.log.viewer.views.msc.ui.core.Lifeline;
import org.eclipse.titan.log.viewer.views.msc.ui.core.LifelineHeader;
import org.eclipse.titan.log.viewer.views.msc.ui.core.MSCNode;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * MSCWidget
 * 
 */
public class MSCWidget extends ScrollView implements ISelectionProvider, SelectionListener, DisposeListener {

	private DiagramToolTip toolTip = null;
	private Frame frame;
	private Image overView = null;
	private MSCNode dragAndDrop = null;
	private MSCNode currentGraphNode = null;
	private boolean zoomInMode = false;
	private boolean zoomOutMode = false;
	private boolean dragAndDropEnabled = false;
	private int dragX = 0;
	private int dragY = 0;
	private int dragAndDropOffsetX;
	private float zoomValue = 1;
	private Set<ISelectionChangedListener> registeredListeners = new HashSet<ISelectionChangedListener>();
	
	/**
	 * Constructor
	 *
	 * @param parent the parent composite
	 * @param style SWT style bits
	 */
	public MSCWidget(final Composite parent, final int style) {
		super(parent, style | SWT.NO_BACKGROUND, true);
		this.toolTip = new DiagramToolTip(getViewControl());
		super.addDisposeListener(this);

		getViewControl().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent e) {
				redraw();
			}

			@Override
			public void focusLost(final FocusEvent e) {
				redraw();
			}
		});
	}

	public boolean isNormalMode() {
		return !zoomInMode && !zoomOutMode;
	}

	/**
	 * Resize the contents to insure the frame fit into the view
	 * 
	 * @param frame the frame which will be drawn into the view
	 */
	public void resizeContents(final Frame frame) {
		int width = Math.round(frame.getWidth() * this.zoomValue);
		int height = Math.round(frame.getHeight() * this.zoomValue);
		resizeContents(width, height);
	}

	/**
	 * The frame to render (the sequence diagram)
	 * 
	 * @param theFrame the frame to display
	 */
	public void setFrame(final Frame theFrame, final boolean resetPosition) {
		this.frame = theFrame;
		if (resetPosition) {
			setContentsPos(0, 0);
			resizeContents(this.frame);
			redraw();
		}
		// Prepare the old overview to be reused
		if (this.overView != null) {
			this.overView.dispose();
		}
		this.overView = null;
		resizeContents(this.frame);
	}

	/**
	 * Returns the current Frame (the sequence diagram container)
	 * 
	 * @return the frame
	 */
	public Frame getFrame() {
		return this.frame;
	}

	@Override
	public boolean setContentsPos(final int x, final int y) {
		int tempX = x;
		if (x < 0) {
			tempX = 0;
		}
		int tempY = y;
		if (y < 0) {
			tempY = 0;
		}
		if (this.frame == null) {
			return false;
		}
		
		if (tempX + getVisibleWidth() > getContentsWidth()) {
			tempX = getContentsWidth() - getVisibleWidth();
		}
		
		if (tempY + getVisibleHeight() > getContentsHeight()) {
			tempY = getContentsHeight() - getVisibleHeight();
		}

		return super.setContentsPos(tempX, tempY);
	}

	@Override
	protected void contentsMouseHover(final MouseEvent event) {
		if (this.frame != null) {
			int x = Math.round(event.x / this.zoomValue);
			int y = Math.round(event.y / this.zoomValue);
			MSCNode graphNode = this.frame.getNodeAt(x, y);
			if (graphNode != null) {
				this.toolTip.showToolTip(graphNode.getName());
			} else {
				this.toolTip.hideToolTip();
			}
		}
	}

	@Override
	protected void contentsMouseMoveEvent(final MouseEvent e) {
		this.toolTip.hideToolTip();
		if (((e.stateMask & SWT.BUTTON_MASK) != 0)
			&& ((this.dragAndDrop != null) || this.dragAndDropEnabled)) {

			this.dragAndDropEnabled = false;
			if (this.currentGraphNode instanceof LifelineHeader) {
				this.dragAndDrop = this.currentGraphNode;
				((LifelineHeader) this.dragAndDrop).setDragAndDropMode(true);
			}
			if (this.dragAndDrop != null) {
				this.dragX = Math.round(e.x / this.zoomValue);
				this.dragY = Math.round(e.y / this.zoomValue);
				redraw();
			}
		} else {
			super.contentsMouseMoveEvent(e);
		}
	}

	@Override
	protected void contentsMouseUpEvent(final MouseEvent event) {
		this.toolTip.hideToolTip();
		if (this.dragAndDrop != null) {
			if ((this.overView != null) && !this.overView.isDisposed()) {
				this.overView.dispose();
			}
			this.overView = null;
			Lifeline node = this.frame.getCloserLifeline(this.dragX);
			
			if (node != null) {
				Lifeline currLifeline = ((LifelineHeader) this.dragAndDrop).getLifeline();
				int rx = Math.round(node.getX() * this.zoomValue);
				if ((rx <= event.x)	&& (Math.round(rx + (node.getWidth() * this.zoomValue)) >= event.x)) {
					// Do nothing
				} else {
					this.frame.moveLifeLineToPosition(currLifeline, node.getIndex());
				}
			}
			((LifelineHeader) this.dragAndDrop).setDragAndDropMode(false);
			this.dragAndDrop = null;
		}
		redraw();
		if (this.frame == null) {
			return;
		}
		super.contentsMouseUpEvent(event);
	}

	@Override
	protected void contentsMouseDownEvent(final MouseEvent event) {
		this.toolTip.hideToolTip();
		if (((this.zoomInMode) || (this.zoomOutMode)) && (event.button == 1)) {
			
			int cx = Math.round(event.x / this.zoomValue);
			int cy = Math.round(event.y / this.zoomValue);
			if (this.zoomInMode) {
				if (this.zoomValue < 64) {
					this.zoomValue = this.zoomValue * (float) 1.25;
				}
			} else {
					this.zoomValue = this.zoomValue / (float) 1.25;
			}
			
			int x = Math.round(cx * this.zoomValue - (float) getVisibleWidth() / 2);
			int y = Math.round(cy * this.zoomValue - (float) getVisibleHeight() / 2);
			
			int width = Math.round(this.frame.getWidth() * this.zoomValue);
			int height = Math.round(this.frame.getHeight() * this.zoomValue);
			resizeContents(width, height);

			setContentsPos(x, y);
			
			redraw();
		} else {
			
			this.dragAndDropEnabled = true;
			
			if (this.frame != null) {
				int oldSelectedLine = frame.getSelectedLine();

				int x = Math.round(event.x / this.zoomValue);
				int y = Math.round(event.y / this.zoomValue);
				MSCNode node = this.frame.getNodeAt(x, y);
				if (event.button == 1 || (node != null)) {
					this.currentGraphNode = node;
					if (node instanceof LifelineHeader) {
						this.dragAndDropOffsetX = node.getX() - x;
					}
				}
				int selectedLine = y / MSCConstants.ROW_HEIGHT - 1; // Header

				this.frame.setSelectedLine(selectedLine);
				if (frame.getSelectedLine() != oldSelectedLine) {
					fireSelectionChangeEvent();
					redraw();
				}
			}
		}
		if (this.dragAndDrop == null) {
			super.contentsMouseDownEvent(event);
		}
	}

	@Override
	protected void drawContents(final GC gc, final int clipx, final int clipy, final int clipw, final int cliph) {
		if (this.frame == null) {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(0, 0, getVisibleWidth(), getVisibleHeight());
			gc.dispose();
			return;
		}

		update();
		Rectangle area = getClientArea();
		Image dbuffer = new Image(getDisplay(), area.width, area.height);
		GC gcim = new GC(dbuffer);
		NGC context = new NGC(this, gcim);

		// Calculate font height and width
		MSCConstants.setFontHeight(context.getFontHeight((Font) Activator.getDefault().getCachedResource(MSCConstants.MSC_DEFAULT_FONT)));
		MSCConstants.setDefaultFontWidth(context.getFontWidth((Font) Activator.getDefault().getCachedResource(MSCConstants.MSC_DEFAULT_FONT)));
		MSCConstants.setBoldFontWidth(context.getFontWidth((Font) Activator.getDefault().getCachedResource(MSCConstants.MSC_BOLD_FONT)));

		int width = Math.round(this.frame.getWidth() * this.zoomValue);
		int height = Math.round(this.frame.getHeight() * this.zoomValue);
		resizeContents(width, height);

		context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));

		context.fillRectangle(0, 0, this.frame.getX(), getContentsHeight());
		context.fillRectangle(this.frame.getX() + this.frame.getWidth() + 1, 0,
				getContentsWidth()
				- (this.frame.getX() + this.frame.getWidth() + 1),
				getContentsHeight());
		context.fillRectangle(0,
				this.frame.getY() + this.frame.getHeight() + 1,
				getContentsWidth(), getContentsHeight()
				- (this.frame.getY() + this.frame.getHeight() + 1));
		gcim.setLineWidth(1);

		this.frame.draw(context);
		if (this.dragAndDrop instanceof LifelineHeader) {
			LifelineHeader node = (LifelineHeader) this.dragAndDrop;
			node.draw(context, this.dragX + this.dragAndDropOffsetX, this.dragY);
		}
		gc.drawImage(dbuffer, 0, 0, area.width, area.height, 0, 0, area.width, area.height);
		gcim.dispose();
		dbuffer.dispose();
		gc.dispose();
		context.dispose();
		setHScrollBarIncrement(Math.round(MSCConstants.COLUMN_WIDTH * this.zoomValue));
		setVScrollBarIncrement(Math.round(MSCConstants.ROW_HEIGHT * this.zoomValue));

		int xRatio = getContentsWidth() / getVisibleWidth();
		int yRatio = getContentsHeight() / getVisibleHeight();
		if (yRatio > xRatio) {
			this.overview.setOverviewSize((int) (getVisibleHeight() * 0.75));
		} else {
			this.overview.setOverviewSize((int) (getVisibleWidth() * 0.75));
		}
	}

	/**
	 * Returns the GraphNode overView the mouse if any
	 * 
	 * @return the GraphNode
	 */
	public MSCNode getMouseOverNode() {
		return this.currentGraphNode;
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {
		// Do nothing
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		redraw();
	}

	/**
	 * Enables zoom in mode
	 * @param value true to enable, false to disable 
	 */
	public void setZoomInMode(final boolean value) {
		if (value) {
			setZoomOutMode(false);
		}
		this.zoomInMode = value;
	}

	/**
	 * Enables zoom out mode
	 * @param value true to enable, false to disable 
	 */
	public void setZoomOutMode(final boolean value) {
		if (value) {
			setZoomInMode(false);
		}
		this.zoomOutMode = value;
	}

	/** 
	 * Gets the zoom factor
	 * @return the zoom factor
	 */
	public float getZoomFactor() {
		return this.zoomValue;
	}

	@Override
	public void widgetDisposed(final DisposeEvent e) {
		if (this.overView != null) {
			this.overView.dispose();
		}

		super.removeDisposeListener(this);
	}

	@Override
	protected void drawOverview(final GC gc, final Rectangle r) {
		float oldZoom = this.zoomValue;
		if (getContentsWidth() > getContentsHeight()) {
			this.zoomValue = (float) r.width / (float) getContentsWidth() * oldZoom;
		} else {
			this.zoomValue = (float) r.height / (float) getContentsHeight() * oldZoom;
		}
		if ((this.overView != null)
			&& ((r.width != this.overView.getBounds().width) || (r.height != this.overView.getBounds().height))) {
			this.overView.dispose();
			this.overView = null;
		}
		if (this.overView == null) {
			int backX = getContentsX();
			int backY = getContentsY();
			setContentsPos(0, 0);
			this.overView = new Image(getDisplay(), r.width, r.height);
			GC gcim = new GC(this.overView);
			NGC context = new NGC(this, gcim);
			context.setBackground((Color) Activator.getDefault().getCachedResource(MSCConstants.DEFAULT_BACKGROUND_COLOR));
			this.frame.draw(context);
			setContentsPos(backX, backY);
			gcim.dispose();
			context.dispose();
		}
		if ((this.overView != null)
			&& (r.width == this.overView.getBounds().width)
			&& (r.height == this.overView.getBounds().height)) {
			gc.drawImage(this.overView, 0, 0, r.width, r.height, 0, 0, r.width,	r.height);
		}		
		this.zoomValue = oldZoom;
		super.drawOverview(gc, r);
	}

	/**
	 * Resets the zoom factor 
	 */
	public void resetZoomFactor() {
		int currentX = Math.round(getContentsX() / this.zoomValue);
		int currentY = Math.round(getContentsY() / this.zoomValue);
		this.zoomValue = 1;
		redraw();
		update();
		setContentsPos(currentX, currentY);
	}

	@Override
	protected void upArrowPressed() {
		// If current selection is visible
		int oldSelection = this.frame.getSelectedLine();
		int currSelectionY = oldSelection * MSCConstants.ROW_HEIGHT;
		int visibleY = this.frame.getVisibleAreaY();
		int visibleHeight = this.frame.getVisibleAreaHeight();
		
		// visible
		int newSelection;
		if (currSelectionY >= visibleY && currSelectionY <= (visibleY + visibleHeight)) {
			newSelection = oldSelection - 1;
			if ((newSelection * MSCConstants.ROW_HEIGHT) < (this.frame.getVisibleAreaY())) {
				scrollBy(0, -MSCConstants.ROW_HEIGHT);
			}
		} else {
			newSelection = visibleY / MSCConstants.ROW_HEIGHT + 1;
		}
		
		this.frame.setSelectedLine(newSelection);
		if (frame.getSelectedLine() != oldSelection) {
			fireSelectionChangeEvent();
			redraw();
		}
	}
	
	@Override
	protected void downArrowPressed() {
		// If current selection is visible
		int oldSelection = this.frame.getSelectedLine();
		int currSelectionY = oldSelection * MSCConstants.ROW_HEIGHT;
		int visibleY = this.frame.getVisibleAreaY();
		int visibleHeight = this.frame.getVisibleAreaHeight();
		
		// visible
		int newSelection;
		if (currSelectionY >= visibleY && currSelectionY <= (visibleY + visibleHeight)) {
			newSelection = oldSelection + 1;
			if ((newSelection * MSCConstants.ROW_HEIGHT) >= (this.frame.getVisibleAreaY() + this.frame.getVisibleAreaHeight() - 2 * MSCConstants.ROW_HEIGHT)) { // Header compensation
				scrollBy(0, MSCConstants.ROW_HEIGHT);
			}
		} else {
			newSelection = (visibleY + visibleHeight) / MSCConstants.ROW_HEIGHT - 2;
		}
		
		this.frame.setSelectedLine(newSelection);
		if (frame.getSelectedLine() != oldSelection) {
			fireSelectionChangeEvent();
			redraw();
		}
	}
	
	@Override
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		this.registeredListeners.add(listener);
	}


	@Override
	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		this.registeredListeners.remove(listener);
	}

	@Override
	public void setSelection(final ISelection selection) {
		if (!(selection instanceof StructuredSelection)) {
			return;
		}

		int oldSelectedLine = frame.getSelectedLine();
		int selectedLine = (Integer) ((StructuredSelection) selection).getFirstElement();
		this.frame.setSelectedLine(selectedLine);

		int y = selectedLine * MSCConstants.ROW_HEIGHT + MSCConstants.ROW_HEIGHT;
		int visY = this.frame.getVisibleAreaY();
		int visHeight = this.frame.getVisibleAreaHeight();
		int scroll = 0;

		// First time and top -> do not scroll
		if ((this.frame.getVisibleAreaHeight() == 0) && (selectedLine == 0)) {
			scroll = 0;
		} else if (y >= (visY + visHeight)) {
			// Scroll down
			// First time
			if (visHeight == 0) {
				scroll = y - 2 * MSCConstants.ROW_HEIGHT;
			} else {
				scroll = y - (visY + visHeight) + 2 * MSCConstants.ROW_HEIGHT;
			}
		} else if (y <= this.frame.getVisibleAreaY()) {
			// Scroll up
			scroll = y - visY - 2 * MSCConstants.ROW_HEIGHT;
		}
		scrollBy(0, scroll);

		// Notify listeners of new selection
		if (frame.getSelectedLine() != oldSelectedLine) {
			fireSelectionChangeEvent();
			redraw();
		}

	}

	@Override
	public ISelection getSelection() {
		return new StructuredSelection(this.frame.getSelectedLine());
	}
	
	private void fireSelectionChangeEvent() {
		final List<ISelectionChangedListener> savedListeners = new ArrayList<ISelectionChangedListener>(registeredListeners);
		for (ISelectionChangedListener listener : savedListeners) {
			listener.selectionChanged(new SelectionChangedEvent(this, getSelection()));
		}
	}
	
	@Override
	protected void keyPressedEvent(final KeyEvent e) {
		// Clear tool tip
		this.toolTip.hideToolTip();
		super.keyPressedEvent(e);
	}
	
	@Override
	protected void verticalScrollBarEvent(final SelectionEvent e) {
		// Clear tool tip
		this.toolTip.hideToolTip();
	}
}
