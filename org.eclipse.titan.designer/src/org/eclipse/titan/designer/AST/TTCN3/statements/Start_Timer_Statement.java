/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a timer starting statement.
 * 
 * @author Kristof Szabados
 * */
public final class Start_Timer_Statement extends Statement {
	private static final String TIMERREFERENCEEXPECTED = "Reference to a timer or timer parameter was expected instead of {0}";
	private static final String INVALIDSUBREFERENCES = "Reference to {0} cannot have field or array sub-references";
	private static final String FLOATEXPECTED = "float operand was expected";
	private static final String NEGATIVEDURATION = "The timer duration is negative: `{0}''";
	private static final String INFINITYDURATION = "The timer duration is `{0}''";
	private static final String MISSINGDEFAULTDURATION = "Missing duration: {0} does not have default duration";

	private static final String FULLNAMEPART1 = ".timerreference";
	private static final String FULLNAMEPART2 = ".timervalue";
	private static final String STATEMENT_NAME = "start timer";

	private final Reference timerReference;
	private final IValue timerValue;

	public Start_Timer_Statement(final Reference timerReference, final IValue value) {
		this.timerReference = timerReference;
		this.timerValue = value;

		if (timerReference != null) {
			timerReference.setFullNameParent(this);
		}
		if (timerValue != null) {
			timerValue.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_START_TIMER;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (timerReference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (timerValue == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (timerReference != null) {
			timerReference.setMyScope(scope);
		}
		if (timerValue != null) {
			timerValue.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		checkTimerReference(timestamp, timerReference);

		if (timerValue == null) {
			Assignment assignment = timerReference.getRefdAssignment(timestamp, true);
			if (assignment != null && Assignment_type.A_TIMER.equals(assignment.getAssignmentType())) {
				Def_Timer defTimer = (Def_Timer) assignment;
				if (!defTimer.hasDefaultDuration(timestamp, timerReference)) {
					location.reportSemanticError(MessageFormat.format(MISSINGDEFAULTDURATION, assignment.getDescription()));
				}
			}
		} else {
			timerValue.setLoweridToReference(timestamp);
			Type_type temporalType = timerValue.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

			switch (temporalType) {
			case TYPE_REAL: {
				IValue last = timerValue.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
				if (!last.isUnfoldable(timestamp)) {
					Real_Value real = (Real_Value) last;
					double val = real.getValue();
					if (val < 0.0) {
						timerValue.getLocation().reportSemanticError(MessageFormat.format(NEGATIVEDURATION, real));
					} else if (real.isPositiveInfinity()) {
						timerValue.getLocation().reportSemanticError(
								MessageFormat.format(INFINITYDURATION, real.createStringRepresentation()));
					}
				}
				return;
			}
			case TYPE_UNDEFINED:
				return;
			default:
				timerValue.getLocation().reportSemanticError(FLOATEXPECTED);
				return;
			}
		}
	}

	public static void checkTimerReference(final CompilationTimeStamp timestamp, final Reference reference) {
		if (reference == null) {
			return;
		}

		Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_TIMER:
			ArrayDimensions dimensions = ((Def_Timer) assignment).getDimensions();
			if (dimensions != null) {
				dimensions.checkIndices(timestamp, reference, "timer", false, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			} else if (reference.getSubreferences().size() > 1) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to a single {0} cannot have field or array sub-references",
								assignment.getDescription()));
			}
			break;
		case A_PAR_TIMER:
			if (reference.getSubreferences().size() > 1) {
				reference.getLocation().reportSemanticError(MessageFormat.format(INVALIDSUBREFERENCES, assignment.getDescription()));
			}
			break;
		default:
			reference.getLocation().reportSemanticError(MessageFormat.format(TIMERREFERENCEEXPECTED, assignment.getDescription()));
			break;
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (timerReference != null) {
			timerReference.updateSyntax(reparser, false);
			reparser.updateLocation(timerReference.getLocation());
		}

		if (timerValue instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) timerValue).updateSyntax(reparser, false);
			reparser.updateLocation(timerValue.getLocation());
		} else if (timerValue != null) {
			throw new ReParseException();
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (timerReference != null) {
			timerReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (timerValue != null) {
			timerValue.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (timerReference != null && !timerReference.accept(v)) {
			return false;
		}
		if (timerValue != null && !timerValue.accept(v)) {
			return false;
		}
		return true;
	}
}
