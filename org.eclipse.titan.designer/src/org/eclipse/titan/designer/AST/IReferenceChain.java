/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * @author Kristof Szabados
 * */
public interface IReferenceChain {

	String CIRCULARREFERENCE = "Circular reference chain: `{0}''";

	/**
	 * Releases the actual referenceChain object.
	 * Any further access is unsafe.
	 * */
	void release();

	/**
	 * Adds an element to the end of the chain.
	 *
	 * @param chainLink the link to add
	 * @return false if this link was already present in the chain, true otherwise
	 * */
	boolean add(final IReferenceChainElement chainLink);

	/**
	 * Marks the actual state of the reference chain, so that later the chain can be returned into this state.
	 * */
	void markState();

	/**
	 * Returns the chain of references into its last saved state and deletes the last state mark.
	 * */
	void previousState();
}
