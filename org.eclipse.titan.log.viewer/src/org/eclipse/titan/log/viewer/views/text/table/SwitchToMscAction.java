package org.eclipse.titan.log.viewer.views.text.table;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.actions.OpenMSCViewMenuAction;
import org.eclipse.titan.log.viewer.extractors.TestCaseExtractor;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;

class SwitchToMscAction extends Action {
	private TextTableView textTableView;

	public SwitchToMscAction(TextTableView textTableView) {
		super("", ImageDescriptor.createFromImage(Activator.getDefault().getIcon(Constants.ICONS_MSC_VIEW)));
		this.textTableView = textTableView;
		setId("switchToMSC");
		setToolTipText("Switch to MSC view");
	}

	@Override
	public void run() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		LogFileMetaData logFileMetaData = textTableView.getLogFileMetaData();
		IProject project = root.getProject(logFileMetaData.getProjectName());
		IFile logFile = project.getFile(logFileMetaData.getProjectRelativePath().substring(logFileMetaData.getProjectName().length() + 1));

		if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
			LogFileCacheHandler.handleLogFileChange(logFile);
			return;
		}

		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		TestCaseExtractor extractor = new TestCaseExtractor();
		try {
			extractor.extractTestCasesFromIndexedLogFile(logFile);
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			MessageBox mb = new MessageBox(activePage.getActivePart().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setText("Test case extraction failed.");
			mb.setMessage("Error while extracting the test cases.");
			return;
		} catch (ClassNotFoundException e) {
			ErrorReporter.logExceptionStackTrace(e);
			MessageBox mb = new MessageBox(activePage.getActivePart().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setText("Test case extraction failed.");
			mb.setMessage("Error while extracting the test cases.");
			return;
		}

		List<TestCase> testCases = extractor.getTestCases();
		if (textTableView.getSelectedRecord() == null) {
			MessageBox mb = new MessageBox(activePage.getActivePart().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setText("Invalid selection.");
			mb.setMessage("Please select a record to open the MSC view.");
			return;
		}

		int recordNumber = textTableView.getSelectedRecord().getRecordNumber();
		int testCaseNumber = findContainingTestCase(testCases, recordNumber);

		if (testCaseNumber == -1) {
			MessageBox mb = new MessageBox(activePage.getActivePart().getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setText("Testcase can not be found.");
			mb.setMessage("The testcase containing the selected log record can not be found.");
			return;
		}

		final OpenMSCViewMenuAction openMSCAction = new OpenMSCViewMenuAction();
		openMSCAction.selectionChanged(null, new StructuredSelection(testCases.get(testCaseNumber)));
		openMSCAction.setFirstRow(recordNumber);
		openMSCAction.run();
	}

	private int findContainingTestCase(List<TestCase> testCases, int recordNumber) {
		int testCaseNumber = -1;
		for (int min = 0, max = testCases.size() - 1, mid = (min + max) / 2;
				min <= max;
				mid = (min + max) / 2) {

			if (recordNumber > testCases.get(mid).getEndRecordNumber()) {
				min = mid + 1;
			} else if (recordNumber < testCases.get(mid).getStartRecordNumber()) {
				max = mid - 1;
			} else {
				testCaseNumber = mid;
				break;
			}
		}
		return testCaseNumber;
	}
}
