/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   
 *   Keremi, Andras
 *   Eros, Levente
 *   Kovacs, Gabor
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

public class Def_Template_Writer {
	private Def_Template templateNode;

	private StringBuilder templateString = new StringBuilder("");
	private CompilationTimeStamp compilationCounter = CompilationTimeStamp.getNewCompilationCounter();
	private String nodeName = null;

	public List<String> templateListValues = new ArrayList<String>();
	public List<String> templateIdentifiers = new ArrayList<String>();
	public List<String> templateRefdVals = new ArrayList<String>();
	private String templateNodeType = null;
	private String modifierValue = null;

	private int firstIdentifier = 0;

	private int paramCount = 0;
	private List<String> paramNames = new ArrayList<String>();
	private List<String> paramTypes = new ArrayList<String>();

	private static Map<String, Object> templateHashes = new LinkedHashMap<String, Object>();

	private Def_Template_Writer(Def_Template node) {
		super();
		this.templateNode = node;

		if (this.templateNode.getType(compilationCounter) instanceof Referenced_Type) {
			templateNodeType = ((Referenced_Type) this.templateNode.getType(compilationCounter)).getReference().getId()
					.toString();
		} else if (this.templateNode.getType(compilationCounter) instanceof Integer_Type) {
			templateNodeType = "INTEGER";

		}

		nodeName = node.getIdentifier().toString();
	}

	public static Def_Template_Writer getInstance(Def_Template node) {
		if (!templateHashes.containsKey(node.getIdentifier().toString())) {
			templateHashes.put(node.getIdentifier().toString(), new Def_Template_Writer(node));
		}
		return (Def_Template_Writer) templateHashes.get(node.getIdentifier().toString());
	}

