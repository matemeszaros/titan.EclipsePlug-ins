/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.core.resources.IResourceVisitor;
import org.w3c.dom.Document;

/**
 * @author Kristof Szabados
 * */
public interface IProjectFileResourceVisitor extends IResourceVisitor {

	void setDocument(Document document);
}
