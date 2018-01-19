package com.gpergrossi.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

import com.gpergrossi.util.data.OrderedPair;
import com.gpergrossi.util.data.btree.AbstractLinkedBinaryNode;
import com.gpergrossi.util.data.btree.IBinaryNode;

import static org.junit.Assert.*;

public class LinkedTreeTest {

	public static class TestNode extends AbstractLinkedBinaryNode<TestNode> {
		TestTree tree;
		int id;
		int value;
		
		protected TestNode(TestTree tree, int value) {
			super();
			this.tree = tree;
			this.id = tree.getNextID();
			this.value = value;
		}
		
		@Override
		public void replaceWith(TestNode child) {
			super.replaceWith(child);
			if (this == tree.root) tree.root = child;
		}
		
		@Override
		public void removeFromParent() {
			super.removeFromParent();
			if (this == tree.root) tree.root = null;
		}
		
		public TestNode insert(int value) {
			if (value < this.value) {
				if (this.hasLeftChild()) {
					return this.getLeftChild().insert(value);
				} else {
					TestNode node = new TestNode(tree, value);
					this.setLeftChild(node);
					return node;
				}
			} else {
				if (this.hasRightChild()) {
					return this.getRightChild().insert(value);
				} else {
					TestNode node = new TestNode(tree, value);
					this.setRightChild(node);
					return node;
				}
			}
		}
		
		public TestNode find(int value) {
			if (this.value == value) return this;
			if (value < this.value) {
				if (this.hasLeftChild()) return this.getLeftChild().find(value);
			} else {
				if (this.hasRightChild()) return this.getRightChild().find(value);
			}
			return null;
		}
		
		@Override
		public String toString() {
			return "TestNode[ID="+id+" value="+value+"]";
		}
	}
	
	public static class TestTree implements Iterable<TestNode> {
		private int idCounter = 0;
		TestNode root;
		
		public TestTree(int value) {
			this.root = new TestNode(this, value);
		}
		
		public TestTree(int... values) {
			this.root = new TestNode(this, values[0]);
			for (int i = 1; i < values.length; i++) {
				this.insert(values[i]);
			}
		}
		
		public int getNextID() {
			return (idCounter++);
		}

		public TestNode insert(int value) {
			return this.root.insert(value);
		}
		
		public TestNode[] insertMany(int... values) {
			List<TestNode> nodes = new ArrayList<>();
			for (int value : values) {
				nodes.add(this.root.insert(value));
			}
			return nodes.toArray(new TestNode[values.length]);
		}
		
		public TestNode find(int value) {
			return this.root.find(value);
		}
		
		private String toString(TestNode node, String indent, String name) {
			if (node == null) return "";
			return toString(node.getLeftChild(), indent + "      ", "left") + indent + name + ": " + node.toString() + "\n" + toString(node.getRightChild(), indent + "      ", "right"); 
		}
		
		@Override
		public String toString() {
			return toString(root, "", "root");
		}
		
		@Override
		public Iterator<TestNode> iterator() {
			return this.root.iterator();
		}	
	}
	
	public static final int[] unbalanced = new int[] {0, 1, 2, 3, 4, 5, 6};
	public static final int[] balanced = new int[] {3, 1, 2, 0, 5, 6, 4};

	@Test
	public void testSingleValueConstructor() {
		TestTree tree = new TestTree(1337);
		TestNode root = tree.root;
		
		assertNotNull(root);
		assertEquals(1337, root.value);
		assertEquals(root, root.getFirstDescendant());
		assertEquals(root, root.getLastDescendant());
		
		assertNull(root.getLeftChild());
		assertNull(root.getRightChild());		
		assertNull(root.getPredecessor());
		assertNull(root.getSuccessor());

		assertFalse(root.hasParent());
		assertFalse(root.hasLeftChild());
		assertFalse(root.hasRightChild());
		assertFalse(root.isLeftChild());
		assertFalse(root.isRightChild());
	}
	
