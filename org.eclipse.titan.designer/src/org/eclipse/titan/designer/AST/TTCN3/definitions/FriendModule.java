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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Module.module_type;
import org.eclipse.titan.designer.AST.TTCN3.IAppendableSyntax;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ParserFactory;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Lexer4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The FriendModule class represents a TTCN-3 module friendship declaration.
 * This is call is used to store the identifier of a friend of a module.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class FriendModule extends ASTNode implements ILocateableNode, IAppendableSyntax, IIncrementallyUpdateable {
	public static final String MISSINGMODULE = "There is no module with name `{0}''";

	private Identifier identifier;
	protected WithAttributesPath withAttributesPath = null;
	private Group parentGroup = null;

	private IProject project;
	private Location location;

	private CompilationTimeStamp lastCheckTimeStamp;

	public FriendModule(final Identifier identifier) {
		this.identifier = identifier;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	/**
	 * Sets the parser of the project this module friendship belongs to.
	 * 
	 * @param project
	 *                the project this module friendship belongs to
	 * */
	public void setProject(final IProject project) {
		this.project = project;
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
	 * Sets the parent group of the assignment.
	 * 
	 * @param parentGroup
	 *                the parent group to be set.
	 * */
	public void setParentGroup(final Group parentGroup) {
		this.parentGroup = parentGroup;
	}

	/** @return the parent group of the assignment */
	public Group getParentGroup() {
		return parentGroup;
	}

	/**
	 * Sets the with attributes for this friend module if it has any. Also
	 * creates the with attribute path, to store the attributes in.
	 * 
	 * @param attributes
	 *                the attribute to be added.
	 * */
	public void setWithAttributes(final MultipleWithAttributes attributes) {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}
		if (attributes != null) {
			withAttributesPath.setWithAttributes(attributes);
		}
	}

	/**
	 * @return the with attribute path element of this friend module. If it
	 *         did not exist it will be created.
	 * */
	public WithAttributesPath getAttributePath() {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}

		return withAttributesPath;
	}

	/**
	 * Sets the parent path for the with attribute path element of this
	 * friend module. Also, creates the with attribute path node if it did
	 * not exist before.
	 * 
	 * @param parent
	 *                the parent to be set.
	 * */
	public void setAttributeParentPath(final WithAttributesPath parent) {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}

		withAttributesPath.setAttributeParent(parent);
	}

	/**
	 * Does the semantic checking of this friend ship.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastCheckTimeStamp != null && !lastCheckTimeStamp.isLess(timestamp)) {
			return;
		}

		ProjectSourceParser parser = GlobalParser.getProjectSourceParser(project);
		if (parser == null || identifier == null) {
			lastCheckTimeStamp = timestamp;
			return;
		}

		Module referredModule = parser.getModuleByName(identifier.getName());

		if (referredModule == null) {
			identifier.getLocation().reportConfigurableSemanticProblem(
					Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.REPORTMISSINGFRIENDMODULE, GeneralConstants.WARNING, null),
					MessageFormat.format(MISSINGMODULE, identifier.getDisplayName()));
		} else if (!module_type.TTCN3_MODULE.equals(referredModule.getModuletype())) {
			identifier.getLocation().reportSemanticError(
					MessageFormat.format("The friend module `{0}'' must be a TTCN-3 module", identifier.getDisplayName()));
		}

		lastCheckTimeStamp = timestamp;
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (withAttributesPath == null || withAttributesPath.getAttributes() == null) {
			List<Integer> result = new ArrayList<Integer>();
			result.add(TTCN3Lexer4.WITH);
			return result;
		}

		return null;
	}

	@Override
	public List<Integer> getPossiblePrefixTokens() {
		return new ArrayList<Integer>(0);
	}

	/**
	 * Handles the incremental parsing of this friend module declaration.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			boolean enveloped = false;
			int result = 1;

			Location temporalIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(temporalIdentifier) || reparser.isExtending(temporalIdentifier)) {
				reparser.extendDamagedRegion(temporalIdentifier);
				IIdentifierReparser r = ParserFactory.createIdentifierReparser(reparser);
				result = r.parse();
				identifier = r.getIdentifier();

				// damage handled
				if (result == 0) {
					enveloped = true;
				} else {
					throw new ReParseException(result);
				}
			}

			if (withAttributesPath != null) {
				if (enveloped) {
					withAttributesPath.updateSyntax(reparser, false);
					reparser.updateLocation(withAttributesPath.getLocation());
				} else if (reparser.envelopsDamage(withAttributesPath.getLocation())) {
					reparser.extendDamagedRegion(withAttributesPath.getLocation());
					result = reparse( reparser );
				}
			}

			if (result != 0) {
				throw new ReParseException();
			}

			return;
		}

		reparser.updateLocation(identifier.getLocation());

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	protected abstract int reparse( TTCN3ReparseUpdater aReparser );

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (withAttributesPath != null && !withAttributesPath.accept(v)) {
			return false;
		}
		return true;
	}

	public IProject getProject() {
		return project;
	}
}
