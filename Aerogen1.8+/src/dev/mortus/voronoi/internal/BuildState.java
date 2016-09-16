package dev.mortus.voronoi.internal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import dev.mortus.util.Pair;
import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.Voronoi;
import dev.mortus.voronoi.internal.tree.Arc;
import dev.mortus.voronoi.internal.tree.Breakpoint;
import dev.mortus.voronoi.internal.tree.ShoreTree;
import dev.mortus.voronoi.internal.tree.TreeNode;


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
	private int eventsProcessed;

	public BuildState (Voronoi voronoi, Rectangle2D bounds) {
		this.voronoi = voronoi;
		this.eventQueue = new PriorityQueue<Event>(EventOrder);
		this.shoreTree = new ShoreTree();
		this.sites = new ArrayList<Site>();
		
		this.minX = bounds.getMinX();
		this.maxX = bounds.getMaxX();
		this.minY = bounds.getMinY();
		this.maxY = bounds.getMaxY();
		
		this.eventsProcessed = 0;
	}

	public void initSiteEvents(List<Point2D> points) {
		eventQueue.clear();
		TreeNode.IDCounter = 0;
		Site.IDCounter = 0;
		
		for(Point2D point : points) {
			Site site = new Site(point);
			Event e = Event.createSiteEvent(site);
			sites.add(site);
			addEvent(e);
		}

		Event e = eventQueue.poll();
		
		// Create tree with first site
		shoreTree.initialize(e.site);
		eventsProcessed = 1;
	}
	
	public boolean hasNextEvent() {
		return eventQueue.size() > 0;
	}
	
	private void addEvent(Event e) {
		 if (e == null) return;
		 eventQueue.offer(e);
	}
	
	private void removeEvent(Event e) {
		 if (e == null) return;
		 eventQueue.remove(e);
	}
	
	public void processNextEvent() {
		if (!shoreTree.isInitialized()) throw new RuntimeException("Shore Tree has not yet been initialized");
		
		Event e = eventQueue.poll();
		sweeplineY = e.getPosition().getY();
		
		switch(e.type) {
			case SITE:
				processSiteEvent(e.site);
				break;
				
			case CIRCLE:
				processCircleEvent(e.arc);
				break;
		}
		
		eventsProcessed++;
		
		if (Voronoi.DEBUG) {
			int siteCount = 0, circleCount = 0;
			for (Event event : eventQueue) {
				if (event.type == Event.Type.SITE) siteCount++;
				if (event.type == Event.Type.CIRCLE) circleCount++;
			}
			System.out.println("processed: "+eventsProcessed+" events. "+siteCount+" site events and "+circleCount+" circle events remaining.");
			System.out.println("just processed: "+e+", next: "+eventQueue.peek());
			
			TreeNode n = shoreTree.getRoot().getFirstDescendant();
			System.out.println(" ========== TREELIST ==========");
			while (n != null) {
				System.out.println(n);
				n = n.getSuccessor();
			}
		}
		
	}
	
	private void processSiteEvent(Site site) {
		Arc arcUnderSite = shoreTree.getArcUnderSite(this.sweeplineY, site);
		removeEvent(arcUnderSite.getCircleEvent());
		
		// Split the arc with two breakpoints (possibly just one) and insert an arc for the site event
		Arc newArc = arcUnderSite.insertArc(this, site);
		
		// Check for circle events on neighboring arcs
		for (Arc neighbor : newArc.getNeighborArcs()) {
			addEvent(neighbor.checkCircleEvent(this));
		}
		
		// TODO: Create new edges
//		for (TreeNode node : newArc.getBreakpoints() ) {
//			node.checkEdge();
//		}
		
	}
	
	private void processCircleEvent(Arc arc) {
		
		// Save these, they will change
		Pair<Arc> neighbors = arc.getNeighborArcs(); 
		Breakpoint predecessor = arc.getPredecessor();
		Breakpoint successor = arc.getSuccessor();
		
		
		// Step 1. Remove arc and one of its breakpoints
		TreeNode parentBreakpoint = arc.getParent();
		TreeNode sibling = arc.getSibling();
			
		if (parentBreakpoint != successor && parentBreakpoint != predecessor) {
			if (arc.isLeftChild()) throw new RuntimeException("Unexpected successor! "+successor + ", should be "+parentBreakpoint);
			if (arc.isRightChild()) throw new RuntimeException("Unexpected predecessor! "+predecessor + ", should be "+parentBreakpoint);
			throw new RuntimeException("The parent of any arc should be it successor or its predecessor!");
		}
		
		sibling.disconnect();
		parentBreakpoint.replaceWith(sibling);
		
		
		// Step 2. Update the remaining breakpoint
		if (parentBreakpoint == successor) predecessor.updateArcs();
		if (parentBreakpoint == predecessor) successor.updateArcs();
		
		
		// Step 3. Update circle events
		for (Arc neighbor : neighbors) {
			removeEvent(neighbor.circleEvent);
			addEvent(neighbor.checkCircleEvent(this));
		}
		
	}
	
	public double getSweeplineY() {
		return sweeplineY;
	}
	
	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
	}

	public void drawDebugState(Graphics2D g) {
		g.setFont(new Font("Consolas", Font.PLAIN, 12));
		this.shoreTree.draw(this, g);
		
		g.setColor(Color.YELLOW);
		Line2D line = new Line2D.Double(minX, sweeplineY, maxX, sweeplineY);
		g.draw(line);
		
		AffineTransform transform = g.getTransform();
		AffineTransform identity = new AffineTransform();
		
		for (Site s : sites) {
			Ellipse2D sitedot = new Ellipse2D.Double(s.getX()-1, s.getY()-1, 2, 2);
			g.setColor(new Color(0,128,0));
			g.draw(sitedot);
			
			g.setTransform(identity);
			Point2D pt = new Point2D.Double(s.getX(), s.getY());
			transform.transform(pt, pt);
			g.setColor(new Color(0,255,0));
			g.drawString(""+s.id, (int) pt.getX(), (int) pt.getY());
			g.setTransform(transform);
		}
	}

	public void debugAdvanceSweepline(double v) {
		this.sweeplineY += v;
	}

	public int getEventsProcessed() {
		return eventsProcessed;
	}

	public int getTheoreticalMaxSteps() {
		int maxPossibleCircleEvents = sites.size()*2 - 5;
		if (sites.size() <= 2) maxPossibleCircleEvents = 0;
		int numSiteEvents = sites.size(); 
		return numSiteEvents + maxPossibleCircleEvents;
	}

	public void processNextEventVerbose() {
		boolean hold = Voronoi.DEBUG;
		Voronoi.DEBUG = true;
		processNextEvent();
		Voronoi.DEBUG = hold;
	}
	
}
