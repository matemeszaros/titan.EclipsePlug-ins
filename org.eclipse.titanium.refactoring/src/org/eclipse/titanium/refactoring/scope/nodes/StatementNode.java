/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope.nodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titanium.refactoring.Utils;
import org.eclipse.titanium.refactoring.scope.MinimizeScopeRefactoring;

/**
 * A node representing a Statement.
 * 
 * @author Viktor Varga
 */
public class StatementNode extends Node {

	protected BlockNode parent;
	
	protected final List<BlockNode> blocks;
	protected final Set<Variable> referedVars;
	
	protected Variable declaredVar;
	protected MultiDeclaration multiDeclaration;	//null if this node is not a multi-declaration
	/**
	 * Whether the statement contains a function call.
	 * See {@link MinimizeScopeRefactoring.Settings#AVOID_MOVING_WHEN_FUNCCALL}
	 * */
	protected boolean hasFunctionCall = false;
	/**
	 * Whether the statement contains an unchecked ref.
	 * See {@link MinimizeScopeRefactoring.Settings#AVOID_MOVING_WHEN_UNCHECKED_REF}
	 * */
	protected boolean hasUncheckedRef = false;
	
	protected boolean moved = false;

	public StatementNode(final IVisitableNode astNode) {
		super(astNode);
		this.blocks = new ArrayList<BlockNode>();
		this.referedVars = new HashSet<Variable>();
	}
	
	public BlockNode getParent() {
		return parent;
	}
	public List<BlockNode> getBlocks() {
		return blocks;
	}
	public Set<Variable> getReferredVars() {
		return referedVars;
	}
	public Variable getDeclaredVar() {
		return declaredVar;
	}	
	public MultiDeclaration getMultiDeclaration() {
		return multiDeclaration;
	}
	
	public boolean isLocationEqualTo(final StatementNode sn) {
		if (!(this.getAstNode() instanceof ILocateableNode)) {
			return false;
		}
		
		if (!(sn.getAstNode() instanceof ILocateableNode)) {
			return false;
		}
		
		final Location loc0 = ((ILocateableNode)this.getAstNode()).getLocation();
		final Location loc1 = ((ILocateableNode)sn.getAstNode()).getLocation();
		return loc0.getFile().equals(loc1.getFile()) &&
				loc0.getOffset() == loc1.getOffset() &&
				loc0.getEndOffset() == loc1.getEndOffset();
	}
	public boolean isDeclaration() {
		return declaredVar != null;
	}
	public boolean isMultiDeclaration() {
		return multiDeclaration != null;
	}
	public boolean hasFunctionCall() {
		return hasFunctionCall;
	}
	
	public boolean isMoved() {
		return moved;
	}
	
	public void setParent(final BlockNode parent) {
		this.parent = parent;
	}
	public void addBlock(final BlockNode bl) {
		blocks.add(bl);
	}
	public void addReferedVars(final Variable var) {
		referedVars.add(var);
	}
	public void setDeclaredVar(final Variable declaredVar) {
		if (this.declaredVar != null) {
			ErrorReporter.logError("StatementNode.setDeclaredVar(): A declared variable is already present! ");
		}
		this.declaredVar = declaredVar;
	}
	public void setMultiDeclaration(final MultiDeclaration multiDeclaration) {
		this.multiDeclaration = multiDeclaration;
	}
	public void setHasFunctionCall() {
		hasFunctionCall = true;
	}
	
	public void setHasUncheckedRef() {
		this.hasUncheckedRef = true;
	}
	public void linkWithOtherAsMultiDeclaration(final StatementNode sn) {
		if (this.multiDeclaration != null && sn.multiDeclaration != null) {
			ErrorReporter.logError("StatementNode.linkWithOtherAsMultiDeclaration(): Both nodes are already part of a multi-declaration! ");
			return;
		}
		if (this.multiDeclaration != null) {
			this.multiDeclaration.addDeclarationStatement(sn);
			sn.setMultiDeclaration(this.multiDeclaration);
		} else if (sn.multiDeclaration != null) {
			sn.multiDeclaration.addDeclarationStatement(this);
			this.setMultiDeclaration(sn.multiDeclaration);
		} else {
			final MultiDeclaration md = new MultiDeclaration();
			md.addDeclarationStatement(this);
			md.addDeclarationStatement(sn);
			this.setMultiDeclaration(md);
			sn.setMultiDeclaration(md);
		}
	}
	public void removeFromMultiDeclaration() {
		if (multiDeclaration == null) {
			return;
		}
		multiDeclaration.declStmts.remove(this);
		/*if (multiDeclaration.declStmts.size() <= 1) {
			for (StatementNode sn: multiDeclaration.declStmts) {
				sn.multiDeclaration = null;
			}
		}*/
		multiDeclaration = null;
	}
	
	public void setMoved() {
		this.moved = true;
	}
	
