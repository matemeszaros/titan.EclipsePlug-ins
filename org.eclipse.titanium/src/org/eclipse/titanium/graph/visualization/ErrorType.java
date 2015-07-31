/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.visualization;

/**
 * Three possible values to provide extra information for
 * {@link BadLayoutException} exception: <br/>
 * <ul>
 * <li>NOT_EXISTING_LAYOUT</li>
 * <li>NO_OBJECT</li>
 * <li>CYCLIC_GRAPH</li>
 * <li>EMPTY_GRAPH</li>
 * <li>INVALID_SAVE_MODE</li>
 * <li>IO_ERROR</li>
 * </ul>
 * 
 * @author Gabor Jenei
 * 
 */
public enum ErrorType {
	NOT_EXISITING_LAYOUT, NO_OBJECT, CYCLIC_GRAPH, EMPTY_GRAPH, INVALID_SAVE_MODE, IO_ERROR
}