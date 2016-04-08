/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;

public class TitanListener extends BaseErrorListener {
	protected List<SyntacticErrorStorage> errorsStored = null;
	
	public TitanListener() {
		this.errorsStored = new ArrayList<SyntacticErrorStorage>();
	}
	public TitanListener(final List<SyntacticErrorStorage> storage) {
		this.errorsStored = storage;
	}
	
	public void reset() {
		errorsStored.clear();
	}
	
	@Override
	public void syntaxError(final @NotNull Recognizer<?, ?> recognizer, final @Nullable Object offendingSymbol, final int line, final int charPositionInLine,
			final @NotNull String msg, final @Nullable RecognitionException e) {
		SyntacticErrorStorage errorStorage;
		if (offendingSymbol instanceof CommonToken) {
			final CommonToken token = (CommonToken) offendingSymbol;
			errorStorage = new SyntacticErrorStorage(line, token.getStartIndex(), token.getStopIndex() + 1, msg, e);
		} else {
			errorStorage = new SyntacticErrorStorage(line, charPositionInLine, charPositionInLine + 1, msg, e);
		}

		errorsStored.add(errorStorage);
	}
	public List<SyntacticErrorStorage> getErrorsStored() {
		return errorsStored;
	}
	
	public boolean addAll(final List<SyntacticErrorStorage> errorStore) {
		return errorsStored.addAll(errorStore);
	}
}