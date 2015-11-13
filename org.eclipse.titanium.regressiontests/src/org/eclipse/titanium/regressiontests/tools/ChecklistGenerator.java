/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

public class ChecklistGenerator extends AbstractHandler implements IWorkbenchWindowActionDelegate {
	static final String LOCATION_FORMAT = "line (.+)";
	static final Pattern LOCATION_FORMAT_PATTERN = Pattern.compile(LOCATION_FORMAT);
	
	private ArrayList<IFile> files;

	// represents a testing element (a file), needed to be able to generate the tests themselves
	final class TestSuiteElement {
		private final  String fileName;
		private final  String fileLocation;
		
		private TestSuiteElement(final String fileName, final String fileLocation) {
			this.fileName = fileName;
			this.fileLocation = fileLocation;
		}
	}
	
	// represents an error message
	final class Error_Message {
		private final String text;
		private final int line;
		private final String filename;
		private final String location;

		private Error_Message(final String text, final int line, final String filename, final String location) {
			this.text = text;
			this.line = line;
			this.filename = filename;
			this.location = location;
		}
	}
	
	@Override
	public void init(final IWorkbenchWindow window) {
		files = new ArrayList<IFile>();
	}

	@Override
	public void dispose() {
		// sometimes #init() was not called, so files.clear() caused NPE. 
		files = new ArrayList<IFile>();
	}
	
	/*
	 * Prints the error section to a StringBuilder.
	 * An error section is a region of error messages that can be grouped to have only one code item generated for them
	 * */
	public static void printErrorSection(final StringBuilder builder, final Error_Message sectionStarterError, final int inside_row, final int followings, final int last_row, final AtomicInteger printI) {
		if (inside_row > 1) {
			if (sectionStarterError.line != last_row) {
				builder.append("\t\tlineNum += " + (sectionStarterError.line - last_row) + ";\n");
			}

			if (printI.get() == 1) {
				builder.append("\t\tint i = 0;\n");
				printI.set(0);
			}
			builder.append("\t\tfor (i = 0; i < " + new Integer(inside_row).toString() + "; i++) { ");
			builder.append("markersToCheck.add(new MarkerToCheck(\"").append(sectionStarterError.text).append("\", ");
			builder.append("lineNum, IMarker.SEVERITY_FILL_ME_OUT)); }\n");
		} else if (followings > 0) {
			if (sectionStarterError.line != last_row) {
				builder.append("\t\tlineNum += " + (sectionStarterError.line - last_row) + ";\n");
			}
			if (printI.get() == 1) {
				builder.append("\t\tint i = 0;\n");
				printI.set(0);
			}
			builder.append("\t\tfor (i = 0; i < " + new Integer(followings + 1).toString() + "; i++) { ");
			builder.append("markersToCheck.add(new MarkerToCheck(\"").append(sectionStarterError.text).append("\", ");
			builder.append("lineNum++, IMarker.SEVERITY_FILL_ME_OUT)); }\n");
		} else {
			if (sectionStarterError.line == last_row) {
				builder.append("\t\tmarkersToCheck.add(new MarkerToCheck(\"").append(sectionStarterError.text).append("\", ");
				builder.append(" lineNum, IMarker.SEVERITY_FILL_ME_OUT));\n");
			} else if (sectionStarterError.line == last_row + 1) {
				builder.append("\t\tmarkersToCheck.add(new MarkerToCheck(\"").append(sectionStarterError.text).append("\", ");
				builder.append(" ++lineNum, IMarker.SEVERITY_FILL_ME_OUT));\n");
			} else if (sectionStarterError.line == last_row - 1) {
				builder.append("\t\tmarkersToCheck.add(new MarkerToCheck(\"").append(sectionStarterError.text).append("\", ");
				builder.append(" --lineNum, IMarker.SEVERITY_FILL_ME_OUT));\n");
			} else {
				builder.append("\t\tlineNum += " + (sectionStarterError.line - last_row) + ";\n");
				builder.append("\t\tmarkersToCheck.add(new MarkerToCheck(\"").append(sectionStarterError.text).append("\", ");
				builder.append(" lineNum, IMarker.SEVERITY_FILL_ME_OUT));\n");
			}
		}
	}
	
