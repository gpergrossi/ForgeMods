package dev.mortus.voronoi.diagram;

import java.util.List;

import dev.mortus.util.math.geom.Vec2;

public class Site implements Comparable<Site> {

	public final Voronoi voronoi;
	public final int id;
	public final Vec2 pos;
	
	protected boolean isFinished;
	protected boolean isClosed;
	protected List<Vertex> vertices;
	protected List<Edge> edges;

	protected Site (Voronoi voronoi, int id, Vec2 pos) {
		this.voronoi = voronoi;
		this.id = id;
		this.pos = pos;
	}
	
	public boolean isClosed() {
		if (isFinished) return isClosed;
		if (vertices == null) return false;
		for (Vertex v : vertices) {
			if (v.isBoundary) return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "Site[ID="+id+", X="+pos.x+", Y="+pos.y+"]";
	}

	@Override
	public int compareTo(Site o) {
		return pos.compareTo(o.pos);
	}
	
}