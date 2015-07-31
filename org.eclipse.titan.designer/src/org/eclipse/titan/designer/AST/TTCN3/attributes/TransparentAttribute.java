package org.eclipse.titan.designer.AST.TTCN3.attributes;
/**
 * Represents the transparent attribute 
 * 
 * @author Laszlo Baji
 * */
public final class TransparentAttribute extends ExtensionAttribute {

	@Override
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.TRANSPARENT;
	}

}
