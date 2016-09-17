package dev.mortus.voronoi;

import dev.mortus.voronoi.internal.tree.Breakpoint;

public class Edge {
	
	Site siteLeft, siteRight;
	
	Vertex start;
	Vertex end;
	
	public Edge(Breakpoint bp) {
		this.siteLeft = bp.arcLeft.site;
		this.siteRight = bp.arcRight.site;
	}
	
	public Vertex getStart() {
		return start;
	}
	
	public boolean isFinished() {
		return end != null;
	}
	 public Vertex getEnd() {
		 return end;
	 }

	public void finish(Vertex vertex) {
		this.end = vertex;
	}

	public void start(Vertex vertex) {
		this.start = vertex;		
	}
	
}
