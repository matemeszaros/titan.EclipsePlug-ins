/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.lazyfication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.IParameterisedAssignment;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Value_ActualParameter;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Function_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Return_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * The task of this class is to find the formal parameters that could be lazy
 * 
 * @author Istvan Bohm
 */
class LazyChecker {

	private final List<FormalParameter> lazyParameterList;
	
	private RelevantFormalParameterCollector formalParameterCollector;
	private boolean haveToContinue;
	
	LazyChecker() {
		lazyParameterList = new ArrayList<FormalParameter>();
	}
	
	public void process(final IVisitableNode node) {
		// This variable indicates occurrence of Return_Statement.
		haveToContinue = true;

		// Collect and store FormalParameters.
		formalParameterCollector = new RelevantFormalParameterCollector();
		node.accept(formalParameterCollector);

		// Build structure.
		final RelevantNodeBuilder relevantNodeBuilder = new RelevantNodeBuilder(node);
		node.accept(relevantNodeBuilder);

		// Evaluate tree and return with FormalParameters which have to be evaluated.
		final Set<FormalParameter> shouldBeEvaluated = relevantNodeBuilder.collectRelevantReferences();

		for (final FormalParameter formalParameter : formalParameterCollector.getItems()) {
			if(!shouldBeEvaluated.contains(formalParameter) && !formalParameter.getIsLazy() ) {
				lazyParameterList.add(formalParameter);
			}
		}
	}
	
	public List<FormalParameter> getLazyParameterList() {
		return lazyParameterList;
	}
	
	/**
	 * This class collects default and in FormalParameters.
	 * 
	 * @author Peter Olah
	 */
	public class RelevantFormalParameterCollector extends ASTVisitor {
		private final List<FormalParameter> items;

		public RelevantFormalParameterCollector() {
			items = new ArrayList<FormalParameter>();
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof IParameterisedAssignment) {
				final FormalParameterList formalParameterList = ((IParameterisedAssignment) node).getFormalParameterList();
				for (int i = 0; i < formalParameterList.getNofParameters(); ++i) {
					final FormalParameter formalParameter = formalParameterList.getParameterByIndex(i);
					final Assignment_type type = formalParameter.getAssignmentType();
					switch (type) {
						case A_PAR_VAL:
						case A_PAR_VAL_IN:
						case A_PAR_TEMP_IN:
							items.add(formalParameter);
							break;
						default:
							continue;
					}
				}
				return V_ABORT;
			}
			return V_CONTINUE;
		}

