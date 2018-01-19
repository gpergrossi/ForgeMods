package com.gpergrossi.util.data.btree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;

public class RedBlackTree<Key, Value> implements NavigableMap<Key, Value> {
		
	private final Comparator<Key> comparator;
	private int size;
	private Node root;

	public RedBlackTree() {
		this.comparator = null;
	}
	
	public RedBlackTree(Comparator<Key> comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public Comparator<? super Key> comparator() {
		return comparator;
	}

	@SuppressWarnings("unchecked")
	private final int compare(Key a, Key b) {
		// These comparisons exist for the closestNode() method,
		// providing a valid comparison with null predecessors/successors
		// Ordinarily, null key values are NOT allowed.
		if (a == null) return Integer.MIN_VALUE;
		if (b == null) return Integer.MAX_VALUE;
		
		Comparator<? super Key> comparator = comparator();
		if (comparator != null) {
			return comparator.compare(a, b);
		} else {
			return ((Comparable<Key>) a).compareTo(b);
		}
	}
	
	@Override
	public Key firstKey() {
		Entry<Key, Value> entry = firstEntry();
		if (entry == null) return null;
		return entry.getKey();
	}

	@Override
	public Key lastKey() {
		Entry<Key, Value> entry = lastEntry();
		if (entry == null) return null;
		return entry.getKey();
	}

	@Override
	public Set<Key> keySet() {
		return navigableKeySet();
	}

	@Override
	public Collection<Value> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Entry<Key, Value>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return (size == 0);
	}

	@Override
	public boolean containsKey(Object key) {
		@SuppressWarnings("unchecked")
		Key castKey = (Key) key;
		return (findNode(castKey) != null);
	}

	@Override
	public boolean containsValue(Object value) {
		for (Value v : values()) {
			if (v == null) {
				if (value == null) return true;
			} else if (v.equals(value)) return true;
		}
		return false;
	}

	@Override
	public Value get(Object key) {
		@SuppressWarnings("unchecked")
		Key castKey = (Key) key;
		Node node = findNode(castKey);
		if (node == null) return null;
		return node.value;
	}

	@Override
	public Value put(Key key, Value value) {
		if (key == null) throw new NullPointerException();		
		Node node = new Node(key, value);
		if (root == null) {
			root = node;
		} else {
			Node parent = findParent(key);
			int compare = compare(key, parent.key);
			if (compare < 0) {
				parent.setLeftChild(node);
			} else if (compare > 0) {
				parent.setRightChild(node);
			} else {
				Value oldValue = parent.value;
				parent.value = value;
				return oldValue;
			}
		}
		insertRepairTree(node);
		size++;
		return null;
	}

	@Override
	public Value remove(Object key) {
		@SuppressWarnings("unchecked")
		Key castKey = (Key) key;
		Node node = findNode(castKey);
		if (node == null) return null;
		Value value = node.value;
		removeNode(node);
		return value;
	}

