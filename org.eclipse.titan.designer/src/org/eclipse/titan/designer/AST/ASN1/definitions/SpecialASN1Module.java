/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.definitions;

import java.util.ArrayList;

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ModuleImportationChain;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignments;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserFactory;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class SpecialASN1Module {
	public static final String INTERNAL_MODULE = "<internal_module>";
	public static final String PARSINGFAILED = "Parsing failed for internal ASN.1 module";
	public static final String PARSINGFAILEDWITHREASON = "Parsing failed for internal ASN.1 module with: `{1}''";

	private static final String NEWLINE = System.getProperty("line.separator");

	private static final String EXTERNAL_ASSIGNMENT = " [UNIVERSAL 8] IMPLICIT SEQUENCE {" + NEWLINE
	+ "  identification CHOICE {" + NEWLINE
	+ "    syntaxes SEQUENCE {" + NEWLINE
	+ "      abstract OBJECT IDENTIFIER, " + NEWLINE
	+ "      transfer OBJECT IDENTIFIER " + NEWLINE
	+ "    }, " + NEWLINE
	+ "    syntax OBJECT IDENTIFIER, " + NEWLINE
	+ "    presentation-context-id INTEGER, " + NEWLINE
	+ "    context-negotiation SEQUENCE {" + NEWLINE
	+ "      presentation-context-id INTEGER, " + NEWLINE
	+ "      transfer-syntax OBJECT IDENTIFIER " + NEWLINE
	+ "    }, " + NEWLINE
	+ "    transfer-syntax OBJECT IDENTIFIER, " + NEWLINE
	+ "    fixed NULL " + NEWLINE
	+ "  }, " + NEWLINE
	+ "  data-value-descriptor ObjectDescriptor OPTIONAL, " + NEWLINE
	+ "  data-value OCTET STRING " + NEWLINE
	+ "} (WITH COMPONENTS {" + NEWLINE
	+ "  ..., " + NEWLINE
	+ "  identification (WITH COMPONENTS {" + NEWLINE
	+ "    ..., " + NEWLINE
	+ "    syntaxes        ABSENT, " + NEWLINE
	+ "    transfer-syntax ABSENT, " + NEWLINE
	+ "    fixed           ABSENT " + NEWLINE
	+ "  }) " + NEWLINE
	+ "})";

	private static final String EMBEDDED_PDV_ASSIGNMENT = "[UNIVERSAL 11] IMPLICIT SEQUENCE {" + NEWLINE
	+ "  identification CHOICE {" + NEWLINE
	+ "    syntaxes SEQUENCE {" + NEWLINE
	+ "      abstract OBJECT IDENTIFIER, " + NEWLINE
	+ "      transfer OBJECT IDENTIFIER " + NEWLINE
	+ "    }, " + NEWLINE
	+ "    syntax OBJECT IDENTIFIER, " + NEWLINE
	+ "    presentation-context-id INTEGER, " + NEWLINE
	+ "    context-negotiation SEQUENCE {" + NEWLINE
	+ "      presentation-context-id INTEGER, " + NEWLINE
	+ "      transfer-syntax OBJECT IDENTIFIER " + NEWLINE
	+ "    }, " + NEWLINE
	+ "    transfer-syntax OBJECT IDENTIFIER, " + NEWLINE
	+ "    fixed NULL " + NEWLINE
	+ "  }, " + NEWLINE
	+ "  data-value-descriptor ObjectDescriptor OPTIONAL, " + NEWLINE
	+ "  data-value OCTET STRING " + NEWLINE
	+ "} (WITH COMPONENTS {" + NEWLINE
	+ "  ..., " + NEWLINE
	+ "  data-value-descriptor ABSENT " + NEWLINE
	+ "})";

	private static final String CHARACTER_STRING_ASSIGNMENT = "[UNIVERSAL 29] IMPLICIT SEQUENCE {" + NEWLINE
	+ "  identification CHOICE {" + NEWLINE
	+ "    syntaxes SEQUENCE {" + NEWLINE
	+ "      abstract OBJECT IDENTIFIER, " + NEWLINE
	+ "      transfer OBJECT IDENTIFIER " + NEWLINE
	+ "    }, " + NEWLINE
	+ "    syntax OBJECT IDENTIFIER, " + NEWLINE
	+ "    presentation-context-id INTEGER, " + NEWLINE
	+ "    context-negotiation SEQUENCE {" + NEWLINE
	+ "      presentation-context-id INTEGER, " + NEWLINE
	+ "      transfer-syntax OBJECT IDENTIFIER " + NEWLINE
	+ "    }, " + NEWLINE
	+ "    transfer-syntax OBJECT IDENTIFIER, " + NEWLINE
	+ "    fixed NULL " + NEWLINE
	+ "  }, " + NEWLINE
	+ "  data-value-descriptor ObjectDescriptor OPTIONAL, " + NEWLINE
	+ "  string-value OCTET STRING " + NEWLINE
	+ "} (WITH COMPONENTS {" + NEWLINE
	+ "  ..., " + NEWLINE
	+ "  data-value-descriptor ABSENT " + NEWLINE
	+ "})";

	private static final String REAL_ASSIGNMENT = "[UNIVERSAL 9] IMPLICIT SEQUENCE {" + NEWLINE
	+ "  mantissa INTEGER, " + NEWLINE
	+ "  base INTEGER (2|10), " + NEWLINE
	+ "  exponent INTEGER " + NEWLINE
	+ "}";

	private static final String TYPE_IDENTIFIER_ASSIGNMENT = "CLASS " + NEWLINE
	+ "{" + NEWLINE
	+ "  &id OBJECT IDENTIFIER UNIQUE, " + NEWLINE
	+ "  &Type " + NEWLINE
	+ "} " + NEWLINE
	+ "WITH SYNTAX {" + NEWLINE
	+ "  &Type IDENTIFIED BY &id " + NEWLINE
	+ "}";

	private static final String ABSTRACT_SYNTAX_ASSIGNMENT = "CLASS {" + NEWLINE
	+ "  &id OBJECT IDENTIFIER UNIQUE, " + NEWLINE
	+ "  &Type, " + NEWLINE
	+ "  &property BIT STRING {handles-invalid-encodings(0)} DEFAULT {} " + NEWLINE
	+ "} " + NEWLINE
	+ "WITH SYNTAX {" + NEWLINE
	+ "  &Type IDENTIFIED BY &id [HAS PROPERTY &property] " + NEWLINE
	+ "}";

	private static final String [][] INTERNAL_ASSIGNMENTS = {{"EXTERNAL", EXTERNAL_ASSIGNMENT},
		{"EMBEDDED PDV", EMBEDDED_PDV_ASSIGNMENT},
		{"CHARACTER STRING", CHARACTER_STRING_ASSIGNMENT},
		{"REAL", REAL_ASSIGNMENT},
		{"TYPE-IDENTIFIER", TYPE_IDENTIFIER_ASSIGNMENT},
		{"ABSTRACT-SYNTAX", ABSTRACT_SYNTAX_ASSIGNMENT}};

	private static ASN1Module specialAssignmentsModule = createSpecAsss();

	protected SpecialASN1Module() {
		// Do nothing
	}

	public static ASN1Module getSpecialModule() {
		return specialAssignmentsModule;
	}

	/**
	 * Creates the special assignments by parsing the strings as if they
	 * were coming from an internal file and creating a module around them.
	 * 
	 * @return the module of the special assignments created.
	 */
	private static ASN1Module createSpecAsss() {
		if (null != specialAssignmentsModule) {
			return specialAssignmentsModule;
		}

		ASN1Assignments parsedAssignments = new ASN1Assignments();
		ASN1Assignment actualAssignment;

		for (String[] assignment : INTERNAL_ASSIGNMENTS) {
			actualAssignment = ParserFactory.parseSpecialInternalAssignment(assignment[1], new Identifier(Identifier_type.ID_ASN, assignment[0]));
			parsedAssignments.addAssignment(actualAssignment);
		}

		// null as a project might not be a good idea
		specialAssignmentsModule = new ASN1Module(new Identifier(Identifier_type.ID_ASN, INTERNAL_MODULE), null, Tag_types.AUTOMATIC_TAGS,
				false);
		specialAssignmentsModule.setExports(new Exports(true));
		specialAssignmentsModule.setImports(new Imports());
		specialAssignmentsModule.setAssignments(parsedAssignments);
		specialAssignmentsModule.setLocation(NULL_Location.INSTANCE);
		specialAssignmentsModule.setScopeName(INTERNAL_MODULE);

		CompilationTimeStamp timestamp = CompilationTimeStamp.getBaseTimestamp();
		ModuleImportationChain referenceChain = new ModuleImportationChain(ModuleImportationChain.CIRCULARREFERENCE, false);
		specialAssignmentsModule.checkImports(timestamp, referenceChain, new ArrayList<Module>());

		specialAssignmentsModule.check(timestamp);

		return specialAssignmentsModule;
	}

	/** Used to preload the class, also loading the ASN.1 parsing framework. */
	public static void preLoad() {
		createSpecAsss();
	}

	/**
	 * Checks whether the provided module is the module that contains the
	 * special assignments.
	 * 
	 * @param module
	 *                the module to check
	 * @return true if the provided module is the module of the special
	 *         assignments, otherwise false.
	 * */
	public static boolean isSpecAsss(final Module module) {
		return null != specialAssignmentsModule && !specialAssignmentsModule.equals(module);
	}
}
