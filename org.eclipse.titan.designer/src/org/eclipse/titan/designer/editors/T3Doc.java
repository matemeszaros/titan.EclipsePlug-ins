/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NamedBridgeScope;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ControlPart;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IEditorPart;

public final class T3Doc {

	private static final String LINE_BREAK = "<br></br>";

	// ToDo: Note! This is initial phase of the T3Doc implementation.
	// check and measure the bottlenecks, and do some optimization if
	// necessary

	private static final String NEWLINE = System.getProperty("line.separator");

	private static final String[][] PARAGRAPHS = {
			{ "@author", "Author:" },
			{ "@config", "Configuration:" },
			{ "@desc", "Description:" },
			{ "@exception", "Exception:" },
			// these are not needed in new structure, just to know
			// that they are present
			// {"@member", "Member:"},
			// {"@param", "Parameter:"},
			{ "@purpose", "Purpose:" }, { "@remark", "Remark:" }, { "@return", "Return:" }, { "@see", "See:" }, { "@since", "Since:" },
			{ "@status", "Status:" }, { "@remark", "Remark:" }, { "@url", "Url:" }, { "@verdict", "Verdict:" },
			{ "@version", "Version:" }, { "@priority", "Priority:" }, { "@requirement", "Requirement:" }, { "@reference", "Reference:" } };

	private static final String[] TEST_CASE = { "@exception", "@member", "@return" };

	private static final String[] PORT = { "@config", "@exception", "@param", "@priority", "@purpose", "@requirement", "@return", "@verdict" };

	private static final String[] CONSTANT = { "@config", "@exception", "@member", "@param", "@priority", "@purpose", "@requirement", "@return",
			"@verdict" };

	private static final String[] CONSTANT_MEMBER_CAPABLE = { "@config", "@exception", "@param", "@priority", "@purpose", "@requirement",
			"@return", "@verdict" };

	private static final String[] TEMPLATE = { "@config", "@exception", "@priority", "@purpose", "@requirement", "@return", "@verdict" };

	private static final String[] ALTSTEP = { "@config", "@exception", "@member", "@priority", "@purpose", "@return" };

	private static final String[] FUNCTION = { "@config", "@exception", "@member", "@priority", "@purpose" };

	private static final String[] TYPE_SIGNATURE = { "@config", "@member", "@priority", "@purpose", "@requirement", "@verdict" };

	private static final String[] MODULE = { "@config", "@exception", "@member", "@param", "@priority", "@return" };

	private static final String[] GROUP = { "@config", "@exception", "@member", "@param", "@priority", "@purpose", "@requirement", "@return",
			"@verdict" };

	private static final String[] TTCN3_SEQUENCE = { "@config", "@exception", "@param", "@priority", "@purpose", "@requirement", "@return",
			"@verdict" };

	private static final String[] TTCN3_SEQUENCE_OF = { "@config", "@exception", "@member", "@param", "@priority", "@purpose", "@requirement",
			"@return", "@verdict" };

	private static final String[] TTCN3_MEMBER_CAPABLE = { Type_type.TYPE_TTCN3_CHOICE.toString(), Type_type.TYPE_TTCN3_ENUMERATED.toString(),
			Type_type.TYPE_TTCN3_SET.toString(), Type_type.TYPE_TTCN3_SEQUENCE.toString(), Type_type.TYPE_REFERENCED.toString() };

	private static boolean T3DocEnable;

	private static Location location;

	private Map<String, String> params;
	private List<String> paramsArraylist;

	private Map<String, String> members;

	private String signature;

	private List<String> author;
	private List<String> config;
	private List<String> desc;
	private List<String> exception;
	// these are not needed, just to know that they are present
	// private ArrayList<String> member;
	// private ArrayList<String> param;
	private List<String> purpose;
	private List<String> remark;
	private List<String> creturn;
	private List<String> see;
	private List<String> since;
	private List<String> status;
	private List<String> url;
	private List<String> verdict;
	private List<String> version;
	private List<String> priority;
	private List<String> requirement;
	private List<String> reference;

