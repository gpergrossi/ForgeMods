package dev.mortus.voronoi.internal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.function.Predicate;

import dev.mortus.util.data.Pair;
import dev.mortus.util.math.geom.LineSeg;
import dev.mortus.util.math.geom.Ray;
import dev.mortus.util.math.geom.Rect;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.Edge;
import dev.mortus.voronoi.diagram.Site;
import dev.mortus.voronoi.diagram.Vertex;
import dev.mortus.voronoi.diagram.Voronoi;
import dev.mortus.voronoi.diagram.VoronoiBuilder;
import dev.mortus.voronoi.exception.OverlappingSiteException;
import dev.mortus.voronoi.internal.Event.Type;
import dev.mortus.voronoi.internal.tree.*;


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
			double x1 = o1.getPos().x;
			double x2 = o2.getPos().x;
			if(x1 < x2) return -1;
			if(x1 > x2) return 1;
			
			// Allow equal priority circle events
			if (o1.type == Type.CIRCLE) return 0;
			if (o2.type == Type.CIRCLE) return 0;
			
			// We cannot allow multiple site events with the same exact position
			throw new OverlappingSiteException(o1.site, o2.site);
		}
	};

	private static final Predicate<Breakpoint> NEW_BREAKPOINTS = (bp -> bp != null && bp.edge == null);

	private Rect bounds;
	private final Queue<Event> eventQueue;
	private final ShoreTree shoreTree;
	
	private double sweeplineY = Double.NEGATIVE_INFINITY;
	private boolean debugSweeplineAdjust;
	private double backupSweeplineY;

	private int numEventsProcessed;
	
	private Voronoi voronoi;
	private List<MutableEdge> edges;
	private List<MutableVertex> vertices;
	private List<MutableSite> sites;

	private boolean finished;
	
	public BuildState (VoronoiBuilder.InitialState init) {
		if (init == null) throw new NullPointerException();
		
		this.bounds = init.bounds;
		this.eventQueue = new PriorityQueue<Event>(EVENT_ORDER);
		this.shoreTree = new ShoreTree();
		
		this.voronoi = init.voronoi;
		this.edges = new ArrayList<>();
		this.vertices = new ArrayList<>();
		this.sites = new ArrayList<>();
		
		this.initSiteEvents(init.sites);
		this.finished = false;
	}
	
	
	
	public boolean hasNextEvent() {
		return !finished;
	}

	public int getNumEventsProcessed() {
		return numEventsProcessed;
	}	

	public int getTheoreticalMaxSteps() {
		int numSites = sites.size();
		int maxPossibleCircleEvents = numSites*2 - 5;
		if (numSites <= 2) maxPossibleCircleEvents = 0;
		return numSites + maxPossibleCircleEvents;
	}
	
	public void debugAdvanceSweepline(double v) {
		this.debugSweeplineAdjust = true;
		this.backupSweeplineY = sweeplineY;
		this.sweeplineY += v;
	}
	
	private void initSiteEvents(Vec2[] sites) {
		try {
			int id = 0;
			for(Vec2 sitePos : sites) {
				MutableSite site = new MutableSite(this.voronoi, id++, sitePos);
				this.sites.add(site);
				addEvent(Event.createSiteEvent(site));
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Cannot build, multiple sites with same position");
		}

		// Create tree with first site
		Event e = eventQueue.poll();
		shoreTree.initialize(e.site);
		this.numEventsProcessed = 1;
	}
	
	public void processNextEvent() {
		if (!shoreTree.isInitialized()) throw new RuntimeException("Shore Tree has not yet been initialized");

		Event e = eventQueue.poll();
		if (e == null) {
			finish();
			return;
		}
		advanceSweepLine(e);
		
		// Process the event
		switch(e.type) {
			case SITE:		processSiteEvent(e.site);	break;
			case CIRCLE:	processCircleEvent(e.arc);	break;
		}
		
		numEventsProcessed++;
		if (Voronoi.DEBUG) printDebugEvent(e);
	}	
	
	private void advanceSweepLine(Event e) {
		if (e == null) return;
		
		// Restore debug sweep line
		if (debugSweeplineAdjust) {
			debugSweeplineAdjust = false;
			sweeplineY = backupSweeplineY;
		}
		
		// Advance sweep line
		if (sweeplineY <= e.getPos().y) {
			sweeplineY = e.getPos().y;
		} else {
			// An event may be above the sweep line by a VERY_SMALL amount if it is a circle event 
			// Such circle events are created when a site event that lands on top of a breakpoint.
			if (sweeplineY > e.getPos().y+Voronoi.VERY_SMALL) {
				throw new RuntimeException("Event inserted after it should have already been processed. Event="+e+", sweeplineY="+sweeplineY);
			}
		}
	}
	
	private void printDebugEvent(Event e) {
		int siteCount = 0, circleCount = 0;
		for (Event event : eventQueue) {
			if (event.type == Event.Type.SITE) siteCount++;
			if (event.type == Event.Type.CIRCLE) circleCount++;
		}
		System.out.println("processed: "+numEventsProcessed+" events. "+siteCount+" site events and "+circleCount+" circle events remaining.");
		System.out.println("just processed: "+e+", next: "+eventQueue.peek());
		
		TreeNode n = shoreTree.getRoot().getFirstDescendant();
		System.out.println(" ========== TREELIST ==========");
		while (n != null) {
			System.out.println(n);
			n = n.getSuccessor();
		}
	}
	
	public void processNextEventVerbose() {
		boolean hold = Voronoi.DEBUG;
		Voronoi.DEBUG = true;
		processNextEvent();
		Voronoi.DEBUG = hold;
	}

	private void processSiteEvent(Site site) {		
		Arc arcUnderSite = shoreTree.getArcUnderSite(this, site);
		
		Event arcCircleEvent = arcUnderSite.getCircleEvent();
		if (arcCircleEvent != null) removeEvent(arcCircleEvent);
		
		Arc newArc = arcUnderSite.insertArc(this, site);
		
		for (Arc neighbor : newArc.getNeighborArcs()) {
			Event circleEvent = neighbor.checkCircleEvent(this);
			if (circleEvent != null) addEvent(circleEvent);
		}
		
		Pair<Breakpoint> bps = newArc.getBreakpoints();
		bps = bps.filter(NEW_BREAKPOINTS);

		// Check for errors
		if (bps.size() == 0) throw new RuntimeException("Site event did not create any breakpoints");
		
		// Create vertex for new edges 
		Vec2 pos = bps.get(0).getPosition(this);
		MutableVertex vert = new MutableVertex(pos);
		
		// In the case that the new site was at the same Y coordinate as a previous site,
		// there will be only one breakpoint formed.
		if (bps.size() == 1) {
			Breakpoint bp = bps.get(0);
			bp.edge = new MutableEdge(bp, vert);
			vertices.add(vert);
			return;
		}
		
		// Otherwise we expect two breakpoints that will move exactly opposite each other as the
		// sweep line progresses. These two edges are "twin" edges and will be combined later.
		if (bps.size() == 2) {
			Pair<HalfEdge> twins = HalfEdge.createTwinPair(bps, vert);
			bps.get(0).edge = twins.get(0);
			bps.get(1).edge = twins.get(1);
			return;
		}
	}
	
	private void processCircleEvent(Arc arc) {
		// Save these, they will change
		Pair<Arc> neighbors = arc.getNeighborArcs(); 
		Breakpoint predecessor = arc.getPredecessor();
		Breakpoint successor = arc.getSuccessor();
		
		// Step 1. Finish the edges of each breakpoint
		MutableVertex sharedVertex = new MutableVertex(predecessor.getPosition(this));
		vertices.add(sharedVertex);
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
			if (neighbor.circleEvent != null) removeEvent(neighbor.circleEvent);
			Event newCircleEvent = neighbor.checkCircleEvent(this);
			if (newCircleEvent != null) addEvent(newCircleEvent);
		}
		
		// Step 5. Form new edge
		remainingBP.edge = new MutableEdge(remainingBP, sharedVertex);
	}
	
	private void finish() {
		if (finished) return;
		if (sweeplineY < bounds.minY()) sweeplineY = bounds.maxY();

		// Finish any edges that have not been assigned a second vertex
		// by projecting them on to the bounding rectangle
		extendUnfinishedEdges();
		
		// Merge half edges into full edges
		joinHalfEdges();
		
		// Clip edges against bounding rectangle
		clipEdges();
				
		// combines vertices closer than Vec2.EPSILON (DANGER! EPSILON must
		// be significantly smaller than the smallest distance between sites)
		combineVertices();
		
		// Adds all edges, vertices, and sites to each others' lists using the 
		// references already defined in edges
		createLinks();

		// Sorts the vertex and edge lists in each site to be in counterclockwise order
		sortSiteLists();

		// Finish boundaries
		createBoundaryEdges();
		
		// Replaces the lists in each site and vertex with unmodifiable lists
		finalizeLinks();

		finished = true;
	}

	/**
	 * Projects all unfinished edges out to the bounding box and gives them a closing vertex
	 */
	private void extendUnfinishedEdges() {
		for (TreeNode node : shoreTree) {
			if (!(node instanceof Breakpoint)) continue;
			Breakpoint bp = (Breakpoint) node;
			
			MutableEdge edge = bp.edge;
			if (edge == null || edge.isFinished()) {
				throw new RuntimeException("All breakpoints in shore tree should have partial edges");
			}
			
			
			Ray edgeRay = new Ray(edge.getStart().getPosition(), bp.getDirection());
			Pair<Vec2> intersect = bounds.intersect(edgeRay);

			Vec2 endPoint = null;
			if (bounds.contains(edge.getStart().getPosition())) {
				// Ray starts inside bounds, intersection will be first point of intersection along ray
				if (intersect.size() != 1) {
					throw new RuntimeException("Expected 1 intersection between\n"+edgeRay+"\nand\n"+bounds+"\ngot\n"+intersect);
				}
				endPoint = intersect.get(0);
			} else {
				// Ray starts out of bounds, intersection will be second point of intersection along ray (if any intersections exist)
				if (intersect.size() != 2 && intersect.size() != 0) {
					throw new RuntimeException("Expected 0 or 2 intersections between "+edgeRay+" and "+bounds);
				}
				endPoint = intersect.get(1);
			}
			if (endPoint == null) endPoint = bp.getPosition(this);
			
			MutableVertex vert = new MutableVertex(endPoint, true);
			vertices.add(vert);
			addEdge(edge.finish(vert));
			bp.edge = null;
		}
	}
	
	/**
	 * Joins all half edges into full edges.
	 * <pre>
	 * before:  A.end <-- A.start == B.start --> B.end
	 * after:   C.start -----------------------> C.end
	 * </pre>
 	 */
	private void joinHalfEdges() {
		List<MutableEdge> joined = new ArrayList<MutableEdge>();
		for (MutableEdge e : edges) {
			if (!e.isHalf()) {
				joined.add(e);
				continue;
			}
			
			HalfEdge edge = (HalfEdge) e;
			HalfEdge twin = edge.getTwin();
			
			if (edge.hashCode() > twin.hashCode()) continue;
			
			joined.add(edge.joinHalves());
		}
		edges = joined;
	}

	/**
	 * Clip edges to bounding rectangle
	 */
	private void clipEdges() {
		List<MutableEdge> remove = new ArrayList<MutableEdge>();
		List<MutableEdge> add = new ArrayList<MutableEdge>();
		
		for (MutableEdge edge : edges) {
			if (!edge.isFinished()) throw new RuntimeException("unfinished edge");
			
			LineSeg seg = edge.toLineSeg();
			seg = bounds.clip(seg);
			if (seg == null) {
				vertices.remove(edge.getStart());
				vertices.remove(edge.getEnd());
				remove.add(edge);
				continue;
			}
			
			boolean sameStart = seg.pos.equals(edge.getStart().getPosition());
			boolean sameEnd = seg.end.equals(edge.getEnd().getPosition());
			
			if (!sameStart || !sameEnd) {				
				MutableVertex start = edge.getStart();
				MutableVertex end = edge.getEnd();
				if (!sameStart) {
					vertices.remove(start);
					start = new MutableVertex(seg.pos, true);
					vertices.add(start);
				}
				if (!sameEnd) {
					vertices.remove(end);
					end = new MutableVertex(seg.end, true);
					vertices.add(end);
				}
				
				remove.add(edge);
				add.add(edge.clip(start, end));
			}
		}
		for (MutableEdge edge : remove) removeEdge(edge);
		for (MutableEdge edge : add) addEdge(edge);
	}

	/**
	 * Searches for edges with 0 length and deletes them, combining the vertices
	 */
	private void combineVertices() {
		// Map vertices to the vertex that should replace them;
		Map<MutableVertex, MutableVertex> replace = new HashMap<>();
		for (MutableEdge e : edges) {
			if (e.toLineSeg().length() > Vec2.EPSILON) continue;
			MutableVertex v0 = e.getStart();
			MutableVertex v1 = e.getEnd();
			
			// Replace greater hashcode vertex with smaller
			if (v0.hashCode() < v1.hashCode()) replace.put(v1, v0);
			else replace.put(v0, v1);
		}
		
		if (replace.keySet().size() == 0) {
			// No vertices to collapse
			return;
		}

		List<MutableEdge> result = new ArrayList<MutableEdge>();
		for (MutableEdge e : edges) {
			MutableVertex start = e.getStart();			
			MutableVertex replaceStart = replace.get(start);
			if (replaceStart != null) start = replaceStart;
			
			MutableVertex end = e.getEnd();
			MutableVertex replaceEnd = replace.get(end);
			if (replaceEnd != null) end = replaceEnd;
			
			if (start == end) {
				// edge should be eliminated, add nothing to result
				continue;
			}
			
			if (start != e.getStart() || end != e.getEnd()) {
				// Add clipped edge to result
				result.add(e.clip(start, end));
			} else {
				// Edge doesn't need clipping
				result.add(e);
			}
		}
		edges = result;
	}
	
	/**
	 * Adds all vertices and edges to site lists
	 * Adds all edges and sites to vertex lists
	 * Based on existing edges
	 */
	private void createLinks() {		
		for (MutableEdge edge : edges) {
			for (MutableVertex vertex : edge.getVertices()) {
				vertex.addEdge(edge);
				for (MutableSite site : edge.getSites()) {
					if (!vertex.hasSite(site)) {
						vertex.addSite(site);
						site.addVertex(vertex);
					}
				}
			}
		}
	}
	
	/**
	 * Sorts the vertex and edge lists in each site to be in counterclockwise order
	 */
	private void sortSiteLists() {	
		for (MutableSite s : sites) sortSiteLists(s);
	}	
	
	private Comparator<Vertex> getVertexOrder(Vec2 center) {
		return (vertex0, vertex1) -> {
			Vec2 v0 = vertex0.getPosition();
			Vec2 v1 = vertex1.getPosition();
			double theta1 = v0.subtract(center).angle();
			double theta2 = v1.subtract(center).angle();
			if (theta1 > theta2) return 1;
			if (theta1 < theta2) return -1;
			// consistent comparison between identical angles
			return vertex0.hashCode() - vertex1.hashCode();
		};
	}
	
	private Comparator<Edge> getEdgeOrder(Vec2 center) {
		return (edge0, edge1) -> {
			Vec2 v0 = edge0.getCenter();
			Vec2 v1 = edge1.getCenter();
			double theta1 = v0.subtract(center).angle();
			double theta2 = v1.subtract(center).angle();
			if (theta1 > theta2) return 1;
			if (theta1 < theta2) return -1;
			// consistent comparison between identical angles
			return edge0.hashCode() - edge1.hashCode();
		};
	}
	
	private void sortSiteLists(MutableSite s) {
		Vec2 center = s.pos;
		s.sortVertices(getVertexOrder(center));
		s.sortEdges(getEdgeOrder(center));
	}
	
	private void createBoundaryEdges() {
		// Create corner vertices
		List<MutableVertex> corners = new ArrayList<>(4);
		MutableVertex v00 = new MutableVertex(new Vec2(bounds.minX(), bounds.minY()), true);
		MutableVertex v10 = new MutableVertex(new Vec2(bounds.maxX(), bounds.minY()), true);
		MutableVertex v11 = new MutableVertex(new Vec2(bounds.maxX(), bounds.maxY()), true);
		MutableVertex v01 = new MutableVertex(new Vec2(bounds.minX(), bounds.maxY()), true);
		corners.add(v00);
		corners.add(v10);
		corners.add(v11);
		corners.add(v01);
		
		// Assign corner vertices to appropriate sites
		for (MutableVertex corner : corners) {
			vertices.add(corner);
			MutableSite closest = null;
			double distance = Double.MAX_VALUE;
			for (MutableSite s : sites) {
				double dist = s.pos.subtract(corner.getPosition()).length();
				if (dist < distance) {
					distance = dist;
					closest = s;
				}
			}
			corner.addSite(closest);
			closest.addVertex(corner);
			sortSiteLists(closest);
		}

		// Add new edges
		for (MutableSite s : sites) {			
			MutableVertex prev = null;
			MutableVertex last = s.getLastVertex(); 
			if (last.isBoundary()) prev = last;
			boolean modified = false;
			for (MutableVertex v : s.getVertexIterator()) {
				if (!v.isBoundary()) {
					prev = null;
					continue;
				}
				if (prev != null) {		
					MutableEdge edge = new MutableEdge(prev, v, s, null);
					v.addEdge(edge);
					prev.addEdge(edge);
					s.addEdge(edge);
					edges.add(edge);
					modified = true;
				}
				prev = v;
			}
			if (modified) sortSiteLists(s);
		}
	}
	
	/**
	 * Makes the edge and vertex lists in each site unmodifiable.
	 * Makes the edge and site lists in each vertex unmodifiable.
	 * The vertices and sites in each edge are already final. 
	 */
	private void finalizeLinks() {
		for (MutableSite s : sites) s.makeListsUnmodifiable();
		for (MutableVertex v : vertices) v.makeListsUnmodifiable();
	}
	
	private void addEdge(MutableEdge edge) {
		if (!edge.isFinished()) throw new RuntimeException("Cannot add unfinished edge");
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
	
	public Rect getBounds() {
		return bounds;
	}

	public void drawDebugState(Graphics2D g) {
		g.setFont(new Font("Consolas", Font.PLAIN, 12));
		if (this.isFinished()) {
			// Draw polygons
			Random r = new Random(0);
			for (MutableSite s : sites) {
				Path2D shape = new Path2D.Double();
				boolean started = false;
				for (MutableVertex vert : s.getVertexIterator()) {
					Vec2 v = vert.getPosition();
					if (!started) {
						shape.moveTo(v.x, v.y);
						started = true;
						continue;
					}
					shape.lineTo(v.x, v.y);
				}
				shape.closePath();
				
				g.setColor(Color.getHSBColor(r.nextFloat(), 1.0f, 0.5f + r.nextFloat()*0.5f));
				g.fill(shape);
			}
			
			// Draw vertices
//			g.setColor(Color.BLACK);
//			for (Vertex vert : vertices) {
//				Ellipse2D ellipse = new Ellipse2D.Double(vert.getPosition().x-3, vert.getPosition().y-3, 6, 6);
//				g.fill(ellipse);
//			}
			
			// Draw edges
			for (MutableEdge edge : edges) {
				Point2D v0 = edge.getStart().toPoint2D();
				Point2D v1 = edge.getEnd().toPoint2D();
				
				g.setColor(Color.WHITE);
				Line2D line = new Line2D.Double(v0, v1);
				g.draw(line);
				
				Ellipse2D ellipse0 = new Ellipse2D.Double(v0.getX()-0.75, v0.getY()-0.75, 1.5, 1.5);
				Ellipse2D ellipse1 = new Ellipse2D.Double(v1.getX()-0.75, v1.getY()-0.75, 1.5, 1.5);
				g.fill(ellipse0);
				g.fill(ellipse1);
			}
			
			// Draw sites
			g.setXORMode(Color.BLACK);
			g.setColor(Color.WHITE);
			for (Site s : sites) {
				Ellipse2D sitedot = new Ellipse2D.Double(s.pos.x-1, s.pos.y-1, 2, 2);
				g.fill(sitedot);
			}
			g.setPaintMode();
		} else {
			// Draw shore tree (edges, circle events, parabolas)
			this.shoreTree.draw(this, g);
		
			// Draw sweep line
			g.setColor(Color.YELLOW);
			Line2D line = new Line2D.Double(bounds.minX(), sweeplineY, bounds.maxX(), sweeplineY);
			g.draw(line);
			
			// Draw sites and site labels
			AffineTransform transform = g.getTransform();
			AffineTransform identity = new AffineTransform();
			
			for (Site s : sites) {
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

	private void addEvent(Event e) {
		 if (e == null) throw new RuntimeException("Cannot add null event");
		 eventQueue.offer(e);
	}
	
	private void removeEvent(Event e) {
		 if (e == null) throw new RuntimeException("Cannot remove null event");
		 eventQueue.remove(e);
	}
	
}
