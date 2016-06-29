/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.notification;

import static org.eclipse.titan.executor.views.executormonitor.ExecutorMonitorView.EXECUTORMONITOR_VIEW_ID;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.common.utils.ResourceUtils;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.graphics.ImageCache;
import org.eclipse.titan.executor.views.executormonitor.LaunchElement;
import org.eclipse.titan.executor.views.executormonitor.MainControllerElement;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Kristof Szabados
 * */
public final class NotificationView extends ViewPart implements ISelectionListener {
	public static final String NOTIFICATIONVIEW = Activator.PLUGIN_ID + ".views.notification.NotificationView";

	private Table table;
	private TableViewer viewer;
	private NotificationContentProvider contentProvider;
	private NotificationLabelProvider labelProvider;
	private ITreeLeaf actualInput;

	private Action clearAction;
	private Action saveAsAction;
	private Action followLastRecord;
	private boolean isFollowing = true;

	@Override
	public void createPartControl(final Composite parent) {
		table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("Info");
		column.setWidth(300);
		column.setImage(ImageCache.getImage("information2.gif"));
		column.setMoveable(true);

		column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("Component");
		column.setImage(ImageCache.getImage("information2.gif"));
		column.setMoveable(true);
		column.setWidth(50);

		column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("Type");
		column.setImage(ImageCache.getImage("information2.gif"));
		column.setMoveable(true);
		column.setWidth(50);

		column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("Timestamp");
		column.setWidth(164);
		column.setMoveable(true);
		column.setImage(ImageCache.getImage("clock.gif"));

		viewer = new TableViewer(table);
		viewer.setColumnProperties(new String[] {"Timestamp", "Type", "Component", "Info"});
		contentProvider = new NotificationContentProvider();
		viewer.setContentProvider(contentProvider);
		labelProvider = new NotificationLabelProvider();
		viewer.setLabelProvider(labelProvider);

		actualInput = getInitialInput();
		viewer.setInput(actualInput);

		createActions();
		createToolBar();
		createMenu();

		getSite().getPage().addSelectionListener(EXECUTORMONITOR_VIEW_ID, this);
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(this);
		clearAction = null;
		saveAsAction = null;
		followLastRecord = null;
		super.dispose();
	}

	private ITreeLeaf getInitialInput() {
		final ISelection selection = getSite().getPage().getSelection(EXECUTORMONITOR_VIEW_ID);
		if (null == selection || selection.isEmpty()) {
			return null;
		}

		final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		ITreeLeaf element = (ITreeLeaf) structuredSelection.getFirstElement();
		if (null != element) {
			while (!(element instanceof LaunchElement)) {
				element = element.parent();
			}
		}
		return element;
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		if (this.equals(part) || !(selection instanceof IStructuredSelection) || null == viewer.getContentProvider()) {
			return;
		}
		ITreeLeaf selectedElement;
		if (selection.isEmpty()) {
			selectedElement = null;
		} else {
			selectedElement = (ITreeLeaf) ((IStructuredSelection) selection).getFirstElement();
		}
		if (null != selectedElement) {
			while (!(selectedElement instanceof LaunchElement)) {
				selectedElement = selectedElement.parent();
			}
		}
		if (null != actualInput && actualInput.equals(selectedElement)) {
			viewer.refresh(false);
		} else {
			viewer.setInput(selectedElement);
			actualInput = selectedElement;
		}
		if (null == selectedElement) {
			setTitleToolTip(null);
			clearAction.setEnabled(false);
			saveAsAction.setEnabled(false);
		} else {
			setTitleToolTip(selectedElement.name());
			clearAction.setEnabled(true);
			saveAsAction.setEnabled(true);
		}
		if (isFollowing && null != actualInput && actualInput instanceof LaunchElement) {
			final List<ITreeLeaf> children = ((LaunchElement) actualInput).children();
			for (ITreeLeaf aChildren : children) {
				final BaseExecutor executor = ((MainControllerElement) aChildren).executor();
				if (null != executor && !executor.notifications().isEmpty()) {
					viewer.reveal(executor.notifications().get(executor.notifications().size() - 1));
				}
			}
		}
	}

