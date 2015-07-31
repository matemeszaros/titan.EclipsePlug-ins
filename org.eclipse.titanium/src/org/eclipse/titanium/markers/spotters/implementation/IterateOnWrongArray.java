/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.LengthofExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.SizeOfExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * The loop parameter of a for loop indexes an array in the StatementBlock
 * of the loop which differs from the array inside the finalExpression of
 * the for loop
 * 
 * @author Viktor Varga
 */
public class IterateOnWrongArray extends BaseModuleCodeSmellSpotter {
	
	private static final String ERR_MSG = "The loop parameter `{0}'' might be used to index the wrong list.";
	
	
	public IterateOnWrongArray() {
		super(CodeSmellType.ITERATE_ON_WRONG_ARRAY);
	}

	@Override
	protected void process(IVisitableNode node, Problems problems) {
		if (!(node instanceof For_Statement)) {
			return;
		}
		For_Statement fs = (For_Statement) node;
		
		//find the loop variable
		LoopVariableFinder lvVisitor = new LoopVariableFinder();
		fs.accept(lvVisitor);
		Reference loopVar = lvVisitor.getLoopVariable();
		if (loopVar == null) {
			return;
		}
		Assignment loopVarDef = loopVar.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
		if (loopVarDef == null) {
			return;
		}
		//find the array over which the loop iterates
		Value finalExpr = fs.getFinalExpression();
		FinalExprVisitor exprVisitor = new FinalExprVisitor();
		finalExpr.accept(exprVisitor);
		List<Reference> arraysIterated = exprVisitor.getArraysIterated();
		if (arraysIterated.isEmpty()) {
			return;
		}
		/* search every statement block for references that has the loop variable in them and the
		 * reference differs from the reference of the array over which the for loop iterates */
		StatementBlock sb = fs.getStatementBlock();
		if (sb == null) {
			return;
		}
		StatementBlockVisitor sbVisitor = new StatementBlockVisitor(loopVar, arraysIterated);
		sb.accept(sbVisitor);
		List<Reference> matchingRefs = sbVisitor.getMatchingReferences();
		for (Reference r: matchingRefs) {
			if (r.getUsedOnLeftHandSide()) {
				continue;
			}
			problems.report(r.getLocation(), MessageFormat.format(ERR_MSG, loopVar));
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(For_Statement.class);
		return ret;
	}
	
	//call on Value (final expression of For_Statement)
	private static final class FinalExprVisitor extends ASTVisitor {
		
		private List<Reference> arraysIterated = new ArrayList<Reference>();
		
		public List<Reference> getArraysIterated() {
			return arraysIterated;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof LengthofExpression || node instanceof SizeOfExpression) {
				IteratedArrayFinder visitor = new IteratedArrayFinder();
				node.accept(visitor);
				arraysIterated.add(visitor.getArrayIterated());
				return V_SKIP;
			}
			return V_CONTINUE;
		}
	}
	
	//call on LengthofExpression or Sizeof_Expression
	private static final class IteratedArrayFinder extends ASTVisitor {
		
		private Reference arrayIterated;
		
		public Reference getArrayIterated() {
			return arrayIterated;
		}
		
		@Override
		public int visit(IVisitableNode node) {
			if (node instanceof LengthofExpression || node instanceof SizeOfExpression) {
				return V_CONTINUE;
			} else if (node instanceof Value) {
				IValue v = (Value)node;
				if (v instanceof Undefined_LowerIdentifier_Value) {
					Undefined_LowerIdentifier_Value uliv = (Undefined_LowerIdentifier_Value)v;
					v = uliv.setLoweridToReference(CompilationTimeStamp.getBaseTimestamp());
				}
				if (v instanceof Referenced_Value) {
					Referenced_Value rv = (Referenced_Value)v;
					arrayIterated = rv.getReference();
					if (arrayIterated != null) {
						return V_ABORT;
					}
				}
				return V_SKIP;
			}
			return V_CONTINUE;
		}
		
	}

	//call on StatementBlocks
	private static final class StatementBlockVisitor extends ASTVisitor {
		
		private final Reference loopVariable;
		private final List<Reference> arraysIterated;
		private final List<Reference> matchingReferences;
		
