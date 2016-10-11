/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

import org.eclipse.titan.log.viewer.parsers.token.EOR;
import org.eclipse.titan.log.viewer.parsers.token.Unknown;
import org.eclipse.titan.log.viewer.parsers.token.WhiteSpace;

/**
 * This class defines tokens
 *
 */
public final class Tokens {
	public static final Unknown UNKNOWN = new Unknown(""); //$NON-NLS-1$
	public static final EOR EOR = new EOR(""); //$NON-NLS-1$
	public static final WhiteSpace WS = new WhiteSpace(""); //$NON-NLS-1$

	private Tokens() {
	}
}
