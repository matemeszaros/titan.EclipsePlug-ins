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

import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType.Encoding_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.DecodeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.EncodeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErrorBehaviorAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErrorBehaviorList;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.PrintingAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.PrintingType;
import org.eclipse.titan.designer.AST.TTCN3.attributes.PrototypeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function.EncodingPrototype_type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeAnalyzer;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * The Def_ExtFunction class represents TTCN3 external function definitions.
 * 
 * @author Kristof Szabados
 * */
public final class Def_Extfunction extends Definition implements IParameterisedAssignment {
	public enum ExternalFunctionEncodingType_type {
		/** manual encoding. */
		MANUAL,
		/** generated encoder function. */
		ENCODE,
		/** generated decoder function. */
		DECODE
	}

	private static final String FULLNAMEPART1 = ".<formal_parameter_list>";
	private static final String FULLNAMEPART2 = ".<type>";
	private static final String FULLNAMEPART3 = ".<errorbehavior_list>";
	public static final String PORTRETURNNOTALLOWED = "External functions can not return ports";

	private static final String KIND = "external function";

	private final Assignment_type assignmentType;
	private final FormalParameterList formalParList;
	private final Type returnType;
	private final boolean returnsTemplate;
	private final TemplateRestriction.Restriction_type templateRestriction;
	private EncodingPrototype_type prototype;
	private Type inputType;
	private Type outputType;
	private ExternalFunctionEncodingType_type functionEncodingType;
	private Encoding_type encodingType;
	private String encodingOptions;
	private ErrorBehaviorList errorBehaviorList;
	private PrintingType printingType;

	public Def_Extfunction(final Identifier identifier, final FormalParameterList formalParameters, final Type returnType,
			final boolean returnsTemplate, final TemplateRestriction.Restriction_type templateRestriction) {
		super(identifier);
		assignmentType = (returnType == null) ? Assignment_type.A_EXT_FUNCTION : (returnsTemplate ? Assignment_type.A_EXT_FUNCTION_RTEMP
				: Assignment_type.A_EXT_FUNCTION_RVAL);
		formalParList = formalParameters;
		formalParList.setMyDefinition(this);
		this.returnType = returnType;
		this.returnsTemplate = returnsTemplate;
		this.templateRestriction = templateRestriction;
		prototype = EncodingPrototype_type.NONE;
		functionEncodingType = ExternalFunctionEncodingType_type.MANUAL;
		encodingType = Encoding_type.UNDEFINED;
		encodingOptions = null;
		errorBehaviorList = null;
		printingType = null;

		formalParList.setFullNameParent(this);
		if (returnType != null) {
			returnType.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return assignmentType;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (formalParList == child) {
			return builder.append(FULLNAMEPART1);
		} else if (returnType == child) {
			return builder.append(FULLNAMEPART2);
		} else if (errorBehaviorList == child) {
			return builder.append(FULLNAMEPART3);
		} 

		return builder;
	}

	@Override
	public FormalParameterList getFormalParameterList() {
		return formalParList;
	}

	public EncodingPrototype_type getPrototype() {
		return prototype;
	}

	public Type getInputType() {
		return inputType;
	}

	public Type getOutputType() {
		return outputType;
	}

	@Override
	public String getProposalKind() {
		return KIND;
	}

	@Override
	public String getAssignmentName() {
		return "external function";
	}

	@Override
	public String getOutlineIcon() {
		if (returnType == null) {
			return "function_external.gif";
		}

		return "function_external_return.gif";
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		formalParList.setMyScope(scope);
		if (returnType != null) {
			returnType.setMyScope(scope);
		}
		scope.addSubScope(formalParList.getLocation(), formalParList);
	}

	@Override
	public Type getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return returnType;
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
		prototype = EncodingPrototype_type.NONE;
		functionEncodingType = ExternalFunctionEncodingType_type.MANUAL;
		encodingType = Encoding_type.UNDEFINED;
		encodingOptions = null;
		errorBehaviorList = null;
		printingType = null;
		lastTimeChecked = timestamp;

		NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_EXTERNALFUNCTION, identifier, this);
		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

		if (returnType != null) {
			returnType.check(timestamp);
			IType returnedType = returnType.getTypeRefdLast(timestamp);
			if (returnedType != null && Type_type.TYPE_PORT.equals(returnedType.getTypetype()) && returnType.getLocation() != null) {
				returnType.getLocation().reportSemanticError(PORTRETURNNOTALLOWED);
			}
		}