	@Override
	public void putAll(Map<? extends Key, ? extends Value> m) {
		for (Entry<? extends Key, ? extends Value> entry : m.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		this.root = null;
		this.size = 0;
	}
	
	@Override
	public Entry<Key, Value> lowerEntry(Key key) {
		Node node = floorNode(key);
		if (node == null) return null;
		if (compare(key, node.key) == 0) return node.getPredecessor().getEntry();
		return node.getEntry();
	}

	@Override
	public Key lowerKey(Key key) {
		Entry<Key, Value> entry = lowerEntry(key);
		if (entry == null) return null;
		return entry.getKey();
	}

	@Override
	public Entry<Key, Value> floorEntry(Key key) {
		Node node = floorNode(key);
		if (node == null) return null;
		return node.getEntry();
	}

	@Override
	public Key floorKey(Key key) {
		Entry<Key, Value> entry = floorEntry(key);
		if (entry == null) return null;
		return entry.getKey();
	}

	@Override
	public Entry<Key, Value> ceilingEntry(Key key) {
		Node node = ceilNode(key);
		if (node == null) return null;
		return node.getEntry();
	}

	@Override
	public Key ceilingKey(Key key) {
		Entry<Key, Value> entry = ceilingEntry(key);
		if (entry == null) return null;
		return entry.getKey();
	}

	@Override
	public Entry<Key, Value> higherEntry(Key key) {
		Node node = ceilNode(key);
		if (node == null) return null;
		if (compare(key, node.key) == 0) return node.getSuccessor().getEntry();
		return node.getEntry();
	}

	@Override
	public Key higherKey(Key key) {
		Entry<Key, Value> entry = higherEntry(key);
		if (entry == null) return null;
		return entry.getKey();
	}

	@Override
	public Entry<Key, Value> firstEntry() {
		if (root == null) return null;
		return root.getFirstDescendant().getEntry();
	}

	@Override
	public Entry<Key, Value> lastEntry() {
		if (root == null) return null;
		return root.getLastDescendant().getEntry();
	}

	@Override
	public Entry<Key, Value> pollFirstEntry() {
		if (root == null) return null;
		Node first = root.getFirstDescendant();
		Entry<Key, Value> entry = first.getEntry();
		removeNode(first);
		return entry;
	}

	@Override
	public Entry<Key, Value> pollLastEntry() {
		if (root == null) return null;
		Node last = root.getLastDescendant();
		Entry<Key, Value> entry = last.getEntry();
		removeNode(last);
		return entry;
	}

	@Override
	public NavigableMap<Key, Value> descendingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<Key> navigableKeySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<Key> descendingKeySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableMap<Key, Value> subMap(Key fromKey, boolean fromInclusive, Key toKey, boolean toInclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableMap<Key, Value> headMap(Key toKey, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableMap<Key, Value> tailMap(Key fromKey, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedMap<Key, Value> subMap(Key fromKey, Key toKey) {
		return subMap(fromKey, true, toKey, false);
	}

	@Override
	public SortedMap<Key, Value> headMap(Key toKey) {
		return headMap(toKey, false);
	}

	@Override
	public SortedMap<Key, Value> tailMap(Key fromKey) {
		return tailMap(fromKey, true);
	}
	
	
	
	
	
	private static final boolean BLACK = false;
	private static final boolean RED = true;
	
	private final boolean isBlack(Node node) {
		return (node == null || node.color == BLACK);
	}
	
	private final boolean isRed(Node node) {
		return (node != null && node.color == RED);
	}
	
	private final class Node extends AbstractBinaryNode<Node> {
		private Key key;
		private Value value;
		private boolean color; // Red or black?
		
		public Node(Key key, Value value) {
			this.key = key;
			this.value = value;
			this.color = RED;
		}
		
		public Entry<Key, Value> getEntry() {
			return new NodeEntry(this);
		}
		
		@Override
		public void removeFromParent() {
			super.removeFromParent();
			if (this == root) root = null;
		}
		
		@Override
		public void replaceWith(Node child) {
			super.replaceWith(child);
			if (this == root) root = child;
		}

		@Override
		public String toString() {
			return key+":"+value+" ["+(color ? "red" : "black")+"]";
		}
	}
	
	private final class NodeEntry implements Entry<Key, Value> {
		private final Node node;
		private NodeEntry(Node node) {
			this.node = node;
		}
		
		@Override
		public Key getKey() {
			return node.key;
		}

		@Override
		public Value getValue() {
			return node.value;
		}

		@Override
		public Value setValue(Value value) {
			Value oldValue = node.value;
			node.value = value;
			return oldValue;
		}
		
		@Override
		public boolean equals(Object obj) {
			@SuppressWarnings("unchecked")
			NodeEntry entry = (NodeEntry) obj;
			return (this.node == entry.node);
		}
	}
	
	@Override
	public String toString() {
		return root.treeString();
	}
	
	public List<Entry<Key, Value>> removeRange(Key keyMin, Key keyMax) {
		List<Entry<Key, Value>> removed = new ArrayList<>();
		final Node start = ceilNode(keyMin);
		final Node end = floorNode(keyMax);
		
		Node scout = start;
		Node successor;
		boolean done = false;
		do {
			if (scout == end) done = true;
			successor = scout.getSuccessor();
			removeNode(scout);
			removed.add(scout.getEntry());
			scout = successor;
		} while (!done);
		
		return removed;
	}

	protected Node floorNode(Key lookup) {
		return findSurroundingNodes(lookup)[0];
	}
	
	protected Node ceilNode(Key lookup) {
		return findSurroundingNodes(lookup)[1];
	}
	
	protected Node closestNode(Key lookup) {
		Node[] surroundingNodes = findSurroundingNodes(lookup);
		int compareLow = compare(lookup, surroundingNodes[0].key);
		int compareHigh = compare(lookup, surroundingNodes[1].key);
		final Node node;
		if (Math.abs(compareLow) <= Math.abs(compareHigh)) {
			node = surroundingNodes[0];
		} else {
			node = surroundingNodes[1];
		}
		return node;
	}
	
	/**
	 * Returns an two-length array of the Nodes: {floorNode(lookup), ceilNode(lookup)}.
	 * @param lookup
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Node[] findSurroundingNodes(Key lookup) {
		Node parent = findParent(lookup);
		int compare = compare(lookup, parent.key);
		if (compare < 0) {
			return (Node[]) new Object[] {parent.getPredecessor(), parent};
		} else if (compare > 0) {
			return (Node[]) new Object[] {parent, parent.getSuccessor()};
		} else {
			return (Node[]) new Object[] {parent, parent};
		}
	}
	
	private Node findNode(Key key) {
		Node scout = findParent(key);
		if (compare(key, scout.key) == 0) return scout;
		return null;
	}
	
	private Node findParent(Key key) {
		Node scout = root;
		while (true) {
			int compare = compare(key, scout.key);
			if (compare < 0) {
				if (scout.hasLeftChild()) scout = scout.getLeftChild();
				else return scout;
			} else if (compare > 0) {
				if (scout.hasRightChild()) scout = scout.getRightChild();
				else return scout;
			} else {
				return scout;
			}
		}
	}
	
	/**
	 * Swaps the positions of two nodes within the same tree.
	 * Position includes parent (or root status), left and right children, and color of the node.
	 */
	private final void swap(Node a, Node b) {
		if (a == b) return;
		if (a.hasAncestor(b)) swap (b, a);
		if (a.getRoot() != b.getRoot()) throw new IllegalArgumentException("Nodes must be in same tree");

		System.out.println("Swapping "+a+" and "+b);
		System.out.println("Before:\n"+this.toString());

		// Remember all connections
		Node aLeft = a.getLeftChild();
		Node aRight = a.getRightChild();
		Node bLeft = b.getLeftChild();
		Node bRight = b.getRightChild();
		Node bParent = b.getParent();
		boolean bSideIsRight = b.isRightChild();
		
		// Remove children from both nodes
		if (bLeft != null) bLeft.removeFromParent();
		if (bRight != null) bRight.removeFromParent();		
		if (aLeft != null) aLeft.removeFromParent();
		if (aRight != null) aRight.removeFromParent();
		
		// Remove b from its parent, replace a with b
		b.removeFromParent();
		a.replaceWith(b); // replaceWith is overridden in Node to handle tree root
		
		// Reconnect b's old children to a
		a.setLeftChild(bLeft);
		a.setRightChild(bRight);
		
		// Reconnect a's old children to b
		// Special cases for when b is child of a
		if (aLeft != b) b.setLeftChild(aLeft);
		if (aRight != b) b.setRightChild(aRight);
		
		// Reconnect a to parent
		// Special case for when b is child of a
		if (bParent == a) bParent = b; 
		if (bSideIsRight) {
			bParent.setRightChild(a);
		} else {
			bParent.setLeftChild(a);
		}
		
		// Swap colors
		boolean aColor = a.color;
		a.color = b.color;
		b.color = aColor;
		System.out.println("After:\n"+this.toString());
	}
	
	private void removeNode(Node node) {
		if (node.getRoot() != this.root) throw new IllegalArgumentException("Node does not belong to tree!");
		
		// First we make sure the node being considered has at most one child:
		if (node.hasLeftChild() && node.hasRightChild()) {
			// If not, we swap with the successor and remove the successor instead
			// Normally, it would be acceptable and preferable to simply swap the 
			// values of the two nodes, however there are benefits to keeping the
			// value of node objects unchanged during tree balancing operations.
			swap(node, node.getSuccessor());
			// node is now in successor's place and visa versa.
		}
		
		Node child = node.getRightChild();
		if (child == null) child = node.getLeftChild();
		
		if (child == null) {
			if (node.color == BLACK) removeRepairTree(node);
			node.removeFromParent();
		} else {
			child.removeFromParent();
			node.replaceWith(child);
			if (node.color == BLACK) {
				if (child.color == RED) {
					child.color = BLACK;
				} else {
					removeRepairTree(child);
				}
			}
		}

		size--;
	}
	
	public void checkInvariants() {
		checkInvariants(root);
		int countedSize = checkCount(root);
		if (countedSize != size) throw new IllegalStateException("Size was expected to be "+size+", but was "+countedSize);
	}
	
	private void checkInvariants(Node node) {
		if (node == null) return;
		if (node.hasParent() && !node.isLeftChild() && !node.isRightChild()) throw new IllegalStateException("Node '"+node+"' is not the child of its parent");
		if (node.color == RED) {
			if (isRed(node.leftChild)) throw new IllegalStateException("Left child of red node '"+node+"' is red");
			if (isRed(node.rightChild)) throw new IllegalStateException("Right child of red node '"+node+"' is red");
		}		
		countBlackNodes(node);
		if (node.hasLeftChild() && node.getLeftChild().getParent() != node) throw new IllegalStateException("Left child of node '"+node+"', does not acknowledge its parent");
		if (node.hasRightChild() && node.getRightChild().getParent() != node) throw new IllegalStateException("Right child of node '"+node+"', does not acknowledge its parent");
		checkInvariants(node.leftChild);
		checkInvariants(node.rightChild);
	}
	
	private int countBlackNodes(Node node) {
		if (node == null) return 1;
		int left = countBlackNodes(node.leftChild);
		int right = countBlackNodes(node.rightChild);
		if (left != right) throw new IllegalStateException("Node '"+node+"' has unbalanced black nodes");
		return left + (node.color == BLACK ? 1 : 0);
	}

	private int checkCount(Node node) {
		if (node == null) return 0;
		int left = checkCount(node.leftChild);
		int right = checkCount(node.rightChild);
		return left + right + 1;
	}
	
	private void insertRepairTree(Node node) {
		while (true) {
			// Case 1) No parent: Set node black
			if (!node.hasParent()) {
				node.color = BLACK;
				return;
			}
			
			// Case 2) Parent is black: Do nothing, tree is still valid
			Node parent = node.getParent();
			if (isBlack(parent)) return;
			
			// Case 3) Parent is red, Uncle is red (not a null/leaf node, which would be considered black):
			Node uncle = parent.getSibling();
			Node grandparent = parent.getParent();
			if (isRed(uncle)) {
				// Flip colors of parent, grandparent, and uncle
				parent.color = BLACK;
				uncle.color = BLACK;
				grandparent.color = RED;
				
				// Re-evaluate insertRepairTree() on grandparent
				node = grandparent;
				continue;
			}
			
			// Case 4) Parent is red, Uncle is black (or null/leaf node, which is considered black):
			// There are four cases for rotation here, but 2 of them are symmetric cases of the other 2.
			// If the node being considered is on the inside of its grandparent's tree, it is first rotated to the outside.
			// Once the considered node is on the outside, its parent is rotated toward its grandparent, restoring balance.
			// Finally, the parent's and grandparent's colors are set.
			if (parent.isLeftChild()) {
				if (node.isRightChild()) {
					parent = parent.rotateLeft();
					node = node.leftChild;
				}
				grandparent.rotateRight();
			} else {
				if (node.isLeftChild()) {
					parent = parent.rotateRight();
					node = node.rightChild;
				}
				grandparent.rotateLeft();
			}				
			parent.color = BLACK;
			grandparent.color = RED;
			return;
		}
	}

	/**
	 * By passing a node to this method, it is assumed that
	 * the tree above the provided node now has one-too-few black 
	 * nodes on the branch to which the provided node belongs.
	 * This method uses specific rotations and re-colorings to re-balance
	 * the tree and restore the invariants, making the assumption that the 
	 * branch containing the node provided is now one black node short.
	 * The provided node's sub-tree already meets all red/black tree conditions.
	 * @param node
	 */
	private void removeRepairTree(Node node) {
		while (true) {
			if (!isBlack(node)) throw new IllegalStateException("Node for removeRepairTree() must be black.");
			
			// === Case 1 ===
			// Condition: Node is root.
			// Operation: Do nothing, tree is already balanced
			if (!node.hasParent()) return;
			
			
			
			// === Case 2 ===
			// Preconditions: Node is black.
			// Condition: Sibling is red, therefore Parent is black.
			// Operation: Parent's and Sibling's colors are swapped, Parent
			//   is rotated so that Sibling becomes Grandparent of Node.
			// Effects: This operation does not affect the number of black nodes on any branch.
			//   It does, however, re-arrange the tree into one of the cases: 4, 5, or 6.
			//
			//  ________________Transformation Diagram_______________
			// |                          |                          |
			// |         [P]              |              [S]         |
			// |      ,-'   `-,           |           ,-'   '-,      |
			// |   [N]         (S)        |        (P)         [R]   |
			// |   / \        /   \     --+->     /   \        / \   |
			// | {1} {2}   [L]     [R]    |    [N]     [L]   {5} {6} |
			// |           / \     / \    |    / \     / \           |
			// |         {3} {4} {5} {6}  |  {1} {2} {3} {4}         |
			// |__________________________|__________________________|
			// 	(N) = red, [N] = black, {#} = balanced subtree
			//
			Node parent = node.getParent();
			Node sibling = node.getSibling();
			if (isRed(sibling)) {
				parent.color = RED;
				sibling.color = BLACK;
				if (node.isLeftChild()) {
					parent.rotateLeft();
				} else {
					parent.rotateRight();
				}
				
				// Node's Sibling is now what used to be Sibling's left child
				// which must be black because it was a child of a red node
				sibling = node.getSibling();
			}
			
			
			
			// === Case 3 ===
			// Preconditions: Node is black, Sibling is black.
			// Condition: Both of Sibling's children are black, Parent is black.
			// Operation: Change Sibling's color to red.
			// Effects: Following this operation, the number of black nodes in any path passing through
			//   Sibling has been reduced by one, which restores balance with paths passing through Node.
			//   Now all paths passing through Parent have been reduced by one black node and the repair
			//   procedure must continue upward starting from Parent.
			//
			//  ________________Transformation Diagram________________
			// |                          |                           |
			// |         [P]              |           [P]             |
			// |      ,-'   `-,           |        ,-'   '-,          |
			// |   [N]         [S]        |     [N]         (S)       |
			// |   / \        /   \     --+->   / \        /   \      |
			// | {1} {2}   [L]     [R]    |   {1} {2}   [L]     [R]   |
			// |           / \     / \    |             / \     / \   |
			// |         {3} {4} {5} {6}  |           {3} {4} {5} {6} |
			// |__________________________|___________________________|
			// 	(N) = red, [N] = black, {#} = balanced subtree
			//
			if (isBlack(parent) && (sibling == null || (isBlack(sibling.leftChild) && isBlack(sibling.rightChild)))) {
				if (sibling != null) sibling.color = RED;
				node = parent;
				continue;
			}
		
			
			
			// === Case 4 ===
			// Preconditions: Node is black, Sibling is black.
			// Condition: Both of Sibling's children are black, Parent is red.
			// Operation: Swap Sibling's and Parent's colors
			// Effects: Following this operation, all paths passing through Sibling retain the
			//   same number of black nodes, however paths passing through Node have gained one
			//   black node, which restores balance to the tree. Repair is complete.
			//
			//  ________________Transformation Diagram________________
			// |                          |                           |
			// |         (P)              |           [P]             |
			// |      ,-'   `-,           |        ,-'   '-,          |
			// |   [N]         [S]        |     [N]         (S)       |
			// |   / \        /   \     --+->   / \        /   \      |
			// | {1} {2}   [L]     [R]    |   {1} {2}   [L]     [R]   |
			// |           / \     / \    |             / \     / \   |
			// |         {3} {4} {5} {6}  |           {3} {4} {5} {6} |
			// |__________________________|___________________________|
			// 	(N) = red, [N] = black, {#} = balanced subtree
			//
			if (isRed(parent) && (sibling == null || (isBlack(sibling.leftChild) && isBlack(sibling.rightChild)))) {
				if (sibling != null) sibling.color = RED;
				parent.color = BLACK;
				return;
			}
			
			
			
			// === Case 5 ===
			// Preconditions: Node is black, Sibling is black, one of Sibling's children is red
			//   (Case 3 and 4 consume any possibility of both children being black).
			// Condition: Sibling's inner child (with respect to parent) is red.
			// Operation: Rotate Sibling so that its red child is now its parent, then exchange the
			//   colors of Sibling and Sibling's new parent.
			// Effects: This operation does not affect the number of black nodes on any branch.
			//   It does, however, re-arrange the tree into case 6.
			//
			//  ________________Transformation Diagram____________________
			// |                          |                               |
			// |         {P}*             |           {P}*                |
			// |      ,-'   `-,           |        ,-'   '-,              |
			// |   [N]         [S]        |     [N]         [L]           |
			// |   / \        /   \     --+->   / \        /   \          |
			// | {1} {2}   (L)     [R]    |   {1} {2}   {3}     (S)       |
			// |           / \     / \    |                    /   \      |
			// |         {3} {4} {5} {6}  |                 {4}     [R]   |
			// |                          |                         / \   |
			// |                          |                       {5} {6} |
			// |__________________________|_______________________________|
			// 	(N) = red, [N] = black, {#} = balanced subtree
			//  {P}* = parent with unknown color
			//
			if (node.isLeftChild()) {
				if (isBlack(sibling.rightChild)) {
					// According to preconditions, Sibling's other child is red.
					sibling.color = RED;
					sibling.leftChild.color = BLACK;
					sibling = sibling.rotateRight();
				}
			} else {
				if (isBlack(sibling.leftChild)) {
					// According to preconditions, Sibling's other child is red.
					sibling.color = RED;
					sibling.rightChild.color = BLACK;
					sibling = sibling.rotateLeft();
				}
			}
			
			// === Case 6 ===
			// Preconditions: Node is black, Sibling is black, Sibling's outer child is red, Sibling's inner child is black.
			// Conditions: --
			// Operation: Exchange the colors of Parent and Sibling, then change Sibling's outer-child's color to black,
			//   finally Rotate Parent so that Sibling becomes Grandparent of Node.
			// Effects: Following this operation:
			//   1. Branches passing through Sibling's right child would lose one black node due to either:
			//		 A. Parent being red, resulting in Sibling being turned from black to red, or
			//       B. Parent being black but, because Sibling is promoted, Sibling loses one black ancestor. 
			//     However, the color of Sibling's right child is changed from red to black, restoring the original
			//     number of black nodes on branches passing through Sibling's right child.
			//   2. Branches passing through Sibling's left child retain the same number of black nodes as they now
			//     route through Sibling (which has taken Parent's original color) and Parent (which has taken Sibling's
			//     original color).
			//   3. Node has one additional black ancestor due to either:
			//       A. Parent turning from red to black, or
			//       B. Parent already being black, but becoming a child of Sibling which was black.
			//     This results in completion of the removeRepairTree operation.
			//
			//  ________________Transformation Diagram_______________
			// |                          |                          |
			// |         {P}*             |              {S}*        |
			// |      ,-'   `-,           |           ,-'   '-,      |
			// |   [N]         [S]        |        [P]         [R]   |
			// |   / \        /   \     --+->     /   \        / \   |
			// | {1} {2}   {L}     (R)    |    [N]     {L}   {3} {4} |
			// |                   / \    |    / \                   |
			// |                 {3} {4}  |  {1} {2}                 |
			// |__________________________|__________________________|
			// 	(N) = red, [N] = black, {#} = balanced subtree
			//  {P}* = parent with unknown color, {S}* same color as original parent
			//
			sibling.color = parent.color;
			parent.color = BLACK;
			if (node.isLeftChild()) {
				sibling.rightChild.color = BLACK;
				parent.rotateLeft();
			} else {
				sibling.leftChild.color = BLACK;
				parent.rotateRight();
			}
			return;
		}
	}
	
}
