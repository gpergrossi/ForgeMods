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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import dev.mortus.util.data.Pair;
import dev.mortus.util.math.LineSeg;
import dev.mortus.util.math.Ray;
import dev.mortus.util.math.Rect;
import dev.mortus.util.math.Vec2;
import dev.mortus.voronoi.Edge;
import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.Vertex;
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
	
	/**
	 * <p>Sorts events so that lower Y-coordinate events come first, ties are broken
	 * by giving priority to lower X-coordinates, further ties return 0 (equal).</p>
	 * <p>Since parabola calculations involved in the construction of the diagram
	 * rely on this sorting order, it cannot easily be changed.</p>
	 */
	private static final Comparator<Event> EVENT_ORDER = new Comparator<Event>() {
		public int compare(Event o1, Event o2) {
			// Lowest Y value first
			double y1 = o1.getPos().y;
			double y2 = o2.getPos().y;
			if(y1 < y2) return -1;
			if(y1 > y2) return 1;

			// Lowest X value first
			double x1 = o1.getPos().y;
			double x2 = o2.getPos().y;
			if(x1 < x2) return -1;
			if(x1 > x2) return 1;
			
			return 0;
		}
	};

	private final Voronoi voronoi;
	private final Queue<Event> eventQueue;
	private final ShoreTree shoreTree;
	private double sweeplineY = Double.NEGATIVE_INFINITY;
	private Rectangle2D bounds;
	private int eventsProcessed;
	private boolean finished;
	private List<MutableEdge> edges;
	private int numSites;

	public BuildState (Voronoi voronoi, Rectangle2D bounds) {
		this.voronoi = voronoi;
		this.eventQueue = new PriorityQueue<Event>(EVENT_ORDER);
		this.shoreTree = new ShoreTree();
		
		this.bounds = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(),bounds.getHeight());
		this.edges = new ArrayList<MutableEdge>();
		
		this.eventsProcessed = 0;
	}

	public void initSiteEvents(List<Site> sites) {
		if (eventsProcessed > 0) throw new RuntimeException("Already initialized.");
		numSites = sites.size();
		
		for(Site site : sites) addEvent(Event.createSiteEvent(site));

		// Create tree with first site
		Event e = eventQueue.poll();
		shoreTree.initialize(e.site);
		eventsProcessed = 1;
	}
	
	public boolean hasNextEvent() {
		return !finished;
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
		
		if (eventQueue.size() == 0) {
			finish();
			return;
		}
		
		Event e = eventQueue.poll();
		
		// Advance sweepline
		if (sweeplineY <= e.getPos().y) sweeplineY = e.getPos().y;
		else {
			// An event may be below the sweepline by a VERY_SMALL amount if it is a circle event created by a 
			// site event that landed exactly on top of an existing breakpoint. In this case an already vanishing 
			// arc will be created and must be cleaned up immediately before any other events are processed.
			if (sweeplineY > e.getPos().y+Voronoi.VERY_SMALL) {
				throw new RuntimeException("Event inserted after it should have already been processed. "
						+ "EventY="+e.getPos().y+", sweeplineY="+sweeplineY);
			}
		}
		
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
		Arc arcUnderSite = shoreTree.getArcUnderSite(this, site);
		removeEvent(arcUnderSite.getCircleEvent());
		
		// Split the arc with two breakpoints (possibly just one) and insert an arc for the site event
		Arc newArc = arcUnderSite.insertArc(this, site);
		
		// Check for circle events on neighboring arcs
		for (Arc neighbor : newArc.getNeighborArcs()) {
			addEvent(neighbor.checkCircleEvent(this));
		}
		
		Pair<Breakpoint> bps = newArc.getBreakpoints();
		Vec2 pos = bps.get(0).getPosition(this);
		Vertex vert = new Vertex(pos);
		
		// In the case that the new site was at the same Y coordinate as a previous site,
		// there will be only one breakpoint formed. 
		if (bps.size() == 1) {
			Breakpoint bp = bps.get(0);
			bp.edge = new MutableEdge(bp, vert);
			return;
		}
		
		// Otherwise we expect two breakpoints that will move exactly opposite each other as the
		// beach line progresses. These two edges are "twin" edges and will be combined later.
		if (bps.size() == 2) {
			Pair<HalfEdge> twins = HalfEdge.createTwinPair(bps, vert);
			bps.get(0).edge = twins.get(0);
			bps.get(1).edge = twins.get(1);
			return;
		}
		
		throw new RuntimeException("Site event did not create any breakpoints");
	}
	
	private void processCircleEvent(Arc arc) {
		// Save these, they will change
		Pair<Arc> neighbors = arc.getNeighborArcs(); 
		Breakpoint predecessor = arc.getPredecessor();
		Breakpoint successor = arc.getSuccessor();
		
		// Step 1. Finish the edges of each breakpoint
		Vertex sharedVertex = new Vertex(predecessor.getPosition(this));
		for (Breakpoint bp : arc.getBreakpoints()) {
			if (bp.edge == null) throw new RuntimeException("Circle event expected non-null edge");
			if (bp.edge.isFinished()) throw new RuntimeException("Circle even expected unfinished edge");
			
			addEdge(bp.edge.finish(sharedVertex));
			bp.edge = null;
		}
		
		// Step 2. Remove arc and one of its breakpoints
		TreeNode parentBreakpoint = arc.getParent();
		TreeNode sibling = arc.getSibling();
		if (parentBreakpoint != successor && parentBreakpoint != predecessor) {
			if (arc.isLeftChild()) throw new RuntimeException("Unexpected successor! "+successor + ", should be "+parentBreakpoint);
			if (arc.isRightChild()) throw new RuntimeException("Unexpected predecessor! "+predecessor + ", should be "+parentBreakpoint);
			throw new RuntimeException("The parent of any arc should be its successor or its predecessor!");
		}
		sibling.disconnect();
		parentBreakpoint.replaceWith(sibling);
		
		// Step 3. Update the remaining breakpoint
		Breakpoint remainingBP = null;
		if (parentBreakpoint == successor) remainingBP = predecessor;
		if (parentBreakpoint == predecessor) remainingBP = successor;
		remainingBP.updateArcs();
		
		// Step 4. Update circle events
		for (Arc neighbor : neighbors) {
			removeEvent(neighbor.circleEvent);
			addEvent(neighbor.checkCircleEvent(this));
		}
		
		// Step 5. Form new edge
		remainingBP.edge = new MutableEdge(remainingBP, sharedVertex);
	}
	
	private void finish() {
		if (sweeplineY < bounds.getMinY()) sweeplineY = bounds.getMaxY();
		// TODO merge vertices, clip edges, create new edges along boundaries, link edges/vertices/sites
		// report completed graph back to voronoi

		extendUnfinishedEdges();
		joinHalfEdges();
		clipEdges();
		
		// IDEA output compiler class, takes list of sites and list of edges; returns HashMap<Site, OutputSite>
		
		// TODO create edge list per vertex
		// TODO create edge list and vertex list per site
		
		finished = true;
		System.out.println("Finished. "+edges.size()+" edges");
	}

	private void extendUnfinishedEdges() {		
		Rect boundsRect = new Rect(bounds);
		for (TreeNode node : shoreTree.getRoot().subtreeIterator()) {
			if (!(node instanceof Breakpoint)) continue;
			Breakpoint bp = (Breakpoint) node;
			
			MutableEdge edge = bp.edge;
			if (edge == null) throw new RuntimeException("All breakpoints in shore tree should have partial edges");
			
			Vec2 endPoint = null;
			
			Ray edgeRay = new Ray(edge.start.getPosition(), bp.getDirection());
			if (bounds.contains(edge.start.toPoint2D())) {
				// first point of intersection along ray
				endPoint = boundsRect.intersect(edgeRay).get(0);
			} else {
				// second point of intersection along ray
				endPoint = boundsRect.intersect(edgeRay).get(1);
			}
			
			Vertex vert = new Vertex(endPoint, true);
			addEdge(edge.finish(vert));
			removeEdge(edge);
			bp.edge = null;
		}
	}
	
	private void joinHalfEdges() {
		List<MutableEdge> remove = new ArrayList<MutableEdge>();
		List<MutableEdge> add = new ArrayList<MutableEdge>();
		for (MutableEdge e : edges) {
			if (remove.contains(e)) continue;
			if (!(e instanceof HalfEdge)) continue;
			
			HalfEdge edge = (HalfEdge) e;
			HalfEdge twin = ((HalfEdge) edge).getTwin();
			
			remove.add(edge);
			remove.add(twin);
			add.add(edge.joinHalves());
		}
		for (MutableEdge edge : remove) removeEdge(edge);
		for (MutableEdge edge : add) addEdge(edge);
	}

	private void clipEdges() {
		List<MutableEdge> remove = new ArrayList<MutableEdge>();
		List<MutableEdge> add = new ArrayList<MutableEdge>();
		Rect boundsRect = new Rect(bounds);
		
		for (MutableEdge edge : edges) {
			LineSeg seg = edge.toLineSeg();
			seg = boundsRect.clip(seg);
			
			if (seg == null) {
				remove.add(edge);
				continue;
			}
			
			boolean sameStart = seg.pos.equals(edge.start.getPosition());
			boolean sameEnd = seg.end.equals(edge.end.getPosition());
			
			if (!sameStart || !sameEnd) {				
				Vertex start = edge.start;
				Vertex end = edge.end;
				if (!sameStart) start = new Vertex(seg.pos, true);
				if (!sameEnd) end = new Vertex(seg.end, true);
				
				remove.add(edge);
				add.add(edge.clip(start, end));
			}
		}
		for (MutableEdge edge : remove) removeEdge(edge);
		for (MutableEdge edge : add) addEdge(edge);
	}

	
	private void addEdge(MutableEdge edge) {
		this.edges.add(edge);
	}
	
	private void removeEdge(MutableEdge edge) {
		this.edges.remove(edge);
	}

	public List<Edge> getEdges() {
		return Collections.unmodifiableList(edges);
	}
	
	public boolean isFinished() {
		return finished;
	}

	
	public double getSweeplineY() {
		return sweeplineY;
	}
	
	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

	public void drawDebugState(Graphics2D g) {
		g.setFont(new Font("Consolas", Font.PLAIN, 12));
		if (this.isFinished()) {
			// Draw edges
			for (Edge edge : edges) {
				Line2D line = new Line2D.Double(edge.start.toPoint2D(), edge.end.toPoint2D());
				g.draw(line);
				
				Point2D v0 = edge.start.toPoint2D();
				Point2D v1 = edge.end.toPoint2D();
				Ellipse2D ellipse0 = new Ellipse2D.Double(v0.getX()-1, v0.getY()-1, 2, 2);
				Ellipse2D ellipse1 = new Ellipse2D.Double(v1.getX()-1, v1.getY()-1, 2, 2);
				g.fill(ellipse0);
				g.fill(ellipse1);
			}
			
			// Draw sites
			g.setColor(new Color(0,128,0));
			for (Site s : voronoi.getSites()) {
				Ellipse2D sitedot = new Ellipse2D.Double(s.pos.x-1, s.pos.y-1, 2, 2);
				g.draw(sitedot);
			}
		} else {
			// Draw shore tree (edges, circle events, parabolas)
			this.shoreTree.draw(this, g);
		
			// Draw sweep line
			g.setColor(Color.YELLOW);
			Line2D line = new Line2D.Double(bounds.getMinX(), sweeplineY, bounds.getMaxX(), sweeplineY);
			g.draw(line);
			
			// Draw sites and site labels
			AffineTransform transform = g.getTransform();
			AffineTransform identity = new AffineTransform();
			
			for (Site s : voronoi.getSites()) {
				Ellipse2D sitedot = new Ellipse2D.Double(s.pos.x-1, s.pos.y-1, 2, 2);
				g.setColor(new Color(0,128,0));
				g.draw(sitedot);
				
				g.setTransform(identity);
				Point2D pt = s.pos.toPoint2D();
				transform.transform(pt, pt);
				g.setColor(new Color(0,255,0));
				g.drawString(""+s.id, (int) pt.getX(), (int) pt.getY());
				g.setTransform(transform);
			}
		}
	}

	public void debugAdvanceSweepline(double v) {
		this.sweeplineY += v;
	}

	public int getEventsProcessed() {
		return eventsProcessed;
	}

	public int getTheoreticalMaxSteps() {
		int maxPossibleCircleEvents = numSites*2 - 5;
		if (numSites <= 2) maxPossibleCircleEvents = 0;
		return numSites + maxPossibleCircleEvents;
	}

	public void processNextEventVerbose() {
		boolean hold = Voronoi.DEBUG;
		Voronoi.DEBUG = true;
		processNextEvent();
		Voronoi.DEBUG = hold;
	}
	
}
