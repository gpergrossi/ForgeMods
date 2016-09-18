package dev.mortus.voronoi;

import dev.mortus.util.math.LineSeg;
import dev.mortus.voronoi.internal.tree.Breakpoint;

public class Edge {
	
	Site siteLeft, siteRight;
	
	private Vertex start;
	private Vertex end;
	
	public Edge(Breakpoint bp) {
		this.siteLeft = bp.arcLeft.site;
		this.siteRight = bp.arcRight.site;
	}

	public boolean isFinished() {
		return end != null;
	}
	
	public Vertex start() {
		return start;		
	}

	public void start(Vertex vertex) {
		this.start = vertex;		
	}
	
    public Vertex end() {
    	return end;
	}

	public void end(Vertex vertex) {
		this.end = vertex;
	}
	
	public LineSeg toLineSeg() {
		if (start == null || end == null) return null;
		return new LineSeg(start.position, end.position);
	}
	
}
