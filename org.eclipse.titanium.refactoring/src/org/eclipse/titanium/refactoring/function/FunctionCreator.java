/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titanium.refactoring.function.ReturnVisitor.ReturnCertainty;

/**
 * This class is only instantiated by the ExtractToFunctionRefactoring once per each refactoring operation.
 * <p>
 * By calling <code>perform()</code>, the new function body and call text is created from
 * the selection and the parameters (found by ParamCollector).
 * <p>
 * A list of StringBuilders contain the function body and function call texts.
 * The StringBuilders are used as mutable Strings and allow a later editing of the texts by the wizard
 * (for example, to change the names of the parameters)
 * 
 * @author Viktor Varga
 */
class FunctionCreator implements IModelProvider<ParamTableItem> {
	
	private static final String FUNCTION_TEXT_NL = "\r\n";
	private static final String FUNCTION_TEXT_PREFIX = "function ";
	private static final String FUNCTION_TEXT_PARAMS_START = "(";
	private static final String FUNCTION_TEXT_PARAMS_END = ")";
	private static final String FUNCTION_TEXT_RUNS_ON_PREFIX = " runs on ";
	private static final String FUNCTION_TEXT_RETURN_PREFIX = " return ";
	private static final String FUNCTION_TEXT_BODY_START = "\r\n{\r\n";
	private static final String FUNCTION_TEXT_BODY_END = "\r\n}\r\n";

	private static final String FUNCTION_CALL_RETURN_PREFIX = "return ";
	private static final String FUNCTION_CALL_RETURN_SUFFIX = "return; ";
	private static final String FUNCTION_CALL_PARAMS_START = "(";
	private static final String FUNCTION_CALL_PARAMS_END = ");";
	
	//in
	private StatementList selectedStatements;
	private IFile selectedFile;
	private List<Param> params;
	private Reference runsOnRef;
	private Type returnType;
	private final StringBuilder newFuncName;
	private final List<RefactoringStatusEntry> warnings;
	private final ReturnCertainty returnCertainity;
	
	//out
	/**
	 * StringBuilders are used as mutable Strings to provide edit support for the wizard
	 * */
	private List<StringBuilder> functionText;
	private List<StringBuilder> functionCallText;
	
	FunctionCreator(final StatementList selectedStatements, final IFile selectedFile, final StringBuilder funcName, final List<Param> params, 
			final Reference runsOnRef, final Type returnType, final ReturnCertainty returnCertainty) {
		this.selectedStatements = selectedStatements;
		this.selectedFile = selectedFile;
		this.newFuncName = funcName;
		this.params = params;
		this.runsOnRef = runsOnRef;
		this.returnType = returnType;
		warnings = new ArrayList<RefactoringStatusEntry>();
		this.returnCertainity = returnCertainty;
	}

	void perform() {
		createFunctionText();
		createFunctionCallText();
	}
	
	public List<RefactoringStatusEntry> getWarnings() {
		return warnings;
	}
	List<StringBuilder> getFunctionText() {
		return functionText;
	}
	List<StringBuilder> getFunctionCallText() {
		return functionCallText;
	}

	//MODEL PROVEIDER FOR WIZARD

	/**
	 * only call after <code>perform()</code> has been called
	 */
	@Override
	public List<ParamTableItem> getItems() {
		List<ParamTableItem> items = new LinkedList<ParamTableItem>();
		if (params == null) {
			return items;
		}
		for (Param p: params) {
			if (p.getPassingType() == ArgumentPassingType.NONE) {
				continue;
			}
			items.add(new ParamTableItem(p.getPassingType(), p.getTypeName(), p.getName()));
		}
		return items;
	}

	//MODEL PROVEIDER FOR WIZARD END

	//private methods for creating parts of the new function body/call text
	
	/**
	 * @return a set in which the TextReplaceItems (parameter occurences) are sorted by their location
	 */
	private SortedSet<TextReplaceItem> createSortedTextReplaceItemSet(final String sourceText, final int sourceOffset) {
		SortedSet<TextReplaceItem> hitSet = new TreeSet<TextReplaceItem>();
		if (params == null) {
			return hitSet;
		}
		for (Param p: params) {
			List<ISubReference> srs = p.getRefs();
			for (ISubReference isr: srs) {
				hitSet.add(new TextReplaceItem(isr, p, sourceText, sourceOffset));
			}
			if (p.isDeclaredInside()) {
				hitSet.add(new TextReplaceItem(p.getDef(), p, sourceText, sourceOffset));
			}
		}
		return hitSet;
	}
	
