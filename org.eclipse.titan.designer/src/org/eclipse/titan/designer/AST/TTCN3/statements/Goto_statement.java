/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
public final class Goto_statement extends Statement {
	private static final String UNDEFINEDLABELUSED = "Label `{0}'' is used, but not defined";
	private static final String LOCALDEFINITIONCROSSING = "Jump to label `{0}'' crosses a local definition";
	private static final String CROSSEDDEFINITION = "The Definition crossed by label `{0}'' is here";

	private static final String STATEMENT_NAME = "goto";

	private final Identifier identifier;

	/** The index of this statement in its parent statement block. */
	private int statementIndex;

	/** stores if this goto jumps forward or backward. */
	private boolean jumpsForward;

	// only needed by the code generator
	// private Statement label_statement;

	// The actual value of the severity level to report stricter constant
	// checking on.
	protected static String banishGOTO;

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			banishGOTO = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORT_GOTO,
					GeneralConstants.WARNING, null);

			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.REPORT_GOTO.equals(property)) {
							banishGOTO = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORT_GOTO, GeneralConstants.WARNING, null);
						}
					}
				});
			}
		}
	}

	public Goto_statement(final Identifier identifier) {
		this.identifier = identifier;
		statementIndex = 0;
		jumpsForward = false;
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_GOTO;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		statementIndex = index;
	}

	/** @return the index of this statement in its parent statement block */
	public int getMyStatementBlockIndex() {
		return statementIndex;
	}

	public boolean getJumpsForward() {
		return jumpsForward;
	}

	@Override
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		return true;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (!myStatementBlock.hasLabel(identifier)) {
			location.reportSemanticError(MessageFormat.format(UNDEFINEDLABELUSED, identifier.getDisplayName()));
			lastTimeChecked = timestamp;
			return;
		}

		final Label_Statement labelStatement = myStatementBlock.getLabel(identifier);
		labelStatement.setUsed(true);
		final StatementBlock labelStatementBlock = labelStatement.getMyStatementBlock();
		final int labelIndex = labelStatement.getMyStatementBlockIndex();

		int gotoIndex;
		if (myStatementBlock == labelStatementBlock) {
			gotoIndex = statementIndex;
		} else {
			StatementBlock gotoStatementBlock = myStatementBlock;
			StatementBlock parentStatementBlock = myStatementBlock.getMyStatementBlock();
			while (parentStatementBlock != labelStatementBlock) {
				gotoStatementBlock = parentStatementBlock;
				parentStatementBlock = gotoStatementBlock.getMyStatementBlock();
			}
			gotoIndex = gotoStatementBlock.getMyStatementBlockIndex();
		}

		if (labelIndex > gotoIndex) {
			final boolean errorFound = false;
			for (int i = gotoIndex + 1; i < labelIndex && !errorFound; i++) {
				final 	Statement statement = labelStatementBlock.getStatementByIndex(i);
				if (Statement_type.S_DEF.equals(statement.getType())) {
					location.reportSemanticError(MessageFormat.format(LOCALDEFINITIONCROSSING, identifier.getDisplayName()));
					statement.getLocation().reportSemanticError(
							MessageFormat.format(CROSSEDDEFINITION, identifier.getDisplayName()));
				}
			}
			jumpsForward = true;
		} else {
			// TODO infinite loop detection could be done here
			jumpsForward = false;
		}

		if (!GeneralConstants.IGNORE.equals(banishGOTO)) {
			location.reportConfigurableSemanticProblem(banishGOTO,
					"Usage of goto and label statements is not recommended as they usually break the structure of the code");
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void checkAllowedInterleave() {
		location.reportSemanticError("Goto statement is not allowed within an interleave statement");
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		// label renaming not supported yet (label is not a definition)
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		return true;
	}
}
