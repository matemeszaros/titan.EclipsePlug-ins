/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.designer.core.TITANJob;

/**
 * Plug-in action for "Make archive" function
 * Right click on Titan project (project properties) -> Titan -> Make archive 
 * @author Arpad Lovassy
 */
public final class MakeArchive extends ExternalTitanAction {

	private static final String JOB_TITLE = "Make archive";
	private static final String COMMAND = "make archive";
	private static final String BIN_SUBPATH = File.separatorChar + "bin" + File.separatorChar;

	@Override
	public void run(IAction action) {
		doMakeArchive();
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		doMakeArchive();
		return null;
	}

	/**
	 * This method creates the needed {@link TITANJob} and schedules it.
	 * <p>
	 * The actual work:
	 * <ul>
	 * <li>creates the command that invokes "make archive"
	 * <li>creates a TITANJob for invoking the command and redirecting the results
	 * <li>schedules the job.
	 * </ul>
	 */
	private void doMakeArchive() {
		processSelection();
		if (singleSelectedProject == null) {
			TITANConsole.println("Make archive works only for single selected project");
			return;
		}
			
		final File binDir = new File( singleSelectedProject.getLocation().toFile(), BIN_SUBPATH );
		TITANJob titanJob = new TITANJob( JOB_TITLE, new HashMap<String, IFile>(), binDir, project );
		titanJob.setPriority( Job.DECORATE );
		titanJob.setUser( true );
		titanJob.setRule( project );

		List<String> command = new ArrayList<String>();
		command.add( COMMAND );
		titanJob.addCommand( command, JOB_TITLE );

		titanJob.schedule();
	}
}
