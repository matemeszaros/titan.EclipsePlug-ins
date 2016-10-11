/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.extensions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * @author Daniel Poroszkai
 * */
public enum ExtensionHandler {
	INSTANCE;
	
	private static final String ID = "org.eclipse.titan.designer.extensions.post_analyze";
	private List<IProjectProcesser> plugins;
	
	public synchronized void registerContributors() {
		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(ID); 
		plugins = new ArrayList<IProjectProcesser>();
		for (IConfigurationElement element : config) {
			try {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof IProjectProcesser) {
					plugins.add((IProjectProcesser) o);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While adding extension with value `" + element.getValue() + "'", e);
			}
		}
	}

	public void executeContributors(final IProgressMonitor monitor, final IProject project) {
		final SubMonitor progress = SubMonitor.convert(monitor);

		ISafeRunnable runnable = new ISafeRunnable() {
			@Override
			public void handleException(final Throwable e) {
				ErrorReporter.logExceptionStackTrace("Error in client plugin while processing project `" + project.getName() + "'", e);
			}

			@Override
			public void run() throws Exception {
				try {
					progress.beginTask("Executing extensions", plugins.size());
					for (IProjectProcesser proc : plugins) {
						proc.workOnProject(progress.newChild(1), project);
						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}
					}
				} finally {
					progress.done();
				}
			}
		};
		SafeRunner.run(runnable);
	}

}
