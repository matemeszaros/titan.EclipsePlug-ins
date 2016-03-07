package org.eclipse.titan.log.viewer.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.ui.IActionDelegate;

public class OpenMSCViewAction extends Action implements IActionDelegate, ISelectionChangedListener {
	private static final String NAME = Messages.getString("OpenMSCViewMenuAction.0"); //$NON-NLS-1$

	private IStructuredSelection selection;
	private LogFileMetaData logFileMetaData;
	private int recordToSelect = -1;
	
	/**
	 * Constructor
	 */
	public OpenMSCViewAction() {
		super(NAME);
	}

	@Override
	public void run(final IAction action) {
		run(selection);
	}

	public void run(final IStructuredSelection selection) {
		OpenMSCViewMenuAction menuAction = new OpenMSCViewMenuAction();
		menuAction.setFirstRow(recordToSelect);
		menuAction.run(selection);

	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			setEnabled(false);
			return;
		}
		
		this.selection = (IStructuredSelection) selection;
		if (this.selection.size() != 1 || !(this.selection.getFirstElement() instanceof TestCase)) {
			setEnabled(false);
			return;
		}
		setEnabled(true);
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		selectionChanged(null, event.getSelection());
	}
	
	/**
	 * The given record will be selected in the opening MSC view.
	 * @param recordNumber the recordNumber of the record
	 */
	public void setFirstRow(final int recordNumber) {
		this.recordToSelect = recordNumber;
	}
}
