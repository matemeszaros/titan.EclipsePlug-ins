/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.For_Loop_Definitions;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The For_Statement class represents TTCN3 for statements.
 * 
 * @author Kristof Szabados
 * */
public final class For_Statement extends Statement {
	private static final String OPERANDERROR = "The final expression of a for statement should be a boolean value";
	private static final String UNNECESSARYCONTROL = "This control is unnecessary because the final condition evaluates to true";
	private static final String NEVERREACH = "Control never reaches this code because the final condition evaluates to false";

	private static final String FULLNAMEPART1 = ".init";
	private static final String FULLNAMEPART2 = ".final";
	private static final String FULLNAMEPART3 = ".step";
	private static final String FULLNAMEPART4 = ".block";
	private static final String STATEMENT_NAME = "for";

	/**
	 * The definitions declared in the initial declaration part of the for
	 * statement.
	 */
	private final For_Loop_Definitions definitions;

	/** The initial assignment. */
	private final Assignment_Statement initialAssignment;

	/** The stop condition. */
	private final Value finalExpression;

	/** The stepping assignment. */
	private final Assignment_Statement stepAssignment;

	/**
	 * the statementblock of the for statement.
	 * <p>
	 * This can be null
	 * */
	private final StatementBlock statementblock;

	public For_Statement(final For_Loop_Definitions definitions, final Value finalExpression, final Assignment_Statement incrementStep,
			final StatementBlock statementblock) {
		this.definitions = definitions;
		this.initialAssignment = null;
		this.finalExpression = finalExpression;
		this.stepAssignment = incrementStep;
		this.statementblock = statementblock;

		init();
	}

	public For_Statement(final Assignment_Statement initialAssignment, final Value finalExpression, final Assignment_Statement incrementStep,
			final StatementBlock statementblock) {
		this.definitions = null;
		this.initialAssignment = initialAssignment;
		this.finalExpression = finalExpression;
		this.stepAssignment = incrementStep;
		this.statementblock = statementblock;

		init();
	}

