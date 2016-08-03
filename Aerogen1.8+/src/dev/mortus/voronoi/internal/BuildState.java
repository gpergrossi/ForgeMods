package dev.mortus.voronoi.internal;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.Voronoi;

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

	public final Voronoi voronoi;
	public final Queue<Event> eventQueue;
	public final ShoreTree shoreTree;
	public double sweeplineY = Double.NaN;
	public double minX, minY, maxX, maxY;
	public final List<Site> sites;

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
			Event e = new Event(site);
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
				shoreTree.processSiteEvent(this, e.site);
				break;
			case CIRCLE:
				// remove arc
				break;
		}
	}
	
}
