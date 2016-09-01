/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.fieldeditors;

import static org.eclipse.titan.common.utils.StringUtils.isNullOrEmpty;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.path.PathUtil;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.common.utils.environment.EnvironmentVariableResolver;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.dialogs.PathVariableSelectionDialog;

/**
 * @author Kristof Szabados
 * */
public class TITANResourceLocatorFieldEditor extends StringFieldEditor {
	private int type;
	private String rootPath;
	private Composite composite;

	private Button pathButton;
	private Button envButton;
	private Button browseButton;

	private String target = "";
	private Label resolvedPathLabelText;

	// an optional console that might be used to provide debug information.
	private MessageConsole console = null;

	/**
	 * Creates a string button field editor.
	 * 
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 * @param type either FILE or FOLDER from IResource
	 */
	public TITANResourceLocatorFieldEditor(final String name, final String labelText, final Composite parent, final int type, final String rootPath) {
		init(name, labelText);
		this.type = type;
		this.rootPath = rootPath;
		createControl(parent);
	}

	public void setRootPath(final String rootPath) {
		this.rootPath = rootPath;
		resolveVariable();
	}

	public void setConsole(final MessageConsole console) {
		this.console = console;
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	public void addModifyListener(final ModifyListener listener) {
		final Text text = getTextControl();

		if (text != null) {
			text.addModifyListener(listener);
		}
	}

	@Override
	protected void doFillIntoGrid(final Composite parent, final int numColumns) {
		super.doFillIntoGrid(parent, numColumns);

		getTextControl().addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(final ModifyEvent e) {
				target = getTextControl().getText();
				resolveVariable();
			}
		});
		composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 3;
		composite.setLayoutData(gd);

