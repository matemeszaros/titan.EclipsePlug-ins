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
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IReferencingElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ModuleImportation;
import org.eclipse.titan.designer.AST.ModuleImportationChain;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.IAppendableSyntax;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ITTCN3ReparseBase;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Reparser;

/**
 * The ImportModule class represents a TTCN3 import statement. This class is
 * used to create a link between the actual mode containing the import statement
 * and the imported module.
 * 
 * @author Kristof Szabados
 * */
/*
 * Actually this should be subclassed to the possible types to provide correct
 * handling.
 */
// FIXME the normal import specification and import of imports specification
// should be stored in lists.
public final class ImportModule extends ModuleImportation implements ILocateableNode, IAppendableSyntax, IIncrementallyUpdateable,
		IReferencingElement {
	public static final String MISSINGMODULE = "There is no module with name `{0}''";

	protected WithAttributesPath withAttributesPath = null;

	private Identifier myModuleIdentifier;
	private TTCN3Module myModule;

	private Group parentGroup = null;
	private Location location;

	/** The visibility modifier of the definition */
	private VisibilityModifier visibilityModifier;

	private boolean hasNormalImport = false;
	private boolean hasImportOfImport = false;

	public ImportModule(final Identifier identifier) {
		super(identifier);
		visibilityModifier = VisibilityModifier.Private;
	}

	/**
	 * Sets the current import statement to be containing normal (non-import
	 * of imports) imports.
	 * */
	public void setHasNormalImports() {
		hasNormalImport = true;
	}

	/**
	 * Sets the current import statement to be containing import of imports
	 * imports.
	 * */
	public void setHasImportOfImports() {
		hasImportOfImport = true;
	}

	/**
	 * Sets the visibility modifier of this definition.
	 * 
	 * @param modifier
	 *                the modifier to be set
	 * */
	public void setVisibility(final VisibilityModifier modifier) {
		visibilityModifier = modifier;
	}

	/**
	 * @return the visibility modifier of this definition.
	 * */
	public VisibilityModifier getVisibilityModifier() {
		return visibilityModifier;
	}

	/**
	 * Returns the name of the module.
	 * 
	 * @return the name of the module
	 * */
	public String getName() {
		return (identifier != null) ? identifier.getName() : null;
	}

	@Override
	public String chainedDescription() {
		if (myModuleIdentifier != null) {
			return myModuleIdentifier.getDisplayName();
		}

		return (identifier != null) ? identifier.getDisplayName() : null;
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
	 * Sets the module in which this importation can be found.
	 * 
	 * @param module
	 *                the module containing this importation
	 * */
	public void setMyModule(final Identifier module) {
		myModuleIdentifier = module;
	}

	/**
	 * Sets the module of the importation.
	 * 
	 * @param myModule
	 *                the module of the importation.
	 * */
	public void setMyModule(final TTCN3Module myModule) {
		this.myModule = myModule;
	}

	protected TTCN3Module getMyModule() {
		return myModule;
	}

	/**
	 * Sets the parent group of the importation.
	 * 
	 * @param parentGroup
	 *                the parent group to be set.
	 * */
	public void setParentGroup(final Group parentGroup) {
		this.parentGroup = parentGroup;
	}

	/** @return the parent group of the importation */
	public Group getParentGroup() {
		return parentGroup;
	}

	/**
	 * Returns the anytype definition contained in the module pointed to by
	 * this importation.
	 * 
	 * @return the anytype definition of the imported module
	 * */
	public Def_Type getAnytype() {
		return (referredModule == null) ? null : referredModule.getAnytype();
	}

	/**
	 * Sets the with attributes for this importation if it has any. Also
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
	 * @return the with attribute path element of this importation. If it
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
	 * importation. Also, creates the with attribute path node if it did not
	 * exist before.
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

	@Override
	public void checkImports(final CompilationTimeStamp timestamp, final ModuleImportationChain referenceChain, final List<Module> moduleStack) {
		if (lastImportCheckTimeStamp != null && !lastImportCheckTimeStamp.isLess(timestamp)) {
			return;
		}

		final Module temp = referredModule;
		referredModule = null;

		final ProjectSourceParser parser = GlobalParser.getProjectSourceParser(project);
		if (parser == null || identifier == null) {
			lastImportCheckTimeStamp = timestamp;
			return;
		}

		referredModule = parser.getModuleByName(identifier.getName());
		if (temp != referredModule) {
			setUnhandledChange(true);
		}

		if (referredModule == null) {
			identifier.getLocation().reportSemanticError(MessageFormat.format(MISSINGMODULE, identifier.getDisplayName()));
			lastImportCheckTimeStamp = timestamp;
			return;
		}

		moduleStack.add(referredModule);
		if (referenceChain.add(this)) {
			if (hasImportOfImport) {
				if (referredModule instanceof TTCN3Module) {
					final TTCN3Module ttcnmodule = (TTCN3Module) referredModule;
					final List<ImportModule> imports = ttcnmodule.getImports();
					for (ImportModule importation : imports) {
						referenceChain.markState();
						importation.checkImports(timestamp, referenceChain, moduleStack);
						referenceChain.previousState();
					}
				} else {
					// FIXME There was no test for this case
					location.reportSemanticError("import of imports can only be used on TTCN-3 modules");
				}
			}
			if (hasNormalImport) {
				referredModule.checkImports(timestamp, referenceChain, moduleStack);//TODO: Check, this is not recursive!!!!
			}
		}
		moduleStack.remove(moduleStack.size() - 1);

		lastImportCheckTimeStamp = timestamp;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		setUnhandledChange(false);

		if (lastImportCheckTimeStamp != null && !lastImportCheckTimeStamp.isLess(timestamp)) {
			// the flag indicating whether this import is used
			// should be reset only once per semantic check cycle.
			return;
		}

		if (referredModule != null) {
			referredModule.check(timestamp);
		}

		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp);
		}

		usedForImportation = false;
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (withAttributesPath == null || withAttributesPath.getAttributes() == null) {
			List<Integer> result = new ArrayList<Integer>();
			result.add(Ttcn3Lexer.WITH);
			return result;
		}

		return null;
	}

	@Override
	public List<Integer> getPossiblePrefixTokens() {
		if (withAttributesPath == null || withAttributesPath.getAttributes() == null) {
			List<Integer> result = new ArrayList<Integer>(2);
			result.add(Ttcn3Lexer.PUBLIC);
			result.add(Ttcn3Lexer.PRIVATE);
			return result;
		}

		return new ArrayList<Integer>(0);
	}

	/**
	 * Handles the incremental parsing of this module importation.
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

			Location temporalIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(temporalIdentifier) || reparser.isExtending(temporalIdentifier)) {
				reparser.extendDamagedRegion(temporalIdentifier);
				IIdentifierReparser r = new IdentifierReparser(reparser);
				int result = r.parse();
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
					int result = reparse( reparser );
					if (result != 0) {
						throw new ReParseException();
					}
					enveloped = true;
				}
			}

			if (!enveloped) {
				throw new ReParseException(1);
			}

			return;
		}

		reparser.updateLocation(identifier.getLocation());

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	private int reparse(final TTCN3ReparseUpdater aReparser) {
		return aReparser.parse(new ITTCN3ReparseBase() {
			@Override
			public void reparse(final Ttcn3Reparser parser) {
				MultipleWithAttributes attributes = parser.pr_reparser_optionalWithStatement().attributes;
				parser.pr_EndOfFile();
				if ( parser.isErrorListEmpty() ) {
					withAttributesPath.setWithAttributes(attributes);
					if (attributes != null) {
						getLocation().setEndOffset(attributes.getLocation().getEndOffset());
					}
				}
			}
		});
	}

	@Override
	public boolean hasImportedAssignmentWithID(final CompilationTimeStamp timestamp, final Identifier identifier) {
		if (referredModule == null) {
			return false;
		}

		if (hasImportOfImport) {
			if (referredModule instanceof TTCN3Module) {
				final TTCN3Module ttcnmodule = (TTCN3Module) referredModule;
				final List<ImportModule> imports = ttcnmodule.getImports();
				for (ImportModule importation : imports) {
					if (importation.hasImportedAssignmentWithID(timestamp, identifier)) {
						return true;
					}
				}
			}
		}

		if (hasNormalImport) {
			final Assignments assignments = referredModule.getAssignments();
			if (assignments != null && assignments.hasLocalAssignmentWithID(timestamp, identifier)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Assignment importAssignment(final CompilationTimeStamp timestamp, final ModuleImportationChain referenceChain,
			final Identifier moduleId, final Reference reference, final List<ModuleImportation> usedImports) {
		if (referenceChain.contains(this)) {
			return null;
		}

		if (referredModule == null) {
			return null;
		}

		Assignment result = null;
		if (hasImportOfImport) {
			if (referredModule instanceof TTCN3Module) {
				final TTCN3Module ttcnmodule = (TTCN3Module) referredModule;
				Assignment tempResult = null;
				final List<ImportModule> imports = ttcnmodule.getImports();
				for (ImportModule importation : imports) {
					List<ModuleImportation> tempUsedImports = new ArrayList<ModuleImportation>();
					// check if it could be reached if
					// visibility is out of question
					referenceChain.markState();
					if (importation.getVisibilityModifier() == VisibilityModifier.Public) {
						tempResult = importation.importAssignment(timestamp, referenceChain, moduleId, reference,
								tempUsedImports);
					} else if (importation.getVisibilityModifier() == VisibilityModifier.Friend) {
						tempResult = importation.importAssignment(timestamp, referenceChain, moduleId, reference,
								tempUsedImports);
						if (tempResult != null) {
							tempUsedImports.add(importation);
						}
					}
					referenceChain.previousState();

					if (tempResult != null) {
						// if found something check if
						// all imports used to find it
						// are visible in the actual
						// module
						boolean visible = true;
						for (ModuleImportation usedImportation : tempUsedImports) {
							if (usedImportation instanceof ImportModule) {
								ImportModule ttcnImport = (ImportModule) usedImportation;
								if (!ttcnImport.getMyModule().isVisible(timestamp, myModuleIdentifier, ttcnImport)) {
									visible = false;
								}
							}
						}
						if (visible) {
							usedImports.addAll(tempUsedImports);
							if (result == null) {
								result = tempResult;
							} else if (result != tempResult) {
								// the reference could point to two locations
								reference.getLocation().reportSemanticError(
										"It is not possible to resolve this reference unambigously, as  it can be resolved to `"
												+ result.getFullName() + "' and to `"
												+ tempResult.getFullName() + "'");
								return null;
							}
						}
						tempResult = null;
					}
				}
			}
		}
		if (hasNormalImport) {
			result = referredModule.importAssignment(timestamp, moduleId, reference);
		}

		if (result != null) {
			usedImports.add(this);
			setUsedForImportation();
		}

		return result;
	}

	// FIXME ezeket sem teszteltuk
	@Override
	public void addProposal(final ProposalCollector propCollector, final Identifier targetModuleId) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();

		if (propCollector.getReference().getModuleIdentifier() == null && subrefs.size() == 1) {
			propCollector.addProposal(identifier, ImageCache.getImage(getOutlineIcon()), KIND);
		}

		final Module savedReferredModule = referredModule;
		if (savedReferredModule != null) {
			Assignments assignments = savedReferredModule.getAssignments();
			for (int i = 0, size = assignments.getNofAssignments(); i < size; i++) {
				Assignment temporalAssignment = assignments.getAssignmentByIndex(i);
				if (savedReferredModule.isVisible(CompilationTimeStamp.getBaseTimestamp(), targetModuleId, temporalAssignment)) {
					temporalAssignment.addProposal(propCollector, 0);
				}
			}

			if (savedReferredModule instanceof TTCN3Module) {
				final TTCN3Module ttcnmodule = (TTCN3Module) savedReferredModule;
				final List<ImportModule> imports = ttcnmodule.getImports();
				for (ImportModule importation : imports) {
					if (importation.getVisibilityModifier() == VisibilityModifier.Public) {
						importation.addProposal(propCollector, targetModuleId);
					} else if (importation.getVisibilityModifier() == VisibilityModifier.Friend) {
						// The import is the friendly one
						if (ttcnmodule.isVisible(CompilationTimeStamp.getBaseTimestamp(), targetModuleId, importation)) {
							importation.addProposal(propCollector, targetModuleId);
						}
					}
				}
			}
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final Identifier targetModuleId) {
		final Module savedReferredModule = referredModule;
		if (savedReferredModule != null) {
			Assignments assignments = savedReferredModule.getAssignments();
			for (int i = 0; i < assignments.getNofAssignments(); i++) {
				Assignment temporalAssignment = assignments.getAssignmentByIndex(i);
				if (savedReferredModule.isVisible(CompilationTimeStamp.getBaseTimestamp(), targetModuleId, temporalAssignment)) {
					temporalAssignment.addDeclaration(declarationCollector, 0);
				}
			}

			if (savedReferredModule instanceof TTCN3Module) {
				final TTCN3Module ttcnmodule = (TTCN3Module) savedReferredModule;
				final List<ImportModule> imports = ttcnmodule.getImports();
				for (ImportModule importation : imports) {
					if (importation.getVisibilityModifier() == VisibilityModifier.Public) {
						importation.addDeclaration(declarationCollector, targetModuleId);
					} else if (importation.getVisibilityModifier() == VisibilityModifier.Friend) {
						// The import is the friendly
						// one
						if (ttcnmodule.isVisible(CompilationTimeStamp.getBaseTimestamp(), targetModuleId, importation)) {
							importation.addDeclaration(declarationCollector, targetModuleId);
						}
					}
				}
			}

			Identifier moduleId = declarationCollector.getReference().getModuleIdentifier();
			List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
			if (moduleId == null && subrefs.size() == 1 && identifier.getName().equals(subrefs.get(0).getId().getName())) {
				declarationCollector.addDeclaration(savedReferredModule.getIdentifier().getDisplayName(), savedReferredModule
						.getIdentifier().getLocation(), (Assignment) null);
			}
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
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (withAttributesPath != null && !withAttributesPath.accept(v)) {
			return false;
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}

	@Override
	public Declaration getDeclaration() {
		return Declaration.createInstance(getReferredModule());
	}
}
