/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple class which stores the interval related data extracted from source files by the proper lexer/parser. Can be used to quickly find the
 * smallest enclosing interval of an offset.
 * 
 * @author Kristof Szabados
 * */
public class Interval {

	public enum interval_type {
		/**
		 * multi line comment (usually starting with /* end ending with
		 * *\/).
		 */
		MULTILINE_COMMENT,
		/** single line comments. */
		SINGLELINE_COMMENT,
		/** block statement (usually between {} ). */
		NORMAL,
		/** array indexing (usually between [] ). */
		INDEX,
		/** parameter encloser ( usually between () ). */
		PARAMETER
	}

	private int startOffset;
	private int startLine;
	private int endOffset;
	private int endLine;
	private final interval_type type;
	private int depth;
	private boolean erroneous;

	private List<Interval> subIntervals;
	private final Interval parent;

	public Interval(final Interval parent, final interval_type type) {
		this.parent = parent;
		if (parent != null) {
			parent.addSubIntervall(this);
			depth = parent.getDepth() + 1;
		} else {
			depth = 0;
		}
		startOffset = -1;
		startLine = -1;
		endOffset = -1;
		endLine = -1;
		this.type = type;
		erroneous = false;
		subIntervals = null;
	}

	/**
	 * @return the type of the interval.
	 * */
	public final interval_type getType() {
		return type;
	}

	/**
	 * @return whether this interval is a comment type one or not.
	 **/
	public final boolean isComment() {
		return interval_type.MULTILINE_COMMENT.equals(type) || interval_type.SINGLELINE_COMMENT.equals(type);
	}

	public final boolean getErroneous() {
		return erroneous;
	}

	public final void setErroneous() {
		erroneous = true;
	}

	/**
	 * Returns the parent of the interval.
	 *
	 * @return the parent of the interval or null if it is the root interval
	 * */
	public final Interval getParent() {
		return parent;
	}

	/**
	 * Add a child interval to this interval.
	 *
	 * @param child the child interval to be added.
	 * */
	private void addSubIntervall(final Interval child) {
		if (subIntervals == null) {
			subIntervals = new ArrayList<Interval>();
		}

		subIntervals.add(child);
	}

	/**
	 * @return the direct subintervals of this interval.
	 * */
	public final List<Interval> getSubIntervals() {
		if (subIntervals == null) {
			return new ArrayList<Interval>();
		}

		return subIntervals;
	}

	/**
	 * @return the starting offset of the interval.
	 * */
	public final int getStartOffset() {
		return startOffset;
	}

	/**
	 * Sets the starting offset of the interval.
	 *
	 * @param offset the offset to be used as starting offset
	 * */
	public final void setStartOffset(final int offset) {
		startOffset = offset;
	}

	/**
	 * @return the starting line of the interval.
	 * */
	public final int getStartLine() {
		return startLine;
	}

	/**
	 * Sets the starting line of the interval.
	 *
	 * @param line the line number to be used as starting line
	 * */
	public final void setStartLine(final int line) {
		startLine = line;
	}

	/**
	 * @return the ending offset of the interval.
	 * */
	public final int getEndOffset() {
		return endOffset;
	}

	/**
	 * Sets the ending offset of the interval.
	 *
	 * @param offset the offset to be set as endoffset
	 * */
	public final void setEndOffset(final int offset) {
		endOffset = offset;
	}

	/**
	 * @return the ending line of the interval.
	 * */
	public int getEndLine() {
		return endLine;
	}

	/**
	 * Sets the ending line of the interval.
	 *
	 * @param line the line number to be used as ending line
	 * */
	public final void setEndLine(final int line) {
		endLine = line;
	}

	/**
	 * Calculates the depth of the actual interval in the actual hierarchy it is in.
	 *
	 * @return the depth of the interval in the interval hierarchy
	 * */
	public final int getDepth() {
		return depth;
	}

	/**
	 * Finds the smallest enclosing interval to an offset. For this recursively walks the subtrees of the actual interval.
	 *
	 * @param offset the offset
	 *
	 * @return the smallest enclosing interval
	 * */
	public final Interval getSmallestEnclosingInterval(final int offset) {
		return getSmallestEnclosingInterval(offset, offset);
	}

	/**
	 * Finds the smallest enclosing interval to a range. For this recursively walks the subtrees of the actual interval.
	 *
	 * @param startOffset the starting offset of the range
	 * @param endOffset the ending offset of the range
	 *
	 * @return the smallest enclosing interval
	 * */
	public final Interval getSmallestEnclosingInterval(final int startOffset, final int endOffset) {
		if (subIntervals == null) {
			return this;
		}

		if (!subIntervals.isEmpty() && subIntervals.get(0).getStartOffset() <= startOffset
				&& endOffset <= subIntervals.get(subIntervals.size() - 1).getEndOffset()) {
			int lowLimit = 0;
			int highLimit = subIntervals.size();
			int middle;
			Interval testedIntervall;
			while (true) {
				middle = (lowLimit + highLimit) / 2;
				testedIntervall = subIntervals.get(middle);
				if (endOffset < testedIntervall.startOffset) {
					highLimit = middle;
				} else if (startOffset > testedIntervall.endOffset) {
					lowLimit = middle;
				} else {
					return testedIntervall.getSmallestEnclosingInterval(startOffset, endOffset);
				}
				if (middle == (lowLimit + highLimit) / 2) {
					return this;
				}
			}
		}
		return this;
	}

	/**
	 * Finds the offset of the parameters pair. For this recursively walks the subtrees of the actual interval.
	 *
	 * @param offset the offset on which the original element is
	 *
	 * @return the offset of the parameters pair, or -1 if it can not be found
	 * */
	public final int getPairLocation(final int offset) {
		if (startOffset == offset) {
			return endOffset;
		} else if (endOffset == offset) {
			return startOffset;
		} else if (subIntervals != null && !subIntervals.isEmpty()) {
			return findPairInSubIntervals(offset);
		}
		return -1;
	}

	private int findPairInSubIntervals(final int offset) {
		int lowLimit = 0;
		int highLimit = subIntervals.size();
		int middle;
		Interval testedInterval;
		while (true) {
			middle = (lowLimit + highLimit) / 2;
			testedInterval = subIntervals.get(middle);
			if (offset < testedInterval.startOffset) {
				highLimit = middle;
			} else if (offset > testedInterval.endOffset) {
				lowLimit = middle;
			} else {
				return testedInterval.getPairLocation(offset);
			}
			if (middle == (lowLimit + highLimit) / 2) {
				return -1;
			}
		}
	}
}
