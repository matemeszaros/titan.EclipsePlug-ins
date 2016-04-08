/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.GovernedSet;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSetElementVisitor_objectCollector;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent ObjectSet.
 * 
 * @author Kristof Szabados
 */
public abstract class ObjectSet extends GovernedSet {
	protected ObjectClass myGovernor;

	@Override
	public final Setting_type getSettingtype() {
		return Setting_type.S_OS;
	}

	public abstract ObjectSet newInstance();

	@Override
	public final ObjectClass getMyGovernor() {
		return myGovernor;
	}

	/**
	 * Sets the ObjectClass of the object set.
	 * 
	 * @param governor
	 *                the object class to set as governor.
	 * */
	public final void setMyGovernor(final ObjectClass governor) {
		myGovernor = governor;
	}

	public abstract ObjectSet_definition getRefdLast(final CompilationTimeStamp timestamp, IReferenceChain referenceChain);

	public abstract int getNofObjects();

	public abstract ASN1Object getObjectByIndex(int index);

	public abstract void accept(ObjectSetElementVisitor_objectCollector v);

	public abstract void check(final CompilationTimeStamp timestamp);

	/**
	 * Adds the object set to the list completion proposals, with some
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
	 * Adds the object set to the list declaration proposals.
	 * 
	 * @param declarationCollector
	 *                the declaration collector.
	 * @param i
	 *                the index of a part of the full reference, for which
	 *                we wish to find the object set, or a object set which
	 *                might point us a step forward to the declaration.
	 * */
	public abstract void addDeclaration(DeclarationCollector declarationCollector, int i);
}
