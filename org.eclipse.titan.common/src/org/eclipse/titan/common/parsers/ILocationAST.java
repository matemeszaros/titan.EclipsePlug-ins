/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

/**
 * This interface merely serves for us as a common point between the different LocationAST used by the different parsers. So that we can report
 * problems on a common base.
 * 
 * @author Kristof Szabados
 * */
public interface ILocationAST {

	/**
	 * @return the offset at the beginning of the represented element.
	 * */
	int getOffset();

	/**
	 * Sets the offset for the beginning of the represented element.
	 *
	 * @param offset the offset to set
	 * */
	void setOffset(int offset);

	/**
	 * @return the offset at the end of the represented element.
	 * */
	int getEndOffset();

	/**
	 * Sets the offset for the end of the represented element.
	 *
	 * @param endOffset the offset to set
	 * */
	void setEndOffset(int endOffset);

	/**
	 * @return the line in which the beginning of the represented element started
	 * */
	int getLine();
}
