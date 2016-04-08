/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent FieldSettings.
 * 
 * @author Kristof Szabados
 */
public abstract class FieldSetting extends ASTNode implements ILocateableNode {

	/**
	 * The location of the whole field setting. This location encloses the
	 * field setting fully, as it is used to report errors to.
	 **/
	protected Location location;

	protected Identifier name;

	/** the time when this field setting was check the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	public FieldSetting(final Identifier name) {
		this.name = name;
	}

	/** @return a new instance */
	public abstract FieldSetting newInstance();

	public final Identifier getName() {
		return name;
	}

	@Override
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public final Location getLocation() {
		return location;
	}

	public abstract ISetting getSetting();

	@Override
	public final void setMyScope(final Scope scope) {
		getSetting().setMyScope(scope);
	}

	public abstract void check(final CompilationTimeStamp timestamp, FieldSpecification fieldSpecification);

	/**
	 * Adds the field to the list completion proposals, with some
	 * description.
	 * <p>
	 * Extending class only need to implement their
	 * {@link #getProposalKind()} function
	 * 
	 * @param propCollector
	 *                the proposal collector.
	 * @param i
	 *                the index of a part of the full reference, for which
	 *                we wish to find completions.
	 * */
	public abstract void addProposal(ProposalCollector propCollector, int i);

	/**
	 * Adds the field to the list declaration proposals.
	 * 
	 * @param declarationCollector
	 *                the declaration collector.
	 * @param i
	 *                the index of a part of the full reference, for which
	 *                we wish to find the field, or a field which might
	 *                point us a step forward to the declaration.
	 * */
	public abstract void addDeclaration(DeclarationCollector declarationCollector, int i);
}
