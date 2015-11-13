/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * originally CTs in TITAN.
 * 
 * @author Kristof Szabados
 */
public final class ComponentTypeList extends ASTNode {

	private final List<ComponentType> componentTypes;

	public ComponentTypeList() {
		componentTypes = new ArrayList<ComponentType>();
	}

	public void addComponentType(final ComponentType componentType) {
		if (null != componentType) {
			componentTypes.add(componentType);
			componentType.setFullNameParent(this);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		for (ComponentType componentType : componentTypes) {
			componentType.setMyScope(scope);
		}
	}

	public int getNofComps() {
		int result = 0;
		for (ComponentType componentType : componentTypes) {
			result += componentType.getNofComps();
		}
		return result;
	}

	public CompField getCompByIndex(final int index) {
		int offset = index;
		for (int i = 0; i < componentTypes.size(); i++) {
			int subSize = componentTypes.get(i).getNofComps();
			if (offset < subSize) {
				return componentTypes.get(i).getCompByIndex(offset);
			}

			offset -= subSize;
		}

		// FATAL_ERROR
		return null;
	}

	public boolean hasCompWithName(final Identifier identifier) {
		for (ComponentType componentType : componentTypes) {
			if (componentType.hasCompWithName(identifier)) {
				return true;
			}
		}

		return false;
	}

	public CompField getCompByName(final Identifier identifier) {
		for (ComponentType componentType : componentTypes) {
			if (componentType.hasCompWithName(identifier)) {
				return componentType.getCompByName(identifier);
			}
		}

		// FATAL_ERROR
		return null;
	}

	public void trCompsof(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain, final boolean isSet) {
		for (ComponentType componentType : componentTypes) {
			componentType.trCompsof(timestamp, referenceChain, isSet);
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentTypes == null) {
			return;
		}

		for (ComponentType ct : componentTypes) {
			ct.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (componentTypes != null) {
			for (ComponentType ct : componentTypes) {
				if (!ct.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