	@Test
	public void testTreeInsert() {
		TestTree tree = new TestTree(3);
		tree.insert(1);
		
		assertTrue(tree.root.hasLeftChild());
		
		TestNode node = tree.find(1);
		assertNotNull(node);
		
		assertTrue(node.hasParent());
		assertEquals(tree.root, node.getParent());
	}

	@Test
	public void testTreeBreadthAndDepth() {
		TestTree tree = new TestTree(balanced);
		
		OrderedPair<Integer> breadthAndDepth = IBinaryNode.getTreeBreadthAndDepth(tree.root);
		assertEquals(7, (int) breadthAndDepth.first);
		assertEquals(3, (int) breadthAndDepth.second);
		
		tree = new TestTree(unbalanced);
		
		breadthAndDepth = IBinaryNode.getTreeBreadthAndDepth(tree.root);
		assertEquals(7, (int) breadthAndDepth.first);
		assertEquals(7, (int) breadthAndDepth.second);
	}
	
	@Test
	public void testTreeIteratorOrder() {
		TestTree tree = new TestTree(3);
		TestNode node3 = tree.root; 
		TestNode node1 = tree.insert(1);
		TestNode node5 = tree.insert(5);
		TestNode node0 = tree.insert(0);
		TestNode node2 = tree.insert(2);
		TestNode node4 = tree.insert(4);
		TestNode node6 = tree.insert(6);
		
		Iterator<TestNode> iter = tree.iterator();
		assertEquals(iter.next(), node0);
		assertEquals(iter.next(), node1);
		assertEquals(iter.next(), node2);
		assertEquals(iter.next(), node3);
		assertEquals(iter.next(), node4);
		assertEquals(iter.next(), node5);
		assertEquals(iter.next(), node6);
		assertFalse(iter.hasNext());
		
		tree = new TestTree(0);
		node0 = tree.root; 
		node1 = tree.insert(1);
		node2 = tree.insert(2);
		node3 = tree.insert(3);
		node4 = tree.insert(4);
		node5 = tree.insert(5);
		node6 = tree.insert(6);
		
		iter = tree.iterator();
		assertEquals(iter.next(), node0);
		assertEquals(iter.next(), node1);
		assertEquals(iter.next(), node2);
		assertEquals(iter.next(), node3);
		assertEquals(iter.next(), node4);
		assertEquals(iter.next(), node5);
		assertEquals(iter.next(), node6);
		assertFalse(iter.hasNext());
	}
	
	@Test
	public void testTreeStructure() {
		TestTree tree = new TestTree(3);
		TestNode node3 = tree.root;
		TestNode node1 = tree.insert(1);
		TestNode node5 = tree.insert(5);
		TestNode node0 = tree.insert(0);
		TestNode node2 = tree.insert(2);
		TestNode node4 = tree.insert(4);
		TestNode node6 = tree.insert(6);
		
		assertEquals(node0, node3.getFirstDescendant());
		assertEquals(node6, node3.getLastDescendant());
		assertEquals(node1, node3.getLeftChild());
		assertEquals(node5, node3.getRightChild());
		assertEquals(node2, node3.getPredecessor());
		assertEquals(node4, node3.getSuccessor());
		
		assertNull(node3.getSibling());
		assertEquals(node5, node1.getSibling());
		assertEquals(node1, node5.getSibling());
		assertEquals(node2, node0.getSibling());
		assertEquals(node0, node2.getSibling());
		assertEquals(node6, node4.getSibling());
		assertEquals(node4, node6.getSibling());
		
		assertTrue(node3.hasChildren());
		assertTrue(node1.hasChildren());
		assertTrue(node5.hasChildren());
		assertFalse(node0.hasChildren());
		assertFalse(node2.hasChildren());
		assertFalse(node4.hasChildren());
		assertFalse(node6.hasChildren());

		assertFalse(node3.hasParent());
		assertTrue(node0.hasParent());
		assertTrue(node1.hasParent());
		assertTrue(node2.hasParent());
		assertTrue(node4.hasParent());
		assertTrue(node5.hasParent());
		assertTrue(node6.hasParent());
	}
	
