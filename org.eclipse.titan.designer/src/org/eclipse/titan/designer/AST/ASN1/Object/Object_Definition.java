/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent ObjectDefinition.
 * 
 * @author Kristof Szabados
 */
public final class Object_Definition extends ASN1Object {
	private static final String DUPLICATEDFIELDSETTINGFIRST = "Duplicate field setting with name `{0}'' was first declared here";
	private static final String DUPLICATEDFIELDSETTINGREPEATED = "Duplicate field setting with name `{0}'' was declared here again";
	private static final String MISSINGFIELDSETTINGWITHNAME = "No field setting with identifier `{0}'' in object `{1}''";
	private static final String MISSINGSETTINGORDEFAULT = "No field setting or default with identifier `{0}'' in object `'{1}'";

	private final Block mBlock;
	private final ArrayList<FieldSetting> fieldSettings;
	private HashMap<String, FieldSetting> fieldSettingMap;
	@SuppressWarnings("unused")
	private final boolean isGenerated;

	public Object_Definition(final Block aBlock) {
		this.mBlock = aBlock;
		if (null != aBlock && aBlock.getTokenListSize() >= 0) {
			location = new Location(aBlock.getLocation());
		}
		isGenerated = false;
		fieldSettings = new ArrayList<FieldSetting>();
	}

	@Override
	public Object_Definition newInstance() {
		Object_Definition temp = null;
		temp = new Object_Definition(mBlock);
		for (int i = 0; i < fieldSettings.size(); i++) {
			temp.addFieldSetting(fieldSettings.get(i).newInstance());
		}

		return temp;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0; i < fieldSettings.size(); i++) {
			final FieldSetting fieldSetting = fieldSettings.get(i);
			if (fieldSetting == child) {
				return builder.append(INamedNode.DOT).append(fieldSetting.getName().getDisplayName());
			}
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		fieldSettings.trimToSize();
		for (int i = 0; i < fieldSettings.size(); i++) {
			fieldSettings.get(i).setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (null == fieldSettingMap) {
			fieldSettingMap = new HashMap<String, FieldSetting>(fieldSettings.size());
		}

		fieldSettingMap.clear();

		if (null != myGovernor) {
			myGovernor.check(timestamp);
		}

		parseBlock(timestamp);

		String name;
		for (int i = 0; i < fieldSettings.size(); i++) {
			final FieldSetting fieldSetting = fieldSettings.get(i);
			name = fieldSetting.getName().getName();
			if (fieldSettingMap.containsKey(name)) {
				final Location location = fieldSettingMap.get(name).getLocation();
				location.reportSingularSemanticError(MessageFormat.format(DUPLICATEDFIELDSETTINGFIRST, fieldSetting.getName()
						.getDisplayName()));
				fieldSetting.getLocation().reportSemanticError(
						MessageFormat.format(DUPLICATEDFIELDSETTINGREPEATED, fieldSetting.getName().getDisplayName()));

			} else {
				fieldSettingMap.put(name, fieldSetting);
			}
		}

		fieldSettings.trimToSize();

		if (null != myGovernor) {
			myGovernor.checkThisObject(timestamp, this);
		}
	}

	public void addFieldSetting(final FieldSetting fieldSetting) {
		if (null != fieldSetting && null != fieldSetting.getName() && null != fieldSetting.getLocation()) {
			fieldSettings.add(fieldSetting);
			fieldSetting.setMyScope(myScope);
			fieldSetting.setFullNameParent(this);
		}
	}

	public int getNofFieldSettings() {
		return fieldSettings.size();
	}

	public boolean hasFieldSettingWithName(final Identifier identifier) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return fieldSettingMap.containsKey(identifier.getName());
	}

	public FieldSetting getFieldSettingByName(final Identifier identifier) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		final String name = identifier.getName();
		if (fieldSettingMap.containsKey(name)) {
			return fieldSettingMap.get(name);
		}

		if (!isErroneous) {
			location.reportSemanticError(MessageFormat.format(MISSINGFIELDSETTINGWITHNAME, identifier.getDisplayName(), getFullName()));
		}

		return null;
	}

	/**
	 * Checks if a fieldsetting with the provided name exists.
	 * 
	 * @param identifier
	 *                the identifier holding the name to look for.
	 * 
	 * @return true if it exists, false otherwise.
	 * */
	public boolean hasFieldSettingWithNameDefault(final Identifier identifier) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (isErroneous) {
			return true;
		}

		if (fieldSettingMap.containsKey(identifier.getName())) {
			return true;
		}

		if (myGovernor.getFieldSpecifications().hasFieldSpecificationWithId(identifier)
				&& myGovernor.getFieldSpecifications().getFieldSpecificationByIdentifier(identifier).hasDefault()) {
			return true;
		}

		return false;
	}

	public ISetting getSettingByNameDefault(final Identifier identifier) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (fieldSettingMap.containsKey(identifier.getName())) {
			return fieldSettingMap.get(identifier.getName()).getSetting();
		}

		if (myGovernor.getFieldSpecifications().hasFieldSpecificationWithId(identifier)) {
			final FieldSpecification fs = myGovernor.getFieldSpecifications().getFieldSpecificationByIdentifier(identifier);
			if (fs.hasDefault()) {
				return fs.getDefault();
			}
		}

		if (!isErroneous) {
			location.reportSemanticError(MessageFormat.format(MISSINGSETTINGORDEFAULT, identifier.getDisplayName(), getFullName()));
		}

		return null;
	}

	@Override
	public Object_Definition getRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		return this;
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subreferences = propCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		final String subreferenceName = subreference.getId().getName();
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on
				final FieldSetting fieldSetting = fieldSettingMap.get(subreferenceName);
				if (null == fieldSetting) {
					return;
				}

				fieldSetting.addProposal(propCollector, i + 1);
			} else {
				// final part of the reference
				for (int j = 0; j < fieldSettings.size(); j++) {
					final FieldSetting fieldSetting = fieldSettings.get(i);
					if (fieldSetting.getName().getName().startsWith(subreferenceName)) {
						propCollector.addProposal(fieldSetting.getName(), "- Object field", null, "FieldSetting");
					}
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
		final String subreferenceName = subreference.getId().getName();
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on
				final FieldSetting fieldSetting = fieldSettingMap.get(subreferenceName);
				if (null == fieldSetting) {
					return;
				}

				fieldSetting.addDeclaration(declarationCollector, i + 1);
			} else {
				// final part of the reference
				String name;
				for (int j = 0; j < fieldSettings.size(); j++) {
					final FieldSetting fieldSetting = fieldSettings.get(i);
					name = fieldSetting.getName().getName();
					if (name.startsWith(subreferenceName)) {
						declarationCollector.addDeclaration(name, fieldSetting.getLocation(), this);
					}
				}
			}
		}
	}

	protected void parseBlock(final CompilationTimeStamp timestamp) {
		if (null == mBlock || null == myGovernor) {
			return;
		}		
		ObjectClassSyntax_Parser parser = null;
		parser = new ObjectClassSyntax_Parser(mBlock, this);
		final ObjectClassSyntax_root root = myGovernor.getObjectClassSyntax(timestamp);
		fieldSettings.clear();

		if (null != root) {
			root.accept(parser);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (fieldSettings != null) {
			for (final FieldSetting fs : fieldSettings) {
				if (!fs.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
