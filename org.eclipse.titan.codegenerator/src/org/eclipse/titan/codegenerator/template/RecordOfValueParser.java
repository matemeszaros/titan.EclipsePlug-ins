package org.eclipse.titan.codegenerator.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.codegenerator.Scope;
import org.eclipse.titan.codegenerator.SourceCode;
import org.eclipse.titan.codegenerator.myASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.templates.ListOfTemplates;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;

public class RecordOfValueParser extends TemplateValueParser {

	private boolean waitForValues = true;
	private final String subtype;
	private final RecordOfValue list;

	public RecordOfValueParser(Scope parent, ValueHolder holder, String type) {
		super(parent, holder);
		this.subtype = myASTVisitor.nodeNameRecordOfTypesHashMap.get(type);
		this.value = this.list = new RecordOfValue(type);
	}

	@Override
	public Scope process(IVisitableNode node) {
		if (!waitForValues && node instanceof ListOfTemplates) {
			return Action.skip(ListOfTemplates.class, this);
		}
		if (node instanceof TTCN3Template) {
			ValueHolder holder = list.getValueHolder();
			return TemplateValueParser.getScope(this, holder, subtype, node);
		}
		return this;
	}

	@Override
	public Scope finish(IVisitableNode node) {
		if (node instanceof ListOfTemplates) {
			waitForValues = false;
		}
		return super.finish(node);
	}

	private class RecordOfValue extends Value {

		private List<Value> values = new ArrayList<>();

		RecordOfValue(String type) {
			super(type);
		}

		ValueHolder getValueHolder() {
			return v -> values.add(v);
		}

		@Override
		public SourceCode write(SourceCode code, int indent) {
			code.append("new " + type + "(");
			for (int i = 0; i < values.size(); i++) {
				if (i != 0) {
					code.append(",");
				}
				code.newLine().indent(indent + 2).write(indent + 2, values.get(i));
			}
			if (0 < values.size()) {
				code.newLine().indent(indent);
			}
			code.append(")");
			return code;
		}
	}
}