	@Test
	public void testHasAncestor() {
		TestTree tree = new TestTree(3);
		TestNode node3 = tree.root;
		TestNode node1 = tree.insert(1);
		TestNode node5 = tree.insert(5);
		TestNode node0 = tree.insert(0);
		TestNode node2 = tree.insert(2);
		TestNode node4 = tree.insert(4);
		TestNode node6 = tree.insert(6);
		
		assertTrue(node6.hasAncestor(node5));
		assertTrue(node4.hasAncestor(node5));
		assertTrue(node6.hasAncestor(node3));
		assertTrue(node4.hasAncestor(node3));
		assertTrue(node5.hasAncestor(node3));		
		assertTrue(node0.hasAncestor(node1));
		assertTrue(node2.hasAncestor(node1));
		assertTrue(node0.hasAncestor(node3));
		assertTrue(node2.hasAncestor(node3));
		assertTrue(node2.hasAncestor(node3));

		assertFalse(node6.hasAncestor(node4));
		assertFalse(node0.hasAncestor(node2));
		assertFalse(node6.hasAncestor(node0));
	}
	
	@Test
	public void testCommonAncestor() {
		TestTree tree = new TestTree(3);
		TestNode node3 = tree.root;
		TestNode node1 = tree.insert(1);
		TestNode node5 = tree.insert(5);
		TestNode node0 = tree.insert(0);
		TestNode node2 = tree.insert(2);
		TestNode node4 = tree.insert(4);
		TestNode node6 = tree.insert(6);
		
		assertEquals(node1, node0.getCommonAncestor(node2));
		assertEquals(node3, node0.getCommonAncestor(node6));
		assertEquals(node3, node2.getCommonAncestor(node5));
		
		assertEquals(node3, node3.getCommonAncestor(node0));
		assertEquals(node3, node3.getCommonAncestor(node1));
		assertEquals(node3, node3.getCommonAncestor(node2));
		assertEquals(node3, node3.getCommonAncestor(node3));
		assertEquals(node3, node3.getCommonAncestor(node4));
		assertEquals(node3, node3.getCommonAncestor(node5));
		assertEquals(node3, node3.getCommonAncestor(node6));
		
		TestNode outsider = new TestNode(tree, 500);
		assertNull(node0.getCommonAncestor(outsider));
		assertNull(node1.getCommonAncestor(outsider));
		assertNull(node2.getCommonAncestor(outsider));
		assertNull(node3.getCommonAncestor(outsider));
		assertNull(node4.getCommonAncestor(outsider));
		assertNull(node5.getCommonAncestor(outsider));
		assertNull(node6.getCommonAncestor(outsider));
	}
	
	@Test
	public void testRemove() {
		TestTree tree = new TestTree(3);
		TestNode node3 = tree.root;
		TestNode node1 = tree.insert(1);
		TestNode node5 = tree.insert(5);
		TestNode node0 = tree.insert(0);
		TestNode node2 = tree.insert(2);
		TestNode node4 = tree.insert(4);
		TestNode node6 = tree.insert(6);
		
		node6.removeFromParent();
		node6.removeFromParent();
		OrderedPair<Integer> breadthAndDepth = IBinaryNode.getTreeBreadthAndDepth(tree.root);
		assertEquals(6, (int) breadthAndDepth.first);
		assertEquals(3, (int) breadthAndDepth.second);

		node5.removeFromParent();
		node4.removeFromParent();
		node5.removeFromParent();
		node4.removeFromParent();
		
		breadthAndDepth = IBinaryNode.getTreeBreadthAndDepth(node4);
		assertEquals(1, (int) breadthAndDepth.first);
		assertEquals(1, (int) breadthAndDepth.second);
		
		node1.removeFromParent();
		node0.removeFromParent();
		breadthAndDepth = IBinaryNode.getTreeBreadthAndDepth(node2);
		assertEquals(1, (int) breadthAndDepth.first);
		assertEquals(1, (int) breadthAndDepth.second);
		
		node2.removeFromParent();
		breadthAndDepth = IBinaryNode.getTreeBreadthAndDepth(node3);
		assertEquals(1, (int) breadthAndDepth.first);
		assertEquals(1, (int) breadthAndDepth.second);
		
		node3.removeFromParent();
		assertNull(tree.root);
	}
	
