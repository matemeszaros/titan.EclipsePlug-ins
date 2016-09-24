/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.windows;

import java.util.Collection;

import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.generators.ComponentGraphGenerator;
import org.eclipse.titanium.graph.gui.common.Layouts;
import org.eclipse.titanium.graph.visualization.GraphHandler;
import org.eclipse.ui.IFileEditorInput;

/**
 * This class is a subclass of {@link GraphEditor}. It implements the
 * specialties needed for component graph editor window
 * 
 * @author Gabor Jenei
 * @see GraphEditor
 */
public class ComponentGraphEditor extends GraphEditor {
	public static final String ID = "org.eclipse.titanium.graph.editors.ComponentGraphEditor";

	public ComponentGraphEditor() {
		super();
	}

	@Override
	protected void initWindow() {
		super.initWindow();

		final JRadioButtonMenuItem isom = Layouts.LAYOUT_ISOM.newInstance();
		isom.addActionListener(layoutListener);
		layoutGroup.add(isom);
		layoutMenu.add(isom);

		final JMenu dagMenu = new JMenu("Directed layouts");
		layoutMenu.add(dagMenu);

		final JRadioButtonMenuItem tdag = Layouts.LAYOUT_TDAG.newInstance();
		tdag.setSelected(true);
		tdag.addActionListener(layoutListener);
		dagMenu.add(tdag);
		layoutGroup.add(tdag);

		final JRadioButtonMenuItem rtdag = Layouts.LAYOUT_RTDAG.newInstance();
		rtdag.addActionListener(layoutListener);
		dagMenu.add(rtdag);
		layoutGroup.add(rtdag);
	}

	@Override
	public void recolour(final Collection<NodeDescriptor> nodeSet) {
		for (final NodeDescriptor v : nodeSet) {
			v.setNodeColour(NodeColours.LIGHT_GREEN);
		}
	}

	@Override
	protected void initGeneratorAndHandler(final Composite parent) {
		handler = new GraphHandler();
		generator = new ComponentGraphGenerator(((IFileEditorInput) getEditorInput()).getFile().getProject(), errorHandler);
	}

}
