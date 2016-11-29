/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   
 *   Keremi, Andras
 *   Eros, Levente
 *   Kovacs, Gabor
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.codegenerator.experimental.LoggerVisitor;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definitions;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Analyzer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public final class AstWalkerRunnerJava implements IWorkbenchWindowActionDelegate {

	private static IWorkbenchWindow window;

	private static Logger logger;

	private static IProject selectedProject;
	public static Properties props;
	private static List<String> fileNames;
	private static List<IFile> files;

	private static TTCN3Module currentTTCN3module;

	public static String moduleElementName = "";

	public static boolean areCommentsAllowed = true;
	public static List<String> componentList = new ArrayList<String>();
	public static List<String> testCaseList = new ArrayList<String>();
	public static List<String> testCaseRunsOnList = new ArrayList<String>();
	public static List<String> functionList = new ArrayList<String>();
	public static List<String> functionRunsOnList = new ArrayList<String>();

	static {
		try {
			boolean append = true;
			props = new Properties();
			props.load(AstWalkerRunnerJava.class.getResourceAsStream("walker.properties"));

			FileHandler fh = new FileHandler(props.getProperty("log.path"), append);

			fh.setFormatter(new SimpleFormatter());
			logger = Logger.getLogger(AstWalkerRunnerJava.class.getName());
			logger.addHandler(fh);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new AstWalkerRunnerJava().run(null);
	}

	@SuppressWarnings("unchecked")
	public void run(IAction action) {

		(new AstWalkerJava()).run(action);
		(new AstRunnerJava()).run(action);

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	private static void getActiveProject() {
		IWorkbenchPage p = window.getActivePage();
		IFile openFile = null;
		if (p != null) {
			IEditorPart e = p.getActiveEditor();
			if (e != null) {
				IEditorInput i = e.getEditorInput();
				if (i instanceof IFileEditorInput) {
					openFile = ((IFileEditorInput) i).getFile();
					if (openFile.getName().endsWith("ttcn3") || openFile.getName().endsWith("ttcn")) {
						AstWalkerRunnerJava.selectedProject = openFile.getProject();
					}
					logger.severe(openFile.getLocation().toOSString() + "\n");
				}
			}
		}

	}

	public static void initOutputFolder() {
		Path outputPath = Paths.get(props.getProperty("javafile.path"));
		if (Files.exists(outputPath)) {
			System.out.println("");
			File folder = new File(outputPath.toUri());
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].getPath().toString().endsWith("java")) {
				} else {
					listOfFiles[i] = null;
				}
			}
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i] != null) {
					listOfFiles[i].delete();
				}
			}

		} else {
			(new File(props.getProperty("javafile.path"))).mkdirs();

		}
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		fileNames = new ArrayList<String>();
		files = new ArrayList<IFile>();
		AstWalkerRunnerJava.window = window;
		try {
			FileOutputStream fos = new FileOutputStream(props.getProperty("log.path"));
			fos.write("open file".getBytes());
			IWorkbenchPage p = window.getActivePage();
			if (p != null) {
				IEditorPart e = p.getActiveEditor();
				if (e != null) {
					IEditorInput i = e.getEditorInput();
					if (i instanceof IFileEditorInput) {
						fos.write(((IFileEditorInput) i).getFile().getLocation().toOSString().getBytes());
					}
				}
			}
			fos.write("\n".getBytes());
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editor : page.getEditorReferences()) {
					IEditorInput input = editor.getEditorInput();
					if (input instanceof IFileEditorInput) {
						fos.write(((IFileEditorInput) input).getFile().getLocation().toOSString().getBytes());
					}
				}
			}
			fos.flush();
			fos.close();
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final Module doAnalysis(IFile file, String code) {
		TTCN3Analyzer ttcn3Analyzer = new TTCN3Analyzer();

		if (file != null) {
			logger.severe("Calling TTCN3 parser with " + file.getName() + " and " + null + "\n");
			ttcn3Analyzer.parse(file, null);
		} else if (code != null) {
			logger.severe("Calling TTCN3 parser with null and " + code + "\n");
			ttcn3Analyzer.parse(null, code);
		} else
			return null;

		Module module = ttcn3Analyzer.getModule();

		return module;

	}

	private void walkChildren(ASTVisitor visitor, Object[] children) {
		LoggerVisitor logger = new LoggerVisitor();
		for (Object child : children) {
			if (child instanceof Definitions) {

				Definitions definitions = (Definitions) child;

				moduleElementName = definitions.getFullName();

				logToConsole("Starting processing:  " + moduleElementName);
				myASTVisitor.myFunctionTestCaseVisitHandler.clearEverything();
				// myASTVisitor.templateIdValuePairs.clear();

				if (Boolean.parseBoolean(props.getProperty("ast.log.enabled"))) {
					definitions.accept(logger);
				}
				definitions.accept(visitor);
				logToConsole("Finished processing:  " + moduleElementName);
			}
		}
	}

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	public static void logToConsole(String msg) {
		MessageConsole myConsole = findConsole("myLogger");
		MessageConsoleStream consoleLogger = myConsole.newMessageStream();
		consoleLogger.println(msg);
	}

}
