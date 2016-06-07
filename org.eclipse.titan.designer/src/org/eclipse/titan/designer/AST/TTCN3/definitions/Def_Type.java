/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamedBridgeScope;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifier;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.types.Address_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Altstep_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Testcase_Type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.T3Doc;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeAnalyzer;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * The Def_Type class represents TTCN3 type definitions.
 * 
 * @author Kristof Szabados
 * */
public final class Def_Type extends Definition {
	private final Type type;

	private NamedBridgeScope bridgeScope = null;

	public Def_Type(final Identifier identifier, final Type type) {
		super(identifier);
		this.type = type;

		if (type != null) {
			type.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_TYPE;
	}

	@Override
	public String getAssignmentName() {
		return "type";
	}

	@Override
	public String getOutlineIcon() {
		if (type != null) {
			return type.getOutlineIcon();
		}

		return "type.gif";
	}

	@Override
	public int category() {
		int result = super.category();
		if (type != null) {
			result += type.category();
		}
		return result;
	}

	/**
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the type defined by this type definition
	 * */
	@Override
	public Type getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return type;
	}

	@Override
	public Type getSetting(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return type;
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
		if (type != null) {
			type.setMyScope(bridgeScope);
		}
	}

	@Override
	public void setWithAttributes(final MultipleWithAttributes attributes) {
		if (type == null) {
			return;
		}

		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
			type.setAttributeParentPath(withAttributesPath);
		}

