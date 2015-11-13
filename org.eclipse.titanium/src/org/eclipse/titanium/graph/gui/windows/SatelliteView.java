/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.titanium.graph.gui.common.CustomSatelliteViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer;

/**
 * This class extends an Eclipse {@link ViewPart}, it implements the behaviour
 * of the SatelliteGraph view. <b>This class can be constructed only once in a
 * wokrspace, in the meaning that the instance is totally unique</b>
 * 
 * @author Gabor Jenei
 */
public class SatelliteView extends ViewPart {
	private JPanel satView;
	private Frame window;
	private Dimension windowSize;
	private CustomSatelliteViewer satelliteGraph;
	private GraphEditor editor;
	public static final String ID = "org.eclipse.titanium.graph.gui.windows.SatelliteView";

	/**
	 * The constructor, it puts an empty white {@link JPanel} to the
	 * {@link ViewPart}
	 */
	public SatelliteView() {
		satView = new JPanel();
		satView.setBackground(Color.white);
	}

	/**
	 * This method is inherited from {@link ViewPart}, it does the
	 * initialization of the editor window
	 */
	@Override
	public void createPartControl(final Composite parent) {
		Composite temp = new Composite(parent, SWT.NO_BACKGROUND | SWT.EMBEDDED);
		window = SWT_AWT.new_Frame(temp);
		windowSize = new Dimension(parent.getSize().x, parent.getSize().y);

		window.add(satView);

		parent.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				Point tmpSize = parent.getSize();
				windowSize = new Dimension(tmpSize.x, tmpSize.y);
				if (satelliteGraph == null) {
					window.setPreferredSize(windowSize);
					window.repaint();
				} else {
					satelliteGraph.changeSize(windowSize);
				}
			}
		});

		IEditorPart tmpEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (tmpEditor instanceof GraphEditor) {
			editor = (GraphEditor) tmpEditor;
			editor.setSatellite(this);
		}
	}

	/**
	 * This method initializes the site attribute
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		setSite(site);
	}

	/**
	 * This method initializes the site attribute
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		setSite(site);
	}

	/**
	 * This method is called when the view gets into the foreground, it is
	 * currently empty.
	 */
	@Override
	public void setFocus() {
		//Do nothing
	}

	/**
	 * This method is called when the view is closed, it does some clear up.
	 */
	@Override
	public void dispose() {
		super.dispose();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.dispose();
			}
		});

		IWorkbenchWindow actWind = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (actWind == null) {
			return;
		}
		IWorkbenchPage actPage = actWind.getActivePage();
		if (actPage == null) {
			return;
		}
		IEditorPart tmpEditor = actPage.getActiveEditor();
		if (tmpEditor != null && tmpEditor instanceof GraphEditor) {
			((GraphEditor) tmpEditor).setSatellite(null);
		}
	}

	/**
	 * This is an AWT-like method, it adds a
	 * {@link SatelliteVisualizationViewer} to the view, and makes the
	 * components to refresh
	 * 
	 * @param satGraph
	 *            : The satellite graph to add
	 */
	public void add(CustomSatelliteViewer satGraph) {
		if (satGraph == null || satView == null) {
			return;
		}
		satelliteGraph = satGraph;
		satelliteGraph.setPreferredSize(windowSize);
		satelliteGraph.changeSize(windowSize);
		satView.removeAll();
		satView.add(satelliteGraph);
		satelliteGraph.setPreferredSize(windowSize);
		satView.repaint();
		satView.revalidate();
	}

	/**
	 * This method returns the currently set view size.
	 * 
	 * @return The set size
	 */
	public Dimension getSize() {
		return windowSize;
	}

	/**
	 * This method sets a new editor size, and refreshes all the belonging
	 * components as it's needed.
	 * 
	 * @param size
	 *            : The new size
	 */
	public void setSize(Dimension size) {
		windowSize = size;
		window.setSize(windowSize);
		satView.setPreferredSize(windowSize);
		satelliteGraph.changeSize(windowSize);
		satView.repaint();
	}

	// these methods provide an AWT-like behaviour

	/**
	 * This method does the repaint (refresh) of the view
	 */
	public void repaint() {
		satView.repaint();
	}

	/**
	 * This method deletes the shown components from the view, only a pure white
	 * background will stay
	 */
	public void clear() {
		satView.removeAll();
		satView.repaint();
	}

	/**
	 * This method sets the current editor that handles the view.
	 * 
	 * @param editor
	 *            : The editor to set
	 */
	public void setEditor(GraphEditor editor) {
		this.editor = editor;
	}

}