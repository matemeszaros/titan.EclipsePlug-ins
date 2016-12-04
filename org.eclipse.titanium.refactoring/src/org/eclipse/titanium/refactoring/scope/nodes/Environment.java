/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;
import org.eclipse.titanium.refactoring.Utils;
import org.eclipse.titanium.refactoring.scope.MinimizeScopeRefactoring.Settings;

/**
 * The main class contatining the simplified representation of the AST.
 * 
 * @author Viktor Varga
 */
public class Environment {
	
	private final Settings settings;

	private BlockNode rootNode;	//the block of the function or testcase

	private final List<Variable> vars;
	
	public Environment(final Settings settings) {
		vars = new ArrayList<Variable>();
		this.settings = settings;
	}
	
	public BlockNode getRootNode() {
		return rootNode;
	}
	public List<Variable> getVars() {
		return vars;
	}
	
	public void setRootNode(final BlockNode rootNode) {
		this.rootNode = rootNode;
	}
	public void addVariable(final Variable var) {
		this.vars.add(var);
	}
	
	//queries
	
	public Variable getVariable(final Assignment as) {
		for (Variable var: vars) {
			if (var.assmnt.equals(as)) {
				return var;
			}
		}
		return null;
	}
	
	//refactoring
	
	public List<Edit> refactor() {
		List<Edit> edits = new ArrayList<Edit>();
		ListIterator<Variable> it = vars.listIterator(vars.size());
		while (it.hasPrevious()) {
			Variable var = it.previous();
			if (var.isParameter) {
				continue;
			}
			Edit e = refactorVar(var);
			if (e != null) {
				edits.add(e);
			}
		}
		return edits;
	}
	
	private Edit refactorVar(final Variable var) {
		StatementNode declSt = var.getDeclaration();
		// if "avoid moving declarations with function calls" setting is enables, skip variable
		if (settings.getSetting(Settings.AVOID_MOVING_WHEN_FUNCCALL) && declSt.hasFunctionCall()) {
			return null;
		}
		// remove unused
		if (settings.getSetting(Settings.REMOVE_UNUSED_VARS) && var.getReferences().isEmpty()) {
			//remove declaration from tree
			if (!declSt.getParent().getStatements().remove(declSt)) {
				ErrorReporter.logError("Environment.refactorVar(): Parent block did not contain the statement to remove! ");
			}
			//remove references to the removed statement
			Set<Variable> refs = declSt.getReferredVars();
			for (Variable v: refs) {
				v.removeReference(declSt);
			}
			return new Edit(declSt, null);	//remove edit
		} else if (var.getReferences().isEmpty()) {
			return null;
		}
		if (!settings.getSetting(Settings.MOVE_VARS)) {
			return null;
		}
		//
		if (settings.getSetting(Settings.AVOID_MOVING_MULTIDECLARATIONS) &&
				var.getDeclaration().isMultiDeclaration()) {
			return null;
		}
		//calculate smallest common scope for the references of the variable
		BlockNode currScope = declSt.getParent();
		BlockNode newScope = findSmallestCommonAncestorBlock(var.references);
		//if new scope is a loop and it is not equal to the current scope, increase the scope
		while (newScope != null && !newScope.equals(currScope) && isLoopScope(newScope)) {
			newScope = newScope.getParent() == null ? null : newScope.getParent().getParent();
		}
		if (newScope == null) {
			return null;
		}
		if (!settings.getSetting(Settings.MOVE_VARS_IN_CORRECT_SCOPE) && newScope.equals(currScope)) {
			return null;
		}
		//does the declaration contain any unchecked references?
		if (declSt.hasUncheckedRef && settings.getSetting(Settings.AVOID_MOVING_WHEN_UNCHECKED_REF)) {
			return null;
		}
		StatementNode insertionPoint = findInsertionPointInScope(var, currScope, newScope);
		newScope = insertionPoint.getParent();	//the scope could be updated
		//
		if (!settings.getSetting(Settings.MOVE_VARS_IN_CORRECT_SCOPE) && newScope.equals(currScope)) {
			return null;
		}
		//check whether the refactored declaration is already at its desired location (right before insertionPoint)
		int declInd = declSt.getParent().getStatements().indexOf(declSt);
		if (declInd == declSt.getParent().getStatements().size()-1) {
			String errmsg = "variable " + declSt.declaredVar.toString() + ", loc: " + Utils.createLocationString(declSt.astNode);
			ErrorReporter.logError("Environment.refactorVar(): Declaration statement is the last one in the block: " + errmsg);
			return null;
		}
		StatementNode currNextSt = declSt.getParent().getStatements().get(declInd+1);
		if (currNextSt.equals(insertionPoint)) {
			//variable is at its correct location
			return null;
		}
		//moving declaration to in front of the insertion point
		declSt.getParent().getStatements().remove(declSt);
		declSt.setParent(newScope);
		int insertionInd = newScope.getStatements().indexOf(insertionPoint);
		newScope.getStatements().add(insertionInd, declSt);
		declSt.setMoved();
		//update all references to the moved decl st (find their correct positions)
		updateReferencePositions(declSt);
		//create edit
		return new Edit(declSt, insertionPoint);
	}
	
	private BlockNode findSmallestCommonAncestorBlock(final List<Reference> refs) {
		if (refs.isEmpty()) {
			//TODO remove after debugging
			ErrorReporter.logError("Environment.findSmallestCommonScope(): Statements list is empty! ");
			return null;
		}
		//find smallest common ancestor block
		BlockNode currScope = refs.get(0).getRef().parent;
		scopeloop: while (currScope != null) {
			ListIterator<Reference> it = refs.listIterator();
			refloop: while (it.hasNext()) {
				StatementNode currRef = it.next().getRef();
				if (currRef.parent.equals(currScope) || currRef.isBlockAncestorOfThis(currScope)) {
					continue refloop;
				}
				currScope = currScope.parent == null ? null : currScope.parent.parent;
				continue scopeloop;
			}
			return currScope;
		}
		//currScope is null here
		ErrorReporter.logError("Environment.findSmallestCommonAncestorBlock(): No common scope was found. ");
		return currScope;
	}
	
