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
	public TitanListener(ArrayList<SyntacticErrorStorage> storage) {
		this.errorsStored = storage;
	}
	
	public void reset() {
		errorsStored.clear();
	}
	
	@Override
	public void syntaxError(@NotNull Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line, int charPositionInLine,
			@NotNull String msg, @Nullable RecognitionException e) {
		SyntacticErrorStorage errorStorage;
		if (offendingSymbol instanceof CommonToken) {
			CommonToken token = (CommonToken) offendingSymbol;
			errorStorage = new SyntacticErrorStorage(line, token.getStartIndex(), token.getStopIndex() + 1, msg, e);
		} else {
			errorStorage = new SyntacticErrorStorage(line, charPositionInLine, charPositionInLine + 1, msg, e);
		}

		errorsStored.add(errorStorage);
	}
	public List<SyntacticErrorStorage> getErrorsStored() {
		return errorsStored;
	}
	
	public boolean addAll(List<SyntacticErrorStorage> errorStore) {
		return errorsStored.addAll(errorStore);
	}
}