		private Reference parentReference;
		
		public StatementBlockVisitor(Reference loopVariable, List<Reference> arraysIterated) {
			this.loopVariable = loopVariable;
			this.arraysIterated = arraysIterated;
			matchingReferences = new ArrayList<Reference>();
		}
		
		public List<Reference> getMatchingReferences() {
			return matchingReferences;
		}
		
		@Override
		public int visit(IVisitableNode node) {
			//avoid access to nested For_Statements: Analyzer.CodeSmellVisitor will visit them directly
			if (node instanceof For_Statement) {
				return V_SKIP;
			}
			if (node instanceof Reference) {
				Reference ref = (Reference)node;
				parentReference = ref;
				return V_CONTINUE;
			} else if (node instanceof ArraySubReference) {
				ArraySubReference asr = (ArraySubReference)node;
				Value val = asr.getValue();
				if (val == null || val.getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
					return V_SKIP;
				}
				if (val instanceof Undefined_LowerIdentifier_Value) {
					IValue cval = val.setLoweridToReference(CompilationTimeStamp.getBaseTimestamp());
					if (cval instanceof Referenced_Value) {
						val = (Referenced_Value)cval;
					}
				}
				if (val instanceof Referenced_Value) {
					Reference ref = ((Referenced_Value) val).getReference();
					if (ref == null) {
						return V_SKIP;
					}
					Assignment as = ref.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
					Assignment parentRefDef = parentReference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
					Assignment loopVariableDef = loopVariable.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
					for (Reference arrayIterated: arraysIterated) {
						Assignment arrayIteratedDef = arrayIterated.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
						if (as != loopVariableDef
								|| (parentRefDef == arrayIteratedDef && hasSubreferencePrefix(arrayIterated, parentReference))) {
							return V_SKIP;
						}
					}
					matchingReferences.add(parentReference);
				}
				return V_SKIP;
			}
			return V_CONTINUE;
		}
		
		/*
		 * Returns true if the list of SubReferences of 'prefix' is a prefix of the list of 
		 * SubReferences of 'toTest';
		 * SubReferences are compared by their String representation.
		 * */
		private boolean hasSubreferencePrefix(Reference prefix, Reference toTest) {
			if (prefix == null || toTest == null || prefix.getSubreferences() == null || toTest.getSubreferences() == null) {
				return false;
			}
			ListIterator<ISubReference> itPrefix = prefix.getSubreferences().listIterator();
			ListIterator<ISubReference> itToTest = toTest.getSubreferences().listIterator();
			while (itPrefix.hasNext() && itToTest.hasNext()) {
				ISubReference srPrefix = itPrefix.next();
				ISubReference srToTest = itToTest.next();
				if (srPrefix instanceof ArraySubReference) {
					if (!(srToTest instanceof ArraySubReference)) {
						return false;
					}
					ArraySubReference asrPrefix = (ArraySubReference)srPrefix;
					ArraySubReference asrToTest = (ArraySubReference)srToTest;
					Value vPrefix = asrPrefix.getValue();
					Value vToTest = asrToTest.getValue();
					if (vPrefix == null || vToTest == null) {
						return false;
					}
					if (!vPrefix.createStringRepresentation().equals(vToTest.createStringRepresentation())) {
						return false;
					}
				} else {
					if (!(srPrefix.toString().equals(srToTest.toString()))) {
						return false;
					}
				}
			}
			//'prefix' cannot be longer than 'toTest'
			if (itPrefix.hasNext()) {
				return false;
			}
			return true;
		}
		
	}
	
	//call on For_Statements
	private static final class LoopVariableFinder extends ASTVisitor {
		
		private Reference loopVariable;
		
		public Reference getLoopVariable() {
			return loopVariable;
		}
		
		@Override
		public int visit(IVisitableNode node) {
			if (node instanceof For_Statement) {
				return V_CONTINUE;
			} else if (node instanceof Assignment_Statement) {
				Assignment_Statement as = (Assignment_Statement)node;
				loopVariable = as.getReference();
				return V_ABORT;
			}
			return V_SKIP;
		}
		
	}
	
}
