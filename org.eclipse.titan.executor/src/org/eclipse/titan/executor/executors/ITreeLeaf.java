/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors;

/**
 * @author Kristof Szabados
 * */
public interface ITreeLeaf {

	String name();

	ITreeBranch parent();

	void parent(ITreeBranch element);

	void dispose();
}
