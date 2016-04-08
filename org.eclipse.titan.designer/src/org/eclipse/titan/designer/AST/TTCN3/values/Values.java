/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * A list of values provided either by value notation, or by indexed notation.
 * 
 * @author Kristof Szabados
 * */
public final class Values extends ASTNode implements IIncrementallyUpdateable {
	private boolean isIndexed;
	private ArrayList<IValue> values;
	private ArrayList<IndexedValue> indexedValues;

	public Values(final boolean indexed) {
		super();
		isIndexed = indexed;
		if (indexed) {
			indexedValues = new ArrayList<IndexedValue>();
		} else {
			values = new ArrayList<IValue>();
		}
	}

	public boolean isIndexed() {
		return isIndexed;
	}

	public void addValue(final IValue value) {
		if (value != null) {
			values.add(value);
			value.setFullNameParent(this);
		}
	}

	public void addIndexedValue(final IndexedValue indexedValue) {
		if (indexedValue != null) {
			indexedValues.add(indexedValue);
			indexedValue.setFullNameParent(this);
		}
	}

	public int getNofValues() {
		if (isIndexed) {
			return 0;
		}

		return values.size();
	}

	public int getNofIndexedValues() {
		if (!isIndexed) {
			return 0;
		}

		return indexedValues.size();
	}

	public IValue getValueByIndex(final int index) {
		if (isIndexed) {
			return null;
		}

		return values.get(index);
	}

	public IndexedValue getIndexedValueByIndex(final int index) {
		if (!isIndexed) {
			return null;
		}

		return indexedValues.get(index);
	}

	public IValue getIndexedValueByRealIndex(final int index) {
		if (!isIndexed) {
			return null;
		}

		for (int i = 0; i < indexedValues.size(); i++) {
			IndexedValue temp = indexedValues.get(i);
			IValue value = temp.getIndex().getValue();
			if (Value_type.INTEGER_VALUE.equals(value.getValuetype())) {
				Integer_Value integerValue = (Integer_Value) value;
				if (index == integerValue.intValue()) {
					return temp.getValue();
				}
			}
		}

		return null;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (isIndexed) {
			indexedValues.trimToSize();
			for (int i = 0; i < indexedValues.size(); i++) {
				indexedValues.get(i).setMyScope(scope);
			}
		} else {
			values.trimToSize();
			for (int i = 0; i < values.size(); i++) {
				values.get(i).setMyScope(scope);
			}
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (isIndexed) {
			for (int i = 0; i < indexedValues.size(); i++) {
				if (indexedValues.get(i) == child) {
					return builder.append(INamedNode.SQUAREOPEN).append(String.valueOf(i)).append(INamedNode.SQUARECLOSE);
				}
			}
		} else {
			for (int i = 0; i < values.size(); i++) {
				if (values.get(i) == child) {
					return builder.append(INamedNode.SQUAREOPEN).append(String.valueOf(i)).append(INamedNode.SQUARECLOSE);
				}
			}
		}

		return builder;
	}

	/**
	 * Handles the incremental parsing of this value list.
	 *
	 * @param reparser the parser doing the incremental parsing.
	 * @param isDamaged true if the location contains the damaged area, false if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (isIndexed) {
			for (int i = 0, size = indexedValues.size(); i < size; i++) {
				IndexedValue indexedValue = indexedValues.get(i);

				indexedValue.updateSyntax(reparser, false);
				reparser.updateLocation(indexedValue.getLocation());
			}
		} else {
			for (int i = 0, size = values.size(); i < size; i++) {
				IValue value = values.get(i);

				if (value instanceof IIncrementallyUpdateable) {
					((IIncrementallyUpdateable) value).updateSyntax(reparser, false);
					reparser.updateLocation(value.getLocation());
				} else {
					throw new ReParseException();
				}
			}
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (isIndexed) {
			for (IndexedValue iv : indexedValues) {
				iv.findReferences(referenceFinder, foundIdentifiers);
			}
		} else {
			for (IValue v : values) {
				v.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (isIndexed) {
			for (IndexedValue iv : indexedValues) {
				if (!iv.accept(v)) {
					return false;
				}
			}
		} else {
			for (IValue iv : values) {
				if (!iv.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
