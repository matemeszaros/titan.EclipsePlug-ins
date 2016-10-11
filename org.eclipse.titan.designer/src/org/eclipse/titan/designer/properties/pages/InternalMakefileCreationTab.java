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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author Kristof Szabados
 * */
public final class InternalMakefileCreationTab {
	private TabItem newBuildPropertiesTabItem;
	private Composite newBuildPropertiesComposite;
	private TreeViewer optionList;

	private final TTCN3PreprocessorOptionsPage ttcn3PreprocessorPage = new TTCN3PreprocessorOptionsPage();
	private final PreprocessorSymbolsOptionsPage ttcn3PreprocessorSymbolsPage = new PreprocessorSymbolsOptionsPage(true);
	private final PreprocessorIncludedOptionsPage ttcn3PreprocessorIncludesPage;
	private final TITANFlagsOptionsPage titanFlagsPage = new TITANFlagsOptionsPage();
	private final PreprocessorOptionsPage preprocessorPage = new PreprocessorOptionsPage();
	private final PreprocessorSymbolsOptionsPage preprocessorSymbolsPage = new PreprocessorSymbolsOptionsPage(false);
	private final PreprocessorIncludedOptionsPage preprocessorIncludesPage;
	private final CCompilerOptionsPage cCompilerPage = new CCompilerOptionsPage();
	private final COptimalizationOptionsPage optimizationPage = new COptimalizationOptionsPage();
	private final PlatformSpecificLibrariesOptionsPage solarisLibrariesPage = new PlatformSpecificLibrariesOptionsPage("Solaris");
	private final PlatformSpecificLibrariesOptionsPage solaris8LibrariesPage = new PlatformSpecificLibrariesOptionsPage("Solaris8");
	private final PlatformSpecificLibrariesOptionsPage freeBSDLibrariesPage = new PlatformSpecificLibrariesOptionsPage("FreeBSD");
	private final PlatformSpecificLibrariesOptionsPage linuxLibrariesPage = new PlatformSpecificLibrariesOptionsPage("Linux");
	private final PlatformSpecificLibrariesOptionsPage win32LibrariesPage = new PlatformSpecificLibrariesOptionsPage("Win32");
	private final LinkerOptionsPage linkerPage = new LinkerOptionsPage();
	private final LinkerLibrariesOptionsPage linkerLibrariesPage;
	private final LinkerFlagsOptionsPage linkerFlagsOptionsPage = new LinkerFlagsOptionsPage();

	private final IOptionsPage[] pages;

	@SuppressWarnings("unused")
	private IProject project;
	private IOptionsPage actualPage = null;
	private Composite settingsPageContainer;
	private ScrolledComposite containerSC;

	public InternalMakefileCreationTab(final IProject project) {
		this.project = project;
		ttcn3PreprocessorIncludesPage = new PreprocessorIncludedOptionsPage(project, true);
		preprocessorIncludesPage = new PreprocessorIncludedOptionsPage(project, false);
		linkerLibrariesPage = new LinkerLibrariesOptionsPage(project);

		pages = new IOptionsPage[] { ttcn3PreprocessorPage, ttcn3PreprocessorSymbolsPage, ttcn3PreprocessorIncludesPage, titanFlagsPage,
				preprocessorPage, preprocessorSymbolsPage, preprocessorIncludesPage, cCompilerPage, optimizationPage,
				solarisLibrariesPage, solaris8LibrariesPage, freeBSDLibrariesPage, linuxLibrariesPage, win32LibrariesPage,
				linkerPage, linkerLibrariesPage, linkerFlagsOptionsPage};
	}

	/**
	 * Disposes the SWT resources allocated by this tab page.
	 */
	public void dispose() {
		newBuildPropertiesTabItem.dispose();
		newBuildPropertiesComposite.dispose();
		settingsPageContainer.dispose();
		containerSC.dispose();

		for (IOptionsPage page : pages) {
			page.dispose();
		}
	}

	protected TabItem createContents(final TabFolder tabFolder) {
		newBuildPropertiesTabItem = new TabItem(tabFolder, SWT.BORDER);
		newBuildPropertiesTabItem.setText("Internal makefile creation attributes");
		newBuildPropertiesTabItem.setToolTipText("Settings controlling the new generation of the makefile.");

		newBuildPropertiesComposite = new Composite(tabFolder, SWT.MULTI);
		newBuildPropertiesComposite.setEnabled(true);
		newBuildPropertiesComposite.setLayout(new GridLayout());

		SashForm sashForm = new SashForm(newBuildPropertiesComposite, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		optionList = new TreeViewer(sashForm, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		optionList.setContentProvider(new OptionElementContentProvider());
		optionList.setLabelProvider(new OptionElementLabelProvider());
		optionList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				handleSelectionChanged();
			}
		});

		OptionElement root = new OptionElement("root");

		OptionElement ttcn3preprocessor = new OptionElement("TTCN-3 Preprocessor", ttcn3PreprocessorPage);
		root.addChild(ttcn3preprocessor);
		ttcn3preprocessor.addChild(new OptionElement("Symbols (define, undefine)", ttcn3PreprocessorSymbolsPage));
		ttcn3preprocessor.addChild(new OptionElement("Included directories", ttcn3PreprocessorIncludesPage));

		OptionElement titan = new OptionElement("TITAN");
		root.addChild(titan);
		titan.addChild(new OptionElement("Flags", titanFlagsPage));

