parser grammar Ttcn3Reparser;
import Ttcn3Parser;

/*
 ******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************
*/

options {
	tokenVocab=Ttcn3Lexer;
}

@header {
}

@members {

// additional members and functions to the imported grammar (Ttcn3Parser.g4)

public void setModule(TTCN3Module module) {
	act_ttcn3_module = module;
}

}

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */

pr_reparse_TTCN3ModuleId returns[Identifier identifier = null]:
(	i = pr_OwnGlobalModuleId { $identifier = $i.identifier; }
	pr_LanguageSpec?
	EOF
);

pr_reparse_Identifier returns[Identifier identifier = null]:
(	i = pr_Identifier { $identifier = $i.identifier; }
	EOF
);

pr_reparse_ModuleDefinitionsList
	[
	Group parent_group,
	List<Definition> all_definitions,
	List<Definition> local_definitions,
	List<Group> local_groups,
	List<ImportModule> all_imports,
	List<ImportModule> local_imports,
	List<FriendModule> all_friends,
	List<FriendModule> local_friends,
	List<ControlPart> control_parts
	]
:
(	(	pr_reparse_ModuleDefinition[ $parent_group, $all_definitions, $local_definitions, $local_groups, $all_imports, $local_imports, $all_friends, $local_friends ]
		pr_SemiColon?
	)*
	(	{ $control_parts != null }?
		c = pr_ModuleControlPart { $control_parts.add($c.controlpart); }
	)?
	EOF
);

pr_reparse_ModuleDefinitionsList2
	[
	Group parent_group,
	List<Definition> all_definitions,
	List<Definition> local_definitions,
	List<Group> local_groups,
	List<ImportModule> all_imports,
	List<ImportModule> local_imports,
	List<FriendModule> all_friends,
	List<FriendModule> local_friends
	]
:
(	(	pr_reparse_ModuleDefinition[ $parent_group, $all_definitions, $local_definitions, $local_groups, $all_imports, $local_imports, $all_friends, $local_friends ]
		pr_SemiColon?
	)*
);

pr_reparse_ModuleDefinition
	[
	Group parent_group,
	List<Definition> all_definitions,
	List<Definition> local_definitions,
	List<Group> local_groups,
	List<ImportModule> all_imports,
	List<ImportModule> local_imports,
	List<FriendModule> all_friends,
	List<FriendModule> local_friends
	]
:
(	d = pr_ModuleDef
	{	List<Definition> defs = $d.definitions;
		if ( $parent_group == null ) {
			for ( Definition def : defs ) {
				def.setAttributeParentPath( act_ttcn3_module.getAttributePath() );
			}
		} else {
			$parent_group.addDefinitions( defs );
			for ( Definition def : defs ) {
				def.setAttributeParentPath( $parent_group.getAttributePath() );
			}
		}
		$all_definitions.addAll( defs );
		$local_definitions.addAll( defs );
	}
|	pr_reparse_ImportDef[ $parent_group, $all_imports, $local_imports ]
|	pr_reparse_GroupDef[ $parent_group, $all_definitions, $local_definitions, $local_groups, $all_imports, $local_imports, $all_friends, $local_friends ]
|	pr_reparse_FriendModuleDef[ $parent_group, $all_friends, $local_friends ]
);

pr_reparse_ImportDef [Group parent_group, List<ImportModule> all_imports, List<ImportModule> local_imports]
@init {
	boolean selective = false;
	ImportModule impmod = null;
	MultipleWithAttributes attributes = null;
}:
(	PRIVATE?
	IMPORT
	i = pr_ImportFromSpec { impmod = $i.impmod; }
	(	pr_AllWithExcepts { if ( impmod != null ) { impmod.setHasNormalImports(); } }
	|	pr_BeginChar
		pr_ImportSpec[impmod]
		pr_EndChar { selective = true; } 
	)
	(	a = pr_WithStatement { attributes = $a.attributes; }	)?
)
{	if ( impmod != null ) {
		impmod.setWithAttributes( attributes );
		impmod.setLocation( getLocation( $start, getStopToken() ) );
		if ( $parent_group == null ) {
			impmod.setAttributeParentPath( act_ttcn3_module.getAttributePath() );
		} else {
			$parent_group.addImportedModule( impmod );
			impmod.setAttributeParentPath( $parent_group.getAttributePath() );
	    }
	    $all_imports.add( impmod );
	    $local_imports.add( impmod );
	}
};

