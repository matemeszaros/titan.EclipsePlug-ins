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

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.designer.editors.configeditor.ConfigItemTransferBase;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class IncludeItemTransfer extends ConfigItemTransferBase {
	private static IncludeItemTransfer instance = new IncludeItemTransfer();
	private static final String TYPE_NAME = "TITAN-IncludeItem-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	public static IncludeItemTransfer getInstance() {
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
		ParseTree[] items = (ParseTree[]) object;

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		byte[] bytes = null;

		try {
			out.writeInt(items.length);

			for (int i = 0; i < items.length; i++) {
				out.writeUTF( convertToString( items[ 2 * i ] ) );
				out.writeUTF( convertToString( items[ 2 * i + 1 ] ) );
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
	protected ParseTree[] nativeToJava(final TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int n = in.readInt();
			ParseTree[] items = new ParseTree[ 2 * n ];

			String fileName;
			String hiddenBefore;
			for (int i = 0; i < n; i++) {
				hiddenBefore = in.readUTF();
				fileName = in.readUTF();
				items[ 2 * i ] = ConfigTreeNodeUtilities.createHiddenTokenNode( hiddenBefore );
				items[ 2 * i + 1 ] = new AddedParseTree(fileName);
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new ParseTree[] {};
		}
	}

}
