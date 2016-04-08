/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IAppendableSyntax;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.TTCN3Scope;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement.Statement_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.SkeletonTemplateProposal;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Keywords;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ITTCN3ReparseBase;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Reparser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The StatementBlock class represents TTCN3 statement block (the scope unit).
 * 
 * @author Kristof Szabados
 * */
// FIXME add support for determining if this statementblock has receiving
// statements or not.
public final class StatementBlock extends TTCN3Scope implements ILocateableNode, IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".statement_";
	private static final String INFINITELOOP = "Inifinite loop detected: the program can not escape from this goto statement";
	public static final String HIDINGSCOPEELEMENT = "Definition with identifier `{0}'' is not unique in the scope hierarchy";
	public static final String HIDDENSCOPEELEMENT = "Previous definition with identifier `{0}'' in higher scope unit is here";
	public static final String HIDINGMODULEIDENTIFIER = "Definition with name `{0}'' hides a module identifier";
	private static final String NEVER_REACH = "Control never reaches this statement";
	private static final String UNUSEDLABEL = "Label `{0}'' is defined, but not used";
	private static final String DUPLICATEDLABELFIRST = "Previous definition of label `{0}'' is here";
	private static final String DUPLICATELABELAGAIN = "Duplicated label `{0}''";

	private static final String EMPTY_STATEMENT_BLOCK = "Empty statement block";
	private static final String TOOMANYSTATEMENTS = "More than {0} statements in a single statementblock";

	public enum ReturnStatus_type {
		/** the block does not have a return statement */
		RS_NO,
		/**
		 * some branches of embedded statements have, some does not have
		 * return
		 */
		RS_MAYBE,
		/**
		 * the block or all branches of embedded statements have a
		 * return statement
		 */
		RS_YES
	}

	public enum ExceptionHandling_type {
		/** normal block */
		EH_NONE,
		/** try{} block */
		EH_TRY,
		/** catch{} block */
		EH_CATCH
	}

	private ExceptionHandling_type exceptionHandling = ExceptionHandling_type.EH_NONE;

	private Location location = NULL_Location.INSTANCE;

	private final List<Statement> statements;

	/** The definitions stored in the scope. */
	private Map<String, Definition> definitionMap;

	/** The labels stored in the scope. */
	private Map<String, Label_Statement> labelMap;

	/** the statementblock in which this statement block resides. */
	private StatementBlock myStatementBlock;
	private int myStatementBlockIndex;

	/** the definition containing this statement block. */
	private Definition myDefinition;

	/** the time when this statement block was check the last time. */
	private CompilationTimeStamp lastTimeChecked;

	/** Indicates if it is a statement block of a loop. */
	private boolean ownerIsLoop;

	/**
	 * Indicates if it is a statement block of an AltGuard (in alt,
	 * interleave, altstep, call).
	 */
	private boolean ownerIsAltguard;

	/**
	 * Caches whether this function has return statement or not. Used to
	 * speed up computation.
	 **/
	private ReturnStatus_type returnStatus;

	/**
	 * A quick cache for all the references escaping this statementblock.
	 * This way the statements can be freed, as we have impostors for the
	 * references.
	 * */
	private List<FakeReference> referencesGoingOut = new ArrayList<FakeReference>();

	/**
	 * stores whether the statements of this statementblock were freed or
	 * not.
	 */
	private boolean freed = false;

	private static final Comparator<Statement> STATEMENT_INSERTION_COMPARATOR = new Comparator<Statement>() {

		@Override
		public int compare(final Statement o1, final Statement o2) {
			return o1.getLocation().getOffset() - o2.getLocation().getOffset();
		}

	};

	/** whether memory usage minimalisation should be used or not. */
	protected static boolean minimiseMemoryUsage;
	/** whether to report the problem of an empty statement block */
	private static String reportEmptyStatementBlock;
	/** whether to report the problem of having too many parameters or not */
	private static String reportTooManyStatements;
	/** the amount that counts to be too many */
	private static int reportTooManyStatementsSize;

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			minimiseMemoryUsage = ps.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.MINIMISEMEMORYUSAGE, false, null);
			reportEmptyStatementBlock = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORT_EMPTY_STATEMENT_BLOCK, GeneralConstants.WARNING, null);
			reportTooManyStatements = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORT_TOOMANY_STATEMENTS, GeneralConstants.WARNING, null);
			reportTooManyStatementsSize = ps.getInt(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORT_TOOMANY_STATEMENTS_SIZE, 150, null);

			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.MINIMISEMEMORYUSAGE.equals(property)) {
							minimiseMemoryUsage = ps.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.MINIMISEMEMORYUSAGE, false, null);
						} else if (PreferenceConstants.REPORT_EMPTY_STATEMENT_BLOCK.equals(property)) {
							reportEmptyStatementBlock = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORT_EMPTY_STATEMENT_BLOCK, GeneralConstants.WARNING, null);
						} else if (PreferenceConstants.REPORT_TOOMANY_STATEMENTS.equals(property)) {
							reportTooManyStatements = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORT_TOOMANY_STATEMENTS,
									GeneralConstants.WARNING, null);
						} else if (PreferenceConstants.REPORT_TOOMANY_STATEMENTS_SIZE.equals(property)) {
							reportTooManyStatementsSize = ps.getInt(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORT_TOOMANY_STATEMENTS_SIZE, 150, null);
						}
					}
				});
			}
		}
	}

	private static final class FakeReference {
		String moduleName;
		String definition;
		boolean usedOnLeftSide;

		FakeReference(final Reference reference) {
			final Identifier moduleId = reference.getModuleIdentifier();
			if (moduleId != null) {
				moduleName = moduleId.getTtcnName();
			} else {
				moduleName = null;
			}

			final Identifier id = reference.getId();
			if (id != null) {
				definition = id.getTtcnName();
			} else {
				definition = null;
			}
			usedOnLeftSide = reference.getUsedOnLeftHandSide();
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}

			if (obj instanceof FakeReference) {
				final FakeReference other = (FakeReference) obj;
				if (moduleName == null) {
					if (other.moduleName != null) {
						return false;
					}
				} else if (!moduleName.equals(other.moduleName)) {
					return false;
				}

				if (definition == null) {
					if (other.definition != null) {
						return false;
					}
				} else if (!definition.equals(other.definition)) {
					return false;
				}

				return usedOnLeftSide == other.usedOnLeftSide;
			}

			return false;
		}

		@Override
		public int hashCode() {
			if (definition != null) {
				return definition.hashCode();
			}

			return super.hashCode();
		}
	}

	public void setExceptionHandling(final ExceptionHandling_type eh) {
		exceptionHandling = eh;
	}

	public ExceptionHandling_type getExceptionHandling() {
		return exceptionHandling;
	}

	public StatementBlock() {
		scopeName = "statementblock";
		statements = new ArrayList<Statement>();
		ownerIsLoop = false;
		ownerIsAltguard = false;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = statements.size(); i < size; i++) {
			if (statements.get(i) == child) {
				return builder.append(FULLNAMEPART).append(Integer.toString(i + 1));
			}
		}

		return builder;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	public Definition getMyDefinition() {
		return myDefinition;
	}

	/**
	 * Sets the definition in which this statement block resides.
	 * 
	 * @param definition
	 *                the definition to be set.
	 * */
	public void setMyDefinition(final Definition definition) {
		myDefinition = definition;
		for (int i = 0, size = statements.size(); i < size; i++) {
			statements.get(i).setMyDefinition(definition);
		}
	}

	/**
	 * Sets the scope in which this statementblock resides.
	 * <p>
	 * The scope to be set will become the parent scope of scope of this
	 * statementblock.
	 * 
	 * @param scope
	 *                the scope to be set.
	 * */
	public void setMyScope(final Scope scope) {
		setParentScope(scope);
		if (location != null) {
			scope.addSubScope(location, this);
		}
		for (int i = 0, size = statements.size(); i < size; i++) {
			statements.get(i).setMyScope(this);
		}
	}

	/**
	 * Sets indication that it is a statement block of a loop.
	 * */
	public void setOwnerIsLoop() {
		ownerIsLoop = true;
	}

	/**
	 * Sets indication that it is a statement block of an altguard.
	 * */
	public void setOwnerIsAltguard() {
		ownerIsAltguard = true;
	}

	public void addStatement(final Statement statement) {
		addStatement(statement, true);
	}

	/**
	 * Adds a statement to the list of statements stored in this statement
	 * block.
	 * <p>
	 * Statements of null value are not added to keep the semantic checks at
	 * a relatively low complexity (such statements are syntactically
	 * erroneous)
	 * 
	 * @param statement
	 *                the statement to be added.
	 * */
	public void addStatement(final Statement statement, final boolean append) {
		if (statement != null) {
			if (append) {
				statements.add(statement);
			} else {
				// add to front
				statements.add(0, statement);
			}
			statement.setMyStatementBlock(this, statements.size() - 1);
			statement.setMyScope(this);
			statement.setFullNameParent(this);
		}
	}

	/**
	 * Adds a list of new statements into the actual list of statement in an
	 * ordered fashion.
	 * 
	 * @param statements
	 *                the new list of statements to be merged with the
	 *                original.
	 * */
	void addStatementsOrdered(final List<Statement> statements) {
		if (statements == null || statements.isEmpty()) {
			return;
		}

		Statement statement;
		for (int i = 0, size = statements.size(); i < size; i++) {
			statement = statements.get(i);

			final int position = Collections.binarySearch(this.statements, statement, STATEMENT_INSERTION_COMPARATOR);

			if (position < 0) {
				this.statements.add((position + 1) * -1, statement);
			} else {
				this.statements.add(position + 1, statement);
			}

			statement.setMyScope(this);
			statement.setFullNameParent(this);
			statement.setMyDefinition(myDefinition);
		}
		// refresh indices
		for (int i = 0, size = this.statements.size(); i < size; i++) {
			statement = this.statements.get(i);

			statement.setMyStatementBlock(this, i);
		}
	}

	/**
	 * @return the number of statements in this statement block.
	 * */
	public int getSize() {
		return statements.size();
	}

	public Statement getStatementByIndex(final int i) {
		return statements.get(i);
	}

	public Statement getFirstStatement() {
		for (int i = 0, size = statements.size(); i < size; i++) {
			final Statement statement = statements.get(i);
			switch (statement.getType()) {
			case S_LABEL:
				// skip this statement
				break;
			case S_BLOCK: {
				final Statement firstStatement = ((StatementBlock_Statement) statement).getStatementBlock().getFirstStatement();
				if (firstStatement != null) {
					return firstStatement;
				}
				break;
			}
			case S_DOWHILE: {
				final Statement firstStatement = ((DoWhile_Statement) statement).getStatementBlock().getFirstStatement();
				if (firstStatement != null) {
					return firstStatement;
				}
				break;
			}
			default:
				return statement;
			}
		}

		return null;
	}

	/** @return the parent statement block */
	public StatementBlock getMyStatementBlock() {
		return myStatementBlock;
	}

	/**
	 * Sets the statementblock in which this statement was be found.
	 * 
	 * @param statementBlock
	 *                the statementblock containing this statement.
	 * @param index
	 *                the index of this statement in the statement block.
	 * */
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		myStatementBlock = statementBlock;
		myStatementBlockIndex = index;
		for (int i = 0, size = statements.size(); i < size; i++) {
			statements.get(i).setMyStatementBlock(this, i);
		}
	}

	/**
	 * @return the index of this statement block in its parent statement
	 *         block
	 */
	public int getMyStatementBlockIndex() {
		return myStatementBlockIndex;
	}

	public void setMyAltguards(final AltGuards altGuards) {
		for (int i = 0, size = statements.size(); i < size; i++) {
			statements.get(i).setMyAltguards(altGuards);
		}
	}

	/**
	 * Checks whether the statementblock has a return statement, either
	 * directly or embedded.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the return status of the statement block.
	 * */
	public ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && returnStatus != null) {
			return returnStatus;
		}

		returnStatus = ReturnStatus_type.RS_NO;

		for (int i = 0, size = statements.size(); i < size; i++) {
			Statement statement = statements.get(i);
			if (Statement_type.S_GOTO.equals(statement.getType())) {
				final Goto_statement gotoStatement = (Goto_statement) statement;
				if (gotoStatement.getJumpsForward()) {
					// heuristics without deep analysis of
					// the control flow graph:
					// skip over the next statements until a
					// (used) label is found
					// the behavior will be sound (i.e. no
					// false errors will be reported)
					for (i++; i < size; i++) {
						statement = statements.get(i);
						if (statement instanceof Label_Statement && ((Label_Statement) statement).labelIsUsed()) {
							break;
						}
					}
				} else {
					if (ReturnStatus_type.RS_NO.equals(returnStatus)) {
						statement.getLocation().reportConfigurableSemanticProblem(
								Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
										PreferenceConstants.REPORTINFINITELOOPS, GeneralConstants.WARNING,
										null), INFINITELOOP);
					}

					return returnStatus;
				}
			}
			if (Statement_type.S_BLOCK.equals(statement.getType())
					&& ((StatementBlock_Statement) statement).getStatementBlock().getExceptionHandling() != ExceptionHandling_type.EH_NONE) {
				switch (((StatementBlock_Statement) statement).getStatementBlock().getExceptionHandling()) {
				case EH_TRY:
					// the i-th statement is a try{} statement block,
					// the (i+1)-th must be a catch{} block
					if ((i + 1) < statements.size()
							&& Statement_type.S_BLOCK.equals(statements.get(i + 1).getType())
							&& ((StatementBlock_Statement) statements.get(i + 1)).getStatementBlock()
									.getExceptionHandling() == ExceptionHandling_type.EH_CATCH) {
						final ReturnStatus_type tryBlockReturnStatus = statement.hasReturn(timestamp);
						final ReturnStatus_type catchBlockReturnStatus = statements.get(i + 1).hasReturn(timestamp);
						// 3 x 3 combinations
						if (tryBlockReturnStatus == catchBlockReturnStatus) {
							switch (tryBlockReturnStatus) {
							case RS_YES:
								return ReturnStatus_type.RS_YES;
							case RS_MAYBE:
								returnStatus = ReturnStatus_type.RS_MAYBE;
								break;
							default:
								break;
							}
						} else {
							returnStatus = ReturnStatus_type.RS_MAYBE;
						}
					} else {
						// if next statement is not a
						// catch{} block
						// then that error has already
						// been reported.
						// Assume the catch block was an
						// RS_MAYBE
						returnStatus = ReturnStatus_type.RS_MAYBE;
					}
					break;
				case EH_CATCH:
					// logically this is part of the
					// preceding try{} block, handle it as
					// part of it, see above case EH_TRY
					break;
				default:
					ErrorReporter.INTERNAL_ERROR();
				}
			} else {
				switch (statement.hasReturn(timestamp)) {
				case RS_YES:
					returnStatus = ReturnStatus_type.RS_YES;
					break;
				case RS_MAYBE:
					returnStatus = ReturnStatus_type.RS_MAYBE;
					break;
				default:
					break;
				}
			}
		}

		return returnStatus;
	}

	/**
	 * Registers a definition (for example new variable) into the list of
	 * definitions available in this statement block.
	 * 
	 * Please note, that this is done while the semantic check is happening,
	 * as it must not be allowed to reach definitions not yet defined.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param definition
	 *                the definition to register.
	 * */
	public void registerDefinition(final CompilationTimeStamp timestamp, final Definition definition) {
		if (definition == null) {
			return;
		}

		final Identifier identifier = definition.getIdentifier();
		if (identifier == null) {
			return;
		}

		if (definitionMap == null) {
			definitionMap = new HashMap<String, Definition>(3);
		}

		final String definitionName = identifier.getName();
		if (definitionMap.containsKey(definitionName)) {
			if (definition.getLocation() != null && definitionMap.get(definitionName).getLocation() != null) {
				final Location otherLocation = definitionMap.get(definitionName).getLocation();
				otherLocation.reportSingularSemanticError(MessageFormat.format(Assignments.DUPLICATEDEFINITIONFIRST,
						identifier.getDisplayName()));
				definition.getLocation().reportSemanticError(
						MessageFormat.format(Assignments.DUPLICATEDEFINITIONREPEATED, identifier.getDisplayName()));
			}
		} else {
			definitionMap.put(definitionName, definition);
			if (parentScope != null && definition.getLocation() != null) {
				if (parentScope.hasAssignmentWithId(timestamp, identifier)) {
					definition.getLocation().reportSemanticError(
							MessageFormat.format(HIDINGSCOPEELEMENT, identifier.getDisplayName()));

					final List<ISubReference> subReferences = new ArrayList<ISubReference>();
					subReferences.add(new FieldSubReference(identifier));
					final Reference reference = new Reference(null, subReferences);
					final Assignment assignment = parentScope.getAssBySRef(timestamp, reference);
					if (assignment != null && assignment.getLocation() != null) {
						assignment.getLocation().reportSingularSemanticError(
								MessageFormat.format(HIDDENSCOPEELEMENT, identifier.getDisplayName()));
					}
				} else if (parentScope.isValidModuleId(identifier)) {
					definition.getLocation().reportSemanticWarning(
							MessageFormat.format(HIDINGMODULEIDENTIFIER, identifier.getDisplayName()));
				}
			}
		}
	}

	/**
	 * Check that a try{} block is followed by a catch{} block and a catch{}
	 * block is preceded by a try{} block
	 */
	void checkTrycatchBlocks(final Statement s1, final Statement s2) {
		if (s1 != null && Statement_type.S_BLOCK.equals(s1.getType())
				&& ((StatementBlock_Statement) s1).getStatementBlock().getExceptionHandling() == ExceptionHandling_type.EH_TRY) {
			if (!(s2 != null && Statement_type.S_BLOCK.equals(s2.getType()) && ((StatementBlock_Statement) s2).getStatementBlock()
					.getExceptionHandling() == ExceptionHandling_type.EH_CATCH)) {
				s1.getLocation().reportSemanticError("`@try' statement block must be followed by a `@catch' block");
			}
		}
		if (s2 != null && Statement_type.S_BLOCK.equals(s2.getType())
				&& ((StatementBlock_Statement) s2).getStatementBlock().getExceptionHandling() == ExceptionHandling_type.EH_CATCH) {
			if (!(s1 != null && Statement_type.S_BLOCK.equals(s1.getType()) && ((StatementBlock_Statement) s1).getStatementBlock()
					.getExceptionHandling() == ExceptionHandling_type.EH_TRY)) {
				s2.getLocation().reportSemanticError("`@catch' statement block must be preceded by a `@try' block");
			}
		}
	}

	/**
	 * Does the semantic checking of the statement block.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (freed && minimiseMemoryUsage) {
			MarkerHandler.reEnableAllSemanticMarkers((IFile) location.getFile(), location.getOffset(), location.getEndOffset());
			final List<FakeReference> copy = new ArrayList<FakeReference>(referencesGoingOut);
			for (final FakeReference reference : copy) {
				Reference tempRef;
				if (reference.moduleName != null) {
					tempRef = new Reference(new Identifier(Identifier_type.ID_TTCN, reference.moduleName));
				} else {
					tempRef = new Reference(null);
				}
				tempRef.addSubReference(new FieldSubReference(new Identifier(Identifier_type.ID_TTCN, reference.definition)));
				tempRef.setMyScope(this);
				final Assignment assignment = tempRef.getRefdAssignment(timestamp, false);
				if (assignment == null) {
					return;
				}

				if (reference.usedOnLeftSide) {
					switch (assignment.getAssignmentType()) {
					case A_VAR_TEMPLATE:
						((Def_Var_Template) assignment).setWritten();
						break;
					case A_VAR:
						((Def_Var) assignment).setWritten();
						break;
					case A_PAR_TEMP_OUT:
					case A_PAR_TEMP_INOUT:
					case A_PAR_VAL_OUT:
					case A_PAR_VAL_INOUT:
					case A_PAR_VAL:
						((FormalParameter) assignment).setWritten();
						break;
					default:
						break;
					}
				}
			}

			return;
		}

		referencesGoingOut.clear();

		if (definitionMap != null) {
			definitionMap.clear();
		}
		if (labelMap != null) {
			labelMap.clear();
		}

		checkLabels(timestamp);

		boolean unreachableFound = false;
		Statement previousStatement = null;
		for (int i = 0, size = statements.size(); i < size; i++) {
			final Statement statement = statements.get(i);
			try {
				statement.check(timestamp);
			} catch (Exception e) {
				final Location loc = statement.getLocation();
				ErrorReporter.logExceptionStackTrace("An exception was thrown when analyzing the statement in file '"
						+ loc.getFile().getLocationURI() + "' at line " + loc.getLine(), e);
			}

			if (!unreachableFound && !Statement_type.S_LABEL.equals(statement.getType()) && previousStatement != null
					&& previousStatement.isTerminating(timestamp)) {
				// a statement is unreachable if:
				// - it is not a label (i.e. goto cannot jump to
				// it)
				// - it is not the first statement of the block
				// - the previous statement terminates the
				// control flow
				statement.getLocation().reportSemanticWarning(NEVER_REACH);
				unreachableFound = true;
			}
			// check try-catch statement block usage
			checkTrycatchBlocks(previousStatement, statement);
			previousStatement = statement;
		}
		checkTrycatchBlocks(previousStatement, null);

		if (statements.isEmpty()) {
			getLocation().reportConfigurableSemanticProblem(reportEmptyStatementBlock, EMPTY_STATEMENT_BLOCK);
		} else if (statements.size() > reportTooManyStatementsSize) {
			getLocation().reportConfigurableSemanticProblem(reportTooManyStatements,
					MessageFormat.format(TOOMANYSTATEMENTS, reportTooManyStatementsSize));
		}

		checkUnusedLabels(timestamp);

		lastTimeChecked = timestamp;
	}

	/**
	 * Checks the properties of the statement block, that can only be
	 * checked after the semantic check was completely run.
	 * */
	public void postCheck() {
		if (statements.isEmpty()) {
			return;
		}

		for (int i = 0, size = statements.size(); i < size; i++) {
			statements.get(i).postCheck();
		}
	}

	/**
	 * Free up the statements stored inside this statement block.
	 * */
	public void free() {
		if (minimiseMemoryUsage && !freed) {
			freed = true;

			statements.clear();
			if (definitionMap != null) {
				definitionMap.clear();
			}
			if (labelMap != null) {
				labelMap.clear();
			}
		}
	}

	/**
	 * Pre-check the labels for duplicates and also sets them unused.
	 * 
	 * @param timestamp
	 *                the actual semantic check cycle
	 * */
	private void checkLabels(final CompilationTimeStamp timestamp) {
		for (int i = 0, size = statements.size(); i < size; i++) {
			final Statement statement = statements.get(i);
			if (Statement_type.S_LABEL.equals(statement.getType())) {
				final Label_Statement labelStatement = (Label_Statement) statement;
				labelStatement.setUsed(false);
				final Identifier identifier = labelStatement.getLabelIdentifier();
				if (hasLabel(identifier)) {
					statement.getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATELABELAGAIN, identifier.getDisplayName()));
					final Statement statement2 = getLabel(identifier);
					statement2.getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATEDLABELFIRST, identifier.getDisplayName()));
				} else {
					if (labelMap == null) {
						labelMap = new HashMap<String, Label_Statement>(1);
					}
					labelMap.put(identifier.getName(), labelStatement);
				}
			}
		}
	}

	/**
	 * Post-checks the label for ones that were not used.
	 * 
	 * @param timestamp
	 *                the actual semantic check cycle
	 * */
	private void checkUnusedLabels(final CompilationTimeStamp timestamp) {
		for (int i = 0, size = statements.size(); i < size; i++) {
			final Statement statement = statements.get(i);
			if (Statement_type.S_LABEL.equals(statement.getType())) {
				final Label_Statement labelStatement = (Label_Statement) statement;
				if (!labelStatement.labelIsUsed()) {
					statement.getLocation().reportSemanticError(
							MessageFormat.format(UNUSEDLABEL, labelStatement.getLabelIdentifier().getDisplayName()));
				}
			}
		}
	}

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		for (int i = 0, size = statements.size(); i < size; i++) {
			statements.get(i).checkAllowedInterleave();
		}
	}

	/**
	 * Checks if this statement block or any of its parents has a label
	 * declared with a name.
	 * 
	 * @param identifier
	 *                the identifier of the label to search for
	 * 
	 * @return true if a label with the given name exists, false otherwise
	 * */
	protected boolean hasLabel(final Identifier identifier) {
		for (StatementBlock statementBlock = this; statementBlock != null; statementBlock = statementBlock.myStatementBlock) {
			if (statementBlock.labelMap != null && statementBlock.labelMap.containsKey(identifier.getName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if this statement block or any of its parents has a label
	 * declared with a name.
	 * 
	 * @param identifier
	 *                the identifier of the label to search for
	 * 
	 * @return the {@link Label_Statement} having the provided name, or null
	 *         if none was found.
	 * */
	protected Label_Statement getLabel(final Identifier identifier) {
		for (StatementBlock statementBlock = this; statementBlock != null; statementBlock = statementBlock.myStatementBlock) {
			if (statementBlock.labelMap != null && statementBlock.labelMap.containsKey(identifier.getName())) {
				return statementBlock.labelMap.get(identifier.getName());
			}
		}

		return null;
	}

	/**
	 * @return indication if the statement block is enclosed by a loop.
	 * */
	public boolean hasEnclosingLoop() {
		return ownerIsLoop || (myStatementBlock != null && myStatementBlock.hasEnclosingLoop());
	}

	/**
	 * @return indication if the statement block is enclosed by an altguard.
	 * */
	public boolean hasEnclosingLoopOrAltguard() {
		return ownerIsLoop || ownerIsAltguard || (myStatementBlock != null && myStatementBlock.hasEnclosingLoopOrAltguard());
	}

	@Override
	public StatementBlock getStatementBlockScope() {
		return this;
	}

	@Override
	public Component_Type getMtcSystemComponentType(final CompilationTimeStamp timestamp, final boolean isSystem) {
		if (myDefinition == null || !Assignment_type.A_TESTCASE.equals(myDefinition.getAssignmentType())) {
			return null;
		}

		final Def_Testcase testcase = (Def_Testcase) myDefinition;
		if (isSystem) {
			final Component_Type type = testcase.getSystemType(timestamp);
			if (type != null) {
				return type;
			}
			// if the system clause is not set the type of the
			// `system' is the same as the type of the `mtc'
		}

		return testcase.getRunsOnType(timestamp);
	}

	@Override
	public boolean hasAssignmentWithId(final CompilationTimeStamp timestamp, final Identifier identifier) {
		if (definitionMap != null && definitionMap.containsKey(identifier.getName())) {
			return true;
		}
		if (parentScope != null) {
			return parentScope.hasAssignmentWithId(timestamp, identifier);
		}
		return false;
	}

	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference) {
		if (reference.getModuleIdentifier() != null || definitionMap == null) {
			if (minimiseMemoryUsage) {
				final FakeReference fakeReference = new FakeReference(reference);
				if (!referencesGoingOut.contains(fakeReference)) {
					referencesGoingOut.add(fakeReference);
				}
			}

			return getParentScope().getAssBySRef(timestamp, reference);
		}

		final Assignment assignment = definitionMap.get(reference.getId().getName());
		if (assignment != null) {
			return assignment;
		}

		if (minimiseMemoryUsage) {
			final FakeReference fakeReference = new FakeReference(reference);
			if (!referencesGoingOut.contains(fakeReference)) {
				referencesGoingOut.add(fakeReference);
			}
		}

		return getParentScope().getAssBySRef(timestamp, reference);
	}

	@Override
	public void addProposal(final ProposalCollector propCollector) {
		if (definitionMap != null && propCollector.getReference().getModuleIdentifier() == null) {
			final HashMap<String, Definition> temp = new HashMap<String, Definition>(definitionMap);
			for (final Definition definition : temp.values()) {
				definition.addProposal(propCollector, 0);
			}
		}
		if (labelMap != null && propCollector.getReference().getModuleIdentifier() == null) {
			final HashMap<String, Label_Statement> temp = new HashMap<String, Label_Statement>(labelMap);
			for (final String name : temp.keySet()) {
				propCollector.addProposal(name, name, null);
			}
		}

		super.addProposal(propCollector);
	}

	@Override
	public void addSkeletonProposal(final ProposalCollector propCollector) {
		for (final SkeletonTemplateProposal templateProposal : TTCN3CodeSkeletons.STATEMENT_LEVEL_SKELETON_PROPOSALS) {
			propCollector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(),
					TTCN3CodeSkeletons.SKELETON_IMAGE);
		}
	}

	@Override
	public void addKeywordProposal(final ProposalCollector propCollector) {
		propCollector.addProposal(TTCN3Keywords.STATEMENT_SCOPE, null, TTCN3Keywords.KEYWORD);
		super.addKeywordProposal(propCollector);
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector) {
		if (definitionMap != null && declarationCollector.getReference().getModuleIdentifier() == null) {
			final String name = declarationCollector.getReference().getId().getName();
			if (definitionMap.containsKey(name)) {
				declarationCollector.addDeclaration(name, definitionMap.get(name).getLocation(), this);
			}
		}
		if (labelMap != null && declarationCollector.getReference().getModuleIdentifier() == null) {
			final String name = declarationCollector.getReference().getId().getName();
			if (labelMap.containsKey(name)) {
				declarationCollector.addDeclaration(name, labelMap.get(name).getLocation(), this);
			}
		}
		super.addDeclaration(declarationCollector);
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (!isDamaged) {
			// handle the simple case quickly
			for (int i = 0, size = statements.size(); i < size; i++) {
				final Statement statement = statements.get(i);

				statement.updateSyntax(reparser, false);
				reparser.updateLocation(statement.getLocation());
			}

			return;
		}

		freed = false;
		returnStatus = null;
		lastTimeChecked = null;
		boolean enveloped = false;
		int nofDamaged = 0;
		int leftBoundary = location.getOffset() + 1;
		int rightBoundary = location.getEndOffset() - 1;
		final int damageOffset = reparser.getDamageStart();
		IAppendableSyntax lastAppendableBeforeChange = null;
		IAppendableSyntax lastPrependableBeforeChange = null;

		for (int i = 0, size = statements.size(); i < size && !enveloped; i++) {
			final Statement statement = statements.get(i);
			final Location temporalLocation = statement.getLocation();

			if (reparser.envelopsDamage(temporalLocation)) {
				enveloped = true;
				leftBoundary = temporalLocation.getOffset();
				rightBoundary = temporalLocation.getEndOffset();
			} else if (reparser.isDamaged(temporalLocation)) {
				nofDamaged++;
				if (reparser.getDamageStart() == temporalLocation.getEndOffset()) {
					lastAppendableBeforeChange = statement;
				} else if (reparser.getDamageEnd() == temporalLocation.getOffset()) {
					lastPrependableBeforeChange = statement;
				}
			} else {
				if (temporalLocation.getEndOffset() < damageOffset && temporalLocation.getEndOffset() > leftBoundary) {
					leftBoundary = temporalLocation.getEndOffset() + 1;
					lastAppendableBeforeChange = statement;
				}
				if (temporalLocation.getOffset() >= damageOffset && temporalLocation.getOffset() < rightBoundary) {
					rightBoundary = temporalLocation.getOffset();
					lastPrependableBeforeChange = statement;
				}
			}
		}

		// extend the reparser to the calculated values if the damage
		// was not enveloped
		if (!enveloped) {
			reparser.extendDamagedRegion(leftBoundary, rightBoundary);
		}

		// if there is a component field that is right now being
		// extended we should add it to the damaged domain as the
		// extension might be correct
		if (lastAppendableBeforeChange != null) {
			final boolean isBeingExtended = reparser.startsWithFollow(lastAppendableBeforeChange.getPossibleExtensionStarterTokens());
			if (isBeingExtended) {
				leftBoundary = lastAppendableBeforeChange.getLocation().getOffset();
				nofDamaged++;
				enveloped = false;
				reparser.extendDamagedRegion(leftBoundary, rightBoundary);
			}
		}

		if (lastPrependableBeforeChange != null) {
			final List<Integer> temp = lastPrependableBeforeChange.getPossiblePrefixTokens();

			if (temp != null && reparser.endsWithToken(temp)) {
				rightBoundary = lastPrependableBeforeChange.getLocation().getEndOffset();
				nofDamaged++;
				enveloped = false;
				reparser.extendDamagedRegion(leftBoundary, rightBoundary);
			}
		}

		if (nofDamaged != 0) {
			removeStuffInRange(reparser);
		}

		for (final Iterator<Statement> iterator = statements.iterator(); iterator.hasNext();) {
			final Statement statement = iterator.next();
			final Location temporalLocation = statement.getLocation();

			if (reparser.isAffectedAppended(temporalLocation)) {
				try {
					statement.updateSyntax(reparser, enveloped && reparser.envelopsDamage(temporalLocation));
					reparser.updateLocation(statement.getLocation());
				} catch (ReParseException e) {
					if (e.getDepth() == 1) {
						enveloped = false;
						iterator.remove();
						reparser.extendDamagedRegion(temporalLocation);
					} else {
						e.decreaseDepth();
						throw e;
					}
				}
			}
		}

		if (!enveloped) {
			reparser.extendDamagedRegion(leftBoundary, rightBoundary);
			final int result = reparse( reparser );
			if (result > 1) {
				throw new ReParseException(result - 1);
			}
		}
	}
	
	private int reparse( final TTCN3ReparseUpdater aReparser ) {
		return aReparser.parse(new ITTCN3ReparseBase() {
			@Override
			public void reparse(final Ttcn3Reparser parser) {
				final List<Statement> statements = parser.pr_reparse_FunctionStatementOrDefList().statements;
				if ( parser.isErrorListEmpty() ) {
					if (statements != null) {
						addStatementsOrdered(statements);
					}
				}
			}
		});
	}

	private void removeStuffInRange(final TTCN3ReparseUpdater reparser) {
		Location temp;
		for (int i = statements.size() - 1; i >= 0; i--) {
			temp = statements.get(i).getLocation();
			if (reparser.isDamaged(temp)) {
				reparser.extendDamagedRegion(temp);
				statements.remove(i);
			}
		}
	}

	@Override
	public Assignment getEnclosingAssignment(final int offset) {
		if (definitionMap == null) {
			return null;
		}
		for (final Definition definition : definitionMap.values()) {
			if (definition.getLocation().containsOffset(offset)) {
				return definition;
			}
		}
		return null;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (statements == null) {
			return;
		}

		final List<Statement> tempList = new ArrayList<Statement>(statements);
		for (final Statement statement : tempList) {
			statement.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	public boolean accept(final ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (statements != null) {
			for (final Statement statement : statements) {
				if (!statement.accept(v)) {
					return false;
				}
			}
		}
		// TODO: labelMap
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}

	public boolean isEmpty() {
		return statements.isEmpty();
	}

}
