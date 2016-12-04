/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.modulepar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;

/**
 * This class is only instantiated by {@link ExtractModuleParRefactoring} once per
 * each refactoring operation. By calling {@link #perform()}, all module parameters
 * are collected from the source project.
 * 
 * @author Viktor Varga
 */
public class SelectionFinder {

	//in
	private final IProject project;
	//out
	private Set<Def_ModulePar> modulePars;
	
	public SelectionFinder(final IProject project) {
		this.project = project;
	}
	
	public Set<Def_ModulePar> getModulePars() {
		return modulePars;
	}
	
	public void perform() {
		modulePars = new HashSet<Def_ModulePar>();
		Collection<Module> modules = GlobalParser.getProjectSourceParser(project).getModules();
		for (Module m: modules) {
			ModuleParFinder vis = new ModuleParFinder();
			m.accept(vis);
			modulePars.addAll(vis.getModulePars());
		}
	}
	
	public String createModuleParListForSaving() {
		if (modulePars == null || modulePars.isEmpty()) {
			return "<empty>";
		}
		List<ModuleParListRecord> records = new ArrayList<ModuleParListRecord>();
		for (Def_ModulePar def: modulePars) {
			IResource f = def.getLocation().getFile();
			if (!(f instanceof IFile)) {
				ErrorReporter.logError("ExtractModulePar/SelectionFinder: IResource `" + f.getName() + "' is not an IFile.");
				continue;
			}
			Identifier id = def.getIdentifier();
			Type t = def.getType(CompilationTimeStamp.getBaseTimestamp());
			records.add(new ModuleParListRecord(def.getMyScope().getModuleScope().getIdentifier().getDisplayName(), id.getDisplayName(), t.getTypename()));
		}
		//
		Collections.sort(records);
		StringBuilder sb = new StringBuilder();
		for (ModuleParListRecord rec: records) {
			sb.append(rec.toString()).append('\n');
		}
		return sb.toString();
	}
	
	private static class ModuleParListRecord implements Comparable<ModuleParListRecord> {
		
		public ModuleParListRecord(final String moduleName, final String id, final String typeId) {
			this.moduleName = moduleName;
			this.id = id;
			this.typeId = typeId;
		}
		
		private final String moduleName;
		private final String id;
		private final String typeId;
		
		@Override
		public int compareTo(final ModuleParListRecord arg0) {
			int cmp = moduleName.compareTo(arg0.moduleName);
			if (cmp != 0) {
				return cmp;
			}
			return id.compareTo(arg0.id);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result	+ ((moduleName == null) ? 0 : moduleName.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ModuleParListRecord)) {
				return false;
			}
			ModuleParListRecord other = (ModuleParListRecord)obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
			if (moduleName == null) {
				if (other.moduleName != null) {
					return false;
				}
			} else if (!moduleName.equals(other.moduleName)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return moduleName + "." + id + ": " + typeId;
		}
	}

	/**
	 * Collects all module parameter descendant nodes.
	 * <p>
	 * Should be called on {@link Module} objects.
	 *  */
	private class ModuleParFinder extends ASTVisitor {

		private final Set<Def_ModulePar> modulePars = new HashSet<Def_ModulePar>();
		
		public Set<Def_ModulePar> getModulePars() {
			return modulePars;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Def_ModulePar) {
				modulePars.add((Def_ModulePar)node);
				return V_SKIP;
			}
			return V_CONTINUE;
		}
		
	}
	
	
}
