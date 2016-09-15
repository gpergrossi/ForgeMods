package dev.mortus.voronoi.internal.tree;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.internal.BuildState;
import dev.mortus.voronoi.internal.Event;
import dev.mortus.voronoi.internal.MathUtil.Circle;
import dev.mortus.voronoi.internal.MathUtil.Parabola;
import dev.mortus.voronoi.internal.MathUtil.Vec2;
import dev.mortus.voronoi.internal.tree.TreeNode.Type;

public class ShoreTree {
	TreeNode root;
	
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

		AffineTransform transform = g.getTransform();
		AffineTransform identity = new AffineTransform();
		
		g.setColor(Color.BLUE);
		
		TreeNode n = root.getFirstDescendant();
		while (n != null) {
			if (n.getType() == Type.Arc) {
				Arc arc = (Arc) n;
				
				Event circleEvent = arc.getCircleEvent();
				if (circleEvent == null) {
					n = n.getSuccessor();
					continue;
				}

				Breakpoint leftBP = (Breakpoint) arc.getPredecessor();
				Breakpoint rightBP = (Breakpoint) arc.getSuccessor();
				
				Point2D intersection = Breakpoint.getIntersection(state.getSweeplineY(), leftBP, rightBP);
				if (intersection != null) {
					g.setColor(new Color(0,0,128));
					Point2D leftPos = leftBP.getPosition(state.getSweeplineY());
					Point2D rightPos = rightBP.getPosition(state.getSweeplineY());
					Line2D lineBP0 = new Line2D.Double(leftPos.getX(), leftPos.getY(), intersection.getX(), intersection.getY());
					Line2D lineBP1 = new Line2D.Double(rightPos.getX(), rightPos.getY(), intersection.getX(), intersection.getY());
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
			if (n.getType() == Type.Arc) {
				Arc arc = (Arc) n;
				Parabola par = Parabola.fromPointAndLine(new Vec2(arc.site.getPos()), state.getSweeplineY());
				
				Rectangle2D bounds = state.getBounds();
				double minX = bounds.getMinX();
				double maxX = bounds.getMaxX();
				double minY = bounds.getMinY();
				
				TreeNode pred = n.getPredecessor(); 
				Point2D predBreakpoint = null;
				if (pred != null && pred.getType() == Type.Breakpoint) {
					Breakpoint breakpoint = (Breakpoint) pred;
					predBreakpoint = breakpoint.getPosition(state.getSweeplineY());
					if (predBreakpoint != null) minX = predBreakpoint.getX();
				}
				
				TreeNode succ = n.getSuccessor(); 
				Point2D succBreakpoint = null;
				if (succ != null && succ.getType() == Type.Breakpoint) {
					Breakpoint breakpoint = (Breakpoint) succ;
					succBreakpoint = breakpoint.getPosition(state.getSweeplineY());
					if (succBreakpoint != null) maxX = succBreakpoint.getX();
				}
				
				if (minX < bounds.getMinX()) minX = bounds.getMinX();
				if (maxX > bounds.getMaxX()) maxX = bounds.getMaxX();

//				g.setColor(Color.WHITE);
//				Ellipse2D bpe = new Ellipse2D.Double(minX-2.5, par.get(minX)-2.5, 5.0, 5.0);
//				g.draw(bpe);
//				bpe = new Ellipse2D.Double(maxX-2.5, par.get(maxX)-2.5, 5.0, 5.0);
//				g.draw(bpe);
				
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
			if (n.getType() == Type.Breakpoint) {
				Breakpoint breakpoint = (Breakpoint) n;
				Point2D p = breakpoint.getPosition(state.getSweeplineY());
				if (p == null) {
					n = n.getSuccessor();
					continue;
				}

				g.setColor(new Color(128,0,0));
				Ellipse2D bpe = new Ellipse2D.Double(p.getX()-2.5, p.getY()-2.5, 5.0, 5.0);
				g.draw(bpe);
				g.setTransform(identity);
				transform.transform(p, p);
				g.setColor(new Color(255,0,0));
				g.drawString(breakpoint.arcLeft.site.id+":"+breakpoint.arcRight.site.id, (int) p.getX(), (int) p.getY());
				g.setTransform(transform);
			}
			n = n.getSuccessor();
		}
	}

	public TreeNode getRoot() {
		return root;
	}
	
}
