package dev.mortus.voronoi.internal;

import dev.mortus.voronoi.diagram.Voronoi;
import dev.mortus.voronoi.diagram.VoronoiBuilder.InitialState;
import dev.mortus.voronoi.exception.UnfinishedStateException;

public class Worker {

	InitialState init;
	BuildState state;

	public Worker(InitialState init) {
		this.init = init;
	}

	public boolean isDone() {
		if (state == null) return false;
		return state.isFinished();
	}

	public void doWork() {
		if (state == null) {
			state = new BuildState(init);
			return;
		}
		state.processNextEvent();
	}

	public Voronoi getResult() {
		if (!isDone()) throw new UnfinishedStateException();
		return init.voronoi;
	}

}