		resolvedPathLabelText = new Label(composite, SWT.SINGLE);
		resolvedPathLabelText.setText("Resolved location: ");
		resolvedPathLabelText.setVisible(false);
		gd = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING);
		resolvedPathLabelText.setLayoutData(gd);

		final Composite buttons = new Composite(composite, SWT.NONE);
		layout = new GridLayout(3, false);
		buttons.setLayout(layout);
		gd = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_END);
		buttons.setLayoutData(gd);

		pathButton = new Button(buttons, SWT.PUSH);
		pathButton.setText("path variable");
		pathButton.setFont(parent.getFont());
		pathButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent evt) {
				handleVariablesButtonPressed();
			}
		});
		
		gd = new GridData();
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		pathButton.setLayoutData(gd);

		envButton = new Button(buttons, SWT.PUSH);
		envButton.setText("env variable");
		envButton.setFont(parent.getFont());
		envButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent evt) {
				handleEnvSelectButtonSelected();
			}
		});

		gd = new GridData();
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		envButton.setLayoutData(gd);

		browseButton = new Button(buttons, SWT.PUSH);
		browseButton.setText("browse");
		browseButton.setFont(parent.getFont());
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent evt) {
				handleBrowseButtonPressed();
			}
		});
		
		gd = new GridData();
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		browseButton.setLayoutData(gd);
	}

	private static final class EnvironmentVariable {
		private String name;
		private String value;

		private EnvironmentVariable() {
		}
	}

	/**
	 * Displays a dialog that allows user to select native environment variables 
	 * to add to the table.
	 */
	private void handleEnvSelectButtonSelected() {
		//get Environment Variables from the OS
		final Map<?, ?> envVariables = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();
		final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getTextControl().getShell(), new LabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof EnvironmentVariable) {
					final EnvironmentVariable temp = (EnvironmentVariable) element;
					return temp.name + " [ " + temp.value + " ] ";
				}

				return super.getText(element);
			}
			
		});
		dialog.setTitle("Environmental variable selection");
		dialog.setMessage("Select a variable to constrain your search.");
		
		final List<EnvironmentVariable> variables = new ArrayList<EnvironmentVariable>(envVariables.size());
		for (final Iterator<?> i = envVariables.keySet().iterator(); i.hasNext();) {
			final EnvironmentVariable variable = new EnvironmentVariable();
			variable.name = (String) i.next();
			variable.value = (String) envVariables.get(variable.name);

			variables.add(variable);
		}

		dialog.setElements(variables.toArray(new EnvironmentVariable[variables.size()]));
		dialog.setMultipleSelection(false);
		if (dialog.open() == IDialogConstants.OK_ID) {
			final Object result = dialog.getFirstResult();
			final EnvironmentVariable variable = (EnvironmentVariable) result;
			final String original = getTextControl().getText();
			getTextControl().setText(original + "[" + variable.name + "]");
		}
	}

	/**
	 * Opens a path variable selection dialog
	 */
	private void handleVariablesButtonPressed() {
		int variableTypes = IResource.FOLDER;

		// allow selecting file and folder variables when creating a
		// linked file
		if (type == IResource.FILE) {
			variableTypes |= IResource.FILE;
		}

		final PathVariableSelectionDialog dialog = new PathVariableSelectionDialog(getTextControl().getShell(), variableTypes);
		if (dialog.open() == IDialogConstants.OK_ID) {
			final String[] variableNames = (String[]) dialog.getResult();
			if (variableNames != null && variableNames.length == 1) {
				getTextControl().setText(variableNames[0]);
			}
		}
	}

	/**
	 * Opens a file or directory browser depending on the link type.
	 */
	private void handleBrowseButtonPressed() {
		String selection = null;
		final URI resolvedPath = TITANPathUtilities.resolvePathURI(target, rootPath);

		if (type == IResource.FILE) {
			final FileDialog dialog = new FileDialog(getTextControl().getShell());
			dialog.setText("Select the target file.");
			IPath path = URIUtil.toPath(resolvedPath);
			dialog.setFilterPath(path.removeLastSegments(1).toOSString());
			selection = dialog.open();
		} else {
			final DirectoryDialog dialog = new DirectoryDialog(getTextControl().getShell());
			dialog.setMessage("Select the target folder.");
			dialog.setFilterPath(URIUtil.toPath(resolvedPath).toOSString());
			selection = dialog.open();
		}
		if (selection != null) {
			if (console != null) {
				console.newMessageStream().println("Browsed the path: " + selection);
			}
			final String result = PathUtil.getRelativePath(rootPath, selection);
			setStringValue(result);
		}
	}


	/**
	 * Tries to resolve the value entered in the link target field as a
	 * variable, if the value is a relative path. Displays the resolved value if
	 * the entered value is a variable.
	 */
	private void resolveVariable() {
		if ("".equals(target)) {
			resolvedPathLabelText.setVisible(false);
			return;
		}

		final URI path = URIUtil.toURI(target);
		final URI resolvedPath = TITANPathUtilities.resolvePathURI(target, rootPath);
		String message = null;
		if(resolvedPath != null) {
			message = "Resolved location: " + URIUtil.toPath(resolvedPath);
		} else {
			message = "Resolved location: cannot be calculated from " + target;
		}
		resolvedPathLabelText.setText(message);
		
		
		if (path.equals(resolvedPath)) {
			resolvedPathLabelText.setVisible(false);
		} else {
			resolvedPathLabelText.setVisible(true);
		}
		composite.layout();
	}

	@Override
	protected boolean doCheckState() {
		if (isNullOrEmpty(target)) {
			return isEmptyStringAllowed();
		}
		
		final Map<?, ?> envVariables = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();

		String result;
		try {
			result = EnvironmentVariableResolver.eclipseStyle().resolve(target, envVariables);
			result = EnvironmentVariableResolver.unixStyle().resolve(result, envVariables);
		} catch (EnvironmentVariableResolver.VariableNotFoundException e) {
			setErrorMessage(e.getMessage());
			return false;
		}

		final IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
		URI uri = URIUtil.toURI(result);
		URI resolvedURI = pathVariableManager.resolveURI(uri);

		if (rootPath != null && !resolvedURI.isAbsolute()) {
			URI root = URIUtil.toURI(rootPath);
			URI absoluteURI = root.resolve(resolvedURI);

			if (absoluteURI != null && !absoluteURI.isAbsolute()) {
				setErrorMessage("Could not be resolved to an absolute path");
				return false;
			}
		}

		return true;
	}
}
