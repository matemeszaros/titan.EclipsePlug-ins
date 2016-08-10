/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests;

import org.eclipse.titan.regressiontests.designer.statictests.Basic_tests.templates.TemplateTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	AST_warnings_tests.class, 
	AST_tests.class, 
	TemplateTestSuite.class, 
	LazyTryCatchTest.class,
	ExpectedValueTypeTest.class })
public class Basic_tests {
}
