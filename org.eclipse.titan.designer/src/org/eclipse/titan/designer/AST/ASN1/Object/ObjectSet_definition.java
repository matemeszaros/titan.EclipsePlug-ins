/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceChainElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.IObjectSet_Element;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * ObjectSet definition.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class ObjectSet_definition extends ObjectSet implements IReferenceChainElement {

	private ArrayList<IObjectSet_Element> objectSetElements;
	protected ASN1Objects objects;

	public ObjectSet_definition() {
		setObjectSetElements(new ArrayList<IObjectSet_Element>());
	}

	public ObjectSet_definition(final ASN1Objects objects) {
		this();
		this.objects = objects;
	}

	public final ArrayList<IObjectSet_Element> getObjectSetElements() {
		return objectSetElements;
	}

	public final void setObjectSetElements(ArrayList<IObjectSet_Element> objectSetElements) {
		this.objectSetElements = objectSetElements;
	}
	
	@Override
	public final String chainedDescription() {
		return getFullName();
	}

	@Override
	public final Location getChainLocation() {
		return getLocation();
	}

	public final void steelObjectSetElements(final ObjectSet_definition definition) {
		if (null == definition) {
			return;
		}

		for (int i = 0; i < definition.getObjectSetElements().size(); i++) {
			addObjectSetElement(definition.getObjectSetElements().get(i).newOseInstance());
		}

		definition.getObjectSetElements().clear();
		getObjectSetElements().trimToSize();
	}

	@Override
	public final StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0; i < getObjectSetElements().size(); i++) {
			if (getObjectSetElements().get(i) == child) {
				return builder.append(INamedNode.DOT).append(String.valueOf(i + 1));
			}
		}

		return builder;
	}

	@Override
	public final void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		for (IObjectSet_Element element : getObjectSetElements()) {
			element.setMyScopeOse(scope);
		}
	}

	public final void addObjectSetElement(final IObjectSet_Element element) {
		if (null != element) {
			getObjectSetElements().add(element);
		}
	}

	@Override
	public final ObjectSet_definition getRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (1 != getObjectSetElements().size()) {
			return this;
		}

		final IObjectSet_Element element = getObjectSetElements().get(0);
		if (!(element instanceof Referenced_ObjectSet)) {
			return this;
		}

		final boolean newChain = null == referenceChain;
		IReferenceChain temporalReferenceChain;
		if (null == referenceChain) {
			temporalReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		} else {
			temporalReferenceChain = referenceChain;
		}

		temporalReferenceChain.add(this);
		final ObjectSet_definition result = ((Referenced_ObjectSet) element).getRefdLast(timestamp, temporalReferenceChain);

		if (newChain) {
			temporalReferenceChain.release();
		}

		return result;
	}

	@Override
	public final int getNofObjects() {
		createObjects(false);

		return objects.getNofObjects();
	}

	@Override
	public final ASN1Object getObjectByIndex(final int index) {
		createObjects(false);

		return objects.getObjectByIndex(index);
	}

	public final ASN1Objects getObjs() {
		createObjects(false);

		return objects;
	}

	@Override
	public final void accept(final ObjectSetElementVisitor_objectCollector v) {
		v.visitObjectSet(this, false);
	}

	abstract protected void parseBlockObjectSetSpecifications();
	
	protected final void createObjects(final boolean force) {
		if (null != objects && !force) {
			return;
		}

		if (null == myGovernor) {
			return;
		}

		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		final ObjectSetElementVisitor_objectCollector visitor = new ObjectSetElementVisitor_objectCollector(this, lastTimeChecked);
		getObjectSetElements().trimToSize();
		for (int i = 0; i < getObjectSetElements().size(); i++) {
			getObjectSetElements().get(i).accept(visitor);
		}
		objects = visitor.giveObjects();
		objects.trimToSize();
	}

	@Override
	public final void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subreferences = propCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on
				Object_Definition def;
				for (int j = 0; j < objects.getNofObjects(); j++) {
					def = objects.getObjectByIndex(j);
					def.addProposal(propCollector, i + 1);
				}
			} else {
				// final part of the reference
				Object_Definition def;
				for (int j = 0; j < objects.getNofObjects(); j++) {
					def = objects.getObjectByIndex(j);
					def.addProposal(propCollector, i);
				}
			}
		}
	}

	@Override
	public final void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on
				Object_Definition def;
				for (int j = 0; j < objects.getNofObjects(); j++) {
					def = objects.getObjectByIndex(j);
					def.addDeclaration(declarationCollector, i + 1);
				}
			} else {
				// final part of the reference
				Object_Definition def;
				for (int j = 0; j < objects.getNofObjects(); j++) {
					def = objects.getObjectByIndex(j);
					def.addDeclaration(declarationCollector, i);
				}
			}
		}
	}

	@Override
	protected final boolean memberAccept(ASTVisitor v) {
		// TODO: objectSetElements ?
		if (objects != null && !objects.accept(v)) {
			return false;
		}
		return true;
	}
}
