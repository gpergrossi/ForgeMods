package dev.mortus.voronoi;

import java.util.List;

import dev.mortus.util.math.Vec2;

public class Site implements Comparable<Site> {

	public final Voronoi voronoi;
	public final int id;
	public final Vec2 pos;
	
	public List<Vertex> vertices;
	public List<Edge> edges;

	Site (Voronoi v, int id, Vec2 pos) {
		this.voronoi = v;
		this.id = id;
		this.pos = pos;
	}
	
	/**
	 * 
	 */
	public boolean isClosed() {
		if (!voronoi.isComplete()) return false;
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