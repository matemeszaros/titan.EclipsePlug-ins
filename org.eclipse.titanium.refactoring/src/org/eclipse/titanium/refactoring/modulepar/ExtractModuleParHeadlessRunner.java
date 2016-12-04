/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.modulepar;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * Extract Module Parameters to a new TITAN project, from headless environment.
 * 
 * @author Istvan Bohm
 * */
public class ExtractModuleParHeadlessRunner implements IApplication {
	
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
		final boolean save = args.get("save")!=null;

		final IProject sourceProj = ResourcesPlugin.getWorkspace().getRoot().getProject(sourceProjName);
		if (sourceProj == null || !sourceProj.exists()) {
			ErrorReporter.logError("ExtractModuleParHeadless: Source project is not exist: " + sourceProjName);
			System.out.println("ExtractModuleParHeadless: Source project is not exist: " + sourceProjName);
			return -1;
		}
		
		final IProject targetProj = ResourcesPlugin.getWorkspace().getRoot().getProject(targetProjName);
		if(targetProj.exists()) {
			ErrorReporter.logError("ExtractModuleParHeadless: Output project is already exist: " + targetProjName);
			System.out.println("ExtractModuleParHeadless: Output project is already exist: " + targetProjName );
			return -1;
		}
		
		final ExtractModuleParHeadless headless = new ExtractModuleParHeadless();
		
		final String location = args.get("location");
		if(location!=null) {
			try {
				File d = new File(location);
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
				File f = new File(location,targetProjName);
				if(f.exists()) {
					throw new Exception("Project folder is already exist: " + f.getAbsolutePath() );
				}
				headless.setLocation(f.toURI());
			} catch (URISyntaxException e) {
				ErrorReporter.logError("ExtractModuleParHeadless: Location parameter is not a valid URI.");
				System.out.println("ExtractModuleParHeadless: Location parameter is not a valid URI. ");
				return -1;
			} catch (Exception e) {
				ErrorReporter.logError("ExtractModuleParHeadless: " + e.getMessage());
				System.out.println("ExtractModuleParHeadless: " + e.getMessage());
				return -1;
			}
		} 
		
		
		headless.run(sourceProj,targetProjName,save);
		
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
			if("-save".equals(arg)) {
				arguments.put("save","save");
			} else if("-in".equals(arg) || "-out".equals(arg) || "-location".equals(arg)) {
				++i;
				if(i==cliArgs.length) {
					throw new ArgumentException("Missing argument parameter of " + cliArgs[i-1]);
				}
				arguments.put(cliArgs[i-1].substring(1), cliArgs[i]);
			}
		}
		
		if(!arguments.containsKey("in") || arguments.get("in").length()==0) {
			throw new ArgumentException("Error, missing mandatory argument: -in");
		}
		if(!arguments.containsKey("out") || arguments.get("out").length()==0) {
			throw new ArgumentException("Error, missing mandatory argument: -out");
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