	private void createActions() {
		clearAction = new Action("Clear") {
			@Override
			public void run() {
				if (null != actualInput && actualInput instanceof LaunchElement) {
					final List<ITreeLeaf> children = ((LaunchElement) actualInput).children();
					for (ITreeLeaf aChildren : children) {
						final BaseExecutor executor = ((MainControllerElement) aChildren).executor();
						if (null != executor) {
							executor.notifications().clear();
							viewer.refresh();
						}
					}
				}
			}
		};
		clearAction.setToolTipText("Clear");
		clearAction.setImageDescriptor(ImageCache.getImageDescriptor("trash_enabled.gif"));
		clearAction.setDisabledImageDescriptor(ImageCache.getImageDescriptor("trash_disabled.gif"));
		clearAction.setEnabled(false);

		saveAsAction = new Action("Save As") {
			@Override
			public void run() {
				saveAs();
			}
		};
		saveAsAction.setToolTipText("Save As");
		saveAsAction.setImageDescriptor(ImageCache.getImageDescriptor("saveas_edit.gif"));
		saveAsAction.setEnabled(false);

		followLastRecord = new Action("Follow the last record") {
			@Override
			public void run() {
				isFollowing = followLastRecord.isChecked();
			}
		};
		followLastRecord.setToolTipText("Toggles the follow the last line behavior");
		followLastRecord.setEnabled(true);
		followLastRecord.setChecked(isFollowing);
	}

	private void createToolBar() {
		final IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(clearAction);
		toolbarManager.add(saveAsAction);
	}

	private void createMenu() {
		final IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		manager.add(followLastRecord);
	}

	@Override
	public void setFocus() {
		viewer.getTable().setFocus();
	}

	private void saveAs() {
		saveLaunchElementAs(getSite().getShell(), actualInput);
	}

	/**
	 * Saves the contents of a tree starting at the provided launch config node.
	 *
	 * @param shell the shell to be used to display dialogs.
	 * @param actualInput the launch configuration to start from.
	 * */
	public static void saveLaunchElementAs(final Shell shell, final ITreeLeaf actualInput) {
		final FileDialog dialog = new FileDialog(shell, SWT.NONE);
		dialog.setFilterExtensions(new String[] {"log"});
		final String filePath = dialog.open();

		if (null == filePath) {
			return;
		}

		final File file = new File(filePath);
		if (file.exists()) {
			if (!file.canWrite()) {
				ErrorDialog.openError(shell, "Read-only file", "The file is read-only, please choose another file",
						Status.OK_STATUS);
				return;
			}
		} else {
			try {
				file.createNewFile();
			} catch (IOException e) {
				ErrorDialog.openError(shell, "Can not create file", "The file could not be created and will not be saved",
						Status.OK_STATUS);
				return;
			}
		}

		if (null == actualInput || !(actualInput instanceof LaunchElement)) {
			// nothing or wrong kind of element selected
			return;
		}

		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(file));
			final List<ITreeLeaf> children = ((LaunchElement) actualInput).children();
			for (ITreeLeaf aChildren : children) {
				final BaseExecutor executor = ((MainControllerElement) aChildren).executor();
				if (null != executor) {
					List<Notification> tempList = executor.notifications();
					for (Notification aTempList : tempList) {
						out.println(aTempList.toString());
					}
				}
			}
		} catch (IOException e) {
			ErrorDialog.openError(shell, "Can not write the file", "An error occured while writing to the file\n" + filePath,
					Status.OK_STATUS);
			return;
		} finally {
			IOUtils.closeQuietly(out);
		}

		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IFile[] outputFiles = root.findFilesForLocationURI(URIUtil.toURI(filePath));
		ResourceUtils.refreshResources(Arrays.asList(outputFiles));
	}
}
