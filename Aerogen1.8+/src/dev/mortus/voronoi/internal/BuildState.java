package dev.mortus.voronoi.internal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import dev.mortus.util.data.Pair;
import dev.mortus.util.math.geom.Circle;
import dev.mortus.util.math.geom.LineSeg;
import dev.mortus.util.math.geom.Ray;
import dev.mortus.util.math.geom.Rect;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.Edge;
import dev.mortus.voronoi.diagram.Site;
import dev.mortus.voronoi.diagram.Vertex;
import dev.mortus.voronoi.diagram.Voronoi;
import dev.mortus.voronoi.internal.Event.Type;
import dev.mortus.voronoi.internal.tree.*;


/**
 * Listened to https://www.youtube.com/watch?v=NXXivAiS59Y while coding. Recommended.
 * 
 * @author Gregary
 *
 */
public final class BuildState {

	private static final Predicate<Breakpoint> NEW_BREAKPOINTS = (bp -> bp != null && bp.edge == null);

	private Rect bounds;
	private final Queue<Event> eventQueue;
	private final ShoreTree shoreTree;
	
	private double sweeplineY = Double.NEGATIVE_INFINITY;
	private boolean debugSweeplineAdjust;
	private double backupSweeplineY;

	private int numEventsProcessed;
	
	private MutableVoronoi voronoi;
	private Set<MutableEdge> edges;
	private Set<MutableVertex> vertices;
	private Map<Vec2, MutableSite> sites;

	private boolean finished;
	
	public BuildState (Rect bounds, Vec2[] sites) {
		this.bounds = bounds;
		this.eventQueue = new PriorityQueue<Event>(sites.length*2);
		this.shoreTree = new ShoreTree();

		this.voronoi = new MutableVoronoi(bounds);
		
		this.sites = new HashMap<>(sites.length);
		this.edges = new HashSet<>(10*sites.length);
		this.vertices = new HashSet<>(8*sites.length);
		
		this.initSiteEvents(sites);
		this.finished = false;
	}
	
	
	
	

	public void processEvents(int ms) {
		if (ms == 0) {
			processNextEvent();
			return;
		}
		
		if (ms == -1) {
			while (!isFinished()) {
				processNextEvent();
			}
		}
		
		long start = System.currentTimeMillis();
		while (!isFinished() && (System.currentTimeMillis() - start) < ms) {
			processNextEvent();
		}
	}
	
	int totalCircleEvents = 0;
	int invalidCircleEvents = 0;
	
	public void processNextEvent() {
		if (!shoreTree.isInitialized()) throw new RuntimeException("Shore Tree has not yet been initialized");
		
		Event e;
		while (true) {
			e = eventQueue.poll();
			if (e == null) break;
			if (e.isValid()) break;
			invalidCircleEvents++;
		}
		
		if (e == null) {
			finish();
			return;
		}
		advanceSweepLine(e);

		// Process the event
		switch(e.getType()) {
			case SITE:		processSiteEvent(e.getSite());	break;
			case CIRCLE:	processCircleEvent(e.getArc());	break;
		}
		
		numEventsProcessed++;
		if (Voronoi.DEBUG) printDebugEvent(e);
	}	
	
