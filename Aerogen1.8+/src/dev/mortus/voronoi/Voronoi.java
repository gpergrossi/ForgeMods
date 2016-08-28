package dev.mortus.voronoi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import dev.mortus.voronoi.internal.BuildState;

public class Voronoi {

	static final double VERY_SMALL = 0.000001;

	List<Point2D> inputPoints; // Points that have not yet been added
	Rectangle2D inputBounds; // Bounds to be used in next build
	boolean buildNeeded; // Have points been added or removed?

	List<Site> sites; // Sites included in the last build
	List<Edge> edges; // Edges included in the last build

	public Voronoi() {
		this.inputPoints = new ArrayList<Point2D>();
		this.inputBounds = null;

		sites = new ArrayList<Site>();
		edges = new ArrayList<Edge>();

		buildNeeded = true;
	}

	public void add(Point2D point) {
		buildNeeded = true;

		inputPoints.add(new Point2D.Double(point.getX(), point.getY()));
		Rectangle2D.Double pointRect = new Rectangle2D.Double(point.getX(), point.getY(), 0, 0);

		if(inputBounds == null) {
			inputBounds = pointRect;
		} else if(!inputBounds.contains(point)) {
			Rectangle2D.union(inputBounds, pointRect, inputBounds);
		}
	}
	
	public void buildInit() {
		System.out.println("Build Init");
		
		unbuild();

		state = new BuildState(this, inputBounds);
		
		state.addSiteEvents(inputPoints);
	}
	
	public void buildStep() {
		if (state == null) buildInit();
		if (state.hasNextEvent()) {
			System.out.println("Build Step");
			state.processNextEvent();
		}
	}
	

	public void sweep(double v) {
		if (state != null) state.debugAdvanceSweepline(v);
	}
	
	public void draw(Graphics2D g) {
		if (inputBounds != null) g.drawRect((int) inputBounds.getX(), (int) inputBounds.getY(), (int) inputBounds.getWidth(), (int) inputBounds.getHeight());
		
		if (state == null) return;
		state.drawDebugState(g);
	}

	BuildState state = null;
	
	public void build() {
		unbuild();

		state = new BuildState(this, inputBounds);
		
		state.addSiteEvents(inputPoints);

		while (state.hasNextEvent()) {
			state.processNextEvent();
		}

		buildNeeded = false;
	}

	private void unbuild() {
		for(Site site : sites) {
			add(site.position);
		}
		sites.clear();
		edges.clear();
		buildNeeded = true;
	}

	
}
