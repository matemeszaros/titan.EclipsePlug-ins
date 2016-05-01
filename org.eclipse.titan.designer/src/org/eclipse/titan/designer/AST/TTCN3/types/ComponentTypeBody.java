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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.BridgingNamedNode;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceChainElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.TTCN3Scope;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute.ExtensionAttribute_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionsAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.VisibilityModifier;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.SkeletonTemplateProposal;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Keywords;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeAnalyzer;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the body of a component type.
 * 
 * @author Kristof Szabados
 * */
public final class ComponentTypeBody extends TTCN3Scope implements IReferenceChainElement, ILocateableNode, IIncrementallyUpdateable {
	private static final String FULLNAMEPART = "<extends>";

	public static final String CIRCULAREXTENSIONCHAIN = "Circular extension chain is not allowed: {0}";
	public static final String INHERITEDDEFINITIONLOCATION = "Definition `{0}'' inherited from component type `{1}'' is here";
	public static final String INHERITANCECOLLISSION =
			"Definition `{0}'' inherited from component type `{1}'' collides with definition inherited from `{2}''";
	public static final String LOCALINHERTANCECOLLISSION = "Local Definiton `{0}'' collides with definition inherited from component type `{1}''";
	public static final String INHERITEDLOCATION = "Inherited definition of `{0}'' is here";

	public static final String HIDINGSCOPEELEMENT = "The name of the inherited definition `{0}'' is not unique in the scope hierarchy";
	public static final String HIDDENSCOPEELEMENT = "Previous definition with identifier `{0}'' in higher scope unit is here";
	public static final String HIDINGMODULEIDENTIFIER = "Inherited definition with name `{0}'' hides a module identifier";
	
	public static final String MEMBERNOTVISIBLE = "The member definition `{0}'' in component type `{1}'' is not visible in this scope";

	private Location location;

	private Location commentLocation = null;

	/** the identifier of the component does not belong to the componentTypeBody naturally !*/
	private final Identifier identifier;
	/** component references from the extends part or null if none */
	private final ComponentTypeReferenceList extendsReferences;
	/** component references from the extend attributes null if none */
	private ComponentTypeReferenceList attrExtendsReferences = new ComponentTypeReferenceList();

	private Set<ComponentTypeBody> compatibleBodies = new HashSet<ComponentTypeBody>();

	/** The component's own definitions */
	private DefinitionContainer definitions = new DefinitionContainer();
	/**
	 * The list of definitions gained through extends references.
	 * Used only to speed up some operations.
	 * */
	private Map<String, Definition> extendsGainedDefinitions = new HashMap<String, Definition>();
	/**
	 * The list of definitions gained through extends attribute.
	 * Used only to speed up some operations.
	 * */
	private Map<String, Definition> attributeGainedDefinitions = new HashMap<String, Definition>();

	/** the with attributes of the definition does not belong to the componentTypeBody naturally !*/
	private WithAttributesPath withAttributesPath;

	// the component type to which this body belongs.
	private Component_Type myType;

	/**
	 * Holds the last time when these definitions were checked, or null if never.
	 */
	private CompilationTimeStamp lastCompilationTimeStamp;
	private CompilationTimeStamp lastUniquenessCheck;

