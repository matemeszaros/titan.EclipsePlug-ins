/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuards;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.DoWhile_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Interleave_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Return_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;

/**
 * Call for any {@link ASTNode}.
 * <p>
 * A special visitor class for finding out whether the given {@link ASTNode} will not, might, or will return.
 * Unlike the Statement.hasReturn() method, this class only handles return statements.
 * <p>
 * {@link ReturnVisitor} and its nested classes start an instance of each other recursively, normally in the
 * following order: {@link ReturnVisitor} -> {@link BranchMerger} -> {@link StatementBlockVisitor} ->
 * {@link ReturnVisitor} -> ...
 *
 * @author Viktor Varga
 * */
class ReturnVisitor extends ASTVisitor {

	enum ReturnCertainty {
		NO, MAYBE, YES;

		/** merges the {@link ReturnCertainty} of two branches */
		ReturnCertainty or(final ReturnCertainty other) {
			if (this == YES && other == YES) {
				return YES;
			}
			if (this == NO && other == NO) {
				return NO;
			}
			return MAYBE;
		}
		/** merges the {@link ReturnCertainty} of two consecutive statements */
		ReturnCertainty and(final ReturnCertainty other) {
			if (this == YES || other == YES) {
				return YES;
			}
			if (this == NO && other == NO) {
				return NO;
			}
			return MAYBE;
		}
	}

	private ReturnCertainty certainty = ReturnCertainty.NO;

	public ReturnCertainty getCertainty() {
		return certainty;
	}

	@Override
	public int visit(final IVisitableNode node) {
		//certain YES
		if (node instanceof Return_Statement) {
			certainty = ReturnCertainty.YES;
			return V_ABORT;
		}
		//
		if (node instanceof StatementBlock ||
				node instanceof StatementList) {
			final StatementBlockVisitor blockVis = new StatementBlockVisitor();
			node.accept(blockVis);
			certainty = blockVis.getCertainty();
			return V_SKIP;
		}
		//custom statements
		if (node instanceof While_Statement ||
				node instanceof DoWhile_Statement ||
				node instanceof For_Statement) {
			final BranchMerger branchMerger = new BranchMerger();
			node.accept(branchMerger);
				//conditional blocks: maximum MAYBE
			certainty = branchMerger.getCertainty().or(ReturnCertainty.NO);
			return V_SKIP;
		}
		if (node instanceof If_Statement) {
			final If_Statement ifs = (If_Statement)node;
			final BranchMerger branchMerger = new BranchMerger();
			node.accept(branchMerger);
			if (ifs.getStatementBlock() != null) {
				//must enter one block: maximum YES
				certainty = branchMerger.getCertainty();
			} else {
				//conditional blocks: maximum MAYBE
				certainty = branchMerger.getCertainty().or(ReturnCertainty.NO);
			}
			return V_SKIP;
		}
		if (node instanceof Alt_Statement) {
			final AltGuards ags = ((Alt_Statement)node).getAltGuards();
			final BranchMerger branchMerger = new BranchMerger();
			ags.accept(branchMerger);
			if (ags.hasElse()) {
				//must enter one block: maximum YES
				certainty = branchMerger.getCertainty();
			} else {
				//conditional blocks: maximum MAYBE
				certainty = branchMerger.getCertainty().or(ReturnCertainty.NO);
			}
			return V_SKIP;
		}
		if (node instanceof Interleave_Statement) {
			final BranchMerger branchMerger = new BranchMerger();
			node.accept(branchMerger);
				//conditional block: maximum MAYBE
			certainty = branchMerger.getCertainty().or(ReturnCertainty.NO);
			return V_SKIP;
		}
		if (node instanceof StatementBlock_Statement) {
			final BranchMerger branchMerger = new BranchMerger();
			node.accept(branchMerger);
			//must enter block: maximum YES
			certainty = branchMerger.getCertainty();
			return V_SKIP;
		}
		if (node instanceof SelectCase_Statement) {
			final BranchMerger branchMerger = new BranchMerger();
			node.accept(branchMerger);
			//must enter one block: maximum YES
			certainty = branchMerger.getCertainty();
			return V_SKIP;
		}
		return V_ABORT;
	}

	/**
	 * Call for {@link StatementBlock} or {@link StatementList}.
	 * <p>
	 * Starts a {@link ReturnVisitor} for each {@link Statement} found in it.
	 */
	private class StatementBlockVisitor extends ASTVisitor {

		private ReturnCertainty blockCertainty = ReturnCertainty.NO;

		private ReturnCertainty getCertainty() {
			return blockCertainty;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof StatementBlock) {
				final StatementBlock sb = (StatementBlock)node;
				for (int i=0;i<sb.getSize();i++) {
					final Statement s = sb.getStatementByIndex(i);
					final ReturnVisitor sVis = new ReturnVisitor();
					s.accept(sVis);
					final ReturnCertainty rc = sVis.getCertainty();
					blockCertainty = blockCertainty.and(rc);
					if (blockCertainty == ReturnCertainty.YES) {
						return V_ABORT;
					}
				}
			} else if (node instanceof StatementList) {
				final StatementList sl = (StatementList)node;
				for (int i=0;i<sl.getSize();i++) {
					final Statement s = sl.getStatementByIndex(i);
					final ReturnVisitor sVis = new ReturnVisitor();
					s.accept(sVis);
					final ReturnCertainty rc = sVis.getCertainty();
					blockCertainty = blockCertainty.and(rc);
					if (blockCertainty == ReturnCertainty.YES) {
						return V_ABORT;
					}
				}
			}
			return V_ABORT;
		}

	}

	/**
	 * Call for any {@link ASTNode}.
	 * <p>
	 * Starts a {@link StatementBlockVisitor} on each {@link StatementBlock} found.
	 * However the visitor does not enter {@link StatementBlock} nodes.
	 * When the {@link BranchMerger} finished visiting, {@link BranchMerger#getCertainty()}
	 * returns the 'OR' merged {@link ReturnCertainty}.
	 * */
	private class BranchMerger extends ASTVisitor {

		private ReturnCertainty blockCertainty = ReturnCertainty.NO;
		private boolean foundAnyBlocksBefore = false;

		private ReturnCertainty getCertainty() {
			return blockCertainty;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof StatementBlock) {
				final StatementBlockVisitor sbVis = new StatementBlockVisitor();
				node.accept(sbVis);
				if (!foundAnyBlocksBefore) {
					foundAnyBlocksBefore = true;
					blockCertainty = sbVis.getCertainty();
				} else {
					blockCertainty = blockCertainty.or(sbVis.getCertainty());
				}
				return V_SKIP;
			}
			return V_CONTINUE;
		}

	}

}
