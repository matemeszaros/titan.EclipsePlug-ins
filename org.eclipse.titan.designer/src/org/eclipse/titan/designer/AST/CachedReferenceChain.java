/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
 * This class represents a chain of references, used to detect circular references.
 * The detected chain is not reported automatically, only stored for a later time, when the reporting is requested.
 * <p>
 * This class is able to mark a state, and later on return back to this state.
 * @see #markState()
 * @see #previousState()
 * 
 * @author Kristof Szabados
 * */
public final class CachedReferenceChain implements IReferenceChain {
	/**
	 * The list of references contained in the chain.
	 * */
	private final List<IReferenceChainElement> chainLinks = new ArrayList<IReferenceChainElement>();

	/**
	 * The list of marked states.
	 * */
	private final Stack<Integer> markedStates = new Stack<Integer>();

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
	 * Used to store copies of the previous states of the chain.
	 */
	private List<List<IReferenceChainElement>> safeCopy = new ArrayList<List<IReferenceChainElement>>();

	/**
	 * List of marked error states.
	 */
	private Stack<Integer> errorStates = new Stack<Integer>();

	public CachedReferenceChain(final String message, final boolean isError) {
		this.message = message;
		this.isError = isError;
	}

	@Override
	public void release() {
		//Do nothing
	}

	@Override
	public boolean add(final IReferenceChainElement chainLink) {
		int index = chainLinks.indexOf(chainLink);
		chainLinks.add(chainLink);
		if (index >= 0) {
			safeCopy.add(new ArrayList<IReferenceChainElement>(chainLinks));

			return false;
		}

		return true;
	}

	/**
	 * If there was a circular reference chain detected, this function can be used to report the problem.
	 * */
	public void reportAllTheErrors() {
		if (null == safeCopy || safeCopy.isEmpty()) {
			return;
		}

		StringBuilder builder = new StringBuilder();
		Location location;

		for (int i = errorStates.empty() ? 0 : errorStates.peek(); i < safeCopy.size(); ++i) {
			List<IReferenceChainElement> current = safeCopy.get(i);
			// for every element of the circle
			for (int j = 0; j < current.size(); j++) {
				builder.setLength(0);

				// add the elements till the end of the chain
				for (int i2 = j; i2 < current.size(); i2++) {
					if (builder.length() != 0) {
						builder.append(" -> ");
					}
					builder.append(current.get(i2).chainedDescription());
				}

				// and from the first repeated element
				for (int i2 = 0; i2 < j; i2++) {
					if (builder.length() != 0) {
						builder.append(" -> ");
					}
					builder.append(current.get(i2).chainedDescription());
				}

				location = current.get(j).getChainLocation();
				if (location != null) {
					if (isError) {
						location.reportSingularSemanticError(MessageFormat.format(message, builder.toString()));
					} else {
						location.reportSingularSemanticWarning(MessageFormat.format(message, builder.toString()));
					}
				}
			}
		}

	}

	@Override
	public void markState() {
		markedStates.add(chainLinks.size());
	}

	@Override
	public void previousState() {
		if (markedStates.isEmpty()) {
			return;
		}

		int markedLimit = markedStates.get(markedStates.size() - 1).intValue();
		for (int i = chainLinks.size() - 1; i >= markedLimit; i--) {
			chainLinks.remove(i);
		}

		markedStates.remove(markedStates.size() - 1);
	}

	/**
	 * Marks the current state of the error container.
	 */
	public void markErrorState() {
		errorStates.push(safeCopy.size());
	}

	/**
	 * Restores the error container to the previously marked state.
	 * */
	public void prevErrorState() {
		if (errorStates.empty() || errorStates.peek() > safeCopy.size()) {
			return;
		}

		int markedLimit = errorStates.get(errorStates.size() - 1).intValue();
		for (int i = safeCopy.size() - 1; i >= markedLimit; --i) {
			safeCopy.remove(i);
		}

		errorStates.pop();
	}

	/**
	 * Returns the number of errors detected after the last call of {@link #markErrorState()}.
	 * @return the number of errors
	 */
	public int getNofErrors() {
		return safeCopy.size() - errorStates.peek();
	}
}
