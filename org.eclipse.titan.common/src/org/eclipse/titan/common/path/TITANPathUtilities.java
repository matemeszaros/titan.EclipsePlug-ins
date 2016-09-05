/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.path;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.environment.EnvironmentVariableResolver;
import org.eclipse.titan.common.utils.environment.EnvironmentVariableResolver.VariableNotFoundException;

/**
 * Utility class to resolve eclipse paths.
 * 
 * @author Kristof Szabados
 * */
public final class TITANPathUtilities {

	private TITANPathUtilities() {
		// Do nothing
	}

	/**
	 * Resolves the provided uri relative to the provided base uri.
	 * Environment variables and path variables will be resolved
	 * @param pathToBeResolved
	 *                the path to be resolved.
	 * @param basePath
	 *                the full path to which the resolvable one might be
	 *                relative to.
	 * 
	 * @return the resolved uri.
	 * */
	//TODO: call resolvePathURI, it is the same functionality!!!
	public static URI resolvePath(final String pathToBeResolved, final URI basePath) {
		Map<?, ?> envVariables;
		if (DebugPlugin.getDefault() != null) {
			envVariables = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();
		} else {
			envVariables = null;
		}


		String tmp2 = null;
		try {
			final String tmp1 = EnvironmentVariableResolver.eclipseStyle().resolve(pathToBeResolved, envVariables);
			tmp2 = EnvironmentVariableResolver.unixStyle().resolveIgnoreErrors(tmp1, envVariables);	
		} catch(VariableNotFoundException e){
			ErrorReporter.logError("There was an error while resolving `" + pathToBeResolved + "'");
			return null;
		} 

		final IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
		URI uri = URIUtil.toURI(tmp2);
		uri = pathVariableManager.resolveURI(uri);

		if (basePath != null && uri != null && !uri.isAbsolute()) {
			final String basePathString = URIUtil.toPath(basePath).toOSString();
			final String temp = PathUtil.getAbsolutePath(basePathString, tmp2);
			if (temp != null) {
				uri = URIUtil.toURI(temp);
			}
		}  
		return uri;

	}

	/**
	 * Resolves the provided path relative to the provided base path.
	 * 
	 * @param pathToBeResolved
	 *                the path to be resolved.
	 * @param basePath
	 *                the full path to which the resolvable one might be
	 *                relative to.
	 * 
	 * @return the resolved path.
	 * */
	public static URI resolvePathURI(final String pathToBeResolved, final String basePath) {
		final DebugPlugin debugPlugin = DebugPlugin.getDefault();

		if (debugPlugin == null) {
			ErrorReporter.logError("There was an error while resolving `" + pathToBeResolved + "'"
					+ "the DebugPlugin was not yet initialized");
			return URI.create(pathToBeResolved);
		}

		final Map<?, ?> envVariables = debugPlugin.getLaunchManager().getNativeEnvironmentCasePreserved();
		return resolvePathURI(pathToBeResolved, basePath, envVariables, ResourcesPlugin.getWorkspace().getPathVariableManager());
	}

	/**
	 * Resolves the provided path relative to the provided base path.
	 * @param pathToBeResolved
	 *                the path to be resolved.
	 * @param basePath
	 *                the full path to which the resolvable one might be
	 *                relative to.
	 * 
	 * @return the resolved path.
	 * */
	private static URI resolvePathURI(final String pathToBeResolved, final String basePath, final Map<?, ?> envVariables,
			final IPathVariableManager pathVariableManager) {

		String tmp2 = null;
		try {
			final String tmp1 = EnvironmentVariableResolver.eclipseStyle().resolve(pathToBeResolved, envVariables);
			tmp2 = EnvironmentVariableResolver.unixStyle().resolveIgnoreErrors(tmp1, envVariables);	
		} catch(VariableNotFoundException e){
			ErrorReporter.logError("There was an error while resolving `" + pathToBeResolved + "'");
			return null;
		} 
		
		URI uri = URIUtil.toURI(tmp2);
		URI resolvedURI = pathVariableManager.resolveURI(uri);
	
		if (basePath != null && !"".equals(basePath) && !resolvedURI.isAbsolute()) {
			final String temp = PathUtil.getAbsolutePath(basePath, tmp2);
			if (temp != null) {
				resolvedURI = URIUtil.toURI(temp);
			}
		}

		return resolvedURI;
	}

	/**
	 * Converts the provided uri relative to the provided base uri
	 * Environment variables and path variables will not be resolved.
	 * If the pathToBeConverted is absolute or the basePath is null, the basePath is not used
	 * @deprecated
	 * @param pathToBeConverted
	 *                the path to be resolved.
	 * @param basePath
	 *                the absolute URI with schema part and absolute path
	 *                relative to.
	 * 
	 * @return the resolved uri.
	 * */
	//not used, TODO: remove it!
	@Deprecated
	public static URI convertToAbsoluteURI(final String pathToBeConverted, final String basePath) {
		return convertToAbsoluteURI(pathToBeConverted, URIUtil.toURI(basePath));
	}
	//not used, TODO: remove it!
	
	@Deprecated 
	public static URI convertToAbsoluteURI(final String pathToBeConverted, final URI basePath) {
		final IPath tmp = new Path(pathToBeConverted);
		if( basePath != null && tmp != null && !tmp.isAbsolute()) {
			final URI convertedURI = org.eclipse.core.runtime.URIUtil.append(basePath, pathToBeConverted);
			if(convertedURI != null) {
				return convertedURI;
			}
		}
		return URIUtil.toURI(tmp); //!! wrong if basePath == null && !tmp.isAbsolute() because ../ will be removed !!
	}
}
