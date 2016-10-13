package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.ILocateableNode;

/**
 * Interface for extension attributes with in and out type mappings
 * @author Arpad Lovassy
 */
public interface IInOutTypeMappingAttribute extends ILocateableNode {

	public void setInMappings( final TypeMappings aMappings );

	public void setOutMappings( final TypeMappings aMappings );

}
