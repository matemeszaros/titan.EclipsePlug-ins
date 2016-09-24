/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Log_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Return_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsBoundExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Log2StrExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * An out parameter of a function might be uninitialized before 
 * its first assignment. Reading the parameter before that, can 
 * cause problems.
 * 
 * @author Viktor Varga
 */
public class ReadingOutParBeforeWritten extends BaseModuleCodeSmellSpotter {
	
	private static final String ERR_MSG = "The out parameter `{0}'' is read before it is written.";

	private Definition toFind;
	private Problems problems;
	
	public ReadingOutParBeforeWritten() {
		super(CodeSmellType.READING_OUT_PAR_BEFORE_WRITTEN);
	}

	@Override
	protected void process(final IVisitableNode node, final Problems problems) {
		this.problems = problems;
		if (!(node instanceof FormalParameter)) {
			return;
		}
		final FormalParameter fp = (FormalParameter)node;
		this.toFind = fp;
		final Assignment_type at = fp.getAssignmentType();
		if (at != Assignment_type.A_PAR_VAL_OUT && at != Assignment_type.A_PAR_TEMP_OUT) {
			return;
		}
		final Definition d = fp.getMyParameterList().getMyDefinition();
		final NewFuncVisitor visitor = new NewFuncVisitor();
		//call visitor on function in which the out parameter was found
		d.accept(visitor);
		

	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(FormalParameter.class);
		return ret;
	}
	
	
	//call on Def_Function -> finds the StatementBlock of the function and starts a StatementBlockVisitor on it
	private final class NewFuncVisitor extends ASTVisitor {
		
		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof StatementBlock) {
				final StatementBlockVisitor visitor = new StatementBlockVisitor();
				node.accept(visitor);
				return V_SKIP;
			}
			return V_CONTINUE;
		}
		
	}
	
	//call on StatementBlocks (recursive with StatementVisitor)
	private final class StatementBlockVisitor extends ASTVisitor {
		
		private boolean written = false;
		
		public boolean isWritten() {
			return written;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			
			//start a new statement visitor for all statements
			if (node instanceof Statement) {
				//ignore log statements
				if (node instanceof Log_Statement) {
					return V_SKIP;
				}
				final StatementVisitor visitor = new StatementVisitor();
				node.accept(visitor);
				final boolean visWritten = visitor.isWritten();
				//special statements
				if (node instanceof If_Statement) {
					final boolean allBlocksWritten = visitor.isAllBlocksWritten();
					final If_Statement ifs = (If_Statement)node;
					final boolean hasElse = ifs.getStatementBlock() != null;
					if (hasElse && allBlocksWritten) {
						written = true;
					}
				} else if (node instanceof SelectCase_Statement) {
					final boolean allBlocksWritten = visitor.isAllBlocksWritten();
					final SelectCase_Statement scs = (SelectCase_Statement)node;
					final List<SelectCase> cases = scs.getSelectCases().getSelectCaseArray();
					boolean hasElse = false;
					for (final SelectCase sc: cases) {
						if (sc != null && sc.hasElse()) {
							hasElse = true;
							break;
						}
					}
					if (hasElse && allBlocksWritten) {
						written = true;
					}
				} else if (node instanceof Return_Statement) {
					written = true;
				} else {
					written = visWritten;
				}
				//abort if (all branches were) written in last statement
				if (written) {
					return V_ABORT;
				}
				return V_SKIP;
			}
			
			
			return V_CONTINUE;
		}
		
		
	}
	
	//call on Statements (recursive with StatementBlockVisitor)
	private final class StatementVisitor extends ASTVisitor {
		
		private boolean written = false;
		private boolean allBlocksWritten = true;
		private boolean continueOne = false;
		private boolean refFoundInsideIsBound = false;
		private boolean isInsideIsBound = false;
		
		public boolean isWritten() {
			return written;
		}
		public boolean isAllBlocksWritten() {
			return allBlocksWritten;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			if (continueOne) {
				continueOne = false;
				return V_CONTINUE;
			}
			if (node instanceof StatementBlock) {
				final StatementBlockVisitor visitor = new StatementBlockVisitor();
				node.accept(visitor);
				final boolean visWritten = visitor.isWritten();
				allBlocksWritten &= visWritten;
				return V_SKIP;
			}
			if (node instanceof IsBoundExpression) {
				continueOne = true;
				//entering IsBoundExpression to test if there are any references of 'toFind' inside
				isInsideIsBound = true;
				node.accept(this);
				isInsideIsBound = false;
				if (refFoundInsideIsBound) {
					written = true;
					return V_ABORT;
				}
				return V_SKIP;
			}
			if (node instanceof Log2StrExpression) {
				return V_SKIP;
			}
			if (node instanceof Undefined_LowerIdentifier_Value) {
				((Undefined_LowerIdentifier_Value)node).getAsReference();
				return V_CONTINUE;
			}
			if (node instanceof Reference) {
				final Reference ref = (Reference)node;
				final Assignment as = ref.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				if (!(as instanceof Definition)) {
					return V_SKIP;
				}
				final Definition def = (Definition)as;
				if (def.equals(toFind)) {
					if (ref.getUsedOnLeftHandSide()) {
						//written
						written = true;
					} else {
						//read
						if (isInsideIsBound) {
							refFoundInsideIsBound = true;
							written = true;
							return V_SKIP;
						}
						problems.report(ref.getLocation(), MessageFormat.format(ERR_MSG, node));
					}
				}
			}
			return V_CONTINUE;
		}
		
		
	}
	

}
