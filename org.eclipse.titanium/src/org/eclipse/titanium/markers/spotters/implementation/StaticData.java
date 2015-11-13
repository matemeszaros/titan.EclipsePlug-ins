/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titanium.TypeHierarchy;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.spotters.BaseProjectCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public final class StaticData {
	/**
	 * This data structure is actually a tree, containing the type hierarchy of
	 * AST nodes.
	 */
	public static final Map<Class<? extends IVisitableNode>, Class<? extends IVisitableNode>[]> TYPE_HIERARCHY = TypeHierarchy.createHierarchy();

	private StaticData() {
		//disabled constructor
	}

	/**
	 * This map contains the code smell spotters, that are related to each
	 * semantic problem.
	 */
	public static Map<CodeSmellType, BaseModuleCodeSmellSpotter[]> newSpotters() {
		Map<CodeSmellType, BaseModuleCodeSmellSpotter[]> m = new HashMap<CodeSmellType, BaseModuleCodeSmellSpotter[]>();

		m.put(CodeSmellType.ALTSTEP_COVERAGE, new BaseModuleCodeSmellSpotter[] { new AltstepCoverage.OnAltstep(),
				new AltstepCoverage.OnAltStatement() });
		m.put(CodeSmellType.CONSECUTIVE_ASSIGNMENTS, new BaseModuleCodeSmellSpotter[] { new ConsecutiveAssignments() });
		m.put(CodeSmellType.CONVERT_TO_ENUM, new BaseModuleCodeSmellSpotter[] { new ConvertToEnum() });
		m.put(CodeSmellType.EMPTY_STATEMENT_BLOCK, new BaseModuleCodeSmellSpotter[] { new EmptyStatementBlock() });
		m.put(CodeSmellType.GOTO, new BaseModuleCodeSmellSpotter[] { new Goto() });
		m.put(CodeSmellType.IF_INSTEAD_ALTGUARD, new BaseModuleCodeSmellSpotter[] { new IfInsteadAltguard() });
		m.put(CodeSmellType.IF_INSTEAD_RECEIVE_TEMPLATE, new BaseModuleCodeSmellSpotter[] { new IfInsteadReceiveTemplate() });
		m.put(CodeSmellType.IF_WITHOUT_ELSE, new BaseModuleCodeSmellSpotter[] { new IfWithoutElse() });
		m.put(CodeSmellType.INCORRECT_SHIFT_ROTATE_SIZE, new BaseModuleCodeSmellSpotter[] { new IncorrectRotate.RotateLeft(),
				new IncorrectRotate.RotateRight(), new IncorrectShift.ShiftLeft(),
				new IncorrectShift.ShiftRight() });
		m.put(CodeSmellType.INFINITE_LOOP, new BaseModuleCodeSmellSpotter[] { new InfiniteLoop.DoWhile(), new InfiniteLoop.For(),
				new InfiniteLoop.While() });
		m.put(CodeSmellType.ISBOUND_WITHOUT_ELSE, new BaseModuleCodeSmellSpotter[]{ new IsBoundWithoutElse() });
		m.put(CodeSmellType.ISVALUE_WITH_VALUE, new BaseModuleCodeSmellSpotter[]{ new IsValueWithValue() });
		m.put(CodeSmellType.ITERATE_ON_WRONG_ARRAY, new BaseModuleCodeSmellSpotter[]{ new IterateOnWrongArray() });
		m.put(CodeSmellType.MAGIC_NUMBERS, new BaseModuleCodeSmellSpotter[] { new MagicNumber() });
		m.put(CodeSmellType.MAGIC_STRINGS, new BaseModuleCodeSmellSpotter[] { new MagicString() });
		m.put(CodeSmellType.MISSING_FRIEND, new BaseModuleCodeSmellSpotter[] { new MissingFriend() });
		m.put(CodeSmellType.MISSING_IMPORT, new BaseModuleCodeSmellSpotter[] { new MissingImport() });
		m.put(CodeSmellType.MODULENAME_IN_DEFINITION, new BaseModuleCodeSmellSpotter[] { new ModuleName.InDef(),
				new ModuleName.InGroup() });
		m.put(CodeSmellType.LAZY, new BaseModuleCodeSmellSpotter[] { new Lazy() });
		m.put(CodeSmellType.LOGIC_INVERSION, new BaseModuleCodeSmellSpotter[] { new LogicInversion() });
		m.put(CodeSmellType.NONPRIVATE_PRIVATE,
				new BaseModuleCodeSmellSpotter[] { new NonprivatePrivate() });
		m.put(CodeSmellType.PRIVATE_FIELD_VIA_PUBLIC, new BaseModuleCodeSmellSpotter[] { new PrivateViaPublic.Field() });
		m.put(CodeSmellType.PRIVATE_VALUE_VIA_PUBLIC, new BaseModuleCodeSmellSpotter[] { new PrivateViaPublic.Value() });
		m.put(CodeSmellType.READING_OUT_PAR_BEFORE_WRITTEN, new BaseModuleCodeSmellSpotter[]{ new ReadingOutParBeforeWritten() });
		m.put(CodeSmellType.READONLY_LOC_VARIABLE, new BaseModuleCodeSmellSpotter[] { new ReadOnlyLocal.VarTemplate(),
				new ReadOnlyLocal.Var() });
		m.put(CodeSmellType.READONLY_OUT_PARAM, new BaseModuleCodeSmellSpotter[] { new ReadOnlyOutPar() });
		m.put(CodeSmellType.READONLY_INOUT_PARAM, new BaseModuleCodeSmellSpotter[] { new ReadOnlyInOutPar() });
		m.put(CodeSmellType.RECEIVE_ANY_TEMPLATE, new BaseModuleCodeSmellSpotter[] { new ReceiveAnyTemplate() });
		m.put(CodeSmellType.SELECT_COVERAGE, new BaseModuleCodeSmellSpotter[] { new SelectCoverage() });
		m.put(CodeSmellType.SELECT_WITH_NUMBERS_SORTED, new BaseModuleCodeSmellSpotter[] { new SelectWithNumbersSorted() });
		m.put(CodeSmellType.SETVERDICT_WITHOUT_REASON, new BaseModuleCodeSmellSpotter[] { new VerdictWithoutReason() });
		m.put(CodeSmellType.SHORTHAND, new BaseModuleCodeSmellSpotter[] { new Shorthand() });
		m.put(CodeSmellType.SIZECHECK_IN_LOOP, new BaseModuleCodeSmellSpotter[] { new SizeCheckInLoop() });
		m.put(CodeSmellType.STOP_IN_FUNCTION, new BaseModuleCodeSmellSpotter[] { new StopInFunction() });
		m.put(CodeSmellType.SWITCH_ON_BOOLEAN, new BaseModuleCodeSmellSpotter[] { new SwitchOnBoolean() });
		m.put(CodeSmellType.TOO_COMPLEX_EXPRESSIONS, new BaseModuleCodeSmellSpotter[] { new TooComplexExpression.For(),
				new TooComplexExpression.While(), new TooComplexExpression.DoWhile(), new TooComplexExpression.If(),
				new TooComplexExpression.Assignments() });
		m.put(CodeSmellType.TOO_MANY_PARAMETERS, new BaseModuleCodeSmellSpotter[] { new TooManyParameters() });
		m.put(CodeSmellType.TOO_MANY_STATEMENTS, new BaseModuleCodeSmellSpotter[] { new TooManyStatements() });
		m.put(CodeSmellType.TYPENAME_IN_DEFINITION, new BaseModuleCodeSmellSpotter[] { new TypenameInDef() });
		m.put(CodeSmellType.UNCOMMENTED_FUNCTION,
				new BaseModuleCodeSmellSpotter[] { new UncommentedDefinition() });
		m.put(CodeSmellType.UNINITIALIZED_VARIABLE, new BaseModuleCodeSmellSpotter[] { new UninitializedVar() });
		m.put(CodeSmellType.UNNECESSARY_VALUEOF, new BaseModuleCodeSmellSpotter[] { new UnnecessaryValueof() });
		m.put(CodeSmellType.UNNECESSARY_CONTROLS, new BaseModuleCodeSmellSpotter[] { new UnnecessaryControl.Alt(),
				new UnnecessaryControl.DoWhile(), new UnnecessaryControl.For(), new UnnecessaryControl.If(),
				new UnnecessaryControl.Select(), new UnnecessaryControl.While() });
		m.put(CodeSmellType.UNUSED_FUNTION_RETURN_VALUES, new BaseModuleCodeSmellSpotter[] { new UnusedRetval() });
		m.put(CodeSmellType.UNUSED_STARTED_FUNCTION_RETURN_VALUES, new BaseModuleCodeSmellSpotter[] {
				new UnusedStartedRefFuncRetVal(), new UnusedStartedFuncRetVal() });
		m.put(CodeSmellType.UNUSED_GLOBAL_DEFINITION, new BaseModuleCodeSmellSpotter[] { new UnusedGlobalDefinition() });
		m.put(CodeSmellType.UNUSED_IMPORT, new BaseModuleCodeSmellSpotter[] { new UnusedImport() });
		m.put(CodeSmellType.UNUSED_LOCAL_DEFINITION, new BaseModuleCodeSmellSpotter[] { new UnusedLocalDefinition() });
		m.put(CodeSmellType.VISIBILITY_IN_DEFINITION, new BaseModuleCodeSmellSpotter[] { new Visibility() });

		return Collections.unmodifiableMap(m);
	}

	/**
	 * This map also contains code smell spotters, but those that are related to
	 * projects, not modules. The rationale is that some code smells have to see
	 * a larger scope than the module to analyze to find all problem in the
	 * module itself. An example is finding circular importations.
	 */
	public static Map<CodeSmellType, BaseProjectCodeSmellSpotter[]> newProjectSpotters() {
		Map<CodeSmellType, BaseProjectCodeSmellSpotter[]> pm = new HashMap<CodeSmellType, BaseProjectCodeSmellSpotter[]>();

		pm.put(CodeSmellType.CIRCULAR_IMPORTATION, new BaseProjectCodeSmellSpotter[] { new CircularImportation() });

		return Collections.unmodifiableMap(pm);
	}
}