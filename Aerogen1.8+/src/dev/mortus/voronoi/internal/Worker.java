package dev.mortus.voronoi.internal;

import dev.mortus.util.math.geom.Rect;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.Voronoi;
import dev.mortus.voronoi.exception.UnfinishedStateException;

public class Worker {

	BuildState state;

	Rect bounds;
	Vec2[] siteArray;
	
	public Worker(Rect bounds, Vec2[] siteArray) {
		this.bounds = bounds;
		this.siteArray = siteArray;
	}

	public boolean isDone() {
		if (state == null) return false;
		return state.isFinished();
	}
	
	public double getProgressEstimate() {
		if (isDone()) return 1.0;
		return ((double) state.getNumEventsProcessed()) / ((double) state.getTheoreticalMaxSteps());
	}

	/**
	 * Do at least ms milliseconds of work. Will often exceed this value.<pre>
	 * If ms == 0, doWork will return after the smallest amount of progress is made.
	 * If ms == -1, doWork will not return until finished.</pre>
	 * @param ms
	 */
	public void doWork(int ms) {
		if (state == null) {
			state = new BuildState(bounds, siteArray);
			return;
		}
		state.processEvents(ms);
	}

	public Voronoi getResult() {
		if (!isDone()) throw new UnfinishedStateException();
		return state.getDiagram();
	}

}