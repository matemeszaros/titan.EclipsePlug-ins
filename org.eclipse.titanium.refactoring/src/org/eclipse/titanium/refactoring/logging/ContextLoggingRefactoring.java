/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * This class represents the 'Context logging' refactoring operation.
 * <p>
 * This refactoring operation adds context info to existing log statements, if necessary.
 * The user can specify settings for the operation in the wizard dialog.
 * The operation can be executed using the mechanisms in the superclass, through a wizard for example.
 * 
 * @author Viktor Varga
 */
public class ContextLoggingRefactoring extends Refactoring {

	private final IFile selectedFile;	//not null only if selection is of TextSelection
	private final ISelection selection;
	
	private final Settings settings;
	
	private Object[] affectedObjects;		//the list of objects affected by the change
	

	/*
	 * TODO dev:
	 *  
	 *  what about expressions like lengthof(...)?
	 *  wizard: param count limit input field, link it to Context.getVarCountLimitOption()
	 *  local variable contexts
	 * 
	 * */
	
	/** Use this constructor when the selection is a set of files, folders, or projects. */
	public ContextLoggingRefactoring(IStructuredSelection selection, Settings settings) {
		this.selectedFile = null;
		this.selection = selection;
		if (settings == null) {
			this.settings = new Settings();
		} else {
			this.settings = settings;
		}
	}
	/** Use this constructor when the selection is a part of a single file. */
	public ContextLoggingRefactoring(IFile selectedFile, ITextSelection selection, Settings settings) {
		this.selectedFile = selectedFile;
		this.selection = selection;
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
		return "Add context info to log statements";
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
		if (selection == null) {
			ErrorReporter.logError("ContextLoggingRefactoring: Selection is null! ");
			return null;
		}
		if (selection instanceof IStructuredSelection) {
			CompositeChange cchange = new CompositeChange("ContextLoggingRefactoring");
			IStructuredSelection ssel = (IStructuredSelection)selection;
			Iterator<?> it = ssel.iterator();
			while (it.hasNext()) {
				Object o = it.next();
				if (!(o instanceof IResource)) {
					continue;
				}
				IResource res = (IResource)o;
				ResourceVisitor vis = new ResourceVisitor(cchange);
				res.accept(vis);
			}
			affectedObjects = cchange.getAffectedObjects();
			return cchange;
		} else if (selection instanceof TextSelection) {
			ChangeCreator chCreator = new ChangeCreator(selectedFile, (TextSelection)selection, settings);
			chCreator.perform();
			Change ch = chCreator.getChange();
			if(ch == null) {
				affectedObjects = new Object[]{};
				return new CompositeChange("EmptyLoggingRefactoring");
			} else {
				affectedObjects = ch.getAffectedObjects();
			}
			return ch;
		}
		return null;
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
		
		/** log function parameters. */
		public static final int SETTING_LOG_FUNCPAR = 0x1;
		/** log varaibles in if condition */
		public static final int SETTING_LOG_IF = 0x2;
		/** log loop varaibles */
		public static final int SETTING_LOG_LOOP = 0x4;
		/** log all local variables before the log statement */
		public static final int SETTING_LOG_LOCAL_VARS = 0x8;
		/** log local variables before the log statement from the direct parent block only */
		public static final int SETTING_LOG_LOCAL_VARS_PARENT_BLOCK_ONLY = 0x10;
		/** modify log statements which already log variables */
		public static final int SETTING_MODIFY_LOG_STATEMENTS = 0x20;
		
		private int settings = 0;
		
		/** the maximum number of variables in log statements (including the ones
		 *  in them before the refactoring) */
		private int countLimit;
		
		public Settings() {
			createDefaultSettings();
		}
		
		private void createDefaultSettings() {
			setSetting(SETTING_LOG_FUNCPAR, true);
			setSetting(SETTING_LOG_IF, true);
			setSetting(SETTING_LOG_LOCAL_VARS, false);
			setSetting(SETTING_LOG_LOCAL_VARS_PARENT_BLOCK_ONLY, false);
			setSetting(SETTING_LOG_LOOP, true);
			setSetting(SETTING_MODIFY_LOG_STATEMENTS, true);
			countLimit = 8;
		}
		
		public int getCountLimit() {
			return countLimit;
		}
		public void setCountLimit(int countLimit) {
			this.countLimit = countLimit;
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
