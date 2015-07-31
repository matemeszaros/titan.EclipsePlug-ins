/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This class represents a chain of module importation references, used to detect circular references.
 * <p>
 * Almost the same as ReferenceChain but specialised for module importations.
 *
 * @see ReferenceChain
 * 
 * @author Kristof Szabados
 * */
public final class ModuleImportationChain {
	public static final String CIRCULARREFERENCE = "Circular reference chain: `{0}''";

	/**
	 * The list of references contained in the chain.
	 * */
	private final List<ModuleImportation> chainLinks = new ArrayList<ModuleImportation>();

	/**
	 * The list of marked states.
	 * */
	private final Stack<Integer> marked_states = new Stack<Integer>();

	/**
	 * The message used to report if a circular chain is actually found.
	 * <p>
	 * This String object must have exactly one location for inserting a text into.
	 * See the {@link MessageFormat#format(String, Object...)} method for more information.
	 * */
	private String message;

	/**
	 * Should we report the problem as an error or as a warning.
	 * */
	private boolean isError;

	/**
	 * Private constructor.
	 *
	 * @param message the message or the problem to be reported when a cycle is found
	 * @param isError whether the problem would be reported as error, or warning
	 * */
	public ModuleImportationChain(final String message, final boolean isError) {
		this.message = message;
		this.isError = isError;
	}

	/**
	 * Adds an element to the end of the chain.
	 *
	 * @param chainLink the link to add
	 * @return false if this link was already present in the chain, true otherwise
	 * */
	public boolean add(final ModuleImportation chainLink) {
		int index = -1;
		for (int i = 0; i < chainLinks.size() && index == -1; i++) {
			if (chainLinks.get(i).identifier.getDisplayName().equals(chainLink.getIdentifier().getDisplayName())) {
				index = i;
			}
		}

		if (index >= 0) {
			StringBuilder builder = new StringBuilder();
			Location location;

			//for every element of the circle
			for (int i = index; i < chainLinks.size(); i++) {
				builder.setLength(0);

				//add the elements till the end of the chain
				for (int i2 = i; i2 < chainLinks.size(); i2++) {
					if (builder.length() != 0) {
						builder.append(" -> ");
					}
					builder.append(chainLinks.get(i2).chainedDescription());
				}

				//and from the first repeated element
				for (int i2 = index; i2 < i; i2++) {
					if (builder.length() != 0) {
						builder.append(" -> ");
					}
					builder.append(chainLinks.get(i2).chainedDescription());
				}

				//add the element in question to the end, where it should appear again to provide the "circular" feeling
				if (builder.length() != 0) {
					builder.append(" -> ");
				}
				builder.append(chainLinks.get(i).chainedDescription());

				location = chainLinks.get(i).getChainLocation();
				if (location != null) {
					if (isError) {
						location.reportSingularSemanticError(MessageFormat.format(message, builder.toString()));
					} else {
						location.reportSingularSemanticWarning(MessageFormat.format(message, builder.toString()));
					}
				}
			}

			return false;
		}

		chainLinks.add(chainLink);
		return true;
	}

	/**
	 * Checks if the provided chainlink is already present in the chain.
	 * This should only be used if the recursive -ity error was already handled.
	 *
	 * @param chainLink the chain link to look for.
	 * */
	public boolean contains(final ModuleImportation chainLink) {
		for (int i = 0, size = chainLinks.size(); i < size; i++) {
			if (chainLinks.get(i).identifier.getDisplayName().equals(chainLink.getIdentifier().getDisplayName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Removes all of the elements from this chain.
	 * */
	public void clear() {
		chainLinks.clear();
		marked_states.clear();
	}

	/**
	 * Marks the actual state of the reference chain, so that later the chain can be returned into this state.
	 * */
	public void markState() {
		marked_states.add(chainLinks.size());
	}

	/**
	 * Returns the chain of references into its last saved state and deletes the last state mark.
	 * */
	public void previousState() {
		if (marked_states.empty()) {
			return;
		}

		for (int i = chainLinks.size() - 1; i >= marked_states.get(marked_states.size() - 1); i--) {
			chainLinks.remove(i);
		}
		marked_states.remove(marked_states.size() - 1);
	}
}
