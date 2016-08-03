package list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.tree.TreeCellEditor;

/**
 * TreeList is a linked-list and tree structure combined.
 * The linked list maintained between nodes allows constant time forward
 * and backward traversal of the items while the tree structure allows 
 * for faster searching and therefore fasted sorted insertion of elements.
 * 
 * 
 * Generic type variables:
 *   K is the class of values that will be inserted into the tree
 *   T is the class of locations that will be used to store those values
 *     the location class T must be comparable with the value class K so that
 *     newly inserted values can be placed with reference to the existing nodes.
 *     
 * The reason for these types being separate instead of the alternative:
 * 		TreeList<K extends Comparable<K>>
 * 
 * is that this data structure was designed to store the parabolas of a shore line in 
 * Fortune's algorithm for the generation of Voronoi diagrams. In this particular use 
 * case, items being added to the tree always have a specific location but older parabolas
 * will have expanded to cover a range of values. 
 * 
 * By allowing the the location class to be separate from the value class, the location 
 * class can be a Double value, while the value class can be parabola segements for which 
 * the end-points define a range of values. 
 * 
 * @author Gregary
 *
 * @param <K>
 * @param <T>
 */
public class TreeList<K, T extends Comparable<K>> implements List<T> {

	boolean autoRebalance; 	// Should the tree auto re-balance when modified
	
	Node<K,T> root;

	public TreeList() {
		this(true);
	}
	
	public TreeList(boolean autoRebalance) {
		this.autoRebalance = autoRebalance;
		root = new Node<K,T>(this);
	}
	
	public Node<K,T> getRoot() {
		return root;
	}
	
	/**
	 * Returns the node with the given index in this Tree-List.
	 * 
	 * @param index of the node to look for
	 * @return the node if found, or null
	 * @throws IndexOutOFBoundsException if a node with the given index is not found
	 */
	public Node<K,T> getNodeByIndex(int index) {
		Node<K, T> search = root;
		while (search != null) {
			int searchIndex = search.getIndex();
			if (searchIndex > index) {
				search = search.leftChild;	
			} else if (searchIndex < index) {
				search = search.rightChild;
			} else { 
				//Found node with matching index
				return search;
			}
		}
		throw new IndexOutOfBoundsException();
	}
	
	public static class Node<K, T extends Comparable<K>> {
		
		TreeList<K,T> tree;
		
		Node<K,T> parent;
		Node<K,T> prev;
		Node<K,T> next;
		
		T value;
		
		int weight;		// number of child nodes + 1 (self)
		int leafCount;  // number of descendant leaf nodes or 1 (self)
		int height;		// 1 means leaf, 2 means direct parent of leaf... etc.
		
		Node<K,T> leftChild;
		Node<K,T> rightChild;
				
