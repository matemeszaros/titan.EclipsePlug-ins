/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.execute;

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
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler.ExecuteItem;
import org.eclipse.titan.designer.editors.configeditor.ConfigItemTransferBase;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ExecuteItemTransfer extends ConfigItemTransferBase {
	private static ExecuteItemTransfer instance = new ExecuteItemTransfer();
	private static final String TYPE_NAME = "TITAN-ExecutableItem-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	public static ExecuteItemTransfer getInstance() {
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
		ExecuteItem[] items = (ExecuteItem[]) object;

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
	protected ExecuteItem[] nativeToJava(final TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int n = in.readInt();
			ExecuteItem[] items = new ExecuteItem[n];

			String moduleName;
			String testcaseName;
			String hiddenBefore;
			for (int i = 0; i < n; i++) {
				items[i] = new ExecuteSectionHandler.ExecuteItem();

				final ParseTree root = new ParserRuleContext();
				items[i].setRoot( root );
				
				hiddenBefore = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, ConfigTreeNodeUtilities.createHiddenTokenNode( hiddenBefore ) );
				moduleName = in.readUTF();
				final ParseTree moduleNameNode = new AddedParseTree( moduleName );
				items[i].setModuleName( moduleNameNode );
				ConfigTreeNodeUtilities.addChild( root, moduleNameNode );

				hiddenBefore = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, ConfigTreeNodeUtilities.createHiddenTokenNode( hiddenBefore ) );
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree(".") );

				hiddenBefore = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, ConfigTreeNodeUtilities.createHiddenTokenNode( hiddenBefore ) );
				testcaseName = in.readUTF();
				final ParseTree testcaseNameNode = new AddedParseTree( testcaseName );
				items[i].setTestcaseName( testcaseNameNode );
				ConfigTreeNodeUtilities.addChild( root, testcaseNameNode );
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new ExecuteItem[] {};
		}
	}

}
