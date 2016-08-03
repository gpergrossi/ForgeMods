package dev.mortus.voronoi.internal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.internal.MathUtil.Parabola;
import dev.mortus.voronoi.internal.MathUtil.Vec2;

public class ShoreTree {
	Node head = new Node();

	public void processSiteEvent(BuildState state, Site site) {
		head.addArc(state, site);
	}

	public void processCircleEvent(BuildState state, Arc arc) {
		head = head.removeArc(state, arc);
	}
		
	public void draw(BuildState state, Graphics2D g) {
		Node n = head.getFirst();
		while (n != null) {
			if (n.type == Node.Type.Arc) {
				Parabola par = Parabola.fromPointAndLine(new Vec2(n.arc.site.getPos()), state.sweeplineY);
				
				double minX = state.minX;
				double maxX = state.maxX;
				
				Node pred = n.getPredecessor(); 
				if (pred != null && pred.type == Node.Type.Breakpoint) minX = pred.breakpoint.getPos(state.sweeplineY).getX();
				
				Node succ = n.getSuccessor(); 
				if (succ != null && succ.type == Node.Type.Breakpoint) maxX = succ.breakpoint.getPos(state.sweeplineY).getX();
				
				if (minX < state.minX) minX = state.minX;
				if (maxX > state.maxX) maxX = state.maxX;
				
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
						if (y1 < state.minY) y1 = state.minY;
						if (y0 < state.minY) y0 = state.minY;
						Line2D line = new Line2D.Double(x, y0, x + step, y1);
						g.draw(line);
					}
				} else {
					Line2D line = new Line2D.Double(par.verticalX, state.minY, par.verticalX, state.sweeplineY);
					g.draw(line);
				}
			}
			n = n.getSuccessor();
		}

		n = head.getFirst();
		while (n != null) {
			if (n.type == Node.Type.Breakpoint) {
				g.setColor(Color.RED);
				Point2D p = n.breakpoint.getPos(state.sweeplineY);
				
				Ellipse2D bpe = new Ellipse2D.Double(p.getX()-2.5, p.getY()-2.5, 5.0, 5.0);
				g.draw(bpe);
				g.drawString(n.breakpoint.arcLeft.site.id+":"+n.breakpoint.arcRight.site.id, (int) p.getX(), (int) p.getY());
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
		}
		
		/**
		 * Assumes the new arc has a greater or equal y coordinate than all previous sites.
		 * If the y coordinate is equal to a previous site, the x coordinate must be greater.
		 * @param state
		 * @param site
		 * @return the node of the new arc
		 */
		public Node addArc(BuildState state, Site site) {
			
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
						// new arc has greater x coordinate because it was added by priority queue
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
					Point2D bp = this.breakpoint.getPos(state.sweeplineY);
					if (site.getX() <= bp.getX()) {
						return leftChild.addArc(state, site);
					} else {
						return rightChild.addArc(state, site);
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
		
		public Node getFirst() {
			Node n = this;
			while (n.leftChild != null) n = n.leftChild;
			return n;
		}
		
		public Node getLast() {
			Node n = this;
			while (n.rightChild != null) n = n.rightChild;
			return n;
		}
		
		public Node getSuccessor() {
			if (this.type == Type.Breakpoint) {
				return this.rightChild.getFirst();
			} else if (this.type == Type.Arc) {
				Node n = this;
				while (n != null && !n.isLeftChild()) n = n.parent;
				if (n != null) return n.parent;
			}
			return null;
		}

		public Node getPredecessor() {
			if (this.type == Type.Breakpoint) {
				return this.leftChild.getLast();
			} else if (this.type == Type.Arc) {
				Node n = this;
				while (n != null && !n.isRightChild()) n = n.parent;
				if (n != null) return n.parent;
			}
			return null;
		}
		
	}
	
}