	@Test
	public void testReplaceWithNull() {
		TestTree tree = new TestTree(3);
		TestNode node3 = tree.root;
		TestNode node1 = tree.insert(1);
		TestNode node5 = tree.insert(5);
		TestNode node0 = tree.insert(0);
		TestNode node2 = tree.insert(2);
		TestNode node4 = tree.insert(4);
		TestNode node6 = tree.insert(6);
		
		node6.replaceWith(null);
		node6.replaceWith(null);
		OrderedPair<Integer> breadthAndDepth = IBinaryNode.getTreeBreadthAndDepth(tree.root);
		assertEquals(6, (int) breadthAndDepth.first);
		assertEquals(3, (int) breadthAndDepth.second);

		node5.replaceWith(null);
		node4.replaceWith(null);
		node5.replaceWith(null);
		node4.replaceWith(null);
		
		breadthAndDepth = IBinaryNode.getTreeBreadthAndDepth(node4);
		assertEquals(1, (int) breadthAndDepth.first);
		assertEquals(1, (int) breadthAndDepth.second);
		
		node1.replaceWith(null);
		node0.replaceWith(null);
		breadthAndDepth = IBinaryNode.getTreeBreadthAndDepth(node2);
		assertEquals(1, (int) breadthAndDepth.first);
		assertEquals(1, (int) breadthAndDepth.second);
		
		node2.replaceWith(null);
		breadthAndDepth = IBinaryNode.getTreeBreadthAndDepth(node3);
		assertEquals(1, (int) breadthAndDepth.first);
		assertEquals(1, (int) breadthAndDepth.second);
		
		node3.replaceWith(null);
		assertNull(tree.root);
	}
	
	@Test
	public void testSetChildNull() {
		TestTree tree = new TestTree(3);
		TestNode node3 = tree.root;
		TestNode node1 = tree.insert(1);
		TestNode node5 = tree.insert(5);
		
		node3.setLeftChild(null);
		assertNull(node1.getParent());
		assertNull(node3.getLeftChild());
		
		node3.setRightChild(null);
		assertNull(node5.getParent());
		assertNull(node3.getRightChild());
		
		// assert no errors:
		node3.setLeftChild(null);
		node3.setRightChild(null);
	}
	
	@Test 
	public void testTreeStringSimple() {
		TestTree tree = new TestTree(3);
		assertEquals("root: TestNode[ID=0 value=3]\n", tree.root.treeString());
		
		tree.insert(1);
		tree.insert(5);
		assertEquals(
				"      left: TestNode[ID=1 value=1]\n" + 
				"root: TestNode[ID=0 value=3]\n" + 
				"      right: TestNode[ID=2 value=5]\n"
				, tree.root.treeString());
	}
	
	@Test
	public void testReplaceRoot() {
		TestTree tree = new TestTree(3);
		TestNode root = tree.root;
		
		TestNode node0 = new TestNode(tree, 6);
		root.replaceWith(node0);
		
		assertNull(node0.getParent());
		assertEquals(node0, tree.root);
	}
	
	@Test
	public void testReplaceLeftChild() {
		TestTree tree = new TestTree(3);
		TestNode root = tree.root;
		
		TestNode node0 = tree.insert(0);
		TestNode node1 = new TestNode(tree, 2);
		node0.replaceWith(node1);
		
		assertEquals(root, node1.getParent());
		assertNull(node0.getParent());
	}
	
