package dev.mortus.voronoi.internal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.internal.MathUtil.Parabola;
import dev.mortus.voronoi.internal.MathUtil.Vec2;

public class ShoreTree {
	TreeNode.Sentinel tree = null;

	public Arc getArcUnderSite(final BuildState state, Site site) {
		return tree.getArc(state, site.getX());
	}
	
	public Arc insertArc(final BuildState state, Site site) {
		if (tree == null) {
			tree = new Arc(site);
		}
		Arc newArc = tree.insertArc(state, site);
		return newArc;
	}
	
	public void removeArc(final BuildState state, Arc arc) {
		tree = tree.removeArc(state, arc);
	}
		
	public void draw(final BuildState state, Graphics2D g) {
		TreeNode n = tree.getFirstDescendant();
		while (n != null) {
			if (n.getType().equals("Arc")) {
				Arc arc = (Arc) n;
				Parabola par = Parabola.fromPointAndLine(new Vec2(arc.site.getPos()), state.getSweeplineY());
				
				Rectangle2D bounds = state.getBounds();
				double minX = bounds.getMinX();
				double maxX = bounds.getMaxX();
				double minY = bounds.getMinY();
				
				TreeNode pred = n.getPredecessor(); 
				Point2D predBreakpoint = null;
				if (pred != null && pred.getType().equals("Breakpoint")) {
					Breakpoint breakpoint = (Breakpoint) pred;
					predBreakpoint = breakpoint.getPosition(state.getSweeplineY());
					if (predBreakpoint != null) minX = predBreakpoint.getX();
				}
				
				TreeNode succ = n.getSuccessor(); 
				if (succ != null && succ.getType().equals("Breakpoint")) {
					Breakpoint breakpoint = (Breakpoint) succ;
					Point2D succBreakpoint = breakpoint.getPosition(state.getSweeplineY());
					if (succBreakpoint != null) maxX = succBreakpoint.getX();
				}
				
				if (minX < bounds.getMinX()) minX = bounds.getMinX();
				if (maxX > bounds.getMaxX()) maxX = bounds.getMaxX();
				
				Ellipse2D bpe = new Ellipse2D.Double(minX-2.5, par.get(minX)-2.5, 5.0, 5.0);
				g.draw(bpe);
				bpe = new Ellipse2D.Double(maxX-2.5, par.get(maxX)-2.5, 5.0, 5.0);
				g.draw(bpe);
				
				g.setColor(Color.GRAY);
				int step = 1;
				if (!par.isVertical) {
					for (int x = (int) minX; x < maxX; x += step) {
						if (x > maxX) x = (int) maxX;
						double y0 = par.get(x);
						double y1 = par.get(x + step);
						if (y1 < minY) y1 = minY;
						if (y0 < minY) y0 = minY;
						Line2D line = new Line2D.Double(x, y0, x + step, y1);
						g.draw(line);
					}
				} else {
					Line2D line = new Line2D.Double(par.verticalX, minY, par.verticalX, state.getSweeplineY());
					g.draw(line);
				}
			}
			n = n.getSuccessor();
		}

		g.setColor(Color.RED);
		
		n = tree.getFirstDescendant();
		while (n != null) {
			if (n.getType().equals("Breakpoint")) {
				Breakpoint breakpoint = (Breakpoint) n;
				Point2D p = breakpoint.getPosition(state.getSweeplineY());
				if (p == null) continue;
				
				Ellipse2D bpe = new Ellipse2D.Double(p.getX()-2.5, p.getY()-2.5, 5.0, 5.0);
				g.draw(bpe);
				g.drawString(breakpoint.arcLeft.site.id+":"+breakpoint.arcRight.site.id, (int) p.getX(), (int) p.getY());
			}
			n = n.getSuccessor();
		}
	}
	
	public static class Node {
		
		private static int IDCounter = 0;
		
		enum Type {
			Null, Arc, Breakpoint
		}

		Type type = Type.Null;
		Arc arc;
		Breakpoint breakpoint;
		
		Node leftChild;
		Node rightChild;
		Node parent;
		public final int id;
		
		Node prev, next;

		private Node() {
			this.id = IDCounter++;
		}
		
