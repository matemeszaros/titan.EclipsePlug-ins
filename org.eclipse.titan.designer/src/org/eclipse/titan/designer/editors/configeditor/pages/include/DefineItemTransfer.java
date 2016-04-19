/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.include;

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
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler.Definition;
import org.eclipse.titan.designer.editors.configeditor.ConfigItemTransferBase;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class DefineItemTransfer extends ConfigItemTransferBase {
	private static DefineItemTransfer instance = new DefineItemTransfer();
	private static final String TYPE_NAME = "TITAN-DefineSectionItem-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	public static DefineItemTransfer getInstance() {
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
		Definition[] items = (Definition[]) object;

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
	protected Definition[] nativeToJava(final TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int n = in.readInt();
			Definition[] items = new Definition[n];

			String name;
			String value;
			String hiddenBefore;
			for (int i = 0; i < n; i++) {
				items[i] = new DefineSectionHandler.Definition();

				final ParseTree root = new ParserRuleContext();
				items[i].setRoot( root );
				
				hiddenBefore = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, ConfigTreeNodeUtilities.createHiddenTokenNode( hiddenBefore ) );
				name = in.readUTF();
				final ParseTree nameNode = new AddedParseTree( name );
				items[i].setDefinitionName( nameNode );
				ConfigTreeNodeUtilities.addChild( root, nameNode );

				hiddenBefore = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, ConfigTreeNodeUtilities.createHiddenTokenNode( hiddenBefore ) );
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree(":=") );

				hiddenBefore = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, ConfigTreeNodeUtilities.createHiddenTokenNode( hiddenBefore ) );
				value = in.readUTF();
				final ParseTree valueNode = new AddedParseTree( value );
				items[i].setDefinitionValue( valueNode );
				ConfigTreeNodeUtilities.addChild( root, valueNode );
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new Definition[] {};
		}
	}

}
