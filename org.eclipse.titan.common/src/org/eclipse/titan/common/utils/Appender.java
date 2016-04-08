/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.io.IOException;

/**
 * This class is able to append items to an appendable object, with the specified delimiter.
 *
 */
public class Appender implements Appendable {
	private final String delimiter;
	private final Appendable appendable;
	private boolean first = true;

	/**
	 * Create an appender with "" as the delimeter.
	 * 
	 * @param appendable the first appendable to add to the list.
	 * */
	public Appender(final Appendable appendable) {
		this(appendable, "");
	}

	/**
	 * Create an appender with the provided string as delimeter.
	 * 
	 * @param appendable the appendable to be the first element of the list.
	 * @param delimiter the string to be used the delimeter.
	 * */
	public Appender(final Appendable appendable, final String delimiter) {
		this.appendable = appendable;
		this.delimiter = delimiter;
	}

	/**
	 * Append an item to the internally stored appendable.
	 * 
	 * @param item the item to append.
	 * 
	 * @return the appender to be able to call this function in sequence.
	 * */
	public <T> Appender append(final T item) throws IOException {
		return internalAppend(item);
	}

	/**
	 * Append an item to the internally stored appendable.
	 * Also taking care of not prefixing the first item inserted.
	 * 
	 * @param item the item to append.
	 * 
	 * @return the appender to be able to call this function in sequence.
	 * */
	private <T> Appender internalAppend(final T item) throws IOException {
		if (first) {
			first = false;
		} else {
			appendable.append(delimiter);
		}
		appendable.append(String.valueOf(item));
		return this;
	}

	/**
	 * Append a list of items to the internally stored appendable.
	 * 
	 * @param items the items to append.
	 * 
	 * @return the appender to be able to call this function in sequence.
	 * */
	public <T> Appender append(final Iterable<T> items) throws IOException {
		if (items == null) {
			return internalAppend("null");
		}
		for (final T item : items) {
			internalAppend(item);
		}
		return this;
	}

	@Override
	public Appendable append(final CharSequence csq) throws IOException {
		return internalAppend(csq);
	}

	@Override
	public Appendable append(final CharSequence csq,final  int start,final  int end) throws IOException {
		if (csq == null) {
			return internalAppend("null");
		}
		return internalAppend(csq.subSequence(start, end));
	}

	@Override
	public Appendable append(final char c) throws IOException {
		return internalAppend(c);
	}
	
	protected Appendable getAppendable() {
		return appendable;
	}
}
