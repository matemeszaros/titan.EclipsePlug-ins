/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.modulepar;

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
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler.ModuleParameter;

/**
 * @author Kristof Szabados
 * */
public final class ModuleParameterTransfer extends ByteArrayTransfer {
	private static ModuleParameterTransfer instance = new ModuleParameterTransfer();
	private static final String TYPE_NAME = "TITAN-ModuleParameter-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	public static ModuleParameterTransfer getInstance() {
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
		ModuleParameter[] items = (ModuleParameter[]) object;

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		byte[] bytes = null;

		try {
			out.writeInt(items.length);

			for (int i = 0; i < items.length; i++) {
				if (items[i].getModuleName() == null) {
					// hidden before the module name
					out.writeUTF("");
					// the module name
					out.writeUTF("");
					// hidden before the separating '.'
					out.writeUTF("");
				} else {
					out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getModuleName()));
					out.writeUTF(items[i].getModuleName().getText());
					out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getModuleName().getNextSibling()));
				}

				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getParameterName()));
				out.writeUTF(items[i].getParameterName().getText());

				// hidden before the ":=" sign
				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getParameterName().getNextSibling()));

				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getValue()));
				out.writeUTF(ConfigTreeNodeUtilities.toString(items[i].getValue()));
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
	protected ModuleParameter[] nativeToJava(final TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int n = in.readInt();
			ModuleParameter[] items = new ModuleParameter[n];
			ModuleParameter item;

			String moduleName;
			String parameterName;
			String hiddenBefore1;
			String hiddenBefore2;
			String value;
			for (int i = 0; i < n; i++) {
				item = new ModuleParameterSectionHandler.ModuleParameter();

				// module name part
				hiddenBefore1 = in.readUTF();
				moduleName = in.readUTF();
				hiddenBefore2 = in.readUTF();
				LocationAST node;
				if ("".equals(moduleName)) {
					item.setModuleName(null);
					node = null;
				} else {
					item.setModuleName(new LocationAST(moduleName));
					item.getModuleName().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore1));
					node = new LocationAST(".");
					node.setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore2));
					item.getModuleName().setNextSibling(node);
				}

				// parameter name part
				hiddenBefore1 = in.readUTF();
				parameterName = in.readUTF();
				item.setParameterName(new LocationAST(parameterName));
				item.getParameterName().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore1));
				if (node != null) {
					node.setNextSibling(item.getParameterName());
				}

				// the := sign and the hidden stuff before it
				hiddenBefore1 = in.readUTF();
				node = new LocationAST(":=");
				node.setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore1));
				item.getParameterName().setNextSibling(node);

				// the value part
				hiddenBefore1 = in.readUTF();
				value = in.readUTF();
				item.setValue(new LocationAST(value));
				item.getValue().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore1));
				node.setNextSibling(item.getValue());

				// put it under the root node
				item.setRoot(new LocationAST(""));
				if (item.getModuleName() == null) {
					item.getRoot().setFirstChild(item.getParameterName());
				} else {
					item.getRoot().setFirstChild(item.getModuleName());
				}

				items[i] = item;
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new ModuleParameter[] {};
		}
	}

}
