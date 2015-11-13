/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class ComponentTypeReferenceList extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	public static final String FULLNAMEPART = ".<ref";
	public static final String DUPLICATECOMPONENTREFERENCEFIRST = "Duplicate reference to component `{0}'' was first declared here";
	public static final String DUPLICATECOMPONENTREFERENCEREPEATED = "Duplicate reference to component `{0}'' was declared here again";

	private final List<Reference> componentReferences;
	private Map<ComponentTypeBody, Reference> componentTypeBodies;
	private List<ComponentTypeBody> orderedComponentTypeBodies;

	private Location location;

	/** Holds the last time when these references were checked, or null if never. */
	private CompilationTimeStamp lastCompilationTimeStamp;

	public ComponentTypeReferenceList() {
		componentReferences = new CopyOnWriteArrayList<Reference>();
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		for (int i = 0; i < componentReferences.size(); i++) {
			if (componentReferences.get(i) == child) {
				return builder.append(FULLNAMEPART).append(String.valueOf(i + 1)).append(INamedNode.MORETHAN);
			}
		}

		return builder;
	}

	public void addReference(final Reference reference) {
		componentReferences.add(reference);
		reference.setFullNameParent(this);
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	public List<ComponentTypeBody> getComponentBodies() {
		return orderedComponentTypeBodies;
	}

	@Override
	public void setMyScope(final Scope scope) {
		for (Reference reference : componentReferences) {
			reference.setMyScope(scope);
		}
	}

	/**
	 * Checks if the references of expanded components contain any recursive
	 * references, which is not allowed.
	 *
	 * @param refChain the reference chain used to stroe the visited components.
	 * */
	protected void checkRecursion(final IReferenceChain refChain) {
		if (orderedComponentTypeBodies == null) {
			return;
		}

		for (ComponentTypeBody body : orderedComponentTypeBodies) {
			refChain.markState();
			body.checkRecursion(refChain);
			refChain.previousState();
		}
	}

	/**
	 * Checks the uniqueness of the extensions, and also builds a hashmap of
	 * them to speed up further searches.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle
	 * */
	private void checkUniqueness(final CompilationTimeStamp timestamp) {
		if (orderedComponentTypeBodies == null) {
			componentTypeBodies = new HashMap<ComponentTypeBody, Reference>(componentReferences.size());
			orderedComponentTypeBodies = new ArrayList<ComponentTypeBody>(componentReferences.size());
		}

		componentTypeBodies.clear();
		orderedComponentTypeBodies.clear();

		for (Reference reference : componentReferences) {
			Component_Type componentType = reference.chkComponentypeReference(timestamp);
			if (componentType != null) {
				ComponentTypeBody compTypeBody = (componentType).getComponentBody();
				if (compTypeBody != null) {
					if (componentTypeBodies.containsKey(compTypeBody)) {
						componentTypeBodies.get(compTypeBody).getId().getLocation().reportSingularSemanticError(
								MessageFormat.format(ComponentTypeReferenceList.DUPLICATECOMPONENTREFERENCEFIRST, compTypeBody.getIdentifier()
										.getDisplayName()));
						reference.getLocation().reportSemanticError(
								MessageFormat.format(ComponentTypeReferenceList.DUPLICATECOMPONENTREFERENCEREPEATED, reference.getDisplayName()));
					} else {
						componentTypeBodies.put(compTypeBody, reference);
						orderedComponentTypeBodies.add(compTypeBody);
					}
				}
			}
		}
	}

	/**
	 * Does the semantic checking of the contained definitions.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastCompilationTimeStamp != null && !lastCompilationTimeStamp.isLess(timestamp)) {
			return;
		}

		checkUniqueness(timestamp);

		lastCompilationTimeStamp = timestamp;

		for (ComponentTypeBody body : orderedComponentTypeBodies) {
			body.check(timestamp);
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for (int i = 0, size = componentReferences.size(); i < size; i++) {
			Reference reference = componentReferences.get(i);

			reference.updateSyntax(reparser, false);
			reparser.updateLocation(reference.getLocation());
		}
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentReferences != null) {
			for (Reference ref : componentReferences) {
				ref.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (componentReferences != null) {
			for (Reference ref : componentReferences) {
				if (!ref.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
