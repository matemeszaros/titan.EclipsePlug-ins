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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.ui.*;
import org.eclipse.ui.console.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class AstWalkerJava implements IWorkbenchWindowActionDelegate {

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
			props.load(AstWalkerJava.class.getResourceAsStream("walker.properties"));

			FileHandler fh = new FileHandler(props.getProperty("log.path"), append);

			fh.setFormatter(new SimpleFormatter());
			logger = Logger.getLogger(AstWalkerJava.class.getName());
			logger.addHandler(fh);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new AstWalkerJava().run(null);
	}

	@SuppressWarnings("unchecked")
	public void run(IAction action) {

		/**/
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(IProject p:projects){
			if(p.getName().equals("org.eclipse.titan.codegenerator.output"))
				try{
					p.delete(true, true, null);
				}catch(Exception e){e.printStackTrace();}
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("org.eclipse.titan.codegenerator.output");
		try{
			project.create(null);
			project.open(null);
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);
			IJavaProject javaProject = JavaCore.create(project);
			IFolder binFolder = project.getFolder("bin");
			binFolder.create(false, true, null);
			javaProject.setOutputLocation(binFolder.getFullPath(), null);
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
			for (LibraryLocation element : locations) {
			 entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
			}
			javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
			IFolder sourceFolder = project.getFolder("src");
			sourceFolder.create(false, true, null);
			IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(sourceFolder);
			IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
			IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
			javaProject.setRawClasspath(newEntries, null);
			javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment("org.eclipse.titan.codegenerator.javagen", false, null);
			javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment("org.eclipse.titan.codegenerator.TTCN3JavaAPI", false, null);
		}catch(Exception e){e.printStackTrace();}

		IPath ws;
		ws = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		String basepackage = Paths.get("src", "org", "eclipse", "titan", "codegenerator").toString();
		props.setProperty("javafile.path", ws.append("org.eclipse.titan.codegenerator.output").append(basepackage).append("javagen").toString());
		/**/
		
		AstWalkerJava.files.clear();
		AstWalkerJava.fileNames.clear();
		AstWalkerJava.componentList.clear();
		AstWalkerJava.testCaseList.clear();
		AstWalkerJava.testCaseRunsOnList.clear();
		AstWalkerJava.functionList.clear();
		AstWalkerJava.functionRunsOnList.clear();

		AstWalkerJava.initOutputFolder();
		AstWalkerJava.getActiveProject();

		/*
		 * // init console logger IConsole myConsole = findConsole("myLogger");
		 * IWorkbenchPage page = window.getActivePage(); String id =
		 * IConsoleConstants.ID_CONSOLE_VIEW; IConsoleView view; try { view =
		 * (IConsoleView) page.showView(id); view.display(myConsole); } catch
		 * (PartInitException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		// initialize common files

		myASTVisitor.currentFileName = "TTCN_functions";

		myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings);
		myASTVisitor.visualizeNodeToJava("class TTCN_functions{\r\n}\r\n");

		Def_Template_Visit_Handler.isTemplate = false;

		final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(selectedProject);
		sourceParser.analyzeAll();

		logToConsole("Version built on 2016.10.24");
		logToConsole("Starting to generate files into: " + props.getProperty("javafile.path"));

		myASTVisitor visitor = new myASTVisitor();
		for (Module module : sourceParser.getModules()) {
			// start AST processing
			walkChildren(visitor, module.getOutlineChildren());
		}
		visitor.finish();

		logToConsole("Files generated into: " + props.getProperty("javafile.path"));

		// write additional classes
		Additional_Class_Writer additional_class = new Additional_Class_Writer();
		myASTVisitor.currentFileName = "HC";

		myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings);
		myASTVisitor.visualizeNodeToJava(additional_class.writeHCClass());

		myASTVisitor.currentFileName = "HCType";
		myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings);
		myASTVisitor.visualizeNodeToJava(additional_class.writeHCTypeClass());

		// clear lists

		logger.severe("analysis complete");

		/**/
		File fromdir = ws.append("org.eclipse.titan.codegenerator").append(basepackage).append("TTCN3JavaAPI").toFile();
		String toapidir = ws.append("org.eclipse.titan.codegenerator.output").append(basepackage).append("TTCN3JavaAPI").toFile().toString();
		File[] fromfiles = fromdir.listFiles();
		for(File f: fromfiles){
			try {
				Files.copy(Paths.get(f.getAbsolutePath()), Paths.get(toapidir+f.getName()), StandardCopyOption.REPLACE_EXISTING);
			}catch(Exception e){e.printStackTrace();}
		}
		File tp_cfg_dir = ws.append(selectedProject.getFullPath().segment(1)).append("src").toFile();
		String togendir = ws.append("org.eclipse.titan.codegenerator.output").append(basepackage).append("javagen").toFile().toString();
		File[] from_testports_cfg = tp_cfg_dir.listFiles();
		for(File f: from_testports_cfg){
			if(f.getName().endsWith(".java")){
				try{
					Files.copy(Paths.get(f.getAbsolutePath()), Paths.get(togendir+f.getName()), StandardCopyOption.REPLACE_EXISTING);
				}catch(Exception e){e.printStackTrace();}
			}
			if(f.getName().endsWith(".cfg")){
				try{
					Files.copy(Paths.get(f.getAbsolutePath()), Paths.get(toapidir+"cfg.cfg"), StandardCopyOption.REPLACE_EXISTING);
				}catch(Exception e){e.printStackTrace();}
			}
		}
		try{
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}catch(Exception e){e.printStackTrace();}
		/**/
		
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
						AstWalkerJava.selectedProject = openFile.getProject();
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
		AstWalkerJava.window = window;
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
