/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.finddefinition;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ExternalConst;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.preferences.SubscribedBoolean;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Szabolcs Beres
 * */
public final class StoredDefinitionFilter {

	private static StoredDefinitionFilter instance = null;

	private IProject currentProject;

	private StoredDefinitionFilter(final IProject currentProject) {
		functions = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FIND_DEF_FUNCT, true);
		types = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FIND_DEF_TYPES, true);
		modules = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FIND_DEF_MODULES, true);
		globalVariables = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FIND_DEF_GLOBAL, true);
		workspaceScope = new SubscribedBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FIND_DEF_WS, true);
		this.currentProject = currentProject; 
	}

	private SubscribedBoolean functions;
	private SubscribedBoolean types;
	private SubscribedBoolean modules;
	private SubscribedBoolean globalVariables;
	private SubscribedBoolean workspaceScope;

	public static StoredDefinitionFilter getInstance(final IProject currentProject) {
		if (instance == null) {
			instance = new StoredDefinitionFilter(currentProject);
		} else {
			instance.currentProject = currentProject;
		}
		return instance;
	}
	
	public boolean isWorkspaceScope() {
		return workspaceScope.getValue();
	}
	
	public IProject getCurrentProject() {
		return currentProject;
	}

	public List<Object> filter(final List<Object> obj) {
		List<Object> result = new ArrayList<Object>();
		for (Object o : obj) {
			if (filter(o)) {
				result.add(o);
			}
		}
		return result;
	}

	public boolean filter(final Object obj) {
		if (!functions.getValue() && isFunction(obj)) {
			return false;
		}
		
		if (!types.getValue() && isType(obj)) {
			return false;
		}
		
		if (!modules.getValue() && obj instanceof Module) {
			return false;
		}
		
		if (!globalVariables.getValue() && isGlobalVar(obj)) {
			return false;
		}
		return true;
	}
	
	private boolean isFunction(final Object obj) {
		return obj instanceof Def_Function
				|| obj instanceof Def_Testcase
				|| obj instanceof Def_Altstep
				|| obj instanceof Def_Extfunction;
	}

	private boolean isGlobalVar(final Object obj) {
		return obj instanceof Def_Var || obj instanceof Def_Const
				|| obj instanceof Def_Timer || obj instanceof Def_Var_Template
				|| obj instanceof Def_ExternalConst || obj instanceof Def_ModulePar;
	}

	private boolean isType(final Object obj) {
		return obj instanceof Def_Type || obj  instanceof Def_Template
				|| obj instanceof ASN1Assignment;
	}

	public boolean getFunctions() {
		return functions.getValue();
	}

	public void setFunctions(final boolean functions) {
		this.functions.setValue(functions);
	}

	public boolean getTypes() {
		return types.getValue();
	}

	public void setTypes(final boolean types) {
		this.types.setValue(types);
	}

	public boolean getModules() {
		return modules.getValue();
	}

	public void setModules(final boolean modules) {
		this.modules.setValue(modules);
	}

	public boolean getGlobalVariables() {
		return globalVariables.getValue();
	}

	public void setGlobalVariables(final boolean globalVariables) {
		this.globalVariables.setValue(globalVariables);
	}

	public boolean getWorkspaceScope() {
		return workspaceScope.getValue();
	}

	public void setWorkspaceScope(final boolean workspaceScope) {
		this.workspaceScope.setValue(workspaceScope);
	}
	
	public boolean showOnlyModules() {
		return modules.getValue()
				&& !functions.getValue()
				&& !globalVariables.getValue()
				&& !types.getValue();
	}
}
