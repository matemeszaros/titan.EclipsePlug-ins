/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.designer.parsers.GlobalIntervalHandler;

/**
 * @author Kristof Szabados
 * */
public abstract class GeneralPairMatcher implements ICharacterPairMatcher {
	protected Pair[] pairs;
	protected IDocument document;
	protected IDocumentPartitioner partitioner;
	protected int offset;

	protected int startPos;
	protected int endPos;
	protected int anchor;

	protected abstract String getPertitioning();

	@Override
	public final void clear() {
		offset = 0;
		endPos = 0;
		startPos = 0;
		anchor = 0;
	}

	@Override
	public final void dispose() {
		clear();
		document = null;
	}

	@Override
	public final int getAnchor() {
		return anchor;
	}

	@Override
	public IRegion match(final IDocument iDocument, final int i) {
		offset = i - 1;

		if (i < 1) {
			return null;
		}

		document = iDocument;
		partitioner = null;
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			partitioner = extension3.getDocumentPartitioner(getPertitioning());
		} else {
			partitioner = document.getDocumentPartitioner();
		}

		if (document != null && !isComment(offset) && matchPairs()) {
			return new Region(startPos, endPos - startPos + 1);
		}

		return null;
	}

	/**
	 * The function really doing the pair matching work.
	 * 
	 * @return wheather it could find a matching bracket or not.
	 */
	public final boolean matchPairs() {
		char currBracket;
		try {
			currBracket = document.getChar(offset);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false;
		}

		Interval interval = GlobalIntervalHandler.getInterval(document);
		int temp;

		for (int i = 0; i < pairs.length; i++) {
			if (pairs[i].end == currBracket) {
				endPos = offset;
				anchor = ICharacterPairMatcher.RIGHT;
				if (interval != null) {
					temp = interval.getPairLocation(offset);
					if (temp != -1) {
						startPos = temp;
						return true;
					}
				}
				return findStartingBracket(pairs[i]);
			} else if (pairs[i].start == currBracket) {
				startPos = offset;
				anchor = ICharacterPairMatcher.LEFT;
				if (interval != null) {
					temp = interval.getPairLocation(offset);
					if (temp != -1) {
						endPos = temp;
						return true;
					}
				}
				return findEndingBracket(pairs[i]);
			}
		}
		return false;
	}

	/**
	 * Finds the starting part of a bracket pair.
	 * 
	 * @param pair
	 *                the pair to search for, where the ending position is
	 *                already filled out.
	 * @return the minimal region containing the peer characters
	 */
	protected final boolean findStartingBracket(final Pair pair) {
		try {
			String theDoc = document.get(0, offset);
			int currLevel = 1;
			int currPosition = theDoc.length() - 1;
			while (currLevel > 0 && currPosition > 0) {
				if (isComment(currPosition)) {
					currPosition = getRegionOffset(currPosition);
				} else if ('\"' == theDoc.charAt(currPosition)) {
					currPosition = getStringStart(theDoc, currPosition);
				} else if (!isComment(currPosition)) {
					if (theDoc.charAt(currPosition) == pair.end) {
						currLevel++;
					} else if (theDoc.charAt(currPosition) == pair.start) {
						currLevel--;
					}
				}
				currPosition--;
			}
			startPos = currPosition + 1;
			if (currLevel > 0) {
				return false;
			}

			return true;
		} catch (BadLocationException ble) {
			return false;
		}
	}

	/**
	 * Finds the ending part of a bracket pair.
	 * 
	 * @param pair
	 *                the pair to search for, where the ending position is
	 *                already filled out.
	 * @return the minimal region containing the peer characters
	 */
	protected final boolean findEndingBracket(final Pair pair) {
		String theDoc = document.get();
		int currLevel = 1;
		int currPosition = startPos + 1;
		while (currLevel > 0 && currPosition < theDoc.length()) {
			if (isComment(currPosition)) {
				currPosition = getRegionEnd(currPosition);
			} else if ('\"' == theDoc.charAt(currPosition)) {
				currPosition = getStringEnd(theDoc, currPosition);
			} else if (!isComment(currPosition)) {
				if (theDoc.charAt(currPosition) == pair.end) {
					currLevel--;
				} else if (theDoc.charAt(currPosition) == pair.start) {
					currLevel++;
				}
			}
			currPosition++;
		}

		if (currLevel > 0) {
			return false;
		}

		endPos = currPosition - 1;
		return true;
	}

	/**
	 * Tells if is inside a comment area or not.
	 * 
	 * @param offset
	 *                the offset to decide on
	 * @return true if it is in a comment area,false otherwise.
	 */
	protected final boolean isComment(final int offset) {
		return false;
	}

	/**
	 * Returns the end of a partition region created by the partition
	 * manager.
	 * 
	 * @param offset
	 *                the location whose partition we want to find
	 * @return the end of the found region
	 */
	protected final int getRegionEnd(final int offset) {
		if (partitioner == null) {
			return offset;
		}

		ITypedRegion partition = partitioner.getPartition(offset);
		return partition.getOffset() + partition.getLength() - 1;
	}

	/**
	 * Returns the starting offset of a partition region created by the
	 * partition manager.
	 * 
	 * @param offset
	 *                the location whose partition we want to find
	 * @return the starting offset of the found region
	 */
	protected final int getRegionOffset(final int offset) {
		if (partitioner == null) {
			return offset;
		}

		ITypedRegion partition = partitioner.getPartition(offset);
		return partition.getOffset();
	}

	/**
	 * Returns the end of a string.
	 * 
	 * @param theDoc
	 *                the document in which we work
	 * @param currentPosition
	 *                the location identifiing the string
	 * @return the end offset of the found string
	 */
	protected final int getStringEnd(final String theDoc, final int currentPosition) {
		char tempChar;
		int innerOffset = currentPosition + 1;
		while (innerOffset < theDoc.length()) {
			tempChar = theDoc.charAt(innerOffset);
			if ('\\' == tempChar) {
				innerOffset++;
			} else if ('\"' == tempChar) {
				break;
			}
			innerOffset++;
		}
		return innerOffset;
	}

	/**
	 * Returns the start offset of a string.
	 * 
	 * @param theDoc
	 *                the document in which we work
	 * @param currentPosition
	 *                the location identifiing the string
	 * @return the start offset of the found string
	 */
	protected final int getStringStart(final String theDoc, final int currentPosition) {
		char tempChar;
		int innerOffset = currentPosition - 1;
		while (innerOffset > 0) {
			tempChar = theDoc.charAt(innerOffset);
			if ('\"' == tempChar) {
				if (innerOffset > 0 && '\\' == theDoc.charAt(innerOffset - 1)) {
					innerOffset--;
				} else {
					break;
				}
			}
			innerOffset--;
		}
		return innerOffset;
	}
}
