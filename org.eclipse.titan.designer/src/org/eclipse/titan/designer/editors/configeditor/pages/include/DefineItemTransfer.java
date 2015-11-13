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

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler.Definition;

/**
 * @author Kristof Szabados
 * */
public final class DefineItemTransfer extends ByteArrayTransfer {
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
				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getDefinitionName()));
				out.writeUTF(items[i].getDefinitionName().getText());

				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getDefinitionName().getNextSibling()));

				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getDefinitionValue()));
				out.writeUTF(items[i].getDefinitionValue().getText());
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

				hiddenBefore = in.readUTF();
				name = in.readUTF();
				items[i].setDefinitionName(new LocationAST(name));
				items[i].getDefinitionName().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore));

				hiddenBefore = in.readUTF();
				LocationAST node = new LocationAST(":=");
				node.setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore));
				items[i].getDefinitionName().setNextSibling(node);

				hiddenBefore = in.readUTF();
				value = in.readUTF();
				items[i].setDefinitionValue(new LocationAST(value));
				items[i].getDefinitionValue().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore));
				node.setNextSibling(items[i].getDefinitionValue());

				items[i].setRoot(new LocationAST(""));
				items[i].getRoot().setFirstChild(items[i].getDefinitionName());
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new Definition[] {};
		}
	}

}
