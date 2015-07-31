package org.eclipse.titan.designer.parsers.asn1parser;


import org.eclipse.titan.common.parsers.TitanListener;

public class ASN1Listener extends TitanListener {

	public ASN1Listener() {
		super(); 
	}

	public ASN1Listener(ASN1Parser2 parser) {
		super.errorsStored = parser.getErrorStorage(); 
	}

}
