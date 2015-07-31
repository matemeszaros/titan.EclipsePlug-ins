package org.eclipse.titan.designer.AST.ASN1;

import java.lang.ref.WeakReference;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;

/**
 * Represents a block of tokens.
 * <p>
 * In ASN.1 most of the tokens inside blocks can not be analyzed directly in
 * parse time. For this reason we are collecting them in such blocks, and when
 * the semantics are, these blocks are processed.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class Block implements INamedNode, IVisitableNode {
	/** the naming parent of the block. */
	//private WeakReference<INamedNode> nameParent;

	/**
	 * The location of the whole block. This location encloses the block
	 * fully, as it is used to report errors to.
	 **/
	private Location mLocation;
	
	/** the naming parent of the block. */
	private WeakReference<INamedNode> mNameParent;
	
	public Block() {
	}

	public Block( final Location aLocation ) {
		this.mLocation = aLocation;
	}

	/** @return the location of the block */
	public Location getLocation() {
		return mLocation;
	}
	
	public void setLocation( final Location aLocation ) {
		mLocation = aLocation;
	}

	@Override
	public String getFullName() {
		return getFullName(null).toString();
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		if (null != mNameParent) {
			final INamedNode tempParent = mNameParent.get();
			if (null != tempParent) {
				return tempParent.getFullName(this);
			}
		}

		return new StringBuilder();
	}

	@Override
	public void setFullNameParent(final INamedNode nameParent) {
		this.mNameParent = new WeakReference<INamedNode>(nameParent);
	}

	@Override
	public INamedNode getNameParent() {
		return mNameParent.get();
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		//
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}

	abstract public int getTokenListSize();

}
