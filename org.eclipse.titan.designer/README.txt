###############################################################################
# Copyright (c) 2000-2016 Ericsson Telecom AB
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################
Location of the required ANTLR plug-in :  http://antlreclipse.sourceforge.net/updates/
Location of the required ProGuard plug-in :  http://obfuscate4e.partmaster.de/updates/

BASIC compilation of the plug-in
===================

1)
  In Eclipse turn off automatic build, refresh the contents of this project.
  And Clean... the project.

2)
  Make sure, that the org.eclipse.titan.common is compiled.
  /* or at least is right before enabling the automatic build option */

3)
  Go to the META-INF folder and
  in the MANIFEST.MF file set the correct version of the common plug-in.

4)
  Go to the src/org.eclipse.titan.designer.parsers.ASN1parser package
  and compile with ANTLR these files in this order:
    - ASN1Lexer.g
    - ASN1Parser.g
 
  /* From here on it is not required to compile these files by hand as the automatic build will do this for us */
    - ASN1SpecialParser.g
5)
  Go to the src/org.eclipse.titan.designer.parsers.TTCN3parser package
  and  compile with ANTLR these files in this order:
    - TTCN3Lexer.g
    - TTCN3Parser.g
      
  /* From here on it is not required to compile these files by hand as the automatic build will do this for us */
    - TTCN3KeywordLessLexer.g
    - TTCN3Reparser.g

6)
  Go to the src/org.eclipse.titan.designer.parsers.Logparser package
  and compile with ANTLR these files in this order:
  
  /* It is actually not required to compile this file by hand as the automatic build will do this for us */
    - LogLexer.g

7)
  Go to the src/org.eclipse.titan.designer.parsers.ExtensionAttributeParser package
  and compile with ANTLR these files in this order:
    - ExtensionAttributeLexer.g
    - ExtensionAttributeParser.g
    
8)
  Now you can enable automatic build, which will complete the build process.
  
  OBFUSCATION
=========
After step 7 in the original build process.

1)
Right click on MANIFEST.MF and select PDE Tools / Create ANT build file and PDE Tools / create CustomBuildCallbacks for Proguard

2)
Right click on the generated build.xml and select Run As / Ant Build

3)
After creating the package don't forget to remove the parser related .g, .smap, .txt files.


 R&S BUILD
=========
When building the R&S license checking enabled version.
1)
Add the R&S license checker plugin as dependency to the Designer plugin.

2)
Uncomment all parts of the org.eclipse.titan.designer.license.IndexerThread class.
