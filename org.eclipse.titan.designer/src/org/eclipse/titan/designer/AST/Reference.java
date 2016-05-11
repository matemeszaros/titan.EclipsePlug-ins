/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.ISetting.Setting_type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.IParameterisedAssignment;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The Reference class represent a general reference used to refer to an element
 * in the source code.
 * 
 * @author Kristof Szabados
 * */
public class Reference extends ASTNode implements ILocateableNode, IIncrementallyUpdateable, IReferencingElement {
	private static final String STRINGELEMENTUNUSABLE = "Reference to a string element of type `{0}'' cannot be used in this context";
	private static final String VARIABLEXPECTED = "Reference to a variable or value parameter was expected instead of {0}";
	private static final String ALTSTEPEXPECTED = "Reference to an altstep was expected in the argument instead of {0}";

	private static final String FULLNAMEPART = ".<sub_reference";

	public static final String COMPONENTEXPECTED = "component type expected";
	public static final String TYPEEXPECTED = "Type reference expected";
	public static final String ASN1SETTINGEXPECTED = "Reference to ASN.1 setting expected";

	/**
	 * Module identifier. Might be null. In that case it means, that we were
	 * not able to decide if the reference has a module identifier or not
	 * */
	protected Identifier modid;

	/**
	 * The list of sub-references.
	 * <p>
	 * The first element might be the id of the module until the module id
	 * detection is not executed.
	 * */
	protected ArrayList<ISubReference> subReferences;

	private boolean refersToStringElement = false;

	/**
	 * Stores weather we have already tried to detect the module id.
	 * */
	protected boolean detectedModuleId = false;

	/**
	 * indicates if this reference has already been found erroneous in the
	 * actual checking cycle.
	 */
	private boolean isErroneous;
	/**
	 * The assignment referred to by this reference.
	 * */
	protected Assignment referredAssignment;
	protected CompilationTimeStamp lastTimeChecked;

	/**
	 * Stores whether this reference is used on the left hand side of an
	 * assignment or not.
	 */
	private boolean usedOnLeftSide = false;

	/**
	 * Stores whether this reference is used directly in an isbound call or
	 * not.
	 */
	private boolean usedInIsbound = false;

	public Reference(final Identifier modid) {
		this.modid = modid;
		detectedModuleId = modid != null;
		subReferences = new ArrayList<ISubReference>();
	}

	public Reference(final Identifier modid, final List<ISubReference> subReferences) {
		this.modid = modid;
		detectedModuleId = modid != null;
		this.subReferences = new ArrayList<ISubReference>(subReferences);
		this.subReferences.trimToSize();

		for (int i = 0; i < subReferences.size(); i++) {
			subReferences.get(i).setFullNameParent(this);
		}
	}

	/** @return a new instance of this reference */
	public Reference newInstance() {
		return new Reference(modid, subReferences);
	}

	/**
	 * @return the assignment this reference was last evaluated to point to.
	 *         Might be outdated information.
	 * */
	public Assignment getAssOld() {
		return referredAssignment;
	}

	/**
	 * Sets the scope of the base reference without setting the scope of the
	 * sub references.
	 * 
	 * @param scope
	 *                the scope to set.
	 * */
	public void setBaseScope(final Scope scope) {
		super.setMyScope(scope);
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		subReferences.trimToSize();
		for (int i = 0; i < subReferences.size(); i++) {
			subReferences.get(i).setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		for (int i = 0; i < subReferences.size(); i++) {
			if (child == subReferences.get(i)) {
				return builder.append(FULLNAMEPART).append(String.valueOf(i + 1)).append(INamedNode.MORETHAN);
			}
		}

		return builder;
	}

	@Override
	public void setLocation(final Location location) {
		//Do nothing
	}

	// Location is optimized not to store an object at it is not needed
	@Override
	public Location getLocation() {
		Location temp;
		if (modid != null) {
			temp = new Location(modid.getLocation());
		} else if (!subReferences.isEmpty()) {
			temp = new Location(subReferences.get(0).getLocation());
		} else {
			return NULL_Location.INSTANCE;
		}

		if (!subReferences.isEmpty()) {
			temp.setEndOffset(subReferences.get(subReferences.size() - 1).getLocation().getEndOffset());
		}

		return temp;
	}

	public final CompilationTimeStamp getLastTimeChecked() {
		return lastTimeChecked;
	}

	/**
	 * Checks if the reference was evaluated to be erroneous in this time
	 * frame.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @return true if the setting is erroneous, or false otherwise
	 * */
	public final boolean getIsErroneous(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked == null || !lastTimeChecked.isLess(timestamp)) {
			return isErroneous;
		}

		return false;
	}

