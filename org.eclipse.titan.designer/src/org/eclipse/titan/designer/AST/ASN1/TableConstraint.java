/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.AtNotation;
import org.eclipse.titan.designer.AST.AtNotations;
import org.eclipse.titan.designer.AST.BridgingNamedNode;
import org.eclipse.titan.designer.AST.Constraint;
import org.eclipse.titan.designer.AST.Constraints;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldName;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ObjectClassField_Type;
import org.eclipse.titan.designer.AST.ASN1.types.Open_Type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a TableConstraint (SimpleTableConstraint and
 * ComponentRelationConstraint)
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class TableConstraint extends Constraint {
	private static final String FULLNAMEPART = ".<tableconstraint-os>";
	private static final String OCFTEXPECTED = "TableConstraint can only be applied to ObjectClassFieldType";
	private static final String CANNOTDETERMINEPARENT = "Invalid use of ComponentRelationConstraint (cannot determine parent type)";
	private static final String TOOMANYDOTS = "Too many dots. This component has only {0} parameters.";
	private static final String NOCOMPONENTERROR = "Type `{0}'' has no component with name `{1}''.";
	private static final String SECHOEXPECTED = "Type `{0}'' is not a SEQUENCE, SET or CHOICE type";
	private static final String SAMECONSTRAINTEXPECTED = "The referenced components must be value (set) fields"
			+ " constrained by the same objectset as the referencing component";

	
	protected ObjectSet objectSet;
	protected AtNotations atNotationList;
	//TODO: remove if not used
	private Identifier objectClassFieldname;

	private IType constrainedType;

	public TableConstraint() {
		super(Constraint_type.CT_TABLE);
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		parseBlocks();

		if (null == myType) {
			return;
		}

		objectSet.setMyScope(myType.getMyScope());

		BridgingNamedNode bridge = new BridgingNamedNode(this, FULLNAMEPART);
		objectSet.setFullNameParent(bridge);

		// search the constrained type (not the reference to it)
		constrainedType = myType;
		while (true) {
			if (constrainedType.getIsErroneous(timestamp)) {
				return;
			}

			if (Type_type.TYPE_OPENTYPE.equals(constrainedType.getTypetype())
					|| Type_type.TYPE_OBJECTCLASSFIELDTYPE.equals(constrainedType.getTypetype())) {
				break;
			} else if (constrainedType instanceof IReferencingType) {
				IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				constrainedType = ((IReferencingType) constrainedType).getTypeRefd(timestamp, chain);
				chain.release();
			} else {
				myType.getLocation().reportSemanticError(OCFTEXPECTED);
				return;
			}
		}

		if (Type_type.TYPE_OBJECTCLASSFIELDTYPE.equals(constrainedType.getTypetype())) {
			ObjectClassField_Type ocfType = (ObjectClassField_Type) constrainedType;
			objectClassFieldname = ocfType.getObjectClassFieldName();
			objectSet.setMyGovernor(ocfType.getMyObjectClass());
			objectSet.check(timestamp);
			return;
		}

		// opentype
		final Open_Type openType = (Open_Type) constrainedType;
		openType.setMyTableConstraint(this);
		objectClassFieldname = openType.getObjectClassFieldName();
		objectSet.setMyGovernor(openType.getMyObjectClass());
		objectSet.check(timestamp);

		if (null == atNotationList) {
			return;
		}

		// componentrelationconstraint...
		// search the outermost textually enclosing seq, set or choice
		IType outermostParent = null;
		IType tempType = myType;
		do {
			switch (tempType.getTypetype()) {
			case TYPE_ASN1_CHOICE:
			case TYPE_TTCN3_CHOICE:
			case TYPE_OPENTYPE:
			case TYPE_ASN1_SEQUENCE:
			case TYPE_TTCN3_SEQUENCE:
			case TYPE_ASN1_SET:
			case TYPE_TTCN3_SET:
				outermostParent = tempType;
				break;
			default:
				break;
			}

			tempType = tempType.getParentType();
		} while (null != tempType);

		if (null == outermostParent) {
			myType.getLocation().reportSemanticError(CANNOTDETERMINEPARENT);
			return;
		}

		// TODO implement the setting of set_has_openType
		AtNotation atNotation;
		for (int i = 0; i < atNotationList.getNofAtNotations(); i++) {
			atNotation = atNotationList.getAtNotationByIndex(i);

			IType parent = null;
			if (0 == atNotation.getLevels()) {
				parent = outermostParent;
			} else {
				parent = myType;
				for (int level = atNotation.getLevels(); level > 0; level--) {
					parent = parent.getParentType();
					if (null == parent) {
						myType.getLocation().reportSemanticError(MessageFormat.format(TOOMANYDOTS, atNotation.getLevels()));
						return;
					}
				}
			}

			tempType = parent;
			atNotation.setFirstComponent(parent);

			// component identifiers... do they exist?
			FieldName componentIdentifiers = atNotation.getComponentIdentifiers();
			for (int j = 0; j < componentIdentifiers.getNofFields(); j++) {
				Identifier identifier = componentIdentifiers.getFieldByIndex(i);
				switch (tempType.getTypetype()) {
				case TYPE_ASN1_CHOICE: {
					final ASN1_Choice_Type temp2 = (ASN1_Choice_Type) tempType;
					if (temp2.hasComponentWithName(identifier)) {
						tempType = temp2.getComponentByName(identifier).getType();
					} else {
						myType.getLocation().reportSemanticError(
								MessageFormat.format(NOCOMPONENTERROR, tempType.getFullName(),
										identifier.getDisplayName()));
						return;
					}
					break;
				}
				case TYPE_TTCN3_CHOICE: {
					final TTCN3_Choice_Type temp2 = (TTCN3_Choice_Type) tempType;
					if (temp2.hasComponentWithName(identifier.getName())) {
						tempType = temp2.getComponentByName(identifier.getName()).getType();
					} else {
						myType.getLocation().reportSemanticError(
								MessageFormat.format(NOCOMPONENTERROR, tempType.getFullName(),
										identifier.getDisplayName()));
						return;
					}
					break;
				}
				case TYPE_OPENTYPE: {
					final Open_Type temp2 = (Open_Type) tempType;
					if (temp2.hasComponentWithName(identifier)) {
						tempType = temp2.getComponentByName(identifier).getType();
					} else {
						myType.getLocation().reportSemanticError(
								MessageFormat.format(NOCOMPONENTERROR, tempType.getFullName(),
										identifier.getDisplayName()));
						return;
					}
					break;
				}
				case TYPE_ASN1_SEQUENCE: {
					final ASN1_Sequence_Type temp2 = (ASN1_Sequence_Type) tempType;
					if (temp2.hasComponentWithName(identifier)) {
						tempType = temp2.getComponentByName(identifier).getType();
					} else {
						myType.getLocation().reportSemanticError(
								MessageFormat.format(NOCOMPONENTERROR, tempType.getFullName(),
										identifier.getDisplayName()));
						return;
					}
					break;
				}
				case TYPE_TTCN3_SEQUENCE: {
					final TTCN3_Sequence_Type temp2 = (TTCN3_Sequence_Type) tempType;
					if (temp2.hasComponentWithName(identifier.getName())) {
						tempType = temp2.getComponentByName(identifier.getName()).getType();
					} else {
						myType.getLocation().reportSemanticError(
								MessageFormat.format(NOCOMPONENTERROR, tempType.getFullName(),
										identifier.getDisplayName()));
						return;
					}
					break;
				}
				case TYPE_ASN1_SET: {
					final ASN1_Set_Type temp2 = (ASN1_Set_Type) tempType;
					if (temp2.hasComponentWithName(identifier)) {
						tempType = temp2.getComponentByName(identifier).getType();
					} else {
						myType.getLocation().reportSemanticError(
								MessageFormat.format(NOCOMPONENTERROR, tempType.getFullName(),
										identifier.getDisplayName()));
						return;
					}
					break;
				}
				case TYPE_TTCN3_SET: {
					final TTCN3_Set_Type temp2 = (TTCN3_Set_Type) tempType;
					if (temp2.hasComponentWithName(identifier.getName())) {
						tempType = temp2.getComponentByName(identifier.getName()).getType();
					} else {
						myType.getLocation().reportSemanticError(
								MessageFormat.format(NOCOMPONENTERROR, tempType.getFullName(),
										identifier.getDisplayName()));
						return;
					}
					break;
				}
				default:
					myType.getLocation().reportSemanticError(MessageFormat.format(SECHOEXPECTED, tempType.getFullName()));
					return;
				}
			}
			atNotation.setLastComponent(tempType);

			/*
			 * check if the referenced component is constrained by
			 * the same objectset...
			 */
			boolean ok = false;
			Constraints constraints = tempType.getConstraints();
			if (constraints != null) {
				constraints.check(timestamp);
				TableConstraint tableConstraint = constraints.getTableConstraint();
				if (tableConstraint != null) {
					IType ocft = tableConstraint.constrainedType;
					if (Type_type.TYPE_OBJECTCLASSFIELDTYPE.equals(ocft.getTypetype())) {
						atNotation.setObjectClassFieldname(((ObjectClassField_Type) ocft).getObjectClassFieldName());

						IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
						ObjectSet_definition osdef1 = tableConstraint.objectSet.getRefdLast(timestamp, chain);
						chain.release();
						chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
						ObjectSet_definition osdef2 = objectSet.getRefdLast(timestamp, chain);
						chain.release();

						if (osdef1 == osdef2) {
							ok = true;
						}
					}
				}
			}
			if (!ok) {
				myType.getLocation().reportSemanticError(SAMECONSTRAINTEXPECTED);
				return;
			}
		}

		// FIXME enable once all missing parts are filled in,
		// and we become able to consistently reach the referenced types.
		/*
		 * // well, the atnotations seems to be ok, let's produce the alternatives for the opentype openType.clear();
		 *  ASN1Objects objects; ReferenceChain chain =
		 * ReferenceChain.getInstance(ReferenceChain.CIRCULARREFERENCE, true);
		 * objects = objectSet.get_refd_last(timestamp, chain).get_objs();
		 * chain.release();
		 *
		 * for (int i = 0; i < objects.get_nof_objects(); i++) {
		 * Object_Definition obj = objects.get_object_byIndex(i);
		 * if(!obj.has_fieldSetting_withName_default(objectClass_fieldname)){
		 *  continue;
		 *   }
		 *
		 * tempType = (Type)obj.get_setting_byName_default(objectClass_fieldname);
		 *  openType.add_component(new CompField(get_openTypeAlternativeName(timestamp, tempType), tempType, false, false,
		 * null));
		 * 
		 * //FIXME implement
		 * 
		 * }
		 * 
		 * //FIXME maybe something is missing from here
		 * openType.check(timestamp);
		 */
	}

	/*
	 * //FIXME some options are not used as they are not yet available
	 * private Identifier get_openTypeAlternativeName(CompilationTimeStamp
	 * timestamp, Type type) {
	 * 
	 * String s = null;
	 * 
	 * if (Type_type.TYPE_REFERENCED.equals(type.get_typetype())) {
	 * Reference reference = ((Referenced_Type) type).getReference();
	 * 
	 * if (reference == null) {
	 * 
	 * } else { Identifier identifier = reference.getId(); String
	 * displayName = identifier.get_displayName(); if
	 * (displayName.indexOf('.') == -1) { Scope assignmentScope =
	 * reference.get_refd_assignment(timestamp, true).get_my_scope(); if
	 * (assignmentScope.getParentScope() ==
	 * assignmentScope.getModuleScope()) { s = identifier.get_name(); } else
	 * {
	 * 
	 * }
	 * 
	 * } else {
	 * 
	 * } } } else {
	 * 
	 * }
	 * 
	 * if (s == null) { return null; }
	 * 
	 * StringBuilder builder = new StringBuilder(s); builder.setCharAt(0,
	 * Character.toLowerCase(builder.charAt(0))); Identifier tempId = new
	 * Identifier(Identifier_type.ID_NAME, builder.toString()); // This is
	 * because the origin of the returned ID must be ASN. return new
	 * Identifier(Identifier_type.ID_ASN, tempId.get_asnName()); }
	 */
	protected abstract void parseBlocks();

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		// TODO
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		// TODO
		return true;
	}
}
