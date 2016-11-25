/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IOutlineElement;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IAppendableSyntax;
import org.eclipse.titan.designer.core.LoadBalancingUtilities;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.SkeletonTemplateProposal;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Keywords;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ITTCN3ReparseBase;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Reparser;

/**
 * The Definitions class represents the scope of module level definitions inside
 * Modules.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 * @author Jeno Attila Balasko
 */
public final class Definitions extends Assignments implements ILocateableNode {
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
	 * Stores those definitions which were identified to be duplicates of
	 * others. This is used to provide much faster operation.
	 * */
	protected List<Definition> doubleDefinitions;

	/**
	 * Holds the last time when these definitions were checked, or null if
	 * never.
	 */
	private CompilationTimeStamp lastCompilationTimeStamp;

	/**
	 * Holds the last time when the uniqueness of these definitions were
	 * checked, or null if never.
	 */
	protected CompilationTimeStamp lastUniquenessCheckTimeStamp;

	/** The list of the groups contained in this scope. */
	private final List<Group> groups;

	public Definitions() {
		definitions = new CopyOnWriteArrayList<Definition>();
		groups = new ArrayList<Group>();
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

		Group group;
		for (int i = 0, size = groups.size(); i < size; i++) {
			group = groups.get(i);

			if (group == child) {
				Identifier identifier = group.getIdentifier();
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

	protected void removeGroups() {
		//Do nothing
	}

	@Override
	public Object[] getOutlineChildren() {
		List<IOutlineElement> outlineDefinitions = new ArrayList<IOutlineElement>();
		// Take care of ordering.
		outlineDefinitions.addAll(definitions);
		outlineDefinitions.addAll(groups);
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
		lastUniquenessCheckTimeStamp = null;
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
		lastUniquenessCheckTimeStamp = null;
		if (definitionList != null) {
			for (Definition definition : definitionList) {
				addDefinition(definition);
			}
		}
	}

	/**
	 * Adds a group to the list of groups.
	 * <p>
	 * The scope of the newly added group is set to this scope here.
	 * 
	 * @param group
	 *                the group to be added
	 */
	public void addGroup(final Group group) {
		if (group != null && group.getIdentifier() != null && group.getIdentifier().getLocation() != null) {
			group.setMyScope(this);
			groups.add(group);
			group.setFullNameParent(this);
		}
	}

	/**
	 * Checks the uniqueness of the definitions, and also builds a hashmap
	 * of them to speed up further searches.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	//checkUniquiness has been splitted into to parts because 
	//the check should be done at the beginning of the check but the reporting shall be done finally
	protected void checkUniqueness(final CompilationTimeStamp timestamp) {
		createDefinitionMap(timestamp); //creates a hash map
		reportDoubleDefinitions();	
	}

	// creates or refreshes DefinitionMap and doubleDefinitions but not reports the found double definitons
	protected void createDefinitionMap(final CompilationTimeStamp timestamp) {
		if (lastUniquenessCheckTimeStamp != null && !lastUniquenessCheckTimeStamp.isLess(timestamp)) {
			return;
		}
		lastUniquenessCheckTimeStamp = timestamp;
		
		if (doubleDefinitions != null) {
			doubleDefinitions.clear();
		}
		
		//(rebuild) definitionMap and doubleDefinitions from the updated field "definitions"
		definitionMap = new HashMap<String, Definition>(definitions.size());
		String definitionName;
		Definition definition;
		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			definition = iterator.next();
			definitionName = definition.getIdentifier().getName();
			if (definitionMap.containsKey(definitionName)) {
				if (doubleDefinitions == null) {
					doubleDefinitions = new ArrayList<Definition>();
				}
				doubleDefinitions.add(definition);
			} else {
				definitionMap.put(definitionName, definition);
			}
		}
		//TODO: check if this is necessary or not:
		if (doubleDefinitions != null) {
			for (int i = 0, size = doubleDefinitions.size(); i < size; i++) {
				definitions.remove(doubleDefinitions.get(i));
			}
		}
		
	}

	//reports the found double definitons. It is supposed doubleDefinition to be created already
	protected void reportDoubleDefinitions() {
		if (doubleDefinitions != null) {
			String definitionName;
			Definition definition;
			for (int i = 0, size = doubleDefinitions.size(); i < size; i++) {
				definitions.remove(doubleDefinitions.get(i));
			}

			Identifier identifier;
			for (int i = 0, size = doubleDefinitions.size(); i < size; i++) {
				definition = doubleDefinitions.get(i);
				identifier = definition.getIdentifier();
				definitionName = identifier.getName();
				try {
					final Location otherLocation = definitionMap.get(definitionName).getIdentifier().getLocation();
					otherLocation.reportSingularSemanticError(MessageFormat.format(DUPLICATEDEFINITIONFIRST,
							identifier.getDisplayName()));
					identifier.getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATEDEFINITIONREPEATED, identifier.getDisplayName()));
				} catch (NullPointerException e) {
					ErrorReporter.logError("Nullpointer was detected when reporting duplication error for definition: "
							+ definitionName);
					throw e;
				}
			}
		}
	}

	/**
	 * Checks the uniqueness of the groups, and after that the groups
	 * themselves..
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	private void checkGroups(final CompilationTimeStamp timestamp) {
		if (groups.isEmpty()) {
			return;
		}

		HashMap<String, Group> groupMap = new HashMap<String, Group>(groups.size());
		HashMap<String, Definition> defs = new HashMap<String, Definition>(definitions.size());

		Definition definition;
		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			definition = iterator.next();

			if (definition.getParentGroup() == null) {
				String defName = definition.getIdentifier().getName();
				if (!defs.containsKey(defName)) {
					defs.put(defName, definition);
				}
			}
		}

		Group group;
		for (int i = 0, size = groups.size(); i < size; i++) {
			group = groups.get(i);

			String groupName = group.getIdentifier().getName();
			if (defs.containsKey(groupName)) {
				group.getIdentifier().getLocation().reportSemanticError(MessageFormat.format(Group.GROUPCLASHGROUP, groupName));
				defs.get(groupName).getIdentifier().getLocation()
						.reportSingularSemanticError(MessageFormat.format(Group.GROUPCLASHDEFINITION, groupName));
			}
			if (groupMap.containsKey(groupName)) {
				groupMap.get(groupName).getIdentifier().getLocation()
						.reportSingularSemanticError(MessageFormat.format(Group.DUPLICATEGROUPFIRST, groupName));
				group.getIdentifier().getLocation()
						.reportSemanticError(MessageFormat.format(Group.DUPLICATEGROUPREPEATED, groupName));
			} else {
				groupMap.put(groupName, group);
			}
		}

		for (int i = 0, size = groups.size(); i < size; i++) {
			groups.get(i).check(timestamp);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastCompilationTimeStamp != null && !lastCompilationTimeStamp.isLess(timestamp)) {
			return;
		}
		lastCompilationTimeStamp = timestamp;
		
		Module module = getModuleScope();
		if (module != null) {
			if (module.getSkippedFromSemanticChecking()) {
				return;
			}
		}
		
		// MarkerHandler.markAllSemanticMarkersForRemoval(this); //this removes the imports as well
		
		checkGroups(timestamp);

		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			iterator.next().check(timestamp);
			LoadBalancingUtilities.astNodeChecked();
		}
		
		checkUniqueness(timestamp);
	}
	
	/**
	 * Experimental method for BrokenPartsViaInvertedImports.
	 */
	public void checkWithDefinitions(final CompilationTimeStamp timestamp, final List<Assignment> assignments) {
		if (lastCompilationTimeStamp != null && !lastCompilationTimeStamp.isLess(timestamp)) {
			return;
		}
		lastCompilationTimeStamp = timestamp;
		
		//Remove only the old markers. Cannot this be done even more earlier????
		for( final Assignment assignment : assignments) {
			if(assignment.getLastTimeChecked() == null ||  assignment.getLastTimeChecked().isLess(timestamp) ){
				MarkerHandler.markAllSemanticMarkersForRemoval(assignment);
			}
		}
		
		checkUniqueness(timestamp);
		checkGroups(timestamp);

		for (Iterator<Assignment> iterator = assignments.iterator(); iterator.hasNext();) {
			Assignment assignmentFrom  = iterator.next();
			if(definitionMap.containsKey(assignmentFrom.getIdentifier().getName())) {
				assignmentFrom.check(timestamp);
				LoadBalancingUtilities.astNodeChecked();
			}
		}
		
	}
	

	@Override
	public void postCheck() {
		Module module = getModuleScope();
		if (module != null) {
			if (module.getSkippedFromSemanticChecking()) {
				return;
			}
		}

		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			iterator.next().postCheck();
		}
	}

	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference) {
		return getAssBySRef(timestamp, reference, null);
	}
	
	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference, IReferenceChain refChain) {
		if (reference.getModuleIdentifier() != null) {
			return getModuleScope().getAssBySRef(timestamp, reference);
		}

		Identifier identifier = reference.getId();
		if (identifier == null) {
			return getModuleScope().getAssBySRef(timestamp, reference);
		}
				
		if (lastUniquenessCheckTimeStamp == null) {
			createDefinitionMap(timestamp);
		} //uniqueness shall be reported only after checking all the definitions

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
		if (!this.groups.isEmpty()) {

			for (Group item : this.groups) {
				item.addDeclaration(declarationCollector);
			}
		}
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
	 * @param importedModules
	 *                the list of module importations found in the same
	 *                module.
	 * @param friendModules
	 *                the list of friend module declaration in the same
	 *                module.
	 * @param controlpart
	 *                the control part found in the same module.
	 * @throws ReParseException
	 *                 if there was an error while refreshing the location
	 *                 information and it could not be solved internally.
	 * */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final List<ImportModule> importedModules,
			final List<FriendModule> friendModules, final ControlPart controlpart) throws ReParseException {
		// calculate damaged region
		int result = 0;
		if (doubleDefinitions != null) {
			definitions.addAll(doubleDefinitions);
		}

		boolean enveloped = false;
		int nofDamaged = 0;
		int leftBoundary = location.getOffset() + 1;
		int rightBoundary = location.getEndOffset() - 1;
		final int damageOffset = reparser.getDamageStart();
		IAppendableSyntax lastAppendableBeforeChange = null;
		IAppendableSyntax lastPrependableBeforeChange = null;
		boolean isControlPossible = controlpart == null;

		if (controlpart != null) {
			Location tempLocation = controlpart.getLocation();
			if (reparser.envelopsDamage(tempLocation)) {
				enveloped = true;
			} else if (!reparser.isDamaged(tempLocation)) {
				if (tempLocation.getEndOffset() < damageOffset && tempLocation.getEndOffset() > leftBoundary) {
					leftBoundary = tempLocation.getEndOffset() + 1;
					lastAppendableBeforeChange = controlpart;
				}
				if (tempLocation.getOffset() >= damageOffset && tempLocation.getOffset() < rightBoundary) {
					rightBoundary = tempLocation.getOffset();
					lastPrependableBeforeChange = controlpart;
				}
			}
		}

		for (int i = 0, size = groups.size(); i < size && !enveloped; i++) {
			Group tempGroup = groups.get(i);
			Location tempLocation = tempGroup.getLocation();
			if (reparser.envelopsDamage(tempLocation)) {
				enveloped = true;
				leftBoundary = tempLocation.getOffset();
				rightBoundary = tempLocation.getEndOffset();
			} else if (reparser.isDamaged(tempLocation)) {
				nofDamaged++;
			} else {
				if (tempLocation.getEndOffset() < damageOffset && tempLocation.getEndOffset() > leftBoundary) {
					leftBoundary = tempLocation.getEndOffset();
					lastAppendableBeforeChange = tempGroup;
				}
				if (tempLocation.getOffset() >= damageOffset && tempLocation.getOffset() < rightBoundary) {
					rightBoundary = tempLocation.getOffset();
					lastPrependableBeforeChange = tempGroup;
				}
			}
		}
		if (!groups.isEmpty()) {
			isControlPossible &= groups.get(groups.size() - 1).getLocation().getEndOffset() <= leftBoundary;
		}

		for (int i = 0, size = importedModules.size(); i < size && !enveloped; i++) {
			ImportModule tempImport = importedModules.get(i);
			if (tempImport.getParentGroup() == null) {
				Location tempLocation = tempImport.getLocation();
				if (reparser.envelopsDamage(tempLocation)) {
					enveloped = true;
					leftBoundary = tempLocation.getOffset();
					rightBoundary = tempLocation.getEndOffset();
				} else if (reparser.isDamaged(tempLocation)) {
					nofDamaged++;
				} else {
					if (tempLocation.getEndOffset() < damageOffset && tempLocation.getEndOffset() > leftBoundary) {
						leftBoundary = tempLocation.getEndOffset() + 1;
						lastAppendableBeforeChange = tempImport;
					}
					if (tempLocation.getOffset() >= damageOffset && tempLocation.getOffset() < rightBoundary) {
						rightBoundary = tempLocation.getOffset();
						lastPrependableBeforeChange = tempImport;
					}
				}
			}
		}
		if (!importedModules.isEmpty()) {
			isControlPossible &= importedModules.get(importedModules.size() - 1).getLocation().getEndOffset() <= leftBoundary;
		}

		for (int i = 0, size = friendModules.size(); i < size && !enveloped; i++) {
			FriendModule tempFriend = friendModules.get(i);
			if (tempFriend.getParentGroup() == null) {
				Location tempLocation = tempFriend.getLocation();
				if (reparser.envelopsDamage(tempLocation)) {
					enveloped = true;
					leftBoundary = tempLocation.getOffset();
					rightBoundary = tempLocation.getEndOffset();
				} else if (reparser.isDamaged(tempLocation)) {
					nofDamaged++;
				} else {
					if (tempLocation.getEndOffset() < damageOffset && tempLocation.getEndOffset() > leftBoundary) {
						leftBoundary = tempLocation.getEndOffset() + 1;
						lastAppendableBeforeChange = tempFriend;
					}
					if (tempLocation.getOffset() >= damageOffset && tempLocation.getOffset() < rightBoundary) {
						rightBoundary = tempLocation.getOffset();
						lastPrependableBeforeChange = tempFriend;
					}
				}
			}
		}
		if (!friendModules.isEmpty()) {
			isControlPossible &= friendModules.get(friendModules.size() - 1).getLocation().getEndOffset() <= leftBoundary;
		}

		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext() && !enveloped;) {
			Definition temp = iterator.next();
			if (temp.getParentGroup() == null) {
				Location tempLocation = temp.getLocation();
				Location cumulativeLocation = temp.getCumulativeDefinitionLocation();
				if (tempLocation.equals(cumulativeLocation) && reparser.envelopsDamage(cumulativeLocation)) {
					enveloped = true;
					leftBoundary = cumulativeLocation.getOffset();
					rightBoundary = cumulativeLocation.getEndOffset();
				} else if (reparser.isDamaged(cumulativeLocation)) {
					nofDamaged++;
					if (reparser.getDamageStart() == cumulativeLocation.getEndOffset()) {
						lastAppendableBeforeChange = temp;
					} else if (reparser.getDamageEnd() == cumulativeLocation.getOffset()) {
						lastPrependableBeforeChange = temp;
					}
				} else {
					if (cumulativeLocation.getEndOffset() < damageOffset && cumulativeLocation.getEndOffset() > leftBoundary) {
						leftBoundary = cumulativeLocation.getEndOffset() + 1;
						lastAppendableBeforeChange = temp;
					}
					if (cumulativeLocation.getOffset() >= damageOffset && cumulativeLocation.getOffset() < rightBoundary) {
						rightBoundary = cumulativeLocation.getOffset();
						lastPrependableBeforeChange = temp;
					}
				}
				Location tempCommentLocation = temp.getCommentLocation();
				if (tempCommentLocation != null && reparser.isDamaged(tempCommentLocation)) {
					nofDamaged++;
					rightBoundary = tempLocation.getEndOffset() + 1;
				}
			}
		}
		if (!definitions.isEmpty()) {
			isControlPossible &= definitions.get(definitions.size() - 1).getLocation().getEndOffset() <= leftBoundary;
		}

