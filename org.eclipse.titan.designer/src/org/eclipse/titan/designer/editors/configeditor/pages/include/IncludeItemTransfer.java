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

/**
 * @author Kristof Szabados
 * */
public final class IncludeItemTransfer extends ByteArrayTransfer {
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
		LocationAST[] items = (LocationAST[]) object;

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		byte[] bytes = null;

		try {
			out.writeInt(items.length);

			for (int i = 0; i < items.length; i++) {
				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i]));
				out.writeUTF(items[i].getText());
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
	protected LocationAST[] nativeToJava(final TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int n = in.readInt();
			LocationAST[] items = new LocationAST[n];

			String fileName;
			String hiddenBefore;
			for (int i = 0; i < n; i++) {
				hiddenBefore = in.readUTF();
				fileName = in.readUTF();
				items[i] = new LocationAST(fileName);
				items[i].setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore));
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new LocationAST[] {};
		}
	}

}
