/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.titan.designer.samples.SampleProject;
import org.eclipse.titan.designer.samples.SampleProjects;

/**
 * @author Szabolcs Beres
 * */
public class NewTITANProjectContentPage extends WizardPage {

	private static final String TITLE = "Project content";
	private static final String DESCRIPTION = "Choose the content of the project";

	private Composite pageComposite;
	private SampleProject sampleProject = null;

	public NewTITANProjectContentPage() {
		super(TITLE);
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public void dispose() {
		pageComposite.dispose();
		super.dispose();
	}

	/**
	 * @return The name of the selected sample project or {@code null} if the
	 *         empty project option is selected.
	 */
	public SampleProject getSampleProject() {
		return sampleProject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt
	 * .widgets.Composite)
	 */
	@Override
	public void createControl(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		pageComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		pageComposite.setLayoutData(data);

		createSampleProjectsGroup(pageComposite);

		setErrorMessage(null);
		setControl(pageComposite);
	}

	/**
	 * Creates the sample project selection part of the window
	 * 
	 * @param parent
	 *                the parent composite
	 */
	private void createSampleProjectsGroup(final Composite parent) {
		Group projectAndDescription = new Group(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		projectAndDescription.setLayout(gridLayout);
		projectAndDescription.setLayoutData(new GridData(GridData.FILL_BOTH));
		projectAndDescription.setText("Sample projects");

		final List samplesList = new List(projectAndDescription, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		samplesList.setLayoutData(new GridData(GridData.FILL_BOTH));
		int indexOfEmptyProject = 0;
		int i = 0;
		for (Map.Entry<String, SampleProject> entry : SampleProjects.getProjects().entrySet()) {
			samplesList.add(entry.getValue().getName());
			if ("Empty Project".equals(entry.getValue().getName())) {
				indexOfEmptyProject = i;
			}
			++i;
		}
		samplesList.select(indexOfEmptyProject);
		sampleProject = SampleProjects.getProjects().get(samplesList.getSelection()[0]);

		final Label description = new Label(projectAndDescription, SWT.BORDER);
		description.setLayoutData(new GridData(GridData.FILL_BOTH));
		description.setText(sampleProject.getDescription());

		samplesList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (samplesList.getSelectionCount() != 1) {
					return;
				}
				sampleProject = SampleProjects.getProjects().get(samplesList.getSelection()[0]);
				description.setText(sampleProject.getDescription());
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// Do nothing
			}
		});
	}
}
