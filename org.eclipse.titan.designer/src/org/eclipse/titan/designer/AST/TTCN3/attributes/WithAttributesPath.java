/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IIdentifierContainer;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a with attribute path element. Using which the attributes can be
 * distributed in the whole system.
 * 
 * @author Kristof Szabados
 * */
public final class WithAttributesPath implements ILocateableNode, IIncrementallyUpdateable, IIdentifierContainer, IVisitableNode {

	private MultipleWithAttributes attributes;

	private WeakReference<WithAttributesPath> attributeParent;

	/**
	 * the time when this attributepath was checked on global level the last
	 * time.
	 */
	private CompilationTimeStamp lastTimeGlobalChecked;

	/** the time when this attributepath was checked the last time. */
	private CompilationTimeStamp lastTimeChecked;

	/**
	 * the time when the real attributes of this attributepath were created
	 * the last time.
	 */
	private CompilationTimeStamp lastTimeRealAttributesCalculated;
	private List<SingleWithAttribute> realAttributeCache = new ArrayList<SingleWithAttribute>();

	private boolean steppedOverEncode = false;

	/**
	 * Set the attributes handled by this with attribute path element.
	 * 
	 * @param attributes
	 *                the attributes to be handled in this node.
	 * */
	public void setWithAttributes(final MultipleWithAttributes attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the attributes handled by this node. Can be null if none are
	 *         handled here.
	 * */
	public MultipleWithAttributes getAttributes() {
		return attributes;
	}

	/**
	 * Sets the with attribute path element to be handled as the parent
	 * element of this.
	 * 
	 * @param parent
	 *                the with attribute path to be set as the parent
	 *                element.
	 * */
	public void setAttributeParent(final WithAttributesPath parent) {
		attributeParent = new WeakReference<WithAttributesPath>(parent);
	}

	/**
	 * @return the with attribute parent handled as the parent node of this
	 *         node. Can be null if none are set.
	 * */
	public WithAttributesPath getAttributeParent() {
		if (attributeParent == null) {
			return null;
		}

		return attributeParent.get();
	}

	@Override
	public Location getLocation() {
		if (attributes == null) {
			return NULL_Location.INSTANCE;
		}

		return new Location(attributes.getLocation());
	}

	@Override
	public void setLocation(final Location location) {
	}

	/**
	 * Checks whether there is inconsistency among global attributes or not.
	 * Only the last encode can have effect this is because encode is not an
	 * attribute, but a "context". if there is a overriding
	 * variant/display/extension than the followings from the same type
	 * should be omitted.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 */
	public void checkGlobalAttributes(final CompilationTimeStamp timestamp, final boolean erroneousAllowed) {
		if (lastTimeGlobalChecked != null && !lastTimeGlobalChecked.isLess(timestamp)) {
			return;
		}

		lastTimeGlobalChecked = timestamp;

		if (attributes == null) {
			return;
		}

		SingleWithAttribute tempAttribute;
		if (!erroneousAllowed) {
			for (int i = 0, size = attributes.getNofElements(); i < size; i++) {
				tempAttribute = attributes.getAttribute(i);
				if (tempAttribute.getAttributeType() == Attribute_Type.Erroneous_Attribute) {
					tempAttribute.getLocation().reportSemanticError(
							"The `erroneous' attribute can be used only on template and constant definitions");
				}
			}
		}

		boolean hasEncode = false;
		boolean hasOverrideVariant = false;
		boolean hasOverrideDisplay = false;
		boolean hasOverrideExtension = false;
		boolean hasOverrideOptional = false;

		for (int i = attributes.getNofElements() - 1; i >= 0; i--) {
			tempAttribute = attributes.getAttribute(i);
			switch (tempAttribute.getAttributeType()) {
			case Encode_Attribute:
				if (hasEncode) {
					tempAttribute.getLocation()
							.reportSemanticError("Only the last encode of the with statement will have effect");
				} else {
					hasEncode = true;
				}
				break;
			case Erroneous_Attribute:
				if (tempAttribute.hasOverride()) {
					tempAttribute.getLocation().reportSemanticError("Override cannot be used with erroneous");
				}
				break;
			default:
				break;
			}
		}

		for (int i = 0, size = attributes.getNofElements(); i < size; i++) {
			tempAttribute = attributes.getAttribute(i);
			switch (tempAttribute.getAttributeType()) {
			case Variant_Attribute:
				if (hasOverrideVariant) {
					tempAttribute.getLocation().reportSemanticWarning(
							"Only the first override variant of the with statement will have effect");
				} else {
					if (tempAttribute.hasOverride()) {
						hasOverrideVariant = true;
					}
				}
				break;
			case Display_Attribute:
				if (hasOverrideDisplay) {
					tempAttribute.getLocation().reportSemanticWarning(
							"Only the first override display of the with statement will have effect");
				} else {
					if (tempAttribute.hasOverride()) {
						hasOverrideDisplay = true;
					}
				}
				break;
			case Extension_Attribute:
				if (hasOverrideExtension) {
					tempAttribute.getLocation().reportSemanticWarning(
							"Only the first override extension of the with statement will have effect");
				} else {
					if (tempAttribute.hasOverride()) {
						hasOverrideExtension = true;
					}
				}
				break;
			case Optional_Attribute:
				AttributeSpecification attributeSpecification = tempAttribute.getAttributeSpecification();
				String tempSpecification = attributeSpecification.getSpecification();
				if (!"implicit omit".equals(tempSpecification) && !"explicit omit".equals(tempSpecification)) {
					final String message = MessageFormat
							.format("The specification of an optional attribute can only be \"implicit omit\" or \"explicit omit\", not \"{0}\"",
									tempSpecification);
					attributeSpecification.getLocation().reportSemanticError(message);
				}

				if (hasOverrideOptional) {
					tempAttribute.getLocation().reportSemanticWarning(
							"Only the first override optional of the with statement will have effect");
				} else {
					if (tempAttribute.hasOverride()) {
						hasOverrideOptional = true;
					}
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Checks that only certain kind of types are assigned qualifiers.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param type
	 *                the type_type of the type, used to decide if
	 *                qualifiers are allowed or not.
	 */
	public void checkAttributes(final CompilationTimeStamp timestamp, final Type_type type) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (attributes == null) {
			return;
		}

		switch (type) {
		case TYPE_TTCN3_CHOICE:
		case TYPE_ASN1_CHOICE:
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_ASN1_SEQUENCE:
		case TYPE_TTCN3_SET:
		case TYPE_ASN1_SET:
		case TYPE_SEQUENCE_OF:
		case TYPE_SET_OF:
			// field qualifiers are allowed here.
			return;
		default:
			break;
		}

		SingleWithAttribute tempAttribute;
		Qualifiers qualifiers;
		for (int i = 0, size = attributes.getNofElements(); i < size; i++) {
			tempAttribute = attributes.getAttribute(i);
			qualifiers = tempAttribute.getQualifiers();
			if (qualifiers != null && qualifiers.getNofQualifiers() != 0) {
				tempAttribute.getLocation().reportSemanticError("Field qualifiers are only allowed for record, set and union types");
			}
		}
	}

	/**
	 * Only types may have qualifiers, so this checks that there are no
	 * qualifiers present.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 */
	public void checkAttributes(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (attributes == null) {
			return;
		}

		SingleWithAttribute tempAttribute;
		Qualifiers qualifiers;
		for (int i = 0, size = attributes.getNofElements(); i < size; i++) {
			tempAttribute = attributes.getAttribute(i);
			qualifiers = tempAttribute.getQualifiers();
			if (qualifiers != null && qualifiers.getNofQualifiers() != 0) {
				tempAttribute.getLocation().reportSemanticError("Field qualifiers are only allowed for record, set and union types");
			}
		}
	}

	/**
	 * Checks whether an encode attribute was stepped over in this attribute
	 * path, the last time it got checked.
	 * 
	 * @return true if an encode attribute was stepped over, false
	 *         otherwise.
	 * */
	private boolean hasSteppedOverEncode() {
		return steppedOverEncode;
	}

	/**
	 * Processes the qualifier less attributes of the attribute path parent
	 * and the local attributes. Can be used to find out, what are the final
	 * attributes on an element knowing the attributes set on its parents,
	 * and itself.
	 * <p>
	 * This function is doing the actual calculations
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle
	 * 
	 * @return the list of attributes that are finally assigned to the
	 *         element this path piece belongs to. Can not be null, and
	 *         adding/removing elements will not change the original lists
	 *         (but modifying the elements will)
	 * */
	private List<SingleWithAttribute> qualifierlessAttributeProcessor(final CompilationTimeStamp timestamp) {
		if (lastTimeRealAttributesCalculated != null && !lastTimeRealAttributesCalculated.isLess(timestamp)) {
			return realAttributeCache;
		}

		lastTimeRealAttributesCalculated = timestamp;

		realAttributeCache = new ArrayList<SingleWithAttribute>();

		if (attributeParent != null) {
			WithAttributesPath parentPath = attributeParent.get();
			if (parentPath != null) {
				List<SingleWithAttribute> temp = parentPath.qualifierlessAttributeProcessor(timestamp);
				realAttributeCache.addAll(temp);
				steppedOverEncode = parentPath.hasSteppedOverEncode();
			} else {
				steppedOverEncode = false;
			}
		} else {
			steppedOverEncode = false;
		}

		if (attributes == null || attributes.getNofElements() == 0) {
			return realAttributeCache;
		}

		int selfEncodeIndex = -1;
		boolean selfHasVariant = false;

		// checking the owned attributes
		SingleWithAttribute actualSingleAttribute;
		Qualifiers actualQualifiers;
		for (int i = 0, size = attributes.getNofElements(); i < size; i++) {
			actualSingleAttribute = attributes.getAttribute(i);
			actualQualifiers = actualSingleAttribute.getQualifiers();

			// global attribute
			if (actualQualifiers == null || actualQualifiers.getNofQualifiers() == 0) {
				switch (actualSingleAttribute.getAttributeType()) {
				case Encode_Attribute:
					selfEncodeIndex = i;
					break;
				case Variant_Attribute:
					selfHasVariant = true;
					break;
				default:
					break;
				}
			}
		}

		boolean parentHasEncode = false;
		boolean parentHasOverrideEncode = false;
		/**
		 * true if there is an encode attribute in the local attribute
		 * list, it differs from the parents encode, and the parent does
		 * not overwrite it.
		 */
		boolean newLocalEncodeContext = false;
		boolean parentHasOverrideVariant = false;
		boolean parentHasOverrideDisplay = false;
		boolean parentHasOverrideExtension = false;
		boolean parentHasOverrideOptional = false;

		// gather information on the attributes collected from the
		// parents
		for (int i = 0, size = realAttributeCache.size(); i < size; i++) {
			actualSingleAttribute = realAttributeCache.get(i);

			switch (actualSingleAttribute.getAttributeType()) {
			case Encode_Attribute:
				parentHasEncode = true;
				parentHasOverrideEncode |= actualSingleAttribute.hasOverride();
				if (selfEncodeIndex != -1) {
					final String actualSpecification = actualSingleAttribute.getAttributeSpecification().getSpecification();
					final String selfSpecification = attributes.getAttribute(selfEncodeIndex).getAttributeSpecification()
							.getSpecification();
					newLocalEncodeContext = !actualSpecification.equals(selfSpecification);
				}
				break;
			case Variant_Attribute:
				parentHasOverrideVariant |= actualSingleAttribute.hasOverride();
				break;
			case Display_Attribute:
				parentHasOverrideDisplay |= actualSingleAttribute.hasOverride();
				break;
			case Extension_Attribute:
				parentHasOverrideExtension |= actualSingleAttribute.hasOverride();
				break;
			case Optional_Attribute:
				parentHasOverrideOptional |= actualSingleAttribute.hasOverride();
				break;
			default:
				break;
			}
		}

		if (!parentHasEncode && selfEncodeIndex == -1 && selfHasVariant) {
			for (int i = 0, size = attributes.getNofElements(); i < size; i++) {
				actualSingleAttribute = attributes.getAttribute(i);
				if (Attribute_Type.Variant_Attribute.equals(actualSingleAttribute.getAttributeType())) {
					actualSingleAttribute.getLocation().reportSemanticWarning("This variant does not belong to an encode");
				}
			}
		}

		// remove the encode and variant attributes, that are
		// overwritten
		for (int i = realAttributeCache.size() - 1; i >= 0; i--) {
			switch (realAttributeCache.get(i).getAttributeType()) {
			case Encode_Attribute:
				if (selfEncodeIndex != -1 && !parentHasOverrideEncode && newLocalEncodeContext) {
					realAttributeCache.remove(i);
				}
				break;
			case Variant_Attribute:
				if (selfEncodeIndex != -1 && newLocalEncodeContext) {
					if ((parentHasEncode && !parentHasOverrideEncode) || !parentHasEncode) {
						realAttributeCache.remove(i);
					}
				}
				break;
			default:
				break;
			}
		}

		// adding the right ones from the local attributes
		for (int i = 0, size = attributes.getNofElements(); i < size; i++) {
			actualSingleAttribute = attributes.getAttribute(i);
			actualQualifiers = actualSingleAttribute.getQualifiers();
			if (actualQualifiers == null || actualQualifiers.getNofQualifiers() == 0) {
				switch (actualSingleAttribute.getAttributeType()) {
				case Encode_Attribute:
					if ((parentHasEncode && !parentHasOverrideEncode && newLocalEncodeContext) || !parentHasEncode) {
						realAttributeCache.add(0, actualSingleAttribute);
						steppedOverEncode = false;
					} else if (newLocalEncodeContext) {
						steppedOverEncode = true;
					} else {
						steppedOverEncode = false;
					}
					break;
				case Variant_Attribute: {
					final boolean parentHasNothing = !parentHasEncode && !parentHasOverrideVariant;
					final boolean noParentButLocalEncode = !parentHasEncode && selfEncodeIndex != -1;
					final boolean newLocalEncode = parentHasEncode && selfEncodeIndex != -1 && !steppedOverEncode
							&& !parentHasOverrideVariant;
					final boolean localEncodeOverwritesParent = parentHasEncode && selfEncodeIndex != -1
							&& !parentHasOverrideEncode && !parentHasOverrideVariant;
					if (parentHasNothing || noParentButLocalEncode || newLocalEncode || localEncodeOverwritesParent) {
						realAttributeCache.add(actualSingleAttribute);
					}
					break;
				}
				case Display_Attribute:
					if (!parentHasOverrideDisplay) {
						realAttributeCache.add(actualSingleAttribute);
					}
					break;
				case Extension_Attribute:
					if (!parentHasOverrideExtension) {
						realAttributeCache.add(actualSingleAttribute);
					}
					break;
				case Optional_Attribute:
					if (!parentHasOverrideOptional) {
						realAttributeCache.add(actualSingleAttribute);
					}
					break;
				default:
					break;
				}
			}
		}

		return realAttributeCache;
	}

	/**
	 * Return the qualifier less attributes of the attribute path parent and
	 * the local attributes. Can be used to find out, what are the final
	 * attributes on an element knowing the attributes set on its parents,
	 * and itself.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle
	 * 
	 * @return the list of attributes that are finally assigned to the
	 *         element this path piece belongs to. Can not be null, and
	 *         adding/removing elements will not change the original lists
	 *         (but modifying the elements will)
	 * */
	public List<SingleWithAttribute> getRealAttributes(final CompilationTimeStamp timestamp) {
		return qualifierlessAttributeProcessor(timestamp);
	}

	/**
	 * Handles the incremental parsing of this attribute set.
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
			throw new ReParseException();
		}

		if (attributes != null) {
			attributes.updateSyntax(reparser, false);
			reparser.updateLocation(attributes.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (attributes == null) {
			return;
		}

		attributes.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (attributes != null) {
			if (!attributes.accept(v)) {
				return false;
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
