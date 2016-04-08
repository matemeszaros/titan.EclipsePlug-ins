/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.usagestats;

import java.util.Map;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This class can send usage stats through HTTP.
 */
public class UsageStatSender {

	private static final String HOST = "ttcn.ericsson.se";
	private static final String PAGE = "/download/usage_stats/usage_stats.php";
	private static final int PORT = 80;

	private UsageStatInfoCollector infoCollector;

	public UsageStatSender(final UsageStatInfoCollector infoCollector) {
		this.infoCollector = infoCollector;
	}

	public void sendSync() {
		final Map<String, String> finalData = new PlatformDataCollector().collect();
		finalData.putAll(infoCollector.collect());
		new HttpPoster(HOST, PAGE, PORT).post(finalData);
	}

	public WorkspaceJob sendAsync() {
		final WorkspaceJob job = new WorkspaceJob("Sending Usage statistics") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				sendSync();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.DECORATE);
		job.schedule(1000);
		return job;
	}
}
