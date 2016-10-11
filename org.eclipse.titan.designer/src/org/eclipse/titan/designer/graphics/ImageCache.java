/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.graphics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Generic class to cache images inside the Designer plug-in.
 * 
 * @author Kristof Szabados
 * */
public final class ImageCache {
	private static final String ICONS_SUBDIR = "icons/";

	private static Map<ImageDescriptor, Image> imageCache = new ConcurrentHashMap<ImageDescriptor, Image>();

	private ImageCache() {
		// Hide constructor
	}

	/**
	 * Creates an ImageDescriptor from the image's name, whose root is the
	 * icons directory.
	 * 
	 * @param name
	 *                the name of the image starting from the icons
	 *                directory
	 * @return the created ImageDescriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String name) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(ProductConstants.PRODUCT_ID_DESIGNER, ICONS_SUBDIR + name);
	}

	/**
	 * Creates and returns the image identified by the provided name. This
	 * always gives back the same image instance for the same file name
	 * 
	 * @param name
	 *                the name of the image starting from the icons
	 *                directory
	 * @return the created ImageDescriptor
	 */
	public static Image getImage(final String name) {
		final ImageDescriptor descriptor = getImageDescriptor(name);
		if (descriptor == null) {
			return null;
		}
		if (imageCache.containsKey(descriptor)) {
			return imageCache.get(descriptor);
		}

		final Image image = descriptor.createImage();
		imageCache.put(descriptor, image);
		return image;
	}
}
