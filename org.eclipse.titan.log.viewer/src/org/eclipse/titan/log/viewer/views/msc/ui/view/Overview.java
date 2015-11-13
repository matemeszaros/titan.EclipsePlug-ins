/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;

public class Overview {
	
	private static Cursor overviewCursor;
	/** default size for overview */
	private int overviewSize = 100;
	private ScrollView sv;
	private static final int MIN_SCROLL_VALUE = -50;
	private static final int MAX_SCROLL_VALUE = 50;
	private static final int MIN_EVENTTIME = 40;

	/** Factors from real and overview sizes, for mouse move speed. */
	private float overviewFactorX, overviewFactorY;

	/** Shell used to show overview */
	private Shell overview;

	/** Save mouse cursor location for disappear(); */
	private int saveCursorX, saveCursorY;

	private long lastTime;

	/**
	 * Constructor
	 * @param sv the scroll view
	 */
	public Overview(final ScrollView sv) {
		this.sv = sv;
	}

	/** apply overview support on a control. Replace existing corner_widget */
	public void useControl(final Control c) {
		final Point pos = c.getLocation();
		c.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent e) {
				overviewAppear(e.x, e.y);
			}

			@Override
			public void mouseUp(final MouseEvent e) {
				overviewDisappear();
			}
		});

		c.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(final MouseEvent e) {
				if (overviewing()) {
					overviewMove(e);
				}
			}
		});
		
		c.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent e) {
				if (overviewing()) {
					overviewDisappear(false);
				}
			}
		});

		c.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if ((e.keyCode == 32) && !overviewing()) {
					overviewAppear(pos.x, pos.y);
				} else if (e.keyCode == 32) {
					overviewDisappear();
				}
				if (e.keyCode == SWT.ARROW_DOWN) {
					overviewMove(0, 1, e);
				}
				if (e.keyCode == SWT.ARROW_UP) {
					overviewMove(0, -1, e);
				}
				if (e.keyCode == SWT.ARROW_RIGHT) {
					overviewMove(1, 0, e);
				}
				if (e.keyCode == SWT.ARROW_LEFT) {
					overviewMove(-1, 0, e);
				}
			}
		});
	}

	/** Dispose controls of overview */
	public void dispose() {
		if (this.overview != null) {
			this.overview.dispose();
		}
	}

	/** @return true if overview is currently on screen */
	private boolean overviewing() {
		return this.overview != null && this.overview.isVisible();
	}

	/** Process overview appear */
	private void overviewAppear(final int mx, final int my) {
		if (this.overview == null) {
			this.overview = new Shell(this.sv.getShell(), SWT.ON_TOP | SWT.NO_BACKGROUND);
			this.overview.addPaintListener(new PaintListener() {
				@Override
				public void paintControl(final PaintEvent e) {
					Overview.this.sv.drawOverview(e.gc, Overview.this.overview.getClientArea());
				}
			});
		}
		// Always the same..
		this.overview.setForeground(this.sv.viewcontrol.getForeground());

		// Get location of shell (in screen coordinates)
		Point p = toGlobalCoordinates(this.sv.cornerControl, 0, 0);
		int x = p.x;
		int y = p.y;
		int w = this.overviewSize;
		int h = this.overviewSize;
		Rectangle scr = this.sv.getDisplay().getBounds();
		Point ccs = this.sv.cornerControl.getSize();
		try {
			if (this.sv.contentsWidth > this.sv.contentsHeight) {
				float ratio = this.sv.contentsHeight / (float) this.sv.contentsWidth;
				h = (int) (w * ratio);
				if (h < ccs.y) {
					h = ccs.y;
				} else if (h >= scr.height / 2) {
					h = scr.height / 2;
				}
			} else {
				float ratio = this.sv.contentsWidth / (float) this.sv.contentsHeight;
				w = (int) (h * ratio);
				if (w < ccs.x) {
					w = ccs.x;
				} else if (w >= scr.width / 2) {
					w = scr.width / 2;
				}
			}
			this.overviewFactorX = this.sv.contentsWidth / (float) w / 2;
			this.overviewFactorY = this.sv.contentsHeight / (float) h / 2;
		} catch (java.lang.ArithmeticException e) {
			// Do nothing
		}

		if (x <= 0) {
			x = 1;
		}
		if (y <= 0) {
			y = 1;
		}
		x = x - w + ccs.x;
		y = y - h + ccs.y;
		this.overview.setBounds(x, y, w, h);
		this.overview.setVisible(true);
		this.overview.redraw(x, y, w, h, false);
		
		if (overviewCursor == null) {
			RGB[] rgb = {new RGB(0, 0, 0), new RGB(255, 0, 0)};
			PaletteData pal = new PaletteData(rgb);
			int s = 1;
			byte[] src = new byte[s * s];
			byte[] msk = new byte[s * s];
			for (int i = 0; i < s * s; ++i) {
				src[i] = (byte) 0xFF;
			}
			ImageData iSrc = new ImageData(s, s, 1, pal, 1, src);
			ImageData iMsk = new ImageData(s, s, 1, pal, 1, msk);
			overviewCursor = new Cursor(null, iSrc, iMsk, 0, 0);
		}
		this.sv.cornerControl.setCursor(overviewCursor);
		//convert to global coordinates
		p = toGlobalCoordinates(this.sv.cornerControl, mx, my);
		this.saveCursorX = p.x;
		this.saveCursorY = p.y;

		Rectangle r = this.overview.getClientArea();
		int cx = (int) (r.width * this.sv.contentsX / (float) this.sv.contentsWidth);
		int cy = (int) (r.height * this.sv.contentsY / (float) this.sv.contentsHeight);

		//cx,cy to display's global coordinates
		p = toGlobalCoordinates(this.overview.getParent(), cx, cy);
		cx = p.x;
		cy = p.y;
	}

	/** Process disappear of overview */
	private void overviewDisappear() {
		overviewDisappear(true);
	}

	/** Process disappear of overview */
	private void overviewDisappear(final boolean restoreCursorLoc) {
		if (this.overview == null) {
			return;
		}
		this.overview.setVisible(false);
		this.sv.cornerControl.setCursor(null);
		if (restoreCursorLoc) {
			this.sv.getDisplay().setCursorLocation(this.saveCursorX, this.saveCursorY);
		}
		this.overview.dispose();
		this.overview = null;
		this.sv.setFocus();
	}

	private void overviewMove(final MouseEvent event) {
		Point p = toGlobalCoordinates(this.sv.cornerControl, event.x, event.y);
		int dx = p.x - this.saveCursorX;
		int dy = p.y - this.saveCursorY;

		// Prevent "no movement" events
		if ((dx == 0) && (dy == 0)) {
			return;
		}
		
		// Prevent to many events
		long time = 0xFFFFFFFFL & event.time;
		if (time == this.lastTime) {
			return;
		}
		if ((time > this.lastTime) && ((time - this.lastTime) < MIN_EVENTTIME)) {
			return;
		}
		this.lastTime = time;

		// Prevent "too fast" scrolling
		if (dx < MIN_SCROLL_VALUE) {
			dx = MIN_SCROLL_VALUE;
		} else if (dx > MAX_SCROLL_VALUE) {
			dx = MAX_SCROLL_VALUE;
		}
		if (dy < MIN_SCROLL_VALUE) {
			dy = MIN_SCROLL_VALUE;
		} else if (dy > MAX_SCROLL_VALUE) {
			dy = MAX_SCROLL_VALUE;
		}
		overviewMove(dx, dy, event);
	}

	/** Process mouse move event when overviewing */
	private void overviewMove(final int dx, final int dy, final TypedEvent event) {
		boolean ctrl = false;
		boolean shift = false;

		if (event instanceof MouseEvent) {
			MouseEvent e = (MouseEvent) event;
			this.sv.getDisplay().setCursorLocation(this.saveCursorX, this.saveCursorY);
			ctrl = (e.stateMask & SWT.CONTROL) != 0;
			shift = (e.stateMask & SWT.SHIFT) != 0;
		} else if (event instanceof KeyEvent) {
			KeyEvent e = (KeyEvent) event;
			ctrl = (e.stateMask & SWT.CONTROL) != 0;
			shift = (e.stateMask & SWT.SHIFT) != 0;
		}

		int cx = this.sv.contentsX;
		int cy = this.sv.contentsY;
		float fx = this.overviewFactorX;
		float fy = this.overviewFactorY;

		if (ctrl && shift) {
			if ((fx * 0.25f > 1) && (fy * 0.25 > 1)) {
				fx = 1.0f;
				fy = 1.0f;
			} else {
				fx *= 0.1f;
				fy *= 0.1f;
			}
		} else if (ctrl) {
			fx *= 0.5f;
			fy *= 0.5f;
		} else if (shift) {
			fx *= 0.5f;
			fy *= 0.5f;
		}
		
		this.sv.scrollBy((int) (fx * dx), (int) (fy * dy));	
		
		if (((cx != this.sv.contentsX) || (cy != this.sv.contentsY)) && this.overview != null) {
			this.overview.redraw();
			this.overview.update();
		}
	}

	protected Point toGlobalCoordinates(final Control loc, final int x, final int y) {
		Point p = new Point(x, y);
		for (Control c = loc; c != null; c = c.getParent()) {
			//control might have client area with 'decorations'
			int trimX = 0, trimY = 0;
			//other kind of widget with trimming ??
			if (c instanceof Scrollable) {
				Scrollable s = (Scrollable) c;
				Rectangle rr = s.getClientArea();
				Rectangle tr = s.computeTrim(rr.x, rr.y, rr.width, rr.height);
				trimX = rr.x - tr.x;
				trimY = rr.y - tr.y;
			}
			p.x += c.getLocation().x + trimX;
			p.y += c.getLocation().y + trimY;
		}
		return p;
	}
	
	/** 
	 * Change overview size (at ratio 1:1), default is 100
	 */
	public void setOverviewSize(final int size) {
		this.overviewSize = Math.abs(size);
	}

}