		private Node(Node parent, Arc arc) {
			this();
			this.parent = parent;
			this.type = Type.Arc;
			this.arc = arc;
		}
		
		private Node(Node parent, Breakpoint bp) {
			this();
			this.parent = parent;
			this.breakpoint = bp;
			this.type = Type.Breakpoint;
			
			this.leftChild = new Node(this, bp.arcLeft);
			this.rightChild = new Node(this, bp.arcRight);
			
			this.prev = null;
			this.next = null;
		}
		
		/**
		 * Assumes the new arc has a greater or equal y coordinate than all previous sites.
		 * If the y coordinate is equal to a previous site, the x coordinate must be greater.
		 * @param state
		 * @param site
		 * @return the node of the new arc
		 */
		public Node insertArc(final BuildState state, Site site) {
			
			Node arc = getArc(state, site);
			
			switch (this.type) {
					
				// Replace this node if it is null
				case Null:
					this.arc = new Arc(site);
					this.type = Type.Arc;
					return this;
			
					
				// Split this node if it is an arc
				case Arc:
					Arc oldArc = this.arc;
					Arc newArc = new Arc(site);
					Node newNode = null;
					
					if (site.getY() == this.arc.site.getY()) {
						// Y coordinates equal, single breakpoint between sites
						// new arc has greater X coordinate because it came from
						// a priority queue that ensures so
						this.breakpoint = new Breakpoint(oldArc, newArc);
						this.leftChild = new Node(this, oldArc);
						this.rightChild = new Node(this, newArc);
						newNode = this.rightChild;
						
					} else {
						// Normal site creation, two breakpoints around new arc
						Breakpoint leftBP = new Breakpoint(oldArc, newArc);
						Breakpoint rightBP = new Breakpoint(newArc, oldArc);
	
						this.breakpoint = leftBP;
						this.leftChild = new Node(this, oldArc);
						this.rightChild = new Node(this, rightBP);
						newNode = this.rightChild.leftChild;
					}
					
					this.type = Type.Breakpoint;
					this.arc = null;
					return newNode;
					
			
				// Call down the tree if it is a Breakpoint
				case Breakpoint:
					Point2D bp = this.breakpoint.getPosition(state.getSweeplineY());
					if (site.getX() <= bp.getX()) {
						return leftChild.insertArc(state, site);
					} else {
						return rightChild.insertArc(state, site);
					}
					
			}
			return null;
		}

		public Node removeArc(BuildState state, Arc arc) {
			
			
			return this;
		}
		
		public boolean isRightChild() {
			if (parent == null) return false;
			return parent.rightChild == this;
		}

		public boolean isLeftChild() {
			if (parent == null) return false;
			return parent.leftChild == this;
		}
		
		public Node getFirstDescendent() {
			Node n = this;
			while (n.leftChild != null) n = n.leftChild;
			return n;
		}
		
		public Node getLastDescendent() {
			Node n = this;
			while (n.rightChild != null) n = n.rightChild;
			return n;
		}
		
		public Node getSuccessor() {
			if (this.type == Type.Breakpoint) {
				return this.rightChild.getFirstDescendent();
			} else if (this.type == Type.Arc) {
				Node n = this;
				while (n != null && !n.isLeftChild()) n = n.parent;
				if (n != null) return n.parent;
			}
			return null;
		}

		public Node getPredecessor() {
			if (this.type == Type.Breakpoint) {
				return this.leftChild.getLastDescendent();
			} else if (this.type == Type.Arc) {
				Node n = this;
				while (n != null && !n.isRightChild()) n = n.parent;
				if (n != null) return n.parent;
			}
			return null;
		}
		
		public Node getPreviousArc() {
			if (this.type == Type.Breakpoint) {
				return getPredecessor();
			} else if (this.type == Type.Arc) {
				Node prevBP = getPredecessor();
				if (prevBP != null) return prevBP.getPredecessor();
			}
			return null;
		}

		public Node getNextArc() {
			if (this.type == Type.Breakpoint) {
				return getSuccessor();
			} else if (this.type == Type.Arc) {
				Node nextBP = getSuccessor();
				if (nextBP != null) return nextBP.getSuccessor();
			}
			
			return null;
		}
		
	}
	
}
