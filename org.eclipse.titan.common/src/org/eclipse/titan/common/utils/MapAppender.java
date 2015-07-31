/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class is able to append string typed key and value pairs to a list, with the specified delimiter.
 *
 */
public class MapAppender {

	private final Appender simpleAppender;
	private final String keyValueDelimiter;

	/**
	 * Create a new map appender object.
	 * 
	 * @param appendable the internal appedable to append the elements to.
	 * @param entryDelimiter the delimter text to be used to separate entries.
	 * @param keyValueDelimiter the delimeter text to be used to separate the key and value parts an entry.
	 * */
	public MapAppender(final Appendable appendable, final String entryDelimiter, final String keyValueDelimiter) {
		simpleAppender = new Appender(appendable, entryDelimiter);
		this.keyValueDelimiter = keyValueDelimiter;
	}

	/**
	 * Append a key - value pair to the internally stored appendable.
	 * 
	 * @param key the key to append.
	 * @param value the value to append.
	 * 
	 * @return the appender to be able to call this function in sequence.
	 * */
	public <K, V> MapAppender append(final K key, final V value) throws IOException {
		simpleAppender.append(String.valueOf(key) + keyValueDelimiter + String.valueOf(value));
		return this;
	}

	/**
	 * Append a list of items to the internally stored appendable.
	 * 
	 * @param items the items to append.
	 * 
	 * @return the appender to be able to call this function in sequence.
	 * */
	public MapAppender append(Object ... items) throws IOException {
		Joiner joiner = new Joiner(keyValueDelimiter);
		joiner.join(Arrays.asList(items)).appendTo(simpleAppender);
		return this;
	}

	/**
	 * Append a list of items to the internally stored appendable.
	 * 
	 * @param iterable the items to append.
	 * 
	 * @return the appender to be able to call this function in sequence.
	 * */
	public MapAppender append(final Iterable<? extends Entry<?, ?>> iterable) throws IOException {
		for (Map.Entry<?, ?> entry : iterable) {
			append(entry.getKey(), entry.getValue());
		}
		return this;
	}
	
	/**
	 * Append a map of items to the internally stored appendable.
	 * 
	 * @param map the map to append.
	 * 
	 * @return the appender to be able to call this function in sequence.
	 * */
	public MapAppender append(final Map<?,?> map) throws IOException {
		return append(map.entrySet());
	}

}
