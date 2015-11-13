/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.SkeletonTemplateProposal;
import org.eclipse.titan.designer.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public final class TTCN3CodeSkeletons {
	public static final String CONTEXT_IDENTIFIER = "TTCN3_SOURCE_CODE";
	public static final String CONTEXT_NAME = "TTCN3 Code Skeleton Context";

	private static final String NEWLINE = System.getProperty("line.separator");
	public static final Image SKELETON_IMAGE = ImageCache.getImage(SkeletonTemplateProposal.SKELETON_IMAGE);

	// TODO create a real resolver that can filter out ${cursor}, and
	// replace ${} with ${cursor}
	public static final SkeletonTemplateProposal[] FORMAL_VALUE_PARAMETER_PROPOSALS = new SkeletonTemplateProposal[] {
			new SkeletonTemplateProposal("in", new Template("formal value parameter", "in", CONTEXT_IDENTIFIER,
					"in ${parameterType} pl_${parameterName}", false)),
			new SkeletonTemplateProposal("out", new Template("formal value parameter", "out", CONTEXT_IDENTIFIER,
					"out ${parameterType} pl_${parameterName}", false)),
			new SkeletonTemplateProposal("inout", new Template("formal value parameter", "inout", CONTEXT_IDENTIFIER,
					"inout ${parameterType} pl_${parameterName}", false)) };
	public static final SkeletonTemplateProposal[] FORMAL_TEMPLATE_PARAMETER_PROPOSALS = new SkeletonTemplateProposal[] {
			new SkeletonTemplateProposal("in", new Template("formal template parameter", "in", CONTEXT_IDENTIFIER,
					"in template ${parameterType} pl_${parameterName}", false)),
			new SkeletonTemplateProposal("out", new Template("formal template parameter", "out", CONTEXT_IDENTIFIER,
					"out template ${parameterType} pl_${parameterName}", false)),
			new SkeletonTemplateProposal("inout", new Template("formal template parameter", "inout", CONTEXT_IDENTIFIER,
					"inout template ${parameterType} pl_${parameterName}", false)),
			// above with restriction
			new SkeletonTemplateProposal("in", new Template("formal restricted template parameter", "in", CONTEXT_IDENTIFIER,
					"in template(${parameterRestriction}) ${parameterType} pl_${parameterName}", false)),
			new SkeletonTemplateProposal("out", new Template("formal restricted template parameter", "out", CONTEXT_IDENTIFIER,
					"out template(${parameterRestriction}) ${parameterType} pl_${parameterName}", false)),
			new SkeletonTemplateProposal("inout", new Template("formal restricted template parameter", "inout", CONTEXT_IDENTIFIER,
					"inout template(${parameterRestriction}) ${parameterType} pl_${parameterName}", false)) };
	public static final SkeletonTemplateProposal[] FORMAL_TIMER_PARAMETER_PROPOSALS = new SkeletonTemplateProposal[] { new SkeletonTemplateProposal(
			"in", new Template("formal timer parameter", "inout", CONTEXT_IDENTIFIER, "${inout} timer pl_${parameterName}", false)) };

	public static final SkeletonTemplateProposal[] MODULE_LEVEL_SKELETON_PROPOSALS = new SkeletonTemplateProposal[] {
			// function
			new SkeletonTemplateProposal("function", new Template("function", "", CONTEXT_IDENTIFIER, "function f_${functionName}("
					+ NEWLINE + "  ${inout} ${parameterType} pl_${parameterName} )" + NEWLINE + "{" + NEWLINE
					+ "  //local declarations" + NEWLINE + "  ${}" + NEWLINE + "  //dynamic behavior" + NEWLINE + "}" + NEWLINE,
					false)),
			new SkeletonTemplateProposal("function", new Template("function", "with return statement", CONTEXT_IDENTIFIER,
					"function f_${functionName}(" + NEWLINE + "  ${inout} ${parameterType} pl_${parameterName} )" + NEWLINE
							+ "return ${returnType}" + NEWLINE + "{" + NEWLINE + "  //local declarations" + NEWLINE
							+ "  ${}" + NEWLINE + "  //dynamic behavior" + NEWLINE + "}" + NEWLINE, false)),
			new SkeletonTemplateProposal("function", new Template("function", "with runs on statement", CONTEXT_IDENTIFIER,
					"function f_${functionName}(" + NEWLINE + "  ${inout} ${parameterType} pl_${parameterName} )" + NEWLINE
							+ "runs on ${TestComponentTypeName}" + NEWLINE + "{" + NEWLINE + "  //local declarations"
							+ NEWLINE + "  ${}" + NEWLINE + "  //dynamic behavior" + NEWLINE + "}" + NEWLINE, false)),
			new SkeletonTemplateProposal("function", new Template("function", "with runs on and return statement", CONTEXT_IDENTIFIER,
					"function f_${functionName}(" + NEWLINE + "  ${inout} ${parameterType} pl_${parameterName} )" + NEWLINE
							+ "runs on ${TestComponentTypeName}" + NEWLINE + "return ${returnType}" + NEWLINE + "{"
							+ NEWLINE + "  //local declarations" + NEWLINE + "  ${}" + NEWLINE + "  //dynamic behavior"
							+ NEWLINE + "}" + NEWLINE, false)),
			// external function
			new SkeletonTemplateProposal("external", new Template("external function", "", CONTEXT_IDENTIFIER,
					"external function f_${functionName}(" + NEWLINE + "  ${inout} ${parameterType} pl_${parameterName} );",
					false)),
			// testcase
			new SkeletonTemplateProposal("testcase", new Template("testcase", "", CONTEXT_IDENTIFIER, "testcase tc_${testcaseName}("
					+ NEWLINE + "  ${inout} ${parameterType} pl_${parameterName} )" + NEWLINE
					+ "runs on ${TestComponentTypeName}" + NEWLINE + "system ${SystemComponentTypeName}" + NEWLINE + '{'
					+ NEWLINE + "  //local declarations" + NEWLINE + "  ${}" + NEWLINE + "  //dynamic behavior" + NEWLINE + '}'
					+ NEWLINE, false)),
			// control part
			new SkeletonTemplateProposal("control", new Template("control part", "", CONTEXT_IDENTIFIER, "control" + NEWLINE + '{'
					+ NEWLINE + "  //local declarations" + NEWLINE + "  ${}" + NEWLINE + "  //dynamic behavior" + NEWLINE + '}'
					+ NEWLINE, false)),
			// altstep
			new SkeletonTemplateProposal("altstep", new Template("altstep", "", CONTEXT_IDENTIFIER, "altstep as_${altstepName}("
					+ NEWLINE + "  ${inout} ${parameterType} pl_${parameterName} )" + NEWLINE
					+ "runs on ${TestComponentTypeName}" + NEWLINE + '{' + NEWLINE
					+ "  [${guard1}] ${portName}.${receivingOperation}" + NEWLINE + "    {" + NEWLINE + "      ${}" + NEWLINE
					+ "    }" + NEWLINE + "  [${guard2}] ${timerName}.timeout" + NEWLINE + "    {" + NEWLINE + "      ${}"
					+ NEWLINE + "    }" + NEWLINE + "  [else]" + NEWLINE + "    {" + NEWLINE + "      ${}" + NEWLINE + "    }"
					+ NEWLINE + '}' + NEWLINE, false)),
			// signature
			new SkeletonTemplateProposal("signature", new Template("signature", "", CONTEXT_IDENTIFIER,
					"signature S_${ProvedureSignatureID}(" + NEWLINE + "  ${inout} ${parameterType} pl_${parameterName} )"
							+ NEWLINE + "return ${returnType}" + NEWLINE
							+ "exception( ${exceptionType1}, ${exceptionType2} );" + NEWLINE, false)),
			// import statement
			new SkeletonTemplateProposal("import", new Template("import", "short format", CONTEXT_IDENTIFIER,
					"import from ${moduleName} all;" + NEWLINE, false)),
			new SkeletonTemplateProposal("import", new Template("import", "full format", CONTEXT_IDENTIFIER,
					"import from ${moduleName}.objid{${objid}} language  \"${language_description}\" all;" + NEWLINE, false)),
			new SkeletonTemplateProposal("import", new Template("import", "import of imports format", CONTEXT_IDENTIFIER,
					"import from ${moduleName} {import all};" + NEWLINE, false)),

			// component type
			new SkeletonTemplateProposal("type", new Template("component", "", CONTEXT_IDENTIFIER, "type component ${componentName}_CT"
					+ NEWLINE + '{' + NEWLINE + "  //constant definitions" + NEWLINE + "  ${}" + NEWLINE
					+ "  //variable definitions" + NEWLINE + "  port ${testPortTypeName}_PT ${portname};" + NEWLINE + '}'
					+ NEWLINE, false)),
			new SkeletonTemplateProposal("component", new Template("component", "(postfix)", CONTEXT_IDENTIFIER,
					"component ${componentName}_CT" + NEWLINE + '{' + NEWLINE + "  //constant definitions" + NEWLINE + "  ${}"
							+ NEWLINE + "  //variable definitions" + NEWLINE
							+ "  port ${testPortTypeName}_PT ${portname};" + NEWLINE + '}' + NEWLINE, false)),
			// port type
			new SkeletonTemplateProposal("type", new Template("port", "message based", CONTEXT_IDENTIFIER,
					"type port ${testPortName}_PT message" + NEWLINE + '{' + NEWLINE + "  in ${inType1}, ${inType2};" + NEWLINE
							+ "  out ${outType1}, ${outType2};" + NEWLINE + "  inout ${inoutType1}, ${inoutType2};"
							+ NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("port", new Template("port", "message based (postfixed)", CONTEXT_IDENTIFIER,
					"port ${testPortName}_PT message" + NEWLINE + '{' + NEWLINE + "  in ${inType1}, ${inType2};" + NEWLINE
							+ "  out ${outType1}, ${outType2};" + NEWLINE + "  inout ${inoutType1}, ${inoutType2};"
							+ NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("type", new Template("port", "procedure based", CONTEXT_IDENTIFIER,
					"type port ${testPortName}_PT procedure" + NEWLINE + '{' + NEWLINE + "  in ${inType1}, ${inType2};" + NEWLINE
							+ "  out ${outType1}, ${outType2};" + NEWLINE + "  inout ${inoutType1}, ${inoutType2};"
							+ NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("port", new Template("port", "procedure based (postfixed)", CONTEXT_IDENTIFIER,
					"port ${testPortName}_PT procedure" + NEWLINE + '{' + NEWLINE + "  in ${inType1}, ${inType2};" + NEWLINE
							+ "  out ${outType1}, ${outType2};" + NEWLINE + "  inout ${inoutType1}, ${inoutType2};"
							+ NEWLINE + '}' + NEWLINE, false)),
			// structured types
			new SkeletonTemplateProposal("type", new Template("record", "top level", CONTEXT_IDENTIFIER, "type record ${recordName}"
					+ NEWLINE + '{' + NEWLINE + "  ${subType1} ${field1}," + NEWLINE + "  ${subType2} ${field2}" + NEWLINE + "};"
					+ NEWLINE, false)),
			new SkeletonTemplateProposal("record", new Template("record", "top level (postfix)", CONTEXT_IDENTIFIER,
					"record ${recordName}" + NEWLINE + '{' + NEWLINE + "  ${subType1} ${field1}," + NEWLINE
							+ "  ${subType2} ${field2}" + NEWLINE + "};" + NEWLINE, false)),
			new SkeletonTemplateProposal("record", new Template("record", "nested", CONTEXT_IDENTIFIER, "record" + NEWLINE + '{'
					+ NEWLINE + "  ${subType1} ${field1}," + NEWLINE + "  ${subType2} ${field2}" + NEWLINE
					+ "}  ${fieldIdentifier}" + NEWLINE, false)),
			new SkeletonTemplateProposal("type", new Template("record of", "top level", CONTEXT_IDENTIFIER,
					"type record length(${sizeConstraint}) of ${baseType} ${newTypeName};" + NEWLINE, false)),
			new SkeletonTemplateProposal("record", new Template("record of", "top level (postfix)", CONTEXT_IDENTIFIER,
					"record length(${sizeConstraint}) of ${baseType} ${newTypeName};" + NEWLINE, false)),
			new SkeletonTemplateProposal("record", new Template("record of", "nested", CONTEXT_IDENTIFIER,
					"record length(${sizeConstraint}) of ${baseType} ${fieldIdentifier}", false)),
			new SkeletonTemplateProposal("type", new Template("set", "top level", CONTEXT_IDENTIFIER,
					"type set ${setName}" + NEWLINE + '{' + NEWLINE + "  ${subType1} ${field1}," + NEWLINE
							+ "  ${subType2} ${field2}" + NEWLINE + "};" + NEWLINE, false)),
			new SkeletonTemplateProposal("set", new Template("set", "top level (postfix)", CONTEXT_IDENTIFIER,
					"set ${setName}" + NEWLINE + '{' + NEWLINE + "  ${subType1} ${field1}," + NEWLINE + "  ${subType2} ${field2}"
							+ NEWLINE + "};" + NEWLINE, false)),
			new SkeletonTemplateProposal("set", new Template("set", "nested", CONTEXT_IDENTIFIER, "set" + NEWLINE + '{' + NEWLINE
					+ "  ${subType1} ${field1}," + NEWLINE + "  ${subType2} ${field2}" + NEWLINE + "}  ${fieldIdentifier}"
					+ NEWLINE, false)),
			new SkeletonTemplateProposal("type", new Template("set of", "top level", CONTEXT_IDENTIFIER,
					"type set length(${sizeConstraint}) of ${baseType} ${newTypeName};" + NEWLINE, false)),
			new SkeletonTemplateProposal("set", new Template("set of", "top level (postfix)", CONTEXT_IDENTIFIER,
					"set length(${sizeConstraint}) of ${baseType} ${newTypeName};" + NEWLINE, false)),
			new SkeletonTemplateProposal("set", new Template("set of", "nested", CONTEXT_IDENTIFIER,
					"set length(${sizeConstraint}) of ${baseType} ${fieldIdentifier}", false)),
			new SkeletonTemplateProposal("type", new Template("union", "top level", CONTEXT_IDENTIFIER, "type union ${unionName}"
					+ NEWLINE + '{' + NEWLINE + "  ${subType1} ${field1}," + NEWLINE + "  ${subType2} ${field2}" + NEWLINE + "};"
					+ NEWLINE, false)),
			new SkeletonTemplateProposal("union", new Template("union", "top level (postfix)", CONTEXT_IDENTIFIER, "union ${unionName}"
					+ NEWLINE + '{' + NEWLINE + "  ${subType1} ${field1}," + NEWLINE + "  ${subType2} ${field2}" + NEWLINE + "};"
					+ NEWLINE, false)),
			new SkeletonTemplateProposal("union", new Template("union", "nested", CONTEXT_IDENTIFIER, "union" + NEWLINE + '{' + NEWLINE
					+ "  ${subType1} ${field1}," + NEWLINE + "  ${subType2} ${field2}" + NEWLINE + "}  ${fieldIdentifier}"
					+ NEWLINE, false)),
			new SkeletonTemplateProposal("type", new Template("enumerated", "top level", CONTEXT_IDENTIFIER,
					"type enumerated ${enumerationName}" + NEWLINE + '{' + NEWLINE + "  ${item1}(${0})," + NEWLINE + "  ${item2}"
							+ NEWLINE + "};" + NEWLINE, false)),
			new SkeletonTemplateProposal("enumerated", new Template("enumerated", "top level (postfix)", CONTEXT_IDENTIFIER,
					"enumerated ${enumerationName}" + NEWLINE + '{' + NEWLINE + "  ${item1}(${0})," + NEWLINE + "  ${item2}"
							+ NEWLINE + "};" + NEWLINE, false)),
			new SkeletonTemplateProposal("enumerated", new Template("enumerated", "nested", CONTEXT_IDENTIFIER, "enumerated" + NEWLINE
					+ '{' + NEWLINE + "  ${item1}(${0})," + NEWLINE + "  ${item2}" + NEWLINE + "}  ${fieldIdentifier}" + NEWLINE,
					false)),
			// module parameters
			new SkeletonTemplateProposal("modulepar", new Template("modulepar", "without default value", CONTEXT_IDENTIFIER,
					"modulepar ${typeName} tsp_${typeName}_${descriptiveName};" + NEWLINE, false)),
			new SkeletonTemplateProposal("modulepar", new Template("modulepar", "with default value", CONTEXT_IDENTIFIER,
					"modulepar ${typeName} tsp_${typeName}_${descriptiveName} := ${default_value};" + NEWLINE, false)),
			new SkeletonTemplateProposal("modulepar", new Template("modulepar", "multiple parameter form", CONTEXT_IDENTIFIER,
					"modulepar ${typeName} tsp_${typeName}_${descriptiveName} := ${default_value}," + NEWLINE
							+ "  tsp_${typeName}_${descriptiveName2} := ${default_value2};" + NEWLINE, false)),
			// templates
			new SkeletonTemplateProposal("template", new Template("template", "simple", CONTEXT_IDENTIFIER,
					"template ${typeName} t_${typeName}_${descriptiveName} := ${matchingSymbol}", false)),
			new SkeletonTemplateProposal("template", new Template("template", "structured", CONTEXT_IDENTIFIER,
					"template ${typeName} t_${typeName}_${descriptiveName} :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("template", new Template("template", "with parameter", CONTEXT_IDENTIFIER,
					"template ${typeName} t_${typeName}_${descriptiveName} ( " + NEWLINE
							+ "in ${parameterType1} pl_${parameter1}," + NEWLINE
							+ "in template ${parameterType2} pl_${parameter2}) :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("template", new Template("template", "with modifies clause", CONTEXT_IDENTIFIER,
					"template ${typeName} t_${typeName}_${descriptiveName}" + NEWLINE + "modifies ${baseTemplateName} :="
							+ NEWLINE + '{' + NEWLINE + "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE,
					false)),
			new SkeletonTemplateProposal("template", new Template("template", "with parameter and modifies clause", CONTEXT_IDENTIFIER,
					"template ${typeName} t_${typeName}_${descriptiveName} ( " + NEWLINE
							+ "in ${parameterType1} pl_${parameter1}," + NEWLINE
							+ "in template ${parameterType2} pl_${parameter2})" + NEWLINE
							+ "modifies ${baseTemplateName} :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			// restricted templates
			new SkeletonTemplateProposal("template", new Template("template", "simple restricted", CONTEXT_IDENTIFIER,
					"template(${restriction}) ${typeName} t_${typeName}_${descriptiveName} := ${matchingSymbol}", false)),
			new SkeletonTemplateProposal("template", new Template("template", "structured restricted", CONTEXT_IDENTIFIER,
					"template(${restriction}) ${typeName} t_${typeName}_${descriptiveName} :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("template", new Template("template", "with parameter restricted", CONTEXT_IDENTIFIER,
					"template(${restriction}) ${typeName} t_${typeName}_${descriptiveName} ( " + NEWLINE
							+ "in ${parameterType1} pl_${parameter1}," + NEWLINE
							+ "in template(${parameterRestriction}) ${parameterType2} pl_${parameter2}) :=" + NEWLINE
							+ '{' + NEWLINE + "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("template", new Template("template", "with modifies clause restricted", CONTEXT_IDENTIFIER,
					"template(${restriction}) ${typeName} t_${typeName}_${descriptiveName}" + NEWLINE
							+ "modifies ${baseTemplateName} :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("template", new Template("template", "with parameter and modifies clause restricted",
					CONTEXT_IDENTIFIER, "template(${restriction}) ${typeName} t_${typeName}_${descriptiveName} ( " + NEWLINE
							+ "in ${parameterType1} pl_${parameter1}," + NEWLINE
							+ "in template(${parameterRestriction}) ${parameterType2} pl_${parameter2})" + NEWLINE
							+ "modifies ${baseTemplateName} :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			// constants (module level naming)
			new SkeletonTemplateProposal("const", new Template("const", "simple on module level", CONTEXT_IDENTIFIER,
					"const ${typeName} cg_${constantName} := ${constant_value};" + NEWLINE, false)),
			new SkeletonTemplateProposal("const", new Template("const", "structured on module level", CONTEXT_IDENTIFIER,
					"const ${typeName} cg_${constantName} := {${constant_sub_value1}, ${constant_sub_value2}};" + NEWLINE, false)),
			new SkeletonTemplateProposal("const", new Template("const", "multiple constant form on module level", CONTEXT_IDENTIFIER,
					"const ${typeName} cg_${constantName1} := ${constant_value1}, cg_${constantName2} := ${constant_value2};"
							+ NEWLINE, false)) };

	public static final SkeletonTemplateProposal[] STATEMENT_LEVEL_SKELETON_PROPOSALS = new SkeletonTemplateProposal[] {
			// loop statements
			new SkeletonTemplateProposal("for", new Template("for", "iterate over array", CONTEXT_IDENTIFIER,
					"for ( var integer ${loopCounter} := ${initialValue}; ${loopCounter} < ${limit} ; ${loopCounter} := ${loopCounter}+1 )"
							+ NEWLINE + '{' + NEWLINE + "  ${}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("while", new Template("while", "while loop", CONTEXT_IDENTIFIER,
					"while ( ${booleanExpression} )" + NEWLINE + '{' + NEWLINE + "  ${}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("do", new Template("do", "do while loop", CONTEXT_IDENTIFIER, "do" + NEWLINE + '{' + NEWLINE
					+ "  ${}" + NEWLINE + "} while ( ${booleanExpression} )" + NEWLINE, false)),
			// conditional statements
			new SkeletonTemplateProposal("if", new Template("if", "if statment", CONTEXT_IDENTIFIER, "if ( ${booleanExpression} )"
					+ NEWLINE + '{' + NEWLINE + "  ${}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("if", new Template("if elseif", "if elseif statment", CONTEXT_IDENTIFIER,
					"if ( ${booleanExpression1} )" + NEWLINE + '{' + NEWLINE + "  ${}" + NEWLINE + '}' + NEWLINE
							+ "else if ( ${booleanExpression2} )" + NEWLINE + '{' + NEWLINE + "  ${}" + NEWLINE + '}'
							+ NEWLINE, false)),
			new SkeletonTemplateProposal("if", new Template("if else", "if else statment", CONTEXT_IDENTIFIER,
					"if ( ${booleanExpression} )" + NEWLINE + '{' + NEWLINE + "  ${}" + NEWLINE + '}' + NEWLINE + "else"
							+ NEWLINE + '{' + NEWLINE + "  ${}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("elseif", new Template("elseif", "else if statment", CONTEXT_IDENTIFIER,
					"else if ( ${booleanExpression2} )" + NEWLINE + '{' + NEWLINE + "  ${}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("else", new Template("else", "else statment", CONTEXT_IDENTIFIER, "else" + NEWLINE + '{'
					+ NEWLINE + "  ${}" + NEWLINE + '}' + NEWLINE, false)),
			// select statement
			new SkeletonTemplateProposal("select", new Template("select", "select statment with case else branch", CONTEXT_IDENTIFIER,
					"select( ${expression} )" + NEWLINE + '{' + NEWLINE + "  case ( ${template_instance} )" + NEWLINE + "  {"
							+ NEWLINE + "    ${}" + NEWLINE + "  }" + NEWLINE + "  case else" + NEWLINE + "  {" + NEWLINE
							+ "    ${}" + NEWLINE + "  }" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("case", new Template("case", "select case branch statment", CONTEXT_IDENTIFIER,
					"case ( ${template_instance} )" + NEWLINE + "  {" + NEWLINE + "    ${}" + NEWLINE + "  }" + NEWLINE, false)),
			// alt
			new SkeletonTemplateProposal("alt", new Template("alt", "", CONTEXT_IDENTIFIER, "alt" + NEWLINE + '{' + NEWLINE
					+ "  [${guard1}] ${portName}.${receivingOperation}" + NEWLINE + "    {" + NEWLINE + "      ${}" + NEWLINE
					+ "    }" + NEWLINE + "  [${guard2}] ${timerName}.timeout" + NEWLINE + "    {" + NEWLINE + "      ${}"
					+ NEWLINE + "    }" + NEWLINE + "  [else]" + NEWLINE + "    {" + NEWLINE + "      ${}" + NEWLINE + "    }"
					+ NEWLINE + "};" + NEWLINE, false)),
			new SkeletonTemplateProposal("interleave", new Template("interleave", "", CONTEXT_IDENTIFIER, "interleave" + NEWLINE + '{'
					+ NEWLINE + "  [${guard1}] ${portName}.${receivingOperation}" + NEWLINE + "    {" + NEWLINE + "      ${}"
					+ NEWLINE + "    }" + NEWLINE + "  [${guard2}] ${portName2}.${receivingOperation2}" + NEWLINE + "    {"
					+ NEWLINE + "      ${}" + NEWLINE + "    }" + NEWLINE + "};" + NEWLINE, false)),
			// templates
			new SkeletonTemplateProposal("template", new Template("template", "simple", CONTEXT_IDENTIFIER,
					"template ${typeName} t_${typeName}_${descriptiveName} := ${matchingSymbol}", false)),
			new SkeletonTemplateProposal("template", new Template("template", "structured", CONTEXT_IDENTIFIER,
					"template ${typeName} t_${typeName}_${descriptiveName} :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("template", new Template("template", "with parameter", CONTEXT_IDENTIFIER,
					"template ${typeName} t_${typeName}_${descriptiveName} (" + NEWLINE
							+ "in ${parameterType1} pl_${parameter1}," + NEWLINE
							+ "in template ${parameterType2} pl_${parameter2}) :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("template", new Template("template", "with modifies clause", CONTEXT_IDENTIFIER,
					"template ${typeName} t_${typeName}_${descriptiveName}" + NEWLINE + "modifies ${baseTemplateName} :="
							+ NEWLINE + '{' + NEWLINE + "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE,
					false)),
			new SkeletonTemplateProposal("template", new Template("template", "with parameter and modifies clause", CONTEXT_IDENTIFIER,
					"template ${typeName} t_${typeName}_${descriptiveName} (" + NEWLINE
							+ "in ${parameterType1} pl_${parameter1}," + NEWLINE
							+ "in template ${parameterType2} pl_${parameter2})" + NEWLINE
							+ "modifies ${baseTemplateName} :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			// above with restriction
			new SkeletonTemplateProposal("template", new Template("template", "simple restricted", CONTEXT_IDENTIFIER,
					"template(${restriction}) ${typeName} t_${typeName}_${descriptiveName} := ${matchingSymbol}", false)),
			new SkeletonTemplateProposal("template", new Template("template", "structured restricted", CONTEXT_IDENTIFIER,
					"template(${restriction}) ${typeName} t_${typeName}_${descriptiveName} :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("template", new Template("template", "with parameter restricted", CONTEXT_IDENTIFIER,
					"template(${restriction}) ${typeName} t_${typeName}_${descriptiveName} (" + NEWLINE
							+ "in ${parameterType1} pl_${parameter1}," + NEWLINE
							+ "in template(${parameterRestriction}) ${parameterType2} pl_${parameter2}) :=" + NEWLINE
							+ '{' + NEWLINE + "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("template", new Template("template", "with modifies clause restricted", CONTEXT_IDENTIFIER,
					"template(${restriction}) ${typeName} t_${typeName}_${descriptiveName}" + NEWLINE
							+ "modifies ${baseTemplateName} :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			new SkeletonTemplateProposal("template", new Template("template", "with parameter and modifies clause restricted",
					CONTEXT_IDENTIFIER, "template(${restriction}) ${typeName} t_${typeName}_${descriptiveName} (" + NEWLINE
							+ "in ${parameterType1} pl_${parameter1}," + NEWLINE
							+ "in template(${parameterRestriction}) ${parameterType2} pl_${parameter2})" + NEWLINE
							+ "modifies ${baseTemplateName} :=" + NEWLINE + '{' + NEWLINE
							+ "  ${field1} := ${matchingSymbol}" + NEWLINE + '}' + NEWLINE, false)),
			// constants (local naming)
			new SkeletonTemplateProposal("const", new Template("const", "simple in statement block", CONTEXT_IDENTIFIER,
					"const ${typeName} cl_${constantName} := ${constant_value};" + NEWLINE, false)),
			new SkeletonTemplateProposal("const", new Template("const", "structured in statement block", CONTEXT_IDENTIFIER,
					"const ${typeName} cl_${constantName} := {${constant_sub_value1}, ${constant_sub_value2}};" + NEWLINE, false)),
			new SkeletonTemplateProposal("const", new Template("const", "multiple constant form in statement block", CONTEXT_IDENTIFIER,
					"const ${typeName} cl_${constantName1} := ${constant_value1}, cl_${constantName2} := ${constant_value2};"
							+ NEWLINE, false)),
			// variables (local naming)
			new SkeletonTemplateProposal("var", new Template("variable", "simple in statement block", CONTEXT_IDENTIFIER,
					"var ${typeName} vl_${variableName} := ${initial_value};" + NEWLINE, false)),
			new SkeletonTemplateProposal("var", new Template("variable", "structured in statement block", CONTEXT_IDENTIFIER,
					"var ${typeName} vl_${variableName} := {${initial_sub_value1}, ${initial_sub_value2}};" + NEWLINE, false)),
			new SkeletonTemplateProposal("var", new Template("variable", "multiple variable form in statement block", CONTEXT_IDENTIFIER,
					"var ${typeName} vl_${variableName1} := ${initial_value1}, vl_${variableName2} := ${initial_value2};"
							+ NEWLINE, false)),
			// variable templates (local naming)
			new SkeletonTemplateProposal("var", new Template("variable template", "simple in statement block", CONTEXT_IDENTIFIER,
					"var template ${typeName} vtl_${variableName} := ${initial_value};" + NEWLINE, false)),
			new SkeletonTemplateProposal("var", new Template("variable template", "structured in statement block", CONTEXT_IDENTIFIER,
					"var template ${typeName} vtl_${variableName} := {${initial_sub_value1}, ${initial_sub_value2}};" + NEWLINE,
					false)),
			new SkeletonTemplateProposal("var", new Template("variable template", "multiple variable form in statement block",
					CONTEXT_IDENTIFIER,
					"var template ${typeName} vtl_${variableName1} := ${initial_value1}, vtl_${variableName2} := ${initial_value2};"
							+ NEWLINE, false)),
			// above with restriction
			new SkeletonTemplateProposal("var", new Template("variable restricted template", "simple in statement block",
					CONTEXT_IDENTIFIER, "var template(${restriction}) ${typeName} vtl_${variableName} := ${initial_value};"
							+ NEWLINE, false)),
			new SkeletonTemplateProposal("var", new Template("variable restricted template", "structured in statement block",
					CONTEXT_IDENTIFIER,
					"var template(${restriction}) ${typeName} vtl_${variableName} := {${initial_sub_value1}, ${initial_sub_value2}};"
							+ NEWLINE, false)),
			new SkeletonTemplateProposal("var", new Template("variable restricted template", "multiple variable form in statement block",
					CONTEXT_IDENTIFIER,
					"var template(${restriction}) ${typeName} vtl_${variableName1} := ${initial_value1}, vtl_${variableName2} := ${initial_value2};"
							+ NEWLINE, false)),
			// timer (local naming)
			new SkeletonTemplateProposal("timer", new Template("timer", "in statement block", CONTEXT_IDENTIFIER,
					"timer T_${timerName} := ${initial_duration};" + NEWLINE, false)),
			// others
			new SkeletonTemplateProposal("log", new Template("log", "", CONTEXT_IDENTIFIER, "log ( ${} );" + NEWLINE, false)),
			new SkeletonTemplateProposal("match", new Template("match", "", CONTEXT_IDENTIFIER, "match ( ${Value}, ${Template} )"
					+ NEWLINE, false)),
			new SkeletonTemplateProposal("setverdict", new Template("setverdict", "", CONTEXT_IDENTIFIER,
					"setverdict ( ${} );" + NEWLINE, false)),
			new SkeletonTemplateProposal("any", new Template("any component", "", CONTEXT_IDENTIFIER, "any component", false)),
			new SkeletonTemplateProposal("any", new Template("any port", "", CONTEXT_IDENTIFIER, "any port", false)),
			new SkeletonTemplateProposal("any", new Template("any timer", "", CONTEXT_IDENTIFIER, "any timer", false)),
			new SkeletonTemplateProposal("all", new Template("all component", "", CONTEXT_IDENTIFIER, "all component", false)),
			new SkeletonTemplateProposal("all", new Template("all port", "", CONTEXT_IDENTIFIER, "all port", false)),
			new SkeletonTemplateProposal("all", new Template("all timer", "", CONTEXT_IDENTIFIER, "all timer", false)),
			new SkeletonTemplateProposal("@try", new Template("@try{} @catch{}", "@try{} @catch{} blocks", CONTEXT_IDENTIFIER, NEWLINE
					+ "@try {" + NEWLINE + "  " + NEWLINE + '}' + NEWLINE + "@catch(${dte_string}) {" + NEWLINE + "  " + NEWLINE
					+ '}' + NEWLINE, false)) };

	public static final SkeletonTemplateProposal[] COMPONENT_INTERNAL_SKELETON_TEMPLATE_PROPOSALS = new SkeletonTemplateProposal[] {
			// constants (component internal naming)
			new SkeletonTemplateProposal("const", new Template("const", "simple in component", CONTEXT_IDENTIFIER,
					"const ${typeName} c_${constantName} := ${constant_value};" + NEWLINE, false)),
			new SkeletonTemplateProposal("const", new Template("const", "structured in component", CONTEXT_IDENTIFIER,
					"const ${typeName} c_${constantName} := {${constant_sub_value1}, ${constant_sub_value2}};" + NEWLINE, false)),
			new SkeletonTemplateProposal("const", new Template("const", "multiple constant form in component", CONTEXT_IDENTIFIER,
					"const ${typeName} c_${constantName1} := ${constant_value1}, c_${constantName2} := ${constant_value2};"
							+ NEWLINE, false)),
			// variable (component internal naming)
			new SkeletonTemplateProposal("var", new Template("variable", "simple in component", CONTEXT_IDENTIFIER,
					"var ${typeName} v_${variableName} := ${initial_value};" + NEWLINE, false)),
			new SkeletonTemplateProposal("var", new Template("variable", "structured in component", CONTEXT_IDENTIFIER,
					"var ${typeName} v_${variableName} := {${initial_sub_value1}, ${initial_sub_value2}};" + NEWLINE, false)),
			new SkeletonTemplateProposal("var",
					new Template("variable", "multiple variable form in component", CONTEXT_IDENTIFIER,
							"var ${typeName} v_${variableName1} := ${initial_value1}, v_${variableName2} := ${initial_value2};"
									+ NEWLINE, false)),
			// variable templates (component internal naming)
			new SkeletonTemplateProposal("var", new Template("variable template", "simple in component", CONTEXT_IDENTIFIER,
					"var template ${typeName} vt_${variableName} := ${initial_value};" + NEWLINE, false)),
			new SkeletonTemplateProposal("var", new Template("variable template", "structured in component", CONTEXT_IDENTIFIER,
					"var template ${typeName} vt_${variableName} := {${initial_sub_value1}, ${initial_sub_value2}};" + NEWLINE,
					false)),
			new SkeletonTemplateProposal("var", new Template("variable template", "multiple variable form in component",
					CONTEXT_IDENTIFIER,
					"var template ${typeName} vt_${variableName1} := ${initial_value1}, vt_${variableName2} := ${initial_value2};"
							+ NEWLINE, false)),
			// above with template restrictions
			new SkeletonTemplateProposal("var", new Template("variable restricted template", "simple in component", CONTEXT_IDENTIFIER,
					"var template(${restriction}) ${typeName} vt_${variableName} := ${initial_value};" + NEWLINE, false)),
			new SkeletonTemplateProposal("var", new Template("variable restricted template", "structured in component",
					CONTEXT_IDENTIFIER,
					"var template(${restriction}) ${typeName} vt_${variableName} := {${initial_sub_value1}, ${initial_sub_value2}};"
							+ NEWLINE, false)),
			new SkeletonTemplateProposal("var", new Template("variable restricted template", "multiple variable form in component",
					CONTEXT_IDENTIFIER,
					"var template(${restriction}) ${typeName} vt_${variableName1} := ${initial_value1}, vt_${variableName2} := ${initial_value2};"
							+ NEWLINE, false)),
			// timer (component internal naming)
			new SkeletonTemplateProposal("timer", new Template("timer", "in component", CONTEXT_IDENTIFIER,
					"timer T_${timerName} := ${initial_duration};" + NEWLINE, false)) };

	public static final SkeletonTemplateProposal[] PREDEFINED_FUNCTIONS = new SkeletonTemplateProposal[] {
			// 1 expression parameter
			new SkeletonTemplateProposal("bit2hex", new Template("bit2hex", "predefined function", CONTEXT_IDENTIFIER,
					"bit2hex(${expression})", false)),
			new SkeletonTemplateProposal("bit2int", new Template("bit2int", "predefined function", CONTEXT_IDENTIFIER,
					"bit2int(${expression})", false)),
			new SkeletonTemplateProposal("bit2oct", new Template("bit2oct", "predefined function", CONTEXT_IDENTIFIER,
					"bit2oct(${expression})", false)),
			new SkeletonTemplateProposal("bit2str", new Template("bit2str", "predefined function", CONTEXT_IDENTIFIER,
					"bit2str(${expression})", false)),
			new SkeletonTemplateProposal("char2int", new Template("char2int", "predefined function", CONTEXT_IDENTIFIER,
					"char2int(${expression})", false)),
			new SkeletonTemplateProposal("char2oct", new Template("char2oct", "predefined function", CONTEXT_IDENTIFIER,
					"char2oct(${expression})", false)),
			new SkeletonTemplateProposal("float2int", new Template("float2int", "predefined function", CONTEXT_IDENTIFIER,
					"float2int(${expression})", false)),
			new SkeletonTemplateProposal("float2str", new Template("float2str", "predefined function", CONTEXT_IDENTIFIER,
					"float2str(${expression})", false)),
			new SkeletonTemplateProposal("hex2bit", new Template("hex2bit", "predefined function", CONTEXT_IDENTIFIER,
					"hex2bit(${expression})", false)),
			new SkeletonTemplateProposal("hex2int", new Template("hex2int", "predefined function", CONTEXT_IDENTIFIER,
					"hex2int(${expression})", false)),
			new SkeletonTemplateProposal("hex2oct", new Template("hex2oct", "predefined function", CONTEXT_IDENTIFIER,
					"hex2oct(${expression})", false)),
			new SkeletonTemplateProposal("hex2str", new Template("hex2str", "predefined function", CONTEXT_IDENTIFIER,
					"hex2str(${expression})", false)),
			new SkeletonTemplateProposal("int2char", new Template("int2char", "predefined function", CONTEXT_IDENTIFIER,
					"int2char(${expression})", false)),
			new SkeletonTemplateProposal("int2char(9)", new Template("int2char(9)", "HORIZONTAL TABULATION", CONTEXT_IDENTIFIER,
					"int2char(9)", false)),
			new SkeletonTemplateProposal("int2char(10)", new Template("int2char(10)", "LINE FEED", CONTEXT_IDENTIFIER, "int2char(10)",
					false)),
			new SkeletonTemplateProposal("int2char(11)", new Template("int2char(11)", "VERTICAL TABULATION", CONTEXT_IDENTIFIER,
					"int2char(11)", false)),
			new SkeletonTemplateProposal("int2char(12)", new Template("int2char(12)", "FORM FEED", CONTEXT_IDENTIFIER, "int2char(12)",
					false)),
			new SkeletonTemplateProposal("int2char(13)", new Template("int2char(10)", "CARRIAGE RETURN", CONTEXT_IDENTIFIER,
					"int2char(13)", false)),
			new SkeletonTemplateProposal("int2float", new Template("int2float", "predefined function", CONTEXT_IDENTIFIER,
					"int2float(${expression})", false)),
			new SkeletonTemplateProposal("int2str", new Template("int2str", "predefined function", CONTEXT_IDENTIFIER,
					"int2str(${expression})", false)),
			new SkeletonTemplateProposal("int2unichar", new Template("int2unichar", "predefined function", CONTEXT_IDENTIFIER,
					"int2unichar(${expression})", false)),
			new SkeletonTemplateProposal("int2unichar(9)", new Template("int2unichar(9)", "HORIZONTAL TABULATION", CONTEXT_IDENTIFIER,
					"int2unichar(9)", false)),
			new SkeletonTemplateProposal("int2unichar(10)", new Template("int2unichar(10)", "LINE FEED", CONTEXT_IDENTIFIER,
					"int2unichar(10)", false)),
			new SkeletonTemplateProposal("int2unichar(11)", new Template("int2unichar(11)", "VERTICAL TABULATION", CONTEXT_IDENTIFIER,
					"int2unichar(11)", false)),
			new SkeletonTemplateProposal("int2unichar(12)", new Template("int2unichar(12)", "FORM FEED", CONTEXT_IDENTIFIER,
					"int2unichar(12)", false)),
			new SkeletonTemplateProposal("int2unichar(13)", new Template("int2unichar(10)", "CARRIAGE RETURN", CONTEXT_IDENTIFIER,
					"int2unichar(13)", false)),
			new SkeletonTemplateProposal("oct2bit", new Template("oct2bit", "predefined function", CONTEXT_IDENTIFIER,
					"oct2bit(${expression})", false)),
			new SkeletonTemplateProposal("oct2char", new Template("oct2char", "predefined function", CONTEXT_IDENTIFIER,
					"oct2char(${expression})", false)),
			new SkeletonTemplateProposal("oct2int", new Template("oct2int", "predefined function", CONTEXT_IDENTIFIER,
					"oct2int(${expression})", false)),
			new SkeletonTemplateProposal("oct2str", new Template("oct2str", "predefined function", CONTEXT_IDENTIFIER,
					"oct2str(${expression})", false)),
			new SkeletonTemplateProposal("str2bit", new Template("str2bit", "predefined function", CONTEXT_IDENTIFIER,
					"str2bit(${expression})", false)),
			new SkeletonTemplateProposal("str2float", new Template("str2float", "predefined function", CONTEXT_IDENTIFIER,
					"str2float(${expression})", false)),
			new SkeletonTemplateProposal("str2hex", new Template("str2hex", "predefined function", CONTEXT_IDENTIFIER,
					"str2hex(${expression})", false)),
			new SkeletonTemplateProposal("str2int", new Template("str2int", "predefined function", CONTEXT_IDENTIFIER,
					"str2int(${expression})", false)),
			new SkeletonTemplateProposal("str2int", new Template("str2int", "predefined function", CONTEXT_IDENTIFIER,
					"str2int(${expression})", false)),
			new SkeletonTemplateProposal("unichar2int", new Template("unichar2int", "predefined function", CONTEXT_IDENTIFIER,
					"unichar2int(${expression})", false)),
			new SkeletonTemplateProposal("unichar2char", new Template("unichar2char", "predefined function", CONTEXT_IDENTIFIER,
					"unichar2char(${expression})", false)),
			new SkeletonTemplateProposal("enum2int", new Template("enum2int", "predefined function", CONTEXT_IDENTIFIER,
					"enum2int(${expression})", false)),
			new SkeletonTemplateProposal("encvalue", new Template("encvalue", "predefined function", CONTEXT_IDENTIFIER,
					"encvalue(${expression})", false)),
			new SkeletonTemplateProposal("get_stringencoding", new Template("get_stringencoding", "predefined function", CONTEXT_IDENTIFIER,
					"get_stringencoding(${expression})", false)),
			new SkeletonTemplateProposal("oct2unichar", new Template("oct2unichar", "predefined function", CONTEXT_IDENTIFIER,
					"oct2unichar(${expression})", false)),
			new SkeletonTemplateProposal("remove_bom", new Template("remove_bom", "predefined function", CONTEXT_IDENTIFIER,
					"remove_bom(${expression})", false)),
			new SkeletonTemplateProposal("unichar2oct", new Template("unichar2oct", "predefined function", CONTEXT_IDENTIFIER,
					"unichar2oct(${expression})", false)),
			new SkeletonTemplateProposal("encode_base64", new Template("encode_base64", "predefined function", CONTEXT_IDENTIFIER,
					"encode_base64(${expression})", false)),
			new SkeletonTemplateProposal("decode_base64", new Template("decode_base64", "predefined function", CONTEXT_IDENTIFIER,
					"decode_base64(${expression})", false)),
		
			// 2 expression parameters
			new SkeletonTemplateProposal("int2bit", new Template("int2bit", "predefined function", CONTEXT_IDENTIFIER,
					"int2bit(${expression1},${expression2})", false)),
			new SkeletonTemplateProposal("int2hex", new Template("int2hex", "predefined function", CONTEXT_IDENTIFIER,
					"int2hex(${expression1},${expression2})", false)),
			new SkeletonTemplateProposal("int2oct", new Template("int2oct", "predefined function", CONTEXT_IDENTIFIER,
					"int2oct(${expression1},${expression2})", false)),
			new SkeletonTemplateProposal("decvalue", new Template("decvalue", "predefined function", CONTEXT_IDENTIFIER,
					"decvalue(${expression1}, ${expression2})", false)),
			// 3 expression parameters
			new SkeletonTemplateProposal("decomp", new Template("decomp", "predefined function", CONTEXT_IDENTIFIER,
					"decomp(${expression1},${expression2}, ${expression3})", false)),
			new SkeletonTemplateProposal("regexp", new Template("regexp", "predefined function", CONTEXT_IDENTIFIER,
					"regexp(${expression1},${expression2}, ${expression3})", false)),
			new SkeletonTemplateProposal("substr", new Template("substr", "predefined function", CONTEXT_IDENTIFIER,
					"substr(${expression1},${expression2}, ${expression3})", false)),
			// 4 expression parameters
			new SkeletonTemplateProposal("replace", new Template("replace", "predefined function", CONTEXT_IDENTIFIER,
					"replace(${expression1},${expression2}, ${expression3}, ${expression4})", false)),
			// log2str
			new SkeletonTemplateProposal("log2str", new Template("log2str", "predefined function", CONTEXT_IDENTIFIER, "log2str( ${} )",
					false)),
			// isvalue/lengthof with template instance parameter
			new SkeletonTemplateProposal("isbound", new Template("isbound", "predefined function", CONTEXT_IDENTIFIER,
					"isbound(${templateInstance})", false)),
			new SkeletonTemplateProposal("isvalue", new Template("isvalue", "predefined function", CONTEXT_IDENTIFIER,
					"isvalue(${templateInstance})", false)),
			new SkeletonTemplateProposal("lengthof", new Template("lengthof", "predefined function", CONTEXT_IDENTIFIER,
					"lengthof(${templateInstance})", false)),
			// ischoosen/ispresent with ispresent parameter
			new SkeletonTemplateProposal("ischosen", new Template("ischosen", "predefined function", CONTEXT_IDENTIFIER,
					"ischosen(${templateInstance})", false)),
			new SkeletonTemplateProposal("ispresent", new Template("ispresent", "predefined function", CONTEXT_IDENTIFIER,
					"ispresent(${templateInstance})", false)),
			// rnd with 0 or 1 parameter
			new SkeletonTemplateProposal("rnd", new Template("rnd", "predefined function", CONTEXT_IDENTIFIER, "rnd(${expression})",
					false)),
			// sizeof
			new SkeletonTemplateProposal("sizeof", new Template("sizeof", "predefined function", CONTEXT_IDENTIFIER,
					"sizeof(${expression})", false)) };

	public static final SkeletonTemplateProposal[] CONTROL_PART_FUNCTIONS = new SkeletonTemplateProposal[] {
			new SkeletonTemplateProposal("execute", new Template("execute", "simple", CONTEXT_IDENTIFIER, "execute(${tc_testcase}());",
					false)),
			new SkeletonTemplateProposal("execute", new Template("execute", "with timeout", CONTEXT_IDENTIFIER,
					"execute(${tc_testcase}(),${timeout_value});", false)) };

	private static final String MODULE_KEYWORD = "module ";

	private static final String TTCN3_MODULE_HEADER_SKELETON = "/*" + NEWLINE + "//AUTHOR: " + NEWLINE + "//DATE: " + NEWLINE + "//VERSION: "
			+ NEWLINE + "*/" + NEWLINE;

	private static final String TTCN3_MODULE_BODY_SKELETON = "// [.objid{ itu_t(0) identified_organization(4) etsi(0)" + NEWLINE
			+ "// identified_organization(127) ericsson(5) testing(0)" + NEWLINE + "// <put further nodes here if needed>}]" + NEWLINE
			+ "{" + NEWLINE + NEWLINE + "//=========================================================================" + NEWLINE
			+ "// Import Part" + NEWLINE + "//=========================================================================" + NEWLINE
			+ NEWLINE + "// Insert imports here if applicable!" + NEWLINE + "// You can use the import_part skeleton!" + NEWLINE
			+ NEWLINE + "//=========================================================================" + NEWLINE + "// Module Parameters"
			+ NEWLINE + "//=========================================================================" + NEWLINE + NEWLINE
			+ "// Insert module parameters here if applicable!" + NEWLINE + "// You can use the module_param skeleton!" + NEWLINE
			+ NEWLINE + "//=========================================================================" + NEWLINE + "// Data Types"
			+ NEWLINE + "//=========================================================================" + NEWLINE + NEWLINE
			+ "// Insert data type defintions here if applicable!" + NEWLINE + "// You can use the data_type skeleton!" + NEWLINE
			+ NEWLINE + "//=========================================================================" + NEWLINE + "// Signatures"
			+ NEWLINE + "//=========================================================================" + NEWLINE + NEWLINE
			+ "// Insert signature definitions here if applicable!" + NEWLINE + "// You can use the signature skeleton!" + NEWLINE
			+ NEWLINE + "//=========================================================================" + NEWLINE + "//Port Types"
			+ NEWLINE + "//=========================================================================" + NEWLINE + NEWLINE
			+ "// Insert port type defintions here if applicable!" + NEWLINE + "// You can use the port_type skeleton!" + NEWLINE
			+ NEWLINE + "//=========================================================================" + NEWLINE + "//Component Types"
			+ NEWLINE + "//=========================================================================" + NEWLINE + NEWLINE
			+ "// Insert component type defintions here if applicable!" + NEWLINE + "// You can use the component_type skeleton!"
			+ NEWLINE + NEWLINE + "//=========================================================================" + NEWLINE
			+ "// Constants" + NEWLINE + "//=========================================================================" + NEWLINE
			+ NEWLINE + "// Insert constants here if applicable!" + NEWLINE + "// You can use the constant skeleton!" + NEWLINE + NEWLINE
			+ "//=========================================================================" + NEWLINE + "// Templates" + NEWLINE
			+ "//=========================================================================" + NEWLINE + NEWLINE
			+ "// Insert templates here if applicable!" + NEWLINE + "// You can use the template skeleton!" + NEWLINE + NEWLINE
			+ "//=========================================================================" + NEWLINE + "// Altsteps" + NEWLINE
			+ "//=========================================================================" + NEWLINE + NEWLINE
			+ "// Insert altsteps here if applicable!" + NEWLINE + "// You can use the altstep skeleton!" + NEWLINE + NEWLINE
			+ "//=========================================================================" + NEWLINE + "// Functions" + NEWLINE
			+ "//=========================================================================" + NEWLINE + NEWLINE
			+ "// Insert functions here if applicable!" + NEWLINE + "// You can use the function skeleton!" + NEWLINE + NEWLINE
			+ "//=========================================================================" + NEWLINE + "// Testcases" + NEWLINE
			+ "//=========================================================================" + NEWLINE + NEWLINE
			+ "// Insert test cases here if applicable!" + NEWLINE + "// You can use the test_case skeleton!" + NEWLINE + NEWLINE
			+ "//=========================================================================" + NEWLINE + "// Control" + NEWLINE
			+ "//=========================================================================" + NEWLINE + NEWLINE
			+ "// Insert control part here if applicable!" + NEWLINE + NEWLINE + "}  // end of module" + NEWLINE;

	/** private constructor to disable instantiation */
	private TTCN3CodeSkeletons() {
	}

	/**
	 * Adds the TTCN3 language dependent code skeletons stored in this class
	 * to a proposal collector.
	 * 
	 * @param doc
	 *                the document where the skeletons will be inserted
	 * @param offset
	 *                the offset at which the word to be completed starts
	 * @param collector
	 *                the ProposalCollector which collects the skeletons
	 * */
	public static void addPredefinedSkeletonProposals(final IDocument doc, final int offset, final ProposalCollector collector) {
		for (SkeletonTemplateProposal templateProposal : PREDEFINED_FUNCTIONS) {
			collector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(), SKELETON_IMAGE);
		}
	}

	/**
	 * Adds the TTCN3 language dependent code skeletons stored in this class
	 * to a proposal collector.
	 * 
	 * @param doc
	 *                the document where the skeletons will be inserted
	 * @param offset
	 *                the offset at which the word to be completed starts
	 * @param collector
	 *                the ProposalCollector which collects the skeletons
	 * */
	public static void addSkeletonProposals(final IDocument doc, final int offset, final ProposalCollector collector) {
		for (SkeletonTemplateProposal templateProposal : FORMAL_VALUE_PARAMETER_PROPOSALS) {
			collector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(), SKELETON_IMAGE);
		}
		for (SkeletonTemplateProposal templateProposal : FORMAL_TEMPLATE_PARAMETER_PROPOSALS) {
			collector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(), SKELETON_IMAGE);
		}
		for (SkeletonTemplateProposal templateProposal : FORMAL_TIMER_PARAMETER_PROPOSALS) {
			collector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(), SKELETON_IMAGE);
		}

		for (SkeletonTemplateProposal templateProposal : TTCN3CodeSkeletons.MODULE_LEVEL_SKELETON_PROPOSALS) {
			collector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(), SKELETON_IMAGE);
		}

		for (SkeletonTemplateProposal templateProposal : TTCN3CodeSkeletons.STATEMENT_LEVEL_SKELETON_PROPOSALS) {
			collector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(), SKELETON_IMAGE);
		}

		for (SkeletonTemplateProposal templateProposal : COMPONENT_INTERNAL_SKELETON_TEMPLATE_PROPOSALS) {
			collector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(), SKELETON_IMAGE);
		}
	}

	/**
	 * Returns a valid TTCN3 module skeleton.
	 * 
	 * @param moduleName
	 *                the name of the module to be used to create the
	 *                skeleton
	 * @return the TTCN3 module skeleton
	 * */
	public static String getTTCN3ModuleSkeleton(final String moduleName) {
		StringBuilder buffer = new StringBuilder(TTCN3_MODULE_HEADER_SKELETON);
		buffer.append(MODULE_KEYWORD).append(moduleName).append('\n').append(TTCN3_MODULE_BODY_SKELETON);
		return buffer.toString();
	}
}
