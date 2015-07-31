/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a macro value.
 * 
 * @author Kristof Szabados
 */
public final class Macro_Value extends Value {
	private static final String TESCASEIDINCONTROLPART =
			"Macro %testcaseId cannot be used in the control part. "
			+ "It is allowed only within the statement blocks of function, altsteps and testcases";
	private static final String TESTCASEIDNOTALLOWED =
			"Usage of macro %testcaseId is allowed only within the statement blocks of function, altsteps and testcases";
	private static final String UNDETERMINABLEPATH = "The path can not be determined";
	private static final String UNDETERMINABLESCOPE = "The value of the __SCOPE__ can not be determined";

	public enum Macro_type {
		MODULEID, SCOPE, DEFINITIONID, TESTCASEID, FILENAME, BFILENAME, FILEPATH, LINENUMBER, LINENUMBER_C
	}

	private final Macro_type value;

	public Macro_Value(final Macro_type value) {
		this.value = value;
	}

	@Override
	public Value_type getValuetype() {
		return Value_type.MACRO_VALUE;
	}

	@Override
	public String createStringRepresentation() {
		switch (value) {
		case MODULEID:
			return "%moduleId";
		case FILENAME:
			return "%fileName";
		case BFILENAME:
			return "__BFILE__";
		case FILEPATH:
			return "__FILE__";
		case LINENUMBER:
			return "%lineNumber";
		case LINENUMBER_C:
			return "__LINE__";
		case DEFINITIONID:
			return "%definitionId";
		case SCOPE:
			return "__SCOPE__";
		case TESTCASEID:
			return "%testcaseId";
		default:
			return "<unknown macro>";
		}
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (value == null) {
			return Type_type.TYPE_CHARSTRING;
		}

		switch (value) {
		case LINENUMBER_C:
			return Type_type.TYPE_INTEGER;
		default:
			return Type_type.TYPE_CHARSTRING;
		}
	}

	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		return Value_type.MACRO_VALUE.equals(last.getValuetype()) && value.equals(((Macro_Value) last).value);
	}

	@Override
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference,
			final int actualSubReference, final IReferenceChain refChain) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		IValue result = getValueRefdLast(timestamp, refChain);
		if (result != null && result != this) {
			result = result.getReferencedSubValue(timestamp, reference, actualSubReference, refChain);
			if (result != null && result.getIsErroneous(timestamp)) {
				setIsErroneous(true);
			}
		}

		return this;
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (Macro_type.TESTCASEID.equals(value)) {
			return true;
		}

		return false;
	}

	@Override
	public IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return evaluateMacro(expectedValue);
	}

	/**
	 * Evaluates the value of the macro.
	 *
	 * @param expectedValue the kind of the value to be expected
	 *
	 * @return the actual or the evaluated value
	 * */
	private IValue evaluateMacro(final Expected_Value_type expectedValue) {
		IValue lastValue = this;

		switch (value) {
		case MODULEID:
			if (myScope != null && myScope.getModuleScope() != null) {
				Module module = myScope.getModuleScope();
				if (module.getIdentifier() != null) {
					lastValue = new Charstring_Value(module.getIdentifier().getDisplayName());
					lastValue.copyGeneralProperties(this);
				}
			}
			break;
		case DEFINITIONID:
			if (myScope != null) {
				StatementBlock block = myScope.getStatementBlockScope();
				if (block != null) {
					Definition definition = block.getMyDefinition();
					if (definition != null) {
						lastValue = new Charstring_Value(definition.getIdentifier().getDisplayName());
						lastValue.copyGeneralProperties(this);
					}
				}
			} else {
				setIsErroneous(true);
			}
			break;
		case TESTCASEID:
			if (myScope != null) {
				StatementBlock block = myScope.getStatementBlockScope();
				if (block != null) {
					Definition definition = block.getMyDefinition();
					if (definition == null) {
						location.reportSemanticError(TESCASEIDINCONTROLPART);
						setIsErroneous(true);
					} else {
						if (Assignment_type.A_TESTCASE.equals(definition.getAssignmentType())) {
							// folding is possible in testcases only
							lastValue = new Charstring_Value(definition.getIdentifier().getDisplayName());
							lastValue.copyGeneralProperties(this);
						}
					}
				} else {
					location.reportSemanticError(TESTCASEIDNOTALLOWED);
					setIsErroneous(true);
				}
			} else {
				setIsErroneous(true);
			}
			break;
		case FILENAME:
		case BFILENAME:
			if (NULL_Location.INSTANCE.equals(location)) {
				setIsErroneous(true);
			} else {
				lastValue = new Charstring_Value(location.getFile().getName());
				lastValue.copyGeneralProperties(this);
			}
			break;
		case FILEPATH:
			if (NULL_Location.INSTANCE.equals(location)) {
				setIsErroneous(true);
			} else {
				String canonicalPath;
				IPath absolutePath = location.getFile().getLocation();
				if (absolutePath == null) {
					location.reportSemanticError(UNDETERMINABLEPATH);
					canonicalPath = location.getFile().getName();
					setIsErroneous(true);
				} else {
					File file = absolutePath.toFile();
					try {
						canonicalPath = file.getCanonicalPath();
					} catch (IOException e) {
						location.reportSemanticError(UNDETERMINABLEPATH);
						canonicalPath = location.getFile().getName();
						setIsErroneous(true);
					}
				}
				lastValue = new Charstring_Value(canonicalPath);
				lastValue.copyGeneralProperties(this);
			}
			break;
		case LINENUMBER:
			if (NULL_Location.INSTANCE.equals(location)) {
				setIsErroneous(true);
			} else {
				lastValue = new Charstring_Value(Long.toString(location.getLine()));
				lastValue.copyGeneralProperties(this);
			}
			break;
		case LINENUMBER_C:
			if (NULL_Location.INSTANCE.equals(location)) {
				setIsErroneous(true);
			} else {
				lastValue = new Integer_Value(location.getLine());
				lastValue.copyGeneralProperties(this);
			}
			break;
		case SCOPE:
			if (myScope != null) {
				lastValue = new Charstring_Value(myScope.getScopeMacroName());
				lastValue.copyGeneralProperties(this);
			} else {
				location.reportSemanticError(UNDETERMINABLESCOPE);
				setIsErroneous(true);
			}
			break;
		default:
			setIsErroneous(true);
		}

		return lastValue;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		// no members
		return true;
	}
}
