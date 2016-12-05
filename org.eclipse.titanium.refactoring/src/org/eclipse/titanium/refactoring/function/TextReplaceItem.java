/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;

/**
 * Special class to store ISubReferences and Definition occurrences in text
 * and be able to sort them by Location. Immutable.
 *
 * @author Viktor Varga
 */
final class TextReplaceItem implements Comparator<TextReplaceItem>, Comparable<TextReplaceItem> {
	private static final char SEMICOLON = ';';

	/**
	 * <code>true</code>: reference (Hit), <code>false</code>: declaration
	 * <p>
	 * If true, <code>subref</code> should not be null. If false, <code>def</code> should not be null.
	 */
	private final boolean ref;
	private final ISubReference subref;
	private final Definition def;

	/**
	 * the param of which this subref/def is an occurrence
	 */
	private final Param param;

	private final String source;
	/**
	 * references/definitions can be found in <code>source[ref.location+sourceOffset]</code> as text
	 */
	private final int sourceOffset;

	/**
	 * start location of this node in the text
	 * */
	private final int startOffset;
	/**
	 * end location of this node in the text
	 * */
	private final int endOffset;

	TextReplaceItem(final ISubReference subref, final Param param, final String sourceText, final int sourceOffset) {
		ref = true;
		this.subref = subref;
		this.def = null;
		this.param = param;
		this.source = sourceText;
		this.sourceOffset = sourceOffset;
		startOffset = calculateStartOffset();
		endOffset = calculateEndOffset();

	}
	TextReplaceItem(final Definition def, final Param param, final String sourceText, final int sourceOffset) {
		ref = false;
		this.def = def;
		this.subref = null;
		this.param = param;
		this.source = sourceText;
		this.sourceOffset = sourceOffset;
		startOffset = calculateStartOffset();
		endOffset = calculateEndOffset();

	}

	private int calculateStartOffset() {
		return getLocation().getOffset()-sourceOffset;
	}
	/**
	 * in case of definitions, tries to find the end of them including the optional semicolon
	 * */
	private int calculateEndOffset() {
		int end = getLocation().getEndOffset()-sourceOffset;
		if (ref) {
			return end;
		}
		if (end > source.length()) {
			end = source.length();
		}

		final int lastInd = end-1;
		if (lastInd >= 0 && source.charAt(lastInd) == SEMICOLON) {
			return end;
		}
		if (lastInd+1 < source.length() && source.charAt(lastInd+1) == SEMICOLON) {
			return end+1;
		}
		return end;
	}

	public boolean isReference() {
		return ref;
	}

	public StringBuilder getNewParamName() {
		return param.getName();
	}

	public Location getLocation() {
		if (ref) {
			return subref == null ? null : subref.getLocation();
		} else {
			return def == null ? null : def.getLocation();
		}
	}

	//methods for function text creation

	/**
	 * @return a substring of <code>sourceText</code> which contains this TextReplaceItem
	 */
	public StringBuilder createText() {
		return new StringBuilder(source.substring(startOffset, endOffset));
	}
	/**
	 * @return a substring of <code>sourceText</code> from the beginning of the <code>sourceText</code>
	 *  to the beginning of this TextReplaceItem
	 */
	public StringBuilder createBeginningText() {
		return new StringBuilder(source.substring(0, startOffset));
	}
	/**
	 * @return a substring of <code>sourceText</code> from the end of this TextReplaceItem
	 *  to the end of the <code>sourceText</code>
	 */
	public StringBuilder createEndingText() {
		return new StringBuilder(source.substring(endOffset));
	}
	/**
	 * @return a substring of <code>sourceText</code> from the end of this TextReplaceItem
	 *  to the beginning of <code>till</code>
	 */
	public StringBuilder createIntermediateText(final TextReplaceItem till) {
		return new StringBuilder(source.substring(endOffset, till.startOffset));
	}
	/**
	 * @return if exists, returns that part of a definition statement (<code>def</code>)
	 *  which will be moved to before the new function call (mostly: declarations only)
	 */
	public StringBuilder createPreDeclarationText() {
		//empty (invalid)
		if (ref) {
			return new StringBuilder();
		}
		//no need to split definition
		if (def instanceof Def_Timer) {
			return createText();
		}
		if (def.getLocation().getEndOffset() == def.getIdentifier().getLocation().getEndOffset()) {
			return createText();
		}
		//split definition and only return the declaration part
		final StringBuilder sb = new StringBuilder();
		final int end = def.getIdentifier().getLocation().getEndOffset() - sourceOffset;
		sb.append(source.substring(startOffset, end));
		return sb;
	}
	/**
	 * @return if exists, returns the initialization part of a definition statement (<code>def</code>)
	 */
	public List<StringBuilder> createInitOnlyText() {
		final List<StringBuilder> ret = new ArrayList<StringBuilder>();
		//empty
		if (ref || def instanceof Def_Timer) {
			ret.add(new StringBuilder());
			return ret;
		}
		if (def.getLocation().getEndOffset() == def.getIdentifier().getLocation().getEndOffset()) {
			ret.add(new StringBuilder());
			return ret;
		}
		//split definition and only return the initialization part
		final StringBuilder sb = new StringBuilder();
		ret.add(param.getName());
		final int idEnd = def.getIdentifier().getLocation().getEndOffset() - sourceOffset;
		sb.append(source.substring(idEnd, endOffset));
		ret.add(sb);
		return ret;
	}

	//

	@Override
	public int compare(final TextReplaceItem arg0, final TextReplaceItem arg1) {
		if (arg0 == arg1) {
			return 0;
		}
		if (arg0 == null) {
			return -1;
		}
		if (arg1 == null) {
			return 1;
		}

		final IResource f0 = arg0.getLocation().getFile();
		final IResource f1 = arg1.getLocation().getFile();
		if (!f0.equals(f1)) {
			ErrorReporter.logError("TextReplaceItem::compare(): Files differ! ");
			return f0.getFullPath().toString().compareTo(f1.getFullPath().toString());
		}

		int o0 = arg0.getLocation().getOffset();
		int o1 = arg1.getLocation().getOffset();
		final int comp1 = (o0 < o1) ? -1 : ((o0 == o1) ? 0 : 1);//TODO update with Java 1.7 to Integer.compare
		if (comp1 != 0) {
			return comp1;
		}
		o0 = arg0.getLocation().getEndOffset();
		o1 = arg1.getLocation().getEndOffset();
		return (o0 < o1) ? -1 : ((o0 == o1) ? 0 : 1);//TODO update with Java 1.7 to Integer.compare
	}
	@Override
	public int compareTo(final TextReplaceItem arg0) {
		return compare(this, arg0);
	}

	@Override
	public boolean equals(final Object arg0) {
		if (arg0 == this) {
			return true;
		}
		if (!(arg0 instanceof TextReplaceItem)) {
			return false;
		}
		return compare(this, (TextReplaceItem)arg0) == 0;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getLocation().getFile().getFullPath().toString().hashCode();
		result = prime * result + getLocation().getOffset();
		result = prime * result + getLocation().getEndOffset();
		return result;
	}



}