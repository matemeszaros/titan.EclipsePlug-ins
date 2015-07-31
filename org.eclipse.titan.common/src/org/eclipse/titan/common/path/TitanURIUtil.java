/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.path;

import java.net.URI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;

public final class TitanURIUtil {

	private TitanURIUtil() {
		// Disable constructor
	}

	/**
	 * Checks whether the source URI is a prefix of the other URI.
	 * 
	 * @param source
	 *                the source URI to check
	 * @param other
	 *                the URI to check the source against
	 * 
	 * @return true if the source is a prefix of the other URI, false
	 *         otherwise
	 * */
	public static boolean isPrefix(final URI source, final URI other) {
		if ((source.getScheme() == null && other.getScheme() != null) || !source.getScheme().equals(other.getScheme())) {
			return false;
		}

		final IPath sourcePath = new Path(source.getPath());
		final IPath otherPath = new Path(other.getPath());
		return sourcePath.isPrefixOf(otherPath);
	}

	public static URI removeLastSegment(URI original) {
		String lastSegment = URIUtil.lastSegment(original);
		if (lastSegment == null) {
			return original;
		}
		String originalAsString = original.toString();
		String newAsString = originalAsString.substring(0, originalAsString.length() - lastSegment.length() - 1);
		return URI.create(newAsString);
	}
}
