package dev.mortus.test;

public abstract class LinkedBinaryNode {
	
	public static int IDCounter = 0;

	private LinkedBinaryTree rootParent;
	private LinkedBinaryNode parent;
	private LinkedBinaryNode leftChild, rightChild;
	private LinkedBinaryNode predecessor, successor;
	
	public final int debugID;
	protected String debugName;

	public LinkedBinaryNode() {
		this.debugID = IDCounter++;
	}

	protected LinkedBinaryNode(LinkedBinaryTree rootParent) {
		this();
		this.rootParent = rootParent;
	}
	
	
	/**
	 * Returns this node's parent if hasParent() would return true, otherwise returns null.
	 * @see hasParent
	 */
	public LinkedBinaryNode getParent() {
		return parent;
	}
	
	/**
	 * Returns true if this node has a parent. Note that root nodes, while
	 * attached to a Tree, are not considered to have a parent. Use isRoot()
	 * to check if the node is a root (connected to a Tree object).
	 * @see isRoot
	 */
	public boolean hasParent() {
		return this.getParent() != null;
	}
	
	/**
	 * Returns true if this node is connected directly to a LinkedBinaryTree object.
	 * It is possible for a root node, by normal definition, to not be connected
	 * to a Tree object. This is referred to as floating root. In this case, 
	 * the hasParent() and isFloating() methods are sufficient for locating floating roots.
	 * @see hasParent
	 */
	protected boolean isRoot() {
		return rootParent != null;
	}
	
	/**
	 * When called from a root node (isRoot() == true), the provided node will replace this node
	 * as the new root. This node will become a floating node along with its descendants.
	 * @throws RuntimeException if this node is not the root
	 * @param newRoot
	 */
	public void promoteToRoot(LinkedBinaryNode newRoot) {
		if (!this.isRoot()) throw new RuntimeException("Cannot use promoteToRoot on a non-root node. see: isRoot");
		this.rootParent.root = newRoot;
		newRoot.rootParent = this.rootParent;
		this.rootParent = null;
	}
	
	/**
	 * Traverses this nodes ancestors until the root node is located. If this tree isFloating 
	 * (the root is not attached to a Tree object) then null will be returned.
	 * @return
	 */
	public LinkedBinaryNode getRoot() {
		if (this.isRoot()) return this;
		if (this.getParent() != null) return this.getParent().getRoot();
		return null;
	}
	
	/**
	 * Returns true if the root node of the tree that this node belongs to is not connected to a Tree object.
	 * @return
	 */
	public boolean isFloating() {
		if (getRoot() == null) return true;
		return false;
	}
	
	/**
	 * Returns true if this node has either a right or left child.
	 * @return
	 */
	public boolean hasChildren() {
		return hasLeftChild() || hasRightChild();
	}
	
	/**
	 * Returns this node's left child, or null if it does not have one.
	 * @return
	 */
	public LinkedBinaryNode getLeftChild() {
		return leftChild;
	}
	
