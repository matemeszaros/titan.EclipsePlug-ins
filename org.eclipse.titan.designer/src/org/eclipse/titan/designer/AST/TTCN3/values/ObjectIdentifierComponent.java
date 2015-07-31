/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.Defined_Reference;
import org.eclipse.titan.designer.AST.ASN1.values.RelativeObjectIdentifier_Value;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class ObjectIdentifierComponent extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private Identifier name;
	private IValue number;
	private IValue definedValue;

	private Location location = NULL_Location.INSTANCE;

	public static enum oidState_type {
		/** at the beginning */		START,
		/** after itu-t */					ITU,
		/** after iso */					ISO,
		/** after joint iso-itu-t */	JOINT,
		/** after itu-t recom. */		ITU_REC,
		/** later anywhere */			LATER
	}

	private static final class Nameform {
		String name;
		int value;

		public Nameform(final String name, final int value) {
			this.name = name;
			this.value = value;
		}
	}

	private static final Nameform[] NAMES_ROOT = {new Nameform("itu__t", 0), new Nameform("ccitt", 0), new Nameform("iso", 1),
		new Nameform("joint__iso__itu__t", 2), new Nameform("joint__iso__ccitt", 2)};
	private static final Nameform[] NAMES_ITU = {new Nameform("recommendation", 0), new Nameform("question", 1),
		new Nameform("administration", 2), new Nameform("network__operator", 3), new Nameform("identified__organization", 4),
		new Nameform("r__recommendation", 5)};
	private static final Nameform[] NAMES_ISO = {new Nameform("standard", 0), new Nameform("regisztration__authority", 1),
		new Nameform("member__body", 2), new Nameform("identified__organization", 3)};
	// taken from OID repository: http://asn1.elibel.tm.fr/oid/
	private static final Nameform[] NAMES_JOINT = {new Nameform("presentation", 0), new Nameform("asn1", 1),
		new Nameform("association__control", 2), new Nameform("reliable__transfer", 3), new Nameform("remote__operations", 4),
		new Nameform("ds", 5), new Nameform("directory", 5), new Nameform("mhs", 6), new Nameform("mhs__motis", 6), new Nameform("ccr", 7),
		new Nameform("oda", 8), new Nameform("ms", 9), new Nameform("osi__management", 9), new Nameform("transaction__processing", 10),
		new Nameform("dor", 11), new Nameform("distinguished__object__reference", 11), new Nameform("reference__data__transfer", 12),
		new Nameform("network__layer", 13), new Nameform("network__layer__management", 13), new Nameform("transport__layer", 14),
		new Nameform("transport__layer__management", 14), new Nameform("datalink__layer", 15), new Nameform("datalink__layer__management", 15),
		new Nameform("datalink__layer__management__information", 15), new Nameform("country", 16), new Nameform("registration__procedures", 17),
		new Nameform("registration__procedure", 17), new Nameform("physiscal__layer", 18), new Nameform("physical__layer__management", 18),
		new Nameform("mheg", 19), new Nameform("genericULS", 20), new Nameform("generic__upper__layer__security", 20), new Nameform("guls", 20),
		new Nameform("transport__layer__security__protocol", 21), new Nameform("network__layer__security__protocol", 22),
		new Nameform("international__organisations", 23), new Nameform("internationalRA", 23), new Nameform("sios", 24),
		new Nameform("uuid", 25), new Nameform("odp", 26), new Nameform("upu", 40)};

	public ObjectIdentifierComponent(final Identifier name, final IValue number) {
		this.name = name;
		this.number = number;

		if (number != null) {
			number.setFullNameParent(this);
		}
	}

	public ObjectIdentifierComponent(final IValue definedValue) {
		this.definedValue = definedValue;

		if (definedValue != null) {
			definedValue.setFullNameParent(this);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (definedValue != null) {
			definedValue.setMyScope(scope);
		}
		if (number != null) {
			number.setMyScope(scope);
		}
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	/**
	 * Creates and returns a string representation if the component.
	 *
	 * @return the string representation of the component.
	 * */
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		if (number != null) {
			builder.append(number.createStringRepresentation());
		} else if (name != null) {
			builder.append(name.getDisplayName());
		} else if (definedValue != null) {
			builder.append(definedValue.createStringRepresentation());
		} else {
			builder.append("<unknown OID component>");
		}

		return builder.toString();
	}

	/**
	 * Appends its own object identifier component parts to the provided list.
	 *
	 * @param components the list to be extended
	 * */
	public void getOidComponents(final List<Integer> components) {
		if (definedValue != null) {
			if (Value_type.OBJECTID_VALUE.equals(definedValue.getValuetype())) {
				((ObjectIdentifier_Value) definedValue).getOidComponents(components);
			}
		} else if (number != null) {
			if (Value_type.INTEGER_VALUE.equals(number.getValuetype())) {
				components.add(((Integer_Value) number).intValue());
			}
		}
	}

	/**
	 * Check function for object identifier components.
	 *
	 * @param timestamp the timestamp of the actual compilation cycle.
	 * @param parent the parent value.
	 * @param refChain the reference chain used to detect cyclic references.
	 * @param state the state of checking.
	 *
	 * @param the new state after this check was done.
	 * */
	public oidState_type checkOID(final CompilationTimeStamp timestamp, final IReferenceChain refChain, final Value parent, final oidState_type state) {
		if (name != null && number != null) {
			return checkNameAndNumberForm(timestamp, state);
		} else if (name != null) {
			return checkNameForm(timestamp, parent, refChain, state);
		} else if (number != null) {
			return checkNumberFormOID(timestamp, state);
		} else {
			return checkDefdValueOID(timestamp, refChain, state);
		}
	}

	/**
	 * Check function for ROID components.
	 *
	 * @param timestamp the timestamp of the actual compilation cycle.
	 * @param refChain the reference chain used to detect cyclic references.
	 * */
	public void checkROID(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (number != null) {
			checkNumberFormROID(timestamp);
		} else if (definedValue != null) {
			checkDefdValueROID(timestamp, refChain);
		}
	}

	/**
	 * Checks that the identifier in name is a valid name form in the actual state.
	 * Also checks the named form in an OID component.
	 *
	 * @param timestamp the timestamp of the actual compilation cycle.
	 * @param parent the parent value.
	 * @param refChain the reference chain used to detect cyclic references.
	 * @param state the state of checking.
	 *
	 * @param the new state after this check was done.
	 * */
	private oidState_type checkNameForm(final CompilationTimeStamp timestamp, final Value parent, final IReferenceChain refChain,
			final oidState_type state) {
		String nameString = name.getName();
		oidState_type actualState = state;
		int value = -1;
		switch (state) {
		case START:
			if ("itu__t".equals(nameString) || "ccitt".equals(nameString)) {
				actualState = oidState_type.ITU;
				value = 0;
			} else if ("itu__r".equals(nameString)) {
				location.reportSemanticWarning(MessageFormat.format("Identifier `{0}'' should not be used as NameForm", name.getDisplayName()));
				actualState = oidState_type.ITU;
				value = 0;
			} else if ("iso".equals(nameString)) {
				actualState = oidState_type.ISO;
				value = 1;
			} else if ("joint__iso__itu__t".equals(nameString) || "joint__iso__ccitt".equals(nameString)) {
				actualState = oidState_type.JOINT;
				value = 2;
			}
			break;
		case ITU:
			for (int i = 0; i < NAMES_ITU.length; i++) {
				if (nameString.equals(NAMES_ITU[i].name)) {
					value = NAMES_ITU[i].value;
					switch (value) {
					case 0:
						actualState = oidState_type.ITU_REC;
						break;
					case 5:
						location.reportSemanticWarning(
								MessageFormat.format("Identifier `{0}'' should not be used as NumberForm", name.getDisplayName()));
						actualState = oidState_type.LATER;
						break;
					default:
						actualState = oidState_type.LATER;
						break;
					}
				}
			}
			break;
		case ISO:
			for (int i = 0; i < NAMES_ISO.length; i++) {
				if (nameString.equals(NAMES_ISO[i].name)) {
					value = NAMES_ISO[i].value;
					actualState = oidState_type.LATER;
				}
			}
			break;
		case JOINT:
			for (int i = 0; i < NAMES_JOINT.length; i++) {
				if (nameString.equals(NAMES_JOINT[i].name)) {
					value = NAMES_JOINT[i].value;
					actualState = oidState_type.LATER;
					location.reportSemanticWarning(
							MessageFormat.format("Identifier `{0}'' should not be used as NumberForm", name.getDisplayName()));
				}
			}
			break;
		case ITU_REC:
			if (nameString.length() == 1) {
				char c = nameString.charAt(0);
				if (c >= 'a' && c <= 'z') {
					value = c - 'a' + 1;
					actualState = oidState_type.LATER;
				}
			}
			break;
		default:
			break;
		}

		// now we have detected the name form
		if (value < 0) {
			List<ISubReference> newSubreferences = new ArrayList<ISubReference>();
			newSubreferences.add(new FieldSubReference(name));
			Reference reference;
			if (parent.isAsn()) {
				reference = new Defined_Reference(null, newSubreferences);
			} else {
				reference = new Reference(null, newSubreferences);
			}

			IValue newDefinedValue = new Referenced_Value(reference);
			newDefinedValue.setLocation(this.getLocation());
			ObjectIdentifierComponent component = new ObjectIdentifierComponent(newDefinedValue);
			component.setFullNameParent(this);
			component.setMyScope(parent.getMyScope());
			actualState = component.checkDefdValueOID(timestamp, refChain, actualState);
		}

		// the other case is not handled as it would only change a parsed value
		return actualState;
	}

	/**
	 * Checks the defined value form in an OID component.
	 *
	 * @param timestamp the timestamp of the actual compilation cycle.
	 * @param refChain the reference chain used to detect cyclic references.
	 * @param state the state of checking.
	 *
	 * @param the new state after this check was done.
	 * */
	private oidState_type checkDefdValueOID(final CompilationTimeStamp timestamp, final IReferenceChain refChain, final oidState_type state) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue value = definedValue.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (value.getIsErroneous(timestamp)) {
			return oidState_type.LATER;
		}

		switch(value.getValuetype()) {
		case INTEGER_VALUE:
			ObjectIdentifierComponent temp = new ObjectIdentifierComponent(null, definedValue);
			temp.setFullNameParent(this);
			temp.setMyScope(myScope);
			return temp.checkNumberFormOID(timestamp, state);
		case OBJECTID_VALUE:
			if (!oidState_type.START.equals(state)) {
				definedValue.getLocation().reportSemanticError("INTEGER or RELATIVE-OID value was expected");
			}

			((ObjectIdentifier_Value) value).checkOID(timestamp, refChain);
			return oidState_type.LATER;
		case RELATIVEOBJECTIDENTIFIER_VALUE:
			switch (state) {
			case ITU_REC:
				return oidState_type.LATER;
			case LATER:
				return oidState_type.LATER;
			default:
				definedValue.getLocation().reportSemanticError(MessageFormat.format(
						"RELATIVE-OID value cannot be used as the {0} component of an OBJECTIDENTIFIER value",
						oidState_type.START.equals(state) ? "first" : "second"));
				return oidState_type.LATER;
			}
		default:
			if (oidState_type.START.equals(state)) {
				definedValue.getLocation().reportSemanticError("INTEGER or OBJECT IDENTIFIER value was expected for the first component");
			} else {
				definedValue.getLocation().reportSemanticError("INTEGER or RELATIVE-OID value was expected");
			}
			return oidState_type.LATER;
		}
	}

	/**
	 * Checks the defined value form in an ROID component.
	 *
	 * @param timestamp the timestamp of the actual compilation cycle.
	 * @param refChain the reference chain used to detect cyclic references.
	 *
	 * @param the new state after this check was done.
	 * */
	private void checkDefdValueROID(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue value = definedValue.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (value.getIsErroneous(timestamp)) {
			return;
		}

		switch(value.getValuetype()) {
		case INTEGER_VALUE:
			ObjectIdentifierComponent temp = new ObjectIdentifierComponent(null, definedValue);
			temp.setFullNameParent(this);
			temp.setMyScope(myScope);
			temp.checkNumberFormROID(timestamp);
			break;
		case RELATIVEOBJECTIDENTIFIER_VALUE:
			((RelativeObjectIdentifier_Value) value).checkROID(timestamp, referenceChain);
			break;
		default:
			definedValue.getLocation().reportSemanticError("INTEGER or RELATIVE-OID value was expected");
			break;
		}
	}

	/**
	 * Checks the number form (or the number part of name and number form) in an OID component.
	 *
	 * @param timestamp the timestamp of the actual compilation cycle.
	 * @param state the state of checking.
	 *
	 * @param the new state after this check was done.
	 * */
	private oidState_type checkNumberFormOID(final CompilationTimeStamp timestamp,  final oidState_type state) {
		if (number == null) {
			return oidState_type.LATER;
		}

		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = number.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (last.getIsErroneous(timestamp)) {
			return oidState_type.LATER;
		}

		if (!Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
			number.getLocation().reportSemanticError("INTEGER value was expected in the number form");
			return oidState_type.LATER;
		}

		BigInteger value = ((Integer_Value) last).getValueValue();
		if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
			number.getLocation().reportSemanticError(MessageFormat.format(
					"An integer value less then `{0}'' was expected in the number form instead of `{1}''", Integer.MAX_VALUE, value));
			return oidState_type.LATER;
		}

		int value2 = value.intValue();
		switch (state) {
		case START:
			switch (value2) {
			case 0:
				return oidState_type.ITU;
			case 1:
				return oidState_type.ISO;
			case 2:
				return oidState_type.JOINT;
			default:
				number.getLocation().reportSemanticError(MessageFormat.format(
						"The value of first OBJECT IDENTIFIER component must be between 0 and 2 instead of {0}", value2));
				return oidState_type.LATER;
			}
		case ITU:
		case ISO:
			if (value2 < 0 || value2 > 38) {
				number.getLocation().reportSemanticError(MessageFormat.format(
						"The value of second OBJECT IDENTIFIER component must be between 0 and 39 instead of {0}", value2));
			}
			if (oidState_type.ITU.equals(state) && value2 == 0) {
				return oidState_type.ITU_REC;
			}

			return oidState_type.LATER;
		case JOINT:
		default:
			if (value2 < 0) {
				number.getLocation().reportSemanticError(MessageFormat.format("A non-negative integer value was expected instead of {0}", value2));
			}
			return oidState_type.LATER;
		}
	}

	/**
	 * Checks the number form (or the number part of name and number form) in a ROID component.
	 *
	 * @param timestamp the timestamp of the actual compilation cycle.
	 * @param state the state of checking.
	 *
	 * @param the new state after this check was done.
	 * */
	private void checkNumberFormROID(final CompilationTimeStamp timestamp) {
		if (number == null) {
			return;
		}

		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = number.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (last.getIsErroneous(timestamp)) {
			return;
		}

		if (!Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
			number.getLocation().reportSemanticError("INTEGER value was expected in the number form");
			return;
		}

		BigInteger value = ((Integer_Value) last).getValueValue();
		if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
			number.getLocation().reportSemanticError(MessageFormat.format(
					"An integer value less then `{0}'' was expected in the number form instead of `{1}''", Integer.MAX_VALUE, value));
			return;
		} else if (value.compareTo(BigInteger.ZERO) == -1) {
			number.getLocation().reportSemanticError(MessageFormat.format("A non-negative integer value was expected instead of {0}", value));
		}
	}

	/**
	 * Checks if the object identifier component in name and value form is valid.
	 *
	 * @param timestamp the timestamp of the actual compilation cycle.
	 * @param state the state of checking.
	 *
	 * @param the new state after this check was done.
	 * */
	private oidState_type checkNameAndNumberForm(final CompilationTimeStamp timestamp,  final oidState_type state) {
		oidState_type actualState = state;
		actualState = checkNumberFormOID(timestamp, actualState);

		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = number.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (last.getIsErroneous(timestamp)) {
			return actualState;
		}

		if (!Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
			return actualState;
		}

		BigInteger value = ((Integer_Value) last).getValueValue();
		if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
			return actualState;
		}

		String tempName = this.name.getName();
		int tempInt = value.intValue();
		switch (state) {
		case START:
			if (!isValidNameForNumber(tempName, tempInt, NAMES_ROOT)) {
				number.getLocation().reportSemanticWarning(MessageFormat.format(
						"Identifier {0} was expected instead of `{1}'' for number {2} in the NameAndNumberForm as the first OBJECT IDENTIFIER component",
						getExpectedNameForNumber(tempInt, number.isAsn(), NAMES_ROOT), name.getDisplayName(), value));
			}
			break;
		case ITU:
			if (!isValidNameForNumber(tempName, tempInt, NAMES_ITU)) {
				number.getLocation().reportSemanticWarning(MessageFormat.format(
						"Identifier {0} was expected instead of `{1}'' for number {2} in the NameAndNumberForm as the second OBJECT IDENTIFIER component",
						getExpectedNameForNumber(tempInt, number.isAsn(), NAMES_ITU), name.getDisplayName(), value));
			}
			break;
		case ISO:
			if (!isValidNameForNumber(tempName, tempInt, NAMES_ISO)) {
				number.getLocation().reportSemanticWarning(MessageFormat.format(
						"Identifier {0} was expected instead of `{1}'' for number {2} in the NameAndNumberForm as the second OBJECT IDENTIFIER component",
						getExpectedNameForNumber(tempInt, number.isAsn(), NAMES_ISO), name.getDisplayName(), value));
			}
			break;
		case JOINT:
			if (!isValidNameForNumber(tempName, tempInt, NAMES_JOINT)) {
				number.getLocation().reportSemanticWarning(MessageFormat.format(
						"Identifier {0} was expected instead of `{1}'' for number {2} in the NameAndNumberForm as the second OBJECT IDENTIFIER component",
						getExpectedNameForNumber(tempInt, number.isAsn(), NAMES_JOINT), name.getDisplayName(), value));
			}
			break;
		case ITU_REC:
			if (tempInt >= 1 && tempInt <= 26 && (tempName.length() != 1 || tempName.getBytes()[0] != 'a' + tempInt - 1)) {
				number.getLocation().reportSemanticWarning(MessageFormat.format(
						"Identifier {0} was expected instead of `{1}'' for number {2} in the NameAndNumberForm as the third OBJECT IDENTIFIER component",
						('a' + tempInt - 1), name.getDisplayName(), value));
			}
			break;
		default:
			break;
		}

		return actualState;
	}

	/**
	 * Checks whether the provided name and number values are part of the provided name list.
	 *
	 * @param name the name to look for.
	 * @param number the number value to look for.
	 * @param names the list of name-value pairs to search in.
	 *
	 * @return true if there is a name in the list for the value, false otherwise.
	 * */
	private boolean isValidNameForNumber(final String name, final int number , final Nameform[] names) {
		boolean result = true;
		for (int i = 0; i < names.length; i++) {
			if (number == names[i].value) {
				if (name.equals(names[i].name)) {
					return true;
				}

				result = false;
			}
		}
		return result;
	}

	/**
	 * Searches for the appropriate name belonging to the provided number.
	 *
	 * @param number the number to use in the search.
	 * @param asn1 should the found identifier be reported in ASN.1 or TTCN-3 form.
	 * @param names the list to search in.
	 *
	 * @return the name belonging to the provided number.
	 * */
	private String getExpectedNameForNumber(final int number, final boolean asn1, final Nameform[] names) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < names.length; i++) {
			if (number == names[i].value) {
				if (i > 0) {
					builder.append(" or ");
				}

				builder.append('`');
				Identifier identifier = new Identifier(Identifier_type.ID_NAME, names[i].name);
				if (asn1) {
					builder.append(identifier.getAsnName());
				} else {
					builder.append(identifier.getTtcnName());
				}
				builder.append('\'');
			}
		}

		return builder.toString();
	}

	/**
	 *  Handles the incremental parsing of this objid component.
	 *
	 *  @param reparser the parser doing the incremental parsing.
	 *  @param isDamaged true if the location contains the damaged area,
	 *    false if only its' location needs to be updated.
	 *   @return in case of processing error the minimum amount of semantic levels
	 *    that must be destroyed to handle the syntactic changes, otherwise 0.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser,  final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (name != null) {
			reparser.updateLocation(name.getLocation());
		}

		if (number instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) number).updateSyntax(reparser, false);
			reparser.updateLocation(number.getLocation());
		} else if (number != null) {
			throw new ReParseException();
		}

		if (definedValue instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) definedValue).updateSyntax(reparser, false);
			reparser.updateLocation(definedValue.getLocation());
		} else if (definedValue != null) {
			throw new ReParseException();
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (name!=null && !name.accept(v)) {
			return false;
		}
		if (number!=null && !number.accept(v)) {
			return false;
		}
		if (definedValue!=null && !definedValue.accept(v)) {
			return false;
		}
		return true;
	}
}