	@Override
	protected boolean containsNode(final Node n) {
		if (this.equals(n)) {
			return true;
		}
		for (BlockNode bn: blocks) {
			if (bn.containsNode(n)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isBlockAncestorOfThis(final BlockNode bn) {
		BlockNode parent = this.getParent();
		while (parent != null && !parent.equals(bn)) {
			parent = parent.parent == null ? null : parent.parent.parent;
		}
		//
		if (parent == null) {
			return false;
		}
		return true;
	}
	/** Returns false if they are equal. */
	@Override
	public boolean isStmtAncestorOfThis(final StatementNode sn) {
		if (this.equals(sn)) {
			return false;
		}
		StatementNode parent = this.parent == null ? null : this.parent.parent;
		while (parent != null && !parent.equals(sn)) {
			parent = parent.parent == null ? null : parent.parent.parent;
		}
		//
		if (parent == null) {
			return false;
		}
		return true;
	} 

	public BlockNode findBlockInStmtWhichContainsNode(final Node containedNode) {
		if (!containedNode.isStmtAncestorOfThis(this)) {
			return null;
		}
		final ListIterator<BlockNode> it = blocks.listIterator();
		while (it.hasNext()) {
			final BlockNode currB = it.next();
			if (containedNode.isBlockAncestorOfThis(currB)) {
				return currB;
			}
		}
		//invalid state
		return null;
	}

	/**
	 * Returns -1,0,1 if the current position in the tree of 'this' is earlier/(equal or contained)/later than the 'other' Node
	 * Any modification of the tree invalidates all previous comparison results.
	 * Comparison is based on the {@link Node} position in the tree, not on the location of its {@link #astNode}
	 * */
	protected int compareCurrentPositionTo(final StatementNode other) {
		if (this.equals(other)) {return 0;}
		if (this.isStmtAncestorOfThis(other)) {return 0;}
		if (other.isStmtAncestorOfThis(this)) {return 0;}

		final LinkedList<Integer> pos0 = new LinkedList<Integer>();
		StatementNode currSt = this;
		BlockNode currBlock = getParent();
		while (currBlock != null) {
			pos0.addFirst(currBlock.getStatements().indexOf(currSt));
			currSt = currBlock.getParent();
			if (currSt != null) {
				pos0.addFirst(currSt.getBlocks().indexOf(currBlock));
				currBlock = currSt.getParent();
			} else {
				currBlock = null;
			}
		}
		//
		final LinkedList<Integer> pos1 = new LinkedList<Integer>();
		currSt = other;
		currBlock = other.getParent();
		while (currBlock != null) {
			pos1.addFirst(currBlock.getStatements().indexOf(currSt));
			currSt = currBlock.getParent();
			if (currSt != null) {
				pos1.addFirst(currSt.getBlocks().indexOf(currBlock));
				currBlock = currSt.getParent();
			} else {
				currBlock = null;
			}
		}
		//compare lists
		final ListIterator<Integer> it0 = pos0.listIterator();
		final ListIterator<Integer> it1 = pos1.listIterator();
		while (it0.hasNext() && it1.hasNext()) {
			final int ind0 = it0.next();
			final int ind1 = it1.next();
			if (ind0 != ind1) {
				return (ind0 < ind1) ? -1 : ((ind0 == ind1) ? 0 : 1);//TODO update with Java 1.7 to Integer.compare
			}
		}
		ErrorReporter.logError("StatementNode.compareCurrentPositionTo(): Erroneous state: " + Utils.createLocationString(astNode));
		return 0;
	}
	
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("SN(").append(astNode.toString()).append("), loc: ");
		if (astNode instanceof ILocateableNode) {
			final Location loc = ((ILocateableNode)astNode).getLocation();
			sb.append(loc.getOffset()).append('-').append(loc.getEndOffset()).append(';');
		} else {
			sb.append("<none>;");
		}
		sb.append(" parent: ");
		if (parent == null) {
			sb.append("<null>; ");
		} else {
			sb.append("BN(").append(parent.astNode.toString()).append("); ");
		}
		if (moved) {
			sb.append("MOVED; ");
		}
		if (isDeclaration()) {
			sb.append("declaration: " + declaredVar);
		}
		return sb.toString();
	}
	
	public String toStringRecursive(final boolean recursive, final int prefixLen) {
		final String prefix = new String(new char[prefixLen]).replace('\0', ' ');
		final StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("SN: ").append(toString()).append('\n');
		if (recursive) {
			sb.append(prefix).append("  blocks: ").append('\n');
			for (BlockNode bn: blocks) {
				sb.append(bn.toStringRecursive(true, prefixLen+4)).append('\n');
			}
		}
		sb.append(prefix).append("  refdVars: ").append('\n');
		for (Variable var: referedVars) {
			sb.append(var.toStringRecursive(false, prefixLen+4)).append('\n');
		}
		return sb.toString();
	}
	
}