		formalParList.reset();
		formalParList.check(timestamp, getAssignmentType());

		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp);
			analyzeExtensionAttributes(timestamp);
			checkPrototype(timestamp);
			checkFunctionType(timestamp);
		}
	}

	@Override
	public void postCheck() {
		super.postCheck();
		postCheckPrivateness();
	}

	/**
	 * Convert and check the encoding attributes applied to this external
	 * function.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual build cycle.
	 * */
	public void analyzeExtensionAttributes(final CompilationTimeStamp timestamp) {
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
				}
			}
		}

		if (specifications == null) {
			return;
		}

		List<ExtensionAttribute> attributes = new ArrayList<ExtensionAttribute>();
		AttributeSpecification specification;
		for (int i = 0, size = specifications.size(); i < size; i++) {
			specification = specifications.get(i);
			ExtensionAttributeAnalyzer analyzer = new ExtensionAttributeAnalyzer();
			analyzer.parse(specification);
			List<ExtensionAttribute> temp = analyzer.getAttributes();
			if (temp != null) {
				attributes.addAll(temp);
			}
		}

		ExtensionAttribute extensionAttribute;
		for (int i = 0, size = attributes.size(); i < size; i++) {
			extensionAttribute = attributes.get(i);

			switch (extensionAttribute.getAttributeType()) {
			case PROTOTYPE:
				if (EncodingPrototype_type.NONE.equals(prototype)) {
					prototype = ((PrototypeAttribute) extensionAttribute).getPrototypeType();
				} else {
					location.reportSemanticError("duplicate attribute `prototype'.");
				}
				break;
			case ENCODE:
				switch (functionEncodingType) {
				case MANUAL:
					break;
				case ENCODE:
					location.reportSemanticError("duplicate attribute `encode'.");
					break;
				case DECODE:
					location.reportSemanticError("`decode' and `encode' attributes cannot be used at the same time.");
					break;
				default:
					break;
				}
				encodingType = ((EncodeAttribute) extensionAttribute).getEncodingType();
				encodingOptions = ((EncodeAttribute) extensionAttribute).getOptions();
				functionEncodingType = ExternalFunctionEncodingType_type.ENCODE;
				break;
			case DECODE:
				switch (functionEncodingType) {
				case MANUAL:
					break;
				case ENCODE:
					location.reportSemanticError("`decode' and `encode' attributes cannot be used at the same time.");
					break;
				case DECODE:
					location.reportSemanticError("duplicate attribute `decode'.");
					break;
				default:
					break;
				}
				encodingType = ((DecodeAttribute) extensionAttribute).getEncodingType();
				encodingOptions = ((DecodeAttribute) extensionAttribute).getOptions();
				functionEncodingType = ExternalFunctionEncodingType_type.DECODE;
				break;
			case ERRORBEHAVIOR:
				if (errorBehaviorList == null) {
					errorBehaviorList = ((ErrorBehaviorAttribute) extensionAttribute).getErrrorBehaviorList();
				} else {
					errorBehaviorList.addAllBehaviors(((ErrorBehaviorAttribute) extensionAttribute).getErrrorBehaviorList());
				}
				break;
			case PRINTING:
				if (printingType == null) {
					printingType = ((PrintingAttribute) extensionAttribute).getPrintingType();
				} else {
					location.reportSemanticError("duplicate attribute `printing'.");
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Checks the prototype attribute set for this external function
	 * definition.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	private void checkPrototype(final CompilationTimeStamp timestamp) {
		if (EncodingPrototype_type.NONE.equals(prototype)) {
			return;
		}

		// checking formal parameter list
		if (EncodingPrototype_type.CONVERT.equals(prototype)) {
			if (formalParList.getNofParameters() == 1) {
				FormalParameter parameter = formalParList.getParameterByIndex(0);
				switch (parameter.getRealAssignmentType()) {
				case A_PAR_VAL:
				case A_PAR_VAL_IN:
					inputType = parameter.getType(timestamp);
					break;
				default: {
					final String message = MessageFormat
							.format("The parameter must be an `in'' value parameter for attribute `prototype({0})'' instead of {1}",
									prototype.getName(), parameter.getAssignmentName());
					parameter.getLocation().reportSemanticError(message);
					break;
				}
				}
			} else {
				final String message = MessageFormat.format(
						"The external function must have one parameter instead of {0} for attribute `prototype({1})''",
						formalParList.getNofParameters(), prototype.getName());
				formalParList.getLocation().reportSemanticError(message);
			}
		} else if (formalParList.getNofParameters() == 2) {
			FormalParameter firstParameter = formalParList.getParameterByIndex(0);
			if (EncodingPrototype_type.SLIDING.equals(prototype)) {
				if (Assignment_type.A_PAR_VAL_INOUT.equals(firstParameter.getRealAssignmentType())) {
					Type firstParameterType = firstParameter.getType(timestamp);
					IType last = firstParameterType.getTypeRefdLast(timestamp);
					if (last.getIsErroneous(timestamp)) {
						inputType = firstParameterType;
					} else {
						switch (last.getTypetypeTtcn3()) {
						case TYPE_OCTETSTRING:
						case TYPE_CHARSTRING:
						case TYPE_BITSTRING:
							inputType = firstParameterType;
							break;
						default: {
							final String message = MessageFormat
									.format("The type of the first parameter must be `octetstring'' or `charstring'' for attribute `prototype({0})''",
											prototype.getName());
							firstParameter.getLocation().reportSemanticError(message);

							break;
						}
						}
					}
				} else {
					firstParameter.getLocation()
							.reportSemanticError(
									MessageFormat.format(
											"The first parameter must be an `inout'' value parameter for attribute `prototype({0})'' instead of {1}",
											prototype.getName(), firstParameter.getAssignmentName()));
				}
			} else {
				if (Assignment_type.A_PAR_VAL_IN.equals(firstParameter.getRealAssignmentType()) ||
					Assignment_type.A_PAR_VAL.equals(firstParameter.getRealAssignmentType())) {
					inputType = firstParameter.getType(timestamp);
				} else {
					firstParameter.getLocation()
							.reportSemanticError(
									MessageFormat.format(
											"The first parameter must be an `in'' value parameter for attribute `prototype({0})'' instead of {1}",
											prototype.getName(), firstParameter.getAssignmentName()));
				}
			}

			FormalParameter secondParameter = formalParList.getParameterByIndex(1);
			if (Assignment_type.A_PAR_VAL_OUT.equals(secondParameter.getRealAssignmentType())) {
				outputType = secondParameter.getType(timestamp);
			} else {
				secondParameter.getLocation()
						.reportSemanticError(
								MessageFormat.format(
										"The second parameter must be an `out'' value parameter for attribute `prototype({0})'' instead of {1}",
										prototype.getName(), secondParameter.getAssignmentName()));
			}
		} else {
			formalParList.getLocation().reportSemanticError(
					MessageFormat.format("The function must have two parameters for attribute `prototype({0})'' instead of {1}",
							prototype.getName(), formalParList.getNofParameters()));
		}

		// checking the return type
		if (EncodingPrototype_type.FAST.equals(prototype)) {
			if (returnType != null) {
				returnType.getLocation().reportSemanticError(
						MessageFormat.format("The external function cannot have return type fo attribute `prototype({0})''",
								prototype.getName()));
			}
		} else {
			if (returnType == null) {
				location.reportSemanticError(MessageFormat.format(
						"The external function must have a return type for attribute `prototype({0})''", prototype.getName()));
			} else {
				if (Assignment_type.A_FUNCTION_RTEMP.equals(assignmentType)) {
					returnType.getLocation()
							.reportSemanticError(
									MessageFormat.format(
											"The external function must return a value instead of a template for attribute `prototype({0})''",
											prototype.getName()));
				}

				if (EncodingPrototype_type.CONVERT.equals(prototype)) {
					outputType = returnType;
				} else {
					IType last = returnType.getTypeRefdLast(timestamp);

					if (!last.getIsErroneous(timestamp) && !Type_type.TYPE_INTEGER.equals(last.getTypetypeTtcn3())) {
						returnType.getLocation()
								.reportSemanticError(
										MessageFormat.format(
												"The return type of the function must be `integer'' instead of `{0}'' for attribute `prototype({1})''",
												returnType.getTypename(), prototype.getName()));
					}
				}
			}
		}
	}

	/**
	 * Checks that the encoding/decoding attributes set for this external
	 * function definition are valid according to the encoding/decoding type
	 * of the function.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void checkFunctionType(final CompilationTimeStamp timestamp) {
		switch (functionEncodingType) {
		case MANUAL:
			if (errorBehaviorList != null) {
				errorBehaviorList.getLocation().reportSemanticError(
						"Attribute `errorbehavior' can only be used together with `encode' or `decode'");
				errorBehaviorList.check(timestamp);
			}
			break;
		case ENCODE:
			switch (prototype) {
			case NONE:
				location.reportSemanticError("Attribute `encode' cannot be used without `prototype'");
				break;
			case BACKTRACK:
			case SLIDING:
				location.reportSemanticError(MessageFormat.format("Attribute `encode'' cannot be used without `prototype({0})''",
						prototype.getName()));
				break;
			default:
				break;
			}
			// FIXME implement once we know what encoding is set for
			// a type
			if (errorBehaviorList != null) {
				errorBehaviorList.check(timestamp);
			}
			if (printingType != null) {
				printingType.check(timestamp);
			}
			break;
		case DECODE:
			if (EncodingPrototype_type.NONE.equals(prototype)) {
				location.reportSemanticError("Attribute `decode' cannot be used without `prototype'");
			}
			// FIXME implement once we know what encoding is set for
			// a type
			if (errorBehaviorList != null) {
				errorBehaviorList.check(timestamp);
			}
			break;
		default:
			// no other option possible
			break;
		}
		
		if (printingType != null && (functionEncodingType != ExternalFunctionEncodingType_type.ENCODE || 
				encodingType != Encoding_type.JSON)) {
			location.reportSemanticError("Attribute `printing' is only allowed for JSON encoding functions");
		}
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
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (subrefs.size() == i + 1 && identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			StringBuilder patternBuilder = new StringBuilder(identifier.getDisplayName());
			patternBuilder.append('(');
			formalParList.getAsProposalPart(patternBuilder);
			patternBuilder.append(')');
			propCollector.addTemplateProposal(identifier.getDisplayName(),
					new Template(getProposalDescription(), "", propCollector.getContextIdentifier(), patternBuilder.toString(),
							false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			super.addProposal(propCollector, i);
		} else if (subrefs.size() > i + 1 && returnType != null
				&& Subreference_type.parameterisedSubReference.equals(subrefs.get(i).getReferenceType())
				&& identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			returnType.addProposal(propCollector, i + 1);
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (identifier.getName().equals(subrefs.get(0).getId().getName())) {
			if (subrefs.size() > i + 1 && returnType != null) {
				returnType.addDeclaration(declarationCollector, i + 1);
			} else {
				declarationCollector.addDeclaration(this);
			}
		}
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
	public TemplateRestriction.Restriction_type getTemplateRestriction() {
		return templateRestriction;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
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
					throw new ReParseException(result);
				}
			}

			if (formalParList != null) {
				if (enveloped) {
					formalParList.updateSyntax(reparser, false);
					reparser.updateLocation(formalParList.getLocation());
				} else if (reparser.envelopsDamage(formalParList.getLocation())) {
					formalParList.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(formalParList.getLocation());
				}
			}

			if (returnType != null) {
				if (enveloped) {
					returnType.updateSyntax(reparser, false);
					reparser.updateLocation(returnType.getLocation());
				} else if (reparser.envelopsDamage(returnType.getLocation())) {
					returnType.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(returnType.getLocation());
				}
			}

			if (withAttributesPath != null) {
				if (enveloped) {
					withAttributesPath.updateSyntax(reparser, false);
					reparser.updateLocation(withAttributesPath.getLocation());
				} else if (reparser.envelopsDamage(withAttributesPath.getLocation())) {
					withAttributesPath.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(withAttributesPath.getLocation());
				}
			}

			if (!enveloped) {
				throw new ReParseException();
			}

			return;
		}

		reparser.updateLocation(identifier.getLocation());

		if (formalParList != null) {
			formalParList.updateSyntax(reparser, false);
			reparser.updateLocation(formalParList.getLocation());
		}

		if (returnType != null) {
			returnType.updateSyntax(reparser, false);
			reparser.updateLocation(returnType.getLocation());
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (formalParList != null) {
			formalParList.findReferences(referenceFinder, foundIdentifiers);
		}
		if (returnType != null) {
			returnType.findReferences(referenceFinder, foundIdentifiers);
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
		if (returnType != null && !returnType.accept(v)) {
			return false;
		}
		return true;
	}
}
