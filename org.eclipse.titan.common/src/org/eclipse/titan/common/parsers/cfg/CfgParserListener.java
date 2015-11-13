/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
// Generated from CfgParser.g4 by ANTLR 4.3
package org.eclipse.titan.common.parsers.cfg;

import java.util.HashMap;

import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.cfg.indices.ComponentSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExternalCommandSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.GroupSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.IncludeSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.MCSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IFile;

import java.util.Map;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CfgParser}.
 */
public interface CfgParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MainControllerItemTcpPort}.
	 * @param ctx the parse tree
	 */
	void enterPr_MainControllerItemTcpPort(@NotNull CfgParser.Pr_MainControllerItemTcpPortContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MainControllerItemTcpPort}.
	 * @param ctx the parse tree
	 */
	void exitPr_MainControllerItemTcpPort(@NotNull CfgParser.Pr_MainControllerItemTcpPortContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ComponentSpecificLoggingParam}.
	 * @param ctx the parse tree
	 */
	void enterPr_ComponentSpecificLoggingParam(@NotNull CfgParser.Pr_ComponentSpecificLoggingParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ComponentSpecificLoggingParam}.
	 * @param ctx the parse tree
	 */
	void exitPr_ComponentSpecificLoggingParam(@NotNull CfgParser.Pr_ComponentSpecificLoggingParamContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ArithmeticPrimaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_ArithmeticPrimaryExpression(@NotNull CfgParser.Pr_ArithmeticPrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ArithmeticPrimaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_ArithmeticPrimaryExpression(@NotNull CfgParser.Pr_ArithmeticPrimaryExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_OString}.
	 * @param ctx the parse tree
	 */
	void enterPr_OString(@NotNull CfgParser.Pr_OStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_OString}.
	 * @param ctx the parse tree
	 */
	void exitPr_OString(@NotNull CfgParser.Pr_OStringContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_IntegerValueExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_IntegerValueExpression(@NotNull CfgParser.Pr_IntegerValueExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_IntegerValueExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_IntegerValueExpression(@NotNull CfgParser.Pr_IntegerValueExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_StructuredValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_StructuredValue(@NotNull CfgParser.Pr_StructuredValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_StructuredValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_StructuredValue(@NotNull CfgParser.Pr_StructuredValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LoggingParam}.
	 * @param ctx the parse tree
	 */
	void enterPr_LoggingParam(@NotNull CfgParser.Pr_LoggingParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LoggingParam}.
	 * @param ctx the parse tree
	 */
	void exitPr_LoggingParam(@NotNull CfgParser.Pr_LoggingParamContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_IntegerMulExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_IntegerMulExpression(@NotNull CfgParser.Pr_IntegerMulExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_IntegerMulExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_IntegerMulExpression(@NotNull CfgParser.Pr_IntegerMulExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MainControllerItem}.
	 * @param ctx the parse tree
	 */
	void enterPr_MainControllerItem(@NotNull CfgParser.Pr_MainControllerItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MainControllerItem}.
	 * @param ctx the parse tree
	 */
	void exitPr_MainControllerItem(@NotNull CfgParser.Pr_MainControllerItemContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_SimpleValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_SimpleValue(@NotNull CfgParser.Pr_SimpleValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_SimpleValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_SimpleValue(@NotNull CfgParser.Pr_SimpleValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MainControllerItemKillTimer}.
	 * @param ctx the parse tree
	 */
	void enterPr_MainControllerItemKillTimer(@NotNull CfgParser.Pr_MainControllerItemKillTimerContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MainControllerItemKillTimer}.
	 * @param ctx the parse tree
	 */
	void exitPr_MainControllerItemKillTimer(@NotNull CfgParser.Pr_MainControllerItemKillTimerContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MacroCString}.
	 * @param ctx the parse tree
	 */
	void enterPr_MacroCString(@NotNull CfgParser.Pr_MacroCStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MacroCString}.
	 * @param ctx the parse tree
	 */
	void exitPr_MacroCString(@NotNull CfgParser.Pr_MacroCStringContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_AggregateData}.
	 * @param ctx the parse tree
	 */
	void enterPr_AggregateData(@NotNull CfgParser.Pr_AggregateDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_AggregateData}.
	 * @param ctx the parse tree
	 */
	void exitPr_AggregateData(@NotNull CfgParser.Pr_AggregateDataContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_SimpleParameterValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_SimpleParameterValue(@NotNull CfgParser.Pr_SimpleParameterValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_SimpleParameterValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_SimpleParameterValue(@NotNull CfgParser.Pr_SimpleParameterValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_OrderedIncludeSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_OrderedIncludeSection(@NotNull CfgParser.Pr_OrderedIncludeSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_OrderedIncludeSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_OrderedIncludeSection(@NotNull CfgParser.Pr_OrderedIncludeSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_HostName}.
	 * @param ctx the parse tree
	 */
	void enterPr_HostName(@NotNull CfgParser.Pr_HostNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_HostName}.
	 * @param ctx the parse tree
	 */
	void exitPr_HostName(@NotNull CfgParser.Pr_HostNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_IntegerRange}.
	 * @param ctx the parse tree
	 */
	void enterPr_IntegerRange(@NotNull CfgParser.Pr_IntegerRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_IntegerRange}.
	 * @param ctx the parse tree
	 */
	void exitPr_IntegerRange(@NotNull CfgParser.Pr_IntegerRangeContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_BString}.
	 * @param ctx the parse tree
	 */
	void enterPr_BString(@NotNull CfgParser.Pr_BStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_BString}.
	 * @param ctx the parse tree
	 */
	void exitPr_BString(@NotNull CfgParser.Pr_BStringContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_CString}.
	 * @param ctx the parse tree
	 */
	void enterPr_CString(@NotNull CfgParser.Pr_CStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_CString}.
	 * @param ctx the parse tree
	 */
	void exitPr_CString(@NotNull CfgParser.Pr_CStringContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_DefinitionRValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_DefinitionRValue(@NotNull CfgParser.Pr_DefinitionRValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_DefinitionRValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_DefinitionRValue(@NotNull CfgParser.Pr_DefinitionRValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_FieldValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_FieldValue(@NotNull CfgParser.Pr_FieldValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_FieldValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_FieldValue(@NotNull CfgParser.Pr_FieldValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ConfigFile}.
	 * @param ctx the parse tree
	 */
	void enterPr_ConfigFile(@NotNull CfgParser.Pr_ConfigFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ConfigFile}.
	 * @param ctx the parse tree
	 */
	void exitPr_ConfigFile(@NotNull CfgParser.Pr_ConfigFileContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_FloatAddExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_FloatAddExpression(@NotNull CfgParser.Pr_FloatAddExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_FloatAddExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_FloatAddExpression(@NotNull CfgParser.Pr_FloatAddExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_BStringValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_BStringValue(@NotNull CfgParser.Pr_BStringValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_BStringValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_BStringValue(@NotNull CfgParser.Pr_BStringValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_FloatRange}.
	 * @param ctx the parse tree
	 */
	void enterPr_FloatRange(@NotNull CfgParser.Pr_FloatRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_FloatRange}.
	 * @param ctx the parse tree
	 */
	void exitPr_FloatRange(@NotNull CfgParser.Pr_FloatRangeContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LogEventType}.
	 * @param ctx the parse tree
	 */
	void enterPr_LogEventType(@NotNull CfgParser.Pr_LogEventTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LogEventType}.
	 * @param ctx the parse tree
	 */
	void exitPr_LogEventType(@NotNull CfgParser.Pr_LogEventTypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_GroupsSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_GroupsSection(@NotNull CfgParser.Pr_GroupsSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_GroupsSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_GroupsSection(@NotNull CfgParser.Pr_GroupsSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_StringRange}.
	 * @param ctx the parse tree
	 */
	void enterPr_StringRange(@NotNull CfgParser.Pr_StringRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_StringRange}.
	 * @param ctx the parse tree
	 */
	void exitPr_StringRange(@NotNull CfgParser.Pr_StringRangeContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_NULLKeyword}.
	 * @param ctx the parse tree
	 */
	void enterPr_NULLKeyword(@NotNull CfgParser.Pr_NULLKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_NULLKeyword}.
	 * @param ctx the parse tree
	 */
	void exitPr_NULLKeyword(@NotNull CfgParser.Pr_NULLKeywordContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pt_TestComponentID}.
	 * @param ctx the parse tree
	 */
	void enterPt_TestComponentID(@NotNull CfgParser.Pt_TestComponentIDContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pt_TestComponentID}.
	 * @param ctx the parse tree
	 */
	void exitPt_TestComponentID(@NotNull CfgParser.Pt_TestComponentIDContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_Float}.
	 * @param ctx the parse tree
	 */
	void enterPr_Float(@NotNull CfgParser.Pr_FloatContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_Float}.
	 * @param ctx the parse tree
	 */
	void exitPr_Float(@NotNull CfgParser.Pr_FloatContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MainControllerItemUnixDomainSocket}.
	 * @param ctx the parse tree
	 */
	void enterPr_MainControllerItemUnixDomainSocket(@NotNull CfgParser.Pr_MainControllerItemUnixDomainSocketContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MainControllerItemUnixDomainSocket}.
	 * @param ctx the parse tree
	 */
	void exitPr_MainControllerItemUnixDomainSocket(@NotNull CfgParser.Pr_MainControllerItemUnixDomainSocketContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_DiskFullActionValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_DiskFullActionValue(@NotNull CfgParser.Pr_DiskFullActionValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_DiskFullActionValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_DiskFullActionValue(@NotNull CfgParser.Pr_DiskFullActionValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_Section}.
	 * @param ctx the parse tree
	 */
	void enterPr_Section(@NotNull CfgParser.Pr_SectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_Section}.
	 * @param ctx the parse tree
	 */
	void exitPr_Section(@NotNull CfgParser.Pr_SectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_PlainLoggingParam}.
	 * @param ctx the parse tree
	 */
	void enterPr_PlainLoggingParam(@NotNull CfgParser.Pr_PlainLoggingParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_PlainLoggingParam}.
	 * @param ctx the parse tree
	 */
	void exitPr_PlainLoggingParam(@NotNull CfgParser.Pr_PlainLoggingParamContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ArrayItem}.
	 * @param ctx the parse tree
	 */
	void enterPr_ArrayItem(@NotNull CfgParser.Pr_ArrayItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ArrayItem}.
	 * @param ctx the parse tree
	 */
	void exitPr_ArrayItem(@NotNull CfgParser.Pr_ArrayItemContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_VerdictValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_VerdictValue(@NotNull CfgParser.Pr_VerdictValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_VerdictValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_VerdictValue(@NotNull CfgParser.Pr_VerdictValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MainControllerSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_MainControllerSection(@NotNull CfgParser.Pr_MainControllerSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MainControllerSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_MainControllerSection(@NotNull CfgParser.Pr_MainControllerSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_StructuredValue2}.
	 * @param ctx the parse tree
	 */
	void enterPr_StructuredValue2(@NotNull CfgParser.Pr_StructuredValue2Context ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_StructuredValue2}.
	 * @param ctx the parse tree
	 */
	void exitPr_StructuredValue2(@NotNull CfgParser.Pr_StructuredValue2Context ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ExecuteSectionItem}.
	 * @param ctx the parse tree
	 */
	void enterPr_ExecuteSectionItem(@NotNull CfgParser.Pr_ExecuteSectionItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ExecuteSectionItem}.
	 * @param ctx the parse tree
	 */
	void exitPr_ExecuteSectionItem(@NotNull CfgParser.Pr_ExecuteSectionItemContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ArithmeticMulExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_ArithmeticMulExpression(@NotNull CfgParser.Pr_ArithmeticMulExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ArithmeticMulExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_ArithmeticMulExpression(@NotNull CfgParser.Pr_ArithmeticMulExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ArithmeticAddExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_ArithmeticAddExpression(@NotNull CfgParser.Pr_ArithmeticAddExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ArithmeticAddExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_ArithmeticAddExpression(@NotNull CfgParser.Pr_ArithmeticAddExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_StringValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_StringValue(@NotNull CfgParser.Pr_StringValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_StringValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_StringValue(@NotNull CfgParser.Pr_StringValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_IntegerAddExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_IntegerAddExpression(@NotNull CfgParser.Pr_IntegerAddExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_IntegerAddExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_IntegerAddExpression(@NotNull CfgParser.Pr_IntegerAddExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_EnumeratedValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_EnumeratedValue(@NotNull CfgParser.Pr_EnumeratedValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_EnumeratedValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_EnumeratedValue(@NotNull CfgParser.Pr_EnumeratedValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ModuleParam}.
	 * @param ctx the parse tree
	 */
	void enterPr_ModuleParam(@NotNull CfgParser.Pr_ModuleParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ModuleParam}.
	 * @param ctx the parse tree
	 */
	void exitPr_ModuleParam(@NotNull CfgParser.Pr_ModuleParamContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_TestportParametersSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_TestportParametersSection(@NotNull CfgParser.Pr_TestportParametersSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_TestportParametersSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_TestportParametersSection(@NotNull CfgParser.Pr_TestportParametersSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_FloatMulExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_FloatMulExpression(@NotNull CfgParser.Pr_FloatMulExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_FloatMulExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_FloatMulExpression(@NotNull CfgParser.Pr_FloatMulExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_DisableStatistics}.
	 * @param ctx the parse tree
	 */
	void enterPr_DisableStatistics(@NotNull CfgParser.Pr_DisableStatisticsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_DisableStatistics}.
	 * @param ctx the parse tree
	 */
	void exitPr_DisableStatistics(@NotNull CfgParser.Pr_DisableStatisticsContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ObjIdValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_ObjIdValue(@NotNull CfgParser.Pr_ObjIdValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ObjIdValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_ObjIdValue(@NotNull CfgParser.Pr_ObjIdValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LengthMatch}.
	 * @param ctx the parse tree
	 */
	void enterPr_LengthMatch(@NotNull CfgParser.Pr_LengthMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LengthMatch}.
	 * @param ctx the parse tree
	 */
	void exitPr_LengthMatch(@NotNull CfgParser.Pr_LengthMatchContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LoggingBitMask}.
	 * @param ctx the parse tree
	 */
	void enterPr_LoggingBitMask(@NotNull CfgParser.Pr_LoggingBitMaskContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LoggingBitMask}.
	 * @param ctx the parse tree
	 */
	void exitPr_LoggingBitMask(@NotNull CfgParser.Pr_LoggingBitMaskContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ArithmeticValueExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_ArithmeticValueExpression(@NotNull CfgParser.Pr_ArithmeticValueExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ArithmeticValueExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_ArithmeticValueExpression(@NotNull CfgParser.Pr_ArithmeticValueExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_FieldName}.
	 * @param ctx the parse tree
	 */
	void enterPr_FieldName(@NotNull CfgParser.Pr_FieldNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_FieldName}.
	 * @param ctx the parse tree
	 */
	void exitPr_FieldName(@NotNull CfgParser.Pr_FieldNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_DefineSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_DefineSection(@NotNull CfgParser.Pr_DefineSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_DefineSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_DefineSection(@NotNull CfgParser.Pr_DefineSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ArithmeticUnaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_ArithmeticUnaryExpression(@NotNull CfgParser.Pr_ArithmeticUnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ArithmeticUnaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_ArithmeticUnaryExpression(@NotNull CfgParser.Pr_ArithmeticUnaryExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ModuleParametersSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_ModuleParametersSection(@NotNull CfgParser.Pr_ModuleParametersSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ModuleParametersSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_ModuleParametersSection(@NotNull CfgParser.Pr_ModuleParametersSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_DNSName}.
	 * @param ctx the parse tree
	 */
	void enterPr_DNSName(@NotNull CfgParser.Pr_DNSNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_DNSName}.
	 * @param ctx the parse tree
	 */
	void exitPr_DNSName(@NotNull CfgParser.Pr_DNSNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MainControllerItemLocalAddress}.
	 * @param ctx the parse tree
	 */
	void enterPr_MainControllerItemLocalAddress(@NotNull CfgParser.Pr_MainControllerItemLocalAddressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MainControllerItemLocalAddress}.
	 * @param ctx the parse tree
	 */
	void exitPr_MainControllerItemLocalAddress(@NotNull CfgParser.Pr_MainControllerItemLocalAddressContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ComponentItem}.
	 * @param ctx the parse tree
	 */
	void enterPr_ComponentItem(@NotNull CfgParser.Pr_ComponentItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ComponentItem}.
	 * @param ctx the parse tree
	 */
	void exitPr_ComponentItem(@NotNull CfgParser.Pr_ComponentItemContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_IntegerPrimaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_IntegerPrimaryExpression(@NotNull CfgParser.Pr_IntegerPrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_IntegerPrimaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_IntegerPrimaryExpression(@NotNull CfgParser.Pr_IntegerPrimaryExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_Boolean}.
	 * @param ctx the parse tree
	 */
	void enterPr_Boolean(@NotNull CfgParser.Pr_BooleanContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_Boolean}.
	 * @param ctx the parse tree
	 */
	void exitPr_Boolean(@NotNull CfgParser.Pr_BooleanContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_StatisticsFile}.
	 * @param ctx the parse tree
	 */
	void enterPr_StatisticsFile(@NotNull CfgParser.Pr_StatisticsFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_StatisticsFile}.
	 * @param ctx the parse tree
	 */
	void exitPr_StatisticsFile(@NotNull CfgParser.Pr_StatisticsFileContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_DatabaseFile}.
	 * @param ctx the parse tree
	 */
	void enterPr_DatabaseFile(@NotNull CfgParser.Pr_DatabaseFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_DatabaseFile}.
	 * @param ctx the parse tree
	 */
	void exitPr_DatabaseFile(@NotNull CfgParser.Pr_DatabaseFileContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_CompoundValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_CompoundValue(@NotNull CfgParser.Pr_CompoundValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_CompoundValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_CompoundValue(@NotNull CfgParser.Pr_CompoundValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_FloatValueExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_FloatValueExpression(@NotNull CfgParser.Pr_FloatValueExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_FloatValueExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_FloatValueExpression(@NotNull CfgParser.Pr_FloatValueExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_Identifier}.
	 * @param ctx the parse tree
	 */
	void enterPr_Identifier(@NotNull CfgParser.Pr_IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_Identifier}.
	 * @param ctx the parse tree
	 */
	void exitPr_Identifier(@NotNull CfgParser.Pr_IdentifierContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ParameterValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_ParameterValue(@NotNull CfgParser.Pr_ParameterValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ParameterValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_ParameterValue(@NotNull CfgParser.Pr_ParameterValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_PatternChunk}.
	 * @param ctx the parse tree
	 */
	void enterPr_PatternChunk(@NotNull CfgParser.Pr_PatternChunkContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_PatternChunk}.
	 * @param ctx the parse tree
	 */
	void exitPr_PatternChunk(@NotNull CfgParser.Pr_PatternChunkContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_StatisticsFilterEntry}.
	 * @param ctx the parse tree
	 */
	void enterPr_StatisticsFilterEntry(@NotNull CfgParser.Pr_StatisticsFilterEntryContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_StatisticsFilterEntry}.
	 * @param ctx the parse tree
	 */
	void exitPr_StatisticsFilterEntry(@NotNull CfgParser.Pr_StatisticsFilterEntryContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_YesNoOrBoolean}.
	 * @param ctx the parse tree
	 */
	void enterPr_YesNoOrBoolean(@NotNull CfgParser.Pr_YesNoOrBooleanContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_YesNoOrBoolean}.
	 * @param ctx the parse tree
	 */
	void exitPr_YesNoOrBoolean(@NotNull CfgParser.Pr_YesNoOrBooleanContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LogEventTypesValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_LogEventTypesValue(@NotNull CfgParser.Pr_LogEventTypesValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LogEventTypesValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_LogEventTypesValue(@NotNull CfgParser.Pr_LogEventTypesValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_DisableCoverage}.
	 * @param ctx the parse tree
	 */
	void enterPr_DisableCoverage(@NotNull CfgParser.Pr_DisableCoverageContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_DisableCoverage}.
	 * @param ctx the parse tree
	 */
	void exitPr_DisableCoverage(@NotNull CfgParser.Pr_DisableCoverageContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ParameterName}.
	 * @param ctx the parse tree
	 */
	void enterPr_ParameterName(@NotNull CfgParser.Pr_ParameterNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ParameterName}.
	 * @param ctx the parse tree
	 */
	void exitPr_ParameterName(@NotNull CfgParser.Pr_ParameterNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ProfilerSetting}.
	 * @param ctx the parse tree
	 */
	void enterPr_ProfilerSetting(@NotNull CfgParser.Pr_ProfilerSettingContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ProfilerSetting}.
	 * @param ctx the parse tree
	 */
	void exitPr_ProfilerSetting(@NotNull CfgParser.Pr_ProfilerSettingContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_IndexValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_IndexValue(@NotNull CfgParser.Pr_IndexValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_IndexValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_IndexValue(@NotNull CfgParser.Pr_IndexValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ComponentID}.
	 * @param ctx the parse tree
	 */
	void enterPr_ComponentID(@NotNull CfgParser.Pr_ComponentIDContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ComponentID}.
	 * @param ctx the parse tree
	 */
	void exitPr_ComponentID(@NotNull CfgParser.Pr_ComponentIDContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LoggingMaskElement}.
	 * @param ctx the parse tree
	 */
	void enterPr_LoggingMaskElement(@NotNull CfgParser.Pr_LoggingMaskElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LoggingMaskElement}.
	 * @param ctx the parse tree
	 */
	void exitPr_LoggingMaskElement(@NotNull CfgParser.Pr_LoggingMaskElementContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_BStringMatch}.
	 * @param ctx the parse tree
	 */
	void enterPr_BStringMatch(@NotNull CfgParser.Pr_BStringMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_BStringMatch}.
	 * @param ctx the parse tree
	 */
	void exitPr_BStringMatch(@NotNull CfgParser.Pr_BStringMatchContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ExecuteSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_ExecuteSection(@NotNull CfgParser.Pr_ExecuteSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ExecuteSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_ExecuteSection(@NotNull CfgParser.Pr_ExecuteSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_FloatPrimaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_FloatPrimaryExpression(@NotNull CfgParser.Pr_FloatPrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_FloatPrimaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_FloatPrimaryExpression(@NotNull CfgParser.Pr_FloatPrimaryExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_NetFunctionTimes}.
	 * @param ctx the parse tree
	 */
	void enterPr_NetFunctionTimes(@NotNull CfgParser.Pr_NetFunctionTimesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_NetFunctionTimes}.
	 * @param ctx the parse tree
	 */
	void exitPr_NetFunctionTimes(@NotNull CfgParser.Pr_NetFunctionTimesContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_HStringValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_HStringValue(@NotNull CfgParser.Pr_HStringValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_HStringValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_HStringValue(@NotNull CfgParser.Pr_HStringValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_IntegerUnaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_IntegerUnaryExpression(@NotNull CfgParser.Pr_IntegerUnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_IntegerUnaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_IntegerUnaryExpression(@NotNull CfgParser.Pr_IntegerUnaryExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_FloatUnaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPr_FloatUnaryExpression(@NotNull CfgParser.Pr_FloatUnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_FloatUnaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPr_FloatUnaryExpression(@NotNull CfgParser.Pr_FloatUnaryExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MatchingHintsValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_MatchingHintsValue(@NotNull CfgParser.Pr_MatchingHintsValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MatchingHintsValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_MatchingHintsValue(@NotNull CfgParser.Pr_MatchingHintsValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_IncludeSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_IncludeSection(@NotNull CfgParser.Pr_IncludeSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_IncludeSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_IncludeSection(@NotNull CfgParser.Pr_IncludeSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_HString}.
	 * @param ctx the parse tree
	 */
	void enterPr_HString(@NotNull CfgParser.Pr_HStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_HString}.
	 * @param ctx the parse tree
	 */
	void exitPr_HString(@NotNull CfgParser.Pr_HStringContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_PatternChunkList}.
	 * @param ctx the parse tree
	 */
	void enterPr_PatternChunkList(@NotNull CfgParser.Pr_PatternChunkListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_PatternChunkList}.
	 * @param ctx the parse tree
	 */
	void exitPr_PatternChunkList(@NotNull CfgParser.Pr_PatternChunkListContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_UniversalOrNotStringValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_UniversalOrNotStringValue(@NotNull CfgParser.Pr_UniversalOrNotStringValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_UniversalOrNotStringValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_UniversalOrNotStringValue(@NotNull CfgParser.Pr_UniversalOrNotStringValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_Detailed}.
	 * @param ctx the parse tree
	 */
	void enterPr_Detailed(@NotNull CfgParser.Pr_DetailedContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_Detailed}.
	 * @param ctx the parse tree
	 */
	void exitPr_Detailed(@NotNull CfgParser.Pr_DetailedContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_NetLineTimes}.
	 * @param ctx the parse tree
	 */
	void enterPr_NetLineTimes(@NotNull CfgParser.Pr_NetLineTimesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_NetLineTimes}.
	 * @param ctx the parse tree
	 */
	void exitPr_NetLineTimes(@NotNull CfgParser.Pr_NetLineTimesContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ObjIdComponent}.
	 * @param ctx the parse tree
	 */
	void enterPr_ObjIdComponent(@NotNull CfgParser.Pr_ObjIdComponentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ObjIdComponent}.
	 * @param ctx the parse tree
	 */
	void exitPr_ObjIdComponent(@NotNull CfgParser.Pr_ObjIdComponentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ExternalCommandsSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_ExternalCommandsSection(@NotNull CfgParser.Pr_ExternalCommandsSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ExternalCommandsSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_ExternalCommandsSection(@NotNull CfgParser.Pr_ExternalCommandsSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_TestportName}.
	 * @param ctx the parse tree
	 */
	void enterPr_TestportName(@NotNull CfgParser.Pr_TestportNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_TestportName}.
	 * @param ctx the parse tree
	 */
	void exitPr_TestportName(@NotNull CfgParser.Pr_TestportNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_OStringValue}.
	 * @param ctx the parse tree
	 */
	void enterPr_OStringValue(@NotNull CfgParser.Pr_OStringValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_OStringValue}.
	 * @param ctx the parse tree
	 */
	void exitPr_OStringValue(@NotNull CfgParser.Pr_OStringValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_DefaultSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_DefaultSection(@NotNull CfgParser.Pr_DefaultSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_DefaultSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_DefaultSection(@NotNull CfgParser.Pr_DefaultSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LogEventTypeSet}.
	 * @param ctx the parse tree
	 */
	void enterPr_LogEventTypeSet(@NotNull CfgParser.Pr_LogEventTypeSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LogEventTypeSet}.
	 * @param ctx the parse tree
	 */
	void exitPr_LogEventTypeSet(@NotNull CfgParser.Pr_LogEventTypeSetContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LoggerPluginEntry}.
	 * @param ctx the parse tree
	 */
	void enterPr_LoggerPluginEntry(@NotNull CfgParser.Pr_LoggerPluginEntryContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LoggerPluginEntry}.
	 * @param ctx the parse tree
	 */
	void exitPr_LoggerPluginEntry(@NotNull CfgParser.Pr_LoggerPluginEntryContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_deprecatedEventTypeSet}.
	 * @param ctx the parse tree
	 */
	void enterPr_deprecatedEventTypeSet(@NotNull CfgParser.Pr_deprecatedEventTypeSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_deprecatedEventTypeSet}.
	 * @param ctx the parse tree
	 */
	void exitPr_deprecatedEventTypeSet(@NotNull CfgParser.Pr_deprecatedEventTypeSetContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LengthBound}.
	 * @param ctx the parse tree
	 */
	void enterPr_LengthBound(@NotNull CfgParser.Pr_LengthBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LengthBound}.
	 * @param ctx the parse tree
	 */
	void exitPr_LengthBound(@NotNull CfgParser.Pr_LengthBoundContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_StatisticsFilter}.
	 * @param ctx the parse tree
	 */
	void enterPr_StatisticsFilter(@NotNull CfgParser.Pr_StatisticsFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_StatisticsFilter}.
	 * @param ctx the parse tree
	 */
	void exitPr_StatisticsFilter(@NotNull CfgParser.Pr_StatisticsFilterContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_DisableProfiler}.
	 * @param ctx the parse tree
	 */
	void enterPr_DisableProfiler(@NotNull CfgParser.Pr_DisableProfilerContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_DisableProfiler}.
	 * @param ctx the parse tree
	 */
	void exitPr_DisableProfiler(@NotNull CfgParser.Pr_DisableProfilerContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MacroAssignment}.
	 * @param ctx the parse tree
	 */
	void enterPr_MacroAssignment(@NotNull CfgParser.Pr_MacroAssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MacroAssignment}.
	 * @param ctx the parse tree
	 */
	void exitPr_MacroAssignment(@NotNull CfgParser.Pr_MacroAssignmentContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LoggingSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_LoggingSection(@NotNull CfgParser.Pr_LoggingSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LoggingSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_LoggingSection(@NotNull CfgParser.Pr_LoggingSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LogfileName}.
	 * @param ctx the parse tree
	 */
	void enterPr_LogfileName(@NotNull CfgParser.Pr_LogfileNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LogfileName}.
	 * @param ctx the parse tree
	 */
	void exitPr_LogfileName(@NotNull CfgParser.Pr_LogfileNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_TemplateItemList}.
	 * @param ctx the parse tree
	 */
	void enterPr_TemplateItemList(@NotNull CfgParser.Pr_TemplateItemListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_TemplateItemList}.
	 * @param ctx the parse tree
	 */
	void exitPr_TemplateItemList(@NotNull CfgParser.Pr_TemplateItemListContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_StartAutomatically}.
	 * @param ctx the parse tree
	 */
	void enterPr_StartAutomatically(@NotNull CfgParser.Pr_StartAutomaticallyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_StartAutomatically}.
	 * @param ctx the parse tree
	 */
	void exitPr_StartAutomatically(@NotNull CfgParser.Pr_StartAutomaticallyContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ComponentsSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_ComponentsSection(@NotNull CfgParser.Pr_ComponentsSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ComponentsSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_ComponentsSection(@NotNull CfgParser.Pr_ComponentsSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MainControllerItemNumHcs}.
	 * @param ctx the parse tree
	 */
	void enterPr_MainControllerItemNumHcs(@NotNull CfgParser.Pr_MainControllerItemNumHcsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MainControllerItemNumHcs}.
	 * @param ctx the parse tree
	 */
	void exitPr_MainControllerItemNumHcs(@NotNull CfgParser.Pr_MainControllerItemNumHcsContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_Quadruple}.
	 * @param ctx the parse tree
	 */
	void enterPr_Quadruple(@NotNull CfgParser.Pr_QuadrupleContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_Quadruple}.
	 * @param ctx the parse tree
	 */
	void exitPr_Quadruple(@NotNull CfgParser.Pr_QuadrupleContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ProfilerSection}.
	 * @param ctx the parse tree
	 */
	void enterPr_ProfilerSection(@NotNull CfgParser.Pr_ProfilerSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ProfilerSection}.
	 * @param ctx the parse tree
	 */
	void exitPr_ProfilerSection(@NotNull CfgParser.Pr_ProfilerSectionContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_HStringMatch}.
	 * @param ctx the parse tree
	 */
	void enterPr_HStringMatch(@NotNull CfgParser.Pr_HStringMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_HStringMatch}.
	 * @param ctx the parse tree
	 */
	void exitPr_HStringMatch(@NotNull CfgParser.Pr_HStringMatchContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_MacroExpliciteCString}.
	 * @param ctx the parse tree
	 */
	void enterPr_MacroExpliciteCString(@NotNull CfgParser.Pr_MacroExpliciteCStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_MacroExpliciteCString}.
	 * @param ctx the parse tree
	 */
	void exitPr_MacroExpliciteCString(@NotNull CfgParser.Pr_MacroExpliciteCStringContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ComponentName}.
	 * @param ctx the parse tree
	 */
	void enterPr_ComponentName(@NotNull CfgParser.Pr_ComponentNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ComponentName}.
	 * @param ctx the parse tree
	 */
	void exitPr_ComponentName(@NotNull CfgParser.Pr_ComponentNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_OStringMatch}.
	 * @param ctx the parse tree
	 */
	void enterPr_OStringMatch(@NotNull CfgParser.Pr_OStringMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_OStringMatch}.
	 * @param ctx the parse tree
	 */
	void exitPr_OStringMatch(@NotNull CfgParser.Pr_OStringMatchContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_GroupItem}.
	 * @param ctx the parse tree
	 */
	void enterPr_GroupItem(@NotNull CfgParser.Pr_GroupItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_GroupItem}.
	 * @param ctx the parse tree
	 */
	void exitPr_GroupItem(@NotNull CfgParser.Pr_GroupItemContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_LoggerPluginsPart}.
	 * @param ctx the parse tree
	 */
	void enterPr_LoggerPluginsPart(@NotNull CfgParser.Pr_LoggerPluginsPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_LoggerPluginsPart}.
	 * @param ctx the parse tree
	 */
	void exitPr_LoggerPluginsPart(@NotNull CfgParser.Pr_LoggerPluginsPartContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_ParameterValueOrNotUsedSymbol}.
	 * @param ctx the parse tree
	 */
	void enterPr_ParameterValueOrNotUsedSymbol(@NotNull CfgParser.Pr_ParameterValueOrNotUsedSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_ParameterValueOrNotUsedSymbol}.
	 * @param ctx the parse tree
	 */
	void exitPr_ParameterValueOrNotUsedSymbol(@NotNull CfgParser.Pr_ParameterValueOrNotUsedSymbolContext ctx);

	/**
	 * Enter a parse tree produced by {@link CfgParser#pr_Number}.
	 * @param ctx the parse tree
	 */
	void enterPr_Number(@NotNull CfgParser.Pr_NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link CfgParser#pr_Number}.
	 * @param ctx the parse tree
	 */
	void exitPr_Number(@NotNull CfgParser.Pr_NumberContext ctx);
}