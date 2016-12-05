/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.definition;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.refactoring.Utils;

/**
 * Extract Definition and all of its dependencies to a new TITAN project, from headless environment.
 *
 * @author Istvan Bohm
 * */
public class ExtractDefinitionHeadlessRunner implements IApplication {

	@Override
	public Object start(final IApplicationContext context) {

		final String[] cmdArguments = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);

		Map<String, String> args;
		try {
			args = processArgs(cmdArguments);
		} catch (ArgumentException e) {
			ErrorReporter.logError(e.getMessage());
			System.out.println(e.getMessage());
			return -1;
		}

		final String sourceProjName = args.get("in");
		final String targetProjName = args.get("out");
		final String moduleName = args.get("module");
		final String definitionName = args.get("definition");

		final IProject sourceProj = ResourcesPlugin.getWorkspace().getRoot().getProject(sourceProjName);
		if (sourceProj == null || !sourceProj.exists()) {
			ErrorReporter.logError("ExtractDefinitionHeadless: Source project is not exist: " + sourceProjName);
			System.out.println("ExtractDefinitionHeadless: Source project is not exist: " + sourceProjName);
			return -1;
		}

		final IProject targetProj = ResourcesPlugin.getWorkspace().getRoot().getProject(targetProjName);
		if(targetProj.exists()) {
			ErrorReporter.logError("ExtractDefinitionHeadless: Output project is already exist: " + targetProjName);
			System.out.println("ExtractDefinitionHeadless: Output project is already exist: " + targetProjName );
			return -1;
		}

		//update AST to get the module and definition
		final Set<IProject> projsToUpdate = new HashSet<IProject>();
		projsToUpdate.add(sourceProj);
		Utils.updateASTBeforeRefactoring(projsToUpdate, "ExtractDefinition");

		final ProjectSourceParser parser = GlobalParser.getProjectSourceParser(sourceProj);
		final Module sourceModule = parser.getModuleByName(moduleName);
		if (sourceModule == null) {
			ErrorReporter.logError("ExtractDefinitionHeadless: Could not find module: " + moduleName);
			System.out.println("ExtractDefinitionHeadless: Could not find module: " + moduleName);
			return -1;
		}

		final Assignment assignment = sourceModule.getAssignments().getLocalAssignmentByID(CompilationTimeStamp.getBaseTimestamp(), new Identifier(Identifier_type.ID_NAME, definitionName));
		if (assignment == null) {
			ErrorReporter.logError("ExtractDefinitionHeadless: Could not find definition: " + definitionName);
			System.out.println("ExtractDefinitionHeadless: Could not find definition: " + definitionName);
			return -1;
		}

		if (!(assignment instanceof Definition)) {
			ErrorReporter.logError("ExtractDefinitionHeadless: Currently only TTCN-3 definitions can be the source definition");
			System.out.println("ExtractDefinitionHeadless: Currently only TTCN-3 definitions can be the source definition");
			return -1;
		}
		final Definition definition = (Definition)assignment;


		final ExtractDefinitionHeadless headless = new ExtractDefinitionHeadless();

		final String location = args.get("location");
		if(location!=null) {
			try {
				final File d = new File(location);
				if(!d.exists()) {
					try{
						d.mkdirs();
					}
					catch(Exception e){
						throw new Exception("Could not create the parent folder of the project: " + d.getAbsolutePath() );
					}
				} else if(!d.isDirectory()) {
					throw new Exception("Provided location must be a directory.");
				}
				final File f = new File(location,targetProjName);
				if(f.exists()) {
					throw new Exception("Project folder is already exist: " + f.getAbsolutePath() );
				}
				headless.setLocation(f.toURI());
			} catch (URISyntaxException e) {
				ErrorReporter.logError("ExtractDefinitionHeadless: Location parameter is not a valid URI.");
				System.out.println("ExtractDefinitionHeadless: Location parameter is not a valid URI. ");
				return -1;
			} catch (Exception e) {
				ErrorReporter.logError("ExtractDefinitionHeadless: " + e.getMessage());
				System.out.println("ExtractDefinitionHeadless: " + e.getMessage());
				return -1;
			}
		}
		headless.run(sourceProj, definition, targetProjName);

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		//do nothing
	}

	private Map<String, String> processArgs(final String[] cliArgs) throws ArgumentException {
		final HashMap<String, String> arguments = new HashMap<String, String>(cliArgs.length);
		for(int i=0;i<cliArgs.length;++i) {
			final String arg = cliArgs[i];
			if("-in".equals(arg) || "-out".equals(arg) || "-location".equals(arg) ||
					"-definition".equals(arg) || "-module".equals(arg)) {
				++i;
				if(i==cliArgs.length) {
					ErrorReporter.logError("Missing argument parameter of " + cliArgs[i-1]);
					throw new ArgumentException("Missing argument parameter of " + cliArgs[i-1]);
				}
				arguments.put(cliArgs[i-1].substring(1), cliArgs[i]);
			}
		}

		if(!arguments.containsKey("in") || arguments.get("in").length()==0) {
			ErrorReporter.logError("Error, missing mandatory argument: -in");
			throw new ArgumentException("Error, missing mandatory argument: -in");
		}
		if(!arguments.containsKey("out") || arguments.get("out").length()==0) {
			ErrorReporter.logError("Error, missing mandatory argument: -out");
			throw new ArgumentException("Error, missing mandatory argument: -out");
		}
		if(!arguments.containsKey("module") || arguments.get("module").length()==0) {
			ErrorReporter.logError("Error, missing mandatory argument: -module");
			throw new ArgumentException("Error, missing mandatory argument: -module");
		}
		if(!arguments.containsKey("definition") || arguments.get("definition").length()==0) {
			ErrorReporter.logError("Error, missing mandatory argument: -definition");
			throw new ArgumentException("Error, missing mandatory argument: -definition");
		}

		return arguments;
	}

	private class ArgumentException extends Exception {
		private static final long serialVersionUID = 1L;

		public ArgumentException(final String message) {
			super(message);
		}
	}
}
