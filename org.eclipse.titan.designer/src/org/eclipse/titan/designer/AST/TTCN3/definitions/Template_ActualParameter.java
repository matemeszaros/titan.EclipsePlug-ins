/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an actual parameter that has a Template as its actual value.
 * 
 * @author Kristof Szabados
 * */
public final class Template_ActualParameter extends ActualParameter {

	private final TemplateInstance template;

	public Template_ActualParameter(final TemplateInstance template) {
		this.template = template;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (template != null) {
			template.setMyScope(scope);
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (template == null) {
			return;
		}

		Reference derivedReference = template.getDerivedReference();
		if (derivedReference != null) {
			ISubReference subReference = derivedReference.getSubreferences().get(0);
			if (subReference instanceof ParameterisedSubReference) {
				ActualParameterList parameterList = ((ParameterisedSubReference) subReference).getActualParameters();
				if (parameterList != null) {
					parameterList.checkRecursions(timestamp, referenceChain);
				}
			}
		}

		referenceChain.markState();
		template.getTemplateBody().checkRecursions(timestamp, referenceChain);
		referenceChain.previousState();
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (template != null) {
			template.updateSyntax(reparser, false);
			reparser.updateLocation(template.getLocation());
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (template != null) {
			if (!template.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