	public void writeTemplateConstructor(String rootNodeType, String prefix) {
		if (myASTVisitor.nodeNameChildrenNamesHashMap.containsKey(rootNodeType)) {

			if (myASTVisitor.nodeNameNodeTypeHashMap.get(rootNodeType).equals("union")) {

				int unionElementId = 0;
				for (int i = 0; i < myASTVisitor.nodeNameChildrenNamesHashMap.get(rootNodeType).length; i++) {

					if (myASTVisitor.nodeNameChildrenNamesHashMap.get(rootNodeType)[i]
							.equals(templateIdentifiers.get(firstIdentifier))) {
						unionElementId = i + 1;
					}
				}

				templateString.append(prefix + "=new SC_" + unionElementId + "_" + rootNodeType + "();\r\n");

				prefix = "((SC_" + unionElementId + "_" + rootNodeType + ")value)";
			} else if (myASTVisitor.nodeNameNodeTypeHashMap.get(rootNodeType).equals("record")) {
				templateString.append(prefix + "= new " + rootNodeType + "();\r\n");

			}

			String[] childrenNodeNames = myASTVisitor.nodeNameChildrenNamesHashMap.get(rootNodeType);
			String[] childrenNodeTypes = myASTVisitor.nodeNameChildrenTypesHashMap.get(rootNodeType);

			for (int i = 0; i < childrenNodeNames.length; i++) {

				if (myASTVisitor.templateIDs.contains(childrenNodeNames[i])) {
					// idCounter++;
					if (myASTVisitor.nodeNameChildrenNamesHashMap.containsKey(childrenNodeTypes[i])
							&& !myASTVisitor.templateIdValuePairs.containsKey(childrenNodeNames[i])) {
						

						if (myASTVisitor.nodeNameNodeTypeHashMap.get(childrenNodeTypes[i]).equals("record")) {
							// do nothing, will be handled in next iteration
						} else {
							// walk childern
							
							templateString.append(
									prefix + "." + childrenNodeNames[i] + "=new " + childrenNodeTypes[i] + "();\r\n");

						}
						String prefixBackup = prefix;
						prefix = prefix.concat("." + childrenNodeNames[i]);
						writeTemplateConstructor(childrenNodeTypes[i], prefix);
						prefix = prefixBackup;

					} else {// print child element values

						// constant
						if (myASTVisitor.templateIdValuePairs.containsKey(childrenNodeNames[i])) {
							String currentvalue = myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]);

							if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(currentvalue)) {
								if (myASTVisitor.nodeNameNodeTypeHashMap.get(currentvalue).equals("constant")) {
									templateString.append(prefix + "." + childrenNodeNames[i] + "=Constants."
											+ currentvalue + "();\r\n");
								}
							} else if (paramCount > 0) {// p_Uri
								if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(childrenNodeTypes[i])) {
									for (int pCounter = 0; pCounter < paramTypes.size(); pCounter++) {
										if (childrenNodeTypes[i].equals(paramTypes.get(pCounter))) {
											templateString.append(
													prefix + "." + childrenNodeNames[i] + "=" + currentvalue + ";\r\n");
										}
									}
								}
							} else if (childrenNodeTypes[i].equals("INTEGER")) {
								templateString.append(prefix + "." + childrenNodeNames[i] + "= new INTEGER(\""
										+ currentvalue + "\");\r\n");
							}

						}

						// everything else
						if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(childrenNodeTypes[i])) {
							if (myASTVisitor.nodeNameNodeTypeHashMap.get(childrenNodeTypes[i]).equals("enum")) {
								templateString
										.append(prefix + "." + childrenNodeNames[i] + "=new " + childrenNodeTypes[i]
												+ "(\"" + myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i])
												+ "\");" + "\r\n");

							} else if (myASTVisitor.nodeNameNodeTypeHashMap.get(childrenNodeTypes[i])
									.equals("CHARSTRING")) {
								templateString.append(
										prefix + "." + childrenNodeNames[i] + "=new " + childrenNodeTypes[i] + "(new "
												+ myASTVisitor.nodeNameNodeTypeHashMap.get(childrenNodeTypes[i]) + "(\""
												+ myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]) + "\"));"
												+ "\r\n");

							}

						} else if (childrenNodeTypes[i].equals("CHARSTRING")) {

							if (myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]).equals("omit")) {
								templateString.append(prefix + "." + childrenNodeNames[i] + "=new "
										+ childrenNodeTypes[i] + "();" + "\r\n");

								templateString
										.append(prefix + "." + childrenNodeNames[i] + ".omitField=true;" + "\r\n");

							} else if (myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]).equals("?")) {
								templateString.append(prefix + "." + childrenNodeNames[i] + "=new "
										+ childrenNodeTypes[i] + "();" + "\r\n");

								templateString.append(prefix + "." + childrenNodeNames[i] + ".anyField=true;" + "\r\n");
							} else if (myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]).equals("*")) {
								templateString.append(prefix + "." + childrenNodeNames[i] + "=new "
										+ childrenNodeTypes[i] + "();" + "\r\n");

								templateString.append(prefix + "." + childrenNodeNames[i] + ".anyField=true;" + "\r\n");
							} else {

								templateString
										.append(prefix + "." + childrenNodeNames[i] + "=new " + childrenNodeTypes[i]
												+ "(\"" + myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i])
												+ "\");" + "\r\n");
							}
						}
						if (myASTVisitor.nodeNameChildrenNamesHashMap.containsKey(childrenNodeTypes[i])) {
							if (myASTVisitor.templateIdValuePairs.containsKey(childrenNodeNames[i])
									|| myASTVisitor.templateIdValuePairs.containsKey(childrenNodeNames[i])) {
								if (myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]).equals("?")) {
									templateString.append(prefix + "." + childrenNodeNames[i] + "=new "
											+ childrenNodeTypes[i] + "();" + "\r\n");

									templateString
											.append(prefix + "." + childrenNodeNames[i] + ".anyField=true;" + "\r\n");
								} else if (myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]).equals("*")) {
									templateString.append(prefix + "." + childrenNodeNames[i] + "=new "
											+ childrenNodeTypes[i] + "();" + "\r\n");

									templateString.append(
											prefix + "." + childrenNodeNames[i] + ".anyOrOmitField=true;" + "\r\n");
								}
							}
						}
					}
				}
				if (templateListValues.size() > 0) {
					if (childrenNodeTypes[i].equals("INTEGER")) {
						templateString.append(prefix + "." + childrenNodeNames[i] + "= new INTEGER(\""
								+ templateListValues.get(i) + "\");\r\n");
					}
				}

			}
		} else if (rootNodeType.equals("INTEGER")) {
			String integerValue = myASTVisitor.templateIdValuePairs.get(nodeName);
			templateString.append("value=new INTEGER(\"" + integerValue + "\");\r\n");
		}

	}

	public void writeModifierTemplateConstructor(String rootNodeType, String prefix) {
		if (myASTVisitor.nodeNameChildrenNamesHashMap.containsKey(rootNodeType)) {

			if (myASTVisitor.nodeNameNodeTypeHashMap.get(rootNodeType).equals("union")) {

				int unionElementId = 0;
				for (int i = 0; i < myASTVisitor.nodeNameChildrenNamesHashMap.get(rootNodeType).length; i++) {

					if (myASTVisitor.nodeNameChildrenNamesHashMap.get(rootNodeType)[i]
							.equals(templateIdentifiers.get(firstIdentifier))) {
						unionElementId = i + 1;
					}
				}

				templateString.append(prefix + "= " + modifierValue + "();\r\n");

				prefix = "((SC_" + unionElementId + "_" + rootNodeType + ")value)";

				templateString.append(
						"if(value==null) value= new " + "SC_" + unionElementId + "_" + rootNodeType + "();\r\n");

				templateString.append("if(value.anyField) value.anyField=false;\r\n");

				templateString.append("if(value.omitField) value.omitField=false;\r\n");

				templateString.append("if(value.anyOrOmitField) value.anyOrOmitField=false;\r\n\r\n");

			}

			String[] childrenNodeNames = myASTVisitor.nodeNameChildrenNamesHashMap.get(rootNodeType);
			String[] childrenNodeTypes = myASTVisitor.nodeNameChildrenTypesHashMap.get(rootNodeType);

			for (int i = 0; i < childrenNodeNames.length; i++) {
				if (myASTVisitor.templateIDs.contains(childrenNodeNames[i])) {
					if (myASTVisitor.nodeNameChildrenNamesHashMap.containsKey(childrenNodeTypes[i])
							&& !myASTVisitor.templateIdValuePairs.containsKey(childrenNodeNames[i])) {

						// walk childern

						modifierTemplateIfWriter(prefix, "=new " + childrenNodeTypes[i] + "();\r\n",
								childrenNodeNames[i], false, false, false, false);

						String prefixBackup = prefix;
						prefix = prefix.concat("." + childrenNodeNames[i]);

						writeModifierTemplateConstructor(childrenNodeTypes[i], prefix);

						prefix = prefixBackup;

					} // print child element values

					// constant
					if (myASTVisitor.templateIdValuePairs.containsKey(childrenNodeNames[i])) {
						String currentvalue = myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]);
						if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(currentvalue)) {
							if (myASTVisitor.nodeNameNodeTypeHashMap.get(currentvalue).equals("constant")) {
								/*
								 * templateString.append(prefix + "." +
								 * childrenNodeNames[i] + "=Constants." +
								 * currentvalue + "();\r\n");
								 */
							}
						} else if (paramCount > 0) {// p_Uri
							if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(childrenNodeTypes[i])) {
								for (int pCounter = 0; pCounter < paramTypes.size(); pCounter++) {
									if (childrenNodeTypes[i].equals(paramTypes.get(pCounter))) {
										modifierTemplateIfWriter(prefix, "=" + currentvalue + ";\r\n",
												childrenNodeNames[i], true, false, false, false);

									}
								}
							}
						}
					}

					// everything else
					if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(childrenNodeTypes[i])) {
						if (myASTVisitor.nodeNameNodeTypeHashMap.get(childrenNodeTypes[i]).equals("enum")) {
							modifierTemplateIfWriter(prefix,
									"=new " + childrenNodeTypes[i] + "(\""
											+ myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]) + "\");\r\n",
									childrenNodeNames[i], true, false, false, false);

						} else if (myASTVisitor.nodeNameNodeTypeHashMap.get(childrenNodeTypes[i])
								.equals("CHARSTRING")) {
							// TODO CHARSTRING

						}

					} else if (childrenNodeTypes[i].equals("CHARSTRING")) {

						if (myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]).equals("omit")) {
							modifierTemplateIfWriter(prefix, "=null;\r\n", childrenNodeNames[i], true, false, true,
									false);

						} else if (myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]).equals("?")) {
							modifierTemplateIfWriter(prefix, "=null;\r\n", childrenNodeNames[i], true, true, false,
									false);
						} else if (myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]).equals("*")) {
							modifierTemplateIfWriter(prefix, "=null;\r\n", childrenNodeNames[i], true, false, false,
									true);
						} else {
							// TODO rest
						}
					}
					if (myASTVisitor.nodeNameChildrenNamesHashMap.containsKey(childrenNodeTypes[i])) {
						if (myASTVisitor.templateIdValuePairs.containsKey(childrenNodeNames[i])
								|| myASTVisitor.templateIdValuePairs.containsKey(childrenNodeNames[i])) {
							if (myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]).equals("?")) {
								modifierTemplateIfWriter(prefix, "=null;\r\n", childrenNodeNames[i], true, true, false,
										false);
							} else if (myASTVisitor.templateIdValuePairs.get(childrenNodeNames[i]).equals("*")) {
								modifierTemplateIfWriter(prefix, "=null;\r\n", childrenNodeNames[i], true, false, false,
										true);
							}
						}
					}
				}
			}
		} else if (rootNodeType.equals("INTEGER")) {
			String integerValue = myASTVisitor.templateIdValuePairs.get(nodeName);

			 if (integerValue.contains(".")) {
				 templateString.append("value="+integerValue + ";\r\n");
			}else{
				templateString.append("value=new INTEGER(\"" + integerValue + "\");\r\n");
			}

		}
	}

	public void modifierTemplateIfWriter(String preText, String newConstructor, String elementName, boolean isVvalue,
			boolean isAnyField, boolean isOmitFields, boolean isAnyOrOmitField) {

		if (!isVvalue) {
			templateString.append(
					"if(" + preText + "." + elementName + "==null) " + preText + "." + elementName + newConstructor);
		} else {
			templateString.append(preText + "." + elementName + newConstructor);
		}

		if (!isAnyField) {
			templateString.append("if(" + preText + "." + elementName + ".anyField) " + preText + "." + elementName
					+ ".anyField=false;" + "\r\n");
		} else {
			templateString.append("if(" + preText + "." + elementName + ".anyField) " + preText + "." + elementName
					+ ".anyField=true;" + "\r\n");
		}

		if (!isOmitFields) {
			templateString.append("if(" + preText + "." + elementName + ".omitField) " + preText + "." + elementName
					+ ".omitField=false;" + "\r\n");
		} else {
			templateString.append("if(" + preText + "." + elementName + ".omitField) " + preText + "." + elementName
					+ ".omitField=true;" + "\r\n");
		}

		if (!isAnyOrOmitField) {
			templateString.append("if(" + preText + "." + elementName + ".anyOrOmitField) " + preText + "."
					+ elementName + ".anyOrOmitField=false;" + "\r\n\r\n");
		} else {
			templateString.append("if(" + preText + "." + elementName + ".anyOrOmitField) " + preText + "."
					+ elementName + ".anyOrOmitField=true;" + "\r\n\r\n");
		}
	}

	public void clearLists() {
		templateListValues.clear();
		templateIdentifiers.clear();
		templateRefdVals.clear();
		paramNames.clear();
		paramTypes.clear();

	}

	public void init() {

		firstIdentifier = 1;
		if (templateNode.getFormalParameterList() != null) {
			paramCount = templateNode.getFormalParameterList().getNofParameters();
			for (int i = 0; i < paramCount; i++) {
				paramNames.add(templateNode.getFormalParameterList().getParameterByIndex(i).getIdentifier().toString());
				if (templateNode.getFormalParameterList().getParameterByIndex(i)
						.getType(compilationCounter) instanceof Referenced_Type) {

					paramTypes.add(((Referenced_Type) templateNode.getFormalParameterList().getParameterByIndex(i)
							.getType(compilationCounter)).getReference().getId().toString());
				} else if(templateNode.getFormalParameterList().getParameterByIndex(i)
						.getType(compilationCounter) instanceof Integer_Type){
					paramTypes.add("INTEGER");
				}
				
				
				firstIdentifier++;
			}
		}
	}

	public String getJavaSource() {

		AstWalkerJava.logToConsole("	Starting processing:  Template " + nodeName);

		templateString.append("public static " + templateNodeType + " " + nodeName + "(");

		for (int i = 0; i < paramCount; i++) {
			templateString.append(paramTypes.get(i) + " " + paramNames.get(i));
			if(i<paramCount-1){
				templateString.append(" , ");
			}

		}

		templateString.append("){\r\n");
		templateString.append(templateNodeType + " value;\r\n");

		if ((modifierValue == null)) {
			writeTemplateConstructor(templateNodeType, "value");
		} else {
			writeModifierTemplateConstructor(templateNodeType, "value");

		}

		templateString.append("return value;\r\n");
		templateString.append("}\r\n");
		String returnString = templateString.toString();
		modifierValue = null;

		templateString.setLength(0);

		AstWalkerJava.logToConsole("	Finished processing:  Template " + nodeName);

		return returnString;

	}

	public void setModifierValue(String modifierValue) {
		if ((!nodeName.equals(modifierValue)) && (!templateNodeType.equals(modifierValue))) {
			if (paramTypes.size() > 0) {
				for (int i = 0; i < paramTypes.size(); i++) {
					if (!paramTypes.get(i).equals(modifierValue)) {
						this.modifierValue = modifierValue;
					}
				}
			} else {
				this.modifierValue = modifierValue;
			}

		}

	}
}
