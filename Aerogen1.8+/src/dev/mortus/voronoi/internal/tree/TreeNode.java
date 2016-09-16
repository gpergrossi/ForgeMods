package dev.mortus.voronoi.internal.tree;

import dev.mortus.util.LinkedBinaryNode;

/**
 * This class mostly recasts the LinkedBinaryNode class. I tried to find a way around this by
 * using LinkedBinaryNode<T extends LinkedBinaryNode<?>>, but this was not possible to do because
 * the methods that need to remain private depend on the type argument.
 * 
 * @author Gregary Pergrossi
 */
public abstract class TreeNode extends LinkedBinaryNode {
	
	public TreeNode() {
		super();
	}
	
	public TreeNode(ShoreTree rootParent) {
		super(rootParent);
	}
	
	public TreeNode getLeftChild() {
		return (TreeNode) super.getLeftChild();
	}

	public TreeNode getRightChild() {
		return (TreeNode) super.getRightChild();
	}
	
	public TreeNode getParent() {
		return (TreeNode) super.getParent();
	}

	public TreeNode getPredecessor() {
		return (TreeNode) super.getPredecessor();
	}

	public TreeNode getSuccessor() {
		return (TreeNode) super.getSuccessor();
	}
	
	public TreeNode getRoot() {
		return (TreeNode) super.getRoot();
	}
	
	public TreeNode getFirstDescendant() {
		return (TreeNode) super.getFirstDescendant();
	}
	
	public TreeNode getLastDescendant() {
		return (TreeNode) super.getLastDescendant();
	}
	
	public TreeNode getSibling() {
		return (TreeNode) super.getSibling();
	}

	/**
	 * Returns an iterator over this node's subtree.
	 */
	@Override
	public Iterator<TreeNode> subtreeIterator() {
		return super.<TreeNode>castedSubtreeIterator();
	}
	
	public abstract Arc getArc(double sweeplineY, double siteX);
	
}
