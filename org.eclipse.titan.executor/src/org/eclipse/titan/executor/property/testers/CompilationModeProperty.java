/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.property.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.executor.designerconnection.DesignerHelper;

/**
 * @author Kristof Szabados
 * */
public final class CompilationModeProperty extends PropertyTester {
	private static final String CHECKSINGLEMODEPROPERTY = "SingleModeBuild";
	private static final String SINGLEMODE_BUILDPROPERTY = "singleMode";

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (!(receiver instanceof IResource)) {
			return false;
		}

		final IResource resource = (IResource) receiver;
		String temp;

		try {
			temp = resource.getProject().getPersistentProperty(new QualifiedName(DesignerHelper.PROJECTPROPERTIESQUALIFIER, SINGLEMODE_BUILDPROPERTY));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false;
		}

		if (CHECKSINGLEMODEPROPERTY.equals(property)) {
			return "true".equals(temp);
		}

		return !"true".equals(temp);
	}
}
