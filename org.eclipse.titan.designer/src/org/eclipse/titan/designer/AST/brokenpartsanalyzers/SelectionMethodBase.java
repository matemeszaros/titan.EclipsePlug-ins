/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;


/**
 * Base class of selection methods.
 * It is responsible for store input, result and other things.
 * 
 * @author Peter Olah
 */
public abstract class SelectionMethodBase {

	private final SelectionAlgorithm selectionAlgorithm;
	protected List<Module> allModules;
	protected List<Module> modulesToCheck;
	protected List<Module> modulesToSkip;
	
	protected List<String> semanticallyChecked;
	protected long start;
	protected long end;
	protected boolean writeDebugInfo;
	protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.SSS", Locale.US);
	protected String format = "%s %s";

	protected String header;
	protected String footer;
	
	public SelectionAlgorithm getSelectionAlgorithm(){
		return selectionAlgorithm;
	}

	public List<Module> getModulesToCheck() {
		return modulesToCheck;
	}

	public List<Module> getModulesToSkip() {
		return modulesToSkip;
	}

	public void setModules(final List<Module> allModules, final List<String> semanticallyChecked){
		modulesToCheck = new ArrayList<Module>();
		modulesToSkip = new ArrayList<Module>();
		this.allModules = new ArrayList<Module>(allModules);
		this.semanticallyChecked = semanticallyChecked;
	}

	protected SelectionMethodBase(final SelectionAlgorithm selectionAlgorithm) {
		this.selectionAlgorithm = selectionAlgorithm;
		writeDebugInfo = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);
	}

	protected void afterExecute() {
		for (Module module : allModules) {
			if (!modulesToCheck.contains(module) && !modulesToSkip.contains(module)) {
				modulesToSkip.add(module);
			}
		}
	}
	
	protected void infoAfterExecute() {
		TITANDebugConsole.println("**Selection time is:     " + (end * (1e-9)) + " seconds " + "(" + end + " nanoseconds).");
		TITANDebugConsole.println("**Nr. of broken modules: " + modulesToCheck.size());
		TITANDebugConsole.println("**Broken modules name:   " + modulesToCheck);
		TITANDebugConsole.println();
	}

}