/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NamedBridgeScope;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.editors.EditorTracker;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.T3Doc;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * The Def_Testcase class represents TTCN3 testcase definitions.
 * 
 * @author Kristof Szabados
 * */
public final class Def_Testcase extends Definition implements IParameterisedAssignment {
	private static final String FULLNAMEPART1 = ".<formal_parameter_list>";
	private static final String FULLNAMEPART2 = ".<runs_on_type>";
	private static final String FULLNAMEPART3 = ".<system_type>";
	private static final String FULLNAMEPART4 = ".<statement_block>";

	private static final String DASHALLOWEDONLYFORTEMPLATES = "Using not used symbol (`-') as the default parameter"
			+ " is allowed only for modified templates";

	private static final String KIND = "testcase";

	public static String getKind() {
		return KIND;
	}

	private final FormalParameterList formalParList;
	private final Reference runsOnReference;
	private Component_Type runsOnType = null;
	private final Reference systemReference;
	private Component_Type systemType = null;
	private final StatementBlock block;
	private NamedBridgeScope bridgeScope = null;

	public Def_Testcase(final Identifier identifier, final FormalParameterList formalParameters, final Reference runsOnRef,
			final Reference systemRef, final StatementBlock block) {
		super(identifier);
		this.formalParList = formalParameters;
		if (formalParList != null) {
			formalParList.setMyDefinition(this);
		}
		this.runsOnReference = runsOnRef;
		this.systemReference = systemRef;
		this.block = block;
		if (block != null) {
			block.setMyDefinition(this);
			block.setFullNameParent(this);
		}

		if (formalParList != null) {
			formalParList.setFullNameParent(this);
		}
		if (runsOnRef != null) {
			runsOnRef.setFullNameParent(this);
		}
		if (systemRef != null) {
			systemRef.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_TESTCASE;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (formalParList == child) {
			return builder.append(FULLNAMEPART1);
		} else if (runsOnReference == child) {
			return builder.append(FULLNAMEPART2);
		} else if (systemReference == child) {
			return builder.append(FULLNAMEPART3);
		} else if (block == child) {
			return builder.append(FULLNAMEPART4);
		}

		return builder;
	}

	@Override
	public FormalParameterList getFormalParameterList() {
		return formalParList;
	}

	@Override
	public String getAssignmentName() {
		return "testcase";
	}

	@Override
	public String getOutlineIcon() {
		return "testcase.gif";
	}

	@Override
	public String getOutlineText() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		StringBuilder text = new StringBuilder(identifier.getDisplayName());
		if (formalParList == null) {
			return text.toString();
		}

		text.append('(');
		for (int i = 0; i < formalParList.getNofParameters(); i++) {
			if (i != 0) {
				text.append(", ");
			}
			FormalParameter parameter = formalParList.getParameterByIndex(i);
			if (Assignment_type.A_PAR_TIMER.equals(parameter.getRealAssignmentType())) {
				text.append("timer");
			} else {
				IType type = parameter.getType(lastTimeChecked);
				if (type == null) {
					text.append("Unknown type");
				} else {
					text.append(type.getTypename());
				}
			}
		}
		text.append(')');
		return text.toString();
	}

	@Override
	public String getProposalKind() {
		return KIND;
	}

	@Override
	public String getProposalDescription() {
		StringBuilder nameBuilder = new StringBuilder(identifier.getDisplayName());
		nameBuilder.append('(');
		formalParList.getAsProposalDesriptionPart(nameBuilder);
		nameBuilder.append(')');
		return nameBuilder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		if (bridgeScope != null && bridgeScope.getParentScope() == scope) {
			return;
		}

		bridgeScope = new NamedBridgeScope();
		bridgeScope.setParentScope(scope);
		scope.addSubScope(getLocation(), bridgeScope);
		bridgeScope.setScopeMacroName(identifier.getDisplayName());

		super.setMyScope(bridgeScope);
		if (runsOnReference != null) {
			runsOnReference.setMyScope(bridgeScope);
		}
		if (systemReference != null) {
			systemReference.setMyScope(bridgeScope);
		}
		formalParList.setMyScope(bridgeScope);
		if (block != null) {
			block.setMyScope(formalParList);
			bridgeScope.addSubScope(block.getLocation(), block);
		}
		bridgeScope.addSubScope(formalParList.getLocation(), formalParList);
	}

