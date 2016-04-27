/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.compgroupmc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.ComponentSectionHandler.Component;
import org.eclipse.titan.designer.editors.configeditor.ConfigItemTransferBase;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ComponentItemTransfer extends ConfigItemTransferBase {
	private static ComponentItemTransfer instance = new ComponentItemTransfer();
	private static final String TYPE_NAME = "TITAN-ComponentItem-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	public static ComponentItemTransfer getInstance() {
		return instance;
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	@Override
	protected void javaToNative(final Object object, final TransferData transferData) {
		Component[] items = (Component[]) object;

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		byte[] bytes = null;

		try {
			out.writeInt(items.length);

			for (int i = 0; i < items.length; i++) {
				out.writeUTF( convertToString( items[i].getRoot() ) );
			}
			out.close();
			bytes = byteOut.toByteArray();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		if (bytes != null) {
			super.javaToNative(bytes, transferData);
		}
	}

	@Override
	protected Component[] nativeToJava(final TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int n = in.readInt();
			Component[] items = new Component[n];

			String componentName;
			String hostName;
			String hiddenBefore;
			for (int i = 0; i < n; i++) {
				items[i] = new Component();

				final ParseTree root = new ParserRuleContext();
				items[i].setRoot( root );
				
				hiddenBefore = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, ConfigTreeNodeUtilities.createHiddenTokenNode( hiddenBefore ) );
				componentName = in.readUTF();
				final ParseTree componentNameNode = new AddedParseTree( componentName );
				items[i].setComponentName( componentNameNode );
				ConfigTreeNodeUtilities.addChild( root, componentNameNode );

				hiddenBefore = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, ConfigTreeNodeUtilities.createHiddenTokenNode( hiddenBefore ) );
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree(":=") );

				hiddenBefore = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, ConfigTreeNodeUtilities.createHiddenTokenNode( hiddenBefore ) );
				hostName = in.readUTF();
				final ParseTree hostNameNode = new AddedParseTree( hostName );
				items[i].setHostName( hostNameNode );
				ConfigTreeNodeUtilities.addChild( root, hostNameNode );
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new Component[] {};
		}
	}

}
