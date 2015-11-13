/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.Governor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldSpecifications;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_root;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_Definition;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent ObjectClass.
 * 
 * @author Kristof Szabados
 */
public abstract class ObjectClass extends Governor {

	@Override
	public final Setting_type getSettingtype() {
		return Setting_type.S_OC;
	}

	public abstract ObjectClass_Definition getRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/** @return a new instance of this reference */
	public abstract ObjectClass newInstance();

	/**
	 * Does the semantic checking of the setting.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	@Override
	public abstract void check(final CompilationTimeStamp timestamp);

	/**
	 * Does the semantic checking of the provided object with this setting.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param object
	 *                the object to check.
	 * */
	public abstract void checkThisObject(final CompilationTimeStamp timestamp, ASN1Object object);

	/**
	 * Checks and returns the syntax of the object class.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the root of the syntax of this ObjectClass
	 */
	public abstract ObjectClassSyntax_root getObjectClassSyntax(final CompilationTimeStamp timestamp);

	/** @return the field specifications of this ObjectClass */
	public abstract FieldSpecifications getFieldSpecifications();

	/**
	 * Adds the assignment to the list completion proposals, with some
	 * description. Extending class only need to implement their
	 * getProposalKind() function
	 * 
	 * @param propCollector
	 *                the proposal collector.
	 * @param i
	 *                the index of a part of the full reference, for which
	 *                we wish to find completions.
	 * */
	public abstract void addProposal(ProposalCollector propCollector, int i);

	/**
	 * Adds the assignment to the list declaration proposals.
	 * 
	 * @param declarationCollector
	 *                the declaration collector.
	 * @param i
	 *                the index of a part of the full reference, for which
	 *                we wish to find the assignment, or a assignment which
	 *                might point us a step forward to the declaration.
	 * */
	public abstract void addDeclaration(DeclarationCollector declarationCollector, int i);
}
