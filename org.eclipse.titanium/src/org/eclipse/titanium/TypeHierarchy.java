/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.AtNotation;
import org.eclipse.titan.designer.AST.AtNotations;
import org.eclipse.titan.designer.AST.Constraint;
import org.eclipse.titan.designer.AST.Constraints;
import org.eclipse.titan.designer.AST.Error_Setting;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.Governed;
import org.eclipse.titan.designer.AST.GovernedSet;
import org.eclipse.titan.designer.AST.GovernedSimple;
import org.eclipse.titan.designer.AST.Governor;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.KeywordLessIdentifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NamedBridgeScope;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Setting;
import org.eclipse.titan.designer.AST.TemporalReference;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignments;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Ass_pard;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.Defined_Reference;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.InformationFromObj;
import org.eclipse.titan.designer.AST.ASN1.ObjectClass;
import org.eclipse.titan.designer.AST.ASN1.ObjectClass_Assignment;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.AST.ASN1.ObjectSetElement_Visitor;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet_Assignment;
import org.eclipse.titan.designer.AST.ASN1.Object_Assignment;
import org.eclipse.titan.designer.AST.ASN1.Parameterised_Reference;
import org.eclipse.titan.designer.AST.ASN1.TableConstraint;
import org.eclipse.titan.designer.AST.ASN1.Type_Assignment;
import org.eclipse.titan.designer.AST.ASN1.Undefined_Assignment;
import org.eclipse.titan.designer.AST.ASN1.Undefined_Assignment_OS_or_VS;
import org.eclipse.titan.designer.AST.ASN1.Undefined_Assignment_O_or_V;
import org.eclipse.titan.designer.AST.ASN1.Undefined_Assignment_T_or_OC;
import org.eclipse.titan.designer.AST.ASN1.ValueSet_Assignment;
import org.eclipse.titan.designer.AST.ASN1.Value_Assignment;
import org.eclipse.titan.designer.AST.ASN1.Object.ASN1Objects;
import org.eclipse.titan.designer.AST.ASN1.Object.Erroneous_FieldSpecification;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldName;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldSetting;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldSetting_Object;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldSetting_ObjectSet;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldSetting_Type;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldSetting_Value;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldSpecification;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldSpecifications;
import org.eclipse.titan.designer.AST.ASN1.Object.FixedTypeValue_FieldSpecification;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_Definition;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_refd;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSetElementVisitor_checker;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSetElementVisitor_objectCollector;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_FieldSpecification;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition;
import org.eclipse.titan.designer.AST.ASN1.Object.Object_Definition;
import org.eclipse.titan.designer.AST.ASN1.Object.Object_FieldSpecification;
import org.eclipse.titan.designer.AST.ASN1.Object.ReferencedObject;
import org.eclipse.titan.designer.AST.ASN1.Object.Referenced_ObjectSet;
import org.eclipse.titan.designer.AST.ASN1.Object.Type_FieldSpecification;
import org.eclipse.titan.designer.AST.ASN1.Object.Undefined_FieldSpecification;
import org.eclipse.titan.designer.AST.ASN1.definitions.ASN1Module;
import org.eclipse.titan.designer.AST.ASN1.definitions.Exports;
import org.eclipse.titan.designer.AST.ASN1.definitions.Imports;
import org.eclipse.titan.designer.AST.ASN1.definitions.Symbols;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_BitString_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Enumerated_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Integer_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.ASN1.types.Any_Type;
import org.eclipse.titan.designer.AST.ASN1.types.BMPString_Type;
import org.eclipse.titan.designer.AST.ASN1.types.CTs_EE_CTs;
import org.eclipse.titan.designer.AST.ASN1.types.ComponentType;
import org.eclipse.titan.designer.AST.ASN1.types.ComponentTypeList;
import org.eclipse.titan.designer.AST.ASN1.types.ComponentsOfComponentType;
import org.eclipse.titan.designer.AST.ASN1.types.Embedded_PDV_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ExceptionSpecification;
import org.eclipse.titan.designer.AST.ASN1.types.ExtensionAddition;
import org.eclipse.titan.designer.AST.ASN1.types.ExtensionAdditionGroup;
import org.eclipse.titan.designer.AST.ASN1.types.ExtensionAdditions;
import org.eclipse.titan.designer.AST.ASN1.types.ExtensionAndException;
import org.eclipse.titan.designer.AST.ASN1.types.External_Type;
import org.eclipse.titan.designer.AST.ASN1.types.GeneralString_Type;
import org.eclipse.titan.designer.AST.ASN1.types.GeneralizedTime_Type;
import org.eclipse.titan.designer.AST.ASN1.types.GraphicString_Type;
import org.eclipse.titan.designer.AST.ASN1.types.IA5String_Type;
import org.eclipse.titan.designer.AST.ASN1.types.NULL_Type;
import org.eclipse.titan.designer.AST.ASN1.types.NumericString_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ObjectClassField_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ObjectDescriptor_Type;
import org.eclipse.titan.designer.AST.ASN1.types.Open_Type;
import org.eclipse.titan.designer.AST.ASN1.types.PrintableString_Type;
import org.eclipse.titan.designer.AST.ASN1.types.RegularComponentType;
import org.eclipse.titan.designer.AST.ASN1.types.RelativeObjectIdentifier_Type;
import org.eclipse.titan.designer.AST.ASN1.types.Selection_Type;
import org.eclipse.titan.designer.AST.ASN1.types.TeletexString_Type;
import org.eclipse.titan.designer.AST.ASN1.types.UTCTime_Type;
import org.eclipse.titan.designer.AST.ASN1.types.UTF8String_Type;
import org.eclipse.titan.designer.AST.ASN1.types.UniversalString_Type;
import org.eclipse.titan.designer.AST.ASN1.types.UnrestrictedString_Type;
import org.eclipse.titan.designer.AST.ASN1.types.VideotexString_Type;
import org.eclipse.titan.designer.AST.ASN1.types.VisibleString_Type;
import org.eclipse.titan.designer.AST.ASN1.values.ASN1_Null_Value;
import org.eclipse.titan.designer.AST.ASN1.values.Charsymbols_Value;
import org.eclipse.titan.designer.AST.ASN1.values.ISO2022String_Value;
import org.eclipse.titan.designer.AST.ASN1.values.Named_Bits;
import org.eclipse.titan.designer.AST.ASN1.values.Named_Integer_Value;
import org.eclipse.titan.designer.AST.ASN1.values.RelativeObjectIdentifier_Value;
import org.eclipse.titan.designer.AST.ASN1.values.Undefined_Block_Value;
import org.eclipse.titan.designer.AST.TTCN3.TTCN3Scope;
import org.eclipse.titan.designer.AST.TTCN3.attributes.DecodeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.DecodeTypeMappingTarget;
import org.eclipse.titan.designer.AST.TTCN3.attributes.DiscardTypeMappingTarget;
import org.eclipse.titan.designer.AST.TTCN3.attributes.EncodeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.EncodeTypeMappingTarget;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErroneousAttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErroneousAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErrorBehaviorAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErrorBehaviorList;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErrorBehaviorSetting;
import org.eclipse.titan.designer.AST.TTCN3.attributes.FunctionTypeMappingTarget;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifier;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SimpleTypeMappingTarget;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.TypeMapping;
import org.eclipse.titan.designer.AST.TTCN3.attributes.TypeMappingTarget;
import org.eclipse.titan.designer.AST.TTCN3.attributes.TypeMappingTargets;
import org.eclipse.titan.designer.AST.TTCN3.attributes.TypeMappings;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Types;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ControlPart;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ExternalConst;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Default_ActualParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definitions;
import org.eclipse.titan.designer.AST.TTCN3.definitions.For_Loop_Definitions;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FriendModule;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Group;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ImportModule;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Referenced_ActualParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.RunsOnScope;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Template_ActualParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TestcaseFormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Value_ActualParameter;
import org.eclipse.titan.designer.AST.TTCN3.statements.Action_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Activate_Referenced_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Activate_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuards;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Altstep_Applied_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Altstep_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.AssignmentList_Parameter_Redirect;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Break_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Call_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Catch_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Catch_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Getcall_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Getreply_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Clear_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Connect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Continue_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Deactivate_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Definition_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Disconnect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.DoWhile_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Done_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Else_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Function_Applied_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Function_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Getcall_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Getreply_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Goto_statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Halt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clause;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clauses;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Interleave_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Invoke_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Kill_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Killed_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Label_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.LogArgument;
import org.eclipse.titan.designer.AST.TTCN3.statements.LogArguments;
import org.eclipse.titan.designer.AST.TTCN3.statements.Log_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Map_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Operation_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Parameter_Assignment;
import org.eclipse.titan.designer.AST.TTCN3.statements.Parameter_Assignments;
import org.eclipse.titan.designer.AST.TTCN3.statements.Parameter_Redirect;
import org.eclipse.titan.designer.AST.TTCN3.statements.Raise_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Referenced_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Referenced_Testcase_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Repeat_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Reply_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Return_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCases;
import org.eclipse.titan.designer.AST.TTCN3.statements.Send_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Setverdict_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Start_Component_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Start_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Start_Referenced_Component_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Start_Timer_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Stop_Component_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Stop_Execution_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Stop_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Stop_Timer_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.TestcaseStop_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Testcase_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Timeout_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Trigger_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Applied_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Start_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Stop_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unmap_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.VariableList_Parameter_Redirect;
import org.eclipse.titan.designer.AST.TTCN3.statements.Variable_Entries;
import org.eclipse.titan.designer.AST.TTCN3.statements.Variable_Entry;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;
import org.eclipse.titan.designer.AST.TTCN3.templates.AnyOrOmit_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Any_Value_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.BitString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.CharString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ComplementedList_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.HexString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.IndexedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.IndexedTemplates;
import org.eclipse.titan.designer.AST.TTCN3.templates.Indexed_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.Invoke_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.LengthRestriction;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedParameter;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedParameters;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplates;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.NotUsed_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.OctetString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.OmitValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString;
import org.eclipse.titan.designer.AST.TTCN3.templates.PermutationMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.RangeLenghtRestriction;
import org.eclipse.titan.designer.AST.TTCN3.templates.Referenced_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SingleLenghtRestriction;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SubsetMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SupersetMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstances;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.Templates;
import org.eclipse.titan.designer.AST.TTCN3.templates.UnivCharString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueList_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueRange;
import org.eclipse.titan.designer.AST.TTCN3.templates.Value_Range_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.Address_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Altstep_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Anytype_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.BitString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Boolean_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.CompFieldMap;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeReferenceList;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Default_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.EnumItem;
import org.eclipse.titan.designer.AST.TTCN3.types.EnumerationItems;
import org.eclipse.titan.designer.AST.TTCN3.types.Float_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.HexString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.ObjectID_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.OctetString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SequenceOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureExceptions;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Enumerated_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Testcase_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeSet;
import org.eclipse.titan.designer.AST.TTCN3.types.UniversalCharstring_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Verdict_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.Length_ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.Pattern_ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.Range_ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.Single_ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Altstep_Reference_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Anytype_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.Array_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Choice_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Default_Null_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Enumerated_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.FAT_Null_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Function_Reference_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.IndexedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Macro_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValues;
import org.eclipse.titan.designer.AST.TTCN3.values.Notused_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.ObjectIdentifierComponent;
import org.eclipse.titan.designer.AST.TTCN3.values.ObjectIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.RangedArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Set_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SingleArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.TTCN3_Null_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Testcase_Reference_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Values;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.*;

