package org.eclipse.titanium.refactoring.expandvaluelistnotation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * Wizard for the 'Minimize visibility modifiers' refactoring operation.
 * 
 * @author Zsolt Tabi
 */
public class ExpandFieldNamesWizard extends RefactoringWizard implements
		IExecutableExtension {
	
	private static final String WIZ_WINDOWTITLE = "Expand record field names";

	ExpandFieldNamesWizard(Refactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE);
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		
	}

	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle(WIZ_WINDOWTITLE);
	}

}
