package org.eclipse.titan.designer.parsers;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.ActivatorUtil_V4;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_Parser;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_Definition;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition;
import org.eclipse.titan.designer.AST.ASN1.Object.Object_Definition;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Integer_Type;
import org.eclipse.titan.designer.AST.ASN1.values.Undefined_Block_Value;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErroneousAttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.definitions.DefinitionUtil_V4;
import org.eclipse.titan.designer.AST.TTCN3.types.CompFieldMap;
import org.eclipse.titan.designer.AST.TTCN3.types.CompFieldMap_V4;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_Parser_V4;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_Definition_V4;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition_V4;
import org.eclipse.titan.designer.AST.ASN1.definitions.SpecialASN1Module_V4;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Integer_Type_V4;
import org.eclipse.titan.designer.AST.ASN1.values.Undefined_Block_Value_V4;
import org.eclipse.titan.designer.editors.asn1editor.ASN1ReferenceParser_V4;
import org.eclipse.titan.designer.parsers.ProjectSourceSyntacticAnalyzer_V4;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Analyzer_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Analyzer_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReferenceAnalyzer_V4;
import org.eclipse.titan.designer.editors.asn1editor.ASN1ReferenceParser;
import org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeAnalyzer;
import org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeAnalyzer_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ITtcn3FileReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReferenceAnalyzer;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3FileReparser_V4;

/**
 * Factory to create ANTLR 4 specific parsers and data types, values
 * @author Arpad Lovassy
 */
public class ParserFactory {

	public static ASN1ReferenceParser createASN1ReferenceParser() {
		return new ASN1ReferenceParser_V4();
	}
	
	public static TTCN3ReferenceAnalyzer createTTCN3ReferenceAnalyzer() {
		return new TTCN3ReferenceAnalyzer_V4();
	}
	
	public static ISourceAnalyzer createASN1Analyzer(){
		return new ASN1Analyzer_V4();
	}
	
	public static ISourceAnalyzer createTTCN3Analyzer() {
		return new TTCN3Analyzer_V4();
	}

	public static ObjectSet_definition createObjectSetDefinition() {
		return new ObjectSet_definition_V4();
	}

	public static ObjectSet createObjectSetDefinition(Block aSetting) {
		return new ObjectSet_definition_V4((BlockV4)aSetting);
	}

	public static ObjectClass_Definition createObjectClassDefinition() {
		return new ObjectClass_Definition_V4();
	}
	
	public static ObjectClassSyntax_Parser createObjectClassSyntaxParser( final Block aBlock, final Object_Definition aObject ) {
		return new ObjectClassSyntax_Parser_V4((BlockV4)aBlock, aObject);
	}
	
	public static Undefined_Block_Value createUndefinedBlockValue( final Block aBlock ) {
		return new Undefined_Block_Value_V4( (BlockV4)aBlock );
	}
	
	public static ProjectSourceSyntacticAnalyzer createProjectSourceSyntacticAnalyzer( final IProject aProject, final ProjectSourceParser aSourceParser ) {
		return new ProjectSourceSyntacticAnalyzer_V4( aProject, aSourceParser );
	}
	
	public static ASN1_Integer_Type createASN1IntegerType() {
		return new ASN1_Integer_Type_V4();
	}
	
	public static ASN1Assignment parseSpecialInternalAssignment( final String aInputCode, final Identifier aIdentifier ) {
		return SpecialASN1Module_V4.parseSpecialInternalAssignment( aInputCode, aIdentifier );
	}
	
	public static IIdentifierReparser createIdentifierReparser( final TTCN3ReparseUpdater aReparser ) {
		return new IdentifierReparser_V4( aReparser );
	}

	public static ITtcn3FileReparser createTtcn3FileReparser( final TTCN3ReparseUpdater aReparser,
															  final IFile aFile,
															  final ProjectSourceParser aSourceParser,
															  final Map<IFile, String> aFileMap,
															  final Map<IFile, String> aUptodateFiles,
															  final Set<IFile> aHighlySyntaxErroneousFiles ) {
		return new Ttcn3FileReparser_V4( aReparser, aFile, aSourceParser, aFileMap, aUptodateFiles, aHighlySyntaxErroneousFiles );
	}
	
	public static TTCN3ReparseUpdater createTTCN3ReparseUpdater( final IFile file, final String code, int firstLine, int lineShift, int startOffset, int endOffset, int shift) {
		return new TTCN3ReparseUpdater_V4( file, code, firstLine, lineShift, startOffset, endOffset, shift);
	}
	
	public static CompFieldMap createCompFieldMap() {
		return new CompFieldMap_V4();
	}

	public static ErroneousAttributeSpecification parseErrAttrSpecString( final AttributeSpecification aAttrSpec ) {
		return DefinitionUtil_V4.parseErrAttrSpecString( aAttrSpec );
	}

	public static ExtensionAttributeAnalyzer createExtensionAttributeAnalyzer() {
		return new ExtensionAttributeAnalyzer_V4();
	}
	
	public static ProjectConfigurationParser createProjectConfigurationParser( final IProject aProject ) {
		return new ProjectConfigurationParser_V4( aProject );
	}

	public static void actvatorPreload() {
		ActivatorUtil_V4.activatorPreload();
	}
	
	/**  @return true if the "Use ANTLR V4" text and checkbox is visible in the debug settings */
	public static boolean isUseAntlrV4Visible() {
		return false;
	}
}
