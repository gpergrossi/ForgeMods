package dev.mortus.voronoi.internal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

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
			double x1 = o1.getPos().x;
			double x2 = o2.getPos().x;
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
	private List<Vertex> vertices;
	private List<Site> sites;
	private int numSites;

	public BuildState (Voronoi voronoi, Rectangle2D bounds) {
		this.voronoi = voronoi;
		this.eventQueue = new PriorityQueue<Event>(EVENT_ORDER);
		this.shoreTree = new ShoreTree();
		
		this.bounds = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(),bounds.getHeight());
		this.edges = new ArrayList<MutableEdge>();
		this.vertices = new ArrayList<Vertex>();
		this.sites = new ArrayList<Site>();
		
		this.eventsProcessed = 0;
	}

	public void initSiteEvents(List<Site> sites) {
		if (eventsProcessed > 0) throw new RuntimeException("Already initialized.");
		numSites = sites.size();
		
		for(Site site : sites) {
			site.edges = new ArrayList<Edge>();
			site.vertices = new ArrayList<Vertex>();
			addEvent(Event.createSiteEvent(site));
		}

		// Create tree with first site
		Event e = eventQueue.poll();
		this.sites.add(e.site);
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
		sites.add(site);
		
		Arc arcUnderSite = shoreTree.getArcUnderSite(this, site);
		removeEvent(arcUnderSite.getCircleEvent());
		
		// Split the arc with two breakpoints (possibly just one) and insert an arc for the site event
		Arc newArc = arcUnderSite.insertArc(this, site);
		
		// Check for circle events on neighboring arcs
		for (Arc neighbor : newArc.getNeighborArcs()) {
			addEvent(neighbor.checkCircleEvent(this));
		}
		
		// Get pair of new breakpoints (potentially only one)
		Pair<Breakpoint> bps = newArc.getBreakpoints();
		bps = bps.filter(bp -> bp != null && bp.edge == null);

		// Check for errors
		if (bps.size() == 0) throw new RuntimeException("Site event did not create any breakpoints");
		
		// Create vertex for new edges 
		Vec2 pos = bps.get(0).getPosition(this);
		Vertex vert = new Vertex(pos);
		
		// In the case that the new site was at the same Y coordinate as a previous site,
		// there will be only one breakpoint formed. 
		if (bps.size() == 1) {
			Breakpoint bp = bps.get(0);
			bp.edge = new MutableEdge(bp, vert);
			vertices.add(vert);
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
	}
	
	private void processCircleEvent(Arc arc) {
		// Save these, they will change
		Pair<Arc> neighbors = arc.getNeighborArcs(); 
		Breakpoint predecessor = arc.getPredecessor();
		Breakpoint successor = arc.getSuccessor();
		
		// Step 1. Finish the edges of each breakpoint
		Vertex sharedVertex = new Vertex(predecessor.getPosition(this));
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
			removeEvent(neighbor.circleEvent);
			addEvent(neighbor.checkCircleEvent(this));
		}
		
		// Step 5. Form new edge
		remainingBP.edge = new MutableEdge(remainingBP, sharedVertex);
	}
	
	private void finish() {
		if (sweeplineY < bounds.getMinY()) sweeplineY = bounds.getMaxY();

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
		
		// form new edges along boundaries, including:
		// 1. create new vertices at corners of bounds (only if there isn't already a vertex there
		// 2. create edges between adjacent boundary vertices 
		
		// TODO create edge list per vertex
		// TODO create edge list and vertex list per site
		
		finished = true;
		//System.out.println("Finished. "+edges.size()+" edges");
	}

	/**
	 * Projects all unfinished edges out to the bounding box and gives them a closing vertex
	 */
	private void extendUnfinishedEdges() {		
		Rect boundsRect = new Rect(bounds);
		for (TreeNode node : shoreTree.getRoot().subtreeIterator()) {
			if (!(node instanceof Breakpoint)) continue;
			Breakpoint bp = (Breakpoint) node;
			
			MutableEdge edge = bp.edge;
			if (edge == null || edge.isFinished()) {
				throw new RuntimeException("All breakpoints in shore tree should have partial edges");
			}
			
			Vec2 endPoint = null;
			
			Ray edgeRay = new Ray(edge.start().getPosition(), bp.getDirection());
			Pair<Vec2> intersect = boundsRect.intersect(edgeRay);
			
			if (bounds.contains(edge.start().toPoint2D())) {
				// first point of intersection along ray
				if (intersect.size() != 1) {
					throw new RuntimeException("Expected 1 intersection between\n"+edgeRay+"\nand\n"+boundsRect+"\ngot\n"+intersect);
				}
				endPoint = intersect.get(0);
			} else {
				// second point of intersection along ray (if any)
				if (intersect.size() != 2 && intersect.size() != 0) {
					throw new RuntimeException("Expected 0 or 2 intersections between "+edgeRay+" and "+boundsRect);
				}
				endPoint = intersect.get(1);
			}
			if (endPoint == null) endPoint = bp.getPosition(this);
			
			Vertex vert = new Vertex(endPoint, true);
			vertices.add(vert);
			addEdge(edge.finish(vert));
			bp.edge = null;
		}
	}
	
	/**
	 * Joins all half edges into a full edge.
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
		Rect boundsRect = new Rect(bounds);
		
		for (MutableEdge edge : edges) {
			if (!edge.isFinished()) throw new RuntimeException("unfinished edge");
			
			LineSeg seg = edge.toLineSeg();
			seg = boundsRect.clip(seg);
			if (seg == null) {
				vertices.remove(edge.start());
				vertices.remove(edge.end());
				remove.add(edge);
				continue;
			}
			
			boolean sameStart = seg.pos.equals(edge.start().getPosition());
			boolean sameEnd = seg.end.equals(edge.end().getPosition());
			
			if (!sameStart || !sameEnd) {				
				Vertex start = edge.start();
				Vertex end = edge.end();
				if (!sameStart) {
					vertices.remove(start);
					start = new Vertex(seg.pos, true);
					vertices.add(start);
				}
				if (!sameEnd) {
					vertices.remove(end);
					end = new Vertex(seg.end, true);
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
		Map<Vertex, Vertex> duplicates = new HashMap<Vertex, Vertex>();
		List<MutableEdge> remove = new ArrayList<MutableEdge>();
		for (MutableEdge e : edges) {
			if (e.toLineSeg().length() > Vec2.EPSILON) continue;
			Vertex v0 = e.start();
			Vertex v1 = e.end();
			remove.add(e);
			
			// Replace greater hashcode vertex with smaller
			if (v0.hashCode() < v1.hashCode()) duplicates.put(v1, v0);
			else duplicates.put(v0, v1);
		}
		for (MutableEdge e : remove) edges.remove(e);
		remove.clear();
		
		if (duplicates.keySet().size() == 0) return;

		List<MutableEdge> add = new ArrayList<MutableEdge>();
		for (MutableEdge e : edges) {
			Vertex start = e.start();
			Vertex end = e.end();
			Vertex lookup = duplicates.get(start);
			if (lookup != null) start = lookup;
			lookup = duplicates.get(end);
			if (lookup != null) end = lookup;
			
			if (start != e.start() || end != e.end()) {
				remove.add(e);
				add.add(e.clip(start, end));
			}
		}
		for (MutableEdge e : remove) edges.remove(e);
		for (MutableEdge e : add) edges.add(e);
	}
	
	/**
	 * Adds all vertices and edges to site lists
	 * Adds all edges and sites to vertex lists
	 * Based on existing edges
	 */
	private void createLinks() {		
		for (MutableEdge e : edges) {
			Vertex start = e.start();
			start.edges.add(e);
			if (!start.sites.contains(e.sites.first)) {
				start.sites.add(e.sites.first);
				e.sites.first.vertices.add(start);
			}
			if (!start.sites.contains(e.sites.second)) {
				start.sites.add(e.sites.second);
				e.sites.second.vertices.add(start);
			}
			
			Vertex end = e.end();
			end.edges.add(e);
			if (!end.sites.contains(e.sites.first)) {
				end.sites.add(e.sites.first);
				e.sites.first.vertices.add(end);
			}
			if (!end.sites.contains(e.sites.second)) {
				end.sites.add(e.sites.second);
				e.sites.second.vertices.add(end);
			}
		}
	}
	
	/**
	 * Sorts the vertex and edge lists in each site to be in counterclockwise order
	 */
	private void sortSiteLists() {	
		for (Site s : sites) sortSiteLists(s);
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
	
	private void sortSiteLists(Site s) {
		Vec2 center = s.pos;
		s.vertices.sort(getVertexOrder(center));
		s.edges.sort(getEdgeOrder(center));
	}
	
	private void createBoundaryEdges() {
		// Create corner vertices
		List<Vertex> corners = new ArrayList<Vertex>(4);
		Vertex v00 = new Vertex(new Vec2(bounds.getMinX(), bounds.getMinY()), true);
		Vertex v10 = new Vertex(new Vec2(bounds.getMaxX(), bounds.getMinY()), true);
		Vertex v11 = new Vertex(new Vec2(bounds.getMaxX(), bounds.getMaxY()), true);
		Vertex v01 = new Vertex(new Vec2(bounds.getMinX(), bounds.getMaxY()), true);
		corners.add(v00);
		corners.add(v10);
		corners.add(v11);
		corners.add(v01);
		
		// Assign corner vertices to appropriate sites
		for (Vertex corner : corners) {
			vertices.add(corner);
			Site closest = null;
			double distance = Double.MAX_VALUE;
			for (Site s : sites) {
				double dist = s.pos.subtract(corner.getPosition()).length();
				if (dist < distance) {
					distance = dist;
					closest = s;
				}
			}
			corner.sites.add(closest);
			closest.vertices.add(corner);
			sortSiteLists(closest);
		}

		// Add new edges
		for (Site s : sites) {
			Vertex prev = null;
			Vertex last = s.vertices.get(s.vertices.size()-1); 
			if (last.isBoundary()) prev = last;
			boolean modified = false;
			for (Vertex v : s.vertices) {
				if (!v.isBoundary()) {
					prev = null;
					continue;
				}
				if (prev != null) {		
					MutableEdge edge = new MutableEdge(prev, v, s, null);
					v.edges.add(edge);
					prev.edges.add(edge);
					s.edges.add(edge);
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
		for (Site s : sites) {
			s.edges = Collections.unmodifiableList(s.edges);
			s.vertices = Collections.unmodifiableList(s.vertices);
		}
		for (Vertex v : vertices) {
			v.edges = Collections.unmodifiableList(v.edges);
			v.sites = Collections.unmodifiableList(v.sites);
		}
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
	
	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

	public void drawDebugState(Graphics2D g) {
		g.setFont(new Font("Consolas", Font.PLAIN, 6));
		if (this.isFinished()) {
			// Draw polygons
			Random r = new Random(0);
			for (Site s : sites) {
				Path2D shape = new Path2D.Double();
				boolean started = false;
				if (s.vertices.size() == 0) continue;
				for (Vertex vert : s.vertices) {
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
			g.setColor(Color.BLACK);
			for (Vertex vert : vertices) {
				Ellipse2D ellipse = new Ellipse2D.Double(vert.getPosition().x-3, vert.getPosition().y-3, 6, 6);
				g.fill(ellipse);
			}
			
			// Draw edges
			for (MutableEdge edge : edges) {
				Point2D v0 = edge.start().toPoint2D();
				Point2D v1 = edge.end().toPoint2D();
//				Point2D v2 = edge.getCenter().toPoint2D();
//				Point2D v3 = edge.getSiteLeft().pos.toPoint2D();
//				Point2D v4 = edge.getSiteRight().pos.toPoint2D();
				
//				g.setColor(Color.ORANGE);
//				Line2D lineLeft = new Line2D.Double(v2, v3);
//				g.draw(lineLeft);
//
//				g.setColor(Color.BLUE);
//				Line2D lineRight = new Line2D.Double(v2, v4);
//				g.draw(lineRight);
				
				g.setColor(Color.WHITE);
				Line2D line = new Line2D.Double(v0, v1);
				g.draw(line);
				
				Ellipse2D ellipse0 = new Ellipse2D.Double(v0.getX()-1, v0.getY()-1, 2, 2);
				Ellipse2D ellipse1 = new Ellipse2D.Double(v1.getX()-1, v1.getY()-1, 2, 2);
				g.fill(ellipse0);
				g.fill(ellipse1);
				
//				g.setColor(Color.GRAY);
//				Line2D lineBack = new Line2D.Double(v2, v0);
//				g.draw(lineBack);
				
//				g.setColor(Color.GREEN);
//				g.drawString(edge.start().debug, (int) v0.getX(), (int) v0.getY());
//				g.drawString(edge.end().debug, (int) v1.getX(), (int) v1.getY());
			}
			
			// Draw sites
			g.setColor(new Color(0, 128, 0));
			for (Site s : sites) {
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