	private void init() {
		if (definitions != null) {
			definitions.setFullNameParent(this);
		}
		if (initialAssignment != null) {
			initialAssignment.setFullNameParent(this);
		}
		if (finalExpression != null) {
			finalExpression.setFullNameParent(this);
		}
		if (stepAssignment != null) {
			stepAssignment.setFullNameParent(this);
		}
		if (statementblock != null) {
			statementblock.setFullNameParent(this);
			statementblock.setOwnerIsLoop();
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_WHILE;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (definitions == child) {
			return builder.append(FULLNAMEPART1);
		} else if (initialAssignment == child) {
			return builder.append(FULLNAMEPART1);
		} else if (finalExpression == child) {
			return builder.append(FULLNAMEPART2);
		} else if (stepAssignment == child) {
			return builder.append(FULLNAMEPART3);
		} else if (statementblock == child) {
			return builder.append(FULLNAMEPART4);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (definitions == null) {
			if (initialAssignment != null) {
				initialAssignment.setMyScope(scope);
			}
			if (finalExpression != null) {
				finalExpression.setMyScope(scope);
			}
			if (stepAssignment != null) {
				stepAssignment.setMyScope(scope);
			}
			if (statementblock != null) {
				statementblock.setMyScope(scope);
				scope.addSubScope(statementblock.getLocation(), statementblock);
			}
		} else {
			definitions.setParentScope(scope);
			final Location startLoc = definitions.getLocation();
			Location endLoc = null;
			if (finalExpression != null) {
				finalExpression.setMyScope(definitions);
				endLoc = finalExpression.getLocation();
			}
			if (stepAssignment != null) {
				stepAssignment.setMyScope(definitions);
				endLoc = stepAssignment.getLocation();
			}
			scope.addSubScope((endLoc == null) ? startLoc : Location.interval(startLoc, endLoc), definitions);
			if (statementblock != null) {
				statementblock.setMyScope(definitions);
				scope.addSubScope(statementblock.getLocation(), statementblock);
			}
		}
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		if (statementblock != null) {
			statementblock.setMyStatementBlock(statementBlock, index);
		}
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		if (statementblock != null) {
			statementblock.setMyDefinition(definition);
		}
	}

	@Override
	public void setMyAltguards(final AltGuards altGuards) {
		if (statementblock != null) {
			statementblock.setMyAltguards(altGuards);
		}
	}

	@Override
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (statementblock != null) {
			if (StatementBlock.ReturnStatus_type.RS_NO.equals(statementblock.hasReturn(timestamp))) {
				return StatementBlock.ReturnStatus_type.RS_NO;
			}

			return StatementBlock.ReturnStatus_type.RS_MAYBE;
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (definitions != null) {
			definitions.check(timestamp);
		}
		if (initialAssignment != null) {
			initialAssignment.check(timestamp);
		}
		if (finalExpression != null) {
			finalExpression.setLoweridToReference(timestamp);
			final IValue lastValue = finalExpression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			final Type_type temp = lastValue.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

			switch (temp) {
			case TYPE_BOOL:
				if (!lastValue.isUnfoldable(timestamp)) {
					if (((Boolean_Value) lastValue).getValue()) {
						finalExpression.getLocation().reportConfigurableSemanticProblem(
								Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
										PreferenceConstants.REPORTUNNECESSARYCONTROLS,
										GeneralConstants.WARNING, null), UNNECESSARYCONTROL);
					} else {
						finalExpression.getLocation().reportConfigurableSemanticProblem(
								Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
										PreferenceConstants.REPORTUNNECESSARYCONTROLS,
										GeneralConstants.WARNING, null), NEVERREACH);
					}
				}
				break;
			default:
				location.reportSemanticError(OPERANDERROR);
				finalExpression.setIsErroneous(true);
				break;
			}
		}
		if (stepAssignment != null) {
			stepAssignment.check(timestamp);
		}
		if (statementblock != null) {
			statementblock.check(timestamp);
			//warning for "return" has been removed. Not valid problem
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void checkAllowedInterleave() {
		if (statementblock != null) {
			statementblock.checkAllowedInterleave();
		}
	}

	@Override
	public void postCheck() {
		if (definitions != null) {
			definitions.postCheck();
		}

		if (statementblock != null) {
			statementblock.postCheck();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean enveloped = false;

			if (definitions != null) {
				if (enveloped) {
					definitions.updateSyntax(reparser, false);
					reparser.updateLocation(definitions.getLocation());
				} else if (reparser.envelopsDamage(definitions.getLocation())) {
					definitions.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(definitions.getLocation());
				}
			}

			if (initialAssignment != null) {
				if (enveloped) {
					initialAssignment.updateSyntax(reparser, false);
					reparser.updateLocation(initialAssignment.getLocation());
				} else if (reparser.envelopsDamage(initialAssignment.getLocation())) {
					initialAssignment.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(initialAssignment.getLocation());
				}
			}

			if (finalExpression != null) {
				if (enveloped) {
					finalExpression.updateSyntax(reparser, false);
					reparser.updateLocation(finalExpression.getLocation());
				} else if (reparser.envelopsDamage(finalExpression.getLocation())) {
					finalExpression.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(finalExpression.getLocation());
				}
			}

			if (stepAssignment != null) {
				if (enveloped) {
					stepAssignment.updateSyntax(reparser, false);
					reparser.updateLocation(stepAssignment.getLocation());
				} else if (reparser.envelopsDamage(stepAssignment.getLocation())) {
					stepAssignment.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(stepAssignment.getLocation());
				}
			}

			if (statementblock != null) {
				if (enveloped) {
					statementblock.updateSyntax(reparser, false);
					reparser.updateLocation(statementblock.getLocation());
				} else if (reparser.envelopsDamage(statementblock.getLocation())) {
					statementblock.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(statementblock.getLocation());
				}
			}

			if (!enveloped) {
				throw new ReParseException();
			}

			return;
		}

		if (definitions != null) {
			definitions.updateSyntax(reparser, false);
			reparser.updateLocation(definitions.getLocation());
		}

		if (initialAssignment != null) {
			initialAssignment.updateSyntax(reparser, false);
			reparser.updateLocation(initialAssignment.getLocation());
		}

		if (finalExpression != null) {
			finalExpression.updateSyntax(reparser, false);
			reparser.updateLocation(finalExpression.getLocation());
		}

		if (stepAssignment != null) {
			stepAssignment.updateSyntax(reparser, false);
			reparser.updateLocation(stepAssignment.getLocation());
		}

		if (statementblock != null) {
			statementblock.updateSyntax(reparser, false);
			reparser.updateLocation(statementblock.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (definitions != null) {
			definitions.findReferences(referenceFinder, foundIdentifiers);
		}
		if (initialAssignment != null) {
			initialAssignment.findReferences(referenceFinder, foundIdentifiers);
		}
		if (finalExpression != null) {
			finalExpression.findReferences(referenceFinder, foundIdentifiers);
		}
		if (stepAssignment != null) {
			stepAssignment.findReferences(referenceFinder, foundIdentifiers);
		}
		if (statementblock != null) {
			statementblock.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (definitions != null && !definitions.accept(v)) {
			return false;
		}
		if (initialAssignment != null && !initialAssignment.accept(v)) {
			return false;
		}
		if (finalExpression != null && !finalExpression.accept(v)) {
			return false;
		}
		if (stepAssignment != null && !stepAssignment.accept(v)) {
			return false;
		}
		if (statementblock != null && !statementblock.accept(v)) {
			return false;
		}
		return true;
	}

	public Value getFinalExpression() {
		return finalExpression;
	}

	public StatementBlock getStatementBlock() {
		return statementblock;
	}

}
