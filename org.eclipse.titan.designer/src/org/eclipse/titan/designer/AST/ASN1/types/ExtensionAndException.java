/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * ExtensionAndException.
 * <p>
 * originally ExtAndExc in TITAN
 * 
 * @author Kristof Szabados
 */
public final class ExtensionAndException extends ASTNode {
	private static final String FULLNAMEPART = ".<exception>";

	/** optional exception specification. */
	private ExceptionSpecification exceptionSpecification;
	private ExtensionAdditions extensionAdditions;

	public ExtensionAndException(final ExceptionSpecification exceptionSpecification, final ExtensionAdditions extensionAdditions) {
		this.exceptionSpecification = exceptionSpecification;
		this.extensionAdditions = (null != extensionAdditions) ? extensionAdditions : new ExtensionAdditions();

		this.extensionAdditions.setFullNameParent(this);
	}

	public void setExceptionSpecification(final ExceptionSpecification exceptionSpecification) {
		this.exceptionSpecification = exceptionSpecification;

		if (null != exceptionSpecification) {
			exceptionSpecification.setFullNameParent(this);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (exceptionSpecification == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		extensionAdditions.setMyScope(scope);
		if (null != exceptionSpecification) {
			exceptionSpecification.setMyScope(scope);
		}
	}

	public int getNofComps() {
		return extensionAdditions.getNofComps();
	}

	public CompField getCompByIndex(final int index) {
		return extensionAdditions.getCompByIndex(index);
	}

	public boolean hasCompWithName(final Identifier identifier) {
		return extensionAdditions.hasCompWithName(identifier);
	}

	public CompField getCompByName(final Identifier identifier) {
		return extensionAdditions.getCompByName(identifier);
	}

	public void trCompsof(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain, final boolean isSet) {
		extensionAdditions.trCompsof(timestamp, referenceChain, isSet);
	}

	public void setExtensionAdditions(final ExtensionAdditions extensionAdditions) {
		this.extensionAdditions = extensionAdditions;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (exceptionSpecification != null) {
			exceptionSpecification.findReferences(referenceFinder, foundIdentifiers);
		}
		if (extensionAdditions != null) {
			extensionAdditions.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (exceptionSpecification != null && !exceptionSpecification.accept(v)) {
			return false;
		}
		if (extensionAdditions != null && !extensionAdditions.accept(v)) {
			return false;
		}
		return true;
	}
}
