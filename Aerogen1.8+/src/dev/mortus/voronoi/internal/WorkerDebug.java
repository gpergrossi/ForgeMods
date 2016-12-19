package dev.mortus.voronoi.internal;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.VoronoiBuilder.InitialState;

public class WorkerDebug extends Worker {

	public WorkerDebug(InitialState init) {
		super(init);
	}

	public void doWorkVerbose() {
		if (state == null) {
			state = new BuildState(init);
			System.out.println("Build Init");
			System.out.println("\nBuild Step 0 (out of <"+state.getTheoreticalMaxSteps()+")");
			return;
		}
		System.out.println("\nBuild Step "+state.getNumEventsProcessed()+" (out of <"+state.getTheoreticalMaxSteps()+")");
		state.processNextEvent();
	}

	public void stepBack() {
		if (state == null) return;
		int step = state.getNumEventsProcessed();
		if (step == 0) return;
		
		state = new BuildState(init);

		while (state.getNumEventsProcessed() < step-1) {		
			System.out.println("Rewind: "+state.getNumEventsProcessed()+"/"+(step-1));
			if (state.getNumEventsProcessed() == step-1) state.processNextEvent(); 
			else state.processNextEvent();
		}
	}

	public void debugAdvanceSweepline(double v) {
		if (state != null) state.debugAdvanceSweepline(v);
	}
	
	public void debugDraw(Graphics2D g) {
		if (state == null || !state.isFinished()) {
			Rectangle2D rect2d = init.bounds.toRectangle2D();
			g.draw(rect2d);
		}
		if (state == null) {
			for (Vec2 site : init.sites) {
				Ellipse2D ellipse = new Ellipse2D.Double(site.x-1, site.y-1, 2, 2);
				g.fill(ellipse);
			}
			return;
		}
		state.drawDebugState(g);
	}
	
}
