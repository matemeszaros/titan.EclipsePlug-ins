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
	public static URI resolvePath(final String pathToBeResolved, final URI basePath) {
		final Map<?, ?> envVariables;
		if (DebugPlugin.getDefault() != null) {
			envVariables = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();
		} else {
			envVariables = null;
		}

		String tmp1 = null;
		String tmp2 = null;
		try {
			tmp1 = EnvironmentVariableResolver.eclipseStyle().resolve(pathToBeResolved, envVariables);
			tmp2 = EnvironmentVariableResolver.unixStyle().resolveIgnoreErrors(tmp1, envVariables);	
		} catch(VariableNotFoundException e){
			ErrorReporter.logError("There was an error while resolving `" + pathToBeResolved + "'");
			return null;
		} 
		
		final IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
			URI uri = URIUtil.toURI(tmp2);
			uri = pathVariableManager.resolveURI(uri);

			if (basePath != null && uri != null && !uri.isAbsolute()) {
				final URI mergedURI = org.eclipse.core.runtime.URIUtil.append(basePath, uri.toString());
				return mergedURI;
			
			} else {
				return uri;
			}
		
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
	public static IPath resolvePath(final String pathToBeResolved, final String basePath) {
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		if (debugPlugin == null) {
			ErrorReporter.logError("There was an error while resolving `" + pathToBeResolved + "'"
					+ "the DebugPlugin was not yet initialized");
			return new Path(pathToBeResolved);
		}

		final Map<?, ?> envVariables = debugPlugin.getLaunchManager().getNativeEnvironmentCasePreserved();
		return resolvePath(pathToBeResolved, basePath, envVariables, ResourcesPlugin.getWorkspace().getPathVariableManager());
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
	public static IPath resolvePath(final String pathToBeResolved, final String basePath, final Map<?, ?> envVariables,
			IPathVariableManager pathVariableManager) {
		String tmp1 = EnvironmentVariableResolver.eclipseStyle().resolveIgnoreErrors(pathToBeResolved, envVariables);
		String tmp2 = EnvironmentVariableResolver.unixStyle().resolveIgnoreErrors(tmp1, envVariables);

		final IPath path2 = new Path(tmp2);
		IPath resolvedPath = pathVariableManager.resolvePath(path2);

		if (basePath != null && !"".equals(basePath) && !resolvedPath.isAbsolute()) {
			final String temp = PathUtil.getAbsolutePath(basePath, tmp2);
			if (temp != null) {
				resolvedPath = new Path(temp);
			}
		}

		return resolvedPath;
	}

	/**
	 * Resolves the provided path relative to the provided base path and
	 * returns the result as an URI.
	 * 
	 * @param path
	 *                the path to be resolved.
	 * @param rootPath
	 *                the full path to which the resolvable one might be
	 *                relative to.
	 * 
	 * @return the resolved URI.
	 * */
	public static URI getURI(final String path, final String rootPath) {
		final IPath resolvedPath = resolvePath(path, rootPath);

		return URIUtil.toURI(resolvedPath);
	}
	
	/**
	 * Converts the provided uri relative to the provided base uri
	 * Environment variables and path variables will not be resolved.
	 * If the pathToBeConverted is absolute or the basePath is null, the basePath is not used
	 * 
	 * @param pathToBeConverted
	 *                the path to be resolved.
	 * @param basePath
	 *                the absolute URI with schema part and absolute path
	 *                relative to.
	 * 
	 * @return the resolved uri.
	 * */
	public static URI convertToAbsoluteURI(final String pathToBeConverted, final String basePath) {
		return convertToAbsoluteURI(pathToBeConverted, URIUtil.toURI(basePath));
	}
	
	public static URI convertToAbsoluteURI(final String pathToBeConverted, final URI basePath) {
		IPath tmp = new Path(pathToBeConverted);
		if( basePath != null && tmp != null && !tmp.isAbsolute()) {
			final URI convertedURI = org.eclipse.core.runtime.URIUtil.append(basePath, pathToBeConverted);
			if(convertedURI != null) {
				return convertedURI;
			}
		}
		return URIUtil.toURI(tmp); //!! wrong if basePath == null && !tmp.isAbsolute() because ../ will be removed !!
	}
}
