###############################################################################
# Copyright (c) 2000-2016 Ericsson Telecom AB
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################
OUTDATED description!!!
Location of the required ANTLR plug-in :  http://antlreclipse.sourceforge.net/updates/
Location of the required ProGuard plug-in :  http://obfuscate4e.partmaster.de/updates/

BASIC compilation of the plug-in
===================

1)
  In Eclipse turn off automatic build, refresh the contents of this project.
  And Clean... the project.
  
2)
  Go to the src/org.eclipse.titan.common.parsers.cfg package
  
3)
  compile with ANTLR these files in this order:
  - cfgBaseLexer.g
  - cfgParser.g
  
    /* From here on it is not required to compile these files by hand as the automatic build will this for us */
  - cfgResolver.g
  - cfgComponentsSectionLexer.g
  - cfgDefineSectionLexer.g
  - cfgExecuteSectionLexer.g
  - cfgExternalCommandsSectionLexer.g
  - cfgGroupsSectionLexer.g
  - cfgIncludeSectionLexer.g
  - cfgLoggingSectionLexer.g
  - cfgMainControllerSection.g
  - cfgModuleParametersSectionLexer.g
  - cfgTestportParametersSectionLexer.g
  
4)
  Now you can enable automatic build, which will complete the build process.
  
OBFUSCATION
=========
After step 4 in the original build process.

5)
Right click on MANIFEST.MF and select PDE Tools / Create ANT build file and PDE Tools / create CustomBuildCallbacks for Proguard

6)
Right click on the generated build.xml and select Run As / Ant Build

7)
After creating the package don't forget to remove the parser related .g, .smap, .txt files.