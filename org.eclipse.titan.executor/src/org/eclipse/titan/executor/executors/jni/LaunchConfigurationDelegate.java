/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.jni;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.titan.common.product.ProductIdentity;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.executors.TitanLaunchConfigurationDelegate;
import org.eclipse.titan.executor.jni.JNIMiddleWare;

/**
 * @author Kristof Szabados
 * */
public final class LaunchConfigurationDelegate extends TitanLaunchConfigurationDelegate {

	/** { ttcn3_major, ttcn3_minor, ttcn3_patchlevel, ttcn3_buildnumber } */
	private final ProductIdentity versionLow = ProductIdentity.getProductIdentity(ProductIdentity.TITAN_PRODUCT_NUMBER, 5, 5, 1, 0);
	private final ProductIdentity versionHigh = ProductIdentity.getProductIdentity(ProductIdentity.TITAN_PRODUCT_NUMBER, 5, 5, 1, 0);

	@Override
	public void launch(final ILaunchConfiguration arg0, final String arg1, final ILaunch arg2, final IProgressMonitor arg3) throws CoreException {
		final Exception exception = JNIMiddleWare.getException();
		if (null != exception) {
			final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, exception.getMessage(), exception);
			throw new CoreException(status);
		}

		final long version = JNIMiddleWare.getSharedLibraryVersion();
		final ProductIdentity currentVersion = ProductIdentity.getProductIdentity(ProductIdentity.TITAN_PRODUCT_NUMBER, version);

		if (versionLow.compareTo(currentVersion) > 0 || versionHigh.compareTo(currentVersion) < 0) {
			Status status;
			if (versionLow.equals(versionHigh)) {
				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, "Bad version of JNI dynamic library detected.\n"
						+ "Supported version: "  + versionHigh + "\n"
						+ "Current version: " + currentVersion, null);
			} else {
				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, "Bad version of JNI dynamic library detected.\n"
						+ "Supported versions: " + versionLow + " - " + versionHigh + "\n"
						+ "Current version: " + currentVersion,
						null);
			}
			throw new CoreException(status);
		}
		if (JniExecutor.isRunning()) {
			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, "The Jni based launcher is already running.\n"
					+ "Only one instance can be running at any time.", null);
			throw new CoreException(status);
		}
		
		showExecutionPerspective();
		final JniExecutor executor = new JniExecutor(arg0);
		executor.startSession(arg2);

	}
}
