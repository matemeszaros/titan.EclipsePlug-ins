package org.eclipse.titan.common.parsers;

import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

//NOTE: it doesn't know its start and end tokens, it just contains a string
public class AddedParseTree implements ParseTree {

	private String mText;
	public List<ParseTree> children;
	
	public AddedParseTree( final String aText ) {
		mText = aText;
	}
	
	@Override
	public Interval getSourceInterval() {
		return null;
	}

	@Override
	public int getChildCount() {
		return children != null ? children.size() : 0;
	}

	@Override
	public Object getPayload() {
		return null;
	}

	@Override
	public String toStringTree() {
		return mText;
	}

	@Override
	public <T> T accept(ParseTreeVisitor<? extends T> arg0) {
		return null;
	}

	@Override
	public ParseTree getChild( int i ) {
		return children != null && i >= 0 && i < children.size() ? children.get( i ) : null;
	}

	@Override
	public ParseTree getParent() {
		return null;
	}

	@Override
	public String getText() {
		return mText;
	}

	@Override
	public String toStringTree(Parser arg0) {
		return mText;
	}

	public void setText(String aText) {
		mText = aText;
	}

}
