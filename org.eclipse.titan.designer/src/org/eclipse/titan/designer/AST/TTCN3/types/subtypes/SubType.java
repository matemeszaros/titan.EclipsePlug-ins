/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.BridgingNamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.templates.BitString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.CharString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.HexString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.LengthRestriction;
import org.eclipse.titan.designer.AST.TTCN3.templates.OctetString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString;
import org.eclipse.titan.designer.AST.TTCN3.templates.RangeLenghtRestriction;
import org.eclipse.titan.designer.AST.TTCN3.templates.SingleLenghtRestriction;
import org.eclipse.titan.designer.AST.TTCN3.templates.SubsetMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SupersetMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.UnivCharString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The SubType class is the base class for TTCN-3 type restrictions.
 * 
 * @author Adam Delic
 * */
public final class SubType implements IIncrementallyUpdateable {
	/** The kind of sub-types known */
	public enum SubType_type {
		ST_NONE,
		ST_INTEGER,
		ST_FLOAT,
		ST_BOOLEAN,
		ST_OBJID,
		ST_VERDICTTYPE,
		ST_BITSTRING,
		ST_HEXSTRING,
		ST_OCTETSTRING,
		ST_CHARSTRING,
		ST_UNIVERSAL_CHARSTRING,
		ST_RECORD,
		ST_RECORDOF,
		ST_SET,
		ST_SETOF,
		ST_ENUM,
		ST_UNION,
		ST_FUNCTION,
		ST_ALTSTEP,
		ST_TESTCASE
	}

