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

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.ComponentSectionHandler.Component;

/**
 * @author Kristof Szabados
 * */
public final class ComponentItemTransfer extends ByteArrayTransfer {
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
				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getComponentName()));
				out.writeUTF(items[i].getComponentName().getText());

				// hidden before the ":=" sign
				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getComponentName().getNextSibling()));

				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getHostName()));
				out.writeUTF(items[i].getHostName().getText());
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
			LocationAST node;
			for (int i = 0; i < n; i++) {
				items[i] = new Component();

				hiddenBefore = in.readUTF();
				componentName = in.readUTF();
				items[i].setComponentName(new LocationAST(componentName));
				items[i].getComponentName().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore));

				hiddenBefore = in.readUTF();
				node = new LocationAST(":=");
				node.setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore));
				items[i].getComponentName().setNextSibling(node);

				hiddenBefore = in.readUTF();
				hostName = in.readUTF();
				items[i].setHostName(new LocationAST(hostName));
				items[i].getHostName().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore));
				node.setNextSibling(items[i].getHostName());

				items[i].setRoot(new LocationAST(""));
				items[i].getRoot().setFirstChild(items[i].getComponentName());
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new Component[] {};
		}
	}

}
