package dev.mortus.voronoi.internal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.Voronoi;
import dev.mortus.voronoi.internal.MathUtil.Circle;
import dev.mortus.voronoi.internal.MathUtil.Vec2;


/**
 * Listened to https://www.youtube.com/watch?v=NXXivAiS59Y while coding. Recommended.
 * 
 * @author Gregary
 *
 */
public final class BuildState {
	
	private static final Comparator<Event> EventOrder = new Comparator<Event>() {
		public int compare(Event o1, Event o2) {
			// Lowest Y value first
			double y1 = o1.getPosition().getY();
			double y2 = o2.getPosition().getY();
			if(y1 < y2) return -1;
			if(y1 > y2) return 1;

			// Lowest X value first
			double x1 = o1.getPosition().getX();
			double x2 = o2.getPosition().getX();
			if(x1 < x2) return -1;
			if(x1 > x2) return 1;
			
			return 0;
		}
	};

	private final Voronoi voronoi;
	private final Queue<Event> eventQueue;
	private final ShoreTree shoreTree;
	private double sweeplineY = Double.NaN;
	private double minX, maxX, minY, maxY;
	private final List<Site> sites;

	public BuildState (Voronoi voronoi, Rectangle2D bounds) {
		this.voronoi = voronoi;
		this.eventQueue = new PriorityQueue<Event>(EventOrder);
		this.shoreTree = new ShoreTree();
		this.sites = new ArrayList<Site>();
		
		this.minX = bounds.getMinX();
		this.maxX = bounds.getMaxX();
		this.minY = bounds.getMinY();
		this.maxY = bounds.getMaxY();
	}

	public void addSiteEvents(List<Point2D> points) {
		for(Point2D point : points) {
			Site site = new Site(point);
			Event e = Event.createSiteEvent(site);
			sites.add(site);
			eventQueue.offer(e);
		}
	}
	
	public boolean hasNextEvent() {
		return eventQueue.size() > 0;
	}
	
	public void processNextEvent() {
		Event e = eventQueue.poll();
		sweeplineY = e.getPosition().getY();
		
		switch(e.type) {
			case SITE:
				// Get arc node under site event
				ShoreTree.Node arcNodeUnderSite = shoreTree.getArcUnderSite(this, e.site);
				ShoreTree.Node leftNeighbor = arcNodeUnderSite.getPreviousArc();
				ShoreTree.Node rightNeighbor = arcNodeUnderSite.getNextArc();
				
				// Clear the circle event for the arc being split
				if (arcNodeUnderSite.arc != null) {
					Event oldCircleEvent = arcNodeUnderSite.arc.getCircleEvent();
					if (oldCircleEvent != null) eventQueue.remove(oldCircleEvent);
					arcNodeUnderSite.arc.setCircleEvent(null);
				}
				
				// Split the arc with two breakpoints and insert an arc for the site event
				ShoreTree.Node newArcNode = arcNodeUnderSite.insertArc(this, e.site);
				
				// Check for circle events on left and right neighboring arcs
				// Five sites are relevant: farLeft, left, center, right, farRight
				Arc left, right = left = arcNodeUnderSite.arc;
				Arc center = newArcNode.arc;
				
				Vec2 leftSitePos, rightSitePos = leftSitePos = new Vec2(left.site.getPos());
				Vec2 centerSitePos = new Vec2(center.site.getPos());
				
				if (leftNeighbor != null) {
					Arc farLeft = leftNeighbor.arc;
					Vec2 farLeftSitePos = new Vec2(farLeft.site.getPos());
					
					Circle circle = Circle.fromPoints(farLeftSitePos, leftSitePos, centerSitePos);
					if (circle.y + circle.radius > sweeplineY) {
						farLeft.setCircleEvent(Event.createCircleEvent(farLeft, circle.y + circle.radius));
					}
				}
				
				if (rightNeighbor != null) {
					Arc farRight = rightNeighbor.arc;
					Vec2 farRightSitePos = new Vec2(farRight.site.getPos());
					
					Circle circle = Circle.fromPoints(centerSitePos, rightSitePos, farRightSitePos);
					if (circle.y + circle.radius > sweeplineY) {
						farRight.setCircleEvent(Event.createCircleEvent(farRight, circle.y + circle.radius));
					}
				}
				
				break;
				
			case CIRCLE:
				shoreTree.removeArc(this, e.arc);
				break;
		}
	}

	public double getSweeplineY() {
		return sweeplineY;
	}
	
	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
	}

	public void drawDebugState(Graphics2D g) {
		g.setColor(Color.WHITE);
		this.shoreTree.draw(this, g);
		
		g.setColor(Color.YELLOW);
		Line2D line = new Line2D.Double(minX, sweeplineY, maxX, sweeplineY);
		g.draw(line);
		
		g.setColor(Color.GREEN);
		for (Site s : sites) {
			Ellipse2D sitedot = new Ellipse2D.Double(s.getX()-1, s.getY()-1, 2, 2);
			g.draw(sitedot);
			g.drawString(""+s.id, (int) s.getX(), (int) s.getY());
		}
	}

	public void debugAdvanceSweepline(double v) {
		this.sweeplineY += v;
	}
	
}
