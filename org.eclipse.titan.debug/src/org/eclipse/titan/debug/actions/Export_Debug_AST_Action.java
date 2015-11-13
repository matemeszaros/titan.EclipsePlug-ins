/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.debug.actions;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class Export_Debug_AST_Action extends Action implements IObjectActionDelegate {
	private ISelection selection;
	StringBuilder paddingBuffer = new StringBuilder();
	final byte[] lineend = {'\r','\n'};

	@Override
	public void run(IAction action) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		IStructuredSelection structSelection = (IStructuredSelection) selection;
		List<IProject> projects = new ArrayList<IProject>();

		for (Object selected : structSelection.toList()) {
			if (!(selected instanceof IProject)) {
				continue;
			}

			projects.add((IProject) selected);
		}

		for (IProject project: projects) {
			Shell shell = Display.getCurrent().getActiveShell();
			FileDialog d = new FileDialog(shell, SWT.SAVE);
			d.setText("Export debug information of the AST");
			d.setFilterExtensions(new String[] {"*.zip"});
			d.setFilterPath(project.getLocation().toOSString());
			d.setFileName("Debug_AST.zip");
			String zipFilename = d.open();

			try {
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilename));
				ProjectSourceParser parser = GlobalParser.getProjectSourceParser(project);
	
				Set<String> names = parser.getKnownModuleNames();
				for (String name : names) {
					Module module = parser.getModuleByName(name);
					exportDebugAST(out, module);
					
				}
				
				out.close();
			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		
		
	}

	private void exportDebugAST(final ZipOutputStream out, final Module module) throws Exception {
		out.putNextEntry(new ZipEntry("DebugAST_for_" + module.getIdentifier().getName() + ".txt"));
		out.write("*************************".getBytes());
		out.write(lineend);
		out.write(("Printing DEBUG information for module `" + module.getName() + "':").getBytes());
		out.write(lineend);
		out.write("*************************".getBytes());
		out.write(lineend);

		module.accept(new ASTVisitor() {
			private int padding = 0;
			@Override
			public int visit(IVisitableNode node) {
				if (node instanceof Assignment) {
					Assignment assignment = (Assignment) node;
					printInfoln(out, padding, assignment.getAssignmentName(), assignment.getFullName(), assignment.getLastTimeChecked(), assignment.getLocation());
				} else if (node instanceof Identifier) {
					printInfoln(out, padding, "identifier", ((Identifier) node).getDisplayName(), null, ((Identifier) node).getLocation());
				} else if (node instanceof Statement) {
					Statement statement = (Statement) node;
					printInfoln(out, padding, "statement", statement.getFullName(), statement.getLastTimeChecked(), statement.getLocation());
				} else if (node instanceof Reference) {
					Reference ref = (Reference) node;
					printInfoln(out, padding, "reference", ref.getFullName(), ref.getLastTimeChecked(), ref.getLocation());
					Assignment old = ref.getAssOld();
					if (old != null) {
						printInfoln(out, padding + 1, "This reference was last pointing to " + old.getFullName() + " analyzed at " + old.getLastTimeChecked());
					}
				} else if (node instanceof ComponentTypeBody) {
					ComponentTypeBody body = (ComponentTypeBody) node;
					Map<String, Definition> map = body.getDefinitionMap();
					printInfoln(out, padding + 1, " contains definitions:");
					if (map != null) {
						for (Map.Entry<String, Definition> entry: map.entrySet()) {
							printInfoln(out, padding + 2, entry.getKey() + " was last checked at " + entry.getValue().getLastTimeChecked());
						}
					}
				}

				if (node instanceof StatementBlock || node instanceof Definition) {
					padding++;
				}

				return super.visit(node);
			}
			@Override
			public int leave(IVisitableNode node) {
				if (node instanceof StatementBlock || node instanceof Definition) {
					padding--;
				}

				return super.leave(node);
			}
			
		});

		out.write("*************************".getBytes());
		out.write(lineend);
		out.write(("Printing DEBUG information for module `" + module.getName() + "' finished").getBytes());
		out.write(lineend);
		out.write("*************************".getBytes());
		out.write(lineend);

		out.closeEntry();
	}
	
	private void printInfoln(final ZipOutputStream out, int padding, String text) {
		while (paddingBuffer.length() < padding * 2) {
			paddingBuffer.append("  ");
		}

		try {
			out.write(paddingBuffer.substring(0, padding * 2).getBytes());
			out.write(text.getBytes());
			out.write(lineend);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	private void printInfoln(final ZipOutputStream out, int padding, String kind, String fullname, CompilationTimeStamp timestamp, Location location) {
		while (paddingBuffer.length() < padding * 2) {
			paddingBuffer.append("  ");
		}
		
		try {
	//		MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
			StringBuilder builder = new StringBuilder();
	//		stream.print(paddingBuffer.substring(0, padding * 2));
			builder.append(paddingBuffer.substring(0, padding * 2));
			builder.append(kind + " " + fullname);
	//		stream.print(kind + " " + fullname);
			if (timestamp != null) {
				builder.append(" last checked at " + timestamp);
			}
			if (location instanceof NULL_Location) {
				builder.append(" is located at null location");
			} else {
				builder.append(" is located at line " + location.getLine() + " between " + location.getOffset() + " - " + location.getEndOffset());
			}
	
			out.write(builder.toString().getBytes());
			out.write(lineend);
		
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
