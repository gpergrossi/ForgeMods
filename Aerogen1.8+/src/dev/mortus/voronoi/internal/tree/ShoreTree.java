package dev.mortus.voronoi.internal.tree;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import dev.mortus.util.data.LinkedBinaryNode;
import dev.mortus.util.math.Circle;
import dev.mortus.util.math.Parabola;
import dev.mortus.util.math.Vec2;
import dev.mortus.voronoi.Edge;
import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.Voronoi;
import dev.mortus.voronoi.internal.BuildState;
import dev.mortus.voronoi.internal.Event;

public class ShoreTree implements LinkedBinaryNode.Tree {
	TreeNode root;
	

	public TreeNode getRoot() {
		return root;
	}

	@Override
	public void setRoot(LinkedBinaryNode node) {
		if (node == null) throw new RuntimeException("Cannot remove root node from ShoreTree");
		if (!(node instanceof TreeNode)) throw new RuntimeException("ShoreTree can only have a root of type TreeNode");
		this.root = (TreeNode) node;
	}
	
	public void initialize(Site site) {
		if (root != null) throw new RuntimeException("Tree has already been initialized");
		root = new Arc(this, site);
		System.out.println("initialized "+root);
	}
	
	public boolean isInitialized() {
		return (root != null);
	}
	
	public Arc getArcUnderSite(double sweeplineY, Site site) {
		if (root == null) throw new RuntimeException("Tree has not yuet been initialized");
		return root.getArc(sweeplineY, site.getX());
	}
		
	public void draw(final BuildState state, Graphics2D g) {
		if (root == null) return;
		if (state.isFinished()) {
			drawFinished(state, g);
			return;
		}

		boolean debug = Voronoi.DEBUG;
		Voronoi.DEBUG = false;
		
		AffineTransform transform = g.getTransform();
		AffineTransform identity = new AffineTransform();
		
		g.setColor(Color.BLUE);
		
		TreeNode n = root.getFirstDescendant();
		while (n != null) {
			if (n instanceof Arc) {
				Arc arc = (Arc) n;
				
				Event circleEvent = arc.getCircleEvent();
				if (circleEvent == null) {
					n = n.getSuccessor();
					continue;
				}

				Breakpoint leftBP = (Breakpoint) arc.getPredecessor();
				Breakpoint rightBP = (Breakpoint) arc.getSuccessor();
				
				Vec2 intersection = Breakpoint.getIntersection(state.getSweeplineY(), leftBP, rightBP);
				if (intersection != null) {
					g.setColor(new Color(0,0,128));
					Vec2 leftPos = leftBP.getPosition(state.getSweeplineY());
					Vec2 rightPos = rightBP.getPosition(state.getSweeplineY());
					Line2D lineBP0 = new Line2D.Double(leftPos.toPoint2D(), intersection.toPoint2D());
					Line2D lineBP1 = new Line2D.Double(rightPos.toPoint2D(), intersection.toPoint2D());
					g.draw(lineBP0);
					g.draw(lineBP1);
					g.setColor(Color.BLUE);
				} else {
					g.setColor(Color.RED);
				}
				
				Circle circle = circleEvent.getCircle();
				
				Ellipse2D bpe = new Ellipse2D.Double(circle.x - circle.radius, circle.y-circle.radius, circle.radius*2, circle.radius*2);
				g.draw(bpe);
				Line2D line0 = new Line2D.Double(circle.x-3, circle.y-3, circle.x+3, circle.y+3);
				Line2D line1 = new Line2D.Double(circle.x-3, circle.y+3, circle.x+3, circle.y-3);
				g.draw(line0);
				g.draw(line1);
			}
			n = n.getSuccessor();
		}
		
		n = root.getFirstDescendant();
		while (n != null) {
			if (n instanceof Arc) {
				Arc arc = (Arc) n;
				Parabola par = Parabola.fromPointAndLine(new Vec2(arc.site.getPos()), state.getSweeplineY());
				
				Rectangle2D bounds = state.getBounds();
				double minX = bounds.getMinX();
				double maxX = bounds.getMaxX();
				double minY = bounds.getMinY();
				
				TreeNode pred = n.getPredecessor(); 
				Vec2 predBreakpoint = null;
				if (pred != null && pred instanceof Breakpoint) {
					Breakpoint breakpoint = (Breakpoint) pred;
					predBreakpoint = breakpoint.getPosition(state.getSweeplineY());
					if (predBreakpoint != null) minX = predBreakpoint.x;
				}
				
				TreeNode succ = n.getSuccessor(); 
				Vec2 succBreakpoint = null;
				if (succ != null && succ instanceof Breakpoint) {
					Breakpoint breakpoint = (Breakpoint) succ;
					succBreakpoint = breakpoint.getPosition(state.getSweeplineY());
					if (succBreakpoint != null) maxX = succBreakpoint.x;
				}
				
				if (minX < bounds.getMinX()) minX = bounds.getMinX();
				if (maxX > bounds.getMaxX()) maxX = bounds.getMaxX();
				
				g.setColor(Color.GRAY);
				int step = 1;
				if (!par.isVertical) {
					for (int s = (int) minX; s < maxX; s += step) {
						double x = s;
						if (s >= maxX) x = maxX;
						if (s == (int) minX) x = minX;
						double y0 = par.get(x);
						double y1 = par.get(x + step);
						
						// do not exceed bounds
						if (y0 < minY && y1 < minY) continue;
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
		
		n = root.getFirstDescendant();
		while (n != null) {
			if (n instanceof Breakpoint) {
				Breakpoint breakpoint = (Breakpoint) n;
				Vec2 posVec = breakpoint.getPosition(state.getSweeplineY());
				if (posVec == null) {
					n = n.getSuccessor();
					continue;
				}
				Point2D pos = posVec.toPoint2D();

				g.setColor(new Color(128,0,0));
				Ellipse2D bpe = new Ellipse2D.Double(pos.getX()-2.5, pos.getY()-2.5, 5.0, 5.0);
				g.draw(bpe);
				g.setTransform(identity);
				transform.transform(pos, pos);
				g.setColor(new Color(255,0,0));
				g.drawString(breakpoint.arcLeft.site.id+":"+breakpoint.arcRight.site.id, (int) pos.getX(), (int) pos.getY());
				g.setTransform(transform);
				
				g.setColor(new Color(64,64,64));
				drawPartialEdge(g, breakpoint, state);

			}
			n = n.getSuccessor();
		}
		
		for (Edge edge : state.getEdges()) {
			Line2D line = new Line2D.Double(edge.start().toPoint2D(), edge.end().toPoint2D());
			g.draw(line);
		}
		
		Voronoi.DEBUG = debug;
	}
	
	private void drawFinished(final BuildState state, Graphics2D g) {
		for (Edge edge : state.getEdges()) {
			Line2D line = new Line2D.Double(edge.start().toPoint2D(), edge.end().toPoint2D());
			g.draw(line);
		}
	}

	private void drawPartialEdge(Graphics2D g, Breakpoint bp, BuildState state) {
		Edge edge = bp.edge;
		if (edge != null) {
			Vec2 start = edge.start().getPosition();
			Vec2 end;
			if (edge.isFinished()) end = edge.end().getPosition();
			else end = bp.getPosition(state.getSweeplineY());
			Line2D line = new Line2D.Double(start.toPoint2D(), end.toPoint2D());
			g.draw(line);
		}
	}

	
}