		// extend the reparser to the calculated values if the damage
		// was not enveloped
		if (!enveloped && reparser.isDamaged(location)) {
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
				removeStuffInRange(reparser, importedModules, friendModules);

				if (doubleDefinitions != null) {
					doubleDefinitions.clear();
				}
				lastUniquenessCheckTimeStamp = null;
				lastCompilationTimeStamp = null;
			}
		}

		// update what is left
		for (int i = 0; i < groups.size(); i++) {
			Group temp = groups.get(i);
			Location tempLocation = temp.getLocation();
			if (reparser.isAffected(tempLocation)) {
				try {
					temp.updateSyntax(reparser, importedModules, definitions, friendModules);
				} catch (ReParseException e) {
					if (e.getDepth() == 1) {
						enveloped = false;
						groups.remove(i);
						i--;
						reparser.extendDamagedRegion(tempLocation);
						result = 1;
					} else {
						if (doubleDefinitions != null) {
							doubleDefinitions.clear();
						}
						lastUniquenessCheckTimeStamp = null;
						e.decreaseDepth();
						throw e;
					}
				}
			}
		}

		for (int i = 0; i < importedModules.size(); i++) {
			ImportModule temp = importedModules.get(i);
			if (temp.getParentGroup() == null) {
				Location tempLocation = temp.getLocation();
				if (reparser.isAffected(tempLocation)) {
					try {
						boolean isDamaged = enveloped && reparser.envelopsDamage(tempLocation);
						temp.updateSyntax(reparser, enveloped && reparser.envelopsDamage(tempLocation));
						if(isDamaged) {
							((TTCN3Module) parentScope).checkRoot();
						}
					} catch (ReParseException e) {
						if (e.getDepth() == 1) {
							enveloped = false;
							importedModules.remove(i);
							i--;
							reparser.extendDamagedRegion(tempLocation);
							result = 1;
						} else {
							if (doubleDefinitions != null) {
								doubleDefinitions.clear();
							}
							lastUniquenessCheckTimeStamp = null;
							e.decreaseDepth();
							throw e;
						}
					}
				}
			}
		}

		for (int i = 0; i < friendModules.size(); i++) {
			FriendModule temp = friendModules.get(i);
			if (temp.getParentGroup() == null) {
				Location tempLocation = temp.getLocation();
				if (reparser.isAffected(tempLocation)) {
					try {
						boolean isDamaged = enveloped && reparser.envelopsDamage(tempLocation);
						temp.updateSyntax(reparser, enveloped && reparser.envelopsDamage(tempLocation));
						if(isDamaged) {
							((TTCN3Module) parentScope).checkRoot();
						}
					} catch (ReParseException e) {
						if (e.getDepth() == 1) {
							enveloped = false;
							friendModules.remove(i);
							i--;
							reparser.extendDamagedRegion(tempLocation);
							result = 1;
						} else {
							if (doubleDefinitions != null) {
								doubleDefinitions.clear();
							}
							lastUniquenessCheckTimeStamp = null;
							e.decreaseDepth();
							throw e;
						}
					}
				}
			}
		}

		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			Definition temp = iterator.next();
			if (temp.getParentGroup() == null) {
				Location tempLocation = temp.getLocation();
				Location cumulativeLocation = temp.getCumulativeDefinitionLocation();
				if (reparser.isAffected(cumulativeLocation)) {
					try {
						boolean isDamaged = enveloped && reparser.envelopsDamage(tempLocation);
						temp.updateSyntax(reparser, isDamaged);
						if (reparser.getNameChanged()) {
							if (doubleDefinitions != null) {
								doubleDefinitions.clear();
							}
							lastUniquenessCheckTimeStamp = null;
							lastCompilationTimeStamp = null; //to recheck the whole module 
							//TODO: BAAT:trigger the recheck of the importing modules as well!!
							reparser.setNameChanged(false);
							// This could also spread
						}
						if(isDamaged) {
							temp.checkRoot();//TODO lets move this into the definitions
						}
					} catch (ReParseException e) {
						if (e.getDepth() == 1) {
							enveloped = false;
							definitions.remove(temp);
							reparser.extendDamagedRegion(cumulativeLocation);
							result = 1;
						} else {
							if (doubleDefinitions != null) {
								doubleDefinitions.clear();
							}
							lastUniquenessCheckTimeStamp = null;
							e.decreaseDepth();
							throw e;
						}
					}
				}
			}
		}

		if (result == 1) {
			removeStuffInRange(reparser, importedModules, friendModules);
			if (doubleDefinitions != null) {
				doubleDefinitions.clear();
			}
			lastUniquenessCheckTimeStamp = null;
			lastCompilationTimeStamp = null;
		}

		for (int i = 0, size = groups.size(); i < size; i++) {
			Group temp = groups.get(i);
			Location tempLocation = temp.getLocation();
			if (reparser.isAffected(tempLocation)) {
				reparser.updateLocation(tempLocation);
			}
		}

		for (int i = 0, size = importedModules.size(); i < size; i++) {
			ImportModule temp = importedModules.get(i);
			if (temp.getParentGroup() == null) {
				Location tempLocation = temp.getLocation();
				if (reparser.isAffected(tempLocation)) {
					reparser.updateLocation(tempLocation);
				}
			}
		}

		for (int i = 0, size = friendModules.size(); i < size; i++) {
			FriendModule temp = friendModules.get(i);
			if (temp.getParentGroup() == null) {
				Location tempLocation = temp.getLocation();
				if (reparser.isAffected(tempLocation)) {
					reparser.updateLocation(tempLocation);
				}
			}
		}

		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			Definition temp = iterator.next();
			if (temp.getParentGroup() == null) {
				Location tempLocation = temp.getLocation();
				Location cumulativeLocation = temp.getCumulativeDefinitionLocation();
				if (reparser.isAffected(tempLocation)) {
					if(tempLocation != cumulativeLocation) {
						reparser.updateLocation(cumulativeLocation);
					}
					reparser.updateLocation(tempLocation);
				}
			}
		}

		final boolean tempIsControlPossible = isControlPossible;
		if (!enveloped && reparser.envelopsDamage(location)) {
			reparser.extendDamagedRegion(leftBoundary, rightBoundary);
			result = reparse( reparser, tempIsControlPossible );
			result = Math.max(result - 1, 0);
			lastCompilationTimeStamp = null;
		}

		if (result == 0) {
			lastUniquenessCheckTimeStamp = null;
		} else {
			if (doubleDefinitions != null) {
				doubleDefinitions.clear();
			}
			lastUniquenessCheckTimeStamp = null;
			throw new ReParseException(result);
		}
	}

	private int reparse( final TTCN3ReparseUpdater aReparser, final boolean aTempIsControlPossible ) {
		return aReparser.parse(new ITTCN3ReparseBase() {
			@Override
			public void reparse(final Ttcn3Reparser parser) {
				List<Definition> allDefinitions = new ArrayList<Definition>();
				List<Definition> localDefinitions = new ArrayList<Definition>();
				List<Group> localGroups = new ArrayList<Group>();
				List<ImportModule> allImports = new ArrayList<ImportModule>();
				List<ImportModule> localImports = new ArrayList<ImportModule>();
				List<FriendModule> allFriends = new ArrayList<FriendModule>();
				List<FriendModule> localFriends = new ArrayList<FriendModule>();
				List<ControlPart> controlParts = null;
				if (aTempIsControlPossible) {
					controlParts = new ArrayList<ControlPart>();
				}

				TTCN3Module module = (TTCN3Module) parentScope;
				parser.setModule((TTCN3Module) parentScope);
				parser.pr_reparse_ModuleDefinitionsList(null, allDefinitions, localDefinitions, localGroups, allImports,
						localImports, allFriends, localFriends, controlParts);

				if ( parser.isErrorListEmpty() ) {
					addDefinitions(allDefinitions);
					if (doubleDefinitions != null) {
						doubleDefinitions.clear();
					}
					lastUniquenessCheckTimeStamp = null;

					for (ImportModule impmod : allImports) {
						module.addImportedModule(impmod);
					}

					for (Group group : localGroups) {
						addGroup(group);
					}

					for (FriendModule friend : allFriends) {
						module.addFriendModule(friend);
					}
					if (controlParts != null && controlParts.size() == 1) {
						((TTCN3Module) parentScope).addControlpart(controlParts.get(0));
					}
				}
			}
		});
	}

	/**
	 * Destroy every element trapped inside the damage radius.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param importedModules
	 *                the list of module importations found in the same
	 *                module.
	 * @param friendModules
	 *                the list of friend module declaration found in the
	 *                same module.
	 * */
	private void removeStuffInRange(final TTCN3ReparseUpdater reparser, final List<ImportModule> importedModules,
			final List<FriendModule> friendModules) {
		for (int i = groups.size() - 1; i >= 0; i--) {
			Group temp = groups.get(i);
			if (reparser.isDamaged(temp.getLocation())) {
				reparser.extendDamagedRegion(temp.getLocation());
				groups.remove(i);
			}
		}

		for (int i = importedModules.size() - 1; i >= 0; i--) {
			ImportModule temp = importedModules.get(i);
			if (reparser.isDamaged(temp.getLocation())) {
				reparser.extendDamagedRegion(temp.getLocation());
				importedModules.remove(i);
			}
		}

		for (int i = friendModules.size() - 1; i >= 0; i--) {
			FriendModule temp = friendModules.get(i);
			if (reparser.isDamaged(temp.getLocation())) {
				reparser.extendDamagedRegion(temp.getLocation());
				friendModules.remove(i);
			}
		}

		for (Iterator<Definition> iterator = definitions.iterator(); iterator.hasNext();) {
			Definition temp = iterator.next();
			if (reparser.isDamaged(temp.getCumulativeDefinitionLocation())) {
				reparser.extendDamagedRegion(temp.getCumulativeDefinitionLocation());
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
		final List<Definition> tempList = new ArrayList<Definition>(definitions);
		for (Definition definition : tempList) {
			definition.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	public boolean accept(final ASTVisitor v) {
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
		for (Group g : groups) {
			if (!g.accept(v)) {
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