		type.setWithAttributes(attributes);
	}

	@Override
	public void setAttributeParentPath(final WithAttributesPath parent) {
		super.setAttributeParentPath(parent);
		if (type == null) {
			return;
		}

		type.setAttributeParentPath(getAttributePath());
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

		lastTimeChecked = timestamp;
		isUsed = false;

		if (!"ADDRESS".equals(identifier.getTtcnName()) && !"anytype".equals(identifier.getTtcnName())) {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_TYPE, identifier, this);
			NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());
		}

		if (type == null) {
			return;
		}

		T3Doc.check(this.getCommentLocation(), type.getTypetypeTtcn3().toString());

		type.check(timestamp);
		type.checkConstructorName(identifier.getName());
		if ("ADDRESS".equals(identifier.getTtcnName())) {
			Address_Type.checkAddress(timestamp, type);
		}

		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		type.checkRecursions(timestamp, chain);
		chain.release();

		if (withAttributesPath != null) {
			// FIXME: doesn't work
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp, type.getTypetype());
			hasImplicitOmitAttribute(timestamp);
			analyzeExtensionAttributes(timestamp, withAttributesPath);
		}

		switch (type.getTypetype()) {
		case TYPE_FUNCTION:
			((Function_Type) type).getFormalParameters().setMyDefinition(this);
			break;
		case TYPE_ALTSTEP:
			((Altstep_Type) type).getFormalParameters().setMyDefinition(this);
			break;
		case TYPE_TESTCASE:
			((Testcase_Type) type).getFormalParameters().setMyDefinition(this);
			break;
		default:
			break;
		}
	}

	@Override
	public void postCheck() {
		super.postCheck();
		postCheckPrivateness();
	}

	/**
	 * Convert and check the encoding attributes applied to this function.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual build cycle.
	 * */
	public void analyzeExtensionAttributes(final CompilationTimeStamp timestamp, final WithAttributesPath withAttributesPath) {
		List<SingleWithAttribute> realAttributes = withAttributesPath.getRealAttributes(timestamp);

		SingleWithAttribute attribute;
		List<AttributeSpecification> specifications = null;
		for (int i = 0, size = realAttributes.size(); i < size; i++) {
			attribute = realAttributes.get(i);
			if (Attribute_Type.Extension_Attribute.equals(attribute.getAttributeType())) {
				Qualifiers qualifiers = attribute.getQualifiers();
				if (qualifiers == null || qualifiers.getNofQualifiers() == 0) {
					if (specifications == null) {
						specifications = new ArrayList<AttributeSpecification>();
					}
					specifications.add(attribute.getAttributeSpecification());
				} else {
					for (int j = 0, size2 = qualifiers.getNofQualifiers(); j < size2; j++) {
						Qualifier tempQualifier = qualifiers.getQualifierByIndex(i);
						ISubReference tempSubReference = tempQualifier.getSubReferenceByIndex(0);
						if (tempSubReference.getReferenceType() == Subreference_type.arraySubReference) {
							tempQualifier.getLocation().reportSemanticError(Qualifier.INVALID_INDEX_QUALIFIER);
						} else {
							tempQualifier.getLocation().reportSemanticError(
									MessageFormat.format(Qualifier.INVALID_FIELD_QUALIFIER, tempSubReference
											.getId().getDisplayName()));
						}
					}
				}
			}
		}

		if (specifications == null) {
			return;
		}

		List<ExtensionAttribute> attributes = new ArrayList<ExtensionAttribute>();
		for (int i = 0; i < specifications.size(); i++) {
			AttributeSpecification specification = specifications.get(i);
			ExtensionAttributeAnalyzer analyzer = new ExtensionAttributeAnalyzer();
			analyzer.parse(specification);

			List<ExtensionAttribute> temp = analyzer.getAttributes();
			if (temp != null) {
				attributes.addAll(temp);
			}
		}

		for (int i = 0; i < attributes.size(); i++) {
			ExtensionAttribute extensionAttribute = attributes.get(i);
			switch (extensionAttribute.getAttributeType()) {
			case ANYTYPE:
			case VERSION:
			case REQUIRES:
			case TITANVERSION:
				break;
			default:
				// only extension attributes are allowed ... and
				// only because they can not be stopped earlier.
				extensionAttribute.getLocation().reportSemanticError("Extension attributes are not supported for types");
			}
		}
	}

	@Override
	public String getProposalKind() {
		if (type != null) {
			return type.getProposalDescription(new StringBuilder()).toString();
		}
		return "unknown type";
	}

	@Override
	public Object[] getOutlineChildren() {
		if (type == null) {
			return super.getOutlineChildren();
		}

		return type.getOutlineChildren();
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i) {
			return;
		}

		if (subrefs.size() == i + 1 && identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			super.addProposal(propCollector, i);
		} else if (subrefs.size() > i + 1 && type != null && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			type.addProposal(propCollector, i + 1);
		}

		if (type != null && Type_type.TYPE_TTCN3_ENUMERATED.equals(type.getTypetype())) {
			type.addProposal(propCollector, i);
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() > i && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			if (subrefs.size() > i + 1 && type != null) {
				type.addDeclaration(declarationCollector, i + 1);
			} else if (subrefs.size() == i + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
				declarationCollector.addDeclaration(this);
			}
		} else {
			if (type != null && Type_type.TYPE_TTCN3_SEQUENCE.equals(type.getTypetype())) {

				if ((declarationCollector.getReference().getModuleIdentifier() != null && i == 1) || i == 0) {
					type.addDeclaration(declarationCollector, i);
				}

			}
		}

		if (type != null && Type_type.TYPE_TTCN3_ENUMERATED.equals(type.getTypetype())) {
			if ((declarationCollector.getReference().getModuleIdentifier() != null && i == 1) || i == 0) {
				type.addDeclaration(declarationCollector, i);
			}
		}
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		List<Integer> result = new ArrayList<Integer>();
		// might be extended with a subtype
		result.add(Ttcn3Lexer.LPAREN);
		// length restriction
		result.add(Ttcn3Lexer.LENGTH);
		// dimension
		result.add(Ttcn3Lexer.SQUAREOPEN);

		if (withAttributesPath == null || withAttributesPath.getAttributes() == null) {
			// might be extended with a with attribute
			result.add(Ttcn3Lexer.WITH);
		}

		return result;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean enveloped = false;

			Location temporalIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(temporalIdentifier) || reparser.isExtending(temporalIdentifier)) {
				reparser.extendDamagedRegion(temporalIdentifier);
				IIdentifierReparser r = new IdentifierReparser(reparser);
				int result = r.parseAndSetNameChanged();
				identifier = r.getIdentifier();
				// damage handled
				if (result == 0) {
					enveloped = true;
				} else {
					removeBridge();
					throw new ReParseException(result);
				}
			}

			if (type != null) {
				if (enveloped) {
					type.updateSyntax(reparser, false);
					reparser.updateLocation(type.getLocation());
				} else if (reparser.envelopsDamage(type.getLocation())) {
					try {
						type.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(type.getLocation());
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
		if (type != null) {
			type.updateSyntax(reparser, false);
			reparser.updateLocation(type.getLocation());
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
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (type != null && !type.accept(v)) {
			return false;
		}
		return true;
	}
}