	/**
	 * Handles the special characters in the error messages.
	 * As they will be inserted into a string the character '\"' must be escaped.
	 *
	 * @param builder the builder containing the text to be processed.
	 *
	 * @return the StringBuilder containing the processed text.
	 * */
	public static StringBuilder handleEscapeSequences(final StringBuilder builder) {
		for (int index = builder.length() - 1; index >= 0; index--) {
			switch (builder.charAt(index)) {
			case '\"':
			case '\\':
				if (index == 0) {
					builder.insert(0, '\\');
				} else if (builder.charAt(index - 1) != '\\') {
					builder.insert(index, '\\');
				} else {
					index--;
				}
				break;
			default:
				break;
			}
		}

		return builder;
	}
	
	@Override
	public void run(final IAction action) {
		//for every selected file
		for (int i = 0, size = files.size(); i < size; i++) {
			final IFile file = files.get(i);
			
			IPath path = file.getLocation();
			final File source = path.toFile();
			
			String extension = path.getFileExtension();
			path = path.removeFileExtension();
			
			IPath targetPath = path.addFileExtension(extension + "_processed");
			int matching = targetPath.matchingFirstSegments(file.getProject().getLocation());
			final IFile target = file.getProject().getFile(targetPath.removeFirstSegments(matching));
			
			//process the files (possibly  in parallel) in a workspace job so that the user can do something else.
			WorkspaceJob saveJob = new WorkspaceJob("Generating checklist from file" + file.getName()) {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					long formattingStart = System.currentTimeMillis();

					IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
					internalMonitor.beginTask("Processing " + file.getName(), IProgressMonitor.UNKNOWN);

					ArrayList<Error_Message> errorMessages = new ArrayList<Error_Message>();
					
					//read in the error message from the file
					BufferedReader bufferedInput = null;
					try {
						FileInputStream input = new FileInputStream(source);
						bufferedInput = new BufferedReader(new InputStreamReader(input));
						
						//process the first line to see where the needed columns are
						//the used might have reordered them
						String line;
						Pattern p = Pattern.compile("\t");
						String[] items;
						int descriptionIndex = -1;
						int fileNameIndex = -1;
						int pathIndex = -1;
						int locationIndex = -1;
						line = bufferedInput.readLine();
						if (line != null) {
							items = p.split(line);
							for (int i = 0; i < items.length; i++) {
								if ("Description".equals(items[i])) {
									descriptionIndex = i;
								} else if ("Resource".equals(items[i])) {
									fileNameIndex = i;
								} else if ("Path".equals(items[i])) {
									pathIndex = i;
								} else if ("Location".equals(items[i])) {
									locationIndex = i;
								}
							}
						}
						
						if (descriptionIndex == -1 || fileNameIndex == -1 || pathIndex == -1 || locationIndex == -1) {
							//FATAL error
							return new Status(IStatus.ERROR, ProductConstants.PRODUCT_ID_DESIGNER, IStatus.OK, "The input must have at least the Description, Resource, Path and Location columns", null);
						}
						
						//process the rest of the file.
						Matcher locationFormatMatcher = LOCATION_FORMAT_PATTERN.matcher("");
						String rawLocation;
						line = bufferedInput.readLine();
						while (line != null) {
							items = p.split(line);
							rawLocation = items[locationIndex];
							if (locationFormatMatcher.reset(rawLocation).matches()) {
								String text = items[descriptionIndex];
								StringBuilder builder = new StringBuilder(text);
								builder = handleEscapeSequences(builder);
								errorMessages.add(new Error_Message(builder.toString(), new Integer(locationFormatMatcher.group(1)).intValue() , items[fileNameIndex], items[pathIndex]));
							}
							line = bufferedInput.readLine();
						}
						
						bufferedInput.close();
					} catch (FileNotFoundException e) {
						ErrorReporter.logExceptionStackTrace(e);
					} catch (IOException e) {
						ErrorReporter.logExceptionStackTrace(e);
						errorMessages.clear();
					} finally {
						if (bufferedInput != null) {
							try {
								bufferedInput.close();
							} catch (IOException e) {
								ErrorReporter.logExceptionStackTrace(e);
							}
						}
					}
					
					if (errorMessages.size() == 0) {
						return new Status(IStatus.ERROR, ProductConstants.PRODUCT_ID_DESIGNER, IStatus.OK, "No error messages found in the file", null);
					}
					
					errorMessages.trimToSize();
					//sort the error messages
					Collections.sort(errorMessages, new Comparator<Error_Message>() {

						@Override
						public int compare(final Error_Message o1, final Error_Message o2) {
							int order = o1.filename.compareTo(o2.filename);
							if (order != 0) {
								return order;
							}

							return o1.line - o2.line;
						}
						
					});
					
					StringBuilder codeSectionBuilder = new StringBuilder(/*errorMessages.size()*50*/);
					int inside_row = 1;
					int followings = 0;
					Error_Message sectionStarterError = errorMessages.get(0);
					Error_Message actualError;
					
					ArrayList<TestSuiteElement> testSuiteElements = new ArrayList<TestSuiteElement>();
					int last_row = sectionStarterError.line;
					
					testSuiteElements.add(new TestSuiteElement(sectionStarterError.filename, sectionStarterError.location.substring(sectionStarterError.location.indexOf('/', 1) + 1)));
					codeSectionBuilder.append("\tprivate ArrayList<MarkerToCheck> " + sectionStarterError.filename.replace('.', '_') + "_initializer() {\n");
					codeSectionBuilder.append("\t\t//" + sectionStarterError.filename + "\n");
					codeSectionBuilder.append("\t\tArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();\n");
					codeSectionBuilder.append("\t\tint lineNum = " + sectionStarterError.line + ";\n");
					AtomicInteger printI = new AtomicInteger(1);
					
					//process the error message one-by-one, detecting error sections, and build their representation in a StringBuilder.
					for (int i = 1, size = errorMessages.size(); i < size; i++) {
						actualError = errorMessages.get(i);
						
						if (sectionStarterError.filename.equals(actualError.filename)
								&& sectionStarterError.text.equals(actualError.text)) {
							if (sectionStarterError.line == actualError.line) {
								inside_row++;
								continue;
							 } else if (sectionStarterError.line + followings + 1 == actualError.line
									 && inside_row == 1) {
								followings++;
								continue;
							}
						}
						
						printErrorSection(codeSectionBuilder, sectionStarterError, inside_row, followings, last_row, printI);
						last_row = sectionStarterError.line;
						if (followings > 0) {
							last_row += followings + 1;
						}
						
						if (!sectionStarterError.filename.equals(actualError.filename)) {
							last_row = actualError.line;
							
							testSuiteElements.add(new TestSuiteElement(actualError.filename, actualError.location.substring(actualError.location.indexOf('/', 1) + 1)));
							codeSectionBuilder.append("\n");
							codeSectionBuilder.append("\t\treturn markersToCheck;\n");
							codeSectionBuilder.append("\t}\n\n");
							codeSectionBuilder.append("\t private ArrayList<MarkerToCheck> " + actualError.filename.replace('.', '_') + "_initializer() {\n");
							codeSectionBuilder.append("\t\t//" + actualError.filename + "\n");
							codeSectionBuilder.append("\t\tArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();\n");
							codeSectionBuilder.append("\t\tint lineNum = " + actualError.line + ";\n");
							printI.set(1);
						}
						
						sectionStarterError = actualError;
						inside_row = 1;
						followings = 0;
					}
					
					printErrorSection(codeSectionBuilder, sectionStarterError, inside_row, followings, last_row, printI);
					codeSectionBuilder.append("\n");
					codeSectionBuilder.append("\t\treturn markersToCheck;\n");
					codeSectionBuilder.append("\t}\n\n");

					//create a write that will write the output to a file
					BufferedWriter bufferedwriter = null;
					try {
						bufferedwriter = new BufferedWriter(new FileWriter(target.getLocation().toFile()));
					} catch (IOException e) {
						ErrorReporter.logExceptionStackTrace(e);
						return new Status(IStatus.ERROR, ProductConstants.PRODUCT_ID_DESIGNER, IStatus.OK, e.getMessage() != null ? e.getMessage() : "", e);
					}
					
					PrintWriter printWriter = new PrintWriter(bufferedwriter);
					
					TestSuiteElement element;
					for (int i = 0; i < testSuiteElements.size(); i++) {
						element = testSuiteElements.get(i);
						
						printWriter.println("\t//" + element.fileName.replace('.', '_'));
					}
					
					printWriter.println("\t");
//					printWriter.println("\tstatic {");
/*					for (int i = 0; i < testSuiteElements.size(); i++) {
						element = testSuiteElements.get(i);
						
						printWriter.println("\t\t" + element.fileName.replace('.', '_') + "_initializer(); //" + element.fileName.replace('.', '_'));
					}*/
/*					
					printWriter.println("\t\tint i = 0;");
					printWriter.println("\t\tint lineNum = 0;");
					printWriter.println("");
*/					
//					printWriter.println("\t}\n\n");

					printWriter.print(codeSectionBuilder);
					
					printWriter.println("");
					printWriter.println("\t/*");
					printWriter.println("\t* Auto-generated method stub");
					printWriter.println("\t* @param name The name of the test package");
					printWriter.println("\t*/");
					printWriter.println("\tpublic FILL_ME_OUT_contsructor(final String name) {");
					printWriter.println("\t\tsuper(name);");
					printWriter.println("\t}");
					printWriter.println("");
					
					// test suite code
					printWriter.println("\t/*");
					printWriter.println("\t* Auto-generated method stub");
					printWriter.println("\t*/");
					printWriter.println("\tpublic static Test suite() {");
					printWriter.println("\t\tTestSuite suite = new TestSuite(\"FILL_ME_OUT tests\"); //FIXME correct");
					printWriter.println("");
					
					// test suite elements
					
					for (int i = 0; i < testSuiteElements.size(); i++) {
						element = testSuiteElements.get(i);
						
						printWriter.println("\t\tsuite.addTest(new FILL_ME_OUT_contsructor(\"" + element.fileName.replace('.', '_') + "\"));");
					}
					
					printWriter.println("");
					printWriter.println("\t\tTestSetup wrapper = new TestSetup(suite) {");
					printWriter.println("");
					printWriter.println("\t\t\t@Override");
					printWriter.println("\t\t\tprotected void setUp() throws Exception {");
					printWriter.println("\t\t\t}");
					printWriter.println("");
					printWriter.println("\t\t};");
					printWriter.println("");
					printWriter.println("\treturn wrapper;");
					printWriter.println("\t}");
						
					for (int i = 0; i < testSuiteElements.size(); i++) {
						element = testSuiteElements.get(i);

						printWriter.println("");
						printWriter.println("\t// Auto-generated method stub");
						printWriter.println("\tpublic void " + element.fileName.replace('.', '_') + "() throws Exception {");
						printWriter.println("\t\tArrayList<MarkerToCheck> markersToCheck = " + element.fileName.replace('.', '_') + "_initializer();");
						printWriter.println("\t\tIProject project = WorkspaceHandlingLibrary.getWorkspace().getRoot().getProject(\"Regression_test_project\");");
						printWriter.println("");
						printWriter.println("\t\tArrayList<Map<?, ?>> fileMarkerlist = Designer_plugin_tests.semanticMarkers.get(project.getFile(\"" + element.fileLocation + "/" + element.fileName + "\"));");
						printWriter.println("");
						printWriter.println("\t\tassertNotNull(fileMarkerlist);");
						printWriter.println("");
						printWriter.println("\t\tfor (int i = markersToCheck.size() - 1; i >= 0; i--) {");
						printWriter.println("\t\t\tMarkerHandlingLibrary.searchNDestroyFittingMarker(fileMarkerlist, markersToCheck.get(i).getMarkerMap(), true);");
						printWriter.println("\t\t}");
						printWriter.println("");
						printWriter.println("\t\tmarkersToCheck.clear();");
						printWriter.println("\t}");
					}
					
					printWriter.close();
					
					try {
						target.refreshLocal(IResource.DEPTH_ZERO, null);
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
					TITANDebugConsole.println("Processing took " + (System.currentTimeMillis() - formattingStart) / 1000.0 + " secs");

					internalMonitor.done();
					return Status.OK_STATUS;
				}
			};
			
			IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
			ISchedulingRule rule1 = ruleFactory.createRule(file);
			ISchedulingRule rule2 = ruleFactory.createRule(target);
			ISchedulingRule combinedRule = MultiRule.combine(rule1, null);
			combinedRule = MultiRule.combine(rule2, combinedRule);
			saveJob.setRule(combinedRule);
			saveJob.setPriority(Job.LONG);
			saveJob.setUser(true);
			saveJob.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
			saveJob.schedule();
		}
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structSelection = (IStructuredSelection) selection;

			files = new ArrayList<IFile>(structSelection.size());
			
			for (Object selected : structSelection.toList()) {
				if (selected instanceof IFile) {
					IFile file = (IFile) selected;
					if (file.isAccessible()) {
						files.add(file);
					}
				}
			}
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event)
		        .getActivePage().getSelection();
		selectionChanged(null,selection);
		run(null);
		
		return null;
	}
}
