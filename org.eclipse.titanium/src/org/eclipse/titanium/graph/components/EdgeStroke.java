/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.components;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

/**
 * This class can makes edges colored red more emphasized, by doubling their width.
 * 
 * @author Kristof Szabados
 */
public 	class EdgeStroke<E> implements Transformer<E, Stroke> {

	@Override
	public Stroke transform(final E e) {
		if (e instanceof EdgeDescriptor && NodeColours.DARK_RED.equals(((EdgeDescriptor) e).getColor())) {
			return new BasicStroke(2.0f);
		}

		return new BasicStroke(1.0f);
	}
	
}
