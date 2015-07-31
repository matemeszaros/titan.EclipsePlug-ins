/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

/**
 * @author Kristof Szabados
 * */
public final class CompilationTimeStamp {
	private static long compilationCounter = 0;
	private static final CompilationTimeStamp BASE_TIMESTAMP = new CompilationTimeStamp(0);

	private final long compilationTimestamp;

	private CompilationTimeStamp(final long compilationTimestamp) {
		this.compilationTimestamp = compilationTimestamp;
	}

	/**
	 * Returns a new compilation counter, which can be used to provide a
	 * clear baseline between different semantic checks of the same module.
	 * <p>
	 * It is expected that the time difference between returning the same
	 * value twice will be huge enough, to be assumed that this function
	 * always returns a unique value.
	 * <p>
	 * The values returned are always positive..
	 * 
	 * @return a new compilationCounter.
	 * */
	public static CompilationTimeStamp getNewCompilationCounter() {
		compilationCounter++;
		if (compilationCounter == Long.MAX_VALUE) {
			compilationCounter = 0;
		}
		return new CompilationTimeStamp(compilationCounter);
	}

	/**
	 * Returns a base timestamp that can be used to visit AST nodes, that
	 * require a timestamp, without activating the checking functionalities.
	 * 
	 * @return the base timestamp.
	 * */
	public static CompilationTimeStamp getBaseTimestamp() {
		return BASE_TIMESTAMP;
	}

	/**
	 * Returns true if this timestamp is smaller / earlier that the one
	 * provided as parameter.
	 * 
	 * @param other
	 *                the other timestamp to compare to.
	 * @return true if the provided timestamp is newer that the actual.
	 * */
	public boolean isLess(final CompilationTimeStamp other) {
		return compilationTimestamp < other.compilationTimestamp;
	}

	@Override
	public String toString() {
		return "timestamp " + compilationTimestamp;
	}
}
