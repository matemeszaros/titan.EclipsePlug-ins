/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function.EncodingPrototype_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a single type mapping, where a type is mapped to a list of mapping
 * targets.
 * 
 * @author Kristof Szabados
 * */
public final class TypeMapping extends ASTNode implements ILocateableNode {
	private static final String FULLNAMEPART = ".<source_type>";

	private final Type source_type;
	private final TypeMappingTargets mappingTargets;

	/** the time when this type mapping was check the last time. */
	private CompilationTimeStamp lastTimeChecked;

	/**
	 * The location of the whole type mapping. This location encloses the
	 * type mapping fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	public TypeMapping(final Type type, final TypeMappingTargets mappingTargets) {
		source_type = type;
		this.mappingTargets = mappingTargets;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	public Type getSourceType() {
		return source_type;
	}

	public int getNofTargets() {
		return mappingTargets.getNofTargets();
	}

	public TypeMappingTarget getTargetByIndex(final int index) {
		return mappingTargets.getTargetByIndex(index);
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (source_type == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (source_type != null) {
			source_type.setMyScope(scope);
		}
		mappingTargets.setMyScope(scope);
	}

	/**
	 * Does the semantic checking of the type mapping.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (source_type != null) {
			source_type.check(timestamp);
		}

		int nofTargets = mappingTargets.getNofTargets();
		boolean hasSliding = false;
		boolean hasNonSliding = false;
		for (int i = 0, size = nofTargets; i < size; i++) {
			TypeMappingTarget target = mappingTargets.getTargetByIndex(i);
			target.check(timestamp, source_type);
			if (nofTargets > 1) {
				switch (target.getTypeMappingType()) {
				case DISCARD:
					if (hasSliding) {
						target.getLocation().reportSemanticError(
										"Mapping `discard' cannot be used if functions with `prototype(sliding)' are referred from the same source type");
					} else if (i < nofTargets - 1) {
						target.getLocation().reportSemanticError(
								"Mapping `discard' must be the last target of the source type");
					}
					break;
				case FUNCTION: {
					Def_Function function = ((FunctionTypeMappingTarget) target).getFunction();
					Def_Extfunction externalFunction = ((FunctionTypeMappingTarget) target).getExternalFunction();
					EncodingPrototype_type prototype = EncodingPrototype_type.NONE;
					if (function != null) {
						prototype = function.getPrototype();
					} else if (externalFunction != null) {
						prototype = externalFunction.getPrototype();
					} else {
						continue;
					}

					switch (prototype) {
					case NONE:
						break;
					case BACKTRACK:
						hasNonSliding = true;
						break;
					case SLIDING:
						hasSliding = true;
						break;
					default:
						if (function != null) {
							final String message = MessageFormat
									.format("The referenced {0} must have the attribute `prototype(backtrack)'' or `prototype(sliding)'' when more than one targets are present",
											function.getDescription());
							target.getLocation().reportSemanticError(message);
						} else if (externalFunction != null) {
							final String message = MessageFormat
									.format("The referenced {0} must have the attribute `prototype(backtrack)'' or `prototype(sliding)'' when more than one targets are present",
											externalFunction.getDescription());
							target.getLocation().reportSemanticError(message);
						}
						break;
					}

					break;
				}
				case DECODE:
					break;
				default:
					target.getLocation().reportSemanticError(
							MessageFormat.format("The type of the mapping must be `function', `decode',"
									+ " or `discard' instead of {0} when more than one targets are present",
									target.getMappingName()));
					break;
				}
			}
		}

		if (hasSliding && hasNonSliding) {
			location.reportSemanticError("If one of the mappings refers to a function with attribute `prototype(sliding)'"
					+ "then mappings of this source type cannot refer to functions with attribute `prototype(backtrack)'");
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (source_type != null) {
			source_type.findReferences(referenceFinder, foundIdentifiers);
		}
		if (mappingTargets != null) {
			mappingTargets.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (source_type != null && !source_type.accept(v)) {
			return false;
		}
		if (mappingTargets != null && !mappingTargets.accept(v)) {
			return false;
		}
		return true;
	}
}
