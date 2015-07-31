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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value.Verdict_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
/**
 * @author Kristof Szabados
 * */
public final class Setverdict_Statement extends Statement {
	private static final String OPERANDERROR = "The operand of the `setverdict' operation should be a verdict value";
	private static final String INCONTROLPART = "Setverdict statement is not allowed in the control part";
	private static final String ERRORCANNOTBESET = "Error verdict cannot be set by the setverdict operation";
	private static final String WITHOUT_REASON = "{0} verdict should not be set without telling the reason";

	private static final String FULLNAMEPART1 = ".verdictvalue";
	private static final String FULLNAMEPART2 = ".verdictreason";
	private static final String STATEMENT_NAME = "setverdict";

	private final Value verdictValue;
	private LogArguments verdictReason;

	/** whether to report the problem of not having an else branch */
	private static String reportSetverdictWithoutReason;

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			reportSetverdictWithoutReason = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORT_SETVERDICT_WITHOUT_REASON, GeneralConstants.IGNORE, null);

			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.REPORT_SETVERDICT_WITHOUT_REASON.equals(property)) {
							reportSetverdictWithoutReason = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORT_SETVERDICT_WITHOUT_REASON, GeneralConstants.IGNORE, null);
						}
					}
				});
			}
		}
	}

	public Setverdict_Statement(final Value verdictValue, final LogArguments verdict_reason) {
		this.verdictValue = verdictValue;
		this.verdictReason = verdict_reason;

		if (verdictValue != null) {
			verdictValue.setFullNameParent(this);
		}
		if (verdict_reason != null) {
			this.verdictReason.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_SETVERDICT;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (verdictValue == child) {
			return builder.append(FULLNAMEPART1);
		} else if (verdictReason == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (verdictValue != null) {
			verdictValue.setMyScope(scope);
		}
		if (verdictReason != null) {
			verdictReason.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (myStatementBlock.getMyDefinition() == null) {
			location.reportSemanticError(INCONTROLPART);
		}

		if (verdictValue != null) {
			verdictValue.setLoweridToReference(timestamp);
			Type_type temp = verdictValue.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

			switch (temp) {
			case TYPE_VERDICT:
				IValue last = verdictValue.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
				if (Value_type.VERDICT_VALUE.equals(last.getValuetype())
						&& Verdict_type.ERROR.equals(((Verdict_Value) last).getValue())) {
					getLocation().reportSemanticError(ERRORCANNOTBESET);
				}

				if (Value_type.VERDICT_VALUE.equals(last.getValuetype())
						&& !Verdict_type.PASS.equals(((Verdict_Value) last).getValue()) && verdictReason == null) {
					getLocation().reportConfigurableSemanticProblem(reportSetverdictWithoutReason,
							MessageFormat.format(WITHOUT_REASON, ((Verdict_Value) last).getValue()));
				}
				break;
			default:
				location.reportSemanticError(OPERANDERROR);
				verdictValue.setIsErroneous(true);
				break;
			}

		}

		if (verdictReason != null) {
			verdictReason.check(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (verdictValue != null) {
			verdictValue.updateSyntax(reparser, false);
			reparser.updateLocation(verdictValue.getLocation());
		}

		if (verdictReason != null) {
			verdictReason.updateSyntax(reparser, false);
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (verdictValue != null) {
			verdictValue.findReferences(referenceFinder, foundIdentifiers);
		}
		if (verdictReason != null) {
			verdictReason.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (verdictValue != null && !verdictValue.accept(v)) {
			return false;
		}
		if (verdictReason != null && !verdictReason.accept(v)) {
			return false;
		}
		return true;
	}

	public Value getVerdictValue() {
		return verdictValue;
	}

	public LogArguments getVerdictReason() {
		return verdictReason;
	}
}
