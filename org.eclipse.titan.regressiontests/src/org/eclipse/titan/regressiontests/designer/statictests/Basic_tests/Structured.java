package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;
import org.junit.Test;

public class Structured {

	@Test
	public void structured_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile( structured_ttcn_initializer(), "cfgFile/define/structured/structured.ttcn");
		Designer_plugin_tests.checkSyntaxMarkersOnFile(structured_cfg_initializer(), "cfgFile/define/structured/structured.cfg");
	}
	private ArrayList<MarkerToCheck> structured_ttcn_initializer() {
		//structured.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(35);
		int lineNum = 107;
		markersToCheck.add(new MarkerToCheck("The constant `@structured.CLASSMARK2_GSM_ONLY' with name CLASSMARK2_GSM_ONLY breaks the naming convention  `cg_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 26;
		markersToCheck.add(new MarkerToCheck("The constant `@structured.CR_DEFAULT' with name CR_DEFAULT breaks the naming convention  `cg_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 85;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 12;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	//structured_cfg
	//FIXME: These are not the normal markers, there are faulty!!! Plugin shall be repaired !!!
	private ArrayList<MarkerToCheck> structured_cfg_initializer() {
		//structured.cfg
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(101);
		int lineNum = 55;
		markersToCheck.add(new MarkerToCheck("extraneous input '}' expecting {<EOF>, MAIN_CONTROLLER_SECTION, INCLUDE_SECTION, ORDERED_INCLUDE_SECTION, EXECUTE_SECTION, DEFINE_SECTION, EXTERNAL_COMMANDS_SECTION, TESTPORT_PARAMETERS_SECTION, GROUPS_SECTION, MODULE_PARAMETERS_SECTION, COMPONENTS_SECTION, LOGGING_SECTION, PROFILER_SECTION, TTCN3IDENTIFIER5}",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("mismatched input '{' expecting ENDCHAR5",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("token recognition error at: '/$'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 16;
		markersToCheck.add(new MarkerToCheck("token recognition error at: '\\a'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 24;
		markersToCheck.add(new MarkerToCheck("extraneous input '{' expecting {<EOF>, MAIN_CONTROLLER_SECTION, INCLUDE_SECTION, ORDERED_INCLUDE_SECTION, EXECUTE_SECTION, DEFINE_SECTION, EXTERNAL_COMMANDS_SECTION, TESTPORT_PARAMETERS_SECTION, GROUPS_SECTION, MODULE_PARAMETERS_SECTION, COMPONENTS_SECTION, LOGGING_SECTION, PROFILER_SECTION, TTCN3IDENTIFIER5}",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("extraneous input '{' expecting ENDCHAR5",  lineNum, IMarker.SEVERITY_WARNING));
		int i = 0;
		for (i = 0; i < 4; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("mismatched input '${DEF_41}' expecting ENDCHAR5",  lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 3; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum, IMarker.SEVERITY_WARNING));
		}
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("token recognition error at: ','",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("missing ENDCHAR5 at 'f'",  ++lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 4; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum++, IMarker.SEVERITY_WARNING));
		}
		markersToCheck.add(new MarkerToCheck("extraneous input '}' expecting {<EOF>, MAIN_CONTROLLER_SECTION, INCLUDE_SECTION, ORDERED_INCLUDE_SECTION, EXECUTE_SECTION, DEFINE_SECTION, EXTERNAL_COMMANDS_SECTION, TESTPORT_PARAMETERS_SECTION, GROUPS_SECTION, MODULE_PARAMETERS_SECTION, COMPONENTS_SECTION, LOGGING_SECTION, PROFILER_SECTION, IPV6_5, TTCN3IDENTIFIER5, MACRORVALUE5, STRING5, MACRO_ID5, MACRO_INT5, MACRO_BOOL5, MACRO_FLOAT5, MACRO_EXP_CSTR5, MACRO_BSTR5, MACRO_HSTR5, MACRO_OSTR5, MACRO_BINARY5, MACRO_HOSTNAME5, MACRO5, BITSTRING5, HEXSTRING5, OCTETSTRING5, BITSTRINGMATCH5, HEXSTRINGMATCH5, OCTETSTRINGMATCH5}",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("token recognition error at: ','",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("missing ENDCHAR5 at 'f'",  ++lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 4; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum++, IMarker.SEVERITY_WARNING));
		}
		markersToCheck.add(new MarkerToCheck("extraneous input '}' expecting {<EOF>, MAIN_CONTROLLER_SECTION, INCLUDE_SECTION, ORDERED_INCLUDE_SECTION, EXECUTE_SECTION, DEFINE_SECTION, EXTERNAL_COMMANDS_SECTION, TESTPORT_PARAMETERS_SECTION, GROUPS_SECTION, MODULE_PARAMETERS_SECTION, COMPONENTS_SECTION, LOGGING_SECTION, PROFILER_SECTION, IPV6_5, TTCN3IDENTIFIER5, MACRORVALUE5, STRING5, MACRO_ID5, MACRO_INT5, MACRO_BOOL5, MACRO_FLOAT5, MACRO_EXP_CSTR5, MACRO_BSTR5, MACRO_HSTR5, MACRO_OSTR5, MACRO_BINARY5, MACRO_HOSTNAME5, MACRO5, BITSTRING5, HEXSTRING5, OCTETSTRING5, BITSTRINGMATCH5, HEXSTRINGMATCH5, OCTETSTRINGMATCH5}",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("token recognition error at: ','",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("missing ENDCHAR5 at 'f'",  ++lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 4; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum++, IMarker.SEVERITY_WARNING));
		}
		markersToCheck.add(new MarkerToCheck("extraneous input '}' expecting {<EOF>, MAIN_CONTROLLER_SECTION, INCLUDE_SECTION, ORDERED_INCLUDE_SECTION, EXECUTE_SECTION, DEFINE_SECTION, EXTERNAL_COMMANDS_SECTION, TESTPORT_PARAMETERS_SECTION, GROUPS_SECTION, MODULE_PARAMETERS_SECTION, COMPONENTS_SECTION, LOGGING_SECTION, PROFILER_SECTION, IPV6_5, TTCN3IDENTIFIER5, MACRORVALUE5, STRING5, MACRO_ID5, MACRO_INT5, MACRO_BOOL5, MACRO_FLOAT5, MACRO_EXP_CSTR5, MACRO_BSTR5, MACRO_HSTR5, MACRO_OSTR5, MACRO_BINARY5, MACRO_HOSTNAME5, MACRO5, BITSTRING5, HEXSTRING5, OCTETSTRING5, BITSTRINGMATCH5, HEXSTRINGMATCH5, OCTETSTRINGMATCH5}",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("token recognition error at: ','",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("missing ENDCHAR5 at 'f'",  ++lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 4; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum++, IMarker.SEVERITY_WARNING));
		}
		markersToCheck.add(new MarkerToCheck("extraneous input '}' expecting {<EOF>, MAIN_CONTROLLER_SECTION, INCLUDE_SECTION, ORDERED_INCLUDE_SECTION, EXECUTE_SECTION, DEFINE_SECTION, EXTERNAL_COMMANDS_SECTION, TESTPORT_PARAMETERS_SECTION, GROUPS_SECTION, MODULE_PARAMETERS_SECTION, COMPONENTS_SECTION, LOGGING_SECTION, PROFILER_SECTION, IPV6_5, TTCN3IDENTIFIER5, MACRORVALUE5, STRING5, MACRO_ID5, MACRO_INT5, MACRO_BOOL5, MACRO_FLOAT5, MACRO_EXP_CSTR5, MACRO_BSTR5, MACRO_HSTR5, MACRO_OSTR5, MACRO_BINARY5, MACRO_HOSTNAME5, MACRO5, BITSTRING5, HEXSTRING5, OCTETSTRING5, BITSTRINGMATCH5, HEXSTRINGMATCH5, OCTETSTRINGMATCH5}",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 12;
		markersToCheck.add(new MarkerToCheck("token recognition error at: ','",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("missing ENDCHAR5 at 'a5_1'",  ++lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 11; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum++, IMarker.SEVERITY_WARNING));
		}
		markersToCheck.add(new MarkerToCheck("token recognition error at: ','",  ++lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("missing ENDCHAR5 at 'a5_3'",  ++lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 6; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum++, IMarker.SEVERITY_WARNING));
		}
		markersToCheck.add(new MarkerToCheck("extraneous input '}' expecting {<EOF>, MAIN_CONTROLLER_SECTION, INCLUDE_SECTION, ORDERED_INCLUDE_SECTION, EXECUTE_SECTION, DEFINE_SECTION, EXTERNAL_COMMANDS_SECTION, TESTPORT_PARAMETERS_SECTION, GROUPS_SECTION, MODULE_PARAMETERS_SECTION, COMPONENTS_SECTION, LOGGING_SECTION, PROFILER_SECTION, IPV6_5, TTCN3IDENTIFIER5, MACRORVALUE5, STRING5, MACRO_ID5, MACRO_INT5, MACRO_BOOL5, MACRO_FLOAT5, MACRO_EXP_CSTR5, MACRO_BSTR5, MACRO_HSTR5, MACRO_OSTR5, MACRO_BINARY5, MACRO_HOSTNAME5, MACRO5, BITSTRING5, HEXSTRING5, OCTETSTRING5, BITSTRINGMATCH5, HEXSTRINGMATCH5, OCTETSTRINGMATCH5}",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("token recognition error at: ','",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("missing ENDCHAR5 at 'a5_3'",  ++lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 6; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum++, IMarker.SEVERITY_WARNING));
		}
		markersToCheck.add(new MarkerToCheck("extraneous input '}' expecting {<EOF>, MAIN_CONTROLLER_SECTION, INCLUDE_SECTION, ORDERED_INCLUDE_SECTION, EXECUTE_SECTION, DEFINE_SECTION, EXTERNAL_COMMANDS_SECTION, TESTPORT_PARAMETERS_SECTION, GROUPS_SECTION, MODULE_PARAMETERS_SECTION, COMPONENTS_SECTION, LOGGING_SECTION, PROFILER_SECTION, IPV6_5, TTCN3IDENTIFIER5, MACRORVALUE5, STRING5, MACRO_ID5, MACRO_INT5, MACRO_BOOL5, MACRO_FLOAT5, MACRO_EXP_CSTR5, MACRO_BSTR5, MACRO_HSTR5, MACRO_OSTR5, MACRO_BINARY5, MACRO_HOSTNAME5, MACRO5, BITSTRING5, HEXSTRING5, OCTETSTRING5, BITSTRINGMATCH5, HEXSTRINGMATCH5, OCTETSTRINGMATCH5}",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 3;
		markersToCheck.add(new MarkerToCheck("token recognition error at: ','",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("missing ENDCHAR5 at 'a5_1'",  ++lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 11; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum++, IMarker.SEVERITY_WARNING));
		}
		markersToCheck.add(new MarkerToCheck("extraneous input '}' expecting {<EOF>, MAIN_CONTROLLER_SECTION, INCLUDE_SECTION, ORDERED_INCLUDE_SECTION, EXECUTE_SECTION, DEFINE_SECTION, EXTERNAL_COMMANDS_SECTION, TESTPORT_PARAMETERS_SECTION, GROUPS_SECTION, MODULE_PARAMETERS_SECTION, COMPONENTS_SECTION, LOGGING_SECTION, PROFILER_SECTION, IPV6_5, TTCN3IDENTIFIER5, MACRORVALUE5, STRING5, MACRO_ID5, MACRO_INT5, MACRO_BOOL5, MACRO_FLOAT5, MACRO_EXP_CSTR5, MACRO_BSTR5, MACRO_HSTR5, MACRO_OSTR5, MACRO_BINARY5, MACRO_HOSTNAME5, MACRO5, BITSTRING5, HEXSTRING5, OCTETSTRING5, BITSTRINGMATCH5, HEXSTRINGMATCH5, OCTETSTRINGMATCH5}",  ++lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 8;
		markersToCheck.add(new MarkerToCheck("token recognition error at: ','",  lineNum, IMarker.SEVERITY_WARNING));
		markersToCheck.add(new MarkerToCheck("missing ENDCHAR5 at 'a5_1'",  ++lineNum, IMarker.SEVERITY_WARNING));
		for (i = 0; i < 11; i++) {
			markersToCheck.add(new MarkerToCheck("token recognition error at: ','", lineNum++, IMarker.SEVERITY_WARNING));
		}
		markersToCheck.add(new MarkerToCheck("extraneous input '}' expecting {<EOF>, MAIN_CONTROLLER_SECTION, INCLUDE_SECTION, ORDERED_INCLUDE_SECTION, EXECUTE_SECTION, DEFINE_SECTION, EXTERNAL_COMMANDS_SECTION, TESTPORT_PARAMETERS_SECTION, GROUPS_SECTION, MODULE_PARAMETERS_SECTION, COMPONENTS_SECTION, LOGGING_SECTION, PROFILER_SECTION, IPV6_5, TTCN3IDENTIFIER5, MACRORVALUE5, STRING5, MACRO_ID5, MACRO_INT5, MACRO_BOOL5, MACRO_FLOAT5, MACRO_EXP_CSTR5, MACRO_BSTR5, MACRO_HSTR5, MACRO_OSTR5, MACRO_BINARY5, MACRO_HOSTNAME5, MACRO5, BITSTRING5, HEXSTRING5, OCTETSTRING5, BITSTRINGMATCH5, HEXSTRINGMATCH5, OCTETSTRINGMATCH5}",  ++lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}



}
