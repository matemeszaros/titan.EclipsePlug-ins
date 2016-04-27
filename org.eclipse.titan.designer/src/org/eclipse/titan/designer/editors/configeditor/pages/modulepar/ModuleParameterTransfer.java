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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler.ModuleParameter;
import org.eclipse.titan.designer.editors.configeditor.ConfigItemTransferBase;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ModuleParameterTransfer extends ConfigItemTransferBase {
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
	protected ModuleParameter[] nativeToJava(final TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			int n = in.readInt();
			ModuleParameter[] items = new ModuleParameter[n];

			for (int i = 0; i < n; i++) {
				ModuleParameter newModuleParameter = new ModuleParameterSectionHandler.ModuleParameter();

				final ParseTree root = new ParserRuleContext();
				newModuleParameter.setRoot( root );

				// module name part
				final String hiddenBeforeModuleName = in.readUTF();
				final String moduleName = in.readUTF();
				final String hiddenBeforeSeparator = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree("\n") );
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( hiddenBeforeModuleName ) );
				newModuleParameter.setModuleName( new AddedParseTree( moduleName ) );
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( hiddenBeforeSeparator ) );
				ConfigTreeNodeUtilities.addChild( root, newModuleParameter.getModuleName() );
				
				final boolean isModuleNameEmpty = moduleName == null || moduleName.isEmpty();
				
				newModuleParameter.setSeparator( new AddedParseTree( isModuleNameEmpty ? "" : ".") );
				ConfigTreeNodeUtilities.addChild( root, newModuleParameter.getSeparator() );

				// parameter name part
				final String hiddenBeforeParameterName = in.readUTF();
				final String parameterName = in.readUTF();
				
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( hiddenBeforeParameterName ) );
				newModuleParameter.setParameterName( new AddedParseTree( parameterName ) );
				ConfigTreeNodeUtilities.addChild( root, newModuleParameter.getParameterName() );
				
				// the := sign and the hidden stuff before it
				final String hiddenBeforeOperator = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( hiddenBeforeOperator ) );
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree(" := ") );
				
				// the value part
				final String hiddenBeforeValue = in.readUTF();
				final String value = in.readUTF();
				ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( hiddenBeforeValue ) );
				newModuleParameter.setValue( new AddedParseTree( value ) );
				ConfigTreeNodeUtilities.addChild( root, newModuleParameter.getValue() );
				
				// put it under the root node
				items[i] = newModuleParameter;
			}
			return items;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new ModuleParameter[] {};
		}
	}

}
