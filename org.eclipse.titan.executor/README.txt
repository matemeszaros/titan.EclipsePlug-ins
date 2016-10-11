###############################################################################
# Copyright (c) 2000-2016 Ericsson Telecom AB
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################
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
  Now you can enable automatic build, which will complete the build process.
  
  OBFUSCATION
=========
After step 4 in the original build process.

5)
Right click on MANIFEST.MF and select PDE Tools / Create ANT build file and PDE Tools / create CustomBuildCallbacks for Proguard

6)
Right click on the generated build.xml and select Run As / Ant Build