/**
 * This class actually holds the type hierarchy of the AST nodes, starting from
 * the {@link IVisitableNode} class. This is a dangerous but rewarding
 * optimization. Dangerous, because it needs to be updated any time the designer
 * is extended with a new subclass of <code>IVisitableNode</code>. It seems
 * nevertheless necessary, as omitting this shortcut resulted apprx. 4-7 times
 * slowdown in experiments.
 *
 * @author poroszd
 *
 */
public final class TypeHierarchy {
	private TypeHierarchy() {
		throw new AssertionError("Noninstantiable");
	}

	@SuppressWarnings("unchecked")
	public static Map<Class<? extends IVisitableNode>, Class<? extends IVisitableNode>[]> createHierarchy() {
		Map<Class<? extends IVisitableNode>, Class<? extends IVisitableNode>[]> m =
				new HashMap<Class<? extends IVisitableNode>, Class<? extends IVisitableNode>[]>();
		m.put(IValue.class, new Class[] { Value.class });
		m.put(ArrayDimension.class, new Class[] { RangedArrayDimension.class, SingleArrayDimension.class });
		m.put(IVisitableNode.class, new Class[] { ImportModule.class, Symbols.class, Block.class, Ass_pard.class, Scope.class,
				Identifier.class, ASTNode.class, IType.class, ImportModule.class, ParsedSubType.class, Qualifier.class,
				EncodeAttribute.class, ErroneousAttributes.class, Qualifiers.class, ErroneousAttributeSpecification.class,
				ErrorBehaviorAttribute.class, DecodeAttribute.class, MultipleWithAttributes.class, SingleWithAttribute.class,
				WithAttributesPath.class, PatternString.class, ISubReference.class, IValue.class });
		m.put(GovernedSet.class, new Class[] { ObjectSet.class });
		m.put(GovernedSimple.class, new Class[] { Value.class, TTCN3Template.class });
		m.put(ComponentType.class, new Class[] { RegularComponentType.class, ComponentsOfComponentType.class });
		m.put(Setting.class, new Class[] { Governor.class, Error_Setting.class, Governed.class });
		m.put(ActualParameter.class, new Class[] { Referenced_ActualParameter.class, Template_ActualParameter.class,
				Default_ActualParameter.class, Value_ActualParameter.class });
		m.put(Statement.class, new Class[] { Continue_Statement.class, Testcase_Instance_Statement.class, Definition_Statement.class,
				Check_Port_Statement.class, Check_Getreply_Statement.class, Halt_Statement.class, Done_Statement.class,
				Deactivate_Statement.class, Kill_Statement.class, Stop_Execution_Statement.class, Check_Receive_Port_Statement.class,
				Raise_Statement.class, Label_Statement.class, Assignment_Statement.class, Altstep_Applied_Statement.class,
				Interleave_Statement.class, If_Statement.class, Referenced_Testcase_Instance_Statement.class,
				Start_Timer_Statement.class, Log_Statement.class, Activate_Statement.class, Setverdict_Statement.class,
				Alt_Statement.class, Clear_Statement.class, Connect_Statement.class, Getcall_Statement.class, Break_Statement.class,
				Start_Referenced_Component_Statement.class, Goto_statement.class, Stop_Timer_Statement.class, Killed_Statement.class,
				StatementBlock_Statement.class, For_Statement.class, Unknown_Instance_Statement.class,
				Unknown_Applied_Statement.class, Function_Instance_Statement.class, Altstep_Instance_Statement.class,
				Call_Statement.class, Check_Getcall_Statement.class, SelectCase_Statement.class, Timeout_Statement.class,
				Disconnect_Statement.class, Unknown_Stop_Statement.class, Send_Statement.class, Unknown_Start_Statement.class,
				Stop_Port_Statement.class, Start_Component_Statement.class, TestcaseStop_Statement.class,
				Receive_Port_Statement.class, Return_Statement.class, Trigger_Port_Statement.class, Unmap_Statement.class,
				Reply_Statement.class, Repeat_Statement.class, While_Statement.class, Check_Catch_Statement.class,
				Start_Port_Statement.class, Catch_Statement.class, Activate_Referenced_Statement.class,
				Stop_Component_Statement.class, Map_Statement.class, Action_Statement.class, Function_Applied_Statement.class,
				DoWhile_Statement.class, Getreply_Statement.class });
		m.put(TTCN3Template.class, new Class[] { OmitValue_Template.class, CharString_Pattern_Template.class, AnyOrOmit_Template.class,
				ComplementedList_Template.class, HexString_Pattern_Template.class, Named_Template_List.class,
				PermutationMatch_Template.class, SupersetMatch_Template.class, Invoke_Template.class, Referenced_Template.class,
				ValueList_Template.class, SpecificValue_Template.class, Indexed_Template_List.class,
				BitString_Pattern_Template.class, OctetString_Pattern_Template.class, SubsetMatch_Template.class,
				UnivCharString_Pattern_Template.class, Template_List.class, Any_Value_Template.class, Value_Range_Template.class,
				NotUsed_Template.class });
		m.put(Scope.class, new Class[] { NamedBridgeScope.class, Module.class, Assignments.class, TTCN3Scope.class, ControlPart.class });
		m.put(ASN1Assignment.class, new Class[] { Object_Assignment.class, Type_Assignment.class, Value_Assignment.class,
				Undefined_Assignment.class, ObjectClass_Assignment.class, ValueSet_Assignment.class, ObjectSet_Assignment.class });
		m.put(Type.class, new Class[] { ASN1Type.class, Integer_Type.class, TTCN3_Set_Seq_Choice_BaseType.class, Signature_Type.class,
				Function_Type.class, Altstep_Type.class, Array_Type.class, Address_Type.class, TTCN3_Enumerated_Type.class,
				Component_Type.class, Default_Type.class, HexString_Type.class, UniversalCharstring_Type.class,
				CharString_Type.class, BitString_Type.class, Verdict_Type.class, Port_Type.class, Anytype_Type.class,
				Testcase_Type.class });
		m.put(ParsedSubType.class, new Class[] { Pattern_ParsedSubType.class, Range_ParsedSubType.class, Single_ParsedSubType.class,
				Length_ParsedSubType.class });
		m.put(ASTNode.class, new Class[] { Exports.class, Imports.class, ObjectSetElement_Visitor.class, FieldSpecification.class,
				FieldSetting.class, ASN1Objects.class, FieldSpecifications.class, FieldName.class, CTs_EE_CTs.class,
				ExtensionAndException.class, ExceptionSpecification.class, ComponentTypeList.class, ExtensionAdditions.class,
				ExtensionAddition.class, Constraints.class, Setting.class, Reference.class, AtNotations.class, Assignment.class,
				ParameterisedSubReference.class, Constraint.class, AtNotation.class, ArraySubReference.class,
				ObjectIdentifierComponent.class, Values.class, ArrayDimension.class, ArrayDimensions.class, NamedValues.class,
				IndexedValue.class, NamedValue.class, ActualParameterList.class, Group.class, FriendModule.class,
				ActualParameter.class, SignatureFormalParameterList.class, SignatureFormalParameter.class, SignatureExceptions.class,
				CompField.class, EnumItem.class, PortTypeBody.class, TypeSet.class, ComponentTypeReferenceList.class,
				EnumerationItems.class, CompFieldMap.class, TypeMapping.class, TypeMappings.class, TypeMappingTargets.class,
				Types.class, ErrorBehaviorSetting.class, TypeMappingTarget.class, ErrorBehaviorList.class, LengthRestriction.class,
				NamedParameters.class, IndexedTemplates.class, TemplateInstance.class, Templates.class, ValueRange.class,
				NamedParameter.class, NamedTemplate.class, NamedTemplates.class, ParsedActualParameters.class,
				TemplateInstances.class, IndexedTemplate.class, LogArgument.class, If_Clause.class, AltGuard.class,
				SelectCases.class, Parameter_Redirect.class, If_Clauses.class, Variable_Entries.class, Parameter_Assignment.class,
				Parameter_Assignments.class, Statement.class, AltGuards.class, LogArguments.class, Variable_Entry.class,
				SelectCase.class });
		m.put(ObjectSetElement_Visitor.class, new Class[] { ObjectSetElementVisitor_checker.class,
				ObjectSetElementVisitor_objectCollector.class });
		m.put(ObjectClass.class, new Class[] { ObjectClass_refd.class, ObjectClass_Definition.class });
		m.put(FieldSpecification.class, new Class[] { FixedTypeValue_FieldSpecification.class, Erroneous_FieldSpecification.class,
				Type_FieldSpecification.class, ObjectSet_FieldSpecification.class, Undefined_FieldSpecification.class,
				Object_FieldSpecification.class });
		m.put(ExtensionAddition.class, new Class[] { ExtensionAdditionGroup.class, ComponentType.class });
		m.put(ASN1Object.class, new Class[] { ReferencedObject.class, Object_Definition.class });
		m.put(IType.class, new Class[] { IASN1Type.class, Type.class });
		m.put(ObjectSet.class, new Class[] { ObjectSet_definition.class, Referenced_ObjectSet.class });
		m.put(AltGuard.class, new Class[] { Referenced_Altguard.class, Operation_Altguard.class, Else_Altguard.class,
				Invoke_Altguard.class });
		m.put(Definition.class, new Class[] { Def_Timer.class, Def_Template.class, Def_Var.class, FormalParameter.class,
				Def_ExternalConst.class, Def_Altstep.class, Def_Testcase.class, Def_Function.class, Def_Type.class, Def_Port.class,
				Def_Const.class, Def_Var_Template.class, Def_ModulePar.class, Def_Extfunction.class });
		m.put(FieldSetting.class, new Class[] { FieldSetting_ObjectSet.class, FieldSetting_Object.class, FieldSetting_Type.class,
				FieldSetting_Value.class });
		m.put(Constraint.class, new Class[] { TableConstraint.class });
		m.put(TTCN3_Set_Seq_Choice_BaseType.class, new Class[] { TTCN3_Choice_Type.class, TTCN3_Set_Type.class, TTCN3_Sequence_Type.class });
		m.put(Value.class, new Class[] { RelativeObjectIdentifier_Value.class, Named_Bits.class, Named_Integer_Value.class,
				ASN1_Null_Value.class, ISO2022String_Value.class, Charsymbols_Value.class, Undefined_Block_Value.class,
				Macro_Value.class, Referenced_Value.class, Sequence_Value.class, Expression_Value.class, Hexstring_Value.class,
				Boolean_Value.class, UniversalCharstring_Value.class, Testcase_Reference_Value.class,
				Undefined_LowerIdentifier_Value.class, Set_Value.class, Real_Value.class, Octetstring_Value.class,
				Notused_Value.class, Default_Null_Value.class, TTCN3_Null_Value.class, Charstring_Value.class,
				Altstep_Reference_Value.class, SequenceOf_Value.class, Array_Value.class, Bitstring_Value.class, Verdict_Value.class,
				Omit_Value.class, Enumerated_Value.class, FAT_Null_Value.class, Function_Reference_Value.class, Choice_Value.class,
				Integer_Value.class, ObjectIdentifier_Value.class, Anytype_Value.class, SetOf_Value.class });
		m.put(Governor.class, new Class[] { ObjectClass.class, Type.class });
		m.put(TTCN3Scope.class, new Class[] { FormalParameterList.class, RunsOnScope.class, ComponentTypeBody.class, StatementBlock.class });
		m.put(Assignments.class, new Class[] { ASN1Assignments.class, For_Loop_Definitions.class, Definitions.class });
		m.put(Governed.class, new Class[] { ASN1Object.class, GovernedSet.class, GovernedSimple.class });
		m.put(FormalParameterList.class, new Class[] { TestcaseFormalParameterList.class });
		m.put(Identifier.class, new Class[] { KeywordLessIdentifier.class });
		m.put(Defined_Reference.class, new Class[] { Parameterised_Reference.class });
		m.put(Reference.class, new Class[] { Defined_Reference.class, InformationFromObj.class, TemporalReference.class });
		m.put(TypeMappingTarget.class, new Class[] { SimpleTypeMappingTarget.class, EncodeTypeMappingTarget.class,
				DecodeTypeMappingTarget.class, DiscardTypeMappingTarget.class, FunctionTypeMappingTarget.class });
		m.put(ASN1Type.class, new Class[] { UniversalString_Type.class, Open_Type.class, Any_Type.class, NumericString_Type.class,
				External_Type.class, UTF8String_Type.class, UnrestrictedString_Type.class, Embedded_PDV_Type.class,
				IA5String_Type.class, ObjectDescriptor_Type.class, RelativeObjectIdentifier_Type.class, BMPString_Type.class,
				ASN1_Set_Seq_Choice_BaseType.class, ASN1_Integer_Type.class, PrintableString_Type.class, NULL_Type.class,
				GraphicString_Type.class, GeneralizedTime_Type.class, UTCTime_Type.class, VisibleString_Type.class,
				ObjectClassField_Type.class, VideotexString_Type.class, TeletexString_Type.class, GeneralString_Type.class,
				Selection_Type.class, ASN1_BitString_Type.class, ASN1_Enumerated_Type.class, SequenceOf_Type.class,
				Referenced_Type.class, ObjectID_Type.class, SetOf_Type.class, OctetString_Type.class, Float_Type.class,
				Boolean_Type.class });
		m.put(Module.class, new Class[] { ASN1Module.class, TTCN3Module.class });
		m.put(LengthRestriction.class, new Class[] { RangeLenghtRestriction.class, SingleLenghtRestriction.class });
		m.put(Undefined_Assignment.class, new Class[] { Undefined_Assignment_O_or_V.class, Undefined_Assignment_OS_or_VS.class,
				Undefined_Assignment_T_or_OC.class });
		m.put(IASN1Type.class, new Class[] { ASN1Type.class });
		m.put(Expression_Value.class, new Class[] { RotateRightExpression.class, EqualsExpression.class, ExecuteExpression.class,
				Unichar2CharExpression.class, NotequalesExpression.class, AllComponentAliveExpression.class,
				Not4bExpression.class, LengthofExpression.class, UnaryPlusExpression.class, Int2HexExpression.class,
				Log2StrExpression.class, Int2BitExpression.class, ComponentAliveExpression.class, Oct2CharExpression.class,
				AnyComponentRunningExpression.class, ExecuteDereferedExpression.class, Hex2IntExpression.class,
				Oct2HexExpression.class, TimerReadExpression.class, Int2UnicharExpression.class, RotateLeftExpression.class,
				Int2OctExpression.class, XorExpression.class, IsValueExpression.class, Bit2IntExpression.class,
				AllComponentRunningExpression.class, Str2IntExpression.class, NotExpression.class, ReplaceExpression.class,
				SystemComponentExpression.class, Or4bExpression.class, Hex2BitExpression.class,
				ActivateDereferedExpression.class, Bit2StrExpression.class, SelfComponentExpression.class,
				Enum2IntExpression.class, ShiftLeftExpression.class, SubstractExpression.class,
				StringConcatenationExpression.class, Int2CharExpression.class, GreaterThanOrEqualExpression.class,
				Str2BitExpression.class, Oct2StrExpression.class, GetverdictExpression.class, Int2StrExpression.class,
				DecompExpression.class, LessThanOrEqualExpression.class, Int2FloatExpression.class, Xor4bExpression.class,
				SubstrExpression.class, LessThanExpression.class, RegexpExpression.class, DecodeExpression.class,
				MTCComponentExpression.class, SizeOfExpression.class, Float2StrExpression.class, DivideExpression.class,
				ComponentNullExpression.class, AnyTimerRunningExpression.class, RNDExpression.class, Hex2OctExpression.class,
				UndefRunningExpression.class, ApplyExpression.class, Char2OctExpression.class, AndExpression.class,
				RemainderExpression.class, ComponentRunnningExpression.class, ShiftRightExpression.class,
				ActivateExpression.class, MultiplyExpression.class, Str2HexExpression.class, UnaryMinusExpression.class,
				And4bExpression.class, TestcasenameExpression.class, Str2OctExpression.class, IsBoundExpression.class,
				GreaterThanExpression.class, IsPresentExpression.class, AnyComponentAliveExpression.class,
				Bit2HexExpression.class, ValueofExpression.class, TimerRunningExpression.class, AddExpression.class,
				Hex2StrExpression.class, ComponentCreateExpression.class, Oct2IntExpression.class, Char2IntExpression.class,
				Oct2BitExpression.class, Float2IntExpression.class, IsChoosenExpression.class, MatchExpression.class,
				Str2FloatExpression.class, EncodeExpression.class, ModuloExpression.class, OrExpression.class,
				Bit2OctExpression.class, RNDWithValueExpression.class, RefersExpression.class, Unichar2IntExpression.class });
		m.put(Assignment.class, new Class[] { ASN1Assignment.class, Definition.class });
		m.put(ISubReference.class, new Class[] { ParameterisedSubReference.class, ArraySubReference.class, FieldSubReference.class });
		m.put(Parameter_Redirect.class, new Class[] { VariableList_Parameter_Redirect.class, AssignmentList_Parameter_Redirect.class });
		m.put(ASN1_Set_Seq_Choice_BaseType.class, new Class[] { ASN1_Sequence_Type.class, ASN1_Choice_Type.class, ASN1_Set_Type.class });
		return Collections.unmodifiableMap(m);
	}
}
