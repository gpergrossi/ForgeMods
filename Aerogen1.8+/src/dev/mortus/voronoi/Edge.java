package dev.mortus.voronoi;

import dev.mortus.util.math.LineSeg;

public class Edge {
	
	protected final Site siteLeft;
	protected final Site siteRight;
	
	public final Vertex start;
	public final Vertex end;
	
	protected Edge(Vertex start, Vertex end, Site left, Site right) {
		this.start = start;
		this.end = end;
		this.siteLeft = left;
		this.siteRight = right;
	}

	public LineSeg toLineSeg() {
		if (start == null || end == null) return null;
		return new LineSeg(start.position, end.position);
	}

	public boolean isFinished() {
		return start != null && end != null;
	}
	
}
