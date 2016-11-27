/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.lazyfication;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * This class is only instantiated by the {@link LazyficationRefactoring} once per each refactoring operation.
 * <p>
 * By passing the selection through the constructor and calling {@link ChangeCreator#perform()}, this class
 *  creates a {@link Change} object, which can be returned by the standard
 *  {@link Refactoring#createChange(IProgressMonitor)} method in the refactoring class.
 * 
 * @author Istvan Bohm
 */
class ChangeCreator {

	//in
	private final IFile selectedFile;
	
	//out
	private Change change;
	
	ChangeCreator(IFile selectedFile) {
		this.selectedFile = selectedFile;
	}
	
	Change getChange() {
		return change;
	}
	
	/** 
	 * Creates the {@link #change} object, which contains all the inserted and edited visibility modifiers
	 * in the selected resources.
	 * */
	void perform() {
		if (selectedFile == null) {
			return;
		}
		change = createFileChange(selectedFile);
	}
	
	private Change createFileChange(IFile toVisit) {
		
		if (toVisit == null) {
			return null;
		}
		
		ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(toVisit.getProject());
		Module module = sourceParser.containedModule(toVisit);
		if(module == null) {
			return null;
		}
		
		DefinitionVisitor vis = new DefinitionVisitor();
		module.accept(vis);
		List<FormalParameter> nodes = vis.getLocations();
		
		// Calculate edit locations
		final List<Location> locations = new ArrayList<Location>();
		try {
			WorkspaceJob job1 = calculateEditLocations(nodes, toVisit, locations);
			job1.join();
		} catch (InterruptedException ie) {
			ErrorReporter.logExceptionStackTrace(ie);
		} catch (CoreException ce) {
			ErrorReporter.logError("LazyficationRefactoring: "
					+ "CoreException while calculating edit locations in " + toVisit.getName() + ".");
			ErrorReporter.logExceptionStackTrace(ce);
		}
		if (locations.isEmpty()) {
			return null;
		}
		
		// Create a change for each edit location
		TextFileChange tfc = new TextFileChange(toVisit.getName(), toVisit);
		MultiTextEdit rootEdit = new MultiTextEdit();
		tfc.setEdit(rootEdit);
		
		for (Location l: locations) {
			rootEdit.addChild(new InsertEdit(l.getOffset(), "@lazy "));
		}
		return tfc;
	}
	
	/**
	 * Collects the locations of all the formal parameters in a module where they should be @lazy.
	 * <p>
	 * Call on modules.
	 * */
	private class DefinitionVisitor extends ASTVisitor {
		
		private LazyChecker lazychecker;
		
		DefinitionVisitor() {
			lazychecker = new LazyChecker();
		}
		
		@Override
		public int visit(IVisitableNode node) {
			if (node instanceof Definition && isGoodType(node)) {
				lazychecker.process(node);
			}
			return V_CONTINUE;
		}
		
		private boolean isGoodType(IVisitableNode node) {
			if (node instanceof Def_Altstep || node instanceof Def_Function || node instanceof Def_Testcase) {
				return true;
			}
			return false;
		}
		
		private List<FormalParameter> getLocations() {
			return lazychecker.getLazyParameterList();
		}

	}
	
	
	private WorkspaceJob calculateEditLocations(final List<FormalParameter> fparamlist, final IFile file
			, final List<Location> locations_out) throws CoreException {
		WorkspaceJob job = new WorkspaceJob("LazyficationRefactoring: calculate edit locations") {
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				for (FormalParameter fparam : fparamlist) {
					System.out.println("reading: "+file.getName());
					Location typeloc = fparam.getType(CompilationTimeStamp.getBaseTimestamp()).getLocation();
					locations_out.add(0,new Location(fparam.getLocation().getFile(), fparam.getLocation().getLine(),
							typeloc.getOffset(), typeloc.getOffset()));
				}

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return job;
	}

}