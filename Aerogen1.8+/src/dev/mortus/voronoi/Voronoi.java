package dev.mortus.voronoi;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.mortus.voronoi.internal.BuildState;

public class Voronoi {

	public static final double VERY_SMALL = 0.000001;
	public static boolean DEBUG = false;

	Rectangle2D inputBounds; // Bounds to be used in next build
	boolean buildNeeded; // Have points been added or removed?

	List<Site> sites; // Sites included in the last build
	List<Edge> edges; // Edges included in the last build

	public Voronoi() {
		this.inputBounds = null;

		sites = new ArrayList<Site>();
		edges = new ArrayList<Edge>();

		buildNeeded = true;
	}

	public Site addSite(Point2D point) {
		buildNeeded = true;

		Site site = new Site(point);
		sites.add(site);
		
		Rectangle2D.Double pointRect = new Rectangle2D.Double(point.getX()-5, point.getY()-5, 10, 10);
		if(inputBounds == null) {
			inputBounds = pointRect;
		} else if(!inputBounds.contains(point)) {
			Rectangle2D.union(inputBounds, pointRect, inputBounds);
		}
		
		return site;
	}
	
	public void buildInit() {
		System.out.println("Build Init");
		
		state = new BuildState(this, inputBounds);
		state.initSiteEvents(sites);
		buildNeeded = false;
	}
	
	public void buildStep() {
		if (state == null || buildNeeded) {
			buildInit();
			System.out.println("Build Step 0 (out of <"+state.getTheoreticalMaxSteps()+")");
			return;
		}
		if (state.hasNextEvent()) {
			System.out.println("Build Step "+state.getEventsProcessed()+" (out of <"+state.getTheoreticalMaxSteps()+")");
			state.processNextEvent();
		}
	}

	public void stepBack() {
		if (state == null) return;
		int step = state.getEventsProcessed();
		if (step == 0) return;
		
		state = null;
		buildInit();
		while (state.getEventsProcessed() < step-1) {		
			System.out.println("Rewind: "+state.getEventsProcessed()+"/"+(step-1));
			if (state.getEventsProcessed() == step-1) state.processNextEvent(); 
			else state.processNextEventVerbose();
		}
	}

	public void debugAdvanceSweepline(double v) {
		if (state != null) state.debugAdvanceSweepline(v);
	}
	
	public void draw(Graphics2D g) {
		if (inputBounds != null) g.drawRect((int) inputBounds.getX(), (int) inputBounds.getY(), (int) inputBounds.getWidth(), (int) inputBounds.getHeight());
		if (state == null) return;
		state.drawDebugState(g);
	}

	BuildState state = null;
	
	public void build() {
		state = new BuildState(this, inputBounds);
		state.initSiteEvents(sites);

		while (state.hasNextEvent()) {
			state.processNextEvent();
		}
		buildNeeded = false;
	}

	public List<Site> getSites() {
		return Collections.unmodifiableList(sites);
	}
	
}
