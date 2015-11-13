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


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * This class provides an empty implementation of {@link CfgParserListener},
 * which can be extended to create a listener which only needs to handle a subset
 * of the available methods.
 */
public class CfgParserBaseListener implements CfgParserListener {
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MainControllerItemTcpPort(@NotNull CfgParser.Pr_MainControllerItemTcpPortContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MainControllerItemTcpPort(@NotNull CfgParser.Pr_MainControllerItemTcpPortContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ComponentSpecificLoggingParam(@NotNull CfgParser.Pr_ComponentSpecificLoggingParamContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ComponentSpecificLoggingParam(@NotNull CfgParser.Pr_ComponentSpecificLoggingParamContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ArithmeticPrimaryExpression(@NotNull CfgParser.Pr_ArithmeticPrimaryExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ArithmeticPrimaryExpression(@NotNull CfgParser.Pr_ArithmeticPrimaryExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_OString(@NotNull CfgParser.Pr_OStringContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_OString(@NotNull CfgParser.Pr_OStringContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_IntegerValueExpression(@NotNull CfgParser.Pr_IntegerValueExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_IntegerValueExpression(@NotNull CfgParser.Pr_IntegerValueExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_StructuredValue(@NotNull CfgParser.Pr_StructuredValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_StructuredValue(@NotNull CfgParser.Pr_StructuredValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LoggingParam(@NotNull CfgParser.Pr_LoggingParamContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LoggingParam(@NotNull CfgParser.Pr_LoggingParamContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_IntegerMulExpression(@NotNull CfgParser.Pr_IntegerMulExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_IntegerMulExpression(@NotNull CfgParser.Pr_IntegerMulExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MainControllerItem(@NotNull CfgParser.Pr_MainControllerItemContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MainControllerItem(@NotNull CfgParser.Pr_MainControllerItemContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_SimpleValue(@NotNull CfgParser.Pr_SimpleValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_SimpleValue(@NotNull CfgParser.Pr_SimpleValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MainControllerItemKillTimer(@NotNull CfgParser.Pr_MainControllerItemKillTimerContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MainControllerItemKillTimer(@NotNull CfgParser.Pr_MainControllerItemKillTimerContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MacroCString(@NotNull CfgParser.Pr_MacroCStringContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MacroCString(@NotNull CfgParser.Pr_MacroCStringContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_AggregateData(@NotNull CfgParser.Pr_AggregateDataContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_AggregateData(@NotNull CfgParser.Pr_AggregateDataContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_SimpleParameterValue(@NotNull CfgParser.Pr_SimpleParameterValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_SimpleParameterValue(@NotNull CfgParser.Pr_SimpleParameterValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_OrderedIncludeSection(@NotNull CfgParser.Pr_OrderedIncludeSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_OrderedIncludeSection(@NotNull CfgParser.Pr_OrderedIncludeSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_HostName(@NotNull CfgParser.Pr_HostNameContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_HostName(@NotNull CfgParser.Pr_HostNameContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_IntegerRange(@NotNull CfgParser.Pr_IntegerRangeContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_IntegerRange(@NotNull CfgParser.Pr_IntegerRangeContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_BString(@NotNull CfgParser.Pr_BStringContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_BString(@NotNull CfgParser.Pr_BStringContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_CString(@NotNull CfgParser.Pr_CStringContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_CString(@NotNull CfgParser.Pr_CStringContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_DefinitionRValue(@NotNull CfgParser.Pr_DefinitionRValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_DefinitionRValue(@NotNull CfgParser.Pr_DefinitionRValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_FieldValue(@NotNull CfgParser.Pr_FieldValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_FieldValue(@NotNull CfgParser.Pr_FieldValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ConfigFile(@NotNull CfgParser.Pr_ConfigFileContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ConfigFile(@NotNull CfgParser.Pr_ConfigFileContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_FloatAddExpression(@NotNull CfgParser.Pr_FloatAddExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_FloatAddExpression(@NotNull CfgParser.Pr_FloatAddExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_BStringValue(@NotNull CfgParser.Pr_BStringValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_BStringValue(@NotNull CfgParser.Pr_BStringValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_FloatRange(@NotNull CfgParser.Pr_FloatRangeContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_FloatRange(@NotNull CfgParser.Pr_FloatRangeContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LogEventType(@NotNull CfgParser.Pr_LogEventTypeContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LogEventType(@NotNull CfgParser.Pr_LogEventTypeContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_GroupsSection(@NotNull CfgParser.Pr_GroupsSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_GroupsSection(@NotNull CfgParser.Pr_GroupsSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_StringRange(@NotNull CfgParser.Pr_StringRangeContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_StringRange(@NotNull CfgParser.Pr_StringRangeContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_NULLKeyword(@NotNull CfgParser.Pr_NULLKeywordContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_NULLKeyword(@NotNull CfgParser.Pr_NULLKeywordContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPt_TestComponentID(@NotNull CfgParser.Pt_TestComponentIDContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPt_TestComponentID(@NotNull CfgParser.Pt_TestComponentIDContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_Float(@NotNull CfgParser.Pr_FloatContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_Float(@NotNull CfgParser.Pr_FloatContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MainControllerItemUnixDomainSocket(@NotNull CfgParser.Pr_MainControllerItemUnixDomainSocketContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MainControllerItemUnixDomainSocket(@NotNull CfgParser.Pr_MainControllerItemUnixDomainSocketContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_DiskFullActionValue(@NotNull CfgParser.Pr_DiskFullActionValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_DiskFullActionValue(@NotNull CfgParser.Pr_DiskFullActionValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_Section(@NotNull CfgParser.Pr_SectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_Section(@NotNull CfgParser.Pr_SectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_PlainLoggingParam(@NotNull CfgParser.Pr_PlainLoggingParamContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_PlainLoggingParam(@NotNull CfgParser.Pr_PlainLoggingParamContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ArrayItem(@NotNull CfgParser.Pr_ArrayItemContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ArrayItem(@NotNull CfgParser.Pr_ArrayItemContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_VerdictValue(@NotNull CfgParser.Pr_VerdictValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_VerdictValue(@NotNull CfgParser.Pr_VerdictValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MainControllerSection(@NotNull CfgParser.Pr_MainControllerSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MainControllerSection(@NotNull CfgParser.Pr_MainControllerSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_StructuredValue2(@NotNull CfgParser.Pr_StructuredValue2Context ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_StructuredValue2(@NotNull CfgParser.Pr_StructuredValue2Context ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ExecuteSectionItem(@NotNull CfgParser.Pr_ExecuteSectionItemContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ExecuteSectionItem(@NotNull CfgParser.Pr_ExecuteSectionItemContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ArithmeticMulExpression(@NotNull CfgParser.Pr_ArithmeticMulExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ArithmeticMulExpression(@NotNull CfgParser.Pr_ArithmeticMulExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ArithmeticAddExpression(@NotNull CfgParser.Pr_ArithmeticAddExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ArithmeticAddExpression(@NotNull CfgParser.Pr_ArithmeticAddExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_StringValue(@NotNull CfgParser.Pr_StringValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_StringValue(@NotNull CfgParser.Pr_StringValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_IntegerAddExpression(@NotNull CfgParser.Pr_IntegerAddExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_IntegerAddExpression(@NotNull CfgParser.Pr_IntegerAddExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_EnumeratedValue(@NotNull CfgParser.Pr_EnumeratedValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_EnumeratedValue(@NotNull CfgParser.Pr_EnumeratedValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ModuleParam(@NotNull CfgParser.Pr_ModuleParamContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ModuleParam(@NotNull CfgParser.Pr_ModuleParamContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_TestportParametersSection(@NotNull CfgParser.Pr_TestportParametersSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_TestportParametersSection(@NotNull CfgParser.Pr_TestportParametersSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_FloatMulExpression(@NotNull CfgParser.Pr_FloatMulExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_FloatMulExpression(@NotNull CfgParser.Pr_FloatMulExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_DisableStatistics(@NotNull CfgParser.Pr_DisableStatisticsContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_DisableStatistics(@NotNull CfgParser.Pr_DisableStatisticsContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ObjIdValue(@NotNull CfgParser.Pr_ObjIdValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ObjIdValue(@NotNull CfgParser.Pr_ObjIdValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LengthMatch(@NotNull CfgParser.Pr_LengthMatchContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LengthMatch(@NotNull CfgParser.Pr_LengthMatchContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LoggingBitMask(@NotNull CfgParser.Pr_LoggingBitMaskContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LoggingBitMask(@NotNull CfgParser.Pr_LoggingBitMaskContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ArithmeticValueExpression(@NotNull CfgParser.Pr_ArithmeticValueExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ArithmeticValueExpression(@NotNull CfgParser.Pr_ArithmeticValueExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_FieldName(@NotNull CfgParser.Pr_FieldNameContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_FieldName(@NotNull CfgParser.Pr_FieldNameContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_DefineSection(@NotNull CfgParser.Pr_DefineSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_DefineSection(@NotNull CfgParser.Pr_DefineSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ArithmeticUnaryExpression(@NotNull CfgParser.Pr_ArithmeticUnaryExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ArithmeticUnaryExpression(@NotNull CfgParser.Pr_ArithmeticUnaryExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ModuleParametersSection(@NotNull CfgParser.Pr_ModuleParametersSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ModuleParametersSection(@NotNull CfgParser.Pr_ModuleParametersSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_DNSName(@NotNull CfgParser.Pr_DNSNameContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_DNSName(@NotNull CfgParser.Pr_DNSNameContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MainControllerItemLocalAddress(@NotNull CfgParser.Pr_MainControllerItemLocalAddressContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MainControllerItemLocalAddress(@NotNull CfgParser.Pr_MainControllerItemLocalAddressContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ComponentItem(@NotNull CfgParser.Pr_ComponentItemContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ComponentItem(@NotNull CfgParser.Pr_ComponentItemContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_IntegerPrimaryExpression(@NotNull CfgParser.Pr_IntegerPrimaryExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_IntegerPrimaryExpression(@NotNull CfgParser.Pr_IntegerPrimaryExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_Boolean(@NotNull CfgParser.Pr_BooleanContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_Boolean(@NotNull CfgParser.Pr_BooleanContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_StatisticsFile(@NotNull CfgParser.Pr_StatisticsFileContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_StatisticsFile(@NotNull CfgParser.Pr_StatisticsFileContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_DatabaseFile(@NotNull CfgParser.Pr_DatabaseFileContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_DatabaseFile(@NotNull CfgParser.Pr_DatabaseFileContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_CompoundValue(@NotNull CfgParser.Pr_CompoundValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_CompoundValue(@NotNull CfgParser.Pr_CompoundValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_FloatValueExpression(@NotNull CfgParser.Pr_FloatValueExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_FloatValueExpression(@NotNull CfgParser.Pr_FloatValueExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_Identifier(@NotNull CfgParser.Pr_IdentifierContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_Identifier(@NotNull CfgParser.Pr_IdentifierContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ParameterValue(@NotNull CfgParser.Pr_ParameterValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ParameterValue(@NotNull CfgParser.Pr_ParameterValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_PatternChunk(@NotNull CfgParser.Pr_PatternChunkContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_PatternChunk(@NotNull CfgParser.Pr_PatternChunkContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_StatisticsFilterEntry(@NotNull CfgParser.Pr_StatisticsFilterEntryContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_StatisticsFilterEntry(@NotNull CfgParser.Pr_StatisticsFilterEntryContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_YesNoOrBoolean(@NotNull CfgParser.Pr_YesNoOrBooleanContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_YesNoOrBoolean(@NotNull CfgParser.Pr_YesNoOrBooleanContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LogEventTypesValue(@NotNull CfgParser.Pr_LogEventTypesValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LogEventTypesValue(@NotNull CfgParser.Pr_LogEventTypesValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_DisableCoverage(@NotNull CfgParser.Pr_DisableCoverageContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_DisableCoverage(@NotNull CfgParser.Pr_DisableCoverageContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ParameterName(@NotNull CfgParser.Pr_ParameterNameContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ParameterName(@NotNull CfgParser.Pr_ParameterNameContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ProfilerSetting(@NotNull CfgParser.Pr_ProfilerSettingContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ProfilerSetting(@NotNull CfgParser.Pr_ProfilerSettingContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_IndexValue(@NotNull CfgParser.Pr_IndexValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_IndexValue(@NotNull CfgParser.Pr_IndexValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ComponentID(@NotNull CfgParser.Pr_ComponentIDContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ComponentID(@NotNull CfgParser.Pr_ComponentIDContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LoggingMaskElement(@NotNull CfgParser.Pr_LoggingMaskElementContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LoggingMaskElement(@NotNull CfgParser.Pr_LoggingMaskElementContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_BStringMatch(@NotNull CfgParser.Pr_BStringMatchContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_BStringMatch(@NotNull CfgParser.Pr_BStringMatchContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ExecuteSection(@NotNull CfgParser.Pr_ExecuteSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ExecuteSection(@NotNull CfgParser.Pr_ExecuteSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_FloatPrimaryExpression(@NotNull CfgParser.Pr_FloatPrimaryExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_FloatPrimaryExpression(@NotNull CfgParser.Pr_FloatPrimaryExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_NetFunctionTimes(@NotNull CfgParser.Pr_NetFunctionTimesContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_NetFunctionTimes(@NotNull CfgParser.Pr_NetFunctionTimesContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_HStringValue(@NotNull CfgParser.Pr_HStringValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_HStringValue(@NotNull CfgParser.Pr_HStringValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_IntegerUnaryExpression(@NotNull CfgParser.Pr_IntegerUnaryExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_IntegerUnaryExpression(@NotNull CfgParser.Pr_IntegerUnaryExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_FloatUnaryExpression(@NotNull CfgParser.Pr_FloatUnaryExpressionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_FloatUnaryExpression(@NotNull CfgParser.Pr_FloatUnaryExpressionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MatchingHintsValue(@NotNull CfgParser.Pr_MatchingHintsValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MatchingHintsValue(@NotNull CfgParser.Pr_MatchingHintsValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_IncludeSection(@NotNull CfgParser.Pr_IncludeSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_IncludeSection(@NotNull CfgParser.Pr_IncludeSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_HString(@NotNull CfgParser.Pr_HStringContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_HString(@NotNull CfgParser.Pr_HStringContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_PatternChunkList(@NotNull CfgParser.Pr_PatternChunkListContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_PatternChunkList(@NotNull CfgParser.Pr_PatternChunkListContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_UniversalOrNotStringValue(@NotNull CfgParser.Pr_UniversalOrNotStringValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_UniversalOrNotStringValue(@NotNull CfgParser.Pr_UniversalOrNotStringValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_Detailed(@NotNull CfgParser.Pr_DetailedContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_Detailed(@NotNull CfgParser.Pr_DetailedContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_NetLineTimes(@NotNull CfgParser.Pr_NetLineTimesContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_NetLineTimes(@NotNull CfgParser.Pr_NetLineTimesContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ObjIdComponent(@NotNull CfgParser.Pr_ObjIdComponentContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ObjIdComponent(@NotNull CfgParser.Pr_ObjIdComponentContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ExternalCommandsSection(@NotNull CfgParser.Pr_ExternalCommandsSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ExternalCommandsSection(@NotNull CfgParser.Pr_ExternalCommandsSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_TestportName(@NotNull CfgParser.Pr_TestportNameContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_TestportName(@NotNull CfgParser.Pr_TestportNameContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_OStringValue(@NotNull CfgParser.Pr_OStringValueContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_OStringValue(@NotNull CfgParser.Pr_OStringValueContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_DefaultSection(@NotNull CfgParser.Pr_DefaultSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_DefaultSection(@NotNull CfgParser.Pr_DefaultSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LogEventTypeSet(@NotNull CfgParser.Pr_LogEventTypeSetContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LogEventTypeSet(@NotNull CfgParser.Pr_LogEventTypeSetContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LoggerPluginEntry(@NotNull CfgParser.Pr_LoggerPluginEntryContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LoggerPluginEntry(@NotNull CfgParser.Pr_LoggerPluginEntryContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_deprecatedEventTypeSet(@NotNull CfgParser.Pr_deprecatedEventTypeSetContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_deprecatedEventTypeSet(@NotNull CfgParser.Pr_deprecatedEventTypeSetContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LengthBound(@NotNull CfgParser.Pr_LengthBoundContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LengthBound(@NotNull CfgParser.Pr_LengthBoundContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_StatisticsFilter(@NotNull CfgParser.Pr_StatisticsFilterContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_StatisticsFilter(@NotNull CfgParser.Pr_StatisticsFilterContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_DisableProfiler(@NotNull CfgParser.Pr_DisableProfilerContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_DisableProfiler(@NotNull CfgParser.Pr_DisableProfilerContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MacroAssignment(@NotNull CfgParser.Pr_MacroAssignmentContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MacroAssignment(@NotNull CfgParser.Pr_MacroAssignmentContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LoggingSection(@NotNull CfgParser.Pr_LoggingSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LoggingSection(@NotNull CfgParser.Pr_LoggingSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LogfileName(@NotNull CfgParser.Pr_LogfileNameContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LogfileName(@NotNull CfgParser.Pr_LogfileNameContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_TemplateItemList(@NotNull CfgParser.Pr_TemplateItemListContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_TemplateItemList(@NotNull CfgParser.Pr_TemplateItemListContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_StartAutomatically(@NotNull CfgParser.Pr_StartAutomaticallyContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_StartAutomatically(@NotNull CfgParser.Pr_StartAutomaticallyContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ComponentsSection(@NotNull CfgParser.Pr_ComponentsSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ComponentsSection(@NotNull CfgParser.Pr_ComponentsSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MainControllerItemNumHcs(@NotNull CfgParser.Pr_MainControllerItemNumHcsContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MainControllerItemNumHcs(@NotNull CfgParser.Pr_MainControllerItemNumHcsContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_Quadruple(@NotNull CfgParser.Pr_QuadrupleContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_Quadruple(@NotNull CfgParser.Pr_QuadrupleContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ProfilerSection(@NotNull CfgParser.Pr_ProfilerSectionContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ProfilerSection(@NotNull CfgParser.Pr_ProfilerSectionContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_HStringMatch(@NotNull CfgParser.Pr_HStringMatchContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_HStringMatch(@NotNull CfgParser.Pr_HStringMatchContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_MacroExpliciteCString(@NotNull CfgParser.Pr_MacroExpliciteCStringContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_MacroExpliciteCString(@NotNull CfgParser.Pr_MacroExpliciteCStringContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ComponentName(@NotNull CfgParser.Pr_ComponentNameContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ComponentName(@NotNull CfgParser.Pr_ComponentNameContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_OStringMatch(@NotNull CfgParser.Pr_OStringMatchContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_OStringMatch(@NotNull CfgParser.Pr_OStringMatchContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_GroupItem(@NotNull CfgParser.Pr_GroupItemContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_GroupItem(@NotNull CfgParser.Pr_GroupItemContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_LoggerPluginsPart(@NotNull CfgParser.Pr_LoggerPluginsPartContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_LoggerPluginsPart(@NotNull CfgParser.Pr_LoggerPluginsPartContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_ParameterValueOrNotUsedSymbol(@NotNull CfgParser.Pr_ParameterValueOrNotUsedSymbolContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_ParameterValueOrNotUsedSymbol(@NotNull CfgParser.Pr_ParameterValueOrNotUsedSymbolContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPr_Number(@NotNull CfgParser.Pr_NumberContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPr_Number(@NotNull CfgParser.Pr_NumberContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterEveryRule(@NotNull ParserRuleContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitEveryRule(@NotNull ParserRuleContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void visitTerminal(@NotNull TerminalNode node) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void visitErrorNode(@NotNull ErrorNode node) { }
}