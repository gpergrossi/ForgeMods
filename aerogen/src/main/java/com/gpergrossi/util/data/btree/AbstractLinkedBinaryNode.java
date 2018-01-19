package com.gpergrossi.util.data.btree;

/**
 * This class includes some useful methods for a binary tree that
 * calculates and saves predecessor and successor nodes on each addition
 * or deletion of a node. There are no insert methods. This class is intended
 * to be extended by other classes to facilitate useful tree logic.
 * 
 * Originally used as a base class for a ShoreTree in Fortune's algorithm because
 * the tree provides better average case lookup and insertion than an array and
 * the getPredecessor and getSuccessor methods are called very frequently.
 * 
 * @author Gregary Pergrossi
 */

public abstract class AbstractLinkedBinaryNode<T extends AbstractLinkedBinaryNode<T>> extends AbstractBinaryNode<T> {
	
	T predecessor, successor;

	public AbstractLinkedBinaryNode() {
		super();
	}
	
	@Override
	public void removeFromParent() {
		// Remove parent/child relationship
		super.removeFromParent();
		
		// Disconnect the linked predecessor/successor relationships
		T first = this.getFirstDescendant();
		T last = this.getLastDescendant();
		if (first.predecessor != null) first.predecessor.successor = last.successor;
		if (last.successor != null) last.successor.predecessor = first.predecessor;
		first.predecessor = null;
		last.successor = null;
	}

	/**
	 * This method is UNSAFE and is intended only for internal class use. <br />
	 * Improper use can cause internal inconsistencies in the tree structure.<br /><br />
	 * 
	 * Links the predecessor/successor relationships between: <br />
	 * 1. {@code before} and the first descendant of {@code newNodeSubTree} <br />
	 * 2. The last descendant of {@code newNodeSubTree} and {@code after} <br /><br />
	 * 
	 * If {@code before} or {@code after} is null, the subtree's links to the predecessor/successor (respectively) will be null.
	 * 
	 * @param before - the "before" node of the new links
	 * @param newNodeSubTree - the non-null subtree to be inserted between {@code before} and {@code after}
	 * @param after - the "after" node of the new links
	 * 
	 * @throws NullPointerException if newNodeSubtree is null
	 */
	protected final void internalLinkNodes(T before, T newNodeSubtree, T after) {
		if (newNodeSubtree == null) throw new NullPointerException();
		
		T first = newNodeSubtree.getFirstDescendant();
		T last = newNodeSubtree.getLastDescendant();
		if (before != null) before.successor = first;
		if (after != null) after.predecessor = last;
		first.predecessor = before;
		last.successor = after;
	}
	
	@Override
	public void setLeftChild(T left) {
		// Set new parent/child relationship, remove previous child if present
		super.setLeftChild(left);
		if (left == null) return;
		
		@SuppressWarnings("unchecked")
		T self = (T) this;
		
		// Stitch together predecessor/successor relationships
		internalLinkNodes(self.predecessor, left, self);
	}
	
	@Override
	public void setRightChild(T right) {
		// Set new parent/child relationship, remove previous child if present
		super.setRightChild(right);
		if (right == null) return;
		
		@SuppressWarnings("unchecked")
		T self = (T) this;
		
		// Stitch together predecessor/successor relationships
		internalLinkNodes(self, right, self.successor);
	}
	
	@Override
	public T getPredecessor() {
		return predecessor;
	}
	
	@Override
	public T getSuccessor() {
		return successor;
	}
	
}
