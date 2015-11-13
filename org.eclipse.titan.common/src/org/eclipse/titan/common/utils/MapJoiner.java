/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.util.Map;

/**
 * This class is able to create the string representation of a list of key-value pairs.
 * e.g. It can be used to convert a {@link Map} to {@link String}.
 * 
 */
public class MapJoiner {

	private final Joiner simpleJoiner;
	private final String keyValueDelimiter;
	
	public MapJoiner(final String entryDelimiter, final String keyValueDelimiter) {
		simpleJoiner = new Joiner(entryDelimiter);
		this.keyValueDelimiter = keyValueDelimiter;
	}

	/**
	 * Adds the given key and value pairs to the joiner.
	 * 
	 * @param key
	 *            The key. Can be <code>null</code>.
	 * @param value
	 *            The value. Can be <code>null</code>.
	 * @return a reference to this object
	 */
	public <K,V> MapJoiner join(final K key, final V value) {
		simpleJoiner.join(String.valueOf(key) + keyValueDelimiter + String.valueOf(value));
		return this;
	}

	/**
	 * Adds the elements of the map to the joiner one by one.
	 * 
	 * @param mapToJoin
	 *            the map to add.
	 * @return a reference to this object
	 */
	public MapJoiner join(final Map<?, ?> mapToJoin) {
		for (Map.Entry<?, ?> entry : mapToJoin.entrySet()) {
			join(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public String toString() {
		return simpleJoiner.toString();
	}

}