	static {
		final IPreferencesService prefService = Platform.getPreferencesService();
		T3DocEnable = prefService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.T3DOC_ENABLE, false, null);

		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();

					if (PreferenceConstants.T3DOC_ENABLE.equals(property)) {
						T3DocEnable = prefService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.T3DOC_ENABLE, false, null);
						return;
					}
				}
			});
		}
	}

	public T3Doc(final String desc) {
		this.desc = new ArrayList<String>();
		this.desc.add(desc);
	}

	public T3Doc(final Location commentLocation) {

		// Fill content
		t3DocFillContentFromCommentLocation(commentLocation);

		List<String> membersArraylist = T3Doc.getCommentStrings(commentLocation, "@member");
		if (!membersArraylist.isEmpty()) {
			members = new HashMap<String, String>();
		}
		// ToDo what about spaces
		for (String stringItem : membersArraylist) {
			final String stringItemTrim = stringItem.trim();
			final String strWithoutMember = stringItemTrim.substring("@member".length()).trim();
			final String key;
			final String value;
			int cnt = strWithoutMember.indexOf(" ");
			if (cnt == -1) {
				key = strWithoutMember;
				value = "";
			} else {
				key = strWithoutMember.substring(0, strWithoutMember.indexOf(" "));
				value = strWithoutMember.substring(key.length());
			}
			members.put(key, value);
		}

		List<String> paramsArraylistTemp = T3Doc.getCommentStrings(commentLocation, "@param");
		if (!paramsArraylistTemp.isEmpty()) {
			params = new HashMap<String, String>();
			paramsArraylist = new ArrayList<String>();
		}
		// ToDo what about spaces
		for (String stringItem : paramsArraylistTemp) {
			final String stringItemTrim = stringItem.trim();
			final String strWithoutParam = stringItemTrim.substring("@param".length()).trim();
			final String key;
			final String value;
			int cnt = strWithoutParam.indexOf(" ");
			if (cnt == -1) {
				key = strWithoutParam;
				value = "";
			} else {
				key = strWithoutParam.substring(0, strWithoutParam.indexOf(" "));
				value = strWithoutParam.substring(key.length());
			}
			paramsArraylist.add(key.toString() + value.toString());
			params.put(key, value);
		}
	}

	public T3Doc(final Location commentLocation, final String str) {
		// Fill content
		t3DocFillContentFromCommentLocation(commentLocation);

		List<String> membersArraylist = T3Doc.getCommentStrings(commentLocation, "@member");
		if (!membersArraylist.isEmpty()) {
			members = new HashMap<String, String>();
		}
		// ToDo what about spaces
		for (String stringItem : membersArraylist) {
			final String strWithoutMember = stringItem.substring("@member".length()).trim();
			if (strWithoutMember.indexOf(str) == -1) {
				continue;
			}

			final String key;
			final String value;
			int cnt = strWithoutMember.indexOf(" ");
			if (cnt == -1) {
				key = strWithoutMember;
				value = "";
			} else {
				key = str;
				value = strWithoutMember;
			}
			members.put(key, value);
		}

		/* ArrayList<String> */paramsArraylist = T3Doc.getCommentStrings(commentLocation, "@param");
		if (!paramsArraylist.isEmpty()) {
			params = new HashMap<String, String>();
		}
		// ToDo what about spaces
		for (String stringItem : paramsArraylist) {
			final String strWithoutParam = stringItem.substring("@param".length()).trim();
			final String key;
			final String value;
			int cnt = strWithoutParam.indexOf(" ");
			if (cnt == -1) {
				key = strWithoutParam;
				value = "";
			} else {
				key = str;
				value = strWithoutParam;
			}
			params.put(key, value);
		}
	}

	public static boolean isT3DocEnable() {
		return T3DocEnable;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(final String signature) {
		this.signature = signature;
	}

	public Map<String, String> getMembers() {
		return members;
	}

	public Map<String, String> getParams() {
		return params;
	}

	private static String replace(final String str) {
		// remove unnecessary comments
		String localStr = str.replaceAll("\\/\\/\\*", "");

		// some fast check
		if (str.indexOf("@") == -1) {
			return str;
		}

		// Convert to readable format
		for (String[] item : PARAGRAPHS) {
			localStr = localStr.replaceAll(item[0], item[1]);
		}

		return localStr;
	}

	public static void check(final Location commentLocation, final String type) {
		if (!T3DocEnable) { return; } //just to spare time
		check(commentLocation, type, null);
	}

	public static void check(final Location commentLocation, final String type, final Type_type typeType) {
		if (!T3DocEnable) {
			return;
		}

		if (commentLocation != null) {
			final IFile file = (IFile) commentLocation.getFile();
			try {

				String[] temp;

				// Not every version likes switch case
				// "testcase"
				if (Def_Testcase.getKind().equals(type)) {
					temp = TEST_CASE;
				} else if (
				// "port definition"
				Def_Port.getKind().equals(type) ||
				// " module parameter"
						Def_ModulePar.getKind().equals(type) ||
						// "constant "
						Def_Const.getKind().equals(type)) {
					// special treatment for enum, set, seq
					// and choice
					boolean memberCapable = false;
					if (typeType != null) {
						for (String string : TTCN3_MEMBER_CAPABLE) {
							if (typeType.toString().equals(string)) {
								memberCapable = true;
							}
						}
					} else {
						memberCapable = true;
					}
					if (memberCapable) {
						temp = CONSTANT_MEMBER_CAPABLE;
					} else {
						temp = CONSTANT;
					}
				} else if (Def_Template.getKind().equals(type)) {
					temp = TEMPLATE;
				} else if (Def_Altstep.getKind().equals(type)) {
					temp = ALTSTEP;
				} else if (Def_Function.getKind().equals(type)) {
					temp = FUNCTION;
				} else if ("TYPE_SIGNATURE".equals(type)) {
					temp = TYPE_SIGNATURE;
				} else if (TTCN3Module.MODULE.equals(type)) {
					temp = MODULE;
				} else if ("group".equals(type) || ControlPart.getKind().equals(type)) {
					temp = GROUP;
				} else if ("TYPE_TTCN3_SEQUENCE".equals(type) || "TYPE_TTCN3_CHOICE".equals(type) || "TYPE_TTCN3_SET".equals(type)) {
					temp = TTCN3_SEQUENCE;
				} else if ("TYPE_SEQUENCE_OF".equals(type)) {
					temp = TTCN3_SEQUENCE_OF;
				} else if ("TYPE_TTCN3_ENUMERATED".equals(type)) {
					temp = TTCN3_SEQUENCE;
				} else if ("TYPE_PORT".equals(type) || "TYPE_COMPONENT".equals(type)) {
					temp = PORT;
				} else {
					temp = null;
				}

				File realFile = file.getLocation().toFile();

				BufferedReader in = new BufferedReader(new FileReader(realFile));
				in.skip(commentLocation.getOffset());
				String str;

				int lineCnt = 0;
				int offset = commentLocation.getOffset();
				while ((str = in.readLine()) != null) {

					if (str.indexOf("//") == -1) {
						break;
					}

					int loc = -1;
					if (temp != null) {
						for (String item : temp) {
							loc = str.indexOf(item);
							if (loc != -1) {
								location = new Location(commentLocation.getFile(), commentLocation.getLine()
										+ lineCnt, offset + lineCnt, offset + str.length() + lineCnt);
								location.reportSemanticWarning(MessageFormat.format(
										"Comment `{0}'' cannot be used with this type!", item));
							}
						}
					}
					offset += str.length();
					lineCnt++;
				}
				in.close();

			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace("Comment is not reachable", e);
			}
		}
	}

	public static String getCommentStrings(final List<String> arraylistIn) {
		StringBuilder strb = new StringBuilder();

		int i = 0;
		for (String str : arraylistIn) {
			// check if we are still in the comment, do not collect
			// all the members
			if (str.indexOf("//*") == -1) {
				break;
			}
			int loc = str.indexOf("//*");

			if (-1 != loc) {
				if (i > 0) {
					strb.append("<BR></BR>");
				}
				strb.append(str.substring("//*".length()));
			}
			i++;
		}
		return strb.toString();
	}

	private static List<String> getAllCommentStrings(final Location commentLocation) {
		List<String> arraylist = new ArrayList<String>();

		if (commentLocation != null) {
			final IFile file = (IFile) commentLocation.getFile();

			List<String> arraylistMemory = getArrayListFromEditorTracker(file, commentLocation);

			if (arraylistMemory != null) {
				return arraylistMemory;
			}

			try {
				File realFile = file.getLocation().toFile();

				BufferedReader in = new BufferedReader(new FileReader(realFile));

				in.skip(commentLocation.getOffset());
				String str;

				while ((str = in.readLine()) != null) {
					// check if we are still in the comment,
					// do not collect all the members
					if (str.indexOf("//") == -1) {
						break;
					}

					String strTrim = str.trim();
					arraylist.add(strTrim);
				}
				in.close();

			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace("Comment is not reachable", e);
			}
		}

		return arraylist;
	}

	public static List<String> getCommentStrings(final List<String> arraylistIn, final String toSearch) {
		List<String> arraylist = new ArrayList<String>();

		String member = null;
		boolean memberFound = false;
		for (String str : arraylistIn) {

			// check if we are still in the comment, do not collect
			// all the members
			if (str.indexOf("//*") == -1) {
				// break;
				continue;
			}

			String strOld = str;
			int loc = str.indexOf(toSearch);

			int loc2 = str.indexOf("@");

			if (loc2 != -1) {
				memberFound = false;
				if (member != null) {
					arraylist.add(replace(member));
					member = null;
				}

			}

			if (-1 != loc || memberFound) {
				if (member == null) {
					// ToDo: loc can be negative value,
					// defend this case
					member = strOld.substring(loc > 0 ? loc : "//*".length());
				} else {
					// Throw away //******// comments as it
					// is unnecessary
					// ToDo: matches() method is not
					// optimized for performance.
					// Java.util.regex.Pattern and
					// java.util.regex.Matcher might
					// perform better, but without test it
					// at least questionable and overkill
					if (!strOld.matches("//\\**//$")) {
						member += strOld.substring("//*".length());
					}
				}

				memberFound = true;

				if (-1 == loc) {
					arraylist.add(replace(member));
					member = null;
				}
			}

		}
		if (member != null) {
			arraylist.add(replace(member));
		}

		return arraylist;

	}

	public static List<String> getCommentStrings(final Location commentLocation, final String toSearch) {
		if (commentLocation == null) {
			return new ArrayList<String>();
		}

		List<String> arraylist = new ArrayList<String>();

		final IFile file = (IFile) commentLocation.getFile();

		// try to get comments from EditorTracker
		List<String> arraylistMemory = getArrayListFromEditorTracker(file, commentLocation);
		String toSearchExact = " " + toSearch/* + " " */;

		if (arraylistMemory != null) {

			String member = null;
			boolean memberFound = false;
			for (String str : arraylistMemory) {
				// check if we are still in the comment, do not
				// collect all the members
				if (str.indexOf("//") == -1) {
					break;
				}

				String strOld = str;
				int loc = str.indexOf(toSearchExact);

				int loc2 = str.indexOf("@");

				if (loc2 != -1) {
					memberFound = false;
					if (member != null) {
						arraylist.add(replace(member));
						member = null;
					}
				}

				if (-1 != loc || memberFound) {
					int length = 0;

					if (strOld.indexOf("//*") > 0) {
						length = "//*".length();
					} else {
						length = "//".length();
					}

					if (member == null) {
						// ToDo: loc can be negative
						// value, defend this case
						member = strOld.substring(loc > 0 ? loc : length);
					} else {

						member += strOld.substring(length);
					}

					memberFound = true;

					if (-1 == loc) {
						arraylist.add(replace(member));
						member = null;
					}
				}
			}
			if (member != null) {
				arraylist.add(replace(member));
			}
			return arraylist;
		}

		try {
			File realFile = file.getLocation().toFile();

			BufferedReader in = new BufferedReader(new FileReader(realFile));

			in.skip(commentLocation.getOffset());
			String str;

			String member = null;
			boolean memberFound = false;
			while ((str = in.readLine()) != null) {

				// check if we are still in the comment, do not
				// collect all the members
				if (str.indexOf("//") == -1) {
					break;
				}

				String strOld = str;
				int loc = str.indexOf(toSearch);

				int loc2 = str.indexOf("@");

				if (loc2 != -1) {
					memberFound = false;
					if (member != null) {
						// arraylist.add(member);
						arraylist.add(replace(member));
						member = null;
					}
				}

				if (-1 != loc || memberFound) {
					if (member == null) {
						// ToDo: loc can be negative
						// value, defend this case
						member = strOld.substring(loc > 0 ? loc : "//*".length());
					} else {
						member += strOld.substring("//*".length());
					}

					memberFound = true;

					if (-1 == loc) {
						arraylist.add(replace(member));
						member = null;
					}
				}

			}
			if (member != null) {
				arraylist.add(replace(member));
			}
			in.close();

		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Comment is not reachable", e);
		}

		return arraylist;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		if (signature != null) {
			str.append(signature);
		}

		if (author != null && !author.isEmpty()) {
			for (String item : author) {
				str.append(item);
				str.append(LINE_BREAK);
			}
		}

		if (config != null && !config.isEmpty()) {
			for (String item : config) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (desc != null && !desc.isEmpty()) {
			for (String item : desc) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (exception != null && !exception.isEmpty()) {
			for (String item : exception) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (members != null && !members.isEmpty()) {
			Set<Entry<String, String>> set = members.entrySet();
			// Get an iterator
			Iterator<Entry<String, String>> i = set.iterator();
			if (members.size() > 1) {
				str.append("Members: " + LINE_BREAK);
			}
			// Display elements
			while (i.hasNext()) {
				Entry<String, String> me = i.next();
				str.append(me.getKey() + " ");
				str.append(me.getValue());
				str.append(LINE_BREAK);
			}
		}

		if (paramsArraylist != null && !paramsArraylist.isEmpty()) {
			str.append("Params: " + LINE_BREAK);
			for (int index = 0; index < paramsArraylist.size(); index++) {
				str.append(paramsArraylist.get(index));
				str.append(LINE_BREAK);
			}
		}

		if (purpose != null && !purpose.isEmpty()) {
			for (String item : purpose) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (remark != null && !remark.isEmpty()) {
			for (String item : remark) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (creturn != null && !creturn.isEmpty()) {
			for (String item : creturn) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (see != null && !see.isEmpty()) {
			for (String item : see) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (since != null && !since.isEmpty()) {
			for (String item : since) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (status != null && !status.isEmpty()) {
			for (String item : status) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (url != null && !url.isEmpty()) {
			for (String item : url) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (verdict != null && !verdict.isEmpty()) {
			for (String item : verdict) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (version != null && !version.isEmpty()) {
			for (String item : version) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (priority != null && !priority.isEmpty()) {
			for (String item : priority) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (requirement != null && !requirement.isEmpty()) {
			for (String item : requirement) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		if (reference != null && !reference.isEmpty()) {
			for (String item : reference) {
				str.append(item);
			}
			str.append(LINE_BREAK);
		}

		return str.toString();
	}

	public void setDesciption(final String desc) {
		this.desc = new ArrayList<String>();
		this.desc.add(desc);
	}

	void t3DocFillContentFromCommentLocation(final Location commentLocation) {

		if (commentLocation != null) {
			List<String> commentstorage = getAllCommentStrings(commentLocation);

			author = T3Doc.getCommentStrings(commentstorage, "@author");
			config = T3Doc.getCommentStrings(commentstorage, "@config");
			desc = T3Doc.getCommentStrings(commentstorage, "@desc");
			exception = T3Doc.getCommentStrings(commentstorage, "@exception");
			// these are not needed, just to know that they are
			// present
			// member = T3Doc.getCommentStrings(commentstorage,
			// "@member");
			// param = T3Doc.getCommentStrings(commentstorage,
			// "@param");
			purpose = T3Doc.getCommentStrings(commentstorage, "@purpose");
			remark = T3Doc.getCommentStrings(commentstorage, "@remark");
			creturn = T3Doc.getCommentStrings(commentstorage, "@return");
			see = T3Doc.getCommentStrings(commentstorage, "@see");
			since = T3Doc.getCommentStrings(commentstorage, "@since");
			status = T3Doc.getCommentStrings(commentstorage, "@status");
			url = T3Doc.getCommentStrings(commentstorage, "@url");
			verdict = T3Doc.getCommentStrings(commentstorage, "@verdic");
			version = T3Doc.getCommentStrings(commentstorage, "@version");
			priority = T3Doc.getCommentStrings(commentstorage, "@priority");
			requirement = T3Doc.getCommentStrings(commentstorage, "@requirement");
			reference = T3Doc.getCommentStrings(commentstorage, "@reference");
		}
	}

	private static List<String> getArrayListFromEditorTracker(final IFile file, final Location commentLocation) {
		if (file == null || commentLocation == null) {
			return null;
		}

		// get comment from file stored memory if present
		IDocument document = null;
		ISemanticTITANEditor editor = null;

		if (EditorTracker.containsKey(file)) {
			List<ISemanticTITANEditor> editors = EditorTracker.getEditor(file);
			editor = editors.get(0);
			document = editor.getDocument();
			if (document == null) {
				return null;
			}
			List<String> arraylistMemory = new ArrayList<String>();

			String completeFile;
			try {
				completeFile = document
						.get(commentLocation.getOffset(), commentLocation.getEndOffset() - commentLocation.getOffset());

				// store offset
				int offset = 0;
				boolean circulate = true;
				boolean simpleNewline = false;
				while (circulate) {
					// NEWLINE
					int loc = completeFile.indexOf(NEWLINE, offset);
					if (loc < 0) {
						// give a try with \n
						loc = completeFile.indexOf('\n', offset);
						if (loc < 0) {
							circulate = false;
							break;
						}
						simpleNewline = true;
					} else {
						simpleNewline = false;
					}

					String stringLine = completeFile.substring(offset, loc);
					arraylistMemory.add(stringLine);
					if (simpleNewline) {
						offset = loc + "\n".length();
					} else {
						offset = loc + NEWLINE.length();
					}

				}

				if (arraylistMemory != null) {
					return arraylistMemory;
				}

			} catch (BadLocationException e1) {
				ErrorReporter.logExceptionStackTrace(e1);
			}
		}

		return null;
	}

	public static String getCommentStringBasedOnReference(final DeclarationCollector declarationCollector,
			final List<DeclarationCollectionHelper> collected, final IEditorPart targetEditor, final IRegion hoverRegion,
			final IReferenceParser referenceParser, final ITextViewer textViewer) {
		if (!T3Doc.isT3DocEnable()) {
			return null;
		}

		Reference ref = declarationCollector.getReference();
		if (ref == null) {
			return null;
		}

		if ((ref.getMyScope() instanceof NamedBridgeScope || ref.getMyScope() instanceof FormalParameterList) && !collected.isEmpty()) {
			DeclarationCollectionHelper declaration = collected.get(0);

			if (declaration.node instanceof TTCN3_Sequence_Type || declaration.node instanceof FormalParameter) {
				final IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
				ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
				final String moduleName = projectSourceParser.containedModule(file);
				final Module tempModule = projectSourceParser.getModuleByName(moduleName);

				Assignment ass = tempModule.getEnclosingAssignment(hoverRegion.getOffset());
				if (ass != null) {
					Reference reference = referenceParser.findReferenceForOpening(file, hoverRegion.getOffset(),
							textViewer.getDocument());
					String str = reference.getDisplayName();

					List<String> al = T3Doc.getCommentStrings(ass.getCommentLocation(), str);
					if (!al.isEmpty()) {

						StringBuilder sb = new StringBuilder();
						for (String string : al) {
							sb.append(string);
						}
						return sb.toString();
					}
				}
			}
		}
		return null;
	}
}
