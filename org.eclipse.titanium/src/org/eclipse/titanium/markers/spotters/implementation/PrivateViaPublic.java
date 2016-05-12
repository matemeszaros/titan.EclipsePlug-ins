/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks private fields accessing via public (type)definitions. In
 * other words, it should be able to find references pointing to types, that
 * otherwise should be invisible for the actual module. It has got two main
 * parts. PrivateViaPublic.Field class marks references and explicit field
 * assignments. PrivateViaPublic.Value class marks value of private field assignments.
 * 
 * @author Peter Olah
 */
public class PrivateViaPublic {

	private PrivateViaPublic() {
		throw new AssertionError("Noninstantiable");
	}

	private abstract static class Base extends BaseModuleCodeSmellSpotter {

		protected Module actualModule;

		public Base(final CodeSmellType codeSmellType) {
			super(codeSmellType);
		}

		protected boolean isVisibleInActualModule(final Assignment assignment) {
			Module assignmentModule = assignment.getMyScope().getModuleScope();
			return assignmentModule.equals(actualModule) ||
				assignmentModule.isVisible(CompilationTimeStamp.getBaseTimestamp(), actualModule.getIdentifier(), assignment);
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(Module.class);
			return ret;
		}
	}

	public static class Field extends Base {

		private static final String ERROR_MESSAGE = "The {0} field is private but it is accessible because of wrapping into public type.";

		public Field() {
			super(CodeSmellType.PRIVATE_FIELD_VIA_PUBLIC);
		}

		@Override
		protected void process(final IVisitableNode node, final Problems problems) {
			actualModule = (Module) node;
			FieldCollector fieldCollector = new FieldCollector();
			actualModule.accept(fieldCollector);
			check(fieldCollector, problems);
		}

		private class FieldCollector extends ASTVisitor {

			private List<Reference> references;

			private List<NamedValue> namedValues;
						
			public FieldCollector() {
				references = new ArrayList<Reference>();
				namedValues = new ArrayList<NamedValue>();
			}

			@Override
			public int visit(final IVisitableNode node) {
				if (node instanceof Reference) {
					Reference reference = (Reference) node;
					if (reference.getSubreferences().size() > 1) {
						references.add(reference);
					}
				} else if (node instanceof NamedValue) {
					namedValues.add((NamedValue) node);
				}
				return V_CONTINUE;
			}
		}

		protected void check(final FieldCollector fieldCollector, final Problems problems) {
			checkReferences(fieldCollector, problems);
			checkNamedValues(fieldCollector, problems);
		}

		private void checkReferences(final FieldCollector fieldCollector, final Problems problems) {

			Iterator<Reference> referenceIterator = fieldCollector.references.iterator();

			while (referenceIterator.hasNext()) {
				Reference actualReference = referenceIterator.next();

				List<ISubReference> subReferences = new ArrayList<ISubReference>(actualReference.getSubreferences());

				// subReferences.get(0) always irrelevant for us
				if (subReferences.size() > 1) {
					subReferences.remove(0);
				}

				for (int i = 0; i < subReferences.size(); ++i) {

					ISubReference subReference = subReferences.get(i);

					if (subReference.getReferenceType() == Subreference_type.fieldSubReference) {
						Declaration declaration = actualReference.getReferencedDeclaration(subReference);
		
						// Have to check null if no visible elements found
						// if(declaration instanceof FieldDeclaration) {
						if(declaration != null)	{
							Assignment assignment = declaration.getAssignment();

							Identifier identifier = declaration.getIdentifier();

							if (!assignment.getIdentifier().equals(identifier) && (assignment instanceof Def_Type)) {

								IdentifierToDefType identifierToDefType = new IdentifierToDefType(identifier);
								assignment.accept(identifierToDefType);

								if (identifierToDefType.getIsPrivate()) {
									String msg = MessageFormat.format(ERROR_MESSAGE, subReference.getId().getDisplayName());
									problems.report(subReference.getLocation(), msg);
								}
							}
						}
					}
				}
			}
		}

