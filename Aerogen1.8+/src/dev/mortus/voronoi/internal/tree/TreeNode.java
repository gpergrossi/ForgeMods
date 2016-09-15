package dev.mortus.voronoi.internal.tree;

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
	
	public abstract Arc getArc(double sweeplineY, double siteX);
	
}
