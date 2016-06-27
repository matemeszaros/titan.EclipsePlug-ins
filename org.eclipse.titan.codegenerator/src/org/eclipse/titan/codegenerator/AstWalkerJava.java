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
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definitions;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ImportModule;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Analyzer;

public class AstWalkerJava implements IWorkbenchWindowActionDelegate {

	IWorkbenchWindow window;

	private static Logger logger;
	private ISelection selection;
	private IProject selectedProject;
	public static Properties props;
	private List<String> fileNames;
	private List<IFile> files;

	private TTCN3Module currentTTCN3module;


	public static String moduleElementName = "";

	public static boolean areCommentsAllowed=true;

	//public static String currentFileName = "";

	



	// copied from ttcn-3

	static {
		try {
			boolean append = true;
			props = new Properties();
			props.load(AstWalkerJava.class
					.getResourceAsStream("walker.properties"));
			// props.list(System.err);
			// FileHandler fh = new
			// FileHandler("D:\\temp\\GenerateModelActionDelegate.log", append);

			FileHandler fh = new FileHandler(props.getProperty("log.path"),
					append);

			// fh.setFormatter(new XMLFormatter());
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

	public void run(IAction action) {
		// logger.severe(hu.bme.tmit.catg.data.model.System.getSystem().getModelNames());
		// hu.bme.tmit.catg.data.model.System.getSystem().reset();
		// logger.severe(hu.bme.tmit.catg.data.model.System.getSystem().getModelNames());

		this.files.clear();
		this.fileNames.clear();


		try {

			IWorkbenchPage p = window.getActivePage();
			IFile openFile = null;
			if (p != null) {
				IEditorPart e = p.getActiveEditor();
				if (e != null) {
					IEditorInput i = e.getEditorInput();
					if (i instanceof IFileEditorInput) {
						openFile = ((IFileEditorInput) i).getFile();
						if (openFile.getName().endsWith("ttcn3")
								|| openFile.getName().endsWith("ttcn")) {
							this.selectedProject = openFile.getProject();
						}
						logger.severe(openFile.getLocation().toOSString()
								+ "\n");
					}
				}
			}

			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow window = workbench == null ? null : workbench
					.getActiveWorkbenchWindow();
			IWorkbenchPage activePage = window == null ? null : window
					.getActivePage();
			IEditorPart editor = activePage == null ? null : activePage
					.getActiveEditor();
			IEditorInput input = editor == null ? null : editor
					.getEditorInput();
			IPath path = input instanceof FileEditorInput ? ((FileEditorInput) input)
					.getPath() : null;

			IPath folderPath = path.removeLastSegments(1);

			// get files in the folder
			File folder = new File(folderPath.toString());
			File[] listOfFiles = folder.listFiles();
			// Sort them in ascending order (temporary fix for linux)
			Arrays.sort(listOfFiles);
			String leadingPath = "";

			for (int i = 0; i < path.segmentCount(); i++) {
				if (path.segment(i).equals(selectedProject.getName())) {
					leadingPath = path.removeLastSegments(
							(path.segmentCount() - i - 1)).toString();
				}
			}

			if (path != null) {

				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].getPath().endsWith(".ttcn")) {
						// cut the first half of the path so IFile can get it
						String filePath = listOfFiles[i].getPath().substring(
								leadingPath.length());

						IFile file = selectedProject.getFile(filePath);
						files.add(file);
					}
				}

			}

			logger.severe(("Names:" + fileNames.size()));
			for (String s : fileNames) {
				logger.severe(s);
			}
			for (IFile f : files) {
				logger.severe(f.getName() + " " + f.getLocation().toOSString());
			}

			Map<String, Module> modules = new TreeMap<String, Module>();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].getPath().endsWith(".ttcn")) {
					fileNames.add(listOfFiles[i].getPath());
				}
			}

			//initialize common files
			myASTVisitor.currentFileName = "Constants";
			myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings); 
			myASTVisitor.visualizeNodeToJava("class Constants{\r\n}\r\n");

			myASTVisitor.currentFileName = "Templates";

			myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings);
			myASTVisitor.visualizeNodeToJava("class Templates{\r\n}\r\n");

			myASTVisitor.currentFileName = "TTCN_functions";

			myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings); 
			myASTVisitor.visualizeNodeToJava("class TTCN_functions{\r\n}\r\n");
			
			
			if (fileNames.size() > 0) {
				for (int i = 0, num = files.size(); i < num; i++) {
					
					//process files
					currentTTCN3module = (TTCN3Module) doAnalysis(files.get(i),
							null);
					modules.put(fileNames.get(i), currentTTCN3module);
					logger.severe(currentTTCN3module.getName()
							+ " "
							+ currentTTCN3module.getIdentifier()
									.getDisplayName());
					if (currentTTCN3module.getOutlineChildren().length > 1) {
						for (ImportModule mm : (List<ImportModule>) currentTTCN3module
								.getOutlineChildren()[0]) {
							logger.severe(currentTTCN3module.getName()
									+ " imported "
									+ currentTTCN3module.getIdentifier()
											.getDisplayName());
						}
					}
				}

				for (int i = 0, num = files.size(); i < num; i++) {

					Object[] modulestart = modules.get(fileNames.get(i))
							.getOutlineChildren();
					
					//start AST processing
					walkChildren(modulestart);

				}

			} else {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(
								new Shell(Display.getDefault()),
								"Generating test model", "No TTCN-3 files");
					}
				});
				return;
			}

			
			
			//write additional classes
			Additional_Class_Writer additional_class=new Additional_Class_Writer();
			myASTVisitor.currentFileName = "HC";
			
			myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings); 
			myASTVisitor.visualizeNodeToJava(additional_class.writeHCClass());
			
			myASTVisitor.currentFileName = "HCType";
			myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings); 
			myASTVisitor.visualizeNodeToJava(additional_class.writeHCTypeClass());
			
			//clear lists 
			myASTVisitor.componentList.clear();
			myASTVisitor.testCaseList.clear();
			myASTVisitor.testCaseRunsOnList.clear();
			myASTVisitor.functionList.clear();
			myASTVisitor.functionRunsOnList.clear();

		
			logger.severe("analysis complete");

		} finally {

		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		fileNames = new ArrayList<String>();
		files = new ArrayList<IFile>();
		this.window = window;
		try {
			FileOutputStream fos = new FileOutputStream(
					props.getProperty("log.path"));
			fos.write("open file".getBytes());
			IWorkbenchPage p = window.getActivePage();
			if (p != null) {
				IEditorPart e = p.getActiveEditor();
				if (e != null) {
					IEditorInput i = e.getEditorInput();
					if (i instanceof IFileEditorInput) {
						fos.write(((IFileEditorInput) i).getFile()
								.getLocation().toOSString().getBytes());
					}
				}
			}
			fos.write("\n".getBytes());
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editor : page.getEditorReferences()) {
					IEditorInput input = editor.getEditorInput();
					if (input instanceof IFileEditorInput) {
						fos.write(((IFileEditorInput) input).getFile()
								.getLocation().toOSString().getBytes());
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
		// try {
		if (file != null) {
			logger.severe("Calling TTCN3 parser with " + file.getName()
					+ " and " + null + "\n");
			ttcn3Analyzer.parse(file, null);
		} else if (code != null) {
			logger.severe("Calling TTCN3 parser with null and " + code + "\n");
			ttcn3Analyzer.parse(null, code);
		} else
			return null;

		Module module = ttcn3Analyzer.getModule();

		return module;

		/*
		 * } catch (FileNotFoundException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } return null;
		 */
	}

	public void walkChildren(Object[] uncastedChildren) {

		for (int i = 0; i < uncastedChildren.length; i++) {

			if (uncastedChildren[i] instanceof Definitions) {

				Definitions castedChildren = (Definitions) uncastedChildren[i];

				moduleElementName = castedChildren.getFullName().toString();


				myASTVisitor v = new myASTVisitor();
				castedChildren.accept(v);
			}
		}
	}


}