		public List<FormalParameter> getItems() {
			return items;
		}
	}
	
	/**
	 * This class builds own data structure. Each root's of node is a
	 * StatementBlock or a Statement or an AltGuard. On initializing, we set
	 * Def_Altstep or Def_Function or Def_Testcase as root because it is the
	 * StartNode.
	 * 
	 * @author Istvan Bohm
	 */
	public class RelevantNodeBuilder extends ASTVisitor {

		private IVisitableNode root;
		private List<RelevantNodeBuilder> nodes;

		// Contains possible FormalParameters of expression block of If_Statement and SelectCase_Statement.
		private Set<FormalParameter> strictFormalParameters;

		// Contains possible FormalParameters of StatementBloc and Statement and AltGuard.
		private Set<FormalParameter> referencedFormalParameters;
		
		// If this is a lazy formal parameter transmission, then the formal parameter is not relevant unless it is in some expression
		private boolean nextFormalParameterIsNotRelevant;
		
		public RelevantNodeBuilder(final IVisitableNode node) {
			root = node;
			referencedFormalParameters = new HashSet<FormalParameter>();
			strictFormalParameters = new HashSet<FormalParameter>();
			nodes = new ArrayList<RelevantNodeBuilder>();
		}
		
		public RelevantNodeBuilder(final IVisitableNode node,final boolean skipNextFormalParameter) {
			this(node);
			this.nextFormalParameterIsNotRelevant = skipNextFormalParameter;
		}

		@Override
		public int visit(final IVisitableNode node) {
			
			if(nextFormalParameterIsNotRelevant) {
				if(node instanceof Expression_Value) {
					nextFormalParameterIsNotRelevant = false;
				} else if(!(node instanceof Value_ActualParameter)){
					return V_SKIP;
				}
			}
			
			if ((node instanceof StatementBlock || node instanceof Statement || node instanceof AltGuard) && !node.equals(root)) {
				final RelevantNodeBuilder statementBlockCollector = new RelevantNodeBuilder(node,nextFormalParameterIsNotRelevant);

				// Handle separately the expression block of If_Statement and SelectCase_Statement.
				// Store the possible FormalParameters in strictFormalParameters collection.
				if (root instanceof If_Statement || root instanceof SelectCase_Statement) {
					statementBlockCollector.strictFormalParameters.addAll(strictFormalParameters);
					strictFormalParameters.clear();
				}
				nodes.add(statementBlockCollector);
				node.accept(statementBlockCollector);
				return V_SKIP;
			}

			// Only deal with Reference which referred FormalParameter.
			if (node instanceof Reference) {
				final Reference reference = (Reference) node;

				final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);

				if (assignment instanceof Def_Function) {
					final ISubReference subreference = ((Reference) node).getSubreferences().get(0);
					
					if(!(subreference instanceof ParameterisedSubReference)) {
						return V_SKIP;
					}
					
					final ParameterisedSubReference subref = (ParameterisedSubReference)((Reference) node).getSubreferences().get(0);
					final ParsedActualParameters parsedActualParameters = subref.getParsedParameters();
					
					final ActualParameterList nonLazyActualParameters = new ActualParameterList();
					final ActualParameterList lazyActualParameters = new ActualParameterList();
					final FormalParameterList formalParameterList=((Def_Function)assignment).getFormalParameterList();
					formalParameterList.collateLazyAndNonLazyActualParameters(CompilationTimeStamp.getBaseTimestamp(),parsedActualParameters, lazyActualParameters, nonLazyActualParameters);

					if(nonLazyActualParameters.getNofParameters()!=0) {
						final RelevantNodeBuilder statementBlockCollector = new RelevantNodeBuilder(root);
						nodes.add(statementBlockCollector);
						nonLazyActualParameters.accept(statementBlockCollector);
					}
					final int size=lazyActualParameters.getNofParameters(); 
					for(int i=0;i<size;++i) {
						final ActualParameter lazyActualParameter = lazyActualParameters.getParameter(i);
						if(lazyActualParameter instanceof Value_ActualParameter) {
							final RelevantNodeBuilder statementBlockCollector = new RelevantNodeBuilder(root,true);
							nodes.add(statementBlockCollector);
							lazyActualParameter.accept(statementBlockCollector);
						}
					}
					return V_SKIP;
				}
				
				if (assignment instanceof FormalParameter) {
					if(nextFormalParameterIsNotRelevant) {
						return V_CONTINUE;
					}
					final FormalParameter formalParameter = (FormalParameter) assignment;
					if (formalParameterCollector.getItems().contains(formalParameter)) {
						if (root instanceof If_Statement || root instanceof SelectCase_Statement) {
							strictFormalParameters.add(formalParameter);
						} else {
							referencedFormalParameters.add(formalParameter);
						}
					}
				}
			}
			return V_CONTINUE;
		}

		public Set<FormalParameter> collectRelevantReferences() {
			// After that we disregard content's of nodes
			if (root instanceof Return_Statement) {
				haveToContinue = false;
				return referencedFormalParameters;
			}
			
			final Set<FormalParameter> shouldBeEvaluated = new HashSet<FormalParameter>();

			if (nodes.isEmpty()) {
				return referencedFormalParameters;
			} else {
				final Set<FormalParameter> tempStricts = new HashSet<FormalParameter>();
				final int nodeSize = nodes.size();
				for (int index = 0; index < nodeSize; ++index) {
					if (haveToContinue) {
						tempStricts.addAll(nodes.get(index).strictFormalParameters);
						final Set<FormalParameter> temp = nodes.get(index).collectRelevantReferences();
						if (root instanceof StatementBlock || root instanceof Definition || root instanceof AltGuard || root instanceof Function_Instance_Statement) {
							shouldBeEvaluated.addAll(temp);
						} else {
							if ((root instanceof If_Statement || root instanceof SelectCase_Statement) && nodeSize == 1) {
								break;
							}
							// We have to branching because of intersections of empty and non empty set.
							// Have to check index too!
							// If index==0 and shouldBeEvaluated.size()==0 then we have to initialize set with addAll() method.
							if (shouldBeEvaluated.isEmpty() && index == 0) {
								shouldBeEvaluated.addAll(temp);
							} else {
								shouldBeEvaluated.retainAll(temp);
							}
						}
					}
				}
				shouldBeEvaluated.addAll(tempStricts);
				shouldBeEvaluated.addAll(referencedFormalParameters);
			}
			return shouldBeEvaluated;
		}
	}
}