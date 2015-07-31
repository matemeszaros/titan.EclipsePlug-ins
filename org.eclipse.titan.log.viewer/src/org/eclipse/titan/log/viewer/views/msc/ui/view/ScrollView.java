/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.view;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ScrollBar;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.Messages;

/**
 * ScrollView widget provides a scrolling area with on-demand scroll bars.
 * 
 */
public class ScrollView extends Composite {
	
	// Scroll bar modes, default is AUTO
	public static final int AUTO = 0;
	public static final int ALWAYS_OFF = 2;
	public static final int ALWAYS_ON = 1;

	private boolean autoScrollEnabled = true;
	private int autoScrollPeriod = 75;
	private int hScrollbarMode = AUTO;
	private int vScrollbarMode = AUTO;
	private int hScrollbarIncrement = 10;
	private int vScrollbarIncrement = 10;

	protected int contentsHeight = 0;
	protected int contentsWidth = 0;
	protected int contentsX = 0;
	protected int contentsY = 0;

	// Canvas for vertical/horizontal SB only
	private Canvas vertsb, horzsb;
	protected Canvas viewcontrol;

	// Control used in the bottom right corner
	protected Control cornerControl;
	protected Overview overview;

	/** Timer for autoScroll feature */
	private AutoScroll autoScroll = null;

	/** TimerTask for autoScroll feature !=null when auto scroll is running */
	private Timer autoScrollTimer = null;

	/**
	 * Create a ScrollView, child of composite c.
	 * Both scroll bar have the mode AUTO.
	 * Auto scroll feature is enabled using a delay of 250ms.
	 * Overview feature is not enabled by default (use setOverviewEnabled()).
	 * @param c the composite to create the ScrollView on
	 * @param style SWT style bits @see SWT
	 * @param mouseWheel force scrollView to handles mouse wheel
	 */
	public ScrollView(final Composite c, final int style, final boolean mouseWheel) {
		super(c, SWT.NONE);  

		this.horzsb = new Canvas(this, SWT.H_SCROLL | SWT.DOUBLE_BUFFERED);
		if (mouseWheel) {
			// force scroll bar to get mouse wheel, those scrollbar will be hidden
			this.viewcontrol = new Canvas(this, style | SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		} else {
			this.viewcontrol = new Canvas(this, style | SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		}
		this.viewcontrol.setBackground(getBackground());
		// hide scroll bar as their are replaced by vertsb_ and horzsb_.
		if (mouseWheel) {
			this.viewcontrol.getVerticalBar().setVisible(false);
			this.viewcontrol.getHorizontalBar().setVisible(false);
		}
		this.vertsb = new Canvas(this, SWT.V_SCROLL | SWT.DOUBLE_BUFFERED);
		// make vertsb_ able to receive mouse wheel
		// does not help as we can't set a MouseListener on vertsb_.getVerticalBar()
		// to set focus on viewcontrol_
		setLayout(new SVLayout());

		// Create overview
		Button b = new Button(this, SWT.NONE);
		b.setImage(Activator.getDefault().getIcon(Constants.ICONS_OVERVIEW));
		this.overview = new Overview(this);
		this.overview.useControl(b);
		b.setData(this.overview);
		Control cc = b;
		b.setToolTipText(Messages.getString("ScrollView.0"));  //$NON-NLS-1$
		setCornerControl(cc);
		
		// Create all listeners
		initListerners();
	}

	/**
	 * Initialize all listeners 
	 */
	private void initListerners() {
		PaintListener localPaintListener = new PaintListener() {
			@Override
			public void paintControl(final PaintEvent event) {
				// use clipping, to reduce cost of paint.
				Rectangle r = event.gc.getClipping();
				int cx = viewToContentsX(r.x);
				int cy = viewToContentsY(r.y);
				drawContents(event.gc, cx, cy, r.width, r.height);
			}
		};
		
		this.viewcontrol.addPaintListener(localPaintListener);

		MouseMoveListener localMouseMoveListener = new MouseMoveListener() {
			@Override
			public void mouseMove(final MouseEvent e) {
				int ox = e.x, oy = e.y;
				e.x = viewToContentsX(e.x);
				e.y = viewToContentsY(e.y);
				contentsMouseMoveEvent(e);
				e.x = ox;
				e.y = oy;
			}
		};

		this.viewcontrol.addMouseMoveListener(localMouseMoveListener);
		
		this.viewcontrol.getVerticalBar().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				verticalScrollBarEvent(e);
			}
		});

		MouseTrackListener localMouseTrackListener = new MouseTrackListener() {
			@Override
			public void mouseEnter(final MouseEvent e) {
				int ox = e.x, oy = e.y;
				e.x = viewToContentsX(e.x);
				e.y = viewToContentsY(e.y);
				contentsMouseEnter(e);
				e.x = ox;
				e.y = oy;
			}

			@Override
			public void mouseHover(final MouseEvent e) {
				int ox = e.x, oy = e.y;
				e.x = viewToContentsX(e.x);
				e.y = viewToContentsY(e.y);
				contentsMouseHover(e);
				e.x = ox;
				e.y = oy;
			}

			@Override
			public void mouseExit(final MouseEvent e) {
				int ox = e.x, oy = e.y;
				e.x = viewToContentsX(e.x);
				e.y = viewToContentsY(e.y);
				contentsMouseExit(e);
				e.x = ox;
				e.y = oy;
			}

		};