	public ComponentTypeBody(final Identifier identifier, final ComponentTypeReferenceList extendsReferences) {
		this.identifier = identifier;
		this.extendsReferences = extendsReferences;

		if (extendsReferences != null) {
			extendsReferences.setFullNameParent(this);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (definitions != null) {
			Identifier tempIdentifier;
			for (Definition definition : definitions) {
				if (definition == child) {
					tempIdentifier = definition.getIdentifier();
					return builder.append(INamedNode.DOT).append(tempIdentifier.getDisplayName());
				}
			}
		} else if (extendsReferences != child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public Component_Type getMyType() {
		return myType;
	}

	public void setMyType(final Component_Type type) {
		myType = type;
	}

	public void setMyScope(final Scope scope) {
		setParentScope(scope);
		if (location != null) {
			scope.addSubScope(location, this);
		}

		if (extendsReferences != null) {
			extendsReferences.setMyScope(scope);
		}
	}

	/**
	 * Sets the parent path for the with attribute path element of this
	 * component type. Also, creates the with attribute path node if it did not
	 * exist before.
	 *
	 *@param parent the parent to be set.
	 * */
	public void setAttributeParentPath(final WithAttributesPath parent) {
		withAttributesPath = parent;
		for (Definition def : definitions) {
			def.setAttributeParentPath(parent);
		}
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
	 * @return The location of the comment assigned to this definition.
	 *  Or null if none.
	 * */
	@Override
	public Location getCommentLocation() {
		return commentLocation;
	}

	/**
	 * Sets the location of the comment that belongs to this definition.
	 *
	 * @param commentLocation the location of the comment
	 * */
	public void setCommentLocation(final Location commentLocation) {
		this.commentLocation = commentLocation;
	}


	public Map<String, Definition> getDefinitionMap() {
		return definitions.getDefinitionMap();
	}

	/**
	 * @return the list of the extensions.
	 * */
	public ComponentTypeReferenceList getExtensions() {
		return extendsReferences;
	}

	/**
	 * @return The list of attribute extension or null if none given.
	 * */
	public ComponentTypeReferenceList getAttributeExtensions() {
		return attrExtendsReferences;
	}

	public List<Definition> getDefinitions() {
		return definitions.getDefinitions();
	}

	/**
	 * Detects if an assignment with the provided identifier exists inside this
	 * component.
	 *
	 * @param identifier the identifier to be use to search for the assignment.
	 * @return true if an assignment with the same identifier exists, or false
	 *         otherwise.
	 * */
	public boolean hasLocalAssignmentWithId(final Identifier identifier) {
		if (lastUniquenessCheck == null) {
			checkUniqueness(CompilationTimeStamp.getBaseTimestamp());
		}

		Definition definition = definitions.getDefinition(identifier.getName());
		if (definition != null) {
			return true;
		}

		definition = extendsGainedDefinitions.get(identifier.getName());
		if (definition != null) {
			return true;
		}

		if (attributeGainedDefinitions.containsKey(identifier.getName())) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the assignment of this component fitting the provided identifier.
	 *
	 * @param identifier the identifier of the assignment
	 * @return the assignment identified, or null in case of an error.
	 * */
	public Definition getLocalAssignmentById(final Identifier identifier) {
		if (lastUniquenessCheck == null) {
			checkUniqueness(CompilationTimeStamp.getBaseTimestamp());
		}

		Definition definition = definitions.getDefinition(identifier.getName());
		if (definition != null) {
			return definition;
		}

		definition = extendsGainedDefinitions.get(identifier.getName());
		if (definition != null) {
			if (VisibilityModifier.Public.equals(definition.getVisibilityModifier())) {
				return definition;
			} else {
				identifier.getLocation().reportSemanticError(MessageFormat.format(
						MEMBERNOTVISIBLE, identifier.getDisplayName(), this.identifier.getDisplayName()));
			}
		}

		definition = attributeGainedDefinitions.get(identifier.getName());
		if (definition != null) {
			if (VisibilityModifier.Public.equals(definition.getVisibilityModifier())) {
				return definition;
			} else {
				identifier.getLocation().reportSemanticError(MessageFormat.format(
						MEMBERNOTVISIBLE, identifier.getDisplayName(), this.identifier.getDisplayName()));
			}
		}
		
		return null;
	}

	@Override
	public boolean hasAssignmentWithId(final CompilationTimeStamp timestamp, final Identifier identifier) {
		// only the visible ones
		Definition definition = definitions.getDefinition(identifier.getName());
		if (definition != null) {
			return true;
		}

		definition = extendsGainedDefinitions.get(identifier.getName());
		if (definition != null) {
			if (VisibilityModifier.Public.equals(definition.getVisibilityModifier())) {
				return true;
			}
		}

		definition = attributeGainedDefinitions.get(identifier.getName());
		if (definition != null) {
			if (VisibilityModifier.Public.equals(definition.getVisibilityModifier())) {
				return true;
			}
		}

		return super.hasAssignmentWithId(timestamp, identifier);
	}

	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference) {
		return getAssBySRef(timestamp, reference, null);
	}
	
	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference, IReferenceChain refChain) {
		if (reference.getModuleIdentifier() != null) {
			return getParentScope().getAssBySRef(timestamp, reference);
		}

		if (lastUniquenessCheck == null) {
			checkUniqueness(timestamp);
		}

		Definition definition = definitions.getDefinition(reference.getId().getName());
		if (definition != null) {
			return definition;
		}

		definition = extendsGainedDefinitions.get(reference.getId().getName());
		if (definition != null) {
			if (VisibilityModifier.Public.equals(definition.getVisibilityModifier())) {
				return definition;
			}

			reference.getLocation().reportSemanticError(MessageFormat.format(
					MEMBERNOTVISIBLE, reference.getId().getDisplayName(), identifier.getDisplayName()));
			return null;
		}

		definition = attributeGainedDefinitions.get(reference.getId().getName());
		if (definition != null) {
			if (VisibilityModifier.Public.equals(definition.getVisibilityModifier())) {
				return definition;
			}

			reference.getLocation().reportSemanticError(MessageFormat.format(
					MEMBERNOTVISIBLE, reference.getId().getDisplayName(), identifier.getDisplayName()));
			return null;
		}

		return getParentScope().getAssBySRef(timestamp, reference);
	}

	/**
	 * Collect all component type bodies that can be reached, recursively, via extends.
	 * 
	 * @return the collected component type bodies.
	 * */
	private List<ComponentTypeBody> getExtendsInheritedComponentBodies() {
		List<ComponentTypeBody> result = new ArrayList<ComponentTypeBody>();
		LinkedList<ComponentTypeBody> toBeChecked = new LinkedList<ComponentTypeBody>(extendsReferences.getComponentBodies());
		while(!toBeChecked.isEmpty()) {
			ComponentTypeBody body = toBeChecked.removeFirst();
			if(!result.contains(body)) {
				result.add(body);
				for(ComponentTypeBody subBody : body.extendsReferences.getComponentBodies()) {
					if(!result.contains(subBody) && !toBeChecked.contains(subBody)) {
						toBeChecked.add(subBody);
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Collect all component type bodies that can be reached, recursively, via extends attributes.
	 * 
	 * @return the collected component type bodies.
	 * */
	private List<ComponentTypeBody> getAttributeExtendsInheritedComponentBodies() {
		List<ComponentTypeBody> result = new ArrayList<ComponentTypeBody>();
		LinkedList<ComponentTypeBody> toBeChecked = new LinkedList<ComponentTypeBody>(attrExtendsReferences.getComponentBodies());
		while(!toBeChecked.isEmpty()) {
			ComponentTypeBody body = toBeChecked.removeFirst();
			if(!result.contains(body)) {
				result.add(body);
				for(ComponentTypeBody subBody : body.attrExtendsReferences.getComponentBodies()) {
					if(!result.contains(subBody) && !toBeChecked.contains(subBody)) {
						toBeChecked.add(subBody);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Traverse the attribute extension hierarchy and check all components in it,
	 *  to see if there is a definition with the provided name.
	 *
	 * Please note, that semantic checking is not done here.
	 * That should be performed before, but not required.
	 *
	 * @param id the name to search for.
	 * @return the definition reachable in the extension hierarchy or null if non found.
	 * */
	private Definition getAttributesInheritedDefinition(final Identifier id) {
		if (attrExtendsReferences == null) {
			return null;
		}

		final String temporalName = id.getName();

		final List<ComponentTypeBody> bodies = attrExtendsReferences.getComponentBodies();
		if(bodies == null) {
			return null;
		}

		for (ComponentTypeBody body : bodies) {
			Map<String, Definition> subDefinitionMap = body.getDefinitionMap();
			if (subDefinitionMap.containsKey(temporalName)) {
				return subDefinitionMap.get(temporalName);
			}

			Definition temp = body.getAttributesInheritedDefinition(id);
			if (temp != null) {
				return temp;
			}
		}

		return null;
	}

	/**
	 * Adds assignments (right now only definitions) the this component type
	 * body.
	 *
	 * @param assignments the assignments to be added.
	 * */
	public void addAssignments(final List<Definition> assignments) {
		for (Definition def : assignments) {
			if (def != null && def.getIdentifier() != null && def.getIdentifier().getLocation() != null) {
				definitions.add(def);
				def.setFullNameParent(this);
				def.setMyScope(this);
			}
		}
	}

	@Override
	public String chainedDescription() {
		return getFullName();
	}

	@Override
	public Location getChainLocation() {
		if (identifier != null && identifier.getLocation() != null) {
			return identifier.getLocation();
		}

		return null;
	}

	@Override
	public String toString() {
		return getFullName();
	}

	/**
	 * Checks if the references of expanded components contain any recursive
	 * references, which is not allowed.
	 *
	 * @param refChain the reference chain used to store the visited components.
	 * */
	protected void checkRecursion(final IReferenceChain refChain) {
		if (refChain.add(this)) {
			return;
		}

		if (extendsReferences != null && refChain.add(this)) {
			extendsReferences.checkRecursion(refChain);
		}
	}

	/**
	 * Collects the extends extension attributes, from the with attributes assigned to the component type.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle
	 * */
	private void collectExtensionAttributes(final CompilationTimeStamp timestamp) {
		if (withAttributesPath == null) {
			return;
		}

		List<SingleWithAttribute> realAttributes = withAttributesPath.getRealAttributes(timestamp);

		SingleWithAttribute attribute;
		List<AttributeSpecification> specifications = null;
		for (int i = 0; i < realAttributes.size(); i++) {
			attribute = realAttributes.get(i);
			if (Attribute_Type.Extension_Attribute.equals(attribute.getAttributeType())) {
				Qualifiers qualifiers = attribute.getQualifiers();
				if (qualifiers == null || qualifiers.getNofQualifiers() == 0) {
					if (specifications == null) {
						specifications = new ArrayList<AttributeSpecification>();
					}
					specifications.add(attribute.getAttributeSpecification());
				}
			}
		}

		if (specifications == null) {
			return;
		}

		List<ExtensionAttribute> attributes = new ArrayList<ExtensionAttribute>();
		AttributeSpecification specification;
		for (int i = 0; i < specifications.size(); i++) {
			specification = specifications.get(i);
			ExtensionAttributeAnalyzer analyzer = new ExtensionAttributeAnalyzer();
			analyzer.parse(specification);
			List<ExtensionAttribute> temp = analyzer.getAttributes();
			if (temp != null) {
				attributes.addAll(temp);
			}
		}

		if (attributes.isEmpty()) {
			return;
		}

		attrExtendsReferences = new ComponentTypeReferenceList();
		for (int i = 0, size = attributes.size(); i < size; i++) {
			ExtensionAttribute tempAttribute = attributes.get(i);
			if (ExtensionAttribute_type.EXTENDS.equals(tempAttribute.getAttributeType())) {
				ExtensionsAttribute extensionsAttribute = (ExtensionsAttribute) tempAttribute;
				for (int j = 0, size2 = extensionsAttribute.getNofTypes(); j < size2; j++) {
					IType tempType = extensionsAttribute.getType(j);
					if (Type_type.TYPE_REFERENCED.equals(tempType.getTypetype())) {
						attrExtendsReferences.addReference(((Referenced_Type) tempType).getReference());
					}
				}
			}
		}
		attrExtendsReferences.setFullNameParent(new BridgingNamedNode(this, ".<extends attribute>"));
		attrExtendsReferences.setMyScope(parentScope);
	}

	/**
	 * Initializes the list of component type bodies with which the actual one is compatible.
	 * It can be done much faster like this, than doing the checks every time is_compatible is called.
	 *
	 * @param references the component type body references referred by the actual component.
	 * */
	private void initCompatibility(final ComponentTypeReferenceList references) {
		List<ComponentTypeBody> componentBodies = references.getComponentBodies();
		for (int i = 0, size = componentBodies.size(); i < size; i++) {
			ComponentTypeBody componentBody = componentBodies.get(i);
			if (!compatibleBodies.contains(componentBody)) {
				compatibleBodies.add(componentBody);
			}
			// compatible with all components which are compatible with the compatible component
			for (Iterator<ComponentTypeBody> iterator = componentBody.compatibleBodies.iterator(); iterator.hasNext();) {
				ComponentTypeBody tempComponentBody = iterator.next();
				if (!compatibleBodies.contains(tempComponentBody)) {
					compatibleBodies.add(tempComponentBody);
				}
			}
		}
	}

	/**
	 * Checks the uniqueness of the definitions, and also builds a hashmap of
	 * them to speed up further searches.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle
	 * */
	private void checkUniqueness(final CompilationTimeStamp timestamp) {
		if (lastUniquenessCheck != null && !lastUniquenessCheck.isLess(timestamp)) {
			return;
		}

		lastUniquenessCheck = timestamp;

		compatibleBodies.clear();
		definitions.checkUniqueness();

		addDefinitionsOfExtendsParents(timestamp);
		addDefinitionsOfExtendAttributeParents(timestamp);
	}

	private void addDefinitionsOfExtendAttributeParents(final CompilationTimeStamp timestamp) {
		attributeGainedDefinitions.clear();
		if (attrExtendsReferences != null) {
			attrExtendsReferences.check(timestamp);

			IReferenceChain referenceChain = ReferenceChain.getInstance(CIRCULAREXTENSIONCHAIN, true);
			if (referenceChain.add(this)) {
				attrExtendsReferences.checkRecursion(referenceChain);
			}
			referenceChain.release();

			initCompatibility(attrExtendsReferences);

			collectDefinitionsFromAttributeExtends();
		}
	}

	private void addDefinitionsOfExtendsParents(final CompilationTimeStamp timestamp) {
		extendsGainedDefinitions.clear();
		if (extendsReferences == null) {
			return;
		}

		extendsReferences.check(timestamp);

		IReferenceChain referenceChain = ReferenceChain.getInstance(CIRCULAREXTENSIONCHAIN, true);
		if (referenceChain.add(this)) {
			extendsReferences.checkRecursion(referenceChain);
		}
		referenceChain.release();

		initCompatibility(extendsReferences);

		// collect definitions
		List<ComponentTypeBody> bodies = getExtendsInheritedComponentBodies();
		for (ComponentTypeBody body : bodies) {
			Map<String, Definition> subDefinitionMap = body.getDefinitionMap();
			for (Definition definition : subDefinitionMap.values()) {
				String name = definition.getIdentifier().getName();
				if (definitions.hasDefinition(name)) {
					Definition localDefinition = definitions.getDefinition(name);
					localDefinition.getIdentifier().getLocation().reportSemanticError(MessageFormat.format(
							LOCALINHERTANCECOLLISSION, definition.getIdentifier().getDisplayName(), definition.getMyScope().getFullName()));
				} else if (extendsGainedDefinitions.containsKey(name)) {
					Definition previousDefinition = extendsGainedDefinitions.get(name);
					if (!previousDefinition.equals(definition)) {
						// it is not the same definition inherited on two paths
						if (this.equals(previousDefinition.getMyScope())) {
							previousDefinition.getLocation().reportSemanticError(MessageFormat.format(LOCALINHERTANCECOLLISSION,
									previousDefinition.getIdentifier().getDisplayName(), definition.getMyScope().getFullName()));
							definition.getIdentifier().getLocation().reportSemanticWarning(
									MessageFormat.format(INHERITEDLOCATION, definition.getIdentifier().getDisplayName()));
						} else if (identifier != null && identifier.getLocation() != null) {
							identifier.getLocation().reportSemanticError(MessageFormat.format(INHERITANCECOLLISSION,
									definition.getIdentifier().getDisplayName(), definition.getMyScope().getFullName(), previousDefinition.getMyScope().getFullName()));

							definition.getIdentifier().getLocation().reportSingularSemanticWarning(MessageFormat.format(INHERITEDDEFINITIONLOCATION,
									definition.getIdentifier().getDisplayName(), definition.getMyScope().getFullName()));
							previousDefinition.getIdentifier().getLocation().reportSingularSemanticWarning(MessageFormat.format(INHERITEDDEFINITIONLOCATION,
									previousDefinition.getIdentifier().getDisplayName(), previousDefinition.getMyScope().getFullName()));
						}
					}
				} else {
					extendsGainedDefinitions.put(name, definition);

					if (!definition.getMyScope().getModuleScope().equals(parentScope.getModuleScope())) {
						if (parentScope.hasAssignmentWithId(timestamp, definition.getIdentifier())) {
							if (identifier != null && identifier.getLocation() != null) {
								identifier.getLocation().reportSemanticError(
										MessageFormat.format(HIDINGSCOPEELEMENT, definition.getIdentifier().getDisplayName()));
								List<ISubReference> subReferences = new ArrayList<ISubReference>();
								subReferences.add(new FieldSubReference(definition.getIdentifier()));
								Reference reference = new Reference(null, subReferences);
								Assignment assignment = parentScope.getAssBySRef(timestamp, reference);
								if (assignment != null && assignment.getLocation() != null) {
									assignment.getLocation().reportSingularSemanticError(
											MessageFormat.format(HIDDENSCOPEELEMENT, definition.getIdentifier().getDisplayName()));
								}
								definition.getIdentifier().getLocation().reportSingularSemanticWarning(
										MessageFormat.format(INHERITEDDEFINITIONLOCATION, definition.getIdentifier().getDisplayName(),
												definition.getMyScope().getFullName()));
							}
						} else if (parentScope.isValidModuleId(definition.getIdentifier())) {
							definition.getLocation().reportSingularSemanticWarning(
									MessageFormat.format(HIDINGMODULEIDENTIFIER, definition.getIdentifier().getDisplayName()));
						}
					}
				}
			}
		}
	}

	/**
	 * Collects the definitions coming from extends attributes.
	 * In reality no collection is done, but it is checked that the actual component has all definitions from the extended component.
	 * */
	private void collectDefinitionsFromAttributeExtends() {
		List<ComponentTypeBody> parents = getAttributeExtendsInheritedComponentBodies();
		for (ComponentTypeBody parent : parents) {
			for (Definition definition : parent.getDefinitions()) {
				Identifier id = definition.getIdentifier();
				String name = id.getName();

				// Check if we have inherited 2 different definitions with the same name
				if (extendsGainedDefinitions.containsKey(name)) {
					Definition myDefinition = extendsGainedDefinitions.get(name);
					if (definition != myDefinition) {
						location.reportSemanticError(MessageFormat.format(
								INHERITANCECOLLISSION,
								id.getDisplayName(),
								definition.getMyScope().getFullName(),
								myDefinition.getMyScope().getFullName()));
					}
					continue;
				}

				if (!definitions.hasDefinition(name)) {
					location.reportSemanticError(MessageFormat.format(
							"Missing local definition of `{0}'', which was inherited from component type `{1}''",
							id.getDisplayName(), definition.getMyScope().getFullName()));
					continue;
				}

				attributeGainedDefinitions.put(name, definition);
			}
		}
	}

	/**
	 * Checks that the definitions inherited via extends attributes
	 *  are identical to ones contained in the component, or inherited via extends clause.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle
	 * */
	private void checkExtensionAttributes(final CompilationTimeStamp timestamp) {
		if (attributeGainedDefinitions == null || attributeGainedDefinitions.isEmpty()) {
			return;
		}

		for (Definition originalDefinition : definitions) {
			Definition inheritedDefinition = attributeGainedDefinitions.get(originalDefinition.getIdentifier().getName());
			if (inheritedDefinition != null) {
				originalDefinition.checkIdentical(timestamp, inheritedDefinition);
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

		lastCompilationTimeStamp = timestamp;

		attrExtendsReferences = null;
		collectExtensionAttributes(timestamp);
		checkUniqueness(timestamp);

		definitions.checkAll(timestamp);

		checkExtensionAttributes(timestamp);
	}

	/**
	 * Checks if the provided component is compatible with this one.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle
	 * @param other the other component to check against
	 *
	 * @return true if the other component is compatible with this, false
	 *         otherwise.
	 * */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final ComponentTypeBody other) {
		if (this == other) {
			return true;
		}

		check(timestamp);
		other.check(timestamp);

		if (definitions.isEmpty() && extendsReferences == null && attrExtendsReferences == null) {
			// empty component
			return true;
		}

		return other.compatibleBodies.contains(this);
	}

	@Override
	public void addProposal(final ProposalCollector propCollector) {
		if (propCollector.getReference().getModuleIdentifier() == null) {
			addProposal(propCollector, 0);
		}
	}

	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (i > 0) {
			return;
		}

		for (Definition definition : definitions) {
			definition.addProposal(propCollector, i);
		}

		for (Definition definition : extendsGainedDefinitions.values()) {
			if (VisibilityModifier.Public.equals(definition.getVisibilityModifier())) {
				definition.addProposal(propCollector, i);
			}
		}
	}

	@Override
	public void addSkeletonProposal(final ProposalCollector propCollector) {
		for (SkeletonTemplateProposal templateProposal : TTCN3CodeSkeletons.COMPONENT_INTERNAL_SKELETON_TEMPLATE_PROPOSALS) {
			propCollector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(), TTCN3CodeSkeletons.SKELETON_IMAGE);
		}
	}

	@Override
	public void addKeywordProposal(final ProposalCollector propCollector) {
		propCollector.addProposal(TTCN3Keywords.COMPONENT_SCOPE, null, TTCN3Keywords.KEYWORD);
		super.addKeywordProposal(propCollector);
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector) {
		if (declarationCollector.getReference().getModuleIdentifier() == null) {
			addDeclaration(declarationCollector, 0);
		}
	}

	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final Identifier identifier = declarationCollector.getReference().getId();
		Definition definition = definitions.getDefinition(identifier.getName());
		if (definition != null) {
			definition.addDeclaration(declarationCollector, i);
		}

		definition = extendsGainedDefinitions.get(identifier.getName());
		if (definition != null) {
			definition.addDeclaration(declarationCollector, i);
		}

		definition = attributeGainedDefinitions.get(identifier.getName());
		if (definition != null) {
			definition.addDeclaration(declarationCollector, i);
		}

		if (i == 0) {
			super.addDeclaration(declarationCollector);
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for (Definition definition : definitions) {
			definition.updateSyntax(reparser, false);
			reparser.updateLocation(definition.getLocation());
			if(!definition.getLocation().equals(definition.getCumulativeDefinitionLocation())) {
				reparser.updateLocation(definition.getCumulativeDefinitionLocation());
			}
		}

		if (extendsReferences != null) {
			extendsReferences.updateSyntax(reparser, false);
			reparser.updateLocation(extendsReferences.getLocation());
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
		if (definitions != null) {
			for (Definition def : definitions) {
				def.findReferences(referenceFinder, foundIdentifiers);
			}
		}
		if (extendsReferences != null) {
			extendsReferences.findReferences(referenceFinder, foundIdentifiers);
		}
		if (attrExtendsReferences != null) {
			attrExtendsReferences.findReferences(referenceFinder, foundIdentifiers);
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

		if (definitions != null) {
			for (Definition def : definitions) {
				if (!def.accept(v)) {
					return false;
				}
			}
		}
		if (extendsReferences != null) {
			if (!extendsReferences.accept(v)) {
				return false;
			}
		}
		if (attrExtendsReferences != null) {
			if (!attrExtendsReferences.accept(v)) {
				return false;
			}
		}
		if (v.leave(this)==ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
