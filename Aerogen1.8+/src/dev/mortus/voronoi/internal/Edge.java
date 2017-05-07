package dev.mortus.voronoi.internal;

import dev.mortus.util.data.Pair;
import dev.mortus.util.data.storage.Storage;
import dev.mortus.util.data.storage.StorageItem;
import dev.mortus.util.math.geom.LineSeg;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.internal.shoretree.Breakpoint;

public class Edge implements StorageItem, Comparable<Edge> {
	
	private Integer storageIndex;
	private Storage<?> storage;
	
	protected Pair<Site> sites;
	protected Pair<Vertex> vertices;
	
	protected Vec2 center;
	protected LineSeg lineSeg;
	
	protected Edge(Vertex start, Vertex end, Site left, Site right) {
		this.vertices = new Pair<>(start, end);
		this.sites = new Pair<>(left, right);
	}
	
	Edge(Breakpoint bp, Vertex start) {
		this(start, null, bp.getArcRight().site, bp.getArcLeft().site);
	}
	
	void redefine(Vertex start, Vertex end) {
		if (getStart() == start && getEnd() == end) return;
		this.vertices = new Pair<>(start, end);
		this.center = null;
		this.lineSeg = null;
	}

	void combineWith(HalfEdge twin) {
		if (this.getEnd() == null) throw new RuntimeException("Cannot combine, edge has null end");
		if (twin.getEnd() == null) throw new RuntimeException("Cannot combine, twin has null end");
		this.redefine(twin.getEnd(), this.getEnd());
	}
	
	void finish(Vertex end) {
		redefine(getStart(), end);
	}
	
	boolean isHalf() {
		return false;
	}
	
	
	
	
	public LineSeg toLineSeg() {
		if (lineSeg == null) {
			Vec2 v0 = vertices.first.toVec2();
			Vec2 v1 = vertices.second.toVec2();
			lineSeg = new LineSeg(v0, v1);
		}
		return lineSeg;
	}
	
	public Vec2 getCenter() {
		if (center == null) {
			Vec2 v0 = vertices.first.toVec2();
			Vec2 v1 = vertices.second.toVec2();
			center = v0.add(v1).divide(2.0);
		}
		return center;
	}
	
	public boolean isFinished() {
		return vertices.size() == 2;
	}

	public Pair<Vertex> getVertices() { return vertices; }
	public Vertex getStart() { return vertices.first; }
	public Vertex getEnd() { return vertices.second; }
	
	public Pair<Site> getSites() { return sites; }
	public Site getSiteLeft() { return sites.first; }
	public Site getSiteRight() { return sites.second; }

	@Override
	public int compareTo(Edge o) {
		return this.hashCode() - o.hashCode();
	}

	@Override
	public void setStorageIndex(Storage<?> storage, int index) {
		if (this.storage == null || this.storage == storage) {
			this.storage = storage;
			this.storageIndex = index;
			return;
		}
		throw new RuntimeException("The storage saved is "+this.storage+", trying to store additional "+storage);
	}

	@Override
	public Integer getStorageIndex(Storage<?> storage) {
		if (this.storage == storage) return this.storageIndex;
		return null;
	}

	@Override
	public void clearStorageIndex(Storage<?> storage) {
		if (this.storage == storage) this.storage = null;
	}
		
}