	/**
	 * Sets the erroneousness of the setting.
	 * 
	 * @param isErroneous
	 *                set the erroneousness property of the references.
	 * */
	public void setIsErroneous(final boolean isErroneous) {
		this.isErroneous = isErroneous;
	}

	/**
	 * @return whether this reference is used on the left hand side of an
	 *         assignment or not.
	 * */
	public boolean getUsedOnLeftHandSide() {
		return usedOnLeftSide;
	}

	/**
	 * Sets this reference to be used on the left hand side of an
	 * assignment.
	 */
	public void setUsedOnLeftHandSide() {
		usedOnLeftSide = true;
	}

	/**
	 * @return whether this reference is used directly in an isbound call or
	 *         not.
	 * */
	public boolean getUsedInIsbound() {
		return usedInIsbound;
	}

	/** Sets that this reference is used directly inside an isbound call */
	public void setUsedInIsbound() {
		usedInIsbound = true;
	}

	/**
	 * Detects and returns the module identifier.
	 * 
	 * @return the module identifier, might be null if the reference did not
	 *         contain one.
	 * */
	public Identifier getModuleIdentifier() {
		detectModid();
		return modid;
	}

	/**
	 * Returns the identifier of the reference.
	 * <p>
	 * Before returning, the module identifier detection is run. After the
	 * module identifier detection, the first sub-reference must be the
	 * identifier of an assignment (if the sub-reference exists).
	 * 
	 * @return the identifier contained in this reference
	 * */
	public Identifier getId() {
		detectModid();

		if (!subReferences.isEmpty()) {
			return subReferences.get(0).getId();
		}

		return null;
	}

	/**
	 * @return the sub-references of this reference, with any previous
	 *         checks.
	 * */
	public List<ISubReference> getSubreferences() {
		return subReferences;
	}

	/**
	 * Collects the sub-references of the reference.
	 * <p>
	 * It is important that the name of the reference is the first member of
	 * the list.
	 * 
	 * @param from
	 *                the first index to include.
	 * @param till
	 *                the last index to include.
	 * @return the sub-references of this reference, with any previous
	 *         checks.
	 * */
	public List<ISubReference> getSubreferences(final int from, final int till) {
		List<ISubReference> result = new ArrayList<ISubReference>();

		for (int i = Math.max(0, from), size = Math.min(subReferences.size() - 1, till); i <= size; i++) {
			result.add(subReferences.get(i));
		}

		return result;
	}

	/**
	 * Adds a sub-reference to the and of the list of sub-references.
	 * 
	 * @param subReference
	 *                the sub-reference to be added.
	 * */
	public void addSubReference(final ISubReference subReference) {
		subReferences.add(subReference);
		subReference.setFullNameParent(this);
	}

	/**
	 * Deletes and returns the last sub-reference.
	 * 
	 * @return the last sub-reference before the deletion, or null if there
	 *         was none.
	 * */
	public ISubReference removeLastSubReference() {
		if (subReferences.isEmpty()) {
			return null;
		}

		ISubReference result = subReferences.remove(subReferences.size() - 1);

		if (subReferences.isEmpty() && modid != null) {
			subReferences.add(new FieldSubReference(modid));
			modid = null;
			detectedModuleId = false;
		}

		return result;
	}

	/**
	 * @return true if the reference references a string element, false
	 *         otherwise.
	 * */
	public boolean refersToStringElement() {
		return refersToStringElement;
	}

	/** set the reference to be referencing string elements. */
	public void setStringElementReferencing() {
		refersToStringElement = true;
	}

	/**
	 * clears previous information on whether the reference to be
	 * referencing string elements or not.
	 */
	public void clearStringElementReferencing() {
		refersToStringElement = false;
	}

	/**
	 * Clears the cache of this reference.
	 **/
	public void clear() {
		referredAssignment = null;
		lastTimeChecked = null;
	}

