/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler.ExecuteItem;

/**
 * @author Kristof Szabados
 * */
public final class ExecuteItemTransfer extends ByteArrayTransfer {
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
				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getModuleName()));

				out.writeUTF(items[i].getModuleName().getText());
				if (items[i].getTestcaseName() == null) {
					out.writeUTF("");
					out.writeUTF("");
					out.writeUTF("");
				} else {
					out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getModuleName().getNextSibling()));
					out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getTestcaseName()));
					out.writeUTF(items[i].getTestcaseName().getText());
				}
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

				hiddenBefore = in.readUTF();
				moduleName = in.readUTF();
				items[i].setModuleName(new LocationAST(moduleName));
				items[i].getModuleName().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore));

				hiddenBefore = in.readUTF();
				LocationAST node = new LocationAST(".");
				node.setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore));

				hiddenBefore = in.readUTF();
				testcaseName = in.readUTF();

				if ("".equals(testcaseName)) {
					items[i].setTestcaseName(null);
				} else {
					items[i].setTestcaseName(new LocationAST(testcaseName));
					items[i].getTestcaseName().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore));
					items[i].getModuleName().setNextSibling(node);
					node.setNextSibling(items[i].getTestcaseName());
				}

				items[i].setRoot(new LocationAST(""));
				items[i].getRoot().setFirstChild(items[i].getModuleName());
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new ExecuteItem[] {};
		}
	}

}
