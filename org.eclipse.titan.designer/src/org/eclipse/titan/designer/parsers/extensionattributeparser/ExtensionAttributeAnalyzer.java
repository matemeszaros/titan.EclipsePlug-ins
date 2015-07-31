package org.eclipse.titan.designer.parsers.extensionattributeparser;

import java.util.List;

import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute;

/**
 * Extension attribute parser analyzer base class 
 * @author Arpad Lovassy
 */
public abstract class ExtensionAttributeAnalyzer {

	protected List<ExtensionAttribute> attributes;

	public List<ExtensionAttribute> getAttributes() {
		return attributes;
	}

	public abstract void parse( AttributeSpecification specification );
}