	private StatementNode findInsertionPointInScope(final Variable var, final BlockNode oldScope, final BlockNode newScope) {
		if (var.getReferences().isEmpty()) {
			ErrorReporter.logError("Environment.findInsertionPointInScope(): Parent block did not contain the statement to remove! ");
			return null;
		}
		StatementNode firstRef = var.getReferences().get(0).getRef();
		StatementNode insertionPoint = newScope.findStmtInBlockWhichContainsNode(firstRef);
		//for all variables which are referred in the declaration stmt, find the earliest lhs occurrence (single one for all vars)
		if (!var.declaration.getReferredVars().isEmpty()) {
			StatementNode declSt = var.getDeclaration();
			Set<Variable> refdVars = declSt.getReferredVars();
			Reference firstLeftRref = null;
			for (Variable rvar: refdVars) {
				List<Reference> rrefs = rvar.getReferences();
				int declRefInd = Reference.indexOf(declSt, rrefs);
				if (declRefInd < 0) {
					ErrorReporter.logError("Environment.findInsertionPointInScope(): Ref in other vars decl stmt is not present in the ref list! " +
							"var: " + var + "; rvar: " + rvar + "; loc: " + Utils.createLocationString(newScope.astNode));
					continue;
				}
				//find first lefthand ref of rvar after the declaration stmt of var
				for (int i=declRefInd+1;i<rrefs.size();i++) {
					if (rrefs.get(i).isLeftHandSide()) {
						if (firstLeftRref == null) {
							firstLeftRref = rrefs.get(i);
						} else if (rrefs.get(i).getRef().compareCurrentPositionTo(firstLeftRref.getRef()) < 0) {
							firstLeftRref = rrefs.get(i);
						}
						break;
					}
				}
			}
			//
			if (firstLeftRref == null) {
				return insertionPoint;
			}
			//is the earliest referred var lhs occurrence earlier than the first own ref?
			int result = insertionPoint.compareCurrentPositionTo(firstLeftRref.getRef());
			if (result < 0) {
				return insertionPoint;
			}
			//the insertion point should be changed to the earliest lhs reference of referred vars
			//find the common scope of the lhs ref and the calculated new scope
			BlockNode lhsScope = firstLeftRref.getRef().getParent();
			BlockNode commonScope = newScope.findSmallestCommonAncestorBlock(lhsScope);
			//move up if scope is a loop
			while (commonScope != null && !commonScope.equals(oldScope) && isLoopScope(commonScope)) {
				commonScope = commonScope.getParent() == null ? null : commonScope.getParent().getParent();
			}
			if (commonScope == null) {
				ErrorReporter.logError("Environment.findInsertionPointInScope(): Given new scope of variable and the scope of " +
						" the first lhs ref of a referred var has a null common smallest ancestor block! " +
						"var: " + var + "; loc: " + Utils.createLocationString(newScope.astNode));
			}
			insertionPoint = commonScope.findStmtInBlockWhichContainsNode(firstLeftRref.getRef());
			return insertionPoint;
		} else {
			//return the statement in the scope which contains the first own reference
			return insertionPoint;
		}
	}
	
	/**
	 * Updates the Variable.references lists for each variable referred in the given declaration statement.
	 *  Call this if the given declaration statement was moved and it might refer to any variables.
	 * */
	private void updateReferencePositions(final StatementNode declSt) {
		Set<Variable> refdVars = declSt.getReferredVars();
		for (Variable v: refdVars) {
			List<Reference> refsToV = v.getReferences();
			int refInDeclOldInd = Reference.indexOf(declSt, refsToV);
			boolean refInDeclLhs = refsToV.get(refInDeclOldInd).isLeftHandSide();
			refsToV.remove(refInDeclOldInd);
			int refInDeclNewInd = refsToV.size();
			//check all references in the list whether they are before the insertion point of the moved declSt
			for (int i=refInDeclOldInd;i<refsToV.size();i++) {
				StatementNode currSt = refsToV.get(i).getRef();
				int compareSts = currSt.compareCurrentPositionTo(declSt);
				if (compareSts == 1) {
					refInDeclNewInd = i;
				}
			}
			refsToV.add(refInDeclNewInd, new Reference(declSt, refInDeclLhs));
		}
	}
	
	/**
	 * Returns true if the given BlockNode is a statement block of a loop statement (for, while, alt)
	 * */
	private static boolean isLoopScope(final BlockNode scope) {
		StatementNode parentSN = scope.getParent();
		if (parentSN == null) {
			return false;
		}
		IVisitableNode scopeSt = parentSN.getAstNode();
		if (scopeSt instanceof For_Statement ||
				scopeSt instanceof While_Statement ||
				scopeSt instanceof Alt_Statement) {
			return true;
		}
		return false;
	}
	
	public String toStringRecursive() {
		StringBuilder sb = new StringBuilder();
		sb.append("Env {\n").append("  vars:\n");
		for (Variable var: vars) {
			sb.append(var.toStringRecursive(false, 4)).append("\n");
		}
		sb.append("  TREE:\n");
		if (rootNode != null) {
			sb.append(rootNode.toStringRecursive(true, 4)).append("\n");
		}
		sb.append("}\n");
		return sb.toString();
	}
	
	
}