	// constants used by is_compatible_with_elem():
	private static final SubtypeConstraint BIT_SC = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.BITSTRING,
			new SizeLimit(1));
	private static final SubtypeConstraint HEX_SC = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.HEXSTRING,
			new SizeLimit(1));
	private static final SubtypeConstraint OCTET_SC = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.OCTETSTRING,
			new SizeLimit(1));
	private static final SubtypeConstraint CHAR_SC = new StringSetConstraint(StringSubtypeTreeElement.StringType.CHARSTRING,
			StringSetConstraint.ConstraintType.SIZE_CONSTRAINT, new RangeListConstraint(new SizeLimit(1)));
	private static final SubtypeConstraint UCHAR_SC = new StringSetConstraint(StringSubtypeTreeElement.StringType.UNIVERSAL_CHARSTRING,
			StringSetConstraint.ConstraintType.SIZE_CONSTRAINT, new RangeListConstraint(new SizeLimit(1)));

	private final SubType_type subtypeType;

	/** The type to which this sub-type belongs to. */
	private final IType myOwner;

	/**
	 * The list of restrictions parsed, the sub-type restrictions will be
	 * created from these.
	 */
	private final List<ParsedSubType> parsedRestrictions;

	/** inherited subtype */
	private final SubType parentSubtype;

	private SubtypeConstraint subtypeConstraint = null;

	/**
	 * if own type has a length constraint then it is stored separately here
	 */
	private RangeListConstraint lengthRestriction = null;

	/** used to check for circular references */
	private Set<SubType> myParents = new HashSet<SubType>();

	/** The time when this setting was checked the last time. */
	private CompilationTimeStamp lastTimeChecked = null;
	private boolean isErroneous = false;

	/**
	 * Checks whether subtype was reported erroneous or not.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @return true if the subtype is erroneous, or false otherwise
	 */
	public boolean getIsErroneous(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return isErroneous;
		}

		return false;
	}

	public SubType(final SubType_type subtypeType, final IType myOwner, final List<ParsedSubType> parsedRestrictions, final SubType parentSubtype) {
		this.subtypeType = subtypeType;
		this.myOwner = myOwner;
		this.parsedRestrictions = parsedRestrictions;
		this.parentSubtype = parentSubtype;
	}

	/**
	 * Checks if the provided sub-type is compatible with the actual one.
	 *
	 * @param subtype
	 *                the sub-type to be checked.
	 * */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final SubType other) {
		if (other == null) {
			// the other type has no subtype restriction
			return true;
		}

		if ((getIsErroneous(timestamp)) || (other.getIsErroneous(timestamp))) {
			return true;
		}

		if (subtypeType != other.subtypeType) {
			return false;
		}

		if ((subtypeConstraint == null) || (other.subtypeConstraint == null)) {
			return true;
		}

		SubtypeConstraint intersectionSet = subtypeConstraint.intersection(other.subtypeConstraint);
		return intersectionSet.isEmpty() != TernaryBool.TTRUE;
	}

	public boolean isCompatibleWithElem(final CompilationTimeStamp timestamp) {
		if (getIsErroneous(timestamp)) {
			return true;
		}
		if (subtypeConstraint == null) {
			return true;
		}
		SubtypeConstraint sc;
		switch (subtypeType) {
		case ST_BITSTRING:
			sc = BIT_SC;
			break;
		case ST_HEXSTRING:
			sc = HEX_SC;
			break;
		case ST_OCTETSTRING:
			sc = OCTET_SC;
			break;
		case ST_CHARSTRING:
			sc = CHAR_SC;
			break;
		case ST_UNIVERSAL_CHARSTRING:
			sc = UCHAR_SC;
			break;
		default:
			ErrorReporter.INTERNAL_ERROR("not string type");
			return true;
		}
		return subtypeConstraint.intersection(sc).isEmpty() != TernaryBool.TTRUE;
	}

	private boolean checkRecursion(final IReferenceChain refch) {
		if (!refch.add(myOwner)) {
			// try the referenced type
			return false;
		}
		for (SubType st : myParents) {
			refch.markState();
			if (!st.checkRecursion(refch)) {
				return false;
			}
			refch.previousState();
		}
		return true;
	}

	private boolean addParentSubtype(final SubType st) {
		if (myParents.contains(st)) {
			// it was already successfully added -> ignore
			return true;
		}
		IReferenceChain refch = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		// current type
		refch.add(myOwner);
		// recursive check for all parents of referenced type
		if (!st.checkRecursion(refch)) {
			refch.release();
			return false;
		}
		// if no recursion was detected then add the referenced type as
		// parent
		myParents.add(st);
		refch.release();
		return true;
	}

	/** add TTCN-3 single value sub-type constraint to this sub-type */
	private boolean addTtcnSingle(final CompilationTimeStamp timestamp, final Value value, final int restrictionIndex) {
		value.setMyScope(myOwner.getMyScope());
		value.setMyGovernor(myOwner);
		BridgingNamedNode bridge = new BridgingNamedNode(myOwner, myOwner.getTypename() + ".<single_restriction_" + restrictionIndex + ">");
		value.setFullNameParent(bridge);
		IValue last = myOwner.checkThisValueRef(timestamp, value);

		// check if this is type reference, if not then fall through
		IValue refValue = value.setLoweridToReference(timestamp);
		// Value ref_value = value.set_valuetype(timestamp,
		// Value_type.REFERENCED_VALUE);
		if (refValue.getValuetype() == Value.Value_type.REFERENCED_VALUE) {
			Reference ref = ((Referenced_Value) refValue).getReference();
			Assignment ass = ref.getRefdAssignment(timestamp, false);
			if (ass == null) {
				// definition was not found, error was reported
				return false;
			}
			if (ass.getAssignmentType() == Assignment.Assignment_type.A_TYPE) {
				IType t = ass.getType(timestamp);
				t.check(timestamp);
				if (t.getIsErroneous(timestamp)) {
					return false;
				}
				List<ISubReference> subrefs = ref.getSubreferences();
				if (subrefs.size() > 1) {
					// if there were sub-references then get the referenced field's type
					t = t.getFieldType(timestamp, ref, 1, Expected_Value_type.EXPECTED_CONSTANT, false);
					if ((t == null) || t.getIsErroneous(timestamp)) {
						return false;
					}
					t.check(timestamp);
					if (t.getIsErroneous(timestamp)) {
						return false;
					}
				}
				if (!t.isIdentical(timestamp, myOwner)) {
					value.getLocation()
							.reportSemanticError(
									MessageFormat.format(
											"Reference `{0}'' must refer to a type which has the same root type as this type",
											ref.getDisplayName()));
					return false;
				}
				// check subtype of referenced type
				SubType tSt = t.getSubtype();
				if ((tSt == null) || (tSt.subtypeConstraint == null)) {
					value.getLocation().reportSemanticError(
							MessageFormat.format("Type referenced by `{0}'' does not have a subtype",
									ref.getDisplayName()));
					return false;
				}

				// check circular sub-type reference
				if (!addParentSubtype(tSt)) {
					return false;
				}

				if (tSt.getIsErroneous(timestamp)) {
					return false;
				}
				if (tSt.subtypeType != subtypeType) {
					ErrorReporter.INTERNAL_ERROR();
					return false;
				}
				// add the sub-type as union
				if (subtypeConstraint == null) {
					subtypeConstraint = tSt.subtypeConstraint;
				} else {
					subtypeConstraint = subtypeConstraint.union(tSt.subtypeConstraint);
				}
				return true;
			}
		}

		myOwner.checkThisValue(timestamp, last, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, false, false, false, false,
				false));
		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		last = last.getValueRefdLast(timestamp, chain);
		chain.release();
		if (last.getIsErroneous(timestamp) || last.isUnfoldable(timestamp)) {
			return false;
		}

		// create a single value constraint
		SubtypeConstraint sc;
		switch (subtypeType) {
		case ST_INTEGER:
			sc = new RangeListConstraint(new IntegerLimit(((Integer_Value) last).getValueValue()));
			break;
		case ST_FLOAT:
			sc = new RealRangeListConstraint(((Real_Value) last).getValue());
			break;
		case ST_BOOLEAN:
			sc = new BooleanListConstraint(((Boolean_Value) last).getValue());
			break;
		case ST_VERDICTTYPE:
			sc = new VerdicttypeListConstraint(((Verdict_Value) last).getValue());
			break;
		case ST_BITSTRING:
			sc = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.BITSTRING,
					((Bitstring_Value) last).getValue());
			break;
		case ST_HEXSTRING:
			sc = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.HEXSTRING,
					((Hexstring_Value) last).getValue());
			break;
		case ST_OCTETSTRING:
			sc = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.OCTETSTRING,
					((Octetstring_Value) last).getValue());
			break;
		case ST_CHARSTRING:
			if (last.getValuetype() != Value.Value_type.CHARSTRING_VALUE) {
				return false;
			}
			sc = new StringSetConstraint(StringSubtypeTreeElement.StringType.CHARSTRING,
					StringSetConstraint.ConstraintType.VALUE_CONSTRAINT, new StringValueConstraint(
							((Charstring_Value) last).getValue()));
			break;
		case ST_UNIVERSAL_CHARSTRING:
			switch (last.getValuetype()) {
			case CHARSTRING_VALUE:
				sc = new StringSetConstraint(StringSubtypeTreeElement.StringType.UNIVERSAL_CHARSTRING,
						StringSetConstraint.ConstraintType.VALUE_CONSTRAINT, new UStringValueConstraint(
								new UniversalCharstring(((Charstring_Value) last).getValue())));
				break;
			case UNIVERSALCHARSTRING_VALUE:
				sc = new StringSetConstraint(StringSubtypeTreeElement.StringType.UNIVERSAL_CHARSTRING,
						StringSetConstraint.ConstraintType.VALUE_CONSTRAINT, new UStringValueConstraint(
								((UniversalCharstring_Value) last).getValue()));
				break;
			default:
				return false;
			}
			break;
		case ST_OBJID:
		case ST_ENUM:
		case ST_UNION:
		case ST_RECORD:
		case ST_SET:
		case ST_FUNCTION:
		case ST_ALTSTEP:
		case ST_TESTCASE:
			sc = new ValueListConstraint(last);
			break;
		case ST_RECORDOF:
		case ST_SETOF:
			sc = new ValueListAndSizeConstraint(last);
			break;
		default:
			ErrorReporter.INTERNAL_ERROR();
			return false;
		}

		// add next value using union operation
		if (subtypeConstraint == null) {
			subtypeConstraint = sc;
		} else {
			subtypeConstraint = subtypeConstraint.union(sc);
		}

		return true;
	}

	private boolean addTtcnRange(final CompilationTimeStamp timestamp, final Value min, final boolean minExclusive, final Value max,
			final boolean maxExclusive, final int restrictionIndex) {
		switch (subtypeType) {
		case ST_INTEGER:
		case ST_FLOAT:
		case ST_CHARSTRING:
		case ST_UNIVERSAL_CHARSTRING:
			break;
		default:
			myOwner.getLocation().reportSemanticError(
					MessageFormat.format("Range subtyping is not allowed for type `{0}''", myOwner.getTypename()));
			return false;
		}

		if (min == null || max == null) {
			return false;
		}

		IValue vmin = null, vmax = null;

		min.setMyScope(myOwner.getMyScope());
		min.setMyGovernor(myOwner);
		BridgingNamedNode bridge = new BridgingNamedNode(myOwner, myOwner.getFullName() + ".<range_restriction_" + restrictionIndex
				+ "_lower>");
		min.setFullNameParent(bridge);
		IValue last = myOwner.checkThisValueRef(timestamp, min);
		IType lastOwner = myOwner.getTypeRefdLast(timestamp);
		if (lastOwner instanceof Integer_Type) {
			((Integer_Type) lastOwner).checkThisValueLimit(timestamp, last, Expected_Value_type.EXPECTED_CONSTANT, false, false, false,
					false);
		} else {
			myOwner.checkThisValue(timestamp, last, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, false, false, false,
					false, false));
		}
		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		vmin = last.getValueRefdLast(timestamp, chain);
		chain.release();

		max.setMyScope(myOwner.getMyScope());
		max.setMyGovernor(myOwner);
		bridge = new BridgingNamedNode(myOwner, myOwner.getFullName() + ".<range_restriction_" + restrictionIndex + "_upper>");
		max.setFullNameParent(bridge);
		last = myOwner.checkThisValueRef(timestamp, max);
		lastOwner = myOwner.getTypeRefdLast(timestamp);
		if (lastOwner instanceof Integer_Type) {
			((Integer_Type) lastOwner).checkThisValueLimit(timestamp, last, Expected_Value_type.EXPECTED_CONSTANT, false, false, false,
					false);
		} else {
			myOwner.checkThisValue(timestamp, last, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, false, false, false,
					false, false));
		}
		chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		vmax = last.getValueRefdLast(timestamp, chain);
		chain.release();

		if (vmin.getIsErroneous(timestamp) || vmin.isUnfoldable(timestamp)) {
			// the error was already reported
			return false;
		}
		if (vmax.getIsErroneous(timestamp) || vmax.isUnfoldable(timestamp)) {
			// the error was already reported
			return false;
		}

		SubtypeConstraint rangeConstraint;
		switch (subtypeType) {
		case ST_INTEGER: {
			IntegerLimit minLimit;
			if (Value_type.REAL_VALUE.equals(vmin.getValuetype())) {
				Real_Value real = (Real_Value) vmin;
				if (real.isNegativeInfinity()) {
					minLimit = IntegerLimit.MINIMUM;
				} else {
					minLimit = IntegerLimit.MAXIMUM;
				}
			} else {
				minLimit = new IntegerLimit(((Integer_Value) vmin).getValueValue());
			}
			IntegerLimit maxLimit;
			if (Value_type.REAL_VALUE.equals(vmax.getValuetype())) {
				Real_Value real = (Real_Value) vmax;
				if (real.isPositiveInfinity()) {
					maxLimit = IntegerLimit.MAXIMUM;
				} else {
					maxLimit = IntegerLimit.MINIMUM;
				}
			} else {
				maxLimit = new IntegerLimit(((Integer_Value) vmax).getValueValue());
			}

			if (minExclusive) {
				if (minLimit.compareTo(IntegerLimit.MINIMUM) == 0) {
					myOwner.getLocation().reportSemanticError(
							"invalid lower boundary, -infinity cannot be excluded from an integer subtype range");
					return false;
				}

				if (minLimit.compareTo(IntegerLimit.MAXIMUM) == 0) {
					myOwner.getLocation().reportSemanticError("!infinity is not a valid lower boundary");
					return false;
				}
				minLimit = (IntegerLimit) minLimit.increment();
			}
			if (maxExclusive) {
				if (maxLimit.compareTo(IntegerLimit.MAXIMUM) == 0) {
					myOwner.getLocation().reportSemanticError(
							"invalid upper boundary, infinity cannot be excluded from an integer subtype range");
					return false;
				}

				if (maxLimit.compareTo(IntegerLimit.MINIMUM) == 0) {
					myOwner.getLocation().reportSemanticError("!-infinity is not a valid upper boundary");
					return false;
				}
				maxLimit = (IntegerLimit) maxLimit.decrement();
			}
			if (maxLimit.compareTo(minLimit) < 0) {
				Location.interval(min.getLocation(), max.getLocation()).reportSemanticError(
						"lower boundary is bigger than upper boundary in integer subtype range");
				return false;
			}
			rangeConstraint = new RangeListConstraint(minLimit, maxLimit);
			break;
		}
		case ST_FLOAT: {
			if (Double.isNaN(((Real_Value) vmin).getValue())) {
				min.getLocation().reportSemanticError("lower boundary cannot be not_a_number in float subtype range");
				return false;
			}
			if (Double.isNaN(((Real_Value) vmax).getValue())) {
				max.getLocation().reportSemanticError("upper boundary cannot be not_a_number in float subtype range");
				return false;
			}
			RealLimit minLimit = new RealLimit(((Real_Value) vmin).getValue());
			RealLimit maxLimit = new RealLimit(((Real_Value) vmax).getValue());
			if (minExclusive) {
				if (minLimit.compareTo(RealLimit.MAXIMUM) == 0) {
					myOwner.getLocation().reportSemanticError("!infinity is not a valid lower boundary");
					return false;
				}
				minLimit = (RealLimit) minLimit.increment();
			}
			if (maxExclusive) {
				if (maxLimit.compareTo(RealLimit.MINIMUM) == 0) {
					myOwner.getLocation().reportSemanticError("!-infinity is not a valid upper boundary");
					return false;
				}
				maxLimit = (RealLimit) maxLimit.decrement();
			}
			if (maxLimit.compareTo(minLimit) < 0) {
				Location.interval(min.getLocation(), max.getLocation()).reportSemanticError(
						"lower boundary is bigger than upper boundary in float subtype range");
				return false;
			}
			rangeConstraint = new RealRangeListConstraint(minLimit, maxLimit);
			break;
		}
		case ST_CHARSTRING: {
			boolean erroneous = false;
			if (Value_type.REAL_VALUE.equals(vmin.getValuetype()) && ((Real_Value) vmin).isNegativeInfinity()) {
				getParsedLocation().reportSemanticError("lower boundary of a charstring subtype range cannot be -infinity");
				erroneous = true;
			}
			if (Value_type.REAL_VALUE.equals(vmax.getValuetype()) && ((Real_Value) vmax).isPositiveInfinity()) {
				getParsedLocation().reportSemanticError("upper boundary of a charstring subtype range cannot be infinity");
				erroneous = true;
			}
			if (erroneous) {
				return false;
			}
			String minString;
			switch (vmin.getValuetype()) {
			case CHARSTRING_VALUE:
				minString = ((Charstring_Value) vmin).getValue();
				break;
			case UNIVERSALCHARSTRING_VALUE: {
				UniversalCharstring ustr = ((UniversalCharstring_Value) vmin).getValue();
				if ((ustr.length() < 1) || !ustr.get(0).isValidChar()) {
					min.getLocation().reportSemanticError("lower boundary of charstring subtype range is not a valid char");
					return false;
				}
				minString = String.valueOf((char) ustr.get(0).cell());
				break;
			}
			default:
				return false;
			}
			String maxString;
			switch (vmax.getValuetype()) {
			case CHARSTRING_VALUE:
				maxString = ((Charstring_Value) vmax).getValue();
				break;
			case UNIVERSALCHARSTRING_VALUE: {
				UniversalCharstring ustr = ((UniversalCharstring_Value) vmax).getValue();
				if ((ustr.length() < 1) || !ustr.get(0).isValidChar()) {
					max.getLocation().reportSemanticError("upper boundary of charstring subtype range is not a valid char");
					return false;
				}
				maxString = String.valueOf((char) ustr.get(0).cell());
				break;
			}
			default:
				return false;
			}
			if (minString.length() != 1) {
				min.getLocation().reportSemanticError("lower boundary of charstring subtype range must be a single element string");
				return false;
			}
			if (maxString.length() != 1) {
				max.getLocation().reportSemanticError("upper boundary of charstring subtype range must be a single element string");
				return false;
			}
			CharLimit minLimit = new CharLimit(minString.charAt(0));
			CharLimit maxLimit = new CharLimit(maxString.charAt(0));
			if (minExclusive) {
				if (minLimit.compareTo(CharLimit.MAXIMUM) == 0) {
					min.getLocation().reportSemanticError("exclusive lower boundary is not a legal charstring character");
					return false;
				}
				minLimit = (CharLimit) minLimit.increment();
			}
			if (maxExclusive) {
				if (maxLimit.compareTo(CharLimit.MINIMUM) == 0) {
					max.getLocation().reportSemanticError("exclusive upper boundary is not a legal charstring character");
					return false;
				}
				maxLimit = (CharLimit) maxLimit.decrement();
			}
			if (maxLimit.compareTo(minLimit) < 0) {
				Location.interval(min.getLocation(), max.getLocation()).reportSemanticError(
						"lower boundary is bigger than upper boundary in charstring subtype range");
				return false;
			}
			rangeConstraint = new StringSetConstraint(StringSubtypeTreeElement.StringType.CHARSTRING,
					StringSetConstraint.ConstraintType.ALPHABET_CONSTRAINT, new RangeListConstraint(minLimit, maxLimit));
			break;
		}
		case ST_UNIVERSAL_CHARSTRING: {
			boolean erroneous = false;
			if (Value_type.REAL_VALUE.equals(vmin.getValuetype()) && ((Real_Value) vmin).isNegativeInfinity()) {
				min.getLocation().reportSemanticError("lower boundary of a universal charstring subtype range cannot be -infinity");
				erroneous = true;
			}
			if (Value_type.REAL_VALUE.equals(vmax.getValuetype()) && ((Real_Value) vmax).isPositiveInfinity()) {
				max.getLocation().reportSemanticError("upper boundary of a universal charstring subtype range cannot be infinity");
				erroneous = true;
			}
			if (erroneous) {
				return false;
			}
			UniversalCharstring minString;
			switch (vmin.getValuetype()) {
			case CHARSTRING_VALUE:
				minString = new UniversalCharstring(((Charstring_Value) vmin).getValue());
				break;
			case UNIVERSALCHARSTRING_VALUE:
				minString = ((UniversalCharstring_Value) vmin).getValue();
				break;
			default:
				return false;
			}
			UniversalCharstring maxString;
			switch (vmax.getValuetype()) {
			case CHARSTRING_VALUE:
				maxString = new UniversalCharstring(((Charstring_Value) vmax).getValue());
				break;
			case UNIVERSALCHARSTRING_VALUE:
				maxString = ((UniversalCharstring_Value) vmax).getValue();
				break;
			default:
				return false;
			}
			if (minString.length() != 1) {
				min.getLocation().reportSemanticError(
						"lower boundary of universal charstring subtype range must be a single element string");
				return false;
			}
			if (maxString.length() != 1) {
				max.getLocation().reportSemanticError(
						"upper boundary of universal charstring subtype range must be a single element string");
				return false;
			}
			UCharLimit minLimit = new UCharLimit(minString.get(0));
			UCharLimit maxLimit = new UCharLimit(maxString.get(0));
			if (minExclusive) {
				if (minLimit.compareTo(UCharLimit.MAXIMUM) == 0) {
					min.getLocation().reportSemanticError(
							"exclusive lower boundary is not a legal universal charstring character");
					return false;
				}
				minLimit = (UCharLimit) minLimit.increment();
			}
			if (maxExclusive) {
				if (maxLimit.compareTo(UCharLimit.MINIMUM) == 0) {
					max.getLocation().reportSemanticError(
							"exclusive upper boundary is not a legal universal charstring character");
					return false;
				}
				maxLimit = (UCharLimit) maxLimit.decrement();
			}
			if (maxLimit.compareTo(minLimit) < 0) {
				Location.interval(min.getLocation(), max.getLocation()).reportSemanticError(
						"lower boundary is bigger than upper boundary in universal charstring subtype range");
				return false;
			}
			rangeConstraint = new StringSetConstraint(StringSubtypeTreeElement.StringType.UNIVERSAL_CHARSTRING,
					StringSetConstraint.ConstraintType.ALPHABET_CONSTRAINT, new RangeListConstraint(minLimit, maxLimit));
			break;
		}
		default:
			ErrorReporter.INTERNAL_ERROR();
			return false;
		}
		subtypeConstraint = (subtypeConstraint == null) ? rangeConstraint : subtypeConstraint.union(rangeConstraint);
		return true;
	}

	private boolean checkBoundaryValid(final IValue boundary, final String boundaryName) {
		BigInteger lowerInt = ((Integer_Value) boundary).getValueValue();
		if (lowerInt.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
			boundary.getLocation().reportSemanticError(
					MessageFormat.format("The {0} should be less than `{1}'' instead of `{2}''", boundaryName, Integer.MAX_VALUE,
							lowerInt));
			return false;
		}
		return true;
	}

	private boolean setTtcnLength(final SizeLimit min, final SizeLimit max) {
		SubtypeConstraint sc;
		switch (subtypeType) {
		case ST_BITSTRING:
			sc = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.BITSTRING, min, max);
			break;
		case ST_HEXSTRING:
			sc = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.HEXSTRING, min, max);
			break;
		case ST_OCTETSTRING:
			sc = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.OCTETSTRING, min, max);
			break;
		case ST_CHARSTRING:
			sc = new StringSetConstraint(StringSubtypeTreeElement.StringType.CHARSTRING,
					StringSetConstraint.ConstraintType.SIZE_CONSTRAINT, new RangeListConstraint(min, max));
			break;
		case ST_UNIVERSAL_CHARSTRING:
			sc = new StringSetConstraint(StringSubtypeTreeElement.StringType.UNIVERSAL_CHARSTRING,
					StringSetConstraint.ConstraintType.SIZE_CONSTRAINT, new RangeListConstraint(min, max));
			break;
		case ST_RECORDOF:
		case ST_SETOF:
			sc = new ValueListAndSizeConstraint(min, max);
			break;
		default:
			myOwner.getLocation().reportSemanticError(
					MessageFormat.format("Length subtyping is not allowed for type `{0}''", myOwner.getTypename()));
			return false;
		}
		subtypeConstraint = (subtypeConstraint == null) ? sc : subtypeConstraint.intersection(sc);
		RangeListConstraint lr = new RangeListConstraint(min, max);
		lengthRestriction = ((lengthRestriction == null) ? lr : lengthRestriction.intersection(lr));
		return true;
	}

	private boolean addTtcnLength(final CompilationTimeStamp timestamp, final LengthRestriction lengthRestriction, final int restrictionIndex) {
		lengthRestriction.setMyScope(myOwner.getMyScope());
		BridgingNamedNode bridge = new BridgingNamedNode(myOwner, myOwner.getFullName() + ".<length_restriction_" + restrictionIndex + ">");
		lengthRestriction.setFullNameParent(bridge);
		lengthRestriction.check(timestamp, Expected_Value_type.EXPECTED_CONSTANT);

		IValue lower = null, upper = null;
		if (lengthRestriction instanceof SingleLenghtRestriction) {
			lower = ((SingleLenghtRestriction) lengthRestriction).getRestriction(timestamp);
			if (lower == null || lower.getIsErroneous(timestamp) || !Value_type.INTEGER_VALUE.equals(lower.getValuetype())
					|| lower.isUnfoldable(timestamp)) {
				return false;
			}
			if (!checkBoundaryValid(lower, "length restriction value")) {
				return false;
			}
			SizeLimit boundaryLimit = new SizeLimit(((Integer_Value) lower).getValueValue());
			return setTtcnLength(boundaryLimit, boundaryLimit);
		}

		lower = ((RangeLenghtRestriction) lengthRestriction).getLowerValue(timestamp);
		if (lower == null || lower.getIsErroneous(timestamp) || !Value_type.INTEGER_VALUE.equals(lower.getValuetype()) || lower.isUnfoldable(timestamp)) {
			return false;
		}
		if (!checkBoundaryValid(lower, "lower boundary")) {
			return false;
		}
		upper = ((RangeLenghtRestriction) lengthRestriction).getUpperValue(timestamp);
		if (upper != null) {
			if (upper.getMyScope() == null) {
				upper.setMyScope(myOwner.getMyScope());
			}
			if (upper.getIsErroneous(timestamp) || !Value_type.INTEGER_VALUE.equals(upper.getValuetype())
					|| upper.isUnfoldable(timestamp)) {
				return false;
			}
			if (!checkBoundaryValid(upper, "upper boundary")) {
				return false;
			}
			return setTtcnLength(new SizeLimit(((Integer_Value) lower).getValueValue()),
					new SizeLimit(((Integer_Value) upper).getValueValue()));
		}

		// upper is infinity
		return setTtcnLength(new SizeLimit(((Integer_Value) lower).getValueValue()), SizeLimit.MAXIMUM);
	}

	private boolean addTtcnPattern(final CompilationTimeStamp timestamp, final PatternString pattern, final int restrictionIndex) {
		// TODO set scope, fullname
		SubtypeConstraint sc;
		switch (subtypeType) {
		case ST_CHARSTRING:
			// TODO check pattern
			sc = new StringSetConstraint(StringSubtypeTreeElement.StringType.CHARSTRING,
					StringSetConstraint.ConstraintType.PATTERN_CONSTRAINT, new StringPatternConstraint(pattern));
			break;
		case ST_UNIVERSAL_CHARSTRING:
			// TODO check pattern
			sc = new StringSetConstraint(StringSubtypeTreeElement.StringType.UNIVERSAL_CHARSTRING,
					StringSetConstraint.ConstraintType.PATTERN_CONSTRAINT, new StringPatternConstraint(pattern));
			break;
		default:
			myOwner.getLocation().reportSemanticError(
					MessageFormat.format("Pattern subtyping of type `{0}'' is not allowed", myOwner.getTypename()));
			return false;
		}
		subtypeConstraint = (subtypeConstraint == null) ? sc : subtypeConstraint.intersection(sc);
		return true;
	}

	/**
	 * Does the semantic checking of the sub-type.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		lastTimeChecked = timestamp;

		if (parsedRestrictions != null) {
			int addedCount = 0;
			boolean hasSingle = false, hasRange = false;
			for (int i = 0, size = parsedRestrictions.size(); i < size; i++) {
				boolean added = false;
				ParsedSubType parsed = parsedRestrictions.get(i);
				switch (parsed.getSubTypetype()) {
				case SINGLE_PARSEDSUBTYPE:
					hasSingle = true;
					added = addTtcnSingle(timestamp, ((Single_ParsedSubType) parsed).getValue(), i);
					break;
				case RANGE_PARSEDSUBTYPE:
					hasRange = true;
					Range_ParsedSubType rpst = (Range_ParsedSubType) parsed;
					added = addTtcnRange(timestamp, rpst.getMin(), rpst.getMinExclusive(), rpst.getMax(), rpst.getMaxExclusive(),
							i);
					break;
				case LENGTH_PARSEDSUBTYPE:
					added = addTtcnLength(timestamp, ((Length_ParsedSubType) parsed).getLength(), i);
					break;
				case PATTERN_PARSEDSUBTYPE:
					added = addTtcnPattern(timestamp, ((Pattern_ParsedSubType) parsed).getPattern(), i);
					break;
				default:
					ErrorReporter.INTERNAL_ERROR();
				}
				if (added) {
					addedCount++;
				}
			}
			switch (subtypeType) {
			case ST_CHARSTRING:
			case ST_UNIVERSAL_CHARSTRING:
				if (hasSingle && hasRange) {
					myOwner.getLocation().reportSemanticError(
							MessageFormat.format(
									"Mixing of value list and range subtyping is not allowed for type `{0}''",
									myOwner.getTypename()));
					isErroneous = true;
					return;
				}
				break;
			default:
				// in other cases mixing of different
				// restrictions (which are legal for
				// this type) is properly regulated by the
				// TTCN-3 BNF itself
				break;
			}
			if (addedCount < parsedRestrictions.size()) {
				isErroneous = true;
				return;
			}
			if (getIsErroneous(timestamp)) {
				return;
			}
		}

		// if there is a parent sub-type then check if own sub-type is a
		// subset of it,
		// if unknown then remain silent
		// create the intersection of the two sub-types
		if ((parentSubtype != null) && !parentSubtype.getIsErroneous(timestamp)) {
			// check for circular sub-type reference
			if (!addParentSubtype(parentSubtype)) {
				isErroneous = true;
				return;
			}

			if (parentSubtype.subtypeType != subtypeType) {
				ErrorReporter.INTERNAL_ERROR();
				return;
			}

			if (parentSubtype.subtypeConstraint != null) {
				if (subtypeConstraint == null) {
					subtypeConstraint = parentSubtype.subtypeConstraint;
				} else {
					// both own and inherited sub-type constraints exist
					if (subtypeConstraint.isSubset(parentSubtype.subtypeConstraint) == TernaryBool.TFALSE) {
						final String message = MessageFormat
								.format("The subtype restriction is not a subset of the restriction on the parent type. Subtype {0} is not subset of subtype {1}",
										subtypeConstraint.toString(),
										parentSubtype.subtypeConstraint.toString());
						getParsedLocation().reportSemanticError(message);
						isErroneous = true;
						return;
					}
					subtypeConstraint = subtypeConstraint.intersection(parentSubtype.subtypeConstraint);
				}
			}

			if (parentSubtype.lengthRestriction != null) {
				if (lengthRestriction == null) {
					lengthRestriction = parentSubtype.lengthRestriction;
				} else {
					lengthRestriction = lengthRestriction.intersection(parentSubtype.lengthRestriction);
				}
			}
		}

		// check if sub-type is valid: it must not be an empty set
		// (is_empty==TTRUE)
		// issue warning if sub-type is given but is full set
		// (is_full==TTRUE)
		// ignore cases of TUNKNOWN when compiler can't figure out if
		// the aggregate
		// set is empty or full
		if (subtypeConstraint != null) {
			if (subtypeConstraint.isEmpty() == TernaryBool.TTRUE) {
				getParsedLocation().reportSemanticError("The subtype is an empty set");
				isErroneous = true;
				return;
			}
			if (subtypeConstraint.isFull() == TernaryBool.TTRUE) {
				getParsedLocation().reportSemanticWarning(
						MessageFormat.format(
								"The subtype of type `{0}'' is a full set, it does not constrain the root type.",
								myOwner.getTypename()));
				subtypeConstraint = null;
			}
		}

		if ((lengthRestriction != null) && (lengthRestriction.isFull() == TernaryBool.TTRUE)) {
			lengthRestriction = null;
		}
	}

	/**
	 * Checks if a given value is valid according to this sub-type.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param value
	 *                the value to be checked
	 * */
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value) {

		if (getIsErroneous(timestamp) || (subtypeConstraint == null)) {
			return;
		}
		if (value.getIsErroneous(timestamp)) {
			return;
		}

		IValue last = value.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
		if (last.getIsErroneous(timestamp)) {
			return;
		}
		boolean isValid = true;
		switch (last.getValuetype()) {
		case INTEGER_VALUE:
			if (subtypeType != SubType_type.ST_INTEGER) {
				ErrorReporter.INTERNAL_ERROR();
				return;
			}
			isValid = subtypeConstraint.isElement(new IntegerLimit(((Integer_Value) last).getValueValue()));
			break;
		case REAL_VALUE:
			if (subtypeType == SubType_type.ST_FLOAT) {
				isValid = subtypeConstraint.isElement(((Real_Value) last).getValue());
				break;
			} else if (subtypeType == SubType_type.ST_INTEGER) {
				Real_Value real = (Real_Value) last;
				if (real.isNegativeInfinity()) {
					isValid = subtypeConstraint.isElement(IntegerLimit.MINIMUM);
					break;
				} else if (real.isPositiveInfinity()) {
					isValid = subtypeConstraint.isElement(IntegerLimit.MAXIMUM);
					break;
				}
			}
			ErrorReporter.INTERNAL_ERROR();
			return;
		case BOOLEAN_VALUE:
			if (subtypeType != SubType_type.ST_BOOLEAN) {
				ErrorReporter.INTERNAL_ERROR();
				return;
			}
			isValid = subtypeConstraint.isElement(((Boolean_Value) last).getValue());
			break;
		case VERDICT_VALUE:
			if (subtypeType != SubType_type.ST_VERDICTTYPE) {
				ErrorReporter.INTERNAL_ERROR();
				return;
			}
			isValid = subtypeConstraint.isElement(((Verdict_Value) last).getValue());
			break;
		case BITSTRING_VALUE:
			if (subtypeType != SubType_type.ST_BITSTRING) {
				ErrorReporter.INTERNAL_ERROR();
				return;
			}
			isValid = subtypeConstraint.isElement(((Bitstring_Value) last).getValue());
			break;
		case HEXSTRING_VALUE:
			if (subtypeType != SubType_type.ST_HEXSTRING) {
				ErrorReporter.INTERNAL_ERROR();
				return;
			}
			isValid = subtypeConstraint.isElement(((Hexstring_Value) last).getValue());
			break;
		case OCTETSTRING_VALUE:
			if (subtypeType != SubType_type.ST_OCTETSTRING) {
				ErrorReporter.INTERNAL_ERROR();
				return;
			}
			isValid = subtypeConstraint.isElement(((Octetstring_Value) last).getValue());
			break;
		case CHARSTRING_VALUE:
			switch (subtypeType) {
			case ST_CHARSTRING:
				isValid = subtypeConstraint.isElement(((Charstring_Value) last).getValue());
				break;
			case ST_UNIVERSAL_CHARSTRING:
				isValid = subtypeConstraint.isElement(new UniversalCharstring(((Charstring_Value) last).getValue()));
				break;
			default:
				ErrorReporter.INTERNAL_ERROR();
				return;
			}
			break;
		case UNIVERSALCHARSTRING_VALUE:
			if (subtypeType != SubType_type.ST_UNIVERSAL_CHARSTRING) {
				ErrorReporter.INTERNAL_ERROR();
				return;
			}
			isValid = subtypeConstraint.isElement(((UniversalCharstring_Value) last).getValue());
			break;
		case SEQUENCEOF_VALUE:
		case SETOF_VALUE:
		case OBJECTID_VALUE:
		case ENUMERATED_VALUE:
		case CHOICE_VALUE:
		case SEQUENCE_VALUE:
		case SET_VALUE:
		case FUNCTION_REFERENCE_VALUE:
		case ALTSTEP_REFERENCE_VALUE:
		case TESTCASE_REFERENCE_VALUE:
			if (value.isUnfoldable(timestamp)) {
				return;
			}
			isValid = subtypeConstraint.isElement(last);
			break;
		default:
			return;
		}
		if (!isValid) {
			value.getLocation().reportSemanticError(
					MessageFormat.format("{0} is not a valid value for type `{1}'' which has subtype {2}",
							last.createStringRepresentation(), myOwner.getTypename(), subtypeConstraint.toString()));
		}
	}

	/**
	 * Does the semantic checking of the provided template according to the
	 * a specific sub-type.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param template
	 *                the template to be checked by the type.
	 * */
	public void checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final ITTCN3Template template) {
		if (getIsErroneous(timestamp) || (subtypeConstraint == null)) {
			return;
		}
		if (template.getIsErroneous(timestamp)) {
			return;
		}
		TTCN3Template t = template.getTemplateReferencedLast(timestamp);
		if (t.getIsErroneous(timestamp)) {
			return;
		}
		switch (t.getTemplatetype()) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
		case ANY_VALUE:
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
		case SPECIFIC_VALUE:
		case TEMPLATE_REFD:
		case TEMPLATE_INVOKE:
			break;
		case TEMPLATE_LIST:
			if ((subtypeType == SubType_type.ST_RECORDOF) || (subtypeType == SubType_type.ST_SETOF)) {
				if ((lengthRestriction == null) || (lengthRestriction.isEmpty() == TernaryBool.TTRUE)) {
					break;
				}
				SizeLimit minLimit = (SizeLimit) lengthRestriction.getMinimal();
				SizeLimit maxLimit = (SizeLimit) lengthRestriction.getMaximal();
				Template_List list = (Template_List) template;
				int fixComponents = list.getNofTemplatesNotAnyornone(timestamp);
				if (!list.templateContainsAnyornone() && (fixComponents < minLimit.getSize().intValue())) {
					template.getLocation().reportSemanticError(
							MessageFormat.format("At least {0} elements must be present in the list", minLimit.getSize()
									.intValue()));
					return;
				} else if (!maxLimit.getInfinity() && (fixComponents > maxLimit.getSize().intValue())) {
					template.getLocation().reportSemanticError(
							MessageFormat.format("There must not be more than {0} elements in the list", maxLimit
									.getSize().intValue()));
					return;
				}
			}
			// FIXME implement checking of template_list for record
			// and set types
			break;
		case INDEXED_TEMPLATE_LIST:
		case NAMED_TEMPLATE_LIST:
		case VALUE_RANGE:
			// FIXME implement checking
			break;
		case SUPERSET_MATCH: {
			if (subtypeType != SubType_type.ST_SETOF) {
				template.getLocation().reportSemanticError(
						"'superset' template matching mechanism can be used only with 'set of' types");
			}
			SupersetMatch_Template temp = (SupersetMatch_Template) template;
			for (int i = 0, size = temp.getNofTemplates(); i < size; i++) {
				checkThisTemplateGeneric(timestamp, temp.getTemplateByIndex(i));
			}
			break;
		}
		case SUBSET_MATCH: {
			if (subtypeType != SubType_type.ST_SETOF) {
				template.getLocation().reportSemanticError(
						"'subset' template matching mechanism can be used only with 'set of' types");
			}
			SubsetMatch_Template temp = (SubsetMatch_Template) template;
			for (int i = 0, size = temp.getNofTemplates(); i < size; i++) {
				checkThisTemplateGeneric(timestamp, temp.getTemplateByIndex(i));
			}
			break;
		}
		case BSTR_PATTERN:
			checkThisTemplatePattern(template, "bitstring", ((BitString_Pattern_Template) template).getMinLengthOfPattern(),
					((BitString_Pattern_Template) template).containsAnyornoneSymbol());
			break;
		case HSTR_PATTERN:
			checkThisTemplatePattern(template, "hexstring", ((HexString_Pattern_Template) template).getMinLengthOfPattern(),
					((HexString_Pattern_Template) template).containsAnyornoneSymbol());
			break;
		case OSTR_PATTERN:
			checkThisTemplatePattern(template, "octetstring", ((OctetString_Pattern_Template) template).getMinLengthOfPattern(),
					((OctetString_Pattern_Template) template).containsAnyornoneSymbol());
			break;
		case CSTR_PATTERN:
			checkThisTemplatePattern(template, "charstring", ((CharString_Pattern_Template) template).getMinLengthOfPattern(),
					((CharString_Pattern_Template) template).patternContainsAnyornoneSymbol());
			break;
		case USTR_PATTERN:
			checkThisTemplatePattern(template, "universal charstring",
					((UnivCharString_Pattern_Template) template).getMinLengthOfPattern(),
					((UnivCharString_Pattern_Template) template).patternContainsAnyornoneSymbol());
			break;
		default:
			break;
		}

		checkThisTemplateLengthRestriction(timestamp, t);
	}

	private void checkThisTemplateLengthRestriction(final CompilationTimeStamp timestamp, final TTCN3Template template) {
		LengthRestriction lengthRestriction = template.getLengthRestriction();
		if ((lengthRestriction == null) || (subtypeConstraint == null)) {
			return;
		}
		// if there is a length restriction on the template then check
		// if
		// the intersection of the two restrictions is not empty
		SizeLimit tmplMinLen, tmplMaxLen;
		lengthRestriction.check(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

		if (lengthRestriction instanceof SingleLenghtRestriction) {
			SingleLenghtRestriction realRestriction = (SingleLenghtRestriction) lengthRestriction;
			IValue lower = realRestriction.getRestriction(timestamp);
			if (lower.getIsErroneous(timestamp)) {
				return;
			}
			IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IValue last = lower.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, chain);
			chain.release();
			if (!Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
				return;
			}
			BigInteger length = ((Integer_Value) last).getValueValue();
			tmplMinLen = new SizeLimit(length);
			tmplMaxLen = tmplMinLen;
		} else {
			RangeLenghtRestriction realRestriction = (RangeLenghtRestriction) lengthRestriction;
			IValue lower = realRestriction.getLowerValue(timestamp);
			IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IValue lastLower = lower.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, chain);
			chain.release();
			if (lastLower.getIsErroneous(timestamp) || !Value_type.INTEGER_VALUE.equals(lastLower.getValuetype())) {
				return;
			}
			IValue upper = realRestriction.getUpperValue(timestamp);
			if (upper == null) {
				return;
			}
			chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IValue lastUpper = upper.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, chain);
			chain.release();
			if (lastUpper.getIsErroneous(timestamp) || !Value_type.INTEGER_VALUE.equals(lastUpper.getValuetype())) {
				return;
			}

			tmplMinLen = new SizeLimit(((Integer_Value) lastLower).getValueValue());
			tmplMaxLen = new SizeLimit(((Integer_Value) lastUpper).getValueValue());
		}

		SubtypeConstraint tmplConstraint;
		switch (subtypeType) {
		case ST_BITSTRING:
			tmplConstraint = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.BITSTRING, tmplMinLen, tmplMaxLen);
			break;
		case ST_HEXSTRING:
			tmplConstraint = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.HEXSTRING, tmplMinLen, tmplMaxLen);
			break;
		case ST_OCTETSTRING:
			tmplConstraint = new StringSizeAndValueListConstraint(StringSizeAndValueListConstraint.Type.OCTETSTRING, tmplMinLen,
					tmplMaxLen);
			break;
		case ST_CHARSTRING:
			tmplConstraint = new StringSetConstraint(StringSubtypeTreeElement.StringType.CHARSTRING,
					StringSetConstraint.ConstraintType.SIZE_CONSTRAINT, new RangeListConstraint(tmplMinLen, tmplMaxLen));
			break;
		case ST_UNIVERSAL_CHARSTRING:
			tmplConstraint = new StringSetConstraint(StringSubtypeTreeElement.StringType.UNIVERSAL_CHARSTRING,
					StringSetConstraint.ConstraintType.SIZE_CONSTRAINT, new RangeListConstraint(tmplMinLen, tmplMaxLen));
			break;
		case ST_RECORDOF:
		case ST_SETOF:
			tmplConstraint = new ValueListAndSizeConstraint(tmplMinLen, tmplMaxLen);
			break;
		default:
			return;
		}
		if (subtypeConstraint.intersection(tmplConstraint).isEmpty() == TernaryBool.TTRUE) {
			template.getLocation().reportSemanticWarning(
					MessageFormat.format("Template's length restriction {0} is outside of the type's subtype constraint {1}",
							(new RangeListConstraint(tmplMinLen, tmplMaxLen)).toString(), subtypeConstraint.toString()));
		}
	}

	private void checkThisTemplatePattern(final ITTCN3Template template, final String pattType, final int pattMinLength,
			final boolean pattHasAnyornone) {
		if ((lengthRestriction == null) || (lengthRestriction.isEmpty() == TernaryBool.TTRUE)) {
			return;
		}
		SizeLimit minLimit = (SizeLimit) lengthRestriction.getMinimal();
		SizeLimit maxLimit = (SizeLimit) lengthRestriction.getMaximal();
		if ((pattMinLength < minLimit.getSize().intValue()) && !pattHasAnyornone) {
			template.getLocation().reportSemanticError(
					MessageFormat.format("At least {0} string elements must be present in the {1}",
							minLimit.getSize().intValue(), pattType));
		} else if (!maxLimit.getInfinity() && (pattMinLength > maxLimit.getSize().intValue())) {
			template.getLocation().reportSemanticError(
					MessageFormat.format("There must not be more than {0} string elements in the {1}", maxLimit.getSize()
							.intValue(), pattType));
		}
	}

	@Override
	public String toString() {
		if (isErroneous) {
			return "<erroneous>";
		}
		if (subtypeConstraint != null) {
			return subtypeConstraint.toString();
		}
		return "";
	}

	/**
	 * get the location of the subtype constraints parsed for this type, or
	 * return the owner type location if none were parsed
	 */
	public Location getParsedLocation() {
		if ((parsedRestrictions == null) || parsedRestrictions.isEmpty()) {
			return myOwner.getLocation();
		}
		if (parsedRestrictions.size() == 1) {
			Location loc = parsedRestrictions.get(0).getLocation();
			return (loc == null) ? myOwner.getLocation() : loc;
		}
		Location startLoc = parsedRestrictions.get(0).getLocation();
		Location endLoc = parsedRestrictions.get(parsedRestrictions.size() - 1).getLocation();
		if ((startLoc == null) || (endLoc == null)) {
			return myOwner.getLocation();
		}
		return Location.interval(startLoc, endLoc);
	}

	/**
	 * Handles the incremental parsing of this sub-type.
	 *
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * @return in case of processing error the minimum amount of semantic
	 *         levels that must be destroyed to handle the syntactic
	 *         changes, otherwise 0.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (parsedRestrictions != null) {
			for (int i = 0, size = parsedRestrictions.size(); i < size; i++) {
				ParsedSubType parsed = parsedRestrictions.get(i);
				parsed.updateSyntax(reparser, isDamaged);
			}
		}
	}
}
