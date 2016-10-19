###############################################################################
# Copyright (c) 2000-2016 Ericsson Telecom AB
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################
Installing the requirements:
- install antlr plugin "antlr4ide" as described here: https://github.com/jknack/antlr4ide
- download an antlr compiler of version 4.3: http://www.antlr.org/download/antlr-4.3-complete.jar
- to be able to try it out you need to install antlr runtime from orbit: http://download.eclipse.org/tools/orbit/downloads/drops/R20151221205849/repository/

Setting up antlr4IDE
===================
Some of the default settings of antlr4ide need to be changed.

1)
  Go to Window / Preferences / ANTLR4 / Tool

2)
  In the antlr tool region of the preference page ass and enable the antlr compiler (version 4.3)

3)
  In the Options section:
  - set Directory to: "./src/" (so that the files are generated next to the grammar files)
  - turn off the generation of parse tree listener and parse tree visitors

BASIC compilation of the plug-in
===================

After antlr4ide is set up properly the automatic build should be able to compile the project without user intervention.

In some cases the first compilation happens in a wrong order.
To correct it just clear the whole project and let the automatic build try again from zero.