		OptionElement preprocessor = new OptionElement("Preprocessor", preprocessorPage);
		root.addChild(preprocessor);
		preprocessor.addChild(new OptionElement("Symbols (define, undefine)", preprocessorSymbolsPage));
		preprocessor.addChild(new OptionElement("Included directories", preprocessorIncludesPage));

		OptionElement compiler = new OptionElement("C++ compiler", cCompilerPage);
		root.addChild(compiler);
		compiler.addChild(new OptionElement("Optimization", optimizationPage));

		OptionElement platform = new OptionElement("Platform specific libraries");
		root.addChild(platform);
		platform.addChild(new OptionElement("Solaris", solarisLibrariesPage));
		platform.addChild(new OptionElement("Solaris8", solaris8LibrariesPage));
		platform.addChild(new OptionElement("FreeBSD", freeBSDLibrariesPage));
		platform.addChild(new OptionElement("Linux", linuxLibrariesPage));
		platform.addChild(new OptionElement("Win32", win32LibrariesPage));

		OptionElement linker = new OptionElement("Linker", linkerPage);
		root.addChild(linker);
		linker.addChild(new OptionElement("Libraries", linkerLibrariesPage));
		linker.addChild(new OptionElement("Options", linkerFlagsOptionsPage ));

		optionList.setInput(root);

		containerSC = new ScrolledComposite(sashForm, SWT.H_SCROLL | SWT.V_SCROLL);
		containerSC.setExpandHorizontal(true);
		containerSC.setExpandVertical(true);

		settingsPageContainer = new Composite(containerSC, SWT.NULL);
		settingsPageContainer.setLayout(new GridLayout());
		GridData data = new GridData();
		data.exclude = true;
		settingsPageContainer.setLayoutData(data);

		containerSC.setContent(settingsPageContainer);
		containerSC.setMinSize(settingsPageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite comp;
		for (IOptionsPage page : pages) {
			comp = page.createContents(settingsPageContainer);
			comp.setVisible(false);
			((GridData) comp.getLayoutData()).exclude = true;
		}

		settingsPageContainer.layout();

		newBuildPropertiesTabItem.setControl(newBuildPropertiesComposite);
		return newBuildPropertiesTabItem;
	}

	private void handleSelectionChanged() {
		if (optionList == null) {
			return;
		}

		IStructuredSelection selection = (IStructuredSelection) optionList.getSelection();
		OptionElement element = (OptionElement) selection.getFirstElement();
		if (element != null) {
			IOptionsPage next = element.page;

			if (next != null) {
				Composite comp;
				if (actualPage != null) {
					comp = actualPage.createContents(settingsPageContainer);
					comp.setVisible(false);
					((GridData) comp.getLayoutData()).exclude = true;
				}

				comp = next.createContents(settingsPageContainer);
				comp.setVisible(true);
				((GridData) comp.getLayoutData()).exclude = false;

				actualPage = next;

				containerSC.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				settingsPageContainer.layout();
			} else {
				Composite comp;
				if (actualPage != null) {
					comp = actualPage.createContents(settingsPageContainer);
					comp.setVisible(false);
					((GridData) comp.getLayoutData()).exclude = true;
				}

				actualPage = null;

				containerSC.setMinSize(0, 0);
				settingsPageContainer.layout();
			}
		}
	}

	/**
	 * Handles the enabling/disabling of controls when the automatic
	 * Makefile generation is enabled/disabled.
	 * 
	 * @param value
	 *                the actual state of automatic Makefile generation.
	 * */
	// FIXME this page can always be edited ... this change needs to be
	// applied
	protected void setMakefileGenerationEnabled(final boolean value) {
		if (optionList != null) {
			optionList.getControl().setEnabled(value);
		}
		if (actualPage != null) {
			actualPage.setEnabled(value);
		}
	}

	/**
	 * Copies the actual values into the provided preference storage.
	 * 
	 * @param project
	 *                the actual project (the real preference store).
	 * @param tempStorage
	 *                the temporal store to copy the values to.
	 * */
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		for (IOptionsPage page : pages) {
			page.copyPropertyStore(project, tempStorage);
		}
	}

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
	public boolean evaluatePropertyStore(final IProject project, final PreferenceStore tempStorage) {
		boolean result = false;

		for (IOptionsPage page : pages) {
			result |= page.evaluatePropertyStore(project, tempStorage);
		}

		return result;
	}

	/**
	 * Performs special processing when the ProjectBuildProperty page's
	 * Defaults button has been pressed.
	 */
	protected void performDefaults() {
		for (IOptionsPage page : pages) {
			page.performDefaults();
		}
		setMakefileGenerationEnabled(true);
	}

	/**
	 * Checks the properties of this page for errors.
	 * 
	 * @param page
	 *                the page to report errors to.
	 * @return true if no error was found, false otherwise.
	 * */
	public boolean checkProperties(final ProjectBuildPropertyPage page) {
		boolean result = true;

		for (int i = 0; i < pages.length; i++) {
			result &= pages[i].checkProperties(page);
		}

		return result;
	}

	/**
	 * Loads the properties from the property storage, into the user
	 * interface elements.
	 * 
	 * @param project
	 *                the project to load the properties from.
	 * */
	public void loadProperties(final IProject project) {
		for (IOptionsPage page : pages) {
			page.loadProperties(project);
		}
	}

	/**
	 * Saves the properties to the property storage, from the user interface
	 * elements.
	 * 
	 * @param project
	 *                the project to save the properties to.
	 * @return true if the save was successful, false otherwise.
	 * */
	public boolean saveProperties(final IProject project) {
		boolean result = true;

		for (IOptionsPage page : pages) {
			result &= page.saveProperties(project);
		}

		return result;
	}
}
