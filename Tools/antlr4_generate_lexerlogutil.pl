#!/usr/bin/perl
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
# Generates ...LexerLogUtil.java implements ILexerLogUtil
# for all ...Lexer.java under the current directory
# to determine token name from token index.
# This is used for logging purpose,
# see ParserLogger.java and TokenNameResolver.java
#
# Prerequisites: ANTLR lexer .g4 files must be compiled,
#   because generated java files must exist.
#
# Example usage (to create ...LexerLogUtil.java for all the ...Lexer.java
# in titan.EclipsePlug-ins project):
#   cd <titan.EclipsePlug-ins project root>
#   Tools/antlr4_generate_lexerlogutil.pl
###############################################################################

# to read the whole file at once, not just line-by-line
# http://www.kichwa.com/quik_ref/spec_variables.html
undef $/;

$fileindex = 0;

#total number of changed files
$total = 0;

sub load
    {
    local $filename = shift; # 1st parameter
    local $f = "F".$fileindex++;
    if(opendir($f, $filename))
        {
        # $filename is a directory
        while(local $newfilename = readdir($f))
            {
            if(!($newfilename =~ /^\.(\.|git)?$/))  # filter . .. .git
                {
                local $longfilename = $filename."/".$newfilename; # for Unix
                #local $longfilename = $filename."\\".$newfilename; # for DOS/Windows

                load($longfilename);
                }
            }
        closedir($f);
        }
    else
        {
        # $filename is a file

        if($filename =~ /^(.*\/)(Asn1|Cfg|Ttcn3|ExtensionAttribute|PreprocessorDirective)(Lexer)(\.java)$/)
            {
            $file_part1 = $1;
            $file_part2 = $2;
            $file_part3 = $3;
            $file_part4 = "LogUtil";
            $file_part5 = $4;
            print("Source file: $filename\n");
            open(IN, $filename);
            $whole_file = <IN>;
            close(IN);

            $outfilename = $file_part1.$file_part2.$file_part3.$file_part4.$file_part5;
            print("Output file: $outfilename\n");
            open(OUT, ">$outfilename");

            if ( $whole_file =~ /package\s+([a-zA-Z_][a-zA-Z_0-9]*(\.[a-zA-Z_][a-zA-Z_0-9]*)*);/gs ) {
                $package = $1;
                print "Package: $package\n";
            } else {
                print "ERROR: package missing";
            }

            $out =
            "package $package;\n".
            "\n".
            "import org.eclipse.titan.common.parsers.ILexerLogUtil;\n".
            "\n".
            "public class $file_part2$file_part3$file_part4 implements ILexerLogUtil {\n".
            "\n".
            "    public String getTokenName( int aIndex ) {\n".
            "        switch ( aIndex ) {\n";

            if ( $whole_file =~ /public static final int\n\s*(.*?);/gs ) {
                 $const_defs = $1; #comma separated constant definition: WS=1, LINE_COMMENT=2, ..., MACRO12=451
#                 print "\$const_defs == $const_defs\n";
                 my @list = split(/,\s*/, $const_defs);
                 foreach my $const_def (@list) {
#                     print "\$const_def == \"$const_def\"\n";
                     if ( $const_def =~ /(^[A-Za-z][A-Za-z0-9_]*)=([0-9]+)$/ ) {
                         $const_name = $1;
                         $const_value = $2;
#                         print "\$const_name == \"$const_name\"\n";
#                         print "\$const_value == \"$const_value\"\n";
                         $out .=
                         "        case $const_value:\n".
                         "            return \"$const_name\";\n";
                     }
                     else {
                         print "ERROR: $const_def does NOT match!\n";
                     }
                 }
            }

            $out .=
            "        case -1:\n".
            "            return \"EOF\";\n".
            "        default:\n".
            "            return \"\";\n".
            "        }\n".
            "    }\n".
            "}\n".
            "";

            print OUT $out;
            close(OUT);
            print("DONE\n");
            }
        }
    }

load("..");

