/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.statements.LogArgument;
import org.eclipse.titan.designer.AST.TTCN3.statements.Log_Statement;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;
import org.eclipse.titanium.refactoring.logging.context.Context;

/**
 * This class is only instantiated by the {@link ContextLoggingRefactoring} once 
 *  for each file in the selection.
 * <p>
 * By passing the selection through the constructor and calling {@link ChangeCreator#perform()}, this class
 *  creates a {@link Change} object, which can be then used by the refactoring class.
 * 
 * @author Viktor Varga
 */
class ChangeCreator {
	
	private final Settings settings;
	
	//in
	private final IFile file;
	private final ITextSelection textSelection;	//null if only whole resources are selected
	
	//out
	private TextFileChange change;

	/** Use this constructor when the change should be created for the whole file. */
	ChangeCreator(final IFile file, final Settings settings) {
		this.file = file;
		this.textSelection = null;
		this.settings = settings;
	}
	/** Use this constructor when the change should only be created for a part of the file. */
	ChangeCreator(final IFile file, final ITextSelection textSelection, final Settings settings) {
		this.file = file;
		this.textSelection = textSelection;
		this.settings = settings;
	}

	public Change getChange() {
		return change;
	}
	
	void perform() {
		//get module in selected file
		if (file == null) {
			return;
		}
		ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(file.getProject());
		Module module = sourceParser.containedModule(file);
		if(module == null) {
			return;
		}
		//
		if (textSelection != null) {
			performOnSelectionOnly(module);
		} else {
			performOnWholeModule(module);
		}
		
	}
	
	private void performOnWholeModule(final Module module) {
		ContextFinder vis = new ContextFinder(settings);
		module.accept(vis);
		Map<Log_Statement, Context> res = vis.getResult();
		MultiTextEdit rootEdit = new MultiTextEdit();
		for (Map.Entry<Log_Statement, Context> e: res.entrySet()) {
			TextEdit edit = createTextEdit(e.getKey(), e.getValue());
			if (edit != null) {
				rootEdit.addChild(edit);
			}
		}
		
		if(rootEdit.hasChildren()) {
			change = new TextFileChange("Context logging", file);
			change.setEdit(rootEdit);
		}
	}
	private void performOnSelectionOnly(final Module module) {
		Location selLoc = new Location(file, textSelection.getStartLine(),
				textSelection.getOffset(), textSelection.getOffset()+textSelection.getLength());
		SelectionVisitor vis = new SelectionVisitor(selLoc);
		module.accept(vis);
		Map<Log_Statement, Context> res = vis.getResult();
		MultiTextEdit rootEdit = new MultiTextEdit();
		for (Map.Entry<Log_Statement, Context> e: res.entrySet()) {
			TextEdit edit = createTextEdit(e.getKey(), e.getValue());
			if (edit != null) {
				rootEdit.addChild(edit);
			}
		}
		
		if(rootEdit.hasChildren()) {
			change = new TextFileChange("Context logging", file);
			change.setEdit(rootEdit);
		}
	}
	
	private TextEdit createTextEdit(final Log_Statement toEdit, final Context toAdd) {
		//get insert location
		LogInsertLocationFinder vis = new LogInsertLocationFinder();
		toEdit.accept(vis);
		int insertOffset = vis.calculateEndOffset();
		if (insertOffset < 0) {
			ErrorReporter.logError("ChangeCreator.createTextEdit(): Warning! No arguments in log statement! ");
			insertOffset = toEdit.getLocation().getEndOffset()-1;
		}
		//find variable names that are already present in the log statement
		Set<String> varsAlreadyPresent;
		Context bottomContext = toAdd.getBottom();
		if (bottomContext.getNode() instanceof Log_Statement) {
			LoggedVariableFinder vis2 = new LoggedVariableFinder();
			Log_Statement logst = (Log_Statement)bottomContext.getNode();
			logst.accept(vis2);
			varsAlreadyPresent = vis2.getVarsAlreadyPresent();
		} else {
			varsAlreadyPresent = new HashSet<String>();
		}
		//create inserted text
		toAdd.process();
		List<String> contextInfos = toAdd.createLogParts(varsAlreadyPresent);
		int len = Math.min(contextInfos.size(), toAdd.getVarCountLimitOption());
		if (len == 0) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<len;i++) {
			sb.append(contextInfos.get(i));
		}
		//create edit from location and text
		return new InsertEdit(insertOffset, sb.toString());
	}
	
	/** 
	 * Runs the superclass {@link ContextFinder} on all the
	 * {@link IVisitableNode}s included in the selection.
	 * <p>
	 * Call on modules.
	 * */
	private class SelectionVisitor extends ContextFinder {

		private final Location selection;
		
		SelectionVisitor(final Location selection) {
			super(settings);
			this.selection = selection;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof ILocateableNode) {
				Location loc = ((ILocateableNode)node).getLocation();
				if (loc.getFile() == null) {
					return V_SKIP;
				}
				if (!isOverlapping(loc, selection)) {
					return V_SKIP;
				}
			}
			return super.visit(node);
		}
		
		private boolean isOverlapping(final Location l1, final Location l2) {
			if (!l1.getFile().equals(l2.getFile())) {
				return false;
			}
			return (l1.getOffset() <= l2.getEndOffset() && l1.getEndOffset() >= l2.getOffset());
		}
		
	}
	
	/** 
	 * Locates the insertion point for an existing log statement.
	 * The insertion point is the end location of the last parameter
	 *  of the {@link Log_Statement}}. 
	 * <p>
	 * Call on log statements.
	 *  */
	private static class LogInsertLocationFinder extends ASTVisitor {
		
		private List<LogArgument> args = new ArrayList<LogArgument>();
		
		int calculateEndOffset() {
			int endOffset = -1;
			for (LogArgument la: args) {
				if (la.getLocation().getEndOffset() > endOffset) {
					endOffset = la.getLocation().getEndOffset();
				}
			}
			return endOffset;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof LogArgument) {
				args.add((LogArgument)node);
				return V_SKIP;
			}
			return V_CONTINUE;
		}
		
	}
	
	/** 
	 * Collects the identifier strings of all the variables already present
	 *  in the {@link Log_Statement}.
	 * <p>
	 * Call on log statements. */
	private static class LoggedVariableFinder extends ASTVisitor {
		
		private final Set<String> varsAlreadyPresent = new HashSet<String>();
		
		Set<String> getVarsAlreadyPresent() {
			return varsAlreadyPresent;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Reference) {
				varsAlreadyPresent.add(((Reference)node).getDisplayName());
				return V_SKIP;
			}
			return V_CONTINUE;
		}
		
		
	}
	
}
