/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private static final int RESOURCE_CHANGE_FILTER = IResourceChangeEvent.POST_CHANGE
			| IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE;

	// Cache for GUI resources, fonts and colors
	private Map<String, Resource> guiResourceCache;

	private ResourceListener resourceListener;

	// The shared instance
	private static Activator plugin;

	// Cache for the images
	private Map<String, Image> imageCache;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
		this.imageCache = new HashMap<String, Image>();
		this.guiResourceCache = new HashMap<String, Resource>();
	}

	@Override
	public void start(final BundleContext context) throws Exception {

		super.start(context);

		// Installs a resource change listener on workspace level
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		this.resourceListener = new ResourceListener();
		workspace.addResourceChangeListener(this.resourceListener,
				RESOURCE_CHANGE_FILTER);

		// Set default value for Add Log File and Folder last directory
		getPreferenceStore().setDefault(
				PreferenceConstants.PREF_ADD_LOG_FILE_LAST_DIR, ""); //$NON-NLS-1$
		getPreferenceStore().setDefault(
				PreferenceConstants.PREF_ADD_LOG_FOLDER_LAST_DIR, ""); //$NON-NLS-1$
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		// Dispose PreferenceHandler
		PreferencesHandler prefHandler = PreferencesHandler.getInstance();
		prefHandler.removeListener();
		prefHandler.dispose();
		plugin = null;
		disposeImages();
		disposeGUIResources();

		// removes the the resource change listener
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this.resourceListener);

		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Constants.PLUGIN_ID, path);
	}

	/**
	 * Returns an cashed image from given location
	 * 
	 * @param iconPath the plug-relative path
	 * @return the image
	 */
	public Image getIcon(final String iconPath) {
		// obtain the cached image corresponding to the descriptor
		Image image = this.imageCache.get(iconPath);
		if (image == null) {
			image = Activator.getImageDescriptor(iconPath).createImage();
			this.imageCache.put(iconPath, image);
		}
		return image;
	}

	/**
	 * Returns the image descriptor for the given image
	 * 
	 * @param path the path to the image (icon)
	 * @return the image descriptor for the given image
	 */
	public ImageDescriptor getCachedImageDescriptor(final String path) {
		return ImageDescriptor.createFromImage(Activator.getDefault().getIcon(path));
	}
	
	/**
	 * Clears the image cache
	 */
	private void disposeImages() {
		for (Iterator<Image> i = this.imageCache.values().iterator(); i.hasNext();) {
			i.next().dispose();
		}
		this.imageCache.clear();
	}
	
	/**
	 * Return the resource asked for, if the resource is not found in the cache, it will be created 
	 * @param key the resource key
	 * @return resource the resource
	 */
	public Resource getCachedResource(final String key) {
		if (this.guiResourceCache.containsKey(key)) {
			return this.guiResourceCache.get(key);
		}		
		Resource resource = MSCConstants.getResource(key);
		if (resource == null) {
			TitanLogExceptionHandler.handleException(new TechnicalException("Resource could not be found")); //$NON-NLS-1$
		}
		return addResource(key, resource);
	}
	
	/**
	 * Return the resource asked for, if the resource is not found in the cache, it will be created 
	 * @param key the resource key
	 * @return resource the resource
	 */
	public Resource getCachedResource(final RGB color) {
		String key = String.valueOf(color.red)
				+ PreferenceConstants.RGB_COLOR_SEPARATOR
				+ String.valueOf(color.green)
				+ PreferenceConstants.RGB_COLOR_SEPARATOR
				+ String.valueOf(color.blue);

		if (this.guiResourceCache.containsKey(key)) {
			return this.guiResourceCache.get(key);
		}		
		Resource resource = new Color(Display.getDefault(), color);
		TitanLogExceptionHandler.handleException(new TechnicalException("Resource could not be found")); //$NON-NLS-1$
		return addResource(key, resource);
	}

	/**
	 * Adds a resource to the resource cache
	 * 
	 * @param key the resource key
	 * @param resource the resource
	 * @return the resource just added to the cache
	 */
	private Resource addResource(final String key, final Resource resource) {
		this.guiResourceCache.put(key, resource);
		return resource;
	}

	/**
	 * Clears the GUI resource cache
	 */
	private void disposeGUIResources() {
		for (Resource resource : this.guiResourceCache.values()) {
			resource.dispose();
		}
		this.guiResourceCache.clear();
	}
	
	/**
	 * Returns a section in the Log Viewer plugin's dialog settings. If the section doesn't exist yet, it is created.
	 *
	 * @param name the name of the section
	 * @return the section of the given name
	 */
	public IDialogSettings getDialogSettingsSection(final String name) {
		IDialogSettings dialogSettings = getDialogSettings();
		IDialogSettings section = dialogSettings.getSection(name);
		if (section == null) {
			section = dialogSettings.addNewSection(name);
		}
		return section;
	}
}
