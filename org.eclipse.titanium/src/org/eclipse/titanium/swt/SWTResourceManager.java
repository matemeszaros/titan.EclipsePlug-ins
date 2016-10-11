/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.swt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titanium.Activator;

/**
 * Generic class to handle SWT resources.
 * */
public final class SWTResourceManager {

	private static final Map<ImageDescriptor, Image> IMAGE_CACHE = new ConcurrentHashMap<ImageDescriptor, Image>();
	private static final Map<RGB, Color> COLOR_MAP = new HashMap<RGB, Color>();

	private SWTResourceManager() {
		// disabled constructor
	}

	/**
	 * Returns a {@link Color} given its RGB value.
	 * 
	 * @param rgb
	 *            the {@link RGB} value of the color
	 * @return the {@link Color} matching the RGB value
	 */
	public static Color getColor(final RGB rgb) {
		Color color = COLOR_MAP.get(rgb);
		if (color == null) {
			final Display display = Display.getCurrent();
			color = new Color(display, rgb);
			COLOR_MAP.put(rgb, color);
		}
		return color;
	}

	/**
	 * Creates an ImageDescriptor from the image's name, whose root is the root directory.
	 *
	 * @param name the name of the image starting from the root directory
	 * @return the created ImageDescriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String name) {
		return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, name);
	}

	/**
	 * Creates and returns the image identified by the provided name.
	 * This always gives back the same image instance for the same file name
	 *
	 * @param name the name of the image starting from the root directory
	 * @return the created ImageDescriptor
	 */
	public static Image getImage(final String name) {
		final ImageDescriptor descriptor = getImageDescriptor(name);
		if (descriptor == null) {
			return null;
		}
		if (IMAGE_CACHE.containsKey(descriptor)) {
			return IMAGE_CACHE.get(descriptor);
		}

		final Image image = descriptor.createImage();
		IMAGE_CACHE.put(descriptor, image);
		return image;
	}
}
