package dev.mortus.voronoi.list;

public class TreeList<K, T extends Comparable<K>> {

	boolean autoRebalance; 	// Should add or delete cause a re-balance
	boolean specialLeaves; 	// Should re-balancing preserve whether a node is internal or a leaf?
	
	Node<K,T> root;
	
	public TreeList(boolean autoRebalance, boolean specialLeaves) {
		root = new Node<K,T>(this);
	}
	
	
	
	public static class Node<K, T extends Comparable<K>> {
		
		TreeList<K,T> tree;
		
		Node<K,T> parent;
		Node<K,T> prev;
		Node<K,T> next;
		
		T value;
		
		int weight;		// number of child nodes + 1 (self)
		int leafCount;  // number of descendant leaf nodes or 1 (self)
		int height;		// 0 means leaf, 1 means direct parent of leaf... etc.
		
		Node<K,T> leftChild;
		Node<K,T> rightChild;
				
		private Node(TreeList<K,T> tree) {
			this.tree = tree;
			
			this.parent = null;
			this.prev = null;
			this.next = null;

			this.value = null;
			
			this.weight = 0;
			this.leafCount = 0;
			this.height = 0;
		}
		
		private Node(Node<K, T> parent) {
			this(parent.tree);
			this.parent = parent;
		}

		public Node(Node<K, T> parent, T value) {
			this(parent);
			this.value = value;
			this.weight = 1;
			this.leafCount = 1;
		}
		
		

		/**
		 * Returns the node with the exact location given. If no node exists
		 * with the exact given location then null is returned.<br><br>
		 * 
		 * Technically speaking, the value of each node is compared with the
		 * location provided and the first node arrived at for which this 
		 * comparison is equal to 0 will be returned. If there is more than
		 * one node at the given location the returned node may be any one
		 * of them.
		 * 
		 * @param location the location that will be compared with each node's value
		 */
		public Node<K,T> getNode(K location) {
			if (value == null) return null;
			int compare = value.compareTo(location);
			
			if (compare == 0) return this;
			if (!this.hasChildren) return null;
				
			if (compare < 0) {
				if (rightChild == null) return null;
				return rightChild.getNode(location);
			} else {
				if (leftChild == null) return null;
				return leftChild.getNode(location);
			}
		}
		
		/**
		 * Returns the leaf node arrived at by traversing all internal nodes
		 * such that the location lies on the selected side of each subtree.<br><br>
		 * 
		 * If a leaf node does not exist, for example when an internal node has
		 * only one subtree but it is on the wrong side, then null will returned.
		 * In this case, use getInternal() to get the parent of the desired but
		 * non-existent lead node.
		 * 
		 * @see getInternal
		 * @param location the location that will be compared with each node's value
		 */
		public Node<K,T> getLeaf(K location) {
			if (!this.hasChildren) return this;
			
			int compare = value.compareTo(location);
			if (compare < 0) {
				if (rightChild == null) return null;
				return rightChild.getLeaf(location);
			} else {
				if (leftChild == null) return null;
				return leftChild.getLeaf(location);
			}
		}
		
		/**
		 * Returns the internal node that is a parent to the leaf node that would
		 * be arrived at by traversing all internal nodes such that the location 
		 * lies on the selected side of each subtree. The parent node will be the
		 * last internal node that would be compared in such a process. <br><br>
		 * 
		 * This method is intended to be used with getLeaf() in the case that a leaf
		 * node did not exist. In such a case, access to the parent of the desired
		 * non-existent leaf is necessary in order to create the desired leaf.
		 * 
		 * @see getLeaf
		 * @param location the location that will be compared with each node's value
		 */
		public Node<K,T> getInternal(K location) {
			return getInternalPrivate(location, null);
		}
		
		private Node<K,T> getInternalPrivate(K location, Node<K,T> parent) {
			if (!this.hasChildren) return parent;
			
			int compare = value.compareTo(location);
			if (compare < 0) {
				if (rightChild == null) return parent;
				return rightChild.getInternalPrivate(location, this);
			} else {
				if (leftChild == null) return parent;
				return leftChild.getInternalPrivate(location, this);
			}
		}
		
		
		
		public void add(K location, T value) {
			// Only for the first node
			if (this.value == null) {
				this.value = value;
				this.weight = 1;
				this.leafCount = 1;
				return;
			}
			
			int compare = this.value.compareTo(location);
			
			if (compare < 0) {
				if (rightChild == null) addRight(value);
				else rightChild.add(location, value);
			} else {
				if (leftChild == null) {
					addLeft(value);
					doRebalance();
					return;
				}
				leftChild.add(location, value);
			}
		}


		
		private void doRebalance() {
			if (!tree.autoRebalance) return;
			// TODO
		}

		private void addLeft(T value) {
			leftChild = new Node<K,T>(this, value);
			Node<K,T> prev = this.prev;
			Node<K,T> next = this;
			leftChild.linkPrev(prev);
			leftChild.linkNext(next);

			this.childAdded();
		}

		private void addRight(T value2) {
			rightChild = new Node<K,T>(this, value);
			Node<K,T> prev = this;
			Node<K,T> next = this.next;
			leftChild.linkPrev(prev);
			leftChild.linkNext(next);
			
			this.childAdded();
		}
		
		private void childAdded() {			
			// Leaf count should only increase if this node
			// was already an internal node. Otherwise it lost
			// its leaf status when the new child was added.
			if (this.hasChildren) this.leafCount++;
			
			this.hasChildren = true;
			this.weight++;
		}
		
		private void linkPrev(Node<K, T> node) {
			this.prev = node;
			node.next = this;
		}

		private void linkNext(Node<K, T> node) {
			this.next = node;
			node.prev = this;
		}
		
		// deep copy
		private Node<K,T> copy() {
			Node<K,T> node = new Node<K,T>(this.parent, this.value);
			node.prev = this.prev;
			node.next = this.next;
			node.weight = this.weight;
			node.leafCount = this.leafCount;
			
			if (this.hasChildren) {
				node.hasChildren = this.hasChildren;
				if (this.leftChild != null) node.leftChild = this.leftChild.copy();
				if (this.rightChild != null) node.rightChild = this.rightChild.copy();
			}
			
			return node;
		}
	}
	
}
