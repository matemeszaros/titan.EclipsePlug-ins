/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IIdentifierContainer;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErroneousAttributeSpecification.Indicator_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * helper class, container
 * 
 * @author Adam Delic
 */
class FieldErr_Type {
	public Qualifier qualifier;
	public ErroneousAttributeSpecification errAttr;
	public List<Integer> subrefsArray;
	public List<IType> typeArray;

	FieldErr_Type(final Qualifier qualifier, final ErroneousAttributeSpecification err_attr_spec, final List<Integer> subrefs_array,
			final List<IType> type_array) {
		this.qualifier = qualifier;
		this.errAttr = err_attr_spec;
		this.subrefsArray = subrefs_array;
		this.typeArray = type_array;
	}
}

/**
 * helper class, for tree building
 */
class ErroneousValues {
	// null if not specified
	public ErroneousAttributeSpecification before = null;
	public ErroneousAttributeSpecification value = null;
	public ErroneousAttributeSpecification after = null;
	public String fieldName = "";

	public ErroneousValues(final String fieldName) {
		this.fieldName = fieldName;
	}
}

/**
 * helper class, for tree building
 */
class ErroneousDescriptor {
	// -1 if not set
	public int omitBefore = -1;
	public int omitAfter = -1;
	// qualifier string or empty
	public String omitBeforeName = "";
	public String omitAfterName = "";

	// descriptors for the fields
	public Map<Integer, ErroneousDescriptor> descriptorMap = new HashMap<Integer, ErroneousDescriptor>();

	// erroneous values for the fields
	public Map<Integer, ErroneousValues> valuesMap = new HashMap<Integer, ErroneousValues>();
}

/**
 * Helper class used as container of all erroneous attributes of a definition
 * and for semantic check
 */
public class ErroneousAttributes implements IIdentifierContainer, IVisitableNode {
	// type of the definition which this belongs to
	private IType type;
	private List<ErroneousAttributeSpecification> errAttrSpecs = null;
	private List<FieldErr_Type> fieldArray = null;
	private ErroneousDescriptor erroneousDescriptorTree = null;

	public ErroneousAttributes(final IType type) {
		this.type = type;
	}

	public void addSpecification(final ErroneousAttributeSpecification errAttrSpec) {
		if (errAttrSpecs == null) {
			errAttrSpecs = new ArrayList<ErroneousAttributeSpecification>(1);
		}
		errAttrSpecs.add(errAttrSpec);
	}

	public void addFieldErr(final Qualifier qualifier, final ErroneousAttributeSpecification errAttrSpec, final List<Integer> subrefsArray,
			final List<IType> typeArray) {
		if (qualifier == null || errAttrSpec == null || subrefsArray == null || typeArray == null) {
			ErrorReporter.INTERNAL_ERROR();
		}
		if (fieldArray == null) {
			fieldArray = new ArrayList<FieldErr_Type>();
		}
		fieldArray.add(new FieldErr_Type(qualifier, errAttrSpec, subrefsArray, typeArray));
	}

