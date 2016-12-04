/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.visibility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ExternalConst;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.VisibilityModifier;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * This class is only instantiated by the {@link MinimizeVisibilityRefactoring} once per each refactoring operation.
 * <p>
 * By passing the selection through the constructor and calling {@link ChangeCreator#perform()}, this class
 *  creates a {@link Change} object, which can be returned by the standard
 *  {@link Refactoring#createChange(IProgressMonitor)} method in the refactoring class.
 *
 * @author Viktor Varga
 */
class ChangeCreator {

	//in
	private final IFile selectedFile;

	//out
	private Change change;

	ChangeCreator(final IFile selectedFile) {
		this.selectedFile = selectedFile;
	}

	public Change getChange() {
		return change;
	}

	/**
	 * Creates the {@link #change} object, which contains all the inserted and edited visibility modifiers
	 * in the selected resources.
	 * */
	public void perform() {
		if (selectedFile == null) {
			return;
		}
		change = createFileChange(selectedFile);
	}

	private Change createFileChange(final IFile toVisit) {
		if (toVisit == null) {
			return null;
		}
		final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(toVisit.getProject());
		final Module module = sourceParser.containedModule(toVisit);
		if(module == null) {
			return null;
		}
		//find all locations in the module that should be edited
		final DefinitionVisitor vis = new DefinitionVisitor();
		module.accept(vis);
		final NavigableSet<ILocateableNode> nodes = vis.getLocations();

		if (nodes.isEmpty()) {
			return null;
		}
		//calculate edit locations
		final List<Location> locations = new ArrayList<Location>();
		try {
			final WorkspaceJob job1 = calculateEditLocations(nodes, toVisit, locations);
			job1.join();
		} catch (InterruptedException ie) {
			ErrorReporter.logExceptionStackTrace(ie);
		} catch (CoreException ce) {
			ErrorReporter.logError("MinimizeVisibilityRefactoring/CreateChange.createFileChange(): "
					+ "CoreException while calculating edit locations. ");
			ErrorReporter.logExceptionStackTrace(ce);
		}
		if (locations.isEmpty()) {
			return null;
		}
		//create a change for each edit location
		final TextFileChange tfc = new TextFileChange(toVisit.getName(), toVisit);
		final MultiTextEdit rootEdit = new MultiTextEdit();
		tfc.setEdit(rootEdit);
		for (Location l: locations) {
			final int len = l.getEndOffset()-l.getOffset();
			if (len == 0) {
				rootEdit.addChild(new InsertEdit(l.getOffset(), "private "));
			} else {
				rootEdit.addChild(new ReplaceEdit(l.getOffset(), len, "private "));
			}
		}
		return tfc;
	}

	private WorkspaceJob calculateEditLocations(final NavigableSet<ILocateableNode> nodes, final IFile file
			, final List<Location> locations_out) throws CoreException {
		final WorkspaceJob job = new WorkspaceJob("MinimizeVisibilityRefactoring: calculate edit locations") {

			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				final int BUF_LEN = 8;
				try {
					InputStream is = file.getContents();
					InputStreamReader isr = new InputStreamReader(is);
					int currOffset = 0;
					char[] content = new char[BUF_LEN];
					for (ILocateableNode node : nodes) {
						int toSkip = node.getLocation().getOffset()-currOffset;
						if (toSkip < 0) {
							ErrorReporter.logError("MinimizeVisibilityRefactoring.ChangeCreator: Negative skip value" +
									" while parsing file: " + file.getName() + ", offset: " +
									node.getLocation().getOffset() + "->" + currOffset);
							continue;
						}
						isr.skip(toSkip);
						isr.read(content);
						String str = new String(content);
						VisibilityModifier vm = parseVisibilityModifier(str);
						int vmLen = 0;
						if (vm != null) {
							switch (vm) {
								case Public:
									vmLen = 7;
									break;
								case Friend:
									vmLen = 7;
									break;
								case Private:
									vmLen = 8;
									break;
							}
						}
						locations_out.add(new Location(node.getLocation().getFile(), node.getLocation().getLine(),
								node.getLocation().getOffset(), node.getLocation().getOffset()+vmLen));

						currOffset = node.getLocation().getOffset()+BUF_LEN;
					}
					isr.close();
					is.close();
				} catch (IOException ioe) {
					ErrorReporter.logError("MinimizeVisibilityRefactoring.CreateChange: " +
							"Error while reading source project.");
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return job;
	}

	/**
	 * Collects the locations of all the definitions in a module where the visibility modifier
	 *  is not yet minimal.
	 * <p>
	 * Call on modules.
	 * */
	private static class DefinitionVisitor extends ASTVisitor {

		private final NavigableSet<ILocateableNode> locations;

		DefinitionVisitor() {
			locations = new TreeSet<ILocateableNode>(new LocationComparator());
		}

		private NavigableSet<ILocateableNode> getLocations() {
			return locations;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Definition && isGoodType(node)) {
				final Definition d = (Definition)node;
				if (!d.isLocal() && !VisibilityModifier.Private.equals(d.getVisibilityModifier()) &&
						hasValidLocation(d)) {
					final String moduleName = d.getMyScope().getModuleScope().getName();
					if (!d.isUsed()) {
						locations.add(d);
					} else if (d.referingHere.size() == 1 && d.referingHere.get(0).equals(moduleName)) {
						locations.add(d);
					}
				}
			}
			return V_CONTINUE;
		}

		private boolean isGoodType(final IVisitableNode node) {
			if (node instanceof Def_Altstep ||
					node instanceof Def_Const ||
					node instanceof Def_ExternalConst ||
					node instanceof Def_Extfunction ||
					node instanceof Def_Function ||
					node instanceof Def_ModulePar ||
					node instanceof Def_Template ||
					node instanceof Def_Type) {
				return true;
			}
			return false;
		}

		private boolean hasValidLocation(final Definition def) {
			if (def.getLocation() == null) {
				return false;
			}
			if (def.getLocation().getOffset() < 0 || def.getLocation().getEndOffset() < 0) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Compares {@link ILocateableNode}s by comparing the file paths as strings.
	 * If the paths are equal, the two offset integers are compared.
	 * */
	private static class LocationComparator implements Comparator<ILocateableNode> {

		@Override
		public int compare(final ILocateableNode arg0, final ILocateableNode arg1) {
			final IResource f0 = arg0.getLocation().getFile();
			final IResource f1 = arg1.getLocation().getFile();
			if (!f0.equals(f1)) {
				return f0.getFullPath().toString().compareTo(f1.getFullPath().toString());
			}
			final int o0 = arg0.getLocation().getOffset();
			final int o1 = arg1.getLocation().getOffset();
			return (o0 < o1) ? -1 : ((o0 == o1) ? 0 : 1);//TODO update with Java 1.7 to Integer.compare
		}

	}

	private static VisibilityModifier parseVisibilityModifier(final String str) {
		if (str.contains("public ")) {
			return VisibilityModifier.Public;
		}
		if (str.contains("friend ")) {
			return VisibilityModifier.Friend;
		}
		if (str.contains("private ")) {
			return VisibilityModifier.Private;
		}
		return null;
	}

}
