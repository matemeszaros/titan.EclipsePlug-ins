/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IOutlineElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IAppendableSyntax;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.SkeletonTemplateProposal;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Keywords;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The For_Loop_Definitions class represents the initial definitions of for
 * loops.
 * <p>
 * These definitions differ from the simple ones, in that the order of their
 * declaration matters, and the can hide higher level definitions. And still
 * they are not Definition_Statements, as there is no exact statement block they
 * could belong to.
 * 
 * @author Kristof Szabados
 * */
public final class For_Loop_Definitions extends Assignments implements ILocateableNode, IIncrementallyUpdateable {
	/** The list of definitions contained in this scope. */
	private final List<Definition> definitions;

	/** The location physically enclosing the contained definitions. */
	private Location location;

	/**
	 * A hashmap of definitions, used to find multiple declarations, and to
	 * speed up searches.
	 */
	private HashMap<String, Definition> definitionMap;

	/**
	 * Holds the last time when these definitions were checked, or null if
	 * never.
	 */
	private CompilationTimeStamp lastCompilationTimeStamp;

	/**
	 * Holds the last time when the uniqueness of these definitions were
	 * checked, or null if never.
	 */
	private CompilationTimeStamp lastUniquenessCheckTimeStamp;

	public For_Loop_Definitions() {
		definitions = new CopyOnWriteArrayList<Definition>();
		scopeName = "definitions";
		location = NULL_Location.INSTANCE;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		Definition definition;
		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			definition = iterator.next();

			if (definition == child) {
				Identifier identifier = definition.getIdentifier();

				return builder.append(INamedNode.DOT).append(identifier.getDisplayName());
			}
		}

