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
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent a FieldSpec of an ObjectClass.
 * 
 * @author Kristof Szabados
 */
public abstract class FieldSpecification extends ASTNode implements ILocateableNode {

	public enum Fieldspecification_types {
		/** undefined. */
		FS_UNDEFINED,
		/** erroneous field specification. */
		FS_ERROR,
		/** type field specification. */
		FS_T,
		/** fixed type value field specification. */
		FS_V_FT,
		/** variable type value field specification. */
		FS_V_VT,
		/** fixed type valueset field specification. */
		FS_VS_FT,
		/** variable type valueset field specification. */
		FS_VS_VT,
		/** object field specification. */
		FS_O,
		/** objectset field specification. */
		FS_OS
	}

	/**
	 * The location of the whole field specification. This location encloses
	 * the field specification fully, as it is used to report errors to.
	 **/
	protected Location location;

	protected final Identifier identifier;
	protected boolean isOptional;
	protected ObjectClass_Definition myObjectClass;

	/** the time when this field specification was check the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	public FieldSpecification(final Identifier identifier, final boolean isOptional) {
		this.identifier = identifier;
		this.isOptional = isOptional;
	}

	@Override
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public final Location getLocation() {
		return location;
	}

	public final Identifier getIdentifier() {
		return identifier;
	}

	public abstract Fieldspecification_types getFieldSpecificationType();

	public void setMyObjectClass(final ObjectClass_Definition objectClass) {
		myObjectClass = objectClass;
	}

	public final boolean getIsOptional() {
		return isOptional;
	}

	public abstract boolean hasDefault();

	/**
	 * Does the semantic checking of the type.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * */
	public abstract void check(final CompilationTimeStamp timestamp);

	public abstract ISetting getDefault();

	public FieldSpecification getLast() {
		return this;
	}

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
