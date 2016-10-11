/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
/**
 * @author Kristof Szabados
 * */
public final class TemplateInstance_InternalLogArgument extends InternalLogArgument {
	private TemplateInstance templateInstance;

	public TemplateInstance_InternalLogArgument(final TemplateInstance templateInstance) {
		super(ArgumentType.TemplateInstance);
		this.templateInstance = templateInstance;
	}

	public TemplateInstance getTemplate() {
		return templateInstance;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (templateInstance == null) {
			return;
		}

		templateInstance.checkRecursions(timestamp, referenceChain);
	}
}