		this.viewcontrol.addMouseTrackListener(localMouseTrackListener);

		MouseListener localMouseListener = new MouseListener() {
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				int ox = e.x, oy = e.y;
				e.x = viewToContentsX(e.x);
				e.y = viewToContentsY(e.y);
				contentsMouseDoubleClickEvent(e);
				e.x = ox;
				e.y = oy;
				// Notify listeners
				Event event = new Event();
				notifyListeners(SWT.MouseDoubleClick, event);
			}

			@Override
			public void mouseDown(final MouseEvent e) {
				int ox = e.x, oy = e.y;
				ScrollView.this.mouseDownX = viewToContentsX(e.x);
				e.x = ScrollView.this.mouseDownX;
				ScrollView.this.mouseDownY = viewToContentsY(e.y);
				e.y = ScrollView.this.mouseDownY;
				contentsMouseDownEvent(e);
				e.x = ox;
				e.y = oy;
			}

			@Override
			public void mouseUp(final MouseEvent e) {
				int ox = e.x, oy = e.y;
				e.x = viewToContentsX(e.x);
				e.y = viewToContentsY(e.y);
				contentsMouseUpEvent(e);
				e.x = ox;
				e.y = oy;
				ScrollView.this.mouseDownY = -1;
				ScrollView.this.mouseDownX = -1;
			}
		};
		this.viewcontrol.addMouseListener(localMouseListener);

		KeyListener localKeyListener = new KeyListener() {
			@Override
			public void keyPressed(final KeyEvent e) {
				keyPressedEvent(e);
			}

			@Override
			public void keyReleased(final KeyEvent e) {
				keyReleasedEvent(e);
			}
		};

		this.viewcontrol.addKeyListener(localKeyListener);
		
		getVerticalBar().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				setContentsPos(ScrollView.this.contentsX, getVerticalBar().getSelection());
				// need to change "hidden" vertical bar value ?
				// force focus on viewcontrol_ so we got future mouse wheel's scroll events
				if (!ScrollView.this.viewcontrol.isFocusControl()) {
					ScrollView.this.viewcontrol.setFocus();
				}
			}
		});

		if (this.viewcontrol.getVerticalBar() != null) {
			// add view control hidden scrollbar listener to get mouse wheel ...
			this.viewcontrol.getVerticalBar().addSelectionListener(
					new SelectionAdapter() {
						@Override
						public void widgetSelected(final SelectionEvent e) {
							ScrollBar b = ScrollView.this.viewcontrol.getVerticalBar();
							setContentsPos(ScrollView.this.contentsX, b.getSelection());
							// change "real" vertical bar selection too
							getVerticalBar().setSelection(b.getSelection());
						}
					});
		}
		getHorizontalBar().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				setContentsPos(getHorizontalBar().getSelection(), ScrollView.this.contentsY);
				// need to change "real" horizontal bar too ?
				// force focus on viewcontrol_ so we got future mouse wheel's scroll events
				if (!ScrollView.this.viewcontrol.isFocusControl()) {
					ScrollView.this.viewcontrol.setFocus();
				}
			}
		});
		if (this.viewcontrol.getHorizontalBar() != null) {
			this.viewcontrol.getHorizontalBar().addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						ScrollBar b = ScrollView.this.viewcontrol
								.getHorizontalBar();
						setContentsPos(b.getSelection(),
								ScrollView.this.contentsY);
						//change "real" vertical bar selection too
						getHorizontalBar().setSelection(b.getSelection());
					}
				});
		}
	}
	
	/**
	 * Only for override use to be able to fetch scroll events
	 * @param e the event
	 */
	protected void verticalScrollBarEvent(final SelectionEvent e) {
		// Do nothing
	}

	@Override
	public boolean setFocus() {
		return !(viewcontrol == null || viewcontrol.isDisposed())
				&& viewcontrol.forceFocus();
	}

	@Override
	public void setCursor(final Cursor cursor) {
		if (viewcontrol != null) {
			viewcontrol.setCursor(cursor);
		}
	}

	@Override
	public void dispose() {
		if (this.autoScroll != null) {
			this.autoScroll.cancel();
			this.autoScroll = null;
		}
		if (this.viewcontrol != null) {
			this.viewcontrol.dispose();
		}
		this.viewcontrol = null;
		if (this.vertsb != null) {
			this.vertsb.dispose();
		}
		this.vertsb = null;
		if (this.horzsb != null) {
			this.horzsb.dispose();
		}
		this.horzsb = null;
		if (this.cornerControl != null) {
			Object data = this.cornerControl.getData();
			if (data instanceof Overview) {
				((Overview) data).dispose();
			}
			this.cornerControl.dispose();
			this.cornerControl = null;
		}
		super.dispose();
	}

	@Override
	public Rectangle getClientArea() {
		return this.viewcontrol.getClientArea();
	}

	@Override
	public void setBackground(final Color c) {
		super.setBackground(c);
		this.viewcontrol.setBackground(c);
	}

	@Override
	public void setToolTipText(final String text) {
		this.viewcontrol.setToolTipText(text);
	}

	/** 
	 * Draw overview area, @see setOverviewEnabled.
	 * By default draw a rectangle corresponding to the visible area
	 * of scrollview.
	 * You can redefine this method to draw the contents as drawContents does...
	 * ...in an other magnify factor.
	 * @param gc GC to used to draw.
	 * @param r Rectangle corresponding to the client area of overview.
	 */
	protected void drawOverview(final GC gc, final Rectangle r) {
		int x = (int) (r.width * this.contentsX / (float) this.contentsWidth);
		int y = (int) (r.height * this.contentsY / (float) this.contentsHeight);
		int vw = getVisibleWidth();
		int vh = getVisibleHeight();
		int w = r.width - 1;
		if (this.contentsWidth > vw) {
			w = (int) (r.width * vw / (float) this.contentsWidth);
		}
		int h = r.height - 1;
		if (this.contentsHeight > vh) {
			h = (int) (r.height * vh / (float) this.contentsHeight);
		}

		gc.setForeground(getForeground());
		//too small rectangle ?
		if ((w < 5) || (h < 5)) {
			//use a cross ...
			gc.drawLine(x, 0, x, r.height);
			gc.drawLine(0, y, r.width, y);
		} else {
			gc.drawRectangle(x, y, w, h);
		}
	}

	/**
	 * Access method for the contentsHeight property.
	 * 
	 * @return  the current value of the contentsHeight property
	 */
	public int getContentsHeight() {
		return this.contentsHeight;
	}

	/**
	 * Access method for the contentsWidth property.
	 * 
	 * @return   the current value of the contentsWidth property
	 */
	public int getContentsWidth() {
		return this.contentsWidth;
	}

	/**
	 * Access method for the contentsX property.
	 * 
	 * @return   the current value of the contentsX property
	 */
	public int getContentsX() {
		return this.contentsX;
	}

	/**
	 * Access method for the contentsY property.
	 * 
	 * @return   the current value of the contentsY property
	 */
	public int getContentsY() {
		return this.contentsY;
	}

	/**
	 * Determines if the dragAutoScroll property is true.
	 * 
	 * @return <code>true<code> if the dragAutoScroll property is true
	 */
	public boolean getDragAutoScroll() {
		return this.autoScrollEnabled;
	}

	/**
	 * Sets the value of the dragAutoScroll property.
	 * 
	 * @param aDragAutoScroll the new value of the dragAutoScroll property
	 */
	public void setDragAutoScroll(final boolean aDragAutoScroll) {
		this.autoScrollEnabled = aDragAutoScroll;
		if (!this.autoScrollEnabled && (this.autoScroll != null)) {
			this.autoScroll.cancel();
			this.autoScroll = null;
		}
	}

	/**
	 * Change delay (in millisec) used for auto scroll feature.
	 * @param period new period between to auto scroll
	 */
	public void setDragAutoScrollPeriod(final int period) {
		this.autoScrollPeriod = Math.max(0, period);
	}

	/**
	 * Return auto scroll period.
	 */
	public int getDragAutoScrollPeriod() {
		return this.autoScrollPeriod;
	}

	/**
	 * Access method for the hScrollBarMode property.
	 * 
	 * @return   the current value of the hScrollBarMode property
	 */
	public int getHScrollBarMode() {
		return this.hScrollbarMode;
	}

	/**
	 * Sets the value of the hScrollBarMode property.
	 * 
	 * @param aHScrollBarMode the new value of the hScrollBarMode property
	 */
	public void setHScrollBarMode(final int aHScrollBarMode) {
		this.hScrollbarMode = aHScrollBarMode;
	}

	/**
	 * Access method for the vScrollBarMode property.
	 * 
	 * @return   the current value of the vScrollBarMode property
	 */
	public int getVScrollBarMode() {
		return this.vScrollbarMode;
	}

	/**
	 * Sets the value of the vScrollBarMode property.
	 * 
	 * @param aVScrollBarMode the new value of the vScrollBarMode property
	 */
	public void setVScrollBarMode(final int aVScrollBarMode) {
		this.vScrollbarMode = aVScrollBarMode;
	}

	/**
	 * Return horizontal scroll bar increment, default:1
	 */
	public int getHScrollBarIncrement() {
		return this.hScrollbarIncrement;
	}

	/**
	 * Return vertical scroll bar increment, default:1
	 */
	public int getVScrollBarIncrement() {
		return this.vScrollbarIncrement;
	}

	/**
	 * Change horizontal scroll bar increment, minimum:1.
	 * Page increment is always set to visible width.
	 */
	public void setHScrollBarIncrement(final int inc) {
		this.hScrollbarIncrement = Math.max(1, inc);
	}

	/**
	 * Change vertical scroll bar increment, minimum:1.
	 * Page increment is always set to visible height.
	 */
	public void setVScrollBarIncrement(final int inc) {
		this.vScrollbarIncrement = Math.max(1, inc);
	}

	/**
	 * @return control used to display view (might not be this object).
	 * use this control to add/remove listener on the draw area
	 */
	public Control getViewControl() {
		return this.viewcontrol;
	}

	/**
	 * Called when the mouse enter the ScrollView area
	 * @param e
	 */
	protected void contentsMouseExit(final MouseEvent e) {
		// Do nothing
	}

	/**
	 * Called when the mouse enter the ScrollView area after
	 * and system definied time
	 * @param e
	 */
	protected void contentsMouseHover(final MouseEvent e) {
		// Do nothing
	}

	/**
	 * Called when the mouse enter the ScrollView area
	 * @param e
	 */
	protected void contentsMouseEnter(final MouseEvent e) {
		// Do nothing
	}

	/**
	 * Called when user double on contents area.
	 */
	protected void contentsMouseDoubleClickEvent(final MouseEvent e) {
		// Do nothing
	}

	/**
	 * Called when mouse is on contents area and button is pressed.
	 * @param e
	 */
	protected void contentsMouseDownEvent(final MouseEvent e) {
		this.mouseDownX = e.x;
		this.mouseDownY = e.y;
	}

	/** where mouse down appear on contents area */
	private int mouseDownX = -1, mouseDownY = -1;

	/** TimerTask for auto scroll feature. */
	private static class AutoScroll extends TimerTask {
		private int dx, dy;

		private ScrollView sv;

		public AutoScroll(final ScrollView sv, final int dx, final int dy) {
			this.sv = sv;
			this.dx = dx;
			this.dy = dy;
		}

		@Override
		public void run() {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					AutoScroll.this.sv.scrollBy(AutoScroll.this.dx,
							AutoScroll.this.dy);
				}
			});
		}
	}

	/**
	 * Called when mouse is on contents area and mode.
	 * @param event
	 */
	protected void contentsMouseMoveEvent(final MouseEvent event) {
		if ((event.stateMask & SWT.BUTTON_MASK) != 0) {
			if (!this.autoScrollEnabled) {
				scrollBy(-(event.x - this.mouseDownX),
						-(event.y - this.mouseDownY));
				return;
			}

			int sx = 0, sy = 0;

			int vRight = getContentsX() + getVisibleWidth();
			int vBottom = getContentsY() + getVisibleHeight();

			//auto scroll... ?
			if (event.x < getContentsX()) {
				sx = (getContentsX() - event.x);
				this.mouseDownX = getContentsX();
			} else if (event.x > vRight) {
				sx = -event.x + vRight;
				this.mouseDownX = vRight;
			}
			if (event.y < getContentsY()) {
				sy = (getContentsY() - event.y);
				this.mouseDownY = getContentsY();
			} else if (event.y > vBottom) {
				sy = -event.y + vBottom;
				this.mouseDownY = vBottom;
			}

			if ((sx != 0) || (sy != 0)) {
				//start auto scroll...
				if (this.autoScroll == null) {
					if (this.autoScrollTimer == null) {
						this.autoScrollTimer = new Timer(true);
					}
					this.autoScroll = new AutoScroll(this, sx, sy);
					this.autoScrollTimer.schedule(this.autoScroll, 0,
							this.autoScrollPeriod);
				} else {
					this.autoScroll.dx = sx;
					this.autoScroll.dy = sy;
				}
			} else {
				if (this.autoScroll != null) {
					this.autoScroll.cancel();
					this.autoScroll = null;
				}

				scrollBy(-(event.x - this.mouseDownX),
						-(event.y - this.mouseDownY));
			}
		}
	}

	/**
	 * Called when mouse is on contents area and button is released
	 * @param event
	 */
	protected void contentsMouseUpEvent(final MouseEvent event) {
		//reset auto scroll if it's engaged  
		if (this.autoScroll != null) {
			this.autoScroll.cancel();
			this.autoScroll = null;
		}
	}

	/**
	 * Responsible to draw contents area.  At least rectangle clipX must be redrawn.
	 * This rectangle is given in contents coordinates.
	 * By default, no paint is produced.
	 * @param gc
	 * @param clipx
	 * @param clipy
	 * @param clipw
	 * @param cliph
	 */
	protected void drawContents(final GC gc, final int clipx, final int clipy, final int clipw, final int cliph) {
		// Do nothing
	}

	/**
	 * Change the size of the contents area.
	 * @param w new width of the area.
	 * @param h new height of the area.
	 */
	public void resizeContents(final int w, final int h) {
		int tempW = w;
		int tempH = h;
		if (w < 0) {
			tempW = 0;
		}
		if (h < 0) {
			tempH = 0;
		}

		int oldW = this.contentsWidth;
		int oldH = this.contentsHeight;

		if ((tempW == oldW) && (tempH == oldH)) {
			return;
		}

		this.contentsWidth = tempW;
		this.contentsHeight = tempH;

		if (oldW > tempW) {
			int s = tempW;
			tempW = oldW;
			oldW = s;
		}

		int visWidth = getVisibleWidth();
		int visHeight = getVisibleHeight();
		if (oldW < visWidth) {
			if (tempW > visWidth) {
				tempW = visWidth;
			}
			this.viewcontrol.redraw(getContentsX() + oldW, 0, tempW - oldW,
					visHeight, true);
		}

		if (oldH > tempH) {
			int s = tempH;
			tempH = oldH;
			oldH = s;
		}

		if (oldH < visHeight) {
			if (tempH > visHeight) {
				tempH = visHeight;
			}
			this.viewcontrol.redraw(0, getContentsY() + oldH, visWidth, tempH
					- oldH, true);
		}
		if (updateScrollBarVisiblity()) {
			layout();
		} else {
			updateScrollBarsValues();
		}
	}

	@Override
	public void redraw() {
		if(isDisposed()) {
			return;
		}
		super.redraw();
		// ..need to redraw this already:
		this.viewcontrol.redraw();
	}

	/**
	 * @param dx
	 * @param dy
	 */
	public void scrollBy(final int dx, final int dy) {
		setContentsPos(getContentsX() + dx, getContentsY() + dy);
	}

	/**
	 * Scroll to ensure point(in contents coordinates) is visible.
	 */
	public void ensureVisible(final int px, final int py) {
		int cx = getContentsX(), cy = getContentsY();
		int right = getContentsX() + getVisibleWidth();
		int bottom = getContentsY() + getVisibleHeight();
		if (px < getContentsX()) {
			cx = px;
		} else if (px > right) {
			cx = px - getVisibleWidth();
		}
		if (py < getContentsY()) {
			cy = py;
		} else if (py > bottom) {
			cy = py - getVisibleHeight();
		}
		setContentsPos(cx, cy);
	}

	/**
	 * Make rectangle (x,y,w,h, in contents coordinates) visible.
	 * if rectangle cannot be completely visible, use _align flags.
	 * @param x x contents coordinates of rectangle.
	 * @param y y contents coordinates of rectangle.
	 * @param w width of rectangle.
	 * @param h height of rectangle.
	 * @param align bit or'ed SWT flag like SWT.LEFT,RIGHT,CENTER,TOP,BOTTOM,VERTICAL
	 *               used only for bigger rectangle than visible area.
	 *               By default CENTER/VERTICAL
	 */
	public void ensureVisible(final int x, final int y, final int w, final int h, final int align) {
		ensureVisible(x, y, w, h, align, false);
	}

	/**
	 * Make rectangle (x,y,w,h, in contents coordinates) visible.
	 * if rectangle cannot be completely visible, use _align flags.
	 * @param x x contents coordinates of rectangle.
	 * @param y y contents coordinates of rectangle.
	 * @param w width of rectangle.
	 * @param h height of rectangle.
	 * @param align bit or'ed SWT flag like SWT.LEFT,RIGHT,CENTER,TOP,BOTTOM,VERTICAL
	 *               used only for bigger rectangle than visible area.
	 *               By default CENTER/VERTICAL
	 * @param forceAlign force alignment for rectangle smaller than the visible area
	 */
	protected void ensureVisible(final int x, final int y, final int w, final int h, final int align, final boolean forceAlign) {
		int tempX = x;
		int tempY = y;
		int tempW = w;
		int tempH = h;
		if (w < 0) {
			tempX = tempX + w;
			tempW = -w;
		}
		if (h < 0) {
			tempY = tempY + h;
			tempH = -h;
		}
		int hbar = getHorizontalBarHeight();
		int vbar = getVerticalBarWidth();
		int cx = getContentsX(), cy = getContentsY();
		int right = getContentsX() + getVisibleWidth() - vbar;
		int bottom = getContentsY() + getVisibleHeight() - hbar;
		boolean alignH = false, alignV = false;

		if (tempX < getContentsX()) {
			cx = tempX;
		} else if (tempX + tempW > right) {
			cx = tempX - tempW;
		}
		if (tempY < getContentsY()) {
			cy = tempY;

		} else if (tempY + tempH > bottom) {
			cy = tempY - tempH;
		}

		if (tempW > getVisibleWidth()) {
			alignH = true;
		}
		if (tempH > getVisibleHeight()) {
			alignV = true;
		}
		//compute alignment on visible area horizontally
		if (alignH || (forceAlign && (tempX + tempW > right))) {
			//use _align flags
			if ((align & SWT.LEFT) != 0) {
				cx = tempX;
			} else if ((align & SWT.RIGHT) != 0) {
				cx = right - tempW;
			} else {
				cx = tempX + (tempW - getVisibleWidth()) / 2;
			}
		}
		//compute alignment on visible area vertically
		if (alignV || (forceAlign && (tempY + tempH > bottom))) {
			//use _align flags
			if ((align & SWT.TOP) != 0) {
				cy = tempY;
			} else if ((align & SWT.BOTTOM) != 0) {
				cy = bottom - tempH;
			} else {
				cy = tempY + (tempH - getVisibleHeight()) / 2;
			}
		}
		setContentsPos(cx, cy);
	}

	/**
	 * @return true if point is visible (expressed in contents coordinates)
	 */
	public boolean isVisible(final int px, final int py) {
		if (px < getContentsX()) {
			return false;
		}
		if (py < getContentsY()) {
			return false;
		}
		if (px > (getContentsX() + getVisibleWidth())) {
			return false;
		}
		if (py > (getContentsY() + getVisibleHeight())) {
			return false;
		}
		return true;
	}

	/**
	 * @return true if rectangle if partially visible.
	 */
	public boolean isVisible(final int x, final int y, final int w, final int h) {
		if (x + w < getContentsX()) {
			return false;
		}
		if (y + h < getContentsY()) {
			return false;
		}
		int vr = getContentsX() + getVisibleWidth();
		int vb = getContentsY() + getVisibleHeight();
		if (x > vr) {
			return false;
		}
		if (y > vb) {
			return false;
		}
		return true;
	}

	/**
	 * @return visible part of rectangle, or null if rectangle is not visible.
	 *         rectangle is expressed in contents coordinates.
	 */
	public Rectangle getVisiblePart(final int x, final int y, final int w, final int h) {
		if (x + w < getContentsX()) {
			return null;
		}
		if (y + h < getContentsY()) {
			return null;
		}
		int vr = getContentsX() + getVisibleWidth();
		int vb = getContentsY() + getVisibleHeight();
		if (x > vr) {
			return null;
		}
		if (y > vb) {
			return null;
		}
		int rr = x + w, rb = y + h;
		int nl = Math.max(x, getContentsX()), nt = Math
				.max(y, getContentsY()), nr = Math.min(rr, vr), nb = Math.min(
				rb, vb);
		return new Rectangle(nl, nt, nr - nl, nb - nt);
	}

	/**
	 * @param r
	 * @return
	 */
	public final Rectangle getVisiblePart(final Rectangle r) {
		if (r == null) {
			return null;
		}
		return getVisiblePart(r.x, r.y, r.width, r.height);
	}

	/**
	 * Change top left position of visible area. Check if the given point
	 * is inside contents area.
	 * @param x
	 * @param y
	 * @return true if view really moves
	 */
	public boolean setContentsPos(final int x, final int y) {
		int nx = x, ny = y;
		if (getVisibleWidth() >= getContentsWidth()) {
			nx = 0;
		} else {
			if (x < 0) {
				nx = 0;
			} else if (x + getVisibleWidth() > getContentsWidth()) {
				nx = getContentsWidth() - getVisibleWidth();
			}
		}
		if (getVisibleHeight() >= getContentsHeight()) {
			ny = 0;
		} else {
			if (y <= 0) {
				ny = 0;
			} else if (y + getVisibleHeight() > getContentsHeight()) {
				ny = getContentsHeight() - getVisibleHeight();
			}
		}
		//no move
		if ((nx == this.contentsX) && (ny == this.contentsY)) {
			return false;
		}
		this.contentsX = nx;
		this.contentsY = ny;
		updateScrollBarsValues();
		//? find smallest area to redraw only them ?
		this.viewcontrol.redraw();
		return true;
	}

	@Override
	public ScrollBar getVerticalBar() {
		return this.vertsb.getVerticalBar();
	}

	@Override
	public ScrollBar getHorizontalBar() {
		return this.horzsb.getHorizontalBar();
	}

	public static final int VBAR = 0x01;

	public static final int HBAR = 0x02;

	/** compute visibility of vert/hor bar using given width/height and current visibility
	 * (ie is barr size are already in for_xxx */
	public int computeBarVisibility(final int forWidth, final int forHeight, final boolean currHVis, final boolean currVVis) {
		int vis = 0x00;
		switch (this.vScrollbarMode) {
		case ALWAYS_OFF:
			break;
		case ALWAYS_ON:
			vis |= VBAR;
			break;
		case AUTO:
			if (getContentsHeight() > forHeight) {
				vis = VBAR;
				//v bar size is already in for_width.
				if (!currVVis) {
//					for_width -= getVerticalBarWidth();
				}
			}
			break;
			default:
				break;
		}
		switch (this.hScrollbarMode) {
		case ALWAYS_OFF:
			break;
		case ALWAYS_ON:
			vis |= HBAR;
			break;
		case AUTO:
			if (getContentsWidth() > forWidth) {
				vis |= HBAR;
				//h bar is not in for_height
				if (!currHVis
						&& getContentsHeight() > forHeight - getHorizontalBarHeight()) {
					vis |= VBAR;
				}
			}
			break;
		default:
			break;
		}
		return vis;
	}

	/**
	 * setup scroll bars visibility, return true if one of visibility changed.
	 */
	private boolean updateScrollBarVisiblity() {
		boolean change = false;

		boolean currVVis = this.vertsb.getVisible();
		boolean currHVis = this.horzsb.getVisible();
		int barNewVis = computeBarVisibility(getVisibleWidth(),
				getVisibleHeight(), currHVis, currVVis);
		boolean newVVis = (barNewVis & VBAR) != 0;
		boolean newHVis = (barNewVis & HBAR) != 0;
		if (currVVis ^ newVVis) {
			this.vertsb.setVisible(newVVis);
			change = true;
		}
		if (currHVis ^ newHVis) {
			this.horzsb.setVisible(newHVis);
			change = true;
		}

		// Update corner control visibility:
		if ((this.cornerControl != null) && change) {
			boolean vis = newVVis || newHVis;
			if (vis ^ this.cornerControl.getVisible()) {
				this.cornerControl.setVisible(vis);
				change = true; //but must be already the case
			}
		}
		return change;
	}

	/**
	 * Setup scroll bar using contents, visible and scroll bar mode properties.
	 * 
	 */
	private void updateScrollBarsValues() {
		/* update vertical scrollbar */
		ScrollBar b = getVerticalBar();
		if (b != null) {
			b.setMinimum(0);
			b.setMaximum(getContentsHeight());
			b.setThumb(getVisibleHeight());
			b.setPageIncrement(getVisibleHeight());
			b.setIncrement(this.vScrollbarIncrement);
			b.setSelection(getContentsY());
		}

		//update "hidden" vertical bar too
		b = this.viewcontrol.getVerticalBar();
		if (b != null) {
			b.setMinimum(0);
			b.setMaximum(getContentsHeight());
			b.setThumb(getVisibleHeight());
			b.setPageIncrement(getVisibleHeight());
			b.setIncrement(this.vScrollbarIncrement);
			b.setSelection(getContentsY());
		}

		/* update horizontal scrollbar */
		b = getHorizontalBar();
		if (b != null) {
			b.setMinimum(0);
			b.setMaximum(getContentsWidth());
			b.setThumb(getVisibleWidth());
			b.setSelection(getContentsX());
			b.setPageIncrement(getVisibleWidth());
			b.setIncrement(this.hScrollbarIncrement);
		}
		//update "hidden" horizontal bar too
		b = this.viewcontrol.getHorizontalBar();
		if (b != null) {
			b.setMinimum(0);
			b.setMaximum(getContentsWidth());
			b.setThumb(getVisibleWidth());
			b.setSelection(getContentsX());
			b.setPageIncrement(getVisibleWidth());
			b.setIncrement(this.hScrollbarIncrement);
		}
	}

	/**
	 * Change the control used in the bottom right corner (between two scrollbar),
	 * if control is null reset previous corner control.
	 * This control is visible only if at leat one scrollbar is visible.
	 * Given control will be disposed by ScrollView, at dispose() time, at
	 * next setCornetControl() call or when calling setOverviewEnabled().
	 * Pay attention calling this reset overview feature util setOverviewEnabled(true)
	 * if called.
	 */
	public void setCornerControl(final Control w) {
		if (this.cornerControl != null) {
			this.cornerControl.dispose();
		}
		this.cornerControl = w;
		if (this.cornerControl != null) {
			ScrollBar vb = getVerticalBar();
			ScrollBar hb = getHorizontalBar();
			boolean vis = vb.getVisible() || hb.getVisible();
			this.cornerControl.setVisible(vis);
		}
	}

	/**
	 * Transform (x,y) point in widget coordinates to contents coordinates.
	 * @param x
	 * @param y
	 * @return org.eclipse.swt.graphics.Point
	 */
	public final Point viewToContents(final int x, final int y) {
		return new Point(viewToContentsX(x), viewToContentsY(y));
	}

	/** Transform x in widget coordinates to contents coordinates */
	public int viewToContentsX(final int x) {
		return this.contentsX + x;
	}

	/** Transform y in widget coordinates to contents coordinates */
	public int viewToContentsY(final int y) {
		return this.contentsY + y;
	}

	/**
	 * Transform (x,y) point from contents coordinates, to widget coordinates.
	 * @param x
	 * @param y
	 * @return org.eclipse.swt.graphics.Point
	 */
	public final Point contentsToView(final int x, final int y) {
		return new Point(contentsToViewX(x), contentsToViewY(y));
	}

	/**
	 * Transform X axis coordinates from contents to widgets.
	 * @param x contents coordinate to transform
	 * @return coordinate in widget area
	 */
	public int contentsToViewX(final int x) {
		return x - this.contentsX;
	}

	/**
	 * Transform Y axis coordinates from contents to widgets.
	 * @param y contents coordinate to transform
	 * @return coordinate in widget area
	 */
	public int contentsToViewY(final int y) {
		return y - this.contentsY;
	}

	/**
	 * @return int the visible height of scroll view, might be > contentsHeight()
	 */
	public int getVisibleHeight() {
		Rectangle r = this.viewcontrol.getClientArea();
		return r.height;
	}

	/**
	 * @return int the visible width of scroll view, might be > contentsWidth()
	 */
	public int getVisibleWidth() {
		Rectangle r = this.viewcontrol.getClientArea();
		return r.width;
	}

	/**
	 * Add support for arrow key, scroll the ... scroll view.
	 * But you can redefine this method for your convenience.
	 */
	protected void keyPressedEvent(final KeyEvent e) {
		switch (e.keyCode) {
		case SWT.ARROW_UP:
			upArrowPressed();
			break;
		case SWT.ARROW_DOWN:
			downArrowPressed();
			break;
		case SWT.PAGE_UP:
			scrollBy(0, -getVisibleHeight());
			break;
		case SWT.PAGE_DOWN:
			scrollBy(0, +getVisibleHeight());
			break;
		case SWT.ARROW_LEFT:
			scrollBy(-getVisibleWidth() / 4, 0);
			break;
		case SWT.ARROW_RIGHT:
			scrollBy(+getVisibleWidth() / 4, 0);
			break;
		case SWT.CR:
			Event event = new Event();
			event.keyCode = SWT.CR;
			notifyListeners(SWT.KeyDown, event);
			break;
		default:
			break;	
		}
	}

	/** Redefine this method at your convenience */
	protected void keyReleasedEvent(final KeyEvent e) {
		// Do nothing
	}

	/**
	 * Called when ScrollView view is resized.
	 */
	protected void viewResized() {
		// Do nothing
	}

	/**
	 * @return vertical bar width, even if bar isn't visible
	 */
	public int getVerticalBarWidth() {
		int bw = this.vertsb.computeTrim(0, 0, 0, 0).width;
		// +1 because win32 V.bar need 1 pixel canvas size to appear
		return bw + 1;
	}

	/** 
	 * @return horizontal bar height even if bar isn't visible
	 */
	public int getHorizontalBarHeight() {
		int bh = this.horzsb.computeTrim(0, 0, 0, 0).height;
		// +1 because win32 H.bar need 1 pixel canvas size to appear
		return bh + 1;
	}

	@Override
	public Rectangle computeTrim(final int x, final int y, final int w, final int h) {
		Rectangle r = new Rectangle(x, y, w, h);
		int barVis = computeBarVisibility(w, h, false, false);
		if ((barVis & VBAR) != 0) {
			r.width += getVerticalBarWidth();
		}
		if ((barVis & HBAR) != 0) {
			r.height += getHorizontalBarHeight();
		}
		return r;
	}

	private class SVLayout extends Layout {

		/** Internal layout for ScrollView, handle scrollbars, drawzone and corner control */
		private int seek = 0;
		private boolean dontLayout = false;

		@Override
		protected Point computeSize(final Composite composite, final int wHint, final int hHint, final boolean flushCache) {
			Point p = new Point(250, 250);
			if (ScrollView.this.contentsWidth < p.x) {
				p.x = ScrollView.this.contentsWidth;
			}
			if (ScrollView.this.contentsHeight < p.y) {
				p.y = ScrollView.this.contentsHeight;
			}
			return p;
		}

		@Override
		protected void layout(final Composite composite, final boolean flushCache) {
			if (this.dontLayout) {
				return;
			}
			this.seek++;
			if (this.seek > 10) {
				this.dontLayout = true;
			}

			Point cs = composite.getSize();
			int barVisibility = computeBarVisibility(cs.x, cs.y, false, false);
			boolean vbVis = (barVisibility & VBAR) != 0;
			boolean hbVis = (barVisibility & HBAR) != 0;
			ScrollView.this.vertsb.setVisible(vbVis);
			ScrollView.this.horzsb.setVisible(hbVis);
			int vbw = getVerticalBarWidth();
			int hbh = getHorizontalBarHeight();
			int wb = vbVis ? vbw : 0;
			int hb = hbVis ? hbh : 0;
			int cww = 0, cwh = 0;

			if ((ScrollView.this.cornerControl != null) && (vbVis || hbVis)) {
				ScrollView.this.cornerControl.setVisible(true);
				cww = vbw;
				cwh = hbh;
				if (wb == 0) {
					wb = vbw;
				}
				if (hb == 0) {
					hb = hbh;
				}
			} else if (vbVis && hbVis) {
				if (ScrollView.this.cornerControl != null) {
					ScrollView.this.cornerControl.setVisible(false);
				}
				cww = vbw;
				cwh = hbh;
			}
			if (vbVis || hbVis) {
				updateScrollBarsValues();
			}

			int vw = cs.x - (vbVis ? vbw : 0);
			int vh = cs.y - (hbVis ? hbh : 0);
			int vbx = cs.x - wb;
			int hby = cs.y - hb;
			Rectangle rc = ScrollView.this.viewcontrol.getClientArea();
			int oldWidth = rc.width;
			int oldHeight = rc.height;
			ScrollView.this.viewcontrol.setBounds(0, 0, vw, vh);
			boolean doViewResize = false;
			rc = ScrollView.this.viewcontrol.getClientArea();
			if ((oldWidth != rc.width) || (oldHeight != rc.height)) {
				//area size change, so visibleWidth()/Height() change too
				//so scrollbars visibility may change too..
				//so need an other layout !
				doViewResize = true;
			}
			if (vbVis) {
				ScrollView.this.vertsb.setBounds(vbx, 0, wb, cs.y - cwh);
			}
			if (hbVis) {
				ScrollView.this.horzsb.setBounds(0, hby, cs.x - cww, hb);
			}
			if ((ScrollView.this.cornerControl != null)
					&& ScrollView.this.cornerControl.getVisible()) {
				ScrollView.this.cornerControl.setBounds(vbx, hby, vbw, hbh);
			}
			updateScrollBarsValues();
			if (doViewResize) {
				viewResized();
			}
			this.seek--;
			if (this.seek == 0) {
				this.dontLayout = false;
			}
		}
	}

	/**
	 * Called when up arrow is pressed 
	 */
	protected void upArrowPressed() {
		// Do nothing
	}
	
	/**
	 * Called when down arrow is pressed 
	 */
	protected void downArrowPressed() {
		// Do nothing
	}

}