	@Test
	public void testReplaceRightChild() {
		TestTree tree = new TestTree(3);
		TestNode root = tree.root;
		
		TestNode node5 = tree.insert(5);
		TestNode node7 = new TestNode(tree, 7);
		node5.replaceWith(node7);
		
		assertEquals(root, node7.getParent());
		assertNull(node5.getParent());
	}
	
	@Test
	public void testReplaceWithTree() {
		TestTree tree = new TestTree(3);
		TestNode root = tree.root;
		
		TestNode node10 = tree.insert(10);

		TestNode node3 = new TestNode(tree, 3);
		TestNode node6 = new TestNode(tree, 6);
		TestNode node9 = new TestNode(tree, 9);
		node6.setLeftChild(node3);
		node6.setRightChild(node9);
		
		node10.replaceWith(node6);
		
		assertEquals(node6, root.getRightChild());
		
		Iterator<TestNode> iter = tree.iterator();
		assertEquals(iter.next(), root);
		assertEquals(iter.next(), node3);
		assertEquals(iter.next(), node6);
		assertEquals(iter.next(), node9);
		assertFalse(iter.hasNext());
	}
	
	@Test
	public void testPredecessorSuccessorIntegrity() {
		TestTree tree = new TestTree(3);
		tree.insert(1);
		tree.insert(5);
		TestNode node0 = tree.insert(0);
		tree.insert(2);
		tree.insert(4);
		TestNode node6 = tree.insert(6);
		
		TestNode node7 = new TestNode(tree, 7);
		TestNode node9 = new TestNode(tree, 9);
		TestNode node8 = new TestNode(tree, 8);
		TestNode node10 = new TestNode(tree, 10);
		
		node9.setLeftChild(node8);
		node9.setRightChild(node10);
		node7.setRightChild(node9);
		
		node6.replaceWith(node7);
		node7.setLeftChild(node6);
		
		node0.removeFromParent();
		node8.removeFromParent();
		
		Iterator<TestNode> iter = tree.iterator();
		TestNode previous = iter.next();
		while (iter.hasNext()) {
			TestNode current = iter.next();
			
			assertEquals(previous, current.getPredecessor());
			assertEquals(current, previous.getSuccessor());
			
			assertEquals(previous, IBinaryNode.getPredecessor(current));
			assertEquals(current, IBinaryNode.getSuccessor(previous));
			
			previous = current;
		}
		
		tree = new TestTree(unbalanced);
		iter = tree.iterator();
		previous = iter.next();
		while (iter.hasNext()) {
			TestNode current = iter.next();
			
			assertEquals(previous, current.getPredecessor());
			assertEquals(current, previous.getSuccessor());
			
			assertEquals(previous, IBinaryNode.getPredecessor(current));
			assertEquals(current, IBinaryNode.getSuccessor(previous));
			
			previous = current;
		}
	}
	
	@Test
	public void testGetRoot() {
		TestTree tree = new TestTree(3);
		TestNode node3 = tree.root; 
		TestNode node1 = tree.insert(1);
		TestNode node5 = tree.insert(5);
		TestNode node0 = tree.insert(0);
		TestNode node2 = tree.insert(2);
		TestNode node4 = tree.insert(4);
		TestNode node6 = tree.insert(6);

		Iterator<TestNode> iter = tree.iterator();
		while (iter.hasNext()) {
			assertEquals(tree.root, iter.next().getRoot());
		}
		
		node1.removeFromParent();
		assertEquals(node1, node0.getRoot());
		assertEquals(node1, node2.getRoot());
		
		node5.removeFromParent();
		assertEquals(node5, node4.getRoot());
		assertEquals(node5, node6.getRoot());
		
		node3.setLeftChild(node1);
		node2.setRightChild(node5);
		iter = tree.iterator();
		while (iter.hasNext()) {
			assertEquals(tree.root, iter.next().getRoot());
		}
	}

}
