#!/bin/bash
###############################################################################
# Copyright (c) 2000-2016 Ericsson Telecom AB
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Lovassy, Arpad
#
###############################################################################

###############################################################################
# Script to compile all the .g4 files in the Titan EclipsePlug-ins repo.
#
# Example usage:
#   cd <titan.EclipsePlug-ins project root>
#   Tools/antlr4_compile.sh
###############################################################################
set -e
set -o pipefail

ANTLR4="java -classpath $HOME/lib/antlr-4.3-complete.jar org.antlr.v4.Tool"

# script directory
# http://stackoverflow.com/questions/59895/can-a-bash-script-tell-which-directory-it-is-stored-in
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

WORKSPACE_PATH=$DIR/..

cd $WORKSPACE_PATH/org.eclipse.titan.common/src/org/eclipse/titan/common/parsers/cfg/
$ANTLR4 CfgLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.common.parsers.cfg
$ANTLR4 CfgParser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.common.parsers.cfg

# Titan Designer
# ASN1
cd $WORKSPACE_PATH/org.eclipse.titan.designer/src/org/eclipse/titan/designer/parsers/asn1parser/
$ANTLR4 Asn1Lexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.asn1parser
$ANTLR4 Asn1Parser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.asn1parser

# TTCN3
cd $WORKSPACE_PATH/org.eclipse.titan.designer/src/org/eclipse/titan/designer/parsers/ttcn3parser/
$ANTLR4 PreprocessorDirectiveLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
$ANTLR4 PreprocessorDirectiveParser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
$ANTLR4 Ttcn3BaseLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
$ANTLR4 Ttcn3Lexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
$ANTLR4 Ttcn3KeywordlessLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
$ANTLR4 Ttcn3CharstringLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
$ANTLR4 Ttcn3Parser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
$ANTLR4 Ttcn3Reparser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser

# Extension attribute
cd $WORKSPACE_PATH/org.eclipse.titan.designer/src/org/eclipse/titan/designer/parsers/extensionattributeparser/
$ANTLR4 ExtensionAttributeLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.extensionattributeparser
$ANTLR4 ExtensionAttributeParser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.extensionattributeparser

# Generating ...LexerLogUtil.java files from ...Lexer.java files for resolving token names (OPTIONAL)
cd $WORKSPACE_PATH
$DIR/antlr4_generate_lexerlogutil.pl