pr_reparse_GroupDef
	[
	Group parent_group,
	List<Definition> all_definitions,
	List<Definition> local_definitions,
	List<Group> local_groups,
	List<ImportModule> all_imports,
	List<ImportModule> local_imports,
	List<FriendModule> all_friends,
	List<FriendModule> local_friends
	]:
{	Group group = null;
	List<Definition> local_definitions2 = new ArrayList<Definition>();
	List<Group> local_groups2 = new ArrayList<Group>();
	List<ImportModule> local_imports2 = new ArrayList<ImportModule>();
	MultipleWithAttributes attributes = null;
}
(	PUBLIC?
	pr_GroupKeyword
	i = pr_Identifier	{ group = new Group( $i.identifier ); }
	begin = pr_BeginChar
	pr_reparse_ModuleDefinitionsList2[ group, $all_definitions, local_definitions2, local_groups2, $all_imports, local_imports2, $all_friends, $local_friends ]
	end1 = pr_EndChar
	(	a = pr_WithStatement { attributes = $a.attributes; }	)?
)
{	if ( group != null ) {
		group.setWithAttributes( attributes );
		group.setParentGroup( $parent_group );
		group.setLocation( getLocation( $start, getStopToken() ) );
		group.setInnerLocation( getLocation( $begin.start, $end1.stop ) );
		if ( $parent_group != null ) {
			$parent_group.addGroup( group );
			group.setAttributeParentPath( $parent_group.getAttributePath() );
		} else {
			group.setAttributeParentPath( act_ttcn3_module.getAttributePath() );
		}
		$local_groups.add(group);
	}
};

pr_reparse_FriendModuleDef[ Group parent_group, List<FriendModule> all_friends, List<FriendModule> local_friends ]:
{	List<Identifier> identifiers = new ArrayList<Identifier>();
	MultipleWithAttributes attributes = null;
}
(	PRIVATE?
	FRIEND
	MODULE
	i = pr_TTCN3ModuleId  { if ( $i.identifier != null ) { identifiers.add( $i.identifier ); } }
	(	pr_Comma
		i = pr_TTCN3ModuleId  { if ( $i.identifier != null ) { identifiers.add( $i.identifier ); } }
	)*
	(	a = pr_WithStatement { attributes = $a.attributes; }	)?
)
{	for ( Identifier identifier : identifiers ) {
		FriendModule friend = new FriendModule( identifier );
		friend.setWithAttributes(attributes);
		friend.setLocation( getLocation( $start, getStopToken() ) );
		if ( $parent_group == null ) {
			friend.setAttributeParentPath( act_ttcn3_module.getAttributePath() );
		} else {
			$parent_group.addFriendModule( friend );
			friend.setAttributeParentPath( $parent_group.getAttributePath() );
		}
		$all_friends.add( friend );
		$local_friends.add( friend );
	}
};

pr_reparser_optionalWithStatement returns[MultipleWithAttributes attributes = null]:
(	a = pr_WithStatement { $attributes = $a.attributes; }	)?
;

pr_reparse_FunctionStatementOrDefList returns[List<Statement> statements = null]:
(	(	s = pr_FunctionStatementOrDefList { $statements = $s.statements; }
	|	{ $statements = new ArrayList<Statement>(); }
	)
	EOF
);

pr_reparse_StructFieldDefs returns[List<CompField> fields]:
{	$fields = new ArrayList<CompField>();
}
(	(	c = pr_StructFieldDef { $fields.add( $c.compField ); }
		(	pr_Comma
			(	c = pr_StructFieldDef { $fields.add( $c.compField ); }
			)?
		)*
	)?
	EOF
);

pr_reparse_AltGuardList returns [AltGuards altGuards]:
{	$altGuards = new AltGuards();
}
(	(	a = pr_GuardStatement { $altGuards.addAltGuard( $a.altGuard ); }
	|	b = pr_ElseStatement { $altGuards.addAltGuard( $b.altGuard ); }
	)+
	EOF
);