	public void processNextEventVerbose() {
		boolean hold = Voronoi.DEBUG;
		Voronoi.DEBUG = true;
		processNextEvent();
		Voronoi.DEBUG = hold;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public Voronoi getDiagram() {
		return this.voronoi;
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
			for (MutableSite s : sites.values()) {
				Path2D shape = new Path2D.Double();
				boolean started = false;
				for (MutableVertex vert : s.getVertexIterable()) {
					Vec2 v = vert.toVec2();
					if (!started) {
						shape.moveTo(v.getX(), v.getY());
						started = true;
						continue;
					}
					shape.lineTo(v.getX(), v.getY());
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
			for (Site s : sites.values()) {
				Ellipse2D sitedot = new Ellipse2D.Double(s.x-1, s.y-1, 2, 2);
				g.fill(sitedot);
			}
			g.setPaintMode();
		} else {
			// Draw edges
			g.setColor(Color.WHITE);
			for (Edge edge : edges) {
				Line2D line = new Line2D.Double(edge.getStart().toPoint2D(), edge.getEnd().toPoint2D());
				g.draw(line);
			}
			
			// Draw shore tree (edges, circle events, parabolas)
			this.shoreTree.draw(this, g);
		
			// Draw sweep line
			g.setColor(Color.YELLOW);
			Line2D line = new Line2D.Double(bounds.minX(), sweeplineY, bounds.maxX(), sweeplineY);
			g.draw(line);
			
			// Draw sites and site labels
			AffineTransform transform = g.getTransform();
			AffineTransform identity = new AffineTransform();
			
			for (Site s : sites.values()) {
				Ellipse2D sitedot = new Ellipse2D.Double(s.x-1, s.y-1, 2, 2);
				g.setColor(new Color(0,128,0));
				if (pastCircleEvent(s)) g.setColor(Color.RED);				
				g.draw(sitedot);
				
				g.setTransform(identity);
				Point2D pt = new Point2D.Double(s.x, s.y);
				transform.transform(pt, pt);
				g.setColor(new Color(0,255,0));
				g.drawString(""+s.id, (int) pt.getX(), (int) pt.getY());
				g.setTransform(transform);
			}
		}
	}
	
	private boolean pastCircleEvent(Site s) {
		for (Event e : eventQueue) {
			if (e.getCircle() == null) continue;
			Circle c = e.getCircle();
			double cy = c.getY()+c.getRadius();
			if (sweeplineY < cy) continue;
			if (e.getArc().site == s) return true;
		}
		return false;
	}





	private void initSiteEvents(Vec2[] sites) {
		try {
			int id = 0;
			for(Vec2 sitePos : sites) {
				MutableSite site = new MutableSite(this.voronoi, id++, sitePos);
				this.sites.put(sitePos, site);
				addEvent(Event.createSiteEvent(site));
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Cannot build, multiple sites with same position");
		}

		// Create tree with first site
		Event e = eventQueue.poll();
		if (e == null) throw new RuntimeException("Cannot initialize diagram, no sites provided");
		shoreTree.initialize(e.getSite());
		this.numEventsProcessed = 1;
	}
	
	private void advanceSweepLine(Event e) {
		if (e == null) return;
		
		// Restore debug sweep line
		if (debugSweeplineAdjust) {
			debugSweeplineAdjust = false;
			sweeplineY = backupSweeplineY;
		}

		// Advance sweep line
		sweeplineY = e.getY();
		
		// An event may be above the sweep line by a VERY_SMALL amount if it is a circle event 
		// Such circle events are created when a site event lands on top of a breakpoint.
		if (e.is(Type.CIRCLE) && sweeplineY > e.getY()+2*Voronoi.VERY_SMALL) {
			throw new RuntimeException("Event inserted after it should have already been processed. Event="+e+", sweeplineY="+sweeplineY);
		}
	}
	
	private void printDebugEvent(Event e) {
		int siteCount = 0, circleCount = 0;
		for (Event event : eventQueue) {
			if (event.is(Event.Type.SITE)) siteCount++;
			if (event.is(Event.Type.CIRCLE)) circleCount++;
		}
		System.out.println("processed: "+numEventsProcessed+" events. "+siteCount+" site events and "+circleCount+" circle events remaining.");
		System.out.println("just processed: "+e+", next: ");
		Iterator<Event> next = eventQueue.iterator();
		while (next.hasNext()) {
			Event ev = next.next();
			System.out.println(ev);
		}
		
		TreeNode n = shoreTree.getRoot().getFirstDescendant();
		System.out.println(" ========== TREELIST ==========");
		while (n != null) {
			System.out.println(n);
			n = n.getSuccessor();
		}
		System.out.println();
	}

	private void processSiteEvent(Site site) {		
		Arc arcUnderSite = shoreTree.getArcUnderSite(this, site);
		
		Event arcCircleEvent = arcUnderSite.getCircleEvent();
		if (arcCircleEvent != null) removeEvent(arcCircleEvent);
		
		Arc newArc = arcUnderSite.insertArc(this, site);
		
		for (Arc neighbor : newArc.getNeighborArcs()) {
			Event circleEvent = neighbor.checkCircleEvent(this);
			if (circleEvent != null) {
				if (Voronoi.DEBUG) System.out.println("New circle event arising from site event check on "+neighbor);
				addEvent(circleEvent);
			}
		}
		
		Pair<Breakpoint> bps = newArc.getBreakpoints();
		bps = bps.filter(NEW_BREAKPOINTS);

		// Check for errors
		if (bps.size() == 0) throw new RuntimeException("Site event did not create any breakpoints");
		
		// Create vertex for new edges 
		Vec2 pos = bps.get(0).getPosition(this);
		MutableVertex vert = new MutableVertex(pos.getX(), pos.getY());
		
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
		// Save these, they will change and we need original values
		Pair<Arc> neighbors = arc.getNeighborArcs(); 
		Breakpoint predecessor = arc.getPredecessor();
		Breakpoint successor = arc.getSuccessor();
		
		// Step 1. Finish the edges of each breakpoint
		Vec2 predPos = predecessor.getPosition(this);
		MutableVertex sharedVertex = new MutableVertex(predPos.getX(), predPos.getY());
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
			if (newCircleEvent != null) {
				if (Voronoi.DEBUG) System.out.println("New circle event arising from circle event check on "+neighbor);
				addEvent(newCircleEvent);
			}
		}
		
		// Step 5. Form new edge
		remainingBP.edge = new MutableEdge(remainingBP, sharedVertex);
	}
	
	
	
	
	
	private static interface FinishStep {
		/**
		 * Do some work, return true if finish, otherwise keep track of own state 
		 * and return false so that this work can be resume by a later call to work()
		 * @return finished?
		 */
		public boolean work(BuildState self);
	}
	
	private static enum FinishingStep {
		
		BEGIN					("", self -> {return true;}),
		EXTENDING_EDGES 		("Extending unfinished edges", self -> self.extendUnfinishedEdges()),
		JOINING_HALF_EDGES		("Joining half edges", self -> self.joinHalfEdges()),
		CLIPPING_EDGES			("Clipping edges against bounding box", self -> self.clipEdges()),
		COMBINING_VERTICES		("Combining identical edge vertices", self -> self.combineVertices()),
		CREATING_LINKS			("Creating links between diagram elements", self -> self.createLinks()),
		SORTING_LISTS			("Sorting vertex and edge lists to counterclockwise order", self -> self.sortSiteLists()),
		CREATING_BOUNDARY		("Creating boundary edges", self -> self.createBoundaryEdges()),
		FINALIZING_LINKS		("Finalizing links between elements", self -> self.finalizeLinks()),
		DONE					("Done", self -> self.finishComplete());
		
		String message;
		FinishStep stepMethod;
		
		private FinishingStep(String msg, FinishStep stepMethod) {
			this.message = msg;
			this.stepMethod = stepMethod;
		}
		
		public static FinishingStep next(FinishingStep step) {
			int nextOrdinal = step.ordinal()+1;
			if (nextOrdinal >= values().length) return DONE;
			return values()[nextOrdinal];
		}
		
		public boolean doWork(BuildState self) {
			return stepMethod.work(self);
		}
		
	}
	
	/**
	 * Record incremental progress because finishing can take a long time and
	 * ideally the worker threads will not stall significantly when calling doWork()
	 * These allow us to return and come back later for more work
	 */
	private FinishingStep currentFinishStep = null;
	private long iterationStartTime, totalStepTimeInvested;
	
	private void finish() {
		if (finished) return;
		
		if (currentFinishStep == null) {
			if (sweeplineY < bounds.maxY()) sweeplineY = bounds.maxY();
			currentFinishStep = FinishingStep.BEGIN;
		}
		
		long startTime = System.currentTimeMillis();
		
		while (!finished) {
			iterationStartTime = System.currentTimeMillis();
			
			// Do some work on the current step
			boolean stepComplete = currentFinishStep.doWork(this);
			totalStepTimeInvested += (System.currentTimeMillis() - iterationStartTime);
			
			if (stepComplete) {
				// If done with step, move to next step
				FinishingStep previousStep = currentFinishStep;
				currentFinishStep = FinishingStep.next(currentFinishStep);
				
				// Optionally print progress information
				if (Voronoi.DEBUG_FINISH) {
					if (previousStep != FinishingStep.BEGIN) {
						System.out.println("Elapsed time: "+totalStepTimeInvested+" ms");
					}
					if (previousStep != FinishingStep.DONE) {
						System.out.println(currentFinishStep.message+"...");
					}
				}
				
				// Reset step timer
				totalStepTimeInvested = 0;
			} else {
				if (Voronoi.DEBUG_FINISH) System.out.print(".");
			}
			
			if (System.currentTimeMillis() - startTime > 200) return;
		}
	}
	
	/**
	 * Projects all unfinished edges out to the bounding box and gives them a closing vertex
	 */
	private boolean extendUnfinishedEdges() {
		for (TreeNode node : shoreTree) {
			if (!(node instanceof Breakpoint)) continue;
			Breakpoint bp = (Breakpoint) node;
			
			MutableEdge edge = bp.edge;
			if (edge == null || edge.isFinished()) {
				throw new RuntimeException("All breakpoints in shore tree should have partial edges");
			}
			
			Vec2 startPos = edge.getStart().toVec2();
			
			Ray edgeRay = new Ray(startPos, bp.getDirection());
			Pair<Vec2> intersect = bounds.intersect(edgeRay);

			Vec2 endPoint = null;
			if (bounds.contains(startPos)) {
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
			
			MutableVertex vert = new MutableVertex(endPoint.getX(), endPoint.getY(), true);
			vertices.add(vert);
			addEdge(edge.finish(vert));
			bp.edge = null;
		}
		
		return true; // Step completed
	}
	
	/**
	 * Joins all half edges into full edges.
	 * <pre>
	 * before:  A.end <-- A.start == B.start --> B.end
	 * after:   C.start -----------------------> C.end
	 * </pre>
 	 */
	private boolean joinHalfEdges() {
		Iterator<MutableEdge> edgeIterator = edges.iterator();
		while (edgeIterator.hasNext()) {
			MutableEdge e = edgeIterator.next();
			if (!e.isHalf()) continue;
			
			HalfEdge edge = (HalfEdge) e;
			HalfEdge twin = edge.getTwin();
			
			if (edge.isSecondary()) continue;
			
			edge.joinHalves();
			twin.setSecondary();
		}
		
		return true; // Step completed
	}

	private Iterator<MutableEdge> clipEdgesProgress;
	private Set<MutableEdge> validEdges;
	private Set<MutableVertex> validVertices;
	
	/**
	 * Clip edges to bounding rectangle
	 */
	private boolean clipEdges() {
		if (clipEdgesProgress == null) {
			clipEdgesProgress = edges.iterator();
			validEdges = new HashSet<>();
			validVertices = new HashSet<>();
		}
		
		while(clipEdgesProgress.hasNext()) {
			// Return to working thread and indicate that we are not yet finished
			if (System.currentTimeMillis() - iterationStartTime > 200) return false;
			
			MutableEdge edge = clipEdgesProgress.next();
			if (!edge.isFinished()) throw new RuntimeException("unfinished edge");
			
			LineSeg seg = edge.toLineSeg();
			seg = bounds.clip(seg);
			if (seg == null) continue;
			
			boolean sameStart = seg.pos.equals(edge.getStart().toVec2());
			boolean sameEnd = seg.end.equals(edge.getEnd().toVec2());
			
			MutableVertex start = edge.getStart();
			MutableVertex end = edge.getEnd();
			if (!sameStart) {
				start = new MutableVertex(seg.pos.getX(), seg.pos.getY(), true);
				validVertices.add(start);
			} else {
				validVertices.add(edge.getStart());
			}
			if (!sameEnd) {
				end = new MutableVertex(seg.end.getX(), seg.end.getY(), true);
				validVertices.add(end);
			} else {
				validVertices.add(edge.getEnd());
			}
			
			edge.redefine(start, end);
			validEdges.add(edge);
		}
		this.edges = validEdges;
		this.vertices = validVertices;
		
		return true; // Step completed
	}

	/**
	 * Combines vertices closer than Voronoi.VERY_SMALL (DANGER! VERY_SMALL must
	 * be significantly smaller than the smallest distance between sites or they
	 * will be chain combined in a nondeterministic way)
	 */
	private boolean combineVertices() {
		// Map vertices to the vertex that should replace them;
		Map<MutableVertex, MutableVertex> replace = new HashMap<>();		
		
		Iterator<MutableEdge> edgeIterator = edges.iterator();
		while (edgeIterator.hasNext()) {
			MutableEdge e = edgeIterator.next();
			
			// Edge is long enough, skip it
			if (e.toLineSeg().length() > Voronoi.VERY_SMALL) continue;
			
			// Edge is too short, remove it
			edgeIterator.remove();
			MutableVertex oldVertex = e.getEnd();
			MutableVertex newVertex = e.getStart();
			replace.put(oldVertex, newVertex);
			vertices.remove(oldVertex);
		}

		for (MutableEdge e : edges) {
			MutableVertex start = e.getStart();			
			while (replace.containsKey(start)) start = replace.get(start);
			
			MutableVertex end = e.getEnd();
			while (replace.containsKey(end)) end = replace.get(end);
			
			e.redefine(start, end);
		}
		
		return true; // Step completed
	}
	
	/**
	 * Adds all edges, vertices, and sites to each others' lists using the
	 * references already defined in edges
	 */
	private boolean createLinks() {		
		for (MutableEdge edge : edges) {
			for (MutableVertex vertex : edge.getMutableVertices()) {
				vertex.addEdge(edge);
				for (MutableSite site : edge.getMutableSites()) {
					if (!vertex.hasSite(site)) {
						vertex.addSite(site);
						site.addVertex(vertex);
					}
				}
			}
		}
		return true; // Step completed
	}
	
	/**
	 * Sorts the vertex and edge lists in each site to be in counterclockwise order
	 */
	private boolean sortSiteLists() {	
		for (MutableSite s : sites.values()) sortSiteLists(s);
		
		return true; // Step completed
	}	
	
	private Comparator<Vertex> getVertexOrder(final Map<Vertex, Double> angles) {		
		return (vertex0, vertex1) -> {
			double theta0 = angles.get(vertex0);
			double theta1 = angles.get(vertex1);
			if (theta0 > theta1) return 1;
			if (theta0 < theta1) return -1;
			// consistent comparison between identical angles
			return vertex0.hashCode() - vertex1.hashCode();
		};
	}
	
	private Comparator<Edge> getEdgeOrder(final Map<Edge, Double> angles) {
		return (edge0, edge1) -> {
			double theta0 = angles.get(edge0);
			double theta1 = angles.get(edge1);
			if (theta0 > theta1) return 1;
			if (theta0 < theta1) return -1;
			// consistent comparison between identical angles
			return edge0.hashCode() - edge1.hashCode();
		};
	}
	
	private void sortSiteLists(MutableSite s) {
		Vec2 siteCenter = s.toVec2();

		final Map<Vertex, Double> vertexAngles = new HashMap<>();
		for (Vertex vertex : s.getVertices()) {
			vertexAngles.put(vertex, vertex.toVec2().subtract(siteCenter).angle());
		}
		
		final Map<Edge, Double> edgeAngles = new HashMap<>();
		for (Edge edge : s.getEdges()) {
			edgeAngles.put(edge, edge.getCenter().subtract(siteCenter).angle());
		}
		
		s.sortVertices(getVertexOrder(vertexAngles));
		s.sortEdges(getEdgeOrder(edgeAngles));
	}
	
	/**
	 * Forms a new edge or edges for sites along the boundary of the diagram.	
	 */
	private boolean createBoundaryEdges() {
		// Create corner vertices
		MutableVertex[] corners = new MutableVertex[4];
		corners[0] = new MutableVertex(bounds.minX(), bounds.minY(), true);
		corners[1] = new MutableVertex(bounds.maxX(), bounds.minY(), true);
		corners[2] = new MutableVertex(bounds.maxX(), bounds.maxY(), true);
		corners[3] = new MutableVertex(bounds.minX(), bounds.maxY(), true);
		
		// Assign corner vertices to appropriate sites
		for (MutableVertex corner : corners) {
			vertices.add(corner);
			MutableSite closest = null;
			double distance2 = Double.MAX_VALUE;
			for (MutableSite s : sites.values()) {
				double dx = s.x - corner.x;
				double dy = s.y - corner.y;
				double dist2 = dx*dx + dy*dy;
				if (dist2 < distance2) {
					distance2 = dist2;
					closest = s;
				}
			}
			corner.addSite(closest);
			closest.addVertex(corner);
			sortSiteLists(closest);
		}

		// Add new edges
		for (MutableSite s : sites.values()) {			
			MutableVertex prev = null;
			MutableVertex last = s.getLastVertex();
			if (last == null) continue;
			if (last.isBoundary()) prev = last;
			boolean modified = false;
			for (MutableVertex v : s.getVertexIterable()) {
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
		
		return true; // Step completed
	}
	
	/**
	 * Makes the edge and vertex lists in each site unmodifiable.
	 * Makes the edge and site lists in each vertex unmodifiable.
	 * The vertices and sites in each edge are already final. 
	 */
	private boolean finalizeLinks() {
		for (MutableSite s : sites.values()) s.makeListsUnmodifiable();
		for (MutableVertex v : vertices) v.makeListsUnmodifiable();
		
		return true; // Step completed
	}

	private boolean finishComplete() {
		this.voronoi.setMutableSites(this.sites);
		this.voronoi.setMutableVertices(this.vertices);
		this.voronoi.setMutableEdges(this.edges);
		finished = true;
		
		double p = ((double) invalidCircleEvents) / ((double) totalCircleEvents) * 100.0;
		int percent = (int) Math.round(p);
		System.out.println("Invalid circle events: "+invalidCircleEvents+" / "+totalCircleEvents+" ("+percent+"%)");
		
		return true; // Step completed
	}
	
	
	
	
	
	private void addEdge(MutableEdge edge) {
		if (!edge.isFinished()) throw new RuntimeException("Cannot add unfinished edge");
		this.edges.add(edge);
	}

	private void addEvent(Event e) {
		 if (e == null) throw new RuntimeException("Cannot add null event");
		 eventQueue.offer(e);
		 if (e.is(Type.CIRCLE)) totalCircleEvents++;
	}
	
	private void removeEvent(Event e) {
		e.invalidate();
	}
	
}