		private void checkNamedValues(final FieldCollector fieldCollector, final Problems problems) {
			Iterator<NamedValue> namedValueIterator = fieldCollector.namedValues.iterator();

			while (namedValueIterator.hasNext()) {

				NamedValue namedValue = namedValueIterator.next();

				IType namedValueType = namedValue.getValue().getMyGovernor();

				if (namedValueType instanceof Referenced_Type) {
					Reference namedValueReference = ((Referenced_Type) namedValueType).getReference();

					Assignment namedValueAssignment = null;

					if (namedValueReference.getSubreferences().size() > 1) {
						INamedNode namedValueTypeRefd = namedValueType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getNameParent();

						if (namedValueTypeRefd instanceof Def_Type) {
							namedValueAssignment = (Assignment) namedValueTypeRefd;
						}
					} else {
						namedValueAssignment = namedValueReference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
					}

					if (namedValueAssignment instanceof Def_Type) {

						if (!isVisibleInActualModule((Assignment) namedValueAssignment)) {
							Identifier namedValueIdentifier = namedValue.getName();
							String msg = MessageFormat.format(ERROR_MESSAGE, namedValueIdentifier.getDisplayName());
							problems.report(namedValueIdentifier.getLocation(), msg);
						}
					}
				}
			}
		}

		private class IdentifierToDefType extends ASTVisitor {

			private Identifier identifierToFind;

			private boolean isPrivate;

			private boolean getIsPrivate() {
				return isPrivate;
			}

			public IdentifierToDefType(final Identifier identifier) {
				identifierToFind = identifier;
				isPrivate = false;
			}

			@Override
			public int visit(final IVisitableNode node) {

				if (node instanceof CompField) {
					CompField compField = (CompField) node;

					Type compFieldType = compField.getType();

					if (compFieldType instanceof Referenced_Type) {

						if (compField.getIdentifier().equals(identifierToFind)) {
							INamedNode compFieldReferencedType = compFieldType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getNameParent();

							if (compFieldReferencedType instanceof Def_Type) {

								if (!isVisibleInActualModule((Assignment) compFieldReferencedType)) {
									isPrivate = true;
									return V_ABORT;
								}
							}
						}
					}
				}
				return V_CONTINUE;
			}
		}
	}

	public static class Value extends Base {

		private static final String ERROR_MESSAGE = "The parametrization of {0} field is private but it is accessible because of wrapping into public type.";
		
		public Value() {
			super(CodeSmellType.PRIVATE_VALUE_VIA_PUBLIC);
		}

		@Override
		protected void process(final IVisitableNode node, final Problems problems) {
			actualModule = (Module) node;
			ValueCollector valueCollector = new ValueCollector();
			actualModule.accept(valueCollector);
			check(valueCollector, problems);
		}

		private class ValueCollector extends ASTVisitor {

			private List<SequenceOf_Value> sequenceOfValues;
			
			public ValueCollector() {
				sequenceOfValues = new ArrayList<SequenceOf_Value>();
			}

			@Override
			public int visit(final IVisitableNode node) {
				if (node instanceof SequenceOf_Value) {
					sequenceOfValues.add((SequenceOf_Value) node);
				}
				return V_CONTINUE;
			}
		}

		public void check(final ValueCollector valueCollector, final Problems problems) {
			checkSequenceOfValues(valueCollector, problems);
		}

		private void checkSequenceOfValues(final ValueCollector valueCollector, final Problems problems) {
			Iterator<SequenceOf_Value> valueIterator = valueCollector.sequenceOfValues.iterator();

			while (valueIterator.hasNext()) {
				SequenceOf_Value actualValue = valueIterator.next();

				IType myGovernorType = actualValue.getMyGovernor();

				if (myGovernorType != null) {
					INamedNode valueReferencedType;

					String fieldName = "";

					if (myGovernorType instanceof Referenced_Type) {
						valueReferencedType = myGovernorType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getNameParent();
						fieldName = ((Referenced_Type) myGovernorType).getReference().getFullName();
					} else {
						valueReferencedType = myGovernorType.getNameParent();
						fieldName = "this";
					}

					if (valueReferencedType instanceof Def_Type) {
						if (!isVisibleInActualModule((Assignment) valueReferencedType)) {
							String msg = MessageFormat.format(ERROR_MESSAGE, fieldName);
							problems.report(actualValue.getLocation(), msg);
						}
					}
				}
			}
		}
	}
}