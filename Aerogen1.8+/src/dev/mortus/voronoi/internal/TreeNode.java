package dev.mortus.voronoi.internal;

import java.awt.geom.Point2D;

import dev.mortus.voronoi.Site;

public abstract class TreeNode {
	
	private static int IDCounter = 0;

	private TreeNode parent;
	private TreeNode leftChild, rightChild;
	private TreeNode predecessor, successor;
	
	public final int id;
	
	public TreeNode() {
		this.id = IDCounter++;
	}
		
	public abstract String getType();
	public abstract boolean hasChildren();
	
	protected void setLeftChild(TreeNode left) {
		removeLeftChild();
		TreeNode first = left.getFirstDescendant();
		TreeNode last = left.getLastDescendant();
		
		// connect child to tree (predecessor & successor)
		first.predecessor = this.predecessor;
		last.successor = this;
				
		// connect tree to child (predecessor & successor)		
		this.predecessor.successor = first;
		this.predecessor = last;
				
		// create parent/child relationship
		left.parent = this;
		this.leftChild = left;
	}
	
	private void removeLeftChild() {
		if (this.leftChild == null) return;
		TreeNode first = this.leftChild.getFirstDescendant();
		
		// disconnect tree from child (predecessor & successor)
		first.predecessor.successor = this;
		this.predecessor = first.predecessor;
		
		// disconnect removed child from tree (predecessor & successor)
		first.predecessor = null;		
		this.leftChild.getLastDescendant().successor = null;
		
		// remove parent/child relationship
		this.leftChild.parent = null;
		this.leftChild = null;
	}

	public TreeNode getLeftChild() {
		return leftChild;
	}

	public boolean isLeftChild() {
		if (parent == null) return false;
		return parent.leftChild == this;
	}

	protected void setRightChild(TreeNode right) {
		removeRightChild();
		TreeNode first = right.getFirstDescendant();
		TreeNode last = right.getLastDescendant();
		
		// connect child to tree (predecessor & successor)
		first.predecessor = this;
		last.successor = this.successor;
		
		// connect tree to child (predecessor & successor)
		this.successor.predecessor = last;
		this.successor = first;
		
		// create parent/child relationship
		right.parent = this;
		this.rightChild = right;
	}

	private void removeRightChild() {
		if (this.rightChild == null) return;
		TreeNode last = this.rightChild.getLastDescendant();
		
		// disconnect tree from child (predecessor & successor)
		last.successor.predecessor = this;
		this.successor = last.successor;
		
		// disconnect removed child from tree (predecessor & successor)
		last.successor = null;		
		this.rightChild.getFirstDescendant().predecessor = null;
		
		// remove parent/child relationship
		this.rightChild.parent = null;
		this.rightChild = null;
	}

	public TreeNode getRightChild() {
		return rightChild;
	}
	
	public boolean isRightChild() {
		if (parent == null) return false;
		return parent.rightChild == this;
	}
	
	/**
	 * Returns the first descendant of this tree node.
	 * If this node has no left descendants then the first
	 * "descendant" is itself. 
	 * Similar to first item in a doubly linked list when called
	 * from the root node of a tree.
	 */
	public TreeNode getFirstDescendant() {
		TreeNode n = this;
		while (n.leftChild != null) n = n.leftChild;
		return n;
	}

	/**
	 * Returns the last descendant of this tree node.
	 * If this node has no right descendants then the last
	 * "descendant" is itself. 
	 * Similar to last item in a doubly linked list when called
	 * from the root node of a tree.
	 */
	public TreeNode getLastDescendant() {
		TreeNode n = this;
		while (n.rightChild != null) n = n.rightChild;
		return n;
	}

	/**
	 * Finds the predecessor by traversing the tree instead of referencing
	 * the saved node variable. Used internally for newly inserted nodes.
	 * @return
	 */
	protected TreeNode findPredecessor() {
		if (this.hasChildren()) {
			if (this.leftChild == null) return null;
			return this.leftChild.getLastDescendant();
		} else {
			TreeNode n = this;
			while (n != null && !n.isRightChild()) n = n.parent;
			if (n != null && n != this) return n.parent;
		}
		return null;
	}

	public TreeNode getPredecessor() {
		return predecessor;
	}

	/**
	 * Finds the successor by traversing the tree instead of referencing
	 * the saved node variable. Used internally for newly inserted nodes.
	 * @return
	 */
	protected TreeNode findSuccessor() {
		if (this.hasChildren()) {
			if (this.rightChild == null) return null;
			return this.rightChild.getFirstDescendant();
		} else {
			TreeNode n = this;
			while (n != null && !n.isLeftChild()) n = n.parent;
			if (n != null && n != this) return n.parent;
		}
		return null;
	}
	
	public TreeNode getSuccessor() {
		return successor;
	}
	
	public abstract Arc getArc(BuildState state, double x);

	protected static class Sentinel extends TreeNode {

		@Override
		public String getType() {
			return "Sentinel";
		}

		@Override
		public boolean hasChildren() {
			return true;
		}

		@Override
		public Arc getArc(BuildState state, double x) {
			return getLeftChild().getArc(state, x);
		}
		
	}
	
}