		private Node(TreeList<K,T> tree) {
			this.tree = tree;
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
			this.height = 1;
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
		 * @return the node if found, or null
		 */
		public Node<K,T> getNode(K location) {
			if (value == null) return null;
			int compare = value.compareTo(location);
			
			if (compare == 0) return this;
			if (!this.hasChildren()) return null;
				
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
		 * @return the node if found, or null
		 */
		public Node<K,T> getLeaf(K location) {
			if (!this.hasChildren()) return this;
			
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
		 * @return the node if found, or null
		 */
		public Node<K,T> getInternal(K location) {
			return getInternalPrivate(location, null);
		}
		
		private Node<K,T> getInternalPrivate(K location, Node<K,T> parent) {
			if (!this.hasChildren()) return parent;
			
			int compare = value.compareTo(location);
			if (compare < 0) {
				if (rightChild == null) return parent;
				return rightChild.getInternalPrivate(location, this);
			} else {
				if (leftChild == null) return parent;
				return leftChild.getInternalPrivate(location, this);
			}
		}
		
		
		
		public boolean hasChildren() {
			return this.height > 1;
		}

		public int getIndex() {
			// Node has parent and is left child:
			// Index = (Index of parent) - 1 - (Weight of node's right child)
			if (this.isLeftChild()) {
				int index = parent.getIndex()-1;
				if (rightChild != null) index -= rightChild.weight;
				return index; 
			}

			// Node has parent and is left child:
			// Index = (Index of parent) + 1 + (Weight of node's left child)
			if (this.isRightChild()) {
				int index = parent.getIndex()+1;
				if (leftChild != null) index += leftChild.weight;
				return index; 
			}
			
			// No parent: mass of left child
			if (leftChild != null) return leftChild.weight;
			return 0;
		}

		public void add(K location, T value) {
			// Only for the first node
			if (this.value == null) {
				this.value = value;
				this.weight = 1;
				this.leafCount = 1;
				this.height = 1;
				return;
			}
			
			int compare = this.value.compareTo(location);
			
			if (compare < 0) {
				System.out.println("> right of "+this.value);
				if (rightChild == null) addRight(value);
				else rightChild.add(location, value);
			} else {
				System.out.println("> left of "+this.value);
				if (leftChild == null) addLeft(value);
				else leftChild.add(location, value);
			}

			update();
			if (this.parent == null) {
				System.out.println();
				print();
			}
			
			if (tree.autoRebalance) rebalance();
		}


		
		private boolean isLeftChild() {
			if (this.parent == null) return false;
			if (this == parent.leftChild) return true;
			return false;
		}
				
		private boolean isRightChild() {
			if (this.parent == null) return false;
			if (this == parent.rightChild) return true;
			return false;
		}
		
		/**
		 * updates the mass and height of the current node
		 * and re-balances if enabled by this node's tree.
		 */
		private void update() {
			this.weight = 1;
			this.leafCount = 0;
			this.height = 1;
			
			// weight, leafCount, and height determined from children (or lack thereof)
			if (this.leftChild != null || this.rightChild != null) {
				if (this.leftChild != null) {
					this.weight += leftChild.weight;
					this.leafCount += leftChild.leafCount;
					if (leftChild.height+1 > this.height) this.height = leftChild.height+1;
				}
				if (this.rightChild != null) {
					this.weight += rightChild.weight;
					this.leafCount += rightChild.leafCount;
					if (rightChild.height+1 > this.height) this.height = rightChild.height+1;
				}
			} else {
				this.leafCount = 1;
			}
		}

		/**
		 *  Re-balances this tree and returns the new top level node. 
		 *  Assumes all child heights are accurate.
		 */
		private Node<K,T> rebalance() {
			// insure children are balanced
			if (this.leftChild != null) this.leftChild = leftChild.rebalance();
			if (this.rightChild != null) this.rightChild = rightChild.rebalance();
			
			int balance = getBalance();
			
			// balance is good
			if (balance >= -1 && balance <= 1) return this;
			
			// left side is smaller
			if (balance < -1) {
				if (rightChild.getBalance() > 0) {
					this.rightChild = rightChild.rotateRight();
				}
				return this.rotateLeft();
			}
			
			// right side is smaller
			if (balance > 1) {
				if (leftChild.getBalance() < 0) {
					this.leftChild = leftChild.rotateLeft();
				}
				return this.rotateRight();
			}
			
			throw new RuntimeException("impossible condition");
		}
		
		private int getBalance() {
			int balance = 0;
			if (leftChild != null) balance = leftChild.height;
			if (rightChild != null) balance -= rightChild.height;
			return balance;
		}
		
		/**
		 * Left rotates this node (A) and returns the new parent (C).
		 * <pre>
		 *      A             C
		 *     / \           / \
		 *    B   C   -->   A   E
		 *       / \       / \
		 *      D   E     B   D	      
		 * </pre>
		 * Weights, heights, and leaf counts are updated. The parent
		 * of this node will be modified to hold the new top level node.
		 */
		private Node<K,T> rotateLeft() {
			Node<K,T> A = this;
			Node<K,T> C = this.rightChild;
			Node<K,T> D = this.rightChild.leftChild;

			System.out.println("rotate left "+this.value);
			
			// The order of the following sections matters!
			
			// 1. A gets a new right child
			A.rightChild = D;
			if (D != null) D.parent = A;
			A.update();
			
			// 2. new top level node with parent same as old top level node
			C.parent = A.parent;
			
			// 3. C is now the parent of A
			C.leftChild = A;
			A.parent = C;
			C.update();
			
			// 4. fix parent child relationship caused by new top level node
			Node<K,T> X = C.parent;
			if (X != null) {
				if (X.leftChild == A) X.leftChild = C;
				if (X.rightChild == A) X.rightChild = C;
			} else if (tree.root == A) tree.root = C;

			tree.root.print();
			
			return C;
		}
		
		/**
		 * Right rotates this node (A) and returns the new parent (B).
		 * <pre>
		 *      A           B
		 *     / \         / \
		 *    B   C  -->  D   A
		 *   / \             / \
		 *  D   E           E   C	      
		 * </pre>
		 * Weights, heights, and leaf counts are updated. The parent
		 * of this node will be modified to hold the new top level node.
		 */
		private Node<K,T> rotateRight() {
			Node<K,T> A = this;
			Node<K,T> B = this.leftChild;
			Node<K,T> E = this.leftChild.rightChild;

			System.out.println("rotate right "+this.value);
			
			// The order of the following sections matters!

			// 1. A gets a new left child
			A.leftChild = E;
			if (E != null) E.parent = A;
			A.update();

			// 2. new top level node with parent same as old top level node
			B.parent = A.parent;

			// 3. B is now the parent of A
			B.rightChild = A;
			A.parent = B;
			B.update();
			
			// 4. fix parent child relationship caused by new top level node
			Node<K,T> X = B.parent;
			if (X != null) {
				if (X.leftChild == A) X.leftChild = B;
				if (X.rightChild == A) X.rightChild = B;
			} else if (tree.root == A) tree.root = B;

			tree.root.print();
			
			return B;
		}
		
		/**
		 * sets the left child and appropriate other info
		 * an update() will be required
		 */
		private void addLeft(T value) {
			leftChild = new Node<K,T>(this, value);
			Node<K,T> prev = this.prev;
			Node<K,T> next = this;
			leftChild.linkPrev(prev);
			leftChild.linkNext(next);
		}

		/**
		 * sets the right child and appropriate other info
		 * an update() will be required
		 */
		private void addRight(T value) {
			rightChild = new Node<K,T>(this, value);
			Node<K,T> prev = this;
			Node<K,T> next = this.next;
			rightChild.linkPrev(prev);
			rightChild.linkNext(next);
		}
		
		/**
		 * set this node's previous link and links the other node back
		 */
		private void linkPrev(Node<K, T> node) {
			this.prev = node;
			if (node != null) node.next = this;
		}

		/**
		 * set this node's next link and links the other node back
		 */
		private void linkNext(Node<K, T> node) {
			this.next = node;
			if (node != null) node.prev = this;
		}
		
		/**
		 * deep copy
		 */
		private Node<K,T> deepCopy() {
			Node<K,T> node = new Node<K,T>(this.parent, this.value);
			node.prev = this.prev;
			node.next = this.next;
			node.weight = this.weight;
			node.leafCount = this.leafCount;
			node.height = this.height;
			
			if (this.hasChildren()) {
				if (this.leftChild != null) node.leftChild = this.leftChild.deepCopy();
				if (this.rightChild != null) node.rightChild = this.rightChild.deepCopy();
			}
			
			return node;
		}
		
		
		
		public void print() {
			print("", "root: ");
		}
		
		private void print(String pre, String s) {
			T prevValue = null;
			T nextValue = null;
			
			if (this.prev != null) prevValue = this.prev.value;
			if (this.next != null) nextValue = this.next.value;
			
			System.out.println(pre+s+value+"  \t  ("+this.weight+","+this.height+","+this.leafCount+")  \t  [..., "+prevValue+", "+this.value+", "+nextValue+", ...] " + this.getBalance());
			
			if (this.hasChildren()) {
				if (leftChild != null) leftChild.print(pre+"  ", "L: ");
				if (rightChild != null) rightChild.print(pre+"  ", "R: ");
			}
		}
		
	}

	@Override
	public boolean add(T arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void add(int arg0, T arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T get(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int indexOf(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int lastIndexOf(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<T> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<T> listIterator(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T remove(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T set(int arg0, T arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<T> subList(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
}