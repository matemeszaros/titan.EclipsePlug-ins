/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * @author Kristof Szabados
 * */
class TITANProjectExportMainPage extends WizardPage {
	private final IStructuredSelection selection;

	private Text projectFileText;
	private Button projectFileSelectionButton;
	private String projectFile = null;
	private IProject project = null;

	private BasicProjectSelectorListener generalListener = new BasicProjectSelectorListener();

	protected class BasicProjectSelectorListener implements ModifyListener, SelectionListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			Object source = e.getSource();
			if (source == projectFileText) {
				handleProjectFileModified();
			}
		}

		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			Object source = e.getSource();
			if (source == projectFileSelectionButton) {
				handleProjectFileButtonSelected();
			}
		}
	}

	public TITANProjectExportMainPage(final String name, final IStructuredSelection selection) {
		super(name);
		this.selection = selection;
	}

	public String getProjectFilePath() {
		return projectFile;
	}

	@Override
	public void createControl(final Composite parent) {
		Composite pageComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		pageComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		pageComposite.setLayoutData(data);

		check(projectFile);

		createProjectFileEditor(pageComposite);

		setControl(pageComposite);
	}

	/**
	 * Check the current setup, whether it is allowed to do the exportation
	 * or not.
	 * 
	 * @param filePath
	 *                the path of the file as it is seen now.
	 * */
	private boolean check(final String filePath) {
		project = null;

		if (selection == null) {
			setErrorMessage("A project must be selected for the export wizard to work.");
			return false;
		}

		if (selection.size() != 1) {
			setErrorMessage("Exactly 1 project has to be selected");
			return false;
		}

		List<?> selectionList = selection.toList();
		if (!(selectionList.get(0) instanceof IProject)) {
			setErrorMessage("A project has to be selected");
			return false;
		}

		project = (IProject) selectionList.get(0);

		if (filePath != null) {
			IPath projectFilePath = Path.fromOSString(filePath);
			if (!projectFilePath.isValidPath(filePath)) {
				setErrorMessage("The provided file path does not seem to be valid on this platform");
				return false;
			}
		}

		return true;
	}

	protected void createProjectFileEditor(final Composite parent) {
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		group.setText("Target project file:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		projectFileText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projectFileText.setLayoutData(gd);
		projectFileText.setFont(font);
		projectFileText.addModifyListener(generalListener);
		 
		if(project != null) {
			projectFileText.setText(project.getName() + ".tpd"); //default value
			try {
				// if this project is imported, the location of the source tpd is stored in loadLocation,
				// if this project is not imported then loadLocation == null and the new default tpd will be defined
				// in the current project folder in the current workspace
				String loadLocation = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						ProjectBuildPropertyData.LOAD_LOCATION));
				if (loadLocation != null) {
					URI projectFileURI = new URI(loadLocation);
					IPath projectFilePath = URIUtil.toPath(projectFileURI);
					if(projectFilePath != null) {
						projectFileText.setText(projectFilePath.toString());//bugfix by ethbaat // FIXME: toOSString() ???
					} 

				} else if(project.getLocation() != null){
					projectFileText.setText(project.getLocation().append(project.getName() + ".tpd").toOSString());				
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			} catch (URISyntaxException e) {
				//bad loadLocation input:
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		projectFileSelectionButton = new Button(group, SWT.PUSH);
		projectFileSelectionButton.setText("Browse..");
		projectFileSelectionButton.setLayoutData(new GridData());
		projectFileSelectionButton.addSelectionListener(generalListener);
	}

	protected void handleProjectFileButtonSelected() {
		FileDialog dialog = new FileDialog(getShell());
		IPath path = new Path(projectFileText.getText());
		dialog.setFileName(path.lastSegment());
		dialog.setFilterExtensions(new String[] { "*.tpd" });
		final String file = dialog.open();

		if (file != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					projectFileText.setText(file);
				}
			});
		}

		if (check(file)) {
			projectFile = file;
		}
	}

	protected void handleProjectFileModified() {
		String temp = projectFileText.getText();

		if (check(temp)) {
			projectFile = temp;
		}
	}
}
