/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Kristof Szabados
 * */
public final class AnnotationImageProvider implements IAnnotationImageProvider {
	public static final String DEPRECATED = "deprecated";

	@Override
	public ImageDescriptor getImageDescriptor(final String imageDescritporId) {
		return null;
	}

	@Override
	public String getImageDescriptorId(final Annotation annotation) {
		return null;
	}

	@Override
	public Image getManagedImage(final Annotation annotation) {
		if (!(annotation instanceof MarkerAnnotation)) {
			return null;
		}

		final MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
		final IMarker marker = markerAnnotation.getMarker();
		switch (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
		case IMarker.SEVERITY_WARNING:
			if (marker.getAttribute(DEPRECATED, false) || annotation.isMarkedDeleted()) {
				return ImageCache.getImage("compiler_warning_old.gif");
			}

			return ImageCache.getImage("compiler_warning_fresh.gif");
		case IMarker.SEVERITY_INFO:
			if (marker.getAttribute(DEPRECATED, false) || annotation.isMarkedDeleted()) {
				return ImageCache.getImage("compiler_info_old.gif");
			}

			return ImageCache.getImage("compiler_info_fresh.gif");
		case IMarker.SEVERITY_ERROR:
		default:
			if (marker.getAttribute(DEPRECATED, false) || annotation.isMarkedDeleted()) {
				return ImageCache.getImage("compiler_error_old.gif");
			}

			return ImageCache.getImage("compiler_error_fresh.gif");
		}

	}

}
