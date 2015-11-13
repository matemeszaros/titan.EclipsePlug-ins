/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent FieldSpecs.
 * 
 * @author Kristof Szabados
 */
public final class FieldSpecifications extends ASTNode {
	public static final String MISSINGNAMEDFIELDSPECIFICATION = "No field specification with name `{0}''";
	public static final String MISSINGINDEXEDFIELDSPECIFICATION = "No field specification at index `{0}''";
	public static final String DUPLICATEDFIELDSPECIFICATIONFIRST = "Duplicate field specification with name `{0}'' was first declared here";
	public static final String DUPLICATEDFIELDSPECIFICATIONREPEATED = "Duplicate field specification with name `{0}'' was declared here again";

	private Map<String, FieldSpecification> fieldSpecificationsMap;
	private final List<FieldSpecification> fieldSpecifications;
	private Erroneous_FieldSpecification fsError;
	private ObjectClass_Definition myObjectClass;

	/** the time when these field specifications were checked the last time. */
	private CompilationTimeStamp lastTimeChecked;

	public FieldSpecifications() {
		fieldSpecifications = new ArrayList<FieldSpecification>();
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (FieldSpecification fieldSpecification : fieldSpecifications) {
			if (fieldSpecification == child) {
				return builder.append(INamedNode.DOT).append(fieldSpecification.getIdentifier().getDisplayName());
			}
		}

		return builder;
	}

	public void setMyObjectClass(final ObjectClass_Definition objectClassDefinition) {
		myObjectClass = objectClassDefinition;
		for (FieldSpecification fieldSpecification : fieldSpecifications) {
			fieldSpecification.setMyObjectClass(objectClassDefinition);
		}
	}

	public void addFieldSpecification(final FieldSpecification fieldSpecification) {
		if (null != fieldSpecification && null != fieldSpecification.getIdentifier()
				&& null != fieldSpecification.getIdentifier().getLocation()) {

			fieldSpecifications.add(fieldSpecification);
			fieldSpecification.setFullNameParent(this);

			if (null != myObjectClass) {
				fieldSpecification.setMyObjectClass(myObjectClass);
			}

		}
	}

	public boolean hasFieldSpecificationWithId(final Identifier identifier) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return fieldSpecificationsMap.containsKey(identifier.getName());
	}

	public FieldSpecification getFieldSpecificationByIdentifier(final Identifier identifier) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (fieldSpecificationsMap.containsKey(identifier.getName())) {
			return fieldSpecificationsMap.get(identifier.getName());
		}

		myObjectClass.getLocation().reportSemanticError(MessageFormat.format(MISSINGNAMEDFIELDSPECIFICATION, identifier.getDisplayName()));
		return getFieldSpecificationError();
	}

	public FieldSpecification getFieldSpecificationByIndex(final int index) {
		if (index < fieldSpecifications.size()) {
			return fieldSpecifications.get(index);
		}
		myObjectClass.getLocation().reportSemanticError(MessageFormat.format(MISSINGINDEXEDFIELDSPECIFICATION, Integer.valueOf(index)));
		return null;
	}

	public FieldSpecification getFieldSpecificationError() {
		if (null == fsError) {
			fsError = new Erroneous_FieldSpecification(new Identifier(Identifier_type.ID_ASN, "<error>"), true, false);
		}
		return fsError;
	}

	public int getNofFieldSpecifications() {
		return fieldSpecifications.size();
	}

	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		if (null == fieldSpecificationsMap) {
			fieldSpecificationsMap = new HashMap<String, FieldSpecification>(fieldSpecifications.size());
		}

		lastTimeChecked = timestamp;

		fieldSpecificationsMap.clear();

		String name;
		String displayName;
		for (FieldSpecification fieldSpecification : fieldSpecifications) {
			name = fieldSpecification.getIdentifier().getName();
			if (fieldSpecificationsMap.containsKey(name)) {
				displayName = fieldSpecification.getIdentifier().getDisplayName();
				fieldSpecificationsMap.get(name).getIdentifier().getLocation()
						.reportSingularSemanticError(MessageFormat.format(DUPLICATEDFIELDSPECIFICATIONFIRST, displayName));
				fieldSpecification.getIdentifier().getLocation()
						.reportSemanticError(MessageFormat.format(DUPLICATEDFIELDSPECIFICATIONREPEATED, displayName));
			} else {
				fieldSpecificationsMap.put(name, fieldSpecification);
			}
		}

		for (FieldSpecification fieldSpecification : fieldSpecifications) {
			fieldSpecification.check(timestamp);
		}
	}

	public List<FieldSpecification> getFieldSpecificationsWithPrefix(final String prefix) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		final List<FieldSpecification> results = new ArrayList<FieldSpecification>();
		for (FieldSpecification fieldSpec : fieldSpecifications) {
			if (fieldSpec.getIdentifier().getName().startsWith(prefix)) {
				results.add(fieldSpec);
			}
		}

		return results;
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (fieldSpecifications != null) {
			for (FieldSpecification fs : fieldSpecifications) {
				if (!fs.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
