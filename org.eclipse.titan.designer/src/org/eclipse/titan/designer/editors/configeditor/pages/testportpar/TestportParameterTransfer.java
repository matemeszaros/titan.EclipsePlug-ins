/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler.TestportParameter;
import org.eclipse.titan.designer.editors.configeditor.ConfigItemTransferBase;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class TestportParameterTransfer extends ConfigItemTransferBase {
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
	protected TestportParameter[] nativeToJava(final TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int n = in.readInt();
			TestportParameter[] items = new TestportParameter[n];

			String componentName;
			String testportName;
			String parameterName;
			String hiddenBefore1;
			String hiddenBefore2;
			String value;
			for (int i = 0; i < n; i++) {
				TestportParameter item = new TestportParameterSectionHandler.TestportParameter();

				final ParseTree root = new ParserRuleContext(); 
				item.setRoot( root );
				
				// component name part
				hiddenBefore1 = in.readUTF();
				componentName = in.readUTF();
				hiddenBefore2 = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( hiddenBefore1 ) );
				item.setComponentName(new AddedParseTree(componentName));
				ConfigTreeNodeUtilities.addChild( root, item.getComponentName() );
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( hiddenBefore2 ) );
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( "".equals(componentName) ? "" : "." ) );

				// testport name part
				hiddenBefore1 = in.readUTF();
				testportName = in.readUTF();
				hiddenBefore2 = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree(hiddenBefore2) );
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( "".equals(testportName) ? "" : "." ) );

				// parameter name part
				hiddenBefore1 = in.readUTF();
				parameterName = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( hiddenBefore1 ) );
				item.setParameterName(new AddedParseTree(parameterName));
				ConfigTreeNodeUtilities.addChild( root, item.getParameterName() );

				// the := sign and the hidden stuff before it
				hiddenBefore1 = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( hiddenBefore1 ) );
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( ":=" ) );

				// the value part
				value = in.readUTF();
				item.setValue(new AddedParseTree(value));
				ConfigTreeNodeUtilities.addChild( root, item.getValue() );

				items[i] = item;
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new TestportParameter[] {};
		}
	}

}
