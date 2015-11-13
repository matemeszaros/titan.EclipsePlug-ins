/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ExternalConst;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class TypenameInDef extends BaseModuleCodeSmellSpotter {
	private static final String REPORT = "The name `{1}'' of the {0} contains it''s type''s name `{2}''";
	protected final CompilationTimeStamp timestamp;
	
	public TypenameInDef() {
		super(CodeSmellType.TYPENAME_IN_DEFINITION);
		timestamp = CompilationTimeStamp.getBaseTimestamp();
	}
	
	@Override
	protected void process(IVisitableNode node, Problems problems) {
		if (!(node instanceof Def_Const) &&
				!(node instanceof Def_ExternalConst) &&
				!(node instanceof Def_Extfunction) &&
				!(node instanceof Def_Function) &&
				!(node instanceof Def_ModulePar) &&
				!(node instanceof Def_Template) &&
				!(node instanceof Def_Var_Template) &&
				!(node instanceof Def_Var)) {
			return;
		}
		Definition s = (Definition)node;
		check(s.getIdentifier(), s.getType(timestamp), s.getDescription(), problems);
	}
	
	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(8);
		ret.add(Def_Const.class);
		ret.add(Def_ExternalConst.class);
		ret.add(Def_Extfunction.class);
		ret.add(Def_Function.class);
		ret.add(Def_ModulePar.class);
		ret.add(Def_Template.class);
		ret.add(Def_Var_Template.class);
		ret.add(Def_Var.class);
		return ret;
	}

	private void check(final Identifier identifier, final IType type, final String description, Problems problems) {
		if (type == null) {
			return;
		}
		final String displayName = identifier.getDisplayName();
		Identifier typeId = null;
		if (type instanceof Referenced_Type) {
			Referenced_Type referencedType = (Referenced_Type) type;
			typeId = referencedType.getReference().getId();
		}

		String typeName;
		if (typeId == null) {
			typeName = type.getTypename();
		} else {
			typeName = typeId.getDisplayName();
		}
		if (displayName.contains(typeName)) {
			String msg = MessageFormat.format(REPORT, description, displayName, typeName);
			problems.report(identifier.getLocation(), msg);
		}
	}

}
