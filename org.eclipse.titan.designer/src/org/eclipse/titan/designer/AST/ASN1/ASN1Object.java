/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.Governed;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.Object.Object_Definition;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent an ASN.1 Object.
 * 
 * @author Kristof Szabados
 */
public abstract class ASN1Object extends Governed implements IObjectSet_Element {

	protected ObjectClass myGovernor;

	@Override
	public final Setting_type getSettingtype() {
		return Setting_type.S_O;
	}

	/** @return a new instance */
	public abstract ASN1Object newInstance();

	@Override
	public final IObjectSet_Element newOseInstance() {
		return newInstance();
	}

	@Override
	public final ObjectClass getMyGovernor() {
		return myGovernor;
	}

	public abstract Object_Definition getRefdLast(final CompilationTimeStamp timestamp, IReferenceChain referenceChain);

	/**
	 * Sets the governing ObjectClass of the Object.
	 * 
	 * @param governor
	 *                the governor of the Object.
	 * */
	public final void setMyGovernor(final ObjectClass governor) {
		myGovernor = governor;
	}

	/**
	 * @return the number of elements.
	 * */
	public final int getNofElems() {
		return 1;
	}

	@Override
	public final void accept(final ObjectSetElement_Visitor visitor) {
		visitor.visitObject(this);
	}

	@Override
	public final void setMyScopeOse(final Scope scope) {
		setMyScope(scope);
	}

	public abstract void check(final CompilationTimeStamp timestamp);

	/**
	 * Adds the object to the list completion proposals, with some
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
	 * Adds the object to the list declaration proposals.
	 * 
	 * @param declarationCollector
	 *                the declaration collector.
	 * @param i
	 *                the index of a part of the full reference, for which
	 *                we wish to find the object, or an object which might
	 *                point us a step forward to the declaration.
	 * */
	public abstract void addDeclaration(DeclarationCollector declarationCollector, int i);
}
