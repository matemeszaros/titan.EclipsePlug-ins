/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.testportpar;

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
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler.TestportParameter;

/**
 * @author Kristof Szabados
 * */
public final class TestportParameterTransfer extends ByteArrayTransfer {
	private static TestportParameterTransfer instance = new TestportParameterTransfer();
	private static final String TYPE_NAME = "TITAN-TestportParameter-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	public static TestportParameterTransfer getInstance() {
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
		TestportParameter[] items = (TestportParameter[]) object;

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		byte[] bytes = null;

		try {
			out.writeInt(items.length);

			for (int i = 0; i < items.length; i++) {
				if (items[i].getComponentName() == null) {
					// hidden before the component name
					out.writeUTF("");
					// the component name
					out.writeUTF("");
					// hidden before the separating '.'
					out.writeUTF("");
				} else {
					out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getComponentName()));
					out.writeUTF(items[i].getComponentName().getText());
					out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getComponentName().getNextSibling()));
				}

				if (items[i].getTestportName() == null) {
					// hidden before the testport name
					out.writeUTF("");
					// the testport name
					out.writeUTF("");
					// hidden before the separating '.'
					out.writeUTF("");
				} else {
					out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getTestportName()));
					out.writeUTF(items[i].getTestportName().getText());
					out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getTestportName().getNextSibling()));
				}

				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getParameterName()));
				out.writeUTF(items[i].getParameterName().getText());

				// hidden before the ":=" sign
				out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].getParameterName().getNextSibling()));

				// out.writeUTF(ConfigTreeNodeUtilities.getHiddenBefore(items[i].value));
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
	protected TestportParameter[] nativeToJava(final TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int n = in.readInt();
			TestportParameter[] items = new TestportParameter[n];
			TestportParameter item;

			String componentName;
			String testportName;
			String parameterName;
			String hiddenBefore1;
			String hiddenBefore2;
			String value;
			for (int i = 0; i < n; i++) {
				item = new TestportParameterSectionHandler.TestportParameter();

				// component name part
				hiddenBefore1 = in.readUTF();
				componentName = in.readUTF();
				hiddenBefore2 = in.readUTF();
				LocationAST node;
				if ("".equals(componentName)) {
					item.setComponentName(null);
					node = null;
				} else {
					item.setComponentName(new LocationAST(componentName));
					item.getComponentName().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore1));
					node = new LocationAST(".");
					node.setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore2));
					item.getComponentName().setNextSibling(node);
				}

				// testport name part
				hiddenBefore1 = in.readUTF();
				testportName = in.readUTF();
				hiddenBefore2 = in.readUTF();
				if ("".equals(testportName)) {
					item.setTestportName(null);
					node = null;
				} else {
					item.setTestportName(new LocationAST(testportName));
					item.getTestportName().setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore1));
					if (node != null) {
						node.setNextSibling(item.getTestportName());
					}
					node = new LocationAST(".");
					node.setHiddenBefore(new CommonHiddenStreamToken(hiddenBefore2));
					item.getTestportName().setNextSibling(node);
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
				value = in.readUTF();
				item.setValue(new LocationAST(value));
				node.setNextSibling(item.getValue());

				// put it under the root node
				item.setRoot(new LocationAST(""));
				if (item.getComponentName() == null) {
					if (item.getTestportName() == null) {
						item.getRoot().setFirstChild(item.getParameterName());
					} else {
						item.getRoot().setFirstChild(item.getTestportName());
					}
				} else {
					item.getRoot().setFirstChild(item.getComponentName());
				}

				items[i] = item;
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new TestportParameter[] {};
		}
	}

}
