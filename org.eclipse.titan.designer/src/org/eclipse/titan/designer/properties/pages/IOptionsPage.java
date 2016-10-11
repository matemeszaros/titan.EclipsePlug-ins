/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Composite;

/**
 * The interface of the Options pages.
 * 
 * @author Kristof Szabados
 * */
public interface IOptionsPage {
	/**
	 * Created the user interface elements of the page, and returns the main
	 * composite.
	 * 
	 * @param parent
	 *                the parent composite.
	 * @return the composite of the page.
	 * */
	Composite createContents(final Composite parent);

	/**
	 * Disposes the SWT resources allocated by this options page.
	 */
	void dispose();

	/**
	 * Enables the receiver if the argument is <code>true</code>, and
	 * disables it otherwise. A disabled control is typically not selectable
	 * from the user interface and draws with an inactive or "grayed" look.
	 * 
	 * @see Control#setEnabled(boolean)
	 * 
	 * @param enabled
	 *                the new enabled state
	 * */
	void setEnabled(final boolean enabled);

	/**
	 * Copies the actual values into the provided preference storage.
	 * 
	 * @param project
	 *                the actual project (the real preference store).
	 * @param tempStorage
	 *                the temporal store to copy the values to.
	 * */
	void copyPropertyStore(final IProject project, final PreferenceStore tempStorage);

	/**
	 * Evaluates the properties on the option page, and compares them with
	 * the saved values.
	 * 
	 * @param project
	 *                the actual project (the real preference store).
	 * @param tempStorage
	 *                the temporal store to copy the values to.
	 * 
	 * @return true if the values in the real and the temporal storage are
	 *         different (they have changed), false otherwise.
	 * */
	boolean evaluatePropertyStore(final IProject project, final PreferenceStore tempStorage);

	/**
	 * Performs special processing when the ProjectBuildProperty page's
	 * Defaults button has been pressed.
	 */
	void performDefaults();

	/**
	 * Checks the properties of this page for errors.
	 * 
	 * @param page
	 *                the page to report errors to.
	 * @return true if no error was found, false otherwise.
	 * */
	boolean checkProperties(final ProjectBuildPropertyPage page);

	/**
	 * Loads the properties from the property storage, into the user
	 * interface elements.
	 * 
	 * @param project
	 *                the project to load the properties from.
	 * */
	void loadProperties(final IProject project);

	/**
	 * Saves the properties to the property storage, from the user interface
	 * elements.
	 * 
	 * @param project
	 *                the project to save the properties to.
	 * @return true if the save was successful, false otherwise.
	 * */
	boolean saveProperties(final IProject project);
}