	/**
	 * Tries to detect if the list of sub-references contains the module
	 * identifier.
	 * <p>
	 * In parsing time the module identifier can only be detected /
	 * separated from sub-references, if the reference contains "objid"s.
	 * */
	public void detectModid() {
		if (detectedModuleId || modid != null) {
			return;
		}

		Identifier firstId = null;
		if (!subReferences.isEmpty() && Subreference_type.fieldSubReference.equals(subReferences.get(0).getReferenceType())) {
			firstId = subReferences.get(0).getId();
			if (subReferences.size() > 1 && !Subreference_type.arraySubReference.equals(subReferences.get(1).getReferenceType())) {
				// there are 3 possible situations:
				// 1. first_id points to a local definition
				// (this has the priority)
				// modid: 0, id: first_id
				// 2. first_id points to an imported module
				// (trivial case)
				// modid: first_id, id: second_id
				// 3. none of the above (first_id might be an
				// imported symbol)
				// modid: 0, id: first_id
				// Note: Rule 1 has the priority because it can
				// be overridden using
				// the notation <id>.objid { ... }.<id> but
				// there is no work-around in the reverse way.
				if (myScope != null && !myScope.hasAssignmentWithId(CompilationTimeStamp.getBaseTimestamp(), firstId)
						&& myScope.isValidModuleId(firstId)) {
					// rule 1 is not fulfilled, but rule 2
					// is fulfilled
					modid = firstId;
					subReferences.remove(0);
				}
			}
		}

		detectedModuleId = true;
	}

	/**
	 * IDentifies and returns the referred setting.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @return the setting referenced or an Error_Setting instance in case
	 *         of an error
	 * */
	public ISetting getRefdSetting(final CompilationTimeStamp timestamp) {
		Assignment assignment = getRefdAssignment(timestamp, true);
		if (assignment != null) {
			return assignment.getSetting(timestamp);
		}

		return new Error_Setting();
	}

