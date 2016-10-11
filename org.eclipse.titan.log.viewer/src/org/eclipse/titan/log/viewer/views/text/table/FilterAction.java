/*******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.views.text.table;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.progress.IProgressConstants;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.models.FilterPattern;
import org.eclipse.titan.log.viewer.models.TimeInterval;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.MSCFilterDialog;

class FilterAction extends Action {

	private TextTableView textTableView;
	private volatile boolean isFilterRunning = false;

	public FilterAction(TextTableView textTableView) {
		super("Filter", ImageDescriptor.createFromImage(Activator.getDefault().getIcon(Constants.ICONS_FILTER)));
		this.textTableView = textTableView;
		setId("filterTextTable");
	}

	@Override
	public void run() {
		if (isFilterRunning) {
			MessageBox msgBox = new MessageBox(textTableView.getSite().getShell());
			msgBox.setMessage("The filter is already running!");
			msgBox.setText("Filter");
			msgBox.open();
			return;
		}
		isFilterRunning = true;

		if (textTableView.getFilterPattern() == null) {
			textTableView.setFilterPattern(new FilterPattern(new TimeInterval("", "", textTableView.getLogFileMetaData().getTimeStampFormat())));
			SortedMap<String, Boolean> eventsToFilter = new TreeMap<String, Boolean>();
			for (Map.Entry<String, String[]> entry : Constants.EVENT_CATEGORIES.entrySet()) {
				eventsToFilter.put(entry.getKey(), true);
			}
			textTableView.getFilterPattern().setEventsToFilter(eventsToFilter, true, false);
		}

		MSCFilterDialog dialog = new MSCFilterDialog(textTableView.getSite().getShell(), textTableView.getFilterPattern());

		if (dialog.open() == 0 && dialog.getChanged() && !dialog.getFilterPattern().equals(textTableView.getFilterPattern())) {
			final FilterPattern tmpFilterPattern = dialog.getFilterPattern();

			WorkspaceJob op = new WorkspaceJob("Filtering") {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor)
						throws CoreException {
					try {
						textTableView.getFilteredLogReader().runFilter(tmpFilterPattern, monitor);
					} catch (IOException e) {
						ErrorReporter.logExceptionStackTrace(e);
						TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("TextTableModel.6") + e.getMessage()));  //$NON-NLS-1$
						monitor.setCanceled(true);
					} catch (ParseException e) {
						ErrorReporter.logExceptionStackTrace(e);
						TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("TextTableModel.7") + e.getMessage()));  //$NON-NLS-1$
						monitor.setCanceled(true);
					}

					if (monitor.isCanceled()) {
						monitor.done();
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								isFilterRunning = false;
								textTableView.getTable().setEnabled(true);
							}
						});
						monitor.done();
						return Status.CANCEL_STATUS;
					}

					monitor.done();
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							textTableView.setFilterPattern(tmpFilterPattern);
							textTableView.refreshTable();
							isFilterRunning = false;
							textTableView.getTable().setEnabled(true);
						}
					});
					return Status.OK_STATUS;
				}
			};

			op.setPriority(Job.LONG);
			op.setSystem(false);
			op.setUser(true);
			op.setRule(ResourcesPlugin.getWorkspace().getRoot());
			op.setProperty(IProgressConstants.ICON_PROPERTY, Activator.getImageDescriptor("titan.gif"));

			textTableView.getTable().setEnabled(false);
			op.schedule();
		} else {
			isFilterRunning = false;
		}

	}
}
