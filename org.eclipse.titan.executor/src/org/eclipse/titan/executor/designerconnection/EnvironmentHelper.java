/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.designerconnection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.executor.TITANDebugConsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class has utility functions to help handling environmental variables.
 * 
 * @author Kristof Szabados
 * */
public final class EnvironmentHelper {

	public static final String TTCN_3_DIR = "TTCN3_DIR";
	public static final String LD_LIBRARY_PATH = "LD_LIBRARY_PATH";

	private EnvironmentHelper() {
	}

	/**
	 * Tries to resolve the variables in the environmental variables found in the launch configuration.
	 *
	 * @param originalVariables the original variables to use.
	 *
	 * @return the new hashmap of environment variables that were resolved.
	 * */
	public static Map<String, String> resolveVariables(final HashMap<String, String> originalVariables) throws CoreException {
		final Map<String, String> env = new HashMap<String, String>(originalVariables.size());
		final IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		for (Map.Entry<String, String> variable : originalVariables.entrySet()) {
			if (null != variable.getValue()) {
				final String finalValue = manager.performStringSubstitution(variable.getValue());
				if (null != finalValue) {
					env.put(variable.getKey(), finalValue);
				}
			}
		}

		return env;
	}

	/**
	 * Tries to update a set of environmental variables with an other set of environmental variables,
	 * while also resolving the elements if the second set.
	 *
	 * @param originalVariables the original variables to update.
	 * @param additionalVariables the set of environmental variables to update with the original ones.
	 *
	 * @return the original hashmap of environment variables that were updated.
	 * */
	public static Map<String, String> resolveVariables(final Map<String, String> originalVariables,
	                                                   final Map<String, String> additionalVariables) throws CoreException {
		final IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		for (Map.Entry<String, String> variable : additionalVariables.entrySet()) {
			if (null != variable.getValue()) {
				final String finalValue = manager.performStringSubstitution(variable.getValue());
				if (null != finalValue) {
					originalVariables.put(variable.getKey(), finalValue);
				}
			}
		}

		return originalVariables;
	}

	/**
	 * Collects the installation path of TITAN and converts it into usable form.
	 * <p>
	 * First the provided environmental variables are checked, than the workbench level settings from the Designer.
	 *
	 * @param env the environmental variables to use first.
	 * @return the converted path.
	 * */
	public static String getTitanPath(final Map<String, String> env) {
		if (env.containsKey(TTCN_3_DIR)) {
			return env.get(TTCN_3_DIR);
		}

		final String temp = DesignerHelper.getTTCN3DIR();
		if (null == temp || 0 == temp.trim().length()) {
			return temp;
		}

		return PathConverter.convert(temp, true, TITANDebugConsole.getConsole());
	}

	/**
	 * Collects the installation path of TITAN, converts it into usable form and sets it in the provided map of environmental variables.
	 * <p>
	 * First the provided environmental variables are checked, than the workbench level settings from the Designer.
	 *
	 * @param env the environmental variables to use first.
	 * @return the environmental variables updated with the new value if needed..
	 * */
	public static Map<String, String> setTitanPath(final Map<String, String> env) {
		if (env.containsKey(TTCN_3_DIR)) {
			return env;
		}

		String temp = DesignerHelper.getTTCN3DIR();
		if (null == temp || 0 == temp.trim().length()) {
			return env;
		}

		temp = PathConverter.convert(temp, true, TITANDebugConsole.getConsole());
		env.put(TTCN_3_DIR, temp);

		return env;
	}

	/**
	 * Collects the location of the license file, converts it into usable form and sets it in the provided map of environmental variables.
	 * <p>
	 * First the provided environmental variables are checked, than the workbench level settings from the Designer.
	 *
	 * @param env the environmental variables to use first.
	 * @return the environmental variables updated with the new value if needed..
	 * */
	public static Map<String, String> set_LICENSE_FILE_PATH(final Map<String, String> env) {
		if (env.containsKey("TTCN3_LICENSE_FILE")) {
			return env;
		}

		String temp = DesignerHelper.getTTCN3LICENSEFILE();
		if (null == temp || 0 == temp.trim().length()) {
			return env;
		}

		temp = PathConverter.convert(temp, true, TITANDebugConsole.getConsole());
		env.put("TTCN3_LICENSE_FILE", temp);

		return env;
	}

	/**
	 * Calculates the correct value for the LD_LIBRARY_PATH environmental variable so that the executable in a given project could be executed..
	 * <p>
	 * To reach this the working directories of all referenced projects have to be used.
	 *
	 * @param actualProject the project used to build the executable.
	 * @param env the environmental variables to use.
	 * @return the environmental variables updated with the new value if needed..
	 * */
	public static Map<String, String> set_LD_LIBRARY_PATH(final IProject actualProject, final Map<String, String> env) {
		final StringBuilder workingDirectories = new StringBuilder();
		if (null != actualProject) {
			final List<IProject> knownProjects = new ArrayList<IProject>();
			DynamicLinkingHelper.getAllReachableProjects(new ArrayList<IProject>(), actualProject, knownProjects);

			for (IProject tempProject : knownProjects) {
				try {
					final String workingdirectory = tempProject.getPersistentProperty(
							new QualifiedName(DesignerHelper.PROJECT_BUILD_PROPERTYPAGE_QUALIFIER, DesignerHelper.WORKINGDIR_PROPERTY));
					final String projectPath = actualProject.getLocation().toString();
					workingDirectories.append(':').append(PathConverter.convert(projectPath + "/" + workingdirectory, true, TITANDebugConsole.getConsole()));
				} catch (CoreException e) {
					ErrorReporter.logError("The working directory of project " + tempProject.getName() + " could not be determined");
				}
			}

			String libraryPath = env.get(LD_LIBRARY_PATH);
			libraryPath = libraryPath == null ? "" : libraryPath;
			final String titanPath = getTitanPath(env);
			final String newLibraryPath = libraryPath + workingDirectories.toString() + (titanPath.length() > 0 ? (":" + titanPath + "/lib") : "");
			env.put(LD_LIBRARY_PATH, newLibraryPath);
		}

		return env;
	}
}
