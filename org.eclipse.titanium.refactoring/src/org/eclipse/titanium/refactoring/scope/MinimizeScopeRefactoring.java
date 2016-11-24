/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;

/**
 * This class represents the 'Minimize scope of local variables' refactoring operation.
 * 
 * @author Viktor Varga
 */
public class MinimizeScopeRefactoring extends Refactoring {

	private final IStructuredSelection fileSelection;	//not null only if selection is one or multiple resource(s)
	private final Definition defSelection;				//not null only if selection is a single definition
	
	private final Settings settings;
	
	private Object[] affectedObjects;		//the list of objects affected by the change
	
	/*
	 * TODO 
	 * 	check javadoc
	 *  remove unused code from the package
	 *  
	 *  use Utils.createLocationString()
	 * 
	 * 
	 * */

	/** Use this constructor when the selection is a set of files, folders, or projects. */
	public MinimizeScopeRefactoring(IStructuredSelection selection, Settings settings) {
		this.defSelection = null;
		this.fileSelection = selection;
		if (settings == null) {
			this.settings = new Settings();
		} else {
			this.settings = settings;
		}
	}
	/** Use this constructor when the selection is a part of a single file. */
	public MinimizeScopeRefactoring(Definition selection, Settings settings) {
		this.defSelection = selection;
		this.fileSelection = null;
		if (settings == null) {
			this.settings = new Settings();
		} else {
			this.settings = settings;
		}
	}
	
	public Settings getSettings() {
		return settings;
	}
	Object[] getAffectedObjects() {
		return affectedObjects;
	}

	//METHODS FROM REFACTORING

	@Override
	public String getName() {
		return "Minimize scope";
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		return result;
	}
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		return result;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		if (fileSelection != null) {
			//resource(s) selected
			CompositeChange cchange = new CompositeChange("MinimizeScopeRefactoring");
			Iterator it = fileSelection.iterator();
			while (it.hasNext()) {
				Object o = it.next();
				if (!(o instanceof IResource)) {
					continue;
				}
				IResource res = (IResource)o;
				ResourceVisitor vis = new ResourceVisitor(cchange);
				res.accept(vis);
			}
			this.affectedObjects = cchange.getAffectedObjects();
			return cchange;
		} else {
			//a single definition selected
			CompositeChange cchange = new CompositeChange("MinimizeScopeRefactoring");
			IResource file = defSelection.getLocation().getFile();
			if (!(file instanceof IFile)) {
				ErrorReporter.logError("MinimizeScopeRefactoring.createChange(): File container of defSelection is not an IFile! ");
			}
			ChangeCreator chCreator = new ChangeCreator((IFile)file, defSelection, settings);
			chCreator.perform();
			Change ch = chCreator.getChange();
			if (ch != null) {
				cchange.add(ch);
				this.affectedObjects = ch.getAffectedObjects();
			} else {
				this.affectedObjects = new Object[]{};
			}
			return cchange;
		}
	}

	//METHODS FROM REFACTORING END

	/**
	 * Visits all the files of a folder or project (any {@link IResource}).
	 * Creates the {@link Change} for all files and then merges them into a single
	 * {@link CompositeChange}.
	 * <p>
	 * Call on any {@link IResource} object.
	 *  */
	private class ResourceVisitor implements IResourceVisitor {

		private final CompositeChange change;
		
		public ResourceVisitor(CompositeChange change) {
			this.change = change;
		}
		
		CompositeChange getChange() {
			return change;
		}
		
		@Override
		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				ChangeCreator chCreator = new ChangeCreator((IFile)resource, settings);
				chCreator.perform();
				Change ch = chCreator.getChange();
				if (ch != null) {
					change.add(ch);
				}
				//SKIP
				return false;
			}
			//CONTINUE
			return true;
		}
		
	}

	/** 
	 * This class contains the settings configuration for a refactoring operation.
	 * */
	public static class Settings {
		
		/** Enable moving variables at all (can be false if the refactoring is used for removing unused variables). */
		public static final int MOVE_VARS = 0x1;
		/** Enable moving variables when their scope is correct. */
		public static final int MOVE_VARS_IN_CORRECT_SCOPE = 0x2;
		/** Enable removing unused variables. */
		public static final int REMOVE_UNUSED_VARS = 0x4;
		/** 
		 * Avoid moving or removing declarations when there is a function call in them.
		 * <p>
		 * Refactoring the code while disabling this option might change the refactored code behaviour.
		 * */
		public static final int AVOID_MOVING_WHEN_FUNCCALL = 0x8;
		/** 
		 * Avoid moving declarations when there is an unchecked reference in the declaration stmt.
		 * <p>
		 * Some of the variables referred in the declaration stmt are not checked for write occurrences
		 * at the moment. Refactoring the code while disabling this option may lead to variable declarations
		 * moved past these write occurrences of the referred variables. This might change the refactored
		 * code behaviour. 
		 *  */
		public static final int AVOID_MOVING_WHEN_UNCHECKED_REF = 0x10;
		/** Avoid moving and/or taking apart declaration lists (unused variables can still be removed from it). */
		public static final int AVOID_MOVING_MULTIDECLARATIONS = 0x20;
		
		private int settings = 0;
		
		public Settings() {
			createDefaultSettings();
		}
		
		private void createDefaultSettings() {
			setSetting(MOVE_VARS, true);
			setSetting(MOVE_VARS_IN_CORRECT_SCOPE, true);
			setSetting(REMOVE_UNUSED_VARS, false);
			setSetting(AVOID_MOVING_WHEN_FUNCCALL, true);
			setSetting(AVOID_MOVING_WHEN_UNCHECKED_REF, true);
			setSetting(AVOID_MOVING_MULTIDECLARATIONS, false);
		}
		
		public boolean getSetting(int setting) {
			return (settings & setting) == setting;
		}
		public void setSetting(int setting, boolean value) {
			boolean prevVal = getSetting(setting);
			if (prevVal == value) {
				return;
			}
			if (value) {
				settings += setting;
			} else {
				settings -= setting;
			}
		}
		
	}
	
	
}
