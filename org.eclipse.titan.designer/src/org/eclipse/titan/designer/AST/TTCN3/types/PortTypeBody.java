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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.BridgingNamedNode;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.PortTypeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.TypeMapping;
import org.eclipse.titan.designer.AST.TTCN3.attributes.TypeMappings;
import org.eclipse.titan.designer.AST.TTCN3.attributes.UserPortTypeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute.ExtensionAttribute_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeAnalyzer;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class PortTypeBody extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private static final String FULLNAMEPART1 = ".<in_list>";
	private static final String FULLNAMEPART2 = ".<out_list>";
	private static final String FULLNAMEPART3 = ".<inout_list>";
	private static final String FULLNAMEPART4 = ".<incoming_signatures>";
	private static final String FULLNAMEPART5 = ".<outgoing_signatures>";
	private static final String FULLNAMEPART6 = ".<incoming_messages>";
	private static final String FULLNAMEPART7 = ".<outgoing_messages>";

	private static final String REDUNDANTINALL = "Redundant `in all' and `inout all'";
	private static final String REDUNDANTOUTALL = "Redundant `out all' and `inout all' directives";
	private static final String UNSUPPORTEDINOUTALL = "Unsupported `inout all' directive was ignored";
	private static final String UNSUPPORTEDINALL = "Unsupported `in all' directive was ignored";
	private static final String UNSUPPORTEDOUTALL = "Unsupported `out all' directive was ignored";
	private static final String SIGNATUREONMESSAGEPORT = "Signature `{0}'' cannot be used on a message based port";
	private static final String DATAONPROCEDUREPORT = "Data type `{0}'' cannot be {1} on procedure based port";
	private static final String DUPLICATEDINSIGNATURE = "Duplicate incoming signature `{0}''";
	private static final String DUPLICATEDOUTSIGNATURE = "Duplicate outgoing signature `{0}''";
	private static final String DUPLICATEDINMESSAGE = "Duplicate incoming message type `{0}''";
	private static final String DUPLICATEDOUTMESSAGE = "Duplicate outgoing message type `{0}''";

	public enum OperationModes {
		OP_Message, OP_Procedure, OP_Mixed
	}

	public enum TestPortAPI_type {
		/* regular test port API */								TP_REGULAR,
		/* no test port (only connection allowed)*/	TP_INTERNAL,
		/* usage of the address type is supported*/	TP_ADDRESS
	}

	public enum PortType_type {
		/* regular port type*/																		PT_REGULAR,
		/* provides the external interface for other port types*/				PT_PROVIDER,
		/* the port type uses another port type as external interface */	PT_USER
	}

	private final OperationModes operationMode;
	private TestPortAPI_type testportType;
	private PortType_type portType;

	private Port_Type myType;

	private List<IType> inTypes = null;
	private boolean inAll = false;
	private List<IType> outTypes = null;
	private boolean outAll = false;
	private List<IType> inoutTypes = null;
	private boolean inoutAll = false;

	private TypeSet inMessages;
	private TypeSet outMessages;
	private TypeSet inSignatures;
	private TypeSet outSignatures;

	private Reference providerReference;
	private IType providerType;
	private TypeMappings inMappings;
	private TypeMappings outMappings;

	/** the time when this assignment was checked the last time. */
	private CompilationTimeStamp lastTimeChecked;
	private CompilationTimeStamp lastTimeAttributesChecked;

	/**
	 * The location of the whole statement. This location encloses the statement
	 * fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	public PortTypeBody(final OperationModes operationMode) {
		this.operationMode = operationMode;
		testportType = TestPortAPI_type.TP_REGULAR;
		portType = PortType_type.PT_REGULAR;
	}

	public void setMyType(final Port_Type myType) {
		this.myType = myType;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (inTypes != null) {
			for (int i = 0, size = inTypes.size(); i < size; i++) {
				if (inTypes.get(i) == child) {
					return builder.append(FULLNAMEPART1);
				}
			}
		}
		if (outTypes != null) {
			for (int i = 0, size = outTypes.size(); i < size; i++) {
				if (outTypes.get(i) == child) {
					return builder.append(FULLNAMEPART2);
				}
			}
		}
		if (inoutTypes != null) {
			for (int i = 0, size = inoutTypes.size(); i < size; i++) {
				if (inoutTypes.get(i) == child) {
					return builder.append(FULLNAMEPART3);
				}
			}
		}

		if (inMessages == child) {
			return builder.append(FULLNAMEPART4);
		} else if (outMessages == child) {
			return builder.append(FULLNAMEPART5);
		} else if (inSignatures == child) {
			return builder.append(FULLNAMEPART6);
		} else if (outSignatures == child) {
			return builder.append(FULLNAMEPART7);
		}
		if (providerReference == child) {
			return builder.append(".<provider_ref>");
		}
		if (inMappings == child) {
			return builder.append(".<inMappings>");
		}
		if (outMappings == child) {
			return builder.append(".<outMappings>");
		}

		return builder;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	public void addInTypes(final List<IType> types) {
		if (types == null) {
			inAll = true;
		} else {
			if (inTypes == null) {
				inTypes = new ArrayList<IType>();
			}
			inTypes.addAll(types);
		}
	}

	public void addOutTypes(final List<IType> types) {
		if (types == null) {
			outAll = true;
		} else {
			if (outTypes == null) {
				outTypes = new ArrayList<IType>();
			}
			outTypes.addAll(types);
		}
	}

	public void addInoutTypes(final List<IType> types) {
		if (types == null) {
			inoutAll = true;
		} else {
			if (inoutTypes == null) {
				inoutTypes = new ArrayList<IType>();
			}
			inoutTypes.addAll(types);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		if (inTypes != null) {
			for (int i = 0, size = inTypes.size(); i < size; i++) {
				inTypes.get(i).setMyScope(scope);
			}
		}
		if (outTypes != null) {
			for (int i = 0, size = outTypes.size(); i < size; i++) {
				outTypes.get(i).setMyScope(scope);
			}
		}
		if (inoutTypes != null) {
			for (int i = 0, size = inoutTypes.size(); i < size; i++) {
				inoutTypes.get(i).setMyScope(scope);
			}
		}
		if (providerReference != null) {
			providerReference.setMyScope(scope);
		}
		if (inMappings != null) {
			inMappings.setMyScope(scope);
		}
		if (outMappings != null) {
			outMappings.setMyScope(scope);
		}
	}

	public OperationModes getOperationMode() {
		return operationMode;
	}

	public TestPortAPI_type getTestportType() {
		return testportType;
	}

	public PortType_type getPortType() {
		return portType;
	}

	public IType getProviderType() {
		return providerType;
	}

	/**
	 * @return a set of those message types than be received on this port
	 * */
	public TypeSet getInMessages() {
		return inMessages;
	}

	/**
	 * @return a set of those message types than be sent on this port
	 * */
	public TypeSet getOutMessage() {
		return outMessages;
	}

	/**
	 * @return a set of those signature types than be received on this port
	 * */
	public TypeSet getInSignatures() {
		return inSignatures;
	}

	/**
	 * @return a set of those signature types than be sent on this port
	 * */
	public TypeSet getOutSignatures() {
		return outSignatures;
	}

	/** @returns true if this port is internal, false otherwise */
	public boolean isInternal() {
		return TestPortAPI_type.TP_INTERNAL.equals(testportType);
	}

	/**
	 * Calculates the address type that can be used in communication operations on this port type.
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 *
	 * @return   null is returned if addressing inside SUT is not supported or the address type does not exist.
	 * */
	public IType getAddressType(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked == null || !TestPortAPI_type.TP_ADDRESS.equals(testportType)) {
			return null;
		}

		IType t = null;
		// in case of 'user' port types the address visible and supported by the 'provider' port type is relevant
		if (PortType_type.PT_USER.equals(portType) && providerType != null) {
			t = providerType;
		} else {
			t = myType;
		}

		return ((TTCN3Module) t.getMyScope().getModuleScope()).getAddressType(timestamp);
	}

	/**
	 * Marks that this port type body belongs to a provider port.
	 * Also clears all mappings set previously, in case of errors.
	 * */
	private void addProviderAttribute() {
		portType = PortType_type.PT_PROVIDER;
		providerReference = null;
		providerType = null;
		inMappings = null;
		outMappings = null;
	}

	/**
	 * Marks that this port type body belongs to a user port.
	 * Also sets all mappings using the provided data.
	 *
	 * @param providerReference the reference pointing to the provider port
	 * @param inMappings the incoming mappings.
	 * @param outMappings the outgoing mappings.
	 * */
	public void addUserAttribute(final Reference providerReference, final TypeMappings inMappings, final TypeMappings outMappings) {
		portType = PortType_type.PT_USER;
		this.providerReference = providerReference;
		this.providerReference.setFullNameParent(new BridgingNamedNode(this, ".<provider_ref>"));
		this.providerReference.setMyScope(myType.getMyScope());
		providerType = null;

		this.inMappings = inMappings;
		if (inMappings != null) {
			this.inMappings.setFullNameParent(new BridgingNamedNode(this, ".<inMappings>"));
			this.inMappings.setMyScope(myType.getMyScope());
		}

		this.outMappings = outMappings;
		if (outMappings != null) {
			this.outMappings.setFullNameParent(new BridgingNamedNode(this, ".<outMappings>"));
			this.outMappings.setMyScope(myType.getMyScope());
		}
	}

	/**
	 * Does the semantic checking of the body of the port type.
	 * Essentially this is the semantic checking of the port type, minus attributes.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		inMessages = null;
		outMessages = null;
		inSignatures = null;
		outSignatures = null;
		lastTimeChecked = timestamp;

		if (inoutAll) {
			if (inAll) {
				location.reportSemanticWarning(REDUNDANTINALL);
				inAll = false;
			}
			if (outAll) {
				location.reportSemanticWarning(REDUNDANTOUTALL);
				outAll = false;
			}

			location.reportSemanticWarning(UNSUPPORTEDINOUTALL);
		} else {
			if (inAll) {
				location.reportSemanticWarning(UNSUPPORTEDINALL);
			}
			if (outAll) {
				location.reportSemanticWarning(UNSUPPORTEDOUTALL);
			}
		}

		if (inTypes != null) {
			checkList(timestamp, inTypes, true, false);
		}
		if (outTypes != null) {
			checkList(timestamp, outTypes, false, true);
		}
		if (inoutTypes != null) {
			checkList(timestamp, inoutTypes, true, true);
		}
	}

	/**
	 * Checks the attributes for the specific case when the port is of user type.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * */
	private void checkUserAttribute(final CompilationTimeStamp timestamp) {
		if (providerReference == null) {
			return;
		}

		providerType = null;
		PortTypeBody providerBody = null;
		Assignment assignment = providerReference.getRefdAssignment(timestamp, true);
		if (assignment != null) {
			if (Assignment_type.A_TYPE.equals(assignment.getAssignmentType())) {
				IType type = assignment.getType(timestamp).getTypeRefdLast(timestamp);
				if (Type_type.TYPE_PORT.equals(type.getTypetype())) {
					providerType = type;
					providerBody = ((Port_Type) type).getPortBody();
				} else {
					providerReference.getLocation().reportSemanticError(
							MessageFormat.format("Type reference `{0}'' does not refer to a port type", providerReference.getDisplayName()));
				}
			} else {
				providerReference.getLocation().reportSemanticError(
						MessageFormat.format("Reference `{0}'' does not refer to a type", providerReference.getDisplayName()));
			}
		}

		// checking the consistency of attributes in this and provider_body
		if (providerBody != null && !TestPortAPI_type.TP_INTERNAL.equals(testportType)) {
			if (!PortType_type.PT_PROVIDER.equals(providerBody.portType)) {
				providerReference.getLocation().reportSemanticError(
						MessageFormat.format("The referenced port type `{0}'' must have the `provider'' attribute", providerType.getTypename()));
			}
			switch (providerBody.testportType) {
			case TP_REGULAR:
				if (TestPortAPI_type.TP_ADDRESS.equals(testportType)) {
					providerReference.getLocation().reportSemanticError(
							MessageFormat.format("Attribute `address'' cannot be used because the provider port type `{0}''"
									+ " does not have attribute `address''", providerType.getTypename()));
				}
				break;
			case TP_INTERNAL:
				providerReference.getLocation().reportSemanticError(
						MessageFormat.format("Missing attribute `internal''. Provider port type `{0}'' has attribute `internal'',"
								+ " which must be also present here", providerType.getTypename()));
				break;
			case TP_ADDRESS:
				break;
			default:
				break;
			}
			// inherit the test port API type from the provider
			testportType = providerBody.testportType;
		}

		// check the incoming mappings
		if (inMappings != null && inMappings.getNofMappings() != 0) {
			inMappings.check(timestamp);

			if (providerBody != null) {
				if (providerBody.inMessages != null) {
					// check if all source types are present on the `in' list of the provider
					for (int i = 0, size = inMappings.getNofMappings(); i < size; i++) {
						Type sourceType = inMappings.getMappingByIndex(i).getSourceType();
//						if(sourceType == null) {
//							inMappings.getMappingByIndex(i).getLocation().reportSemanticError(MessageFormat.format(
//							"Source type of the `in'' mapping is unknown"
//							 + " on the list of incoming messages in provider port type `{0}''", providerType.getTypename() ));
//							continue;
//						}
						if (sourceType != null && !providerBody.inMessages.hasType(timestamp, sourceType)) {
							sourceType.getLocation().reportSemanticError(MessageFormat.format(
											"Source type `{0}'' of the `in'' mapping is not present "
											+ "on the list of incoming messages in provider port type `{1}''",
											sourceType.getTypename(), providerType.getTypename()));
						}
					}

					// check if all types of the `in' list of the provider are handled by the mappings
					for (int i = 0, size = providerBody.inMessages.getNofTypes(); i < size; i++) {
						IType messageType = providerBody.inMessages.getTypeByIndex(i);
						if (!inMappings.hasMappingForType(timestamp, messageType)) {
							inMappings.getLocation().reportSemanticError(MessageFormat.format(
									"Incoming message type `{0}'' of provider port type `{1}'' is not handled by the incoming mappings",
									messageType.getTypename(), providerType.getTypename()));
							inMappings.hasMappingForType(timestamp, messageType);
						}
					}
				} else {
					inMappings.getLocation().reportSemanticError(MessageFormat.format(
							"Invalid incoming mappings. Provider port type `{0}' does not have incoming message types'",
							providerType.getTypename()));
				}
			}

			// checking target types
			for (int i = 0, size = inMappings.getNofMappings(); i < size; i++) {
				TypeMapping mapping = inMappings.getMappingByIndex(i);
				for (int j = 0, nofTargets = mapping.getNofTargets(); i < nofTargets; i++) {
					Type targetType = mapping.getTargetByIndex(j).getTargetType();
					if (targetType != null && (inMessages == null || !inMessages.hasType(timestamp, targetType))) {
						targetType.getLocation().reportSemanticError(MessageFormat.format(
								"Target type `{0}'' of the `in'' mapping is not present on the list of incoming messages in user port type `{1}''",
								targetType.getTypename(), myType.getTypename()));
					}
				}
			}
		} else if (providerBody != null && providerBody.inMessages != null) {
			location.reportSemanticError(MessageFormat.format(
					"Missing `in'' mappings to handle the incoming message types of provider port type `{0}''", providerType.getTypename()));
		}

		if (outMappings != null && outMappings.getNofMappings() != 0) {
			outMappings.check(timestamp);

			if (outMessages != null) {
				// check if all source types are present on the `in' list of the provider
				for (int i = 0, size = outMappings.getNofMappings(); i < size; i++) {
					Type sourceType = outMappings.getMappingByIndex(i).getSourceType();
					if (sourceType != null && !outMessages.hasType(timestamp, sourceType)) {
						sourceType.getLocation().reportSemanticError(MessageFormat.format(
								"Source type `{0}'' of the `out'' mapping is not present on the list of outgoing messages in user port type `{1}''",
								sourceType.getTypename(), myType.getTypename()));
					}
				}

				// check if all types of the `in' list of the provider are handled by the mappings
				for (int i = 0, size = outMessages.getNofTypes(); i < size; i++) {
					IType messageType = outMessages.getTypeByIndex(i);
					if (!outMappings.hasMappingForType(timestamp, messageType)) {
						outMappings.getLocation().reportSemanticError(MessageFormat.format(
								"Outgoing message type `{0}'' of user port type `{1}'' is not handled by the outgoing mappings",
								messageType.getTypename(), myType.getTypename()));
					}
				}
			} else {
				outMappings.getLocation().reportSemanticError(MessageFormat.format(
						"Invalid outgoing mappings. User port type `{0}'' does not have outgoing message types", myType.getTypename()));
			}

			// checking target types
			if (providerBody != null) {
				for (int i = 0, size = outMappings.getNofMappings(); i < size; i++) {
					TypeMapping mapping = outMappings.getMappingByIndex(i);
					for (int j = 0, nofTargets = mapping.getNofTargets(); i < nofTargets; i++) {
						Type targetType = mapping.getTargetByIndex(j).getTargetType();
						if (targetType != null && (providerBody.outMessages == null || !providerBody.outMessages.hasType(timestamp, targetType))) {
							targetType.getLocation().reportSemanticError(MessageFormat.format(
									"Target type `{0}'' of the `out'' mapping is not present "
									+ "on the list of outgoing messages in provider port type `{1}''",
									targetType.getTypename(), providerType.getTypename()));
						}
					}
				}
			}
		} else if (outMessages != null) {
			location.reportSemanticError(MessageFormat.format(
					"Missing `out'' mapping to handle the outgoing message types of user port type `{0}''", myType.getTypename()));
		}

		// checking the compatibility of signature lists
		if (providerBody == null) {
			return;
		}

		if (inSignatures != null) {
			for (int i = 0, size = inSignatures.getNofTypes(); i < size; i++) {
				IType signatureType = inSignatures.getTypeByIndex(i);
				if (providerBody.inSignatures == null || !providerBody.inSignatures.hasType(timestamp, signatureType)) {
					IType last = signatureType.getTypeRefdLast(timestamp);
					if (!last.getIsErroneous(timestamp) && Type_type.TYPE_SIGNATURE.equals(last.getTypetype())) {
						Signature_Type lastSignature = (Signature_Type) last;
						if (!lastSignature.isNonblocking() || lastSignature.getSignatureExceptions() != null) {
							signatureType.getLocation().reportSemanticError(MessageFormat.format(
									"Incoming signature `{0}'' of user port type `{1}'' is not present on the list "
									+ "of incoming signatures in provider port type `{2}''",
									signatureType.getTypename(), myType.getTypename(), providerType.getTypename()));
						}
					}
				}
			}
		}
		if (providerBody.inSignatures != null) {
			for (int i = 0, size = providerBody.inSignatures.getNofTypes(); i < size; i++) {
				IType signatureType = providerBody.inSignatures.getTypeByIndex(i);
				if (inSignatures == null || !inSignatures.hasType(timestamp, signatureType)) {
					location.reportSemanticError(MessageFormat.format(
							"Incoming signature `{0}'' of provider port type `{1}'' "
							+ "is not present on the list of incoming signatures in user port type `{2}''",
							signatureType.getTypename(), providerType.getTypename(), myType.getTypename()));
				}
			}
		}
		if (outSignatures != null) {
			for (int i = 0, size = outSignatures.getNofTypes(); i < size; i++) {
				IType signatureType = outSignatures.getTypeByIndex(i);
				if (providerBody.outSignatures == null || !providerBody.outSignatures.hasType(timestamp, signatureType)) {
					signatureType.getLocation().reportSemanticError(MessageFormat.format(
							"Outgoing signature `{0}'' of user port type `{1}'' is not present "
							+ "on the list of outgoing signatures in provider port type `{2}''",
							signatureType.getTypename(), myType.getTypename(), providerType.getTypename()));
				}
			}
		}
		if (providerBody.outSignatures != null) {
			for (int i = 0, size = providerBody.outSignatures.getNofTypes(); i < size; i++) {
				IType signatureType = providerBody.outSignatures.getTypeByIndex(i);
				if (outSignatures == null || !outSignatures.hasType(timestamp, signatureType)) {
					IType last = signatureType.getTypeRefdLast(timestamp);
					if (!last.getIsErroneous(timestamp) && Type_type.TYPE_SIGNATURE.equals(last.getTypetype())) {
						Signature_Type lastSignature = (Signature_Type) last;
						if (!lastSignature.isNonblocking() || lastSignature.getSignatureExceptions() != null) {
							location.reportSemanticError(MessageFormat.format(
									"Outgoing signature `{0}'' of provider port type `{1}'' is not present "
									+ "on the list of outgoing signatures in user port type `{2}''",
									signatureType.getTypename(), providerType.getTypename(), myType.getTypename()));
						}
					}
				}
			}
		}
	}

	/**
	 * Does the semantic checking of the attributes assigned to the port type having this body.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param withAttributesPath the withAttributesPath assigned to the port type.
	 * */
	public void checkAttributes(final CompilationTimeStamp timestamp, final WithAttributesPath withAttributesPath) {
		if (lastTimeAttributesChecked != null && !lastTimeAttributesChecked.isLess(timestamp)) {
			return;
		}

		lastTimeAttributesChecked = lastTimeChecked;

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

		//clear the old attributes
		testportType = TestPortAPI_type.TP_REGULAR;
		portType = PortType_type.PT_REGULAR;

		// check the new attributes
		for (int i = 0; i < attributes.size(); i++) {
			ExtensionAttribute extensionAttribute = attributes.get(i);
			if (ExtensionAttribute_type.PORTTYPE.equals(extensionAttribute.getAttributeType())) {
				PortTypeAttribute portAttribute = (PortTypeAttribute) extensionAttribute;
				switch (portAttribute.getPortTypeType()) {
				case INTERNAL:
					switch (testportType) {
					case TP_REGULAR:
						break;
					case TP_INTERNAL:
						extensionAttribute.getLocation().reportSemanticWarning("Duplicate attribute `internal'");
						break;
					case TP_ADDRESS:
						extensionAttribute.getLocation().reportSemanticError("Attributes `address' and `internal' cannot be used at the same time");
						break;
					default:
						break;
					}
					testportType = TestPortAPI_type.TP_INTERNAL;
					break;
				case ADDRESS:
					switch (testportType) {
					case TP_REGULAR:
						break;
					case TP_INTERNAL:
						extensionAttribute.getLocation().reportSemanticError("Attributes `address' and `internal' cannot be used at the same time");
						break;
					case TP_ADDRESS:
						extensionAttribute.getLocation().reportSemanticWarning("Duplicate attribute `address'");
						break;
					default:
						break;
					}
					testportType = TestPortAPI_type.TP_ADDRESS;
					break;
				case PROVIDER:
					switch (portType) {
					case PT_REGULAR:
						break;
					case PT_PROVIDER:
						extensionAttribute.getLocation().reportSemanticWarning("Duplicate attribute `provider'");
						break;
					case PT_USER:
						extensionAttribute.getLocation().reportSemanticError("Attributes `user' and `provider' cannot be used at the same time");
						break;
					default:
						break;
					}
					addProviderAttribute();
					break;
				case USER:
					switch (portType) {
					case PT_REGULAR:
						break;
					case PT_PROVIDER:
						extensionAttribute.getLocation().reportSemanticError("Attributes `provider' and `user' cannot be used at the same time");
						break;
					case PT_USER:
						extensionAttribute.getLocation().reportSemanticError("Duplicate attribute `user'");
						break;
					default:
						break;
					}
					UserPortTypeAttribute user = (UserPortTypeAttribute) portAttribute;
					addUserAttribute(user.getReference(), user.getInMappings(), user.getOutMappings());
					break;
				default:
					break;
				}
			}
		}

		if (PortType_type.PT_USER.equals(portType)) {
			checkUserAttribute(timestamp);
		} else if (TestPortAPI_type.TP_ADDRESS.equals(testportType)) {
			TTCN3Module module = (TTCN3Module) myType.getMyScope().getModuleScope();
			if (module.getAddressType(timestamp) == null) {
				location.reportSemanticError(MessageFormat.format("Type `address'' is not defined in module `{0}''", module.getIdentifier().getDisplayName()));
			}
		}
	}

	/**
	 * Checks a list of types.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle
	 * @param list the list of types to check
	 * @param isIn is the list an in or inout list.
	 * @param isOut is the list an out or inout list.
	 * */
	private void checkList(final CompilationTimeStamp timestamp, final List<IType> list, final boolean isIn, final boolean isOut) {
		String errorMessage;
		if (isIn) {
			if (isOut) {
				errorMessage = "sent or received";
			} else {
				errorMessage = "received";
			}
		} else {
			errorMessage = "sent";
		}

		for (int i = 0, size = list.size(); i < size; i++) {
			IType type = list.get(i);
			type.check(timestamp);

			if (type.isComponentInternal(timestamp)) {
				//check if a value or template of this type can leave the component.
				Set<IType> typeSet = new HashSet<IType>();
				type.checkComponentInternal(timestamp, typeSet, "sent or received on a port");
			}

			IType last = type.getTypeRefdLast(timestamp);
			if (last != null && !last.getIsErroneous(timestamp)) {
				switch (last.getTypetype()) {
				case TYPE_SIGNATURE:
					if (OperationModes.OP_Message.equals(operationMode)) {
						type.getLocation().reportSemanticError(MessageFormat.format(SIGNATUREONMESSAGEPORT, last.getTypename()));
					}
					if (isIn) {
						if (inSignatures != null && inSignatures.hasType(timestamp, last)) {
							type.getLocation().reportSemanticError(MessageFormat.format(DUPLICATEDINSIGNATURE, last.getTypename()));
						} else {
							if (inSignatures == null) {
								inSignatures = new TypeSet();
								inSignatures.setFullNameParent(this);
							}
							inSignatures.addType(type);
						}
					}
					if (isOut) {
						if (outSignatures != null && outSignatures.hasType(timestamp, last)) {
							type.getLocation().reportSemanticError(MessageFormat.format(DUPLICATEDOUTSIGNATURE, last.getTypename()));
						} else {
							if (outSignatures == null) {
								outSignatures = new TypeSet();
								outSignatures.setFullNameParent(this);
							}
							outSignatures.addType(type);
						}
					}
					break;
				default:
					if (OperationModes.OP_Procedure.equals(operationMode)) {
						type.getLocation().reportSemanticError(MessageFormat.format(DATAONPROCEDUREPORT, last.getTypename(), errorMessage));
					}
					if (isIn) {
						if (inMessages != null && inMessages.hasType(timestamp, last)) {
							type.getLocation().reportSemanticError(MessageFormat.format(DUPLICATEDINMESSAGE, type.getTypename()));
						} else {
							if (inMessages == null) {
								inMessages = new TypeSet();
								inMessages.setFullNameParent(this);
							}

							inMessages.addType(type);
						}
					}
					if (isOut) {
						if (outMessages != null && outMessages.hasType(timestamp, last)) {
							type.getLocation().reportSemanticError(MessageFormat.format(DUPLICATEDOUTMESSAGE, type.getTypename()));
						} else {
							if (outMessages == null) {
								outMessages = new TypeSet();
								outMessages.setFullNameParent(this);
							}

							outMessages.addType(type);
						}
					}
					break;
				}
			}
		}
	}

	/**
	 * Checks if the port of this port type body has a queue or not. A queue is
	 * only used if there is at least one blocking signature, or at least one
	 * signature with exceptions.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 *
	 * @return true if the port has a queue, false otherwise
	 * */
	public boolean hasQueue(final CompilationTimeStamp timestamp) {
		check(timestamp);

		if (inMessages != null || inSignatures != null) {
			return true;
		}

		if (outSignatures != null) {
			for (int i = 0, size = outSignatures.getNofTypes(); i < size; i++) {
				Signature_Type signature = (Signature_Type) outSignatures.getTypeByIndex(i).getTypeRefdLast(timestamp);
				if (!signature.isNonblocking() || signature.getSignatureExceptions() != null) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks if a getreply operation can be used on the port this body belongs
	 * to.
	 *
	 * @param timestamp the timestamp of the actual semantic cycle
	 *
	 * @return true if there is at least one outgoing signature which is not
	 *         blocking, false otherwise
	 * */
	public boolean getreplyAllowed(final CompilationTimeStamp timestamp) {
		check(timestamp);

		if (outSignatures != null) {
			IType tempType = null;
			for (int i = 0, size = outSignatures.getNofTypes(); i < size; i++) {
				tempType = outSignatures.getTypeByIndex(i).getTypeRefdLast(timestamp);
				if (!((Signature_Type) tempType).isNonblocking()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks if a catch operation can be used on the port this body belongs to.
	 *
	 * @param timestamp the timestamp of the actual semantic cycle.
	 *
	 * @return true if there is at least one outgoing signature which can throw
	 *         an exception, false otherwise
	 * */
	public boolean catchAllowed(final CompilationTimeStamp timestamp) {
		check(timestamp);

		if (outSignatures != null) {
			IType tempType = null;
			for (int i = 0, size = outSignatures.getNofTypes(); i < size; i++) {
				tempType = outSignatures.getTypeByIndex(i).getTypeRefdLast(timestamp);
				if (((Signature_Type) tempType).getSignatureExceptions() != null) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks if this as the type of a test component port can be mapped to a
	 * system port with an other port type.
	 *
	 * @param timestamp the timestamp of the actual semantic cycle.
	 * @param other the other port type body to compare to.
	 *
	 * @return true if this as the type of a test component port can be mapped
	 *         to a system port with an other port type, false otherwise.
	 * */
	public boolean isMappable(final CompilationTimeStamp timestamp, final PortTypeBody other) {
		if (this == other) {
			return true;
		}

		// the outgoing lists should be covered by the other port
		if (outMessages != null) {
			if (other.outMessages == null) {
				return false;
			}
			for (int i = 0, size = outMessages.getNofTypes(); i < size; i++) {
				if (!other.outMessages.hasType(timestamp, outMessages.getTypeByIndex(i))) {
					return false;
				}
			}
		}

		if (outSignatures != null) {
			if (other.outSignatures == null) {
				return false;
			}
			for (int i = 0, size = outSignatures.getNofTypes(); i < size; i++) {
				if (!other.outSignatures.hasType(timestamp, outSignatures.getTypeByIndex(i))) {
					return false;
				}
			}
		}

		// the incoming list of the other should be covered by local incoming lists.
		if (other.inMessages != null) {
			if (inMessages == null) {
				return false;
			}
			for (int i = 0, size = other.inMessages.getNofTypes(); i < size; i++) {
				if (!inMessages.hasType(timestamp, other.inMessages.getTypeByIndex(i))) {
					return false;
				}
			}
		}

		if (other.inSignatures != null) {
			if (inSignatures == null) {
				return false;
			}
			for (int i = 0, size = other.inSignatures.getNofTypes(); i < size; i++) {
				if (!inSignatures.hasType(timestamp, other.inSignatures.getTypeByIndex(i))) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Reports all errors that prevent mapping of this as the type of a test
	 * component port to system port an other port type.
	 *
	 * @param timestamp the timestamp of the actual semantic cycle.
	 * @param other the other port type body to compare to.
	 * */
	public void reportMappingErrors(final CompilationTimeStamp timestamp, final PortTypeBody other) {
		if (outMessages != null) {
			for (int i = 0, size = outMessages.getNofTypes(); i < size; i++) {
				IType messageType = outMessages.getTypeByIndex(i);
				if (other.outMessages == null || !other.outMessages.hasType(timestamp, messageType)) {
					messageType.getLocation().reportSemanticError(MessageFormat.format(
							"Outgoing message type `{0}'' of test component port type `{1}'' is not present on the outgoing list of system port type `{2}''"
							, messageType.getTypename(), myType.getTypename(), other.myType.getTypename()));
				}
			}
		}

		if (outSignatures != null) {
			for (int i = 0, size = outSignatures.getNofTypes(); i < size; i++) {
				IType signatureType = outSignatures.getTypeByIndex(i);
				if (other.outSignatures == null || !other.outSignatures.hasType(timestamp, signatureType)) {
					signatureType.getLocation().reportSemanticError(MessageFormat.format(
							"Outgoing signature type `{0}'' of test component port type `{1}'' is not present on the outgoing list of system port type `{2}''"
							, signatureType.getTypename(), myType.getTypename(), other.myType.getTypename()));
				}
			}
		}

		if (other.inMessages != null) {
			for (int i = 0, size = other.inMessages.getNofTypes(); i < size; i++) {
				IType messageType = other.inMessages.getTypeByIndex(i);
				if (inMessages == null || !inMessages.hasType(timestamp, messageType)) {
					messageType.getLocation().reportSemanticError(MessageFormat.format(
							"Incoming message type `{0}'' of system port type `{1}'' is not present on the incoming list of test component port type `{2}''"
							, messageType.getTypename(), other.myType.getTypename(), myType.getTypename()));
				}
			}
		}

		if (other.inSignatures != null) {
			for (int i = 0, size = other.inSignatures.getNofTypes(); i < size; i++) {
				IType signatureType = other.inSignatures.getTypeByIndex(i);
				if (inSignatures == null || !inSignatures.hasType(timestamp, signatureType)) {
					signatureType.getLocation().reportSemanticError(MessageFormat.format(
							"Incoming signature type `{0}'' of system port type `{1}'' is not present on the incoming list of test component port type `{2}''"
							, signatureType.getTypename(), other.myType.getTypename(), myType.getTypename()));
				}
			}
		}
	}

	/**
	 * Checks if the outgoing messages and signatures of this are on the
	 * incoming lists of the other port type body.
	 *
	 * @param timestamp the timestamp of the actual semantic cycle.
	 * @param other the other port type body to compare to.
	 *
	 * @return true if the outgoing messages and signatures of this are on the
	 *         incoming lists of the other port type body, false otherwise.
	 * */
	public boolean isConnectable(final CompilationTimeStamp timestamp, final PortTypeBody other) {
		if (outMessages != null) {
			if (other.inMessages == null) {
				return false;
			}
			for (int i = 0, size = outMessages.getNofTypes(); i < size; i++) {
				if (!other.inMessages.hasType(timestamp, outMessages.getTypeByIndex(i))) {
					return false;
				}
			}
		}

		if (outSignatures != null) {
			if (other.inSignatures == null) {
				return false;
			}
			for (int i = 0, size = outSignatures.getNofTypes(); i < size; i++) {
				if (!other.inSignatures.hasType(timestamp, outSignatures.getTypeByIndex(i))) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Reports the error messages about the outgoing types of this that are not
	 * handled by the incoming lists of the other port type.
	 *
	 * @param timestamp the timestamp of the actual semantic cycle.
	 * @param other the other port type body to compare to.
	 * */
	public void reportConnectionErrors(final CompilationTimeStamp timestamp, final PortTypeBody other) {
		if (outMessages != null) {
			for (int i = 0, size = outMessages.getNofTypes(); i < size; i++) {
				IType messageType = outMessages.getTypeByIndex(i);
				if (other.inMessages == null || !other.inMessages.hasType(timestamp, messageType)) {
					messageType.getLocation().reportSemanticError(MessageFormat.format(
							"Outgoing message type `{0}'' of port type `{1}'' is not present on the incoming list of port type `{2}''"
							, messageType.getTypename(), myType.getTypename(), other.myType.getTypename()));
				}
			}
		}

		if (outSignatures != null) {
			for (int i = 0, size = outSignatures.getNofTypes(); i < size; i++) {
				IType signatureType = outSignatures.getTypeByIndex(i);
				if (other.inSignatures == null || !other.inSignatures.hasType(timestamp, outSignatures.getTypeByIndex(i))) {
					signatureType.getLocation().reportSemanticError(MessageFormat.format(
							"Outgoing signature type `{0}'' of port type `{1}'' is not present on the incoming list of port type `{2}''"
							, signatureType.getTypename(), myType.getTypename(), other.myType.getTypename()));
				}
			}
		}
	}

	/**
	 * Adds the port related proposals.
	 *
	 * handles the following proposals:
	 *
	 * <ul>
	 * <li>in message mode:
	 * <ul>
	 * <li>send(template), receive, trigger
	 * </ul>
	 * <li>in procedure mode:
	 * <ul>
	 * <li>call, getcall, reply, raise, getreply, catch
	 * </ul>
	 * <li>general:
	 * <ul>
	 * <li>check
	 * <li>clear, start, stop, halt
	 * </ul>
	 * </ul>
	 *
	 * @param propCollector the proposal collector.
	 * @param i the index of a part of the full reference, for which we wish to find completions.
	 * */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() != i + 1 || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (OperationModes.OP_Message.equals(operationMode) || OperationModes.OP_Mixed.equals(operationMode)) {
			propCollector.addTemplateProposal("send", new Template("send( templateInstance )", "", propCollector.getContextIdentifier(),
					"send( ${templateInstance} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("send", new Template("send( templateInstance ) to location", "", propCollector.getContextIdentifier(),
					"send( ${templateInstance} ) to ${location};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);

			addReceiveProposals(propCollector, i);
			addTriggerProposals(propCollector, i);
		}

		if (OperationModes.OP_Procedure.equals(operationMode) || OperationModes.OP_Mixed.equals(operationMode)) {
			propCollector.addTemplateProposal("call", new Template("call( templateInstance )", "", propCollector.getContextIdentifier(),
					"call( ${templateInstance} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("call", new Template("call( templateInstance , callTimer )", "with timer", propCollector
					.getContextIdentifier(), "call( ${templateInstance} , ${callTimer} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("call", new Template("call( templateInstance ) to location", "with to clause", propCollector
					.getContextIdentifier(), "call( ${templateInstance} ) to ${location};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("call", new Template("call( templateInstance , callTimer ) to location", "with timer and to clause",
					propCollector.getContextIdentifier(), "call( ${templateInstance} , ${callTimer} ) to ${location};", false),
					TTCN3CodeSkeletons.SKELETON_IMAGE);

			addGetcallProposals(propCollector, i);

			propCollector.addTemplateProposal("reply", new Template("reply( templateInstance )", "", propCollector.getContextIdentifier(),
					"reply( ${templateInstance} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("reply", new Template("reply( templateInstance ) to location", "",
					propCollector.getContextIdentifier(), "reply( ${templateInstance} ) to ${location};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);

			addGetreplyProposals(propCollector, i);

			propCollector.addTemplateProposal("raise", new Template("raise( signature, templateInstance )", "", propCollector.getContextIdentifier(),
					"raise( ${signature}, ${templateInstance} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("raise", new Template("raise( signature, templateInstance ) to location", "with to clause",
					propCollector.getContextIdentifier(), "raise( ${signature}, ${templateInstance} ) to ${location};", false),
					TTCN3CodeSkeletons.SKELETON_IMAGE);

			addCatchProposals(propCollector, i);
		}

		addCheckProposals(propCollector, i);

		propCollector.addProposal("clear;", "clear", ImageCache.getImage("port.gif"), "");
		propCollector.addProposal("start;", "start", ImageCache.getImage("port.gif"), "");
		propCollector.addProposal("stop;", "stop", ImageCache.getImage("port.gif"), "");
		propCollector.addProposal("halt;", "halt", ImageCache.getImage("port.gif"), "");
	}

	public static void addAnyorAllProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (i != 0 || subrefs.isEmpty() || Subreference_type.arraySubReference.equals(subrefs.get(0).getReferenceType())) {
			return;
		}

		String fakeModuleName = propCollector.getReference().getModuleIdentifier().getDisplayName();

		if ("any port".equals(fakeModuleName)) {
			addReceiveProposals(propCollector, i);
			addTriggerProposals(propCollector, i);
			addGetcallProposals(propCollector, i);
			addGetreplyProposals(propCollector, i);
			addCatchProposals(propCollector, i);
			addCheckProposals(propCollector, i);
		} else if ("all port".equals(fakeModuleName)) {
			propCollector.addProposal("clear;", "clear", ImageCache.getImage("port.gif"), "");
			propCollector.addProposal("start;", "start", ImageCache.getImage("port.gif"), "");
			propCollector.addProposal("stop;", "stop", ImageCache.getImage("port.gif"), "");
			propCollector.addProposal("halt;", "halt", ImageCache.getImage("port.gif"), "");
		}
	}

	/**
	 * Adds the "receive" related operations of port types, to the completion
	 * list.
	 *
	 * @param propCollector the proposal collector
	 * @param i index, not used
	 * */
	private static void addReceiveProposals(final ProposalCollector propCollector, final int i) {
		propCollector.addProposal("receive", "receive", ImageCache.getImage("port.gif"), "");
		propCollector.addTemplateProposal("receive", new Template("receive -> value myVar", "value redirect", propCollector.getContextIdentifier(),
				"receive -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("receive", new Template("receive -> sender myPeer", "sender redirect",
				propCollector.getContextIdentifier(), "receive -> sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("receive", new Template("receive -> value myVar sender myPeer", "value and sender redirect", propCollector
				.getContextIdentifier(), "receive -> value ${myVar} sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("receive", new Template("receive from myPeer", "from clause", propCollector.getContextIdentifier(),
				"receive from ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("receive", new Template("receive from myPeer -> value myVar", "from clause with value redirect",
				propCollector.getContextIdentifier(), "receive from ${myPeer} -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("receive", new Template("receive( templateInstance )", "", propCollector.getContextIdentifier(),
				"receive( ${template} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("receive", new Template("receive( templateInstance ) -> value myVar", "value redirect", propCollector
				.getContextIdentifier(), "receive( ${templateInstance} ) -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("receive", new Template("receive( templateInstance ) -> sender myPeer", "sender redirect", propCollector
				.getContextIdentifier(), "receive( ${templateInstance} ) -> sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("receive", new Template("receive( templateInstance ) -> value myVar sender myPeer",
				"value and sender redirect", propCollector.getContextIdentifier(),
				"receive( ${templateInstance} ) -> value ${myVar} sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("receive", new Template("receive( templateInstance ) from myPeer", "from clause", propCollector
				.getContextIdentifier(), "receive( ${templateInstance} ) from ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("receive", new Template("receive( templateInstance ) from myPeer -> value myVar",
				"from clause with value redirect", propCollector.getContextIdentifier(),
				"receive( ${templateInstance} ) from ${myPeer} -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
	}

	/**
	 * Adds the "trigger" related operations of port types, to the completion
	 * list.
	 *
	 * @param propCollector the proposal collector
	 * @param i index, not used
	 * */
	public static void addTriggerProposals(final ProposalCollector propCollector, final int i) {
		propCollector.addProposal("trigger", "trigger", ImageCache.getImage("port.gif"), "");
		propCollector.addTemplateProposal("trigger", new Template("trigger -> value myVar", "value redirect", propCollector.getContextIdentifier(),
				"trigger -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("trigger", new Template("trigger -> sender myPeer", "sender redirect",
				propCollector.getContextIdentifier(), "trigger -> sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("trigger", new Template("trigger -> value myVar sender myPeer", "value and sender redirect", propCollector
				.getContextIdentifier(), "trigger -> value ${myVar} sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("trigger", new Template("trigger from myPeer", "from clause", propCollector.getContextIdentifier(),
				"trigger from ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("trigger", new Template("trigger from myPeer -> value myVar", "from clause with value redirect",
				propCollector.getContextIdentifier(), "trigger from ${myPeer} -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("trigger", new Template("trigger( templateInstance )", "", propCollector.getContextIdentifier(),
				"trigger( ${templateInstance} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("trigger", new Template("trigger( templateInstance ) -> value myVar", "value redirect", propCollector
				.getContextIdentifier(), "trigger( ${templateInstance} ) -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("trigger", new Template("trigger( templateInstance ) -> sender myPeer", "sender redirect", propCollector
				.getContextIdentifier(), "trigger( ${templateInstance} ) -> sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("trigger", new Template("trigger( templateInstance ) -> value myVar sender myPeer",
				"value and sender redirect", propCollector.getContextIdentifier(),
				"trigger( ${templateInstance} ) -> value ${myVar} sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("trigger", new Template("trigger( templateInstance ) from myPeer", "from clause", propCollector
				.getContextIdentifier(), "trigger( ${templateInstance} ) from ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("trigger", new Template("trigger( templateInstance ) from myPeer -> value myVar",
				"from clause with value redirect", propCollector.getContextIdentifier(),
				"trigger( ${templateInstance} ) from ${myPeer} -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);

	}

	/**
	 * Adds the "getcall" related operations of port types, to the completion
	 * list.
	 *
	 * @param propCollector the proposal collector
	 * @param i index, not used
	 * */
	public static void addGetcallProposals(final ProposalCollector propCollector, final int i) {
		propCollector.addProposal("getcall", "getcall", ImageCache.getImage("port.gif"), "");
		propCollector.addTemplateProposal("getcall", new Template("getcall from myPartner", "from clause", propCollector.getContextIdentifier(),
				"getcall from ${myPartner};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("getcall", new Template("getcall -> sender myPartnerVar", "sender redirect", propCollector
				.getContextIdentifier(), "getcall -> sender ${myPartnerVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("getcall", new Template("getcall( templateInstance )", "", propCollector.getContextIdentifier(),
				"getcall( ${templateInstance} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("getcall", new Template("getcall( templateInstance ) from myPartner", "from clause", propCollector
				.getContextIdentifier(), "getcall( ${templateInstance} ) from ${myPartner};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("getcall", new Template("getcall( templateInstance ) -> sender myPartnerVar", "sender redirect",
				propCollector.getContextIdentifier(), "getcall( ${templateInstance} ) -> sender ${myPartnerVar};", false),
				TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("getcall", new Template("getcall( templateInstance ) -> param(parameters)", "parameters", propCollector
				.getContextIdentifier(), "getcall( ${templateInstance} ) -> param( ${parameters} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("getcall", new Template("getcall( templateInstance ) from myPartner -> param(parameters)",
				"from clause and parameters", propCollector.getContextIdentifier(),
				"getcall( ${templateInstance} ) from ${myPartner} -> param( ${parameters} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("getcall", new Template("getcall( templateInstance ) -> param(parameters) sender mySenderVar",
				"parameters and sender clause", propCollector.getContextIdentifier(),
				"getcall( ${templateInstance} ) -> param( ${parameters} ) sender ${mySenderVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
	}

	/**
	 * Adds the "getreply" related operations of port types, to the completion
	 * list.
	 *
	 * @param propCollector the proposal collector
	 * @param i index, not used
	 * */
	public static void addGetreplyProposals(final ProposalCollector propCollector, final int i) {
		propCollector.addProposal("getreply", "getreply", ImageCache.getImage("port.gif"), "");
		propCollector.addTemplateProposal("getreply", new Template("getreply from myPartner", "from clause", propCollector.getContextIdentifier(),
				"getreply from ${myPartner};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("getreply", new Template(
				"getreply( templateInstance ) -> value myReturnValue param(parameters) sender mySenderVar", "value, parameters and sender clause",
				propCollector.getContextIdentifier(),
				"getreply( ${templateInstance} ) -> value ${myReturnValue} param( ${parameters} ) sender ${mySenderVar};", false),
				TTCN3CodeSkeletons.SKELETON_IMAGE);
	}

	/**
	 * Adds the "catch" related operations of port types, to the completion
	 * list.
	 *
	 * @param propCollector the proposal collector
	 * @param i index, not used
	 * */
	public static void addCatchProposals(final ProposalCollector propCollector, final int i) {
		propCollector.addProposal("catch", "catch", ImageCache.getImage("port.gif"), "");
		propCollector.addTemplateProposal("catch", new Template("catch -> value myVar", "value redirect", propCollector.getContextIdentifier(),
				"catch -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch -> sender myPeer", "sender redirect", propCollector.getContextIdentifier(),
				"catch -> sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch -> value myVar sender myPeer", "value and sender redirect", propCollector
				.getContextIdentifier(), "catch -> value ${myVar} sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch from myPeer", "from clause", propCollector.getContextIdentifier(),
				"catch from ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch from myPeer -> value myVar", "from clause with value redirect", propCollector
				.getContextIdentifier(), "catch from ${myPeer} -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch( signature, templateInstance )", "", propCollector.getContextIdentifier(),
				"catch( ${signature}, ${templateInstance} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch( signature, templateInstance ) -> value myVar", "value redirect",
				propCollector.getContextIdentifier(), "catch( ${signature}, ${templateInstance} ) -> value ${myVar};", false),
				TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch( signature, templateInstance ) -> sender myPeer", "sender redirect",
				propCollector.getContextIdentifier(), "catch( ${signature}, ${templateInstance} ) -> sender ${myPeer};", false),
				TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch( signature, templateInstance ) -> value myVar sender myPeer",
				"value and sender redirect", propCollector.getContextIdentifier(),
				"catch( ${signature}, ${templateInstance} ) -> value ${myVar} sender ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch( signature, templateInstance ) from myPeer", "from clause", propCollector
				.getContextIdentifier(), "catch( ${signature}, ${templateInstance} ) from ${myPeer};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch( signature, templateInstance ) from myPeer -> value myVar",
				"from clause with value redirect", propCollector.getContextIdentifier(),
				"catch( ${signature}, ${templateInstance} ) from ${myPeer} -> value ${myVar};", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("catch", new Template("catch( timeout )", "", propCollector.getContextIdentifier(), "catch( timeout );",
				false), TTCN3CodeSkeletons.SKELETON_IMAGE);
	}

	/**
	 * Adds the "check" related operations of port types, to the completion
	 * list.
	 *
	 * @param propCollector the proposal collector
	 * @param i index, not used
	 * */
	public static void addCheckProposals(final ProposalCollector propCollector, final int i) {
		propCollector.addProposal("check", "check", ImageCache.getImage("port.gif"), "");
		propCollector.addTemplateProposal("check", new Template("check( portOperation )", "", propCollector.getContextIdentifier(),
				"check( ${portOperation} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("check", new Template("check( from myPeer)", "form clause", propCollector.getContextIdentifier(),
				"check( from ${myPeer});", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("check", new Template("check( from myPeer -> value myVar)", "form and value clause", propCollector
				.getContextIdentifier(), "check( from ${myPeer} -> value ${myVar});", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("check", new Template("check( -> value myVar)", "value clause", propCollector.getContextIdentifier(),
				"check( -> value ${myVar} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		propCollector.addTemplateProposal("check", new Template("check( -> value myVar sender myPeer)", "value and sender clause", propCollector
				.getContextIdentifier(), "check( -> value ${myVar} sender ${myPeer} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		IType type;
		if (inTypes != null) {
			for (int i = 0, size = inTypes.size(); i < size; i++) {
				type = inTypes.get(i);

				if (type instanceof IIncrementallyUpdateable) {
					((IIncrementallyUpdateable) type).updateSyntax(reparser, false);
					reparser.updateLocation(type.getLocation());
				} else {
					throw new ReParseException();
				}
			}
		}
		if (outTypes != null) {
			for (int i = 0, size = outTypes.size(); i < size; i++) {
				type = outTypes.get(i);

				if (type instanceof IIncrementallyUpdateable) {
					((IIncrementallyUpdateable) type).updateSyntax(reparser, false);
					reparser.updateLocation(type.getLocation());
				} else {
					throw new ReParseException();
				}
			}
		}
		if (inoutTypes != null) {
			for (int i = 0, size = inoutTypes.size(); i < size; i++) {
				type = inoutTypes.get(i);

				if (type instanceof IIncrementallyUpdateable) {
					((IIncrementallyUpdateable) type).updateSyntax(reparser, false);
					reparser.updateLocation(type.getLocation());
				} else {
					throw new ReParseException();
				}
			}
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (inTypes != null) {
			for (IType t : inTypes) {
				t.findReferences(referenceFinder, foundIdentifiers);
			}
		}
		if (outTypes != null) {
			for (IType t : outTypes) {
				t.findReferences(referenceFinder, foundIdentifiers);
			}
		}
		if (inoutTypes != null) {
			for (IType t : inoutTypes) {
				t.findReferences(referenceFinder, foundIdentifiers);
			}
		}
		if (providerReference != null) {
			providerReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (inMappings != null) {
			inMappings.findReferences(referenceFinder, foundIdentifiers);
		}
		if (outMappings != null) {
			outMappings.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (inTypes!=null) {
			for (IType t : inTypes) {
				if (!t.accept(v)) {
					return false;
				}
			}
		}
		if (outTypes!=null) {
			for (IType t : outTypes) {
				if (!t.accept(v)) {
					return false;
				}
			}
		}
		if (inoutTypes!=null) {
			for (IType t : inoutTypes) {
				if (!t.accept(v)) {
					return false;
				}
			}
		}
		if (providerReference!=null && !providerReference.accept(v)) {
			return false;
		}
		if (inMappings!=null && !inMappings.accept(v)) {
			return false;
		}
		if (outMappings!=null && !outMappings.accept(v)) {
			return false;
		}
		return true;
	}
}
