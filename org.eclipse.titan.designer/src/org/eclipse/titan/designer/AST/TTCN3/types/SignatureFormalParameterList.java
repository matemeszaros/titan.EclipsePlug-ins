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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Signature parameter-list.
 * 
 * @author Kristof Szabados
 */
public final class SignatureFormalParameterList extends ASTNode implements IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".<unknown_parameter>";

	private final List<SignatureFormalParameter> parameters;
	/** A map of the parameters. */
	private final Map<String, SignatureFormalParameter> parameterMap;

	private List<SignatureFormalParameter> inParameters;
	private List<SignatureFormalParameter> outParameters;

	/** the time when this formal parameter list was check the last time. */
	private CompilationTimeStamp lastTimeChecked;

	private Location location = NULL_Location.INSTANCE;

	public SignatureFormalParameterList(final List<SignatureFormalParameter> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			this.parameters = null;
			parameterMap = null;
		} else {
			this.parameters = new ArrayList<SignatureFormalParameter>(parameters.size());
			parameterMap = new HashMap<String, SignatureFormalParameter>(parameters.size());
			inParameters = new ArrayList<SignatureFormalParameter>();
			outParameters = new ArrayList<SignatureFormalParameter>();

			for (SignatureFormalParameter parameter : parameters) {
				if (parameter != null && parameter.getIdentifier() != null) {
					this.parameters.add(parameter);
					parameter.setFullNameParent(this);
				}
			}
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (parameters == null) {
			return builder;
		}

		for (SignatureFormalParameter parameter : parameters) {
			if (parameter == child) {
				Identifier identifier = parameter.getIdentifier();
				return builder.append(INamedNode.DOT).append(identifier != null ? identifier.getDisplayName() : FULLNAMEPART);
			}
		}

		return builder;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	/**
	 * Sets the scope of the formal parameter list.
	 *
	 * @param scope the scope to be set
	 * */
	@Override
	public void setMyScope(final Scope scope) {
		if (parameters == null) {
			return;
		}

		for (SignatureFormalParameter parameter : parameters) {
			parameter.setMyScope(scope);
		}
	}

	/** @return the number of parameters */
	public int getNofParameters() {
		if (parameters == null) {
			return 0;
		}

		return parameters.size();
	}

	/**
	 * Returns the parameter at the specified position.
	 *
	 * @param index index of the element to return
	 * @return the identifier of the element at the specified position in this list
	 */
	public SignatureFormalParameter getParameterByIndex(final int index) {
		if (parameters == null) {
			return null;
		}

		return parameters.get(index);
	}

	/**
	 * Returns whether a parameter with the specified name exists or not.
	 *
	 * @param name the name to check for
	 * @return true if a parameter with that name exist, false otherwise
	 */
	public boolean hasParameterWithName(final String name) {
		if (parameterMap == null) {
			return false;
		}

		return parameterMap.containsKey(name);
	}

	/**
	 * Returns the parameter with the specified name.
	 *
	 * @param name the name of the parameter to return
	 * @return the element with the specified name in this list
	 */
	public SignatureFormalParameter getParameterByName(final String name) {
		if (parameterMap == null) {
			return null;
		}

		return parameterMap.get(name);
	}

	/** @return the number of in parameters */
	public int getNofInParameters() {
		if (inParameters == null) {
			return 0;
		}

		return inParameters.size();
	}

	/**
	 * Returns the in parameter at the specified position.
	 *
	 * @param index index of the element to return
	 * @return the formal parameter at the specified position in this list of in parameters
	 */
	public SignatureFormalParameter getInParameterByIndex(final int index) {
		if (inParameters == null) {
			return null;
		}

		return inParameters.get(index);
	}

	/** @return the number of ou parameters */
	public int getNofOutParameters() {
		if (outParameters == null) {
			return 0;
		}

		return outParameters.size();
	}

	/**
	 * Returns the out parameter at the specified position.
	 *
	 * @param index index of the element to return
	 * @return the formal parameter at the specified position in this list of out parameters
	 */
	public SignatureFormalParameter getOutParameterByIndex(final int index) {
		if (outParameters == null) {
			return null;
		}

		return outParameters.get(index);
	}

	/**
	 * Checks the uniqueness of the parameters, and also builds a hashmap of
	 * them to speed up further searches.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle, or -1
	 *            in silent check mode.
	 * */
	private void checkUniqueness(final CompilationTimeStamp timestamp) {
		if (parameters == null) {
			return;
		}

		for (SignatureFormalParameter parameter : parameters) {
			if (parameter != null) {
				String parameterName = parameter.getIdentifier().getName();
				if (parameterMap.containsKey(parameterName)) {
					parameterMap.get(parameterName).getIdentifier().getLocation().reportSingularSemanticError(
							MessageFormat.format(FormalParameterList.DUPLICATEPARAMETERFIRST, parameter.getIdentifier().getDisplayName()));
					parameter.getIdentifier().getLocation().reportSemanticError(
							MessageFormat.format(FormalParameterList.DUPLICATEPARAMETERREPEATED, parameter.getIdentifier().getDisplayName()));
				} else {
					parameterMap.put(parameterName, parameter);
				}
			}
		}
	}

	public void check(final CompilationTimeStamp timestamp, final Signature_Type signature) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (parameters == null) {
			return;
		}

		parameterMap.clear();
		inParameters.clear();
		outParameters.clear();

		checkUniqueness(timestamp);

		boolean isNonoblock = signature.isNonblocking();
		for (SignatureFormalParameter parameter : parameters) {
			if (parameter.getDirection() == SignatureFormalParameter.PARAM_IN) {
				inParameters.add(parameter);
			} else if (parameter.getDirection() == SignatureFormalParameter.PARAM_OUT) {
				if (isNonoblock) {
					parameter.getLocation().reportSemanticError("A non-blocking signature cannot have `out' parameter");
				}
				outParameters.add(parameter);
			} else if (parameter.getDirection() == SignatureFormalParameter.PARAM_INOUT) {
				if (isNonoblock) {
					parameter.getLocation().reportSemanticError("A non-blocking signature cannot have `out' parameter");
				}
				inParameters.add(parameter);
				outParameters.add(parameter);
			}

			Type type = parameter.getType();
			type.setParentType(signature);
			type.check(timestamp);
			type.checkEmbedded(timestamp, type.getLocation(), false, "the type of a signature parameter");
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (parameters == null) {
			return;
		}

		for (int i = 0, size = parameters.size(); i < size; i++) {
			SignatureFormalParameter parameter = parameters.get(i);

			parameter.updateSyntax(reparser, isDamaged);
			reparser.updateLocation(parameter.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (parameters != null) {
			for (SignatureFormalParameter sfp : parameters) {
				sfp.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (parameters != null) {
			for (SignatureFormalParameter sfp : parameters) {
				if (!sfp.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