	/**
	 * Connects a Node or entire subtree as the left child to this node. 
	 * IMPORTANT: the left parameter must refer to a floating root node, 
	 * a node that has no parent and is not connected to a Tree object.
	 * @param left - a floating root node
	 * @throws RuntimeException if the child to be added is not a floating root.
	 */
	protected void setLeftChild(LinkedBinaryNode left) {
		if (left == null) {
			removeLeftChild();
			return;
		}
		
		if (!left.isFloating() || left.hasParent()) {
			throw new RuntimeException("A node may only be added as a child if it is a floating root. This means"
					+ "that the node has no parents and is not connected to a Tree object. Use remove() to"
					+ "remove a node from its current tree and ensure that it is floating.");
		}
		
		//IMPORTANT! this.predecessor must refer to a node that is not removed from the tree
		removeLeftChild();
		
		LinkedBinaryNode first = left.getFirstDescendant();
		LinkedBinaryNode last = left.getLastDescendant();
		
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
		LinkedBinaryNode first = this.leftChild.getFirstDescendant();
		
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

	public boolean hasLeftChild() {
		return this.leftChild != null;
	}
	

	
	
	public LinkedBinaryNode getRightChild() {
		return rightChild;
	}
	
	/**
	 * Connects a Node or entire subtree as the right child to this node. 
	 * IMPORTANT: the right parameter must refer to a floating root node, 
	 * a node that has no parent and is not connected to a Tree object.
	 * @param right - a floating root node
	 * @throws RuntimeException if the child to be added is not a floating root.
	 */	
	protected void setRightChild(LinkedBinaryNode right) {
		if (right == null) {
			removeRightChild();
			return;
		}
		
		if (!right.isFloating() || right.hasParent()) {
			throw new RuntimeException("A node may only be added as a child if it is a floating root. This means"
					+ "that the node has no parents and is not connected to a Tree object. Use remove() to"
					+ "remove a node from its current tree and ensure that it is floating.");
		}
		
		//IMPORTANT! this.predecessor must refer to a node that is not removed from the tree
		removeRightChild();
		
		LinkedBinaryNode first = right.getFirstDescendant();
		LinkedBinaryNode last = right.getLastDescendant();
		
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
		LinkedBinaryNode last = this.rightChild.getLastDescendant();
		
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

	public boolean hasRightChild() {
		return this.rightChild != null;
	}

	
	
	/**
	 * Replaces this node (and its children) with the provided node.
	 * The node being replaced must have a parent or be the root of a tree.
	 * @param node
	 * @return
	 */
	public void replaceWith(LinkedBinaryNode node) {
		if (this.isLeftChild()) {
			this.getParent().setLeftChild(node);
		} else if (this.isRightChild()) {
			this.getParent().setRightChild(node);
		} else if (this.isRoot()) {
			this.promoteToRoot(node);
		} else throw new RuntimeException("Replaced node has no valid connection to a parent or tree. Floating?");
	}
	
	
	/**
	 * Returns the first descendant of this tree node.
	 * If this node has no left descendants then the first
	 * "descendant" is itself. 
	 * Similar to first item in a doubly linked list when called
	 * from the root node of a tree.
	 */
	public LinkedBinaryNode getFirstDescendant() {
		LinkedBinaryNode n = this;
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
	public LinkedBinaryNode getLastDescendant() {
		LinkedBinaryNode n = this;
		while (n.rightChild != null) n = n.rightChild;
		return n;
	}

	public LinkedBinaryNode getPredecessor() {
		return predecessor;
	}
	
	public LinkedBinaryNode getSuccessor() {
		return successor;
	}
	
	protected void setName(String name) {
		this.debugName = name;
	}
	
	@Override
	public String toString() {
		if (debugName == null) return "TreeNode[]";
		return "TreeNode[Name='"+debugName+"']";
	}

	/**
	 * Finds the successor by traversing the tree instead of referencing
	 * the saved node variable. Used internally for newly inserted nodes.
	 * @return
	 */
	public LinkedBinaryNode debugFindSuccessor() {
		if (this.hasRightChild()) {
			return this.getRightChild().getFirstDescendant();
		} else {
			LinkedBinaryNode n = this;
			while (n != null && !n.isLeftChild()) n = n.parent;
			if (n != null) return n.parent;
		}
		return null;
	}
	
	/**
	 * Finds the predecessor by traversing the tree instead of referencing
	 * the saved node variable. Used internally for newly inserted nodes.
	 * @return
	 */
	public LinkedBinaryNode debugFindPredecessor() {
		if (this.hasLeftChild()) {
			return this.getLeftChild().getLastDescendant();
		} else {
			LinkedBinaryNode n = this;
			while (n != null && !n.isRightChild()) n = n.parent;
			if (n != null) return n.parent;
		}
		return null;
	}
	
}
