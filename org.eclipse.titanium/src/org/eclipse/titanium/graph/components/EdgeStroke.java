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
	public Stroke transform(E e) {
		if (e instanceof EdgeDescriptor && NodeColours.DARK_RED.equals(((EdgeDescriptor) e).getColor())) {
			return new BasicStroke(2.0f);
		}

		return new BasicStroke(1.0f);
	}
	
}