		return builder;
	}

	@Override
	public int getNofAssignments() {
		return definitions.size();
	}

	@Override
	public Definition getAssignmentByIndex(final int i) {
		return definitions.get(i);
	}

	@Override
	public Object[] getOutlineChildren() {
		List<IOutlineElement> outlineDefinitions = new ArrayList<IOutlineElement>();
		// Take care of ordering.
		outlineDefinitions.addAll(definitions);
		Collections.sort(outlineDefinitions, new Comparator<IOutlineElement>() {
			@Override
			public int compare(final IOutlineElement o1, final IOutlineElement o2) {
				Location l1 = o1.getIdentifier().getLocation();
				Location l2 = o2.getIdentifier().getLocation();
				if (l1.getOffset() < l2.getOffset()) {
					return -1;
				} else if (l1.getOffset() > l2.getOffset()) {
					return 1;
				}

				return 0;
			}
		});
		return outlineDefinitions.toArray();
	}

	@Override
	public String getOutlineIcon() {
		return "ttcn.gif";
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	/**
	 * Adds a definition to the list of definitions.
	 * <p>
	 * The scope of the newly added definition is set to this scope here.
	 * 
	 * @param definition
	 *                the definition to be added
	 * */
	public void addDefinition(final Definition definition) {
		if (definition != null && definition.getIdentifier() != null && definition.getIdentifier().getLocation() != null) {
			definition.setMyScope(this);
			definitions.add(definition);
			definition.setFullNameParent(this);
		}
	}

	/**
	 * Adds a list of definitions to the list of definitions.
	 * <p>
	 * The scope of the newly added definitions is set to this scope scope
	 * here.
	 * 
	 * @param definitionList
	 *                the definitions to be added
	 * */
	public void addDefinitions(final List<Definition> definitionList) {
		if (definitionList != null) {
			for (Definition definition : definitionList) {
				addDefinition(definition);
			}
		}
	}

	/**
	 * Checks the uniqueness of the definitions, and also builds a hashmap
	 * of them to speed up further searches.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	protected void checkUniqueness(final CompilationTimeStamp timestamp) {
		if (lastUniquenessCheckTimeStamp != null && !lastUniquenessCheckTimeStamp.isLess(timestamp)) {
			return;
		}

		lastUniquenessCheckTimeStamp = timestamp;

		if (definitionMap == null) {
			definitionMap = new HashMap<String, Definition>(definitions.size());
		}

		definitionMap.clear();

		String definitionName;
		Definition definition;
		for (int i = 0, size = definitions.size(); i < size; i++) {
			definition = definitions.get(i);
			Identifier identifier = definition.getIdentifier();
			definitionName = identifier.getName();
			if (definitionMap.containsKey(definitionName)) {
				final Location otherLocation = definitionMap.get(definitionName).getIdentifier().getLocation();
				otherLocation.reportSingularSemanticError(MessageFormat.format(DUPLICATEDEFINITIONFIRST, identifier.getDisplayName()));
				identifier.getLocation().reportSemanticError(
						MessageFormat.format(DUPLICATEDEFINITIONREPEATED, identifier.getDisplayName()));
			} else {
				definitionMap.put(definitionName, definition);
				if (parentScope != null && definition.getLocation() != null) {
					if (parentScope.hasAssignmentWithId(timestamp, identifier)) {
						definition.getLocation().reportSemanticError(
								MessageFormat.format(StatementBlock.HIDINGSCOPEELEMENT, identifier.getDisplayName()));

						List<ISubReference> subReferences = new ArrayList<ISubReference>();
						subReferences.add(new FieldSubReference(identifier));
						Reference reference = new Reference(null, subReferences);
						Assignment assignment = parentScope.getAssBySRef(timestamp, reference);
						if (assignment != null && assignment.getLocation() != null) {
							assignment.getLocation().reportSingularSemanticError(
									MessageFormat.format(StatementBlock.HIDDENSCOPEELEMENT,
											identifier.getDisplayName()));
						}
					} else if (parentScope.isValidModuleId(identifier)) {
						definition.getLocation().reportSemanticWarning(
								MessageFormat.format(StatementBlock.HIDINGMODULEIDENTIFIER,
										identifier.getDisplayName()));
					}
				}
			}
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastCompilationTimeStamp != null && !lastCompilationTimeStamp.isLess(timestamp)) {
			return;
		}

		lastCompilationTimeStamp = timestamp;
		lastUniquenessCheckTimeStamp = timestamp;

		if (definitionMap == null) {
			definitionMap = new HashMap<String, Definition>(definitions.size());
		}

		definitionMap.clear();

		String definitionName;
		Definition definition;
		Identifier identifier;
		for (int i = 0, size = definitions.size(); i < size; i++) {
			definition = definitions.get(i);
			identifier = definition.getIdentifier();
			definitionName = identifier.getName();
			if (definitionMap.containsKey(definitionName)) {
				final Location otherLocation = definitionMap.get(definitionName).getIdentifier().getLocation();
				otherLocation.reportSingularSemanticError(MessageFormat.format(DUPLICATEDEFINITIONFIRST, identifier.getDisplayName()));
				identifier.getLocation().reportSemanticError(
						MessageFormat.format(DUPLICATEDEFINITIONREPEATED, identifier.getDisplayName()));
			} else {
				definitionMap.put(definitionName, definition);
				if (parentScope != null && definition.getLocation() != null) {
					if (parentScope.hasAssignmentWithId(timestamp, identifier)) {
						definition.getLocation().reportSemanticError(
								MessageFormat.format(StatementBlock.HIDINGSCOPEELEMENT, identifier.getDisplayName()));

						List<ISubReference> subReferences = new ArrayList<ISubReference>();
						subReferences.add(new FieldSubReference(identifier));
						Reference reference = new Reference(null, subReferences);
						Assignment assignment = parentScope.getAssBySRef(timestamp, reference);
						if (assignment != null && assignment.getLocation() != null) {
							assignment.getLocation().reportSingularSemanticError(
									MessageFormat.format(StatementBlock.HIDDENSCOPEELEMENT,
											identifier.getDisplayName()));
						}
					} else if (parentScope.isValidModuleId(identifier)) {
						definition.getLocation().reportSemanticWarning(
								MessageFormat.format(StatementBlock.HIDINGMODULEIDENTIFIER,
										identifier.getDisplayName()));
					}
				}
			}

			definition.check(timestamp);
		}
	}

	@Override
	public void postCheck() {
		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			iterator.next().postCheck();
		}
	}

	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference) {
		if (reference.getModuleIdentifier() != null) {
			return getModuleScope().getAssBySRef(timestamp, reference);
		}

		Identifier identifier = reference.getId();
		if (identifier == null) {
			return getModuleScope().getAssBySRef(timestamp, reference);
		}

		if (lastUniquenessCheckTimeStamp == null) {
			checkUniqueness(timestamp);
		}

		Definition result = definitionMap.get(identifier.getName());
		if (result != null) {
			return result;
		}

		return getParentScope().getAssBySRef(timestamp, reference);
	}

	/**
	 * Searches the definitions for one with a given Identifier.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param id
	 *                the identifier used to find the definition
	 * 
	 * @return the definition if found, or null otherwise
	 * */
	@Override
	public Definition getLocalAssignmentByID(final CompilationTimeStamp timestamp, final Identifier id) {
		if (lastUniquenessCheckTimeStamp == null) {
			checkUniqueness(timestamp);
		}

		return definitionMap.get(id.getName());
	}

	@Override
	public boolean hasLocalAssignmentWithID(final CompilationTimeStamp timestamp, final Identifier identifier) {
		if (lastUniquenessCheckTimeStamp == null) {
			checkUniqueness(timestamp);
		}

		return definitionMap.containsKey(identifier.getName());
	}

	@Override
	public void addProposal(final ProposalCollector propCollector) {
		if (propCollector.getReference().getModuleIdentifier() == null) {
			for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
				iterator.next().addProposal(propCollector, 0);
			}
		}
		super.addProposal(propCollector);
	}

	@Override
	public void addSkeletonProposal(final ProposalCollector propCollector) {
		for (SkeletonTemplateProposal templateProposal : TTCN3CodeSkeletons.MODULE_LEVEL_SKELETON_PROPOSALS) {
			propCollector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(),
					TTCN3CodeSkeletons.SKELETON_IMAGE);
		}
	}

	@Override
	public void addKeywordProposal(final ProposalCollector propCollector) {
		propCollector.addProposal(TTCN3Keywords.MODULE_SCOPE, null, TTCN3Keywords.KEYWORD);
		propCollector.addProposal(TTCN3Keywords.GENERALLY_USABLE, null, TTCN3Keywords.KEYWORD);
		super.addKeywordProposal(propCollector);
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector) {
		if (declarationCollector.getReference().getModuleIdentifier() == null) {
			for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
				iterator.next().addDeclaration(declarationCollector, 0);
			}
		}
		super.addDeclaration(declarationCollector);
	}

	/**
	 * Handles the incremental parsing of this list of definitions.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (!isDamaged) {
			// handle the simple case quickly
			for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
				Definition temp = iterator.next();
				Location temporalLocation = temp.getLocation();
				if (reparser.isAffected(temporalLocation)) {
					reparser.updateLocation(temporalLocation);
				}
			}

			return;
		}

		// calculate damaged region
		int result = 0;

		boolean enveloped = false;
		int nofDamaged = 0;
		int leftBoundary = location.getOffset() + 1;
		int rightBoundary = location.getEndOffset() - 1;
		final int damageOffset = reparser.getDamageStart();
		IAppendableSyntax lastAppendableBeforeChange = null;
		IAppendableSyntax lastPrependableBeforeChange = null;

		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext() && !enveloped;) {
			Definition temp = iterator.next();
			Location tempLocation = temp.getLocation();
			if (reparser.envelopsDamage(tempLocation)) {
				enveloped = true;
				leftBoundary = tempLocation.getOffset();
				rightBoundary = tempLocation.getEndOffset();
			} else if (reparser.isDamaged(tempLocation)) {
				nofDamaged++;
				if (reparser.getDamageStart() == tempLocation.getEndOffset()) {
					lastAppendableBeforeChange = temp;
				} else if (reparser.getDamageEnd() == tempLocation.getOffset()) {
					lastPrependableBeforeChange = temp;
				}
			} else {
				if (tempLocation.getEndOffset() < damageOffset && tempLocation.getEndOffset() > leftBoundary) {
					leftBoundary = tempLocation.getEndOffset() + 1;
					lastAppendableBeforeChange = temp;
				}
				if (tempLocation.getOffset() >= damageOffset && tempLocation.getOffset() < rightBoundary) {
					rightBoundary = tempLocation.getOffset();
					lastPrependableBeforeChange = temp;
				}
			}
		}

		// extend the reparser to the calculated values if the damage
		// was not enveloped
		if (!enveloped && isDamaged) {
			reparser.extendDamagedRegion(leftBoundary, rightBoundary);

			// if there is an element that is right now being
			// extended we should add it to the damaged domain as
			// the extension might be correct
			if (lastAppendableBeforeChange != null) {
				boolean isBeingExtended = reparser.startsWithFollow(lastAppendableBeforeChange.getPossibleExtensionStarterTokens());
				if (isBeingExtended) {
					leftBoundary = lastAppendableBeforeChange.getLocation().getOffset();
					nofDamaged++;
					enveloped = false;
					reparser.extendDamagedRegion(leftBoundary, rightBoundary);
				}
			}

			if (lastPrependableBeforeChange != null) {
				List<Integer> temp = lastPrependableBeforeChange.getPossiblePrefixTokens();

				if (temp != null && reparser.endsWithToken(temp)) {
					rightBoundary = lastPrependableBeforeChange.getLocation().getEndOffset();
					nofDamaged++;
					enveloped = false;
					reparser.extendDamagedRegion(leftBoundary, rightBoundary);
				}
			}

			if (nofDamaged != 0) {
				// remove damaged stuff
				removeStuffInRange(reparser);

				lastUniquenessCheckTimeStamp = null;
			}
		}

		// update what is left
		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			Definition temp = iterator.next();
			Location temporalLocation = temp.getLocation();
			if (reparser.isAffected(temporalLocation)) {
				try {
					temp.updateSyntax(reparser, enveloped && reparser.envelopsDamage(temporalLocation));
					if (reparser.getNameChanged()) {
						lastUniquenessCheckTimeStamp = null;
						reparser.setNameChanged(false);
					}
				} catch (ReParseException e) {
					if (e.getDepth() == 1) {
						enveloped = false;
						definitions.remove(temp);
						reparser.extendDamagedRegion(temporalLocation);
						result = 1;
					} else {
						e.decreaseDepth();
						throw e;
					}
				}
			}
		}

		if (result == 1) {
			removeStuffInRange(reparser);
			lastUniquenessCheckTimeStamp = null;
		}

		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			Definition temp = iterator.next();
			Location temporalLocation = temp.getLocation();
			if (reparser.isAffected(temporalLocation)) {
				reparser.updateLocation(temporalLocation);
			}
		}

		if (!enveloped && reparser.envelopsDamage(location)) {
			// right now this can not be processed efficiently, the
			// whole definition region has to be re-parsed
			throw new ReParseException();
		}

		if (result != 0) {
			lastUniquenessCheckTimeStamp = null;
			throw new ReParseException(result);
		}
	}

	/**
	 * Destroy every element trapped inside the damage radius.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * */
	private void removeStuffInRange(final TTCN3ReparseUpdater reparser) {
		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			Definition temp = iterator.next();
			if (reparser.isDamaged(temp.getLocation())) {
				reparser.extendDamagedRegion(temp.getLocation());
				definitions.remove(temp);
			}
		}
	}

	@Override
	public Assignment getEnclosingAssignment(final int offset) {
		if (definitions == null) {
			return null;
		}
		for (Definition definition : definitions) {
			if (definition.getLocation().containsOffset(offset)) {
				return definition;
			}
		}
		return null;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		for (Definition definition : definitions) {
			definition.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		for (Definition definition : definitions) {
			if (!definition.accept(v)) {
				return false;
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}

	@Override
	public Iterator<Assignment> iterator() {
		return new Iterator<Assignment>() {
			Iterator<Definition> it = definitions.iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Assignment next() {
				return it.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
