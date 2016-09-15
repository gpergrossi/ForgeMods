package dev.mortus.voronoi.internal.tree;

public abstract class TreeNode {
	
	public static enum Type {
		Sentinel(true), Arc(false), Breakpoint(true);
		
		private boolean canHaveChildren;
		private Type(boolean canHaveChildren) {
			this.canHaveChildren = canHaveChildren;
		}
		
		public boolean canHaveChildren() {
			return canHaveChildren;
		}
	}
	
	public static int IDCounter = 0;

	private ShoreTree rootParent;
	private TreeNode parent;
	private TreeNode leftChild, rightChild;
	private TreeNode predecessor, successor;
	
	public final int id;
	protected String name;

	public TreeNode() {
		this.id = IDCounter++;
	}

	public TreeNode(ShoreTree rootParent) {
		this();
		this.rootParent = rootParent;
	}
	
	public abstract Type getType();
	
	
	
	public TreeNode getParent() {
		return parent;
	}
	
	protected boolean isRoot() {
		return rootParent != null;
	}
	
	public void promoteToRoot(TreeNode newRoot) {
		this.rootParent.root = newRoot;
		newRoot.rootParent = this.rootParent;
		this.rootParent = null;
	}
	
	public void replaceWith(TreeNode node) {
		if (this.isLeftChild()) {
			this.getParent().setLeftChild(node);
		} else if (this.isRightChild()) {
			this.getParent().setRightChild(node);
		} else if (this.isRoot()) {
			this.promoteToRoot(node);
		} else {
			throw new RuntimeException("TreeNode to be replaced has no parent");
		}
	}

	
	
	public TreeNode getLeftChild() {
		return leftChild;
	}
	
	protected void setLeftChild(TreeNode left) {
		//IMPORTANT! this.predecessor must refer to a node that stays in the tree
		removeLeftChild();
		
		TreeNode first = left.getFirstDescendant();
		TreeNode last = left.getLastDescendant();
		
		// connect child to tree (predecessor & successor)
		first.predecessor = this.predecessor;
		last.successor = this;
				
		// connect tree to child (predecessor & successor)		
		if (this.predecessor != null) this.predecessor.successor = first;
		this.predecessor = last;
				
		// create parent/child relationship
		left.parent = this;
		this.leftChild = left;
	}
	
	private void removeLeftChild() {
		if (this.leftChild == null) return;
		TreeNode first = this.leftChild.getFirstDescendant();
		
		// disconnect tree from child (predecessor & successor)
		if (first.predecessor != null) first.predecessor.successor = this;
		this.predecessor = first.predecessor;
		
		// disconnect removed child from tree (predecessor & successor)
		first.predecessor = null;		
		this.leftChild.getLastDescendant().successor = null;
		
		// remove parent/child relationship
		this.leftChild.parent = null;
		this.leftChild = null;
	}

	public boolean isLeftChild() {
		if (parent == null) return false;
		return parent.leftChild == this;
	}

	
	
	public TreeNode getRightChild() {
		return rightChild;
	}
	
	protected void setRightChild(TreeNode right) {
		//IMPORTANT! this.predecessor must refer to a node that stays in the tree
		removeRightChild();
		
		TreeNode first = right.getFirstDescendant();
		TreeNode last = right.getLastDescendant();
		
		// connect child to tree (predecessor & successor)
		first.predecessor = this;
		last.successor = this.successor;
		
		// connect tree to child (predecessor & successor)
		if (this.successor != null) this.successor.predecessor = last;
		this.successor = first;
		
		// create parent/child relationship
		right.parent = this;
		this.rightChild = right;
	}

	private void removeRightChild() {
		if (this.rightChild == null) return;
		TreeNode last = this.rightChild.getLastDescendant();
		
		// disconnect tree from child (predecessor & successor)
		if (last.successor != null) last.successor.predecessor = this;
		this.successor = last.successor;
		
		// disconnect removed child from tree (predecessor & successor)
		last.successor = null;		
		this.rightChild.getFirstDescendant().predecessor = null;
		
		// remove parent/child relationship
		this.rightChild.parent = null;
		this.rightChild = null;
	}
	
	public boolean isRightChild() {
		if (parent == null) return false;
		return parent.rightChild == this;
	}
	
	
	public boolean isTreeless() {
		if (this.parent == null && this.getType() != Type.Sentinel) return true;
		return false;
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
		if (this.getType().canHaveChildren()) {
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
		if (this.getType().canHaveChildren()) {
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
	
	public abstract Arc getArc(double sweeplineY, double siteX);
	
	/*
	 * Removes all connections from this node to other nodes.
	 * Throws an error if this node is still the child of its
	 * parent. Additionally, an error is thrown if the subtree
	 * represented by this node has not been replaced in the 
	 * predecessor/successor linked list.
	 */
	protected void ensureDisconnected() {
		if (this.isLeftChild()) throw new RuntimeException("The disconnected node is still attached! Parent's left child");
		if (this.isRightChild()) throw new RuntimeException("The disconnected node is still attached! Parent's right child");
		if (this.rootParent != null && this.rootParent.root == this) throw new RuntimeException("The disconnected node is still attached! Root node");
		this.parent = null;
		
		TreeNode first = this.getFirstDescendant();		
		if (first.getPredecessor() != null && first.getPredecessor().getSuccessor() == first) {
			throw new RuntimeException("The disconnected node is still attached! First descendant still connected to predecessor");
		}
		first.predecessor = null;
		
		TreeNode last = this.getLastDescendant();		
		if (last.getSuccessor() != null && last.getSuccessor().getPredecessor() == last) {
			throw new RuntimeException("The disconnected node is still attached! Last descendant still connected to successor");
		}
		last.successor = null;
	}

	@Override
	public String toString() {
		if (getType() == Type.Breakpoint) {
			Breakpoint bp = (Breakpoint) this;
			return "Breakpoint["+(name != null ? "Name='"+name+"', " : "")+"ID="+id+", LeftArc="+bp.arcLeft+", RightArc="+bp.arcRight+", Children:[Left="+leftChild.id+", Right="+rightChild.id+"]]";
		} else {
			Arc arc = (Arc) this;
			return "Arc["+(name != null ? "Name='"+name+"', " : "")+"ID="+id+", Site="+arc.site.id+", CircleEvent="+(arc.circleEvent!=null)+"]";
		}
	}
	
	protected void setName(String name) {
		this.name = name;
	}
	
}
