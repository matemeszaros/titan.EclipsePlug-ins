/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import java.io.File;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.product.ProductIdentity;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.core.CompilerVersionInformationCollector;
import org.eclipse.titan.designer.core.ProductIdentityHelper;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.core.TITANInstallationValidator;
import org.eclipse.titan.designer.license.License;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * This preference page hold the controls and functionality to set the TITAN
 * related options.
 * 
 * @author Kristof Szabados
 */
public final class TITANPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	static final String DESCRIPTION = "Preferences for TITAN";
	static final String INSTALL_PATH = "TITAN installation path:";
	static final String INSTALL_PATH_TOOLTIP = "The directory TITAN was installed into.\n"
			+ "This is the same directory which must be set in the $TTCN3_DIR environmental variable"
			+ " in case of using the tools from command line.";
	static final String LICENSE_FILE = "License file :";
	static final String LICENSE_FILE_TOOLTIP = "The path to a valid license file.\n"
			+ "This is the same file which must be set in the $TTCN3_LICENSE_FILE environmental variable"
			+ " in case of using the tools from command line";
	static final String PROBLEM_WITH_MARKER = "Use markers for build error notification instead of a dialog.";
	static final String DEBUGINFORMATION = "Display debug information.";

	private static final String[][] COMPILER_ERROR_OPTIONS = new String[][] {
			{ PreferenceConstantValues.COMPILEROPTIONSTAY, PreferenceConstantValues.COMPILEROPTIONSTAY },
			{ PreferenceConstantValues.COMPILEROPTIONOUTDATE, PreferenceConstantValues.COMPILEROPTIONOUTDATE },
			{ PreferenceConstantValues.COMPILEROPTIONREMOVE, PreferenceConstantValues.COMPILEROPTIONREMOVE } };

	private static final String[][] ONTHEFLY_ERROR_OPTIONS = new String[][] {
			{ PreferenceConstantValues.ONTHEFLYOPTIONREMOVE, PreferenceConstantValues.ONTHEFLYOPTIONREMOVE },
			{ PreferenceConstantValues.ONTHEFLYOPTIONSTAY, PreferenceConstantValues.ONTHEFLYOPTIONSTAY } };
	
	private static final String[][] CONSOLE_ACTION_BEFORE_BUILD = new String[][] {
		{PreferenceConstantValues.BEFORE_BUILD_NOTHING_TO_DO,PreferenceConstantValues.BEFORE_BUILD_NOTHING_TO_DO},
		{PreferenceConstantValues.BEFORE_BUILD_CLEAR_CONSOLE, PreferenceConstantValues.BEFORE_BUILD_CLEAR_CONSOLE},
		{PreferenceConstantValues.BEFORE_BUILD_PRINT_CONSOLE_DELIMITERS, PreferenceConstantValues.BEFORE_BUILD_PRINT_CONSOLE_DELIMITERS}
	};

	private static final String LICENSERENEWALTEXT = "<A> Prolong expired license (only for Ericsson employees) </A>";
	private RenewLicense renewLicense = new RenewLicense();

	static {
		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (!PreferenceConstants.DEBUG_PREFERENCE_PAGE_ENABLED.equals(property)) {
						return;
					}

					final IPreferencesService prefService = Platform.getPreferencesService();
					final boolean enable = prefService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.DEBUG_PREFERENCE_PAGE_ENABLED, false, null);
					Activator.switchActivity(GeneralConstants.ACTIVITY_DEBUG, enable);

					// Refresh the preference page
					// tree if it is opened at the moment
					if (!PlatformUI.isWorkbenchRunning()) {
						return;
					}

					final IWorkbench wb = PlatformUI.getWorkbench();
					if (wb == null) {
						return;
					}

					wb.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							final Shell active = wb.getDisplay().getActiveShell();
							if (active == null) {
								return;
							}

							Object o = active.getData();
							if (o instanceof PreferenceDialog) {
								final PreferenceDialog d = (PreferenceDialog) o;
								d.getTreeViewer().refresh();
							}
						}
					});
				}
			});
		}
	}

	private DirectoryFieldEditor installPath;
	private FileFieldEditor licenseFile;
	private BooleanFieldEditor reportProgramErrorWithMarker;
	private BooleanFieldEditor treatOnTheFlyErrorsFatalforBuild;
	private IntegerFieldEditor processingUnitsToUse;
	private BooleanFieldEditor displayDebugPreferences;//FIXME: remove this functionality!

	private Composite comp;
	private Label titanVersionInformation;
	private Label licenseInfoLabel;
	private Text licenseInfo;
	private Font licenseInfoFont;
	private Link link;

	private ProductIdentity compilerProductNumber;

	private class CreateNewLicense extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			openUrl("https://tcc-licorder.rnd.ki.sw.ericsson.se/order_form.php?type=ericsson");
		}
	}

	private static final String NEWLICENSETEXT = "<A> Order a new license (only for Ericsson employees) </A>";
	private CreateNewLicense createNewLicense = new CreateNewLicense();

	private class RenewLicense extends SelectionAdapter {
		private int uniqueID;

		public void setUniqueID(final int licenseID) {
			uniqueID = licenseID;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			openUrl("https://tcc-licorder.rnd.ki.sw.ericsson.se/order_form.php?type=ericsson"); //licenseID has been removed
		}
	}

	public TITANPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected Control createContents(final Composite parent) {
		comp = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		super.createContents(comp);

		String temp = CompilerVersionInformationCollector.getCompilerProductNumber();
		compilerProductNumber = ProductIdentityHelper.getProductIdentity(temp, null);

		titanVersionInformation = new Label(comp, SWT.NONE);
		titanVersionInformation.setText("The version of the compiler used: unknown");

		if ( License.isLicenseNeeded() ) {
			licenseInfoLabel = new Label(comp, SWT.NONE);
			licenseInfoLabel.setText("License information:");

			licenseInfo = new Text(comp, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI | SWT.READ_ONLY);
			licenseInfo.setEditable(false);
			licenseInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			licenseInfoFont = new Font(getShell().getDisplay(), "Courier New", 8, SWT.NORMAL);
			licenseInfo.setFont(licenseInfoFont);
		}

		link = new Link(comp, SWT.NONE);
		link.setVisible(false);
		link.setEnabled(false);

		refreshTITANInfo();
		refreshLicenseInfo();

		return comp;
	}

	/**
	 * Refreshes the information displayed on this page related to the
	 * version of TITAN used currently.
	 * */
	private void refreshTITANInfo() {
		if (titanVersionInformation == null) {
			return;
		}

		String tempPath = installPath.getStringValue();
		if (tempPath == null || tempPath.length() == 0) {
			return;
		}

		String temp = installPath.getStringValue() + CompilerVersionInformationCollector.COMPILER_SUBPATH;
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			temp = temp + ".exe";
		}
		IPath compilerPath = new Path(temp);

		if (!compilerPath.toFile().exists()) {
			installPath.getTextControl(getFieldEditorParent()).setToolTipText(
					INSTALL_PATH_TOOLTIP + "\n The compiler was not found on the path `" + compilerPath.toOSString() + "'");
			titanVersionInformation.setText("The compiler was not found on the path `" + compilerPath.toOSString() + "'");
			setErrorMessage("The compiler was not found on the path `" + compilerPath.toOSString() + "'");
			return;
		}

		String tempCompilerProductNumber = CompilerVersionInformationCollector.checkTemporalLocation(installPath.getStringValue());
		if (tempCompilerProductNumber == null) {
			installPath.getTextControl(getFieldEditorParent()).setToolTipText(
					INSTALL_PATH_TOOLTIP + "\n The version of the used compiler could not be determined.");
			titanVersionInformation.setText("The version of the compiler used: could not be determined");
			setErrorMessage("The version of the compiler used: could not be determined");
			return;
		}

		ProductIdentity versionNumber = ProductIdentityHelper.getProductIdentity(tempCompilerProductNumber, null);
		if (versionNumber == null) {
			installPath.getTextControl(getFieldEditorParent())
					.setToolTipText(INSTALL_PATH_TOOLTIP
							+ "\n The version of the used compiler seems to be invalid/corrupted or there were problems while processing it.");
			titanVersionInformation
					.setText("The version of the compiler used: seems to be invalid/corrupted or there were problems while processing it");
			setErrorMessage("The version of the compiler used: seems to be invalid/corrupted or there were problems while processing it");
			return;
		}

		installPath.getTextControl(getFieldEditorParent()).setToolTipText(
				INSTALL_PATH_TOOLTIP + "\n The version of the used compiler is: " + versionNumber);
		titanVersionInformation.setText("The version of the compiler used: " + versionNumber);
	}

	/**
	 * Refreshes the information displayed on this page related to the
	 * license file set on this page currently.
	 * */
	public void refreshLicenseInfo() {
		if ( !License.isLicenseNeeded() ) {
			return;
		}
		
		if (licenseFile == null || licenseInfo == null) {
			createLinkNewLicense();
			return;
		}

		String fileName = licenseFile.getStringValue();
		if (fileName == null || fileName.length() == 0) {
			licenseInfo.setText("No license file provided");
			setErrorMessage("No license file provided");
			createLinkNewLicense();
			return;
		}

		if (!licenseFile.isValid()) {
			licenseInfo.setText("The license file seems to be corrupted, or not available on the provided path");
			setErrorMessage("The license file seems to be corrupted, or not available on the provided path");
			createLinkNewLicense();
			return;
		}

		File realFile = new File(fileName);
		if (!realFile.exists()) {
			licenseInfo.setText("File not found");
			setErrorMessage("File not found");
			createLinkNewLicense();
			return;
		}

		if (!realFile.isFile()) {
			licenseInfo.setText("A file was expected as license file");
			setErrorMessage("A file was expected as license file");
			createLinkNewLicense();
			return;
		}

		License license = new License(fileName);
		license.process();

		if (!license.isValid()) {
			licenseInfo.setText("The license file is invalid");
			setErrorMessage("The license file is invalid");
			createLinkNewLicense();
			return;
		}

		long validUntil = license.getValidUntil().getTime();
		long now = System.currentTimeMillis();

		StringBuilder builder = new StringBuilder();

		if (now > validUntil) {
			builder.append("Your TITAN license has expired\n\n");
			setErrorMessage("Your TITAN license has expired");
			createLinkLicenseRenewal(license.getUniqueID());
		} else {
			final long difference = (validUntil - now) / LicenseValidator.MILLISECONDS_IN_A_DAY;
			if (difference == 1) {
				builder.append("Your TITAN license will expire today\n\n");
				createLinkLicenseRenewal(license.getUniqueID());
			} else if (difference < LicenseValidator.EXPIRATION_WARNING_TIMEOUT) {
				builder.append("Please note that your TITAN license will expire in ").append(difference).append(" days.\n\n");
				createLinkLicenseRenewal(license.getUniqueID());
			} else if (link != null) {
				link.setVisible(false);
				link.setEnabled(false);
				link.setText("");
			}
		}

		builder.append(license.toString());

		licenseInfo.setText(builder.toString());
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		installPath = new DirectoryFieldEditor(PreferenceConstants.TITAN_INSTALLATION_PATH, INSTALL_PATH, parent);
		installPath.getTextControl(parent).setToolTipText(INSTALL_PATH_TOOLTIP);
		installPath.getTextControl(parent).addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				setErrorMessage(null);
				CompilerVersionInformationCollector.clearStoredInformation();
				refreshTITANInfo();
				refreshLicenseInfo();
			}
		});
		addField(installPath);

		parent = getFieldEditorParent();
		if ( License.isLicenseNeeded() ) {
			licenseFile = new FileFieldEditor(PreferenceConstants.LICENSE_FILE_PATH, LICENSE_FILE, parent);
			licenseFile.getTextControl(parent).setToolTipText(LICENSE_FILE_TOOLTIP);
			licenseFile.getTextControl(parent).addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(final ModifyEvent e) {
					setErrorMessage(null);
					CompilerVersionInformationCollector.clearStoredInformation();
					refreshTITANInfo();
					refreshLicenseInfo();
				}
			});
			addField(licenseFile);
		}

		reportProgramErrorWithMarker = new BooleanFieldEditor(PreferenceConstants.REPORTPROGRAMERRORWITHMARKER, PROBLEM_WITH_MARKER,
				getFieldEditorParent());
		addField(reportProgramErrorWithMarker);

		treatOnTheFlyErrorsFatalforBuild = new BooleanFieldEditor(PreferenceConstants.TREATONTHEFLYERRORSFATALFORBUILD,
				"Treat on-the-fly errors as fatal for build (the project will not build).", getFieldEditorParent());
		addField(treatOnTheFlyErrorsFatalforBuild);

		
		ComboFieldEditor comboedit = new ComboFieldEditor(PreferenceConstants.COMPILERMARKERSAFTERANALYZATION,
				"When On-the-Fly analyzation ends the compiler markers:", COMPILER_ERROR_OPTIONS, getFieldEditorParent());
		Label text = comboedit.getLabelControl(getFieldEditorParent());
		text.setToolTipText("Keeping the compiler markers can be good for consistency, but might lead to outdated error reports.");
		addField(comboedit);
		
		//"When the compiler runs the on-the-fly markers:"
		ComboFieldEditor comboedit2 = new ComboFieldEditor(PreferenceConstants.ONTHEFLYMARKERSAFTERCOMPILER, "When the compiler runs the on-the-fly markers:",
				ONTHEFLY_ERROR_OPTIONS, getFieldEditorParent());
		Label text2 = comboedit2.getLabelControl(getFieldEditorParent());
		text2.setToolTipText("Keeping the on-the-fly marker is good for performance, but right now the compiler is more reliable.");
		addField(comboedit2);

		processingUnitsToUse = new IntegerFieldEditor(PreferenceConstants.PROCESSINGUNITSTOUSE, "Maximum number of build processes to use:",
				getFieldEditorParent());
		processingUnitsToUse.getLabelControl(getFieldEditorParent()).setToolTipText(
				"Maximum number of processors available right now: " + PreferenceConstantValues.AVAILABLEPROCESSORS);
		processingUnitsToUse.setValidRange(1, PreferenceConstantValues.AVAILABLEPROCESSORS + 1);
		addField(processingUnitsToUse);

		displayDebugPreferences = new BooleanFieldEditor(PreferenceConstants.DEBUG_PREFERENCE_PAGE_ENABLED, "Display debug preferences",
				getFieldEditorParent());
		addField(displayDebugPreferences);
		
		//"Action on the console before build"
		ComboFieldEditor comboedit3 = new ComboFieldEditor(PreferenceConstants.CONSOLE_ACTION_BEFORE_BUILD, "Action on the console before build:",
				CONSOLE_ACTION_BEFORE_BUILD, getFieldEditorParent());
		Label text3 = comboedit3.getLabelControl(getFieldEditorParent());
		text3.setToolTipText("Select what to do in the TITANConsole before starting the build to easier find the starting point of the actual build");
		addField(comboedit3);
		
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		installPath.dispose();
		if (licenseFile != null) {
			licenseFile.dispose();
		}
		reportProgramErrorWithMarker.dispose();
		treatOnTheFlyErrorsFatalforBuild.dispose();
		processingUnitsToUse.dispose();
		displayDebugPreferences.dispose();
		comp.dispose();
		if (licenseInfoLabel != null) {
			licenseInfoLabel.dispose();
		}
		if (licenseInfo != null) {
			licenseInfo.dispose();
		}
		if (licenseInfoFont != null) {
			licenseInfoFont.dispose();
		}
		if (link != null) {
			link.dispose();
		}
		super.dispose();
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(ProductConstants.PRODUCT_ID_DESIGNER);
		if (node != null) {
			try {
				node.flush();
			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		CompilerVersionInformationCollector.clearStoredInformation();
		String temp = CompilerVersionInformationCollector.getCompilerProductNumber();
		ProductIdentity finalVersion = ProductIdentityHelper.getProductIdentity(temp, null);
		if (compilerProductNumber != null && compilerProductNumber.compareTo(finalVersion) != 0) {
			ErrorReporter.parallelWarningDisplayInMessageDialog(
				"The compiler version has changed",
				"All projects are cleaned, so that files generated by the old compiler shall not corrupt further builds.\nThis might take some time.");

			compilerProductNumber = finalVersion;
			// remove all generated Makefiles if the TITAN version
			// has changed.
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

			for (IProject project : projects) {
				if (project.isAccessible() && TITANBuilder.isBuilderEnabled(project)) {
					TITANBuilder.cleanProjectForRebuild(project, false);
				}
			}
		}

		TITANInstallationValidator.clear();
		LicenseValidator.clear();

		return result;
	}

	private void createLinkNewLicense() {
		if (link == null || NEWLICENSETEXT.equals(link.getText())) {
			return;
		}

		link.setText(NEWLICENSETEXT);
		link.removeSelectionListener(renewLicense);
		link.addSelectionListener(createNewLicense);
		link.setVisible(true);
		link.setEnabled(true);

		comp.layout();
	}

	private void createLinkLicenseRenewal(final int licenseID) {
		if (link == null || LICENSERENEWALTEXT.equals(link.getText())) {
			return;
		}

		link.setText(LICENSERENEWALTEXT);
		link.removeSelectionListener(createNewLicense);
		renewLicense.setUniqueID(licenseID);
		link.addSelectionListener(renewLicense);
		link.setVisible(true);
		link.setEnabled(true);

		comp.layout();
	}

	private void openUrl(final String url) {
		try {
			IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
			support.getExternalBrowser().openURL(new URL(url));
		} catch (Exception e) {
			ErrorReporter.logError("Could not open URL in an external browser [" + url + "]");
		}
	}
}
