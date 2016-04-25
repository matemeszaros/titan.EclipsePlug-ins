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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.IParameterisedAssignment;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Return_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell: Lazy parameter passing a special
 * construct in TTCN-3. When an "in" parameter is @lazy, it is not evaluated
 * when the function is called ... but only when the expression is evaluated. As
 * such it is only useful if it is not used on every possible execution path.
 * Also ... if an "in" parameter is not used on every possible execution path,
 * the code might become faster if it is set as @lazy.
 * 
 * @author Peter Olah
 * 
 * TODO: does not check if the parameter is used as an actual parameter to a lazy formal parameter.
 */
public class Lazy extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "The {0} parameter should {1}be @lazy";

	private RelevantFormalParameterCollector formalParameterCollector;

	private boolean haveToContinue;

	public Lazy() {
		super(CodeSmellType.LAZY);
	}

	@Override
	protected void process(final IVisitableNode node, final Problems problems) {
		// This variable indicates occurrence of Return_Statement.
		haveToContinue = true;

		// Collect and store FormalParameters.
		formalParameterCollector = new RelevantFormalParameterCollector();
		node.accept(formalParameterCollector);

		// Build structure.
		RelevantNodeBuilder relevantNodeBuilder = new RelevantNodeBuilder(node);
		node.accept(relevantNodeBuilder);

		// Evaluate tree and return with FormalParameters which have to be evaluated.
		Set<FormalParameter> shouldBeEvaluated = relevantNodeBuilder.collectRelevantReferences();

		for (FormalParameter formalParameter : formalParameterCollector.getItems()) {
			boolean isLazy = formalParameter.getIsLazy();

			String message = null;

			if (shouldBeEvaluated.contains(formalParameter)) {
				if (isLazy) {
					message = "not ";
				}
			} else {
				if (!isLazy) {
					message = "";
				}
			}

			if (message != null) {
				String msg = MessageFormat.format(ERROR_MESSAGE, formalParameter.getIdentifier().getDisplayName(), message);
				problems.report(formalParameter.getLocation(), msg);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(3);
		ret.add(Def_Altstep.class);
		ret.add(Def_Function.class);
		ret.add(Def_Testcase.class);
		return ret;
	}

	/**
	 * This class builds own data structure. Each root's of node is a
	 * StatementBlock or a Statement or an AltGuard. On initializing, we set
	 * Def_Altstep or Def_Function or Def_Testcase as root because it is the
	 * StartNode.
	 * 
	 * @author Peter Olah
	 */
	public class RelevantNodeBuilder extends ASTVisitor {

		private IVisitableNode root;

		private List<RelevantNodeBuilder> nodes;

		// Contains possible FormalParameters of expression block of If_Statement and SelectCase_Statement.
		private Set<FormalParameter> strictFormalParameters;

		// Contains possible FormalParameters of StatementBloc and Statement and AltGuard.
		private HashSet<FormalParameter> referencedFormalParameters;

		public RelevantNodeBuilder(final IVisitableNode node) {
			root = node;
			referencedFormalParameters = new HashSet<FormalParameter>();
			strictFormalParameters = new HashSet<FormalParameter>();
			nodes = new ArrayList<RelevantNodeBuilder>();
		}

		@Override
		public int visit(final IVisitableNode node) {
			if ((node instanceof StatementBlock || node instanceof Statement || node instanceof AltGuard) && !node.equals(root)) {
				RelevantNodeBuilder statementBlockCollector = new RelevantNodeBuilder(node);

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
				Reference reference = (Reference) node;

				Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);

				if (assignment instanceof FormalParameter) {
					FormalParameter formalParameter = (FormalParameter) assignment;

					if (formalParameterCollector.getItems().contains(formalParameter)) {
						if (root instanceof If_Statement || root instanceof SelectCase_Statement) {
							strictFormalParameters.add(formalParameter);
						} else {
							referencedFormalParameters.add(formalParameter);

						}
					}
				}
				
				//TODO it would be more precise to check the actual parameters if parameterized references
			}

			return V_CONTINUE;
		}

		public Set<FormalParameter> collectRelevantReferences() {
			HashSet<FormalParameter> shouldBeEvaluated = new HashSet<FormalParameter>();

			// After that we disregard content's of nodes
			if (root instanceof Return_Statement) {
				haveToContinue = false;
				return referencedFormalParameters;
			}

			if (nodes.isEmpty()) {
				return referencedFormalParameters;
			} else {

				Set<FormalParameter> tempStricts = new HashSet<FormalParameter>();

				for (int index = 0, nodeSize = nodes.size(); index < nodeSize; ++index) {
					if (haveToContinue) {

						tempStricts.addAll(nodes.get(index).strictFormalParameters);

						Set<FormalParameter> temp = nodes.get(index).collectRelevantReferences();

						if (root instanceof StatementBlock || root instanceof Definition || root instanceof AltGuard) {
							shouldBeEvaluated.addAll(temp);
						} else {
							if (((root instanceof If_Statement || root instanceof SelectCase_Statement) && nodeSize == 1)) {
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

	/**
	 * This class collects default and in FormalParameters.
	 * 
	 * @author Peter Olah
	 */
	public class RelevantFormalParameterCollector extends ASTVisitor {
		private List<FormalParameter> items;

		public RelevantFormalParameterCollector() {
			items = new ArrayList<FormalParameter>();
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof IParameterisedAssignment) {
				FormalParameterList formalParameterList = ((IParameterisedAssignment) node).getFormalParameterList();

				for (int i = 0; i < formalParameterList.getNofParameters(); ++i) {
					FormalParameter formalParameter = formalParameterList.getParameterByIndex(i);

					Assignment_type type = formalParameter.getAssignmentType();

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
}