	/**
	 * Detects and returns the referred assignment.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param checkParameterList
	 *                whether the parameter list of the reference should be
	 *                checked or not.
	 * 
	 * @return the assignment referred by this reference, or null if not
	 *         found.
	 * */
	public Assignment getRefdAssignment(final CompilationTimeStamp timestamp, final boolean checkParameterList) {
		return getRefdAssignment(timestamp, checkParameterList, null);
	}
		
	
	public Assignment getRefdAssignment(final CompilationTimeStamp timestamp, final boolean checkParameterList, final IReferenceChain referenceChain) {
		if (myScope == null || getId() == null) {
			return null;
		}

		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp) && !checkParameterList) {
			return referredAssignment;
		}
		
		final boolean newChain = null == referenceChain;
		IReferenceChain tempReferenceChain;
		if (newChain) {
			tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		} else {
			tempReferenceChain = referenceChain;
		}

		detectedModuleId = false;
		detectModid();

		referredAssignment = myScope.getAssBySRef(timestamp, this, referenceChain);

		if (referredAssignment != null) {
			referredAssignment.check(timestamp, tempReferenceChain);
			referredAssignment.setUsed();

			if (referredAssignment instanceof Definition) {
				String referingModuleName = getMyScope().getModuleScope().getName();
				if (!((Definition) referredAssignment).referingHere.contains(referingModuleName)) {
					((Definition) referredAssignment).referingHere.add(referingModuleName);
				}
			}
		}

		if (referredAssignment != null && checkParameterList) {
			FormalParameterList formalParameterList = null;

			if (referredAssignment instanceof IParameterisedAssignment) {
				formalParameterList = ((IParameterisedAssignment) referredAssignment).getFormalParameterList();
			}
			if (formalParameterList == null) {
				if (!subReferences.isEmpty() && subReferences.get(0) instanceof ParameterisedSubReference) {
					final String message = MessageFormat.format("The referenced {0} cannot have actual parameters",
							referredAssignment.getDescription());
					getLocation().reportSemanticError(message);
				}
			} else {
				if (!subReferences.isEmpty()) {
					ISubReference firstSubReference = subReferences.get(0);
					if (firstSubReference instanceof ParameterisedSubReference) {
						formalParameterList.check(timestamp, referredAssignment.getAssignmentType());
						isErroneous = ((ParameterisedSubReference) firstSubReference).checkParameters(timestamp,
								formalParameterList);
					} else {
						// if it is not a parameterless
						// template reference pointing
						// to a template having formal
						// parameters, where each has
						// default values
						if (!Assignment.Assignment_type.A_TEMPLATE.equals(referredAssignment.getAssignmentType())
								|| !formalParameterList.hasOnlyDefaultValues()) {
							final String message = MessageFormat.format(
									"Reference to parameterized definition `{0}'' without actual parameter list",
									referredAssignment.getIdentifier().getDisplayName());
							getLocation().reportSemanticError(message);
						}
					}
				}
			}
		}

		lastTimeChecked = timestamp;

		return referredAssignment;
	}

	/**
	 * Returns the referenced declaration. Referenced by the given
	 * sub-reference. If the parameter is null, the module declaration will
	 * be returned.
	 * 
	 * @param subReference
	 * @return The referenced declaration
	 */
	public Declaration getReferencedDeclaration(final ISubReference subReference) {
		final Assignment ass = getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
		if (ass == null) {
			return null;
		}

		if (subReference == null) {
			return Declaration.createInstance(ass.getMyScope().getModuleScope());
		}

		if (subReferences.size() == 1 || subReferences.get(0) == subReference) {
			return Declaration.createInstance(ass);
		}

		final IType assignmentType = ass.getType(CompilationTimeStamp.getBaseTimestamp());
		if ( assignmentType == null ) {
			return null;
		}
		final IType type = assignmentType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

		if (type instanceof IReferenceableElement) {
			IReferenceableElement iTypeWithComponents = (IReferenceableElement) type;
			return iTypeWithComponents.resolveReference(this, 1, subReference);
		}

		return Declaration.createInstance(ass);
	}

	/**
	 * Checks and returns the type of the component referred to by this
	 * reference.
	 * <p>
	 * This is used to detect the type of "runs on" and "system" clause
	 * elements. In any other case a semantic error will be reported.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the type of the referred component or null in case of
	 *         problems.
	 * */
	public final Component_Type chkComponentypeReference(final CompilationTimeStamp timestamp) {
		Assignment assignment = getRefdAssignment(timestamp, true);
		if (assignment != null) {
			if (Assignment_type.A_TYPE.equals(assignment.getAssignmentType())) {
				IType type = assignment.getType(timestamp);
				if (type != null) {
					type = type.getTypeRefdLast(timestamp);
					if (type != null && !type.getIsErroneous(timestamp)) {
						switch (type.getTypetype()) {
						case TYPE_COMPONENT:
							return (Component_Type) type;
						case TYPE_REFERENCED:
							return null;
						default:
							getLocation().reportSemanticError(COMPONENTEXPECTED);
							break;
						}
					}
				}
			} else {
				getLocation().reportSemanticError(TYPEEXPECTED);
			}
		}
		return null;
	}

	/**
	 * Checks and returns the type of the variable referred to by this
	 * reference.
	 * <p>
	 * This is used to detect the type of value redirects.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the type of the referred variable or null in case of
	 *         problems.
	 * */
	public IType checkVariableReference(final CompilationTimeStamp timestamp) {
		Assignment assignment = getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return null;
		}

		IType type;
		switch (assignment.getAssignmentType()) {
		case A_PAR_VAL_IN:
			((FormalParameter) assignment).useAsLValue(this);
			type = ((FormalParameter) assignment).getType(timestamp);
			((FormalParameter) assignment).setUsed();
			break;
		case A_PAR_VAL:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
			type = ((FormalParameter) assignment).getType(timestamp);
			((FormalParameter) assignment).setUsed();
			break;
		case A_VAR:
			type = ((Def_Var) assignment).getType(timestamp);
			((Def_Var) assignment).setUsed();
			break;
		default:
			getLocation().reportSemanticError(MessageFormat.format(VARIABLEXPECTED, assignment.getDescription()));
			return null;
		}

		IType result = type.getFieldType(timestamp, this, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null, false);
		if (result != null && subReferences != null && refersToStringElement()) {
			getLocation().reportSemanticError(MessageFormat.format(STRINGELEMENTUNUSABLE, result.getTypename()));
		}

		return result;
	}

	/**
	 * Checks whether this is a correct altstep activation reference.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return true if the altstep reference is correct, false otherwise.
	 * */
	public final boolean checkActivateArgument(final CompilationTimeStamp timestamp) {
		Assignment assignment = getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return false;
		}

		if (!Assignment_type.A_ALTSTEP.equals(assignment.getAssignmentType())) {
			getLocation().reportSemanticError(MessageFormat.format(ALTSTEPEXPECTED, assignment.getDescription()));
			return false;
		}

		if (myScope != null) {
			myScope.checkRunsOnScope(timestamp, assignment, this, "activate");
		}

		if (!subReferences.isEmpty()) {
			if (Subreference_type.parameterisedSubReference.equals(subReferences.get(0).getReferenceType())) {
				ActualParameterList actualParameters = ((ParameterisedSubReference) subReferences.get(0)).getActualParameters();
				FormalParameterList formalParameterList = ((Def_Altstep) assignment).getFormalParameterList();

				if (formalParameterList != null) {
					return formalParameterList.checkActivateArgument(timestamp, actualParameters, assignment.getDescription());
				}
			}
		}

		return false;
	}

	/**
	 * Checks if the reference has unfoldable index sub-references.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return true if the reference contains unfoldable index
	 *         sub-references, false otherwise.
	 * */
	public final boolean hasUnfoldableIndexSubReference(final CompilationTimeStamp timestamp) {
		for (int i = 0, size = subReferences.size(); i < size; i++) {
			ISubReference subReference = subReferences.get(i);
			if (Subreference_type.arraySubReference.equals(subReference.getReferenceType())) {
				IValue value = ((ArraySubReference) subReference).getValue();
				if (value != null) {
					value = value.setLoweridToReference(timestamp);
					if (value.isUnfoldable(timestamp)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Checks if the reference is actually refering to a setting of the
	 * provided type, or not.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param settingType
	 *                the setting type to check the reference against.
	 * @param referenceChain
	 *                a referencechain to detect cyclic references.
	 * 
	 * @return true if the reference refers to a setting of the provided
	 *         kind, false otherwise.
	 * */
	public boolean refersToSettingType(final CompilationTimeStamp timestamp, final Setting_type settingType, final IReferenceChain referenceChain) {
		if (myScope == null) {
			return Setting_type.S_ERROR.equals(settingType);
		}

		Assignment assignment = getRefdAssignment(timestamp, true);

		if (assignment == null) {
			return false;
		}

		if (!(assignment instanceof ASN1Assignment)) {
			getLocation().reportSemanticError(ASN1SETTINGEXPECTED);
			return false;
		}

		ASN1Assignment asnAssignment = (ASN1Assignment) assignment;

		switch (settingType) {
		case S_OC:
			return asnAssignment.isAssignmentType(timestamp, Assignment_type.A_OC, referenceChain);
		case S_T:
			return asnAssignment.isAssignmentType(timestamp, Assignment_type.A_TYPE, referenceChain);
		case S_O:
			return asnAssignment.isAssignmentType(timestamp, Assignment_type.A_OBJECT, referenceChain);
		case S_V:
			return asnAssignment.isAssignmentType(timestamp, Assignment_type.A_CONST, referenceChain);
		case S_OS:
			return asnAssignment.isAssignmentType(timestamp, Assignment_type.A_OS, referenceChain);
		case S_VS:
			return asnAssignment.isAssignmentType(timestamp, Assignment_type.A_VS, referenceChain);
		case S_ERROR:
			return asnAssignment.getIsErroneous();
		default:
			// FATAL_ERROR
			return false;
		}
	}

	@Override
	public final String toString() {
		return getDisplayName();
	}

	public String getDisplayName() {
		StringBuilder builder = new StringBuilder();
		if (modid != null) {
			builder.append(modid.getDisplayName());
		}
		for (int i = 0; i < subReferences.size(); i++) {
			subReferences.get(i).appendDisplayName(builder);
		}
		return builder.toString();
	}

	/**
	 * Handles the incremental parsing of this reference.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public final void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (modid != null) {
			reparser.updateLocation(modid.getLocation());
		}

		ISubReference subreference;
		for (int i = 0, size = subReferences.size(); i < size; i++) {
			subreference = subReferences.get(i);

			subreference.updateSyntax(reparser, false);
			reparser.updateLocation(subreference.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		for (ISubReference sr : subReferences) {
			sr.findReferences(referenceFinder, foundIdentifiers);
		}
		if (referredAssignment == null) {
			return;
		}
		if (referenceFinder.fieldId == null) {
			// we are searching for the assignment itself
			if (referenceFinder.assignment != referredAssignment) {
				return;
			}
			foundIdentifiers.add(new Hit(getId(), this));
		} else {
			// we are searching for a field of a type
			IType t = referredAssignment.getType(CompilationTimeStamp.getBaseTimestamp());
			if (t == null) {
				return;
			}
			List<IType> typeArray = new ArrayList<IType>();
			boolean success = t.getFieldTypesAsArray(this, 1, typeArray);
			if (!success) {
				// TODO: maybe a partially erroneous reference could be searched too
				return;
			}
			if (subReferences.size() != typeArray.size() + 1) {
				ErrorReporter.INTERNAL_ERROR();
				return;
			}
			for (int i = 1; i < subReferences.size(); i++) {
				if (typeArray.get(i - 1) == referenceFinder.type && !(subReferences.get(i) instanceof ArraySubReference)
						&& subReferences.get(i).getId().equals(referenceFinder.fieldId)) {
					foundIdentifiers.add(new Hit(subReferences.get(i).getId()));
				}
			}
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (modid != null && !modid.accept(v)) {
			return false;
		}

		if (subReferences != null) {
			for (ISubReference sr : subReferences) {
				if (!sr.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public Declaration getDeclaration() {
		return getReferencedDeclaration(null);
	}
}
