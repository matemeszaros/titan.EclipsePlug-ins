/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Interface for a text editor that use a carret, whose offset can be retrieved.
 * 
 * @author Kristof Szabados
 * */
public interface IEditorWithCarretOffset extends ITextEditor {

	/** @return the offset the carret is currently on. */
	int getCarretOffset();
}