	/** 
	 * Creates function text from <code>params</code> into <code>newFunctionText</code>. 
	 * Call after the user specified param and func names in the wizard
	 */
	private void createFunctionText() {
		List<StringBuilder> declarationsBeforeFunc = new ArrayList<StringBuilder>();
		if (params == null) {
			ErrorReporter.logError("FunctionCreator.createFunctionText(): 'params' is null! ");
			return;
		}
		functionText = new ArrayList<StringBuilder>();
		functionCallText = new ArrayList<StringBuilder>();
		//TODO: add private/<default>/public tag
		//insert function header
		functionText.add(new StringBuilder(FUNCTION_TEXT_NL));
		functionText.add(new StringBuilder(FUNCTION_TEXT_NL));
		functionText.add(new StringBuilder(FUNCTION_TEXT_NL));
		functionText.add(new StringBuilder(FUNCTION_TEXT_PREFIX));
		functionText.add(newFuncName);
		//function params
		functionText.add(new StringBuilder(FUNCTION_TEXT_PARAMS_START));
		ListIterator<Param> it = params.listIterator();
		if (it.hasNext()) {
			Param first = it.next();
			functionText.addAll(first.createParamText(false));
		}
		while (it.hasNext()) {
			Param curr = it.next();
			functionText.addAll(curr.createParamText(true));
		}
		functionText.add(new StringBuilder(FUNCTION_TEXT_PARAMS_END));
		//optional runs on clause
		if (runsOnRef != null) {
			functionText.add(new StringBuilder(FUNCTION_TEXT_RUNS_ON_PREFIX));
			functionText.add(new StringBuilder(runsOnRef.getId().toString()));
		}
		//optional return clause
		if (returnType != null) {
			functionText.add(new StringBuilder(FUNCTION_TEXT_RETURN_PREFIX));
			functionText.add(new StringBuilder(Param.getShortTypename(returnType)));
		}
		functionText.add(new StringBuilder(FUNCTION_TEXT_BODY_START));
		//insert function body
		functionText.addAll(createFunctionBody(declarationsBeforeFunc));
		//function body end
		functionText.add(new StringBuilder(FUNCTION_TEXT_BODY_END));
		functionText.add(new StringBuilder(FUNCTION_TEXT_NL));
		functionText.add(new StringBuilder(FUNCTION_TEXT_NL));
		//
		//insert all declarations before the function header
		functionCallText.addAll(declarationsBeforeFunc);
	}
	
	private List<StringBuilder> createFunctionBody(final List<StringBuilder> declarationsBeforeFunc) {
		List<StringBuilder> body = new ArrayList<StringBuilder>();
		try {
			InputStream istream = selectedFile.getContents();
			BufferedReader br = new BufferedReader(new InputStreamReader(istream));
			final int startOffset = selectedStatements.getLocation().getOffset();
			final int endOffset = selectedStatements.getLocation().getEndOffset();
			br.skip(startOffset);
			char[] contentBuf = new char[endOffset-startOffset];
			br.read(contentBuf, 0, endOffset-startOffset);
			br.close();
			istream.close();
			final String selectedContent = new String(contentBuf);
			SortedSet<TextReplaceItem> itemSet = createSortedTextReplaceItemSet(selectedContent, startOffset);
			Iterator<TextReplaceItem> it = itemSet.iterator();
			TextReplaceItem last = null;
			//no items
			if (!it.hasNext()) {
				body.add(new StringBuilder(selectedContent));
			}
			while (it.hasNext()) {
				TextReplaceItem curr = it.next();
				if (last == null) {
					body.add(curr.createBeginningText());
				} else {
					body.add(last.createIntermediateText(curr));
				}
				if (curr.isReference()) {
					//insert name alternate instead of the old references
					body.add(curr.getNewParamName());
				} else {
					body.addAll(curr.createInitOnlyText());
					declarationsBeforeFunc.add(curr.createPreDeclarationText());
					declarationsBeforeFunc.add(new StringBuilder(FUNCTION_TEXT_NL));
				}
				if (!it.hasNext()) {
					body.add(curr.createEndingText());
				}
				last = curr;
			}
		} catch (CoreException ce) {
			ErrorReporter.logError("FunctionCreator.createFunctionBody(): Unable to read file contents: " + selectedFile);
			return new ArrayList<StringBuilder>();
		} catch (IOException ioe) {
			ErrorReporter.logError("FunctionCreator.createFunctionBody(): Unable to read file contents: " + selectedFile);
			return new ArrayList<StringBuilder>();
		}

		return body;
	}
	
	private void createFunctionCallText() {
		final boolean returnOnAllBranches = (returnCertainity == ReturnCertainty.YES);
		if (returnType != null && returnOnAllBranches) {
			functionCallText.add(new StringBuilder(FUNCTION_CALL_RETURN_PREFIX));
		}
		functionCallText.add(newFuncName);
		functionCallText.add(new StringBuilder(FUNCTION_CALL_PARAMS_START));
		ListIterator<Param> it = params.listIterator();
		if (it.hasNext()) {
			Param first = it.next();
			functionCallText.addAll(first.createParamCallText(false));
		}
		while (it.hasNext()) {
			Param curr = it.next();
			functionCallText.addAll(curr.createParamCallText(true));
		}
		functionCallText.add(new StringBuilder(FUNCTION_CALL_PARAMS_END));
		functionCallText.add(new StringBuilder(FUNCTION_TEXT_NL));
		if (returnType == null && returnOnAllBranches) {
			functionCallText.add(new StringBuilder(FUNCTION_CALL_RETURN_SUFFIX));
		}
	}

}