	private ErroneousDescriptor buildErroneousDescriptorTree(final CompilationTimeStamp timestamp, final List<FieldErr_Type> fldArray,
			final int level) {
		ErroneousDescriptor erroneousDescr = new ErroneousDescriptor();
		Qualifier omitBeforeQualifier = null;
		Qualifier omitAfterQualifier = null;
		Map<Integer, List<FieldErr_Type>> embeddedFieldArrayMap = new HashMap<Integer, List<FieldErr_Type>>();
		for (FieldErr_Type actualFieldErr : fldArray) {
			Indicator_Type actIndicator = actualFieldErr.errAttr.getIndicator();
			boolean isOmit = actualFieldErr.errAttr.isOmit();
			if (actualFieldErr.subrefsArray.size() <= level) {
				ErrorReporter.INTERNAL_ERROR();
				return erroneousDescr;
			}
			int fieldIndex = actualFieldErr.subrefsArray.get(level);
			IType fieldType = actualFieldErr.typeArray.get(level);
			if (omitBeforeQualifier != null && erroneousDescr.omitBefore != -1 && erroneousDescr.omitBefore > fieldIndex) {
				final String message = MessageFormat.format(
						"Field `{0}'' cannot be referenced because all fields before field `{1}'' have been omitted",
						actualFieldErr.qualifier.getDisplayName(), omitBeforeQualifier.getDisplayName());
				actualFieldErr.qualifier.getLocation().reportSemanticError(message);
				continue;
			}
			if (omitAfterQualifier != null && erroneousDescr.omitAfter != -1 && erroneousDescr.omitAfter < fieldIndex) {
				final String message = MessageFormat.format(
						"Field `{0}'' cannot be referenced because all fields after field `{1}'' have been omitted",
						actualFieldErr.qualifier.getDisplayName(), omitAfterQualifier.getDisplayName());
				actualFieldErr.qualifier.getLocation().reportSemanticError(message);
				continue;
			}
			if (actualFieldErr.subrefsArray.size() == level + 1) {
				// erroneous value
				if (actualFieldErr.typeArray.size() != level + 1) {
					ErrorReporter.INTERNAL_ERROR();
					return erroneousDescr;
				}
				if (fieldType.getTypetype() == Type_type.TYPE_ASN1_SET && isOmit && actIndicator != Indicator_Type.Value_Indicator) {
					final String message = MessageFormat
							.format("Cannot omit all fields {0} `{1}'' which is a field of an ASN.1 SET type. "
									+ "The order of fields in ASN.1 SET types changes depending on tagging (see X.690 9.3). "
									+ "Fields can be omitted individually, independently of the field order which depends on tagging",
									actIndicator.getDisplayName(), actualFieldErr.qualifier.getDisplayName());
					actualFieldErr.qualifier.getLocation().reportSemanticError(message);
					continue;
				}
				switch (fieldType.getTypetypeTtcn3()) {
				case TYPE_TTCN3_CHOICE:
					if (actIndicator != Indicator_Type.Value_Indicator) {
						final String message = MessageFormat
								.format("Indicator `{0}'' cannot be used with reference `{1}'' which points to a field of a union type",
										actIndicator.getDisplayName(),
										actualFieldErr.qualifier.getDisplayName());
						actualFieldErr.qualifier.getLocation().reportSemanticError(message);
						continue;
					}
					break;
				case TYPE_TTCN3_SEQUENCE:
				case TYPE_TTCN3_SET:
					if (isOmit && actIndicator == Indicator_Type.After_Indicator) {
						int lastFieldIndex;
						switch (fieldType.getTypetype()) {
						case TYPE_ASN1_SEQUENCE:
							lastFieldIndex = ((ASN1_Sequence_Type) fieldType).getNofComponents(timestamp) - 1;
							break;
						case TYPE_ASN1_SET:
							lastFieldIndex = ((ASN1_Set_Type) fieldType).getNofComponents(timestamp) - 1;
							break;
						default:
							lastFieldIndex = ((TTCN3_Set_Seq_Choice_BaseType) fieldType).getNofComponents() - 1;
						}
						if (fieldIndex == lastFieldIndex) {
							final String message = MessageFormat.format(
									"There is nothing to omit after the last field ({0}) of a record/set type",
									actualFieldErr.qualifier.getDisplayName());
							actualFieldErr.qualifier.getLocation().reportSemanticError(message);
							continue;
						}
					}
					//$FALL-THROUGH$
				case TYPE_SEQUENCE_OF:
				case TYPE_SET_OF:
					if (isOmit && actIndicator == Indicator_Type.Before_Indicator && fieldIndex == 0) {
						actualFieldErr.qualifier.getLocation().reportSemanticError(
								MessageFormat.format("There is nothing to omit before the first field ({0})",
										actualFieldErr.qualifier.getDisplayName()));
						continue;
					}
					break;
				default:
					break;
				}
				// check for duplicate value+indicator
				if (erroneousDescr.valuesMap.containsKey(fieldIndex)) {
					ErroneousValues evs = erroneousDescr.valuesMap.get(fieldIndex);
					if ((evs.before != null && actIndicator == Indicator_Type.Before_Indicator)
							|| (evs.value != null && actIndicator == Indicator_Type.Value_Indicator)
							|| (evs.after != null && actIndicator == Indicator_Type.After_Indicator)) {
						actualFieldErr.qualifier.getLocation().reportSemanticError(
								MessageFormat.format("Duplicate reference to field `{0}'' with indicator `{1}''",
										actualFieldErr.qualifier.getDisplayName(),
										actIndicator.getDisplayName()));
						continue;
					}
				}
				// when overwriting a value check if embedded
				// values were used
				if (actIndicator == Indicator_Type.Value_Indicator && embeddedFieldArrayMap.containsKey(fieldIndex)) {
					final String message = MessageFormat
							.format("Reference to field `{0}'' with indicator `value'' would invalidate previously specified erroneous data",
									actualFieldErr.qualifier.getDisplayName());
					actualFieldErr.qualifier.getLocation().reportSemanticError(message);
					continue;
				}
				// if before/after omit then check that no
				// references to omitted regions and no
				// duplication of omit before/after rule
				if (actIndicator == Indicator_Type.Before_Indicator && isOmit) {
					if (omitBeforeQualifier != null && erroneousDescr.omitBefore != -1) {
						final String message = MessageFormat.format(
								"Duplicate rule for omitting all fields before the specified field. "
										+ "Used on field `{0}'' but previously already used on field `{1}''",
								actualFieldErr.qualifier.getDisplayName(), omitBeforeQualifier.getDisplayName());
						actualFieldErr.qualifier.getLocation().reportSemanticError(message);
						continue;
					}
					boolean isInvalid = false;
					for (Integer idx : erroneousDescr.valuesMap.keySet()) {
						if (idx < fieldIndex) {
							isInvalid = true;
							break;
						}
					}
					if (!isInvalid) {
						for (Integer idx : embeddedFieldArrayMap.keySet()) {
							if (idx < fieldIndex) {
								isInvalid = true;
								break;
							}
						}
					}
					if (isInvalid) {
						final String message = MessageFormat
								.format("Omitting fields before field `{0}'' would invalidate previously specified erroneous data",
										actualFieldErr.qualifier.getDisplayName());
						actualFieldErr.qualifier.getLocation().reportSemanticError(message);
						continue;
					}
					// save valid omit before data
					omitBeforeQualifier = actualFieldErr.qualifier;
					erroneousDescr.omitBefore = fieldIndex;
					erroneousDescr.omitBeforeName = omitBeforeQualifier.getDisplayName();
					continue;
				}
				if (actIndicator == Indicator_Type.After_Indicator && isOmit) {
					if (omitAfterQualifier != null && erroneousDescr.omitAfter != -1) {
						final String message = MessageFormat.format(
								"Duplicate rule for omitting all fields after the specified field. "
										+ "Used on field `{0}'' but previously already used on field `{1}''",
								actualFieldErr.qualifier.getDisplayName(), omitAfterQualifier.getDisplayName());
						actualFieldErr.qualifier.getLocation().reportSemanticError(message);
						continue;
					}
					boolean isInvalid = false;
					for (Integer idx : erroneousDescr.valuesMap.keySet()) {
						if (idx > fieldIndex) {
							isInvalid = true;
							break;
						}
					}
					if (!isInvalid) {
						for (Integer idx : embeddedFieldArrayMap.keySet()) {
							if (idx > fieldIndex) {
								isInvalid = true;
								break;
							}
						}
					}
					if (isInvalid) {
						final String message = MessageFormat
								.format("Omitting fields after field `{0}'' would invalidate previously specified erroneous data",
										actualFieldErr.qualifier.getDisplayName());
						actualFieldErr.qualifier.getLocation().reportSemanticError(message);
						continue;
					}
					// save valid omit after data
					omitAfterQualifier = actualFieldErr.qualifier;
					erroneousDescr.omitAfter = fieldIndex;
					erroneousDescr.omitAfterName = omitAfterQualifier.getDisplayName();
					continue;
				}
				// if not before/after omit then save this into
				// values_m
				boolean hasKey = erroneousDescr.valuesMap.containsKey(fieldIndex);
				ErroneousValues evs = hasKey ? erroneousDescr.valuesMap.get(fieldIndex) : new ErroneousValues(
						actualFieldErr.qualifier.getDisplayName());
				switch (actIndicator) {
				case Before_Indicator:
					evs.before = actualFieldErr.errAttr;
					break;
				case Value_Indicator:
					evs.value = actualFieldErr.errAttr;
					break;
				case After_Indicator:
					evs.after = actualFieldErr.errAttr;
					break;
				default:
					ErrorReporter.INTERNAL_ERROR();
				}
				if (!hasKey) {
					erroneousDescr.valuesMap.put(fieldIndex, evs);
				}
			} else {
				// embedded err.value
				if (erroneousDescr.valuesMap.containsKey(fieldIndex) && erroneousDescr.valuesMap.get(fieldIndex).value != null) {
					final String message = MessageFormat.format(
							"Field `{0}'' is embedded into a field which was previously overwritten or omitted",
							actualFieldErr.qualifier.getDisplayName());
					actualFieldErr.qualifier.getLocation().reportSemanticError(message);
					continue;
				}
				// add the embedded field to the map
				boolean hasIndex = embeddedFieldArrayMap.containsKey(fieldIndex);
				List<FieldErr_Type> embeddedFieldArray = hasIndex ? embeddedFieldArrayMap.get(fieldIndex)
						: new ArrayList<FieldErr_Type>(1);
				embeddedFieldArray.add(actualFieldErr);
				if (!hasIndex) {
					embeddedFieldArrayMap.put(fieldIndex, embeddedFieldArray);
				}
			}
		}
		// recursive calls to create embedded descriptors
		for (Integer idx : embeddedFieldArrayMap.keySet()) {
			erroneousDescr.descriptorMap.put(idx, buildErroneousDescriptorTree(timestamp, embeddedFieldArrayMap.get(idx), level + 1));
		}
		return erroneousDescr;
	}

	public void check(final CompilationTimeStamp timestamp) {
		if (errAttrSpecs == null || fieldArray == null || type == null) {
			return;
		}

		// TODO: check that encodings of erroneous type and
		// templateinstance type match

		// for every erroneous field calculate the corresponding index
		// and type arrays
		// for example: x[5].z -> [3,5,2] and [MyRec,MyRecOf,MyUnion]
		// MyRec.x field has index 3, etc.
		erroneousDescriptorTree = buildErroneousDescriptorTree(timestamp, fieldArray, 0);
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		// qualifiers are searched in withAttributePath part, not here
		if (errAttrSpecs != null) {
			for (ErroneousAttributeSpecification eas : errAttrSpecs) {
				eas.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (errAttrSpecs != null) {
			for (ErroneousAttributeSpecification eas : errAttrSpecs) {
				if (!eas.accept(v)) {
					return false;
				}
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
