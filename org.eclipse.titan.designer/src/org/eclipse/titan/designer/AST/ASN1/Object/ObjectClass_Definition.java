/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ObjectClass;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent ObjectClassDefinition.
 * 
 * @author Kristof Szabados
 */
public abstract class ObjectClass_Definition extends ObjectClass {
	private static final String MISSINGSETTING = "Missing setting for `{0}''";

	protected FieldSpecifications fieldSpecifications;
	protected ObjectClassSyntax_root ocsRoot;

	@Override
	public void checkThisObject(final CompilationTimeStamp timestamp, final ASN1Object object) {
		if (null == object || isErroneous) {
			return;
		}

		if (null == fieldSpecifications) {
			// TODO at times like this we should analyze instead
			return;
		}

		final Object_Definition objectDefinition = object.getRefdLast(timestamp, null);
		FieldSpecification fieldSpecification;
		for (int i = 0; i < fieldSpecifications.getNofFieldSpecifications(); i++) {
			fieldSpecification = fieldSpecifications.getFieldSpecificationByIndex(i).getLast();
			if (objectDefinition.hasFieldSettingWithName(fieldSpecification.getIdentifier())) {
				final FieldSetting fieldSetting = objectDefinition.getFieldSettingByName(fieldSpecification.getIdentifier());
				if (null != fieldSetting) {
					fieldSetting.check(timestamp, fieldSpecification);
				}
			} else {
				if (!fieldSpecification.getIsOptional() && !fieldSpecification.hasDefault()
						&& !objectDefinition.getIsErroneous(timestamp)) {
					objectDefinition.getLocation().reportSemanticError(
							MessageFormat.format(MISSINGSETTING, fieldSpecification.identifier.getDisplayName()));
					objectDefinition.setIsErroneous(true);
				}
			}
		}
	}
	
	abstract protected void parseBlockFieldSpecs();

	@Override
	public ObjectClassSyntax_root getObjectClassSyntax(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return ocsRoot;
	}

	@Override
	public FieldSpecifications getFieldSpecifications() {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return fieldSpecifications;
	}

	@Override
	public ObjectClass_Definition getRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		check(timestamp);

		return this;
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subreferences = propCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on
				final FieldSpecification fieldSpecification = fieldSpecifications.getFieldSpecificationByIdentifier(subreference
						.getId());
				if (null == fieldSpecification) {
					return;
				}

				fieldSpecification.addProposal(propCollector, i + 1);
			} else {
				// final part of the reference
				final List<FieldSpecification> fieldSpecs = fieldSpecifications.getFieldSpecificationsWithPrefix(subreference.getId()
						.getName());
				for (FieldSpecification field : fieldSpecs) {
					propCollector.addProposal(field.getIdentifier(), " - " + "ObjectClass field", null, "ObjectClass field");
				}
			}
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on
				final Identifier identifier = subreference.getId();
				final FieldSpecification fieldSpecification = fieldSpecifications.getFieldSpecificationByIdentifier(identifier);
				if (null == fieldSpecification) {
					return;
				}

				fieldSpecification.addDeclaration(declarationCollector, i + 1);
			} else {
				// final part of the reference
				final String name = subreference.getId().getName();
				final List<FieldSpecification> fieldSpecs = fieldSpecifications.getFieldSpecificationsWithPrefix(name);
				for (FieldSpecification field : fieldSpecs) {
					declarationCollector.addDeclaration(field.getIdentifier().getDisplayName(), field.getLocation(), this);
				}
			}
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (fieldSpecifications != null && !fieldSpecifications.accept(v)) {
			return false;
		}
		// TODO if (ocs_root!=null && !ocs_root.accept(visitor)) return
		// false;
		return true;
	}
}
