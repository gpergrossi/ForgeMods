package dev.mortus.voronoi;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.mortus.voronoi.internal.BuildState;

public class Voronoi {

	public static final double VERY_SMALL = 0.000001;
	public static boolean DEBUG = false;

	Rectangle2D inputBounds; // Bounds to be used in next build

	List<Site> sites; // Sites included in the last build

	public Voronoi() {
		this.inputBounds = null;
		sites = new ArrayList<Site>();
	}

	public Site addSite(Point2D point) {
		state = null;

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
		state = new BuildState(this, inputBounds);
		state.initSiteEvents(sites);
	}
	
	public void buildStep() {
		if (state == null) {
			buildInit();
			System.out.println("Build Init");
			System.out.println("\nBuild Step 0 (out of <"+state.getTheoreticalMaxSteps()+")");
			return;
		}
		if (state.hasNextEvent()) {
			System.out.println("\nBuild Step "+state.getEventsProcessed()+" (out of <"+state.getTheoreticalMaxSteps()+")");
			state.processNextEventVerbose();
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
			else state.processNextEvent();
		}
	}

	public void debugAdvanceSweepline(double v) {
		if (state != null) state.debugAdvanceSweepline(v);
	}
	
	public void draw(Graphics2D g) {
		if (inputBounds != null) g.drawRect((int) inputBounds.getX(), (int) inputBounds.getY(), (int) inputBounds.getWidth(), (int) inputBounds.getHeight());
		if (state == null) {
			for (Site site : sites) {
				Ellipse2D ellipse = new Ellipse2D.Double(site.getX()-1, site.getY()-1, 2, 2);
				g.fill(ellipse);
			}
			return;
		}
		state.drawDebugState(g);
	}

	BuildState state = null;
	
	public void build() {
		if (state == null) buildInit();

		while (state.hasNextEvent()) {
			state.processNextEvent();
		}
	}

	public List<Site> getSites() {
		return Collections.unmodifiableList(sites);
	}
	
	public void savePoints() throws IOException {
		FileOutputStream fos = new FileOutputStream("saved");
		DataOutputStream dos = new DataOutputStream(fos);
		
		dos.writeInt(sites.size());
		for (Site site : sites) {
			dos.writeDouble(site.getX());
			dos.writeDouble(site.getY());
		}

		System.out.println("Saved "+sites.size()+" sites");
		dos.close();
	}
	
	public void loadPoints() throws IOException {
		FileInputStream fis = new FileInputStream("saved");
		DataInputStream dis = new DataInputStream(fis);
		
		int numSites = dis.readInt();
		for (int i = 0; i < numSites; i++) {
			double x = dis.readDouble();
			double y = dis.readDouble();
			
			addSite(new Point2D.Double(x,y));
		}
		
		dis.close();
		System.out.println("Loaded "+numSites+" sites");
	}

	public void clearSites() {
		sites.clear();		
		inputBounds = null;
		state = null;
	}

	public void removeSite(Site site) {
		this.sites.remove(site);
		
		inputBounds = null;
		state = null;
		
		for (Site s : sites) {
			Point2D point = s.getPos();
			Rectangle2D.Double pointRect = new Rectangle2D.Double(point.getX()-5, point.getY()-5, 10, 10);
			if(inputBounds == null) {
				inputBounds = pointRect;
			} else if(!inputBounds.contains(point)) {
				Rectangle2D.union(inputBounds, pointRect, inputBounds);
			}
		}
	}
	
}