	public Component_Type getRunsOnType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return runsOnType;
	}

	public Component_Type getSystemType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return systemType;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		check(timestamp, null);
	}
		
	@Override
	public void check(final CompilationTimeStamp timestamp, IReferenceChain refChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		isUsed = false;
		runsOnType = null;
		systemType = null;
		lastTimeChecked = timestamp;

		T3Doc.check(this.getCommentLocation(), KIND);

		if (runsOnReference != null) {
			runsOnType = runsOnReference.chkComponentypeReference(timestamp);
			if (runsOnType != null) {
				Scope formalParlistPreviosScope = formalParList.getParentScope();
				if (formalParlistPreviosScope instanceof RunsOnScope
						&& ((RunsOnScope) formalParlistPreviosScope).getParentScope() == myScope) {
					((RunsOnScope) formalParlistPreviosScope).setComponentType(runsOnType);
				} else {
					Scope tempScope = new RunsOnScope(runsOnType, myScope);
					formalParList.setMyScope(tempScope);
				}
			}
		}

		if (systemReference != null) {
			systemType = systemReference.chkComponentypeReference(timestamp);
		}

		if (formalParList.hasNotusedDefaultValue()) {
			formalParList.getLocation().reportSemanticError(DASHALLOWEDONLYFORTEMPLATES);
			return;
		}

		boolean canSkip = false;
		if (myScope != null) {
			Module module = myScope.getModuleScope();
			if (module != null) {
				if (module.getSkippedFromSemanticChecking()) {
					canSkip = true;
				}
			}
		}
		
		if(!canSkip) {
			formalParList.reset();
		}
		formalParList.check(timestamp, getAssignmentType());

		if(canSkip) {
			return;
		}

		NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_TESTCASE, identifier, this);
		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

		if (block != null) {
			block.check(timestamp);

			block.postCheck();
			if (!EditorTracker.containsKey((IFile) getLocation().getFile())) {
				block.free();
			}
		}

		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp);
		}
	}

	@Override
	public void postCheck() {
		if (myScope != null) {
			Module module = myScope.getModuleScope();
			if (module != null) {
				if (module.getSkippedFromSemanticChecking()) {
					return;
				}
			}
		}
		super.postCheck();
		formalParList.postCheck();
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (Subreference_type.parameterisedSubReference.equals(subrefs.get(i).getReferenceType())
				&& identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match, but the chain of references ends here,
			// as testcases can not return with a type
			return;
		} else if (identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			// prefix
			if (subrefs.size() == i + 1) {
				StringBuilder patternBuilder = new StringBuilder(identifier.getDisplayName());
				patternBuilder.append('(');
				formalParList.getAsProposalPart(patternBuilder);
				patternBuilder.append(')');
				propCollector.addTemplateProposal(identifier.getDisplayName(), new Template(getProposalDescription(), "",
						propCollector.getContextIdentifier(), patternBuilder.toString(), false),
						TTCN3CodeSkeletons.SKELETON_IMAGE);
				super.addProposal(propCollector, i);
			}
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (identifier.getName().equals(subrefs.get(i).getId().getName()) && subrefs.size() == i + 1) {
			declarationCollector.addDeclaration(this);
		}

	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean enveloped = false;
			int result = 1;

			Location temporalIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(temporalIdentifier) || reparser.isExtending(temporalIdentifier)) {
				reparser.extendDamagedRegion(temporalIdentifier);
				IIdentifierReparser r = new IdentifierReparser(reparser);
				result = r.parseAndSetNameChanged();
				identifier = r.getIdentifier();
				// damage handled
				if (result == 0 && identifier != null) {
					enveloped = true;
				} else {
					removeBridge();
					throw new ReParseException(result);
				}
			}

			if (formalParList != null) {
				if (enveloped) {
					formalParList.updateSyntax(reparser, false);
					reparser.updateLocation(formalParList.getLocation());
				} else if (reparser.envelopsDamage(formalParList.getLocation())) {
					try {
						formalParList.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(formalParList.getLocation());
					} catch (ReParseException e) {
						removeBridge();
						throw e;
					}
				}
			}

			if (runsOnReference != null) {
				if (enveloped) {
					runsOnReference.updateSyntax(reparser, false);
					reparser.updateLocation(runsOnReference.getLocation());
				} else if (reparser.envelopsDamage(runsOnReference.getLocation())) {
					try {
						runsOnReference.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(runsOnReference.getLocation());
					} catch (ReParseException e) {
						removeBridge();
						throw e;
					}
				}
			}

			if (systemReference != null) {
				if (enveloped) {
					systemReference.updateSyntax(reparser, false);
					reparser.updateLocation(systemReference.getLocation());
				} else if (reparser.envelopsDamage(systemReference.getLocation())) {
					try {
						systemReference.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(systemReference.getLocation());
					} catch (ReParseException e) {
						removeBridge();
						throw e;
					}
				}
			}

			if (block != null) {
				if (enveloped) {
					block.updateSyntax(reparser, false);
					reparser.updateLocation(block.getLocation());
				} else if (reparser.envelopsDamage(block.getLocation())) {
					try {
						block.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(block.getLocation());
					} catch (ReParseException e) {
						removeBridge();
						throw e;
					}
				}
			}

			if (withAttributesPath != null) {
				if (enveloped) {
					withAttributesPath.updateSyntax(reparser, false);
					reparser.updateLocation(withAttributesPath.getLocation());
				} else if (reparser.envelopsDamage(withAttributesPath.getLocation())) {
					try {
						withAttributesPath.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(withAttributesPath.getLocation());
					} catch (ReParseException e) {
						removeBridge();
						throw e;
					}
				}
			}

			if (!enveloped) {
				removeBridge();
				throw new ReParseException();
			}

			return;
		}

		reparser.updateLocation(identifier.getLocation());
		if (formalParList != null) {
			formalParList.updateSyntax(reparser, false);
			reparser.updateLocation(formalParList.getLocation());
		}

		if (runsOnReference != null) {
			runsOnReference.updateSyntax(reparser, false);
			reparser.updateLocation(runsOnReference.getLocation());
		}

		if (systemReference != null) {
			systemReference.updateSyntax(reparser, false);
			reparser.updateLocation(systemReference.getLocation());
		}

		if (block != null) {
			block.updateSyntax(reparser, false);
			reparser.updateLocation(block.getLocation());
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	/**
	 * Removes the name bridging scope.
	 * */
	private void removeBridge() {
		if (bridgeScope != null) {
			bridgeScope.remove();
			bridgeScope = null;
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (formalParList != null) {
			formalParList.findReferences(referenceFinder, foundIdentifiers);
		}
		if (runsOnReference != null) {
			runsOnReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (systemReference != null) {
			systemReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (block != null) {
			block.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (formalParList != null && !formalParList.accept(v)) {
			return false;
		}
		if (runsOnReference != null && !runsOnReference.accept(v)) {
			return false;
		}
		if (systemReference != null && !systemReference.accept(v)) {
			return false;
		}
		if (block != null && !block.accept(v)) {
			return false;
		}
		return true;
	}
}
