package org.eclipse.titan.designer.AST;

import java.text.MessageFormat;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Special reference type used by map, unmap, connect, disconnect statements.
 * 
 * This reference is never part of the scope hierarchy,
 * but only references a port within a component type.
 * 
 * */
public class PortReference extends Reference {
	private static final String NOPORTWITHNAME = "Component type `{0}'' does not have a port with name `{1}''";

	private Component_Type componentType;

	public PortReference(final Reference reference) {
		super(null, reference.getSubreferences());
	}
	
	public void setComponent(final Component_Type componentType) {
		this.componentType = componentType;
	}

	/** @return a new instance of this reference */
	public Reference newInstance() {
		ErrorReporter.INTERNAL_ERROR("Port referencies should not be cloned");
		return null;
	}

	@Override
	public Assignment getRefdAssignment(CompilationTimeStamp timestamp, boolean checkParameterList) {
		if(myScope == null || componentType == null) {
			return null;
		}
		
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return referredAssignment;
		}
		
		Identifier portIdentifier = getId();
		ComponentTypeBody componentBody = componentType.getComponentBody();
		if (!componentBody.hasLocalAssignmentWithId(portIdentifier)) {
			getLocation().reportSemanticError(
					MessageFormat.format(NOPORTWITHNAME, componentType.getTypename(), portIdentifier.getDisplayName()));

			referredAssignment = null;
			lastTimeChecked = timestamp;
			return null;
		}
		
		referredAssignment = componentBody.getLocalAssignmentById(portIdentifier);
		
		if (referredAssignment != null) {
			referredAssignment.check(timestamp);
			referredAssignment.setUsed();

			if (referredAssignment instanceof Definition) {
				String referingModuleName = getMyScope().getModuleScope().getName();
				if (!((Definition) referredAssignment).referingHere.contains(referingModuleName)) {
					((Definition) referredAssignment).referingHere.add(referingModuleName);
				}
			}
		}
		
		lastTimeChecked = timestamp;

		return referredAssignment;
	}
	
	
}
