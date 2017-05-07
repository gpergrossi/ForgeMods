package dev.mortus.voronoi.internal;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.mortus.util.math.geom.Vec2;
import dev.mortus.util.data.storage.Storage;
import dev.mortus.util.data.storage.StorageItem;
import dev.mortus.voronoi.diagram.Voronoi;

public class Vertex implements Comparable<Vertex>, StorageItem {

	public static final double VERY_SMALL_2 = Voronoi.VERY_SMALL * Voronoi.VERY_SMALL;

	public final boolean isBoundary;
	public final double x, y;
	
	protected List<Edge> edges;
	protected List<Site> sites;
	
	protected String debug = "";
	
	private Integer storageIndex;
	private Storage<?> storage;

	Vertex(double x, double y, boolean isBoundary) {
		this.x = x;
		this.y = y;
		this.isBoundary = isBoundary;
		this.edges = new ArrayList<Edge>();
		this.sites = new ArrayList<Site>();
	}
	
	Vertex(double x, double y) {
		this(x, y, false);
	}

	public Point2D toPoint2D() {
		return new Point2D.Double(x, y);
	}

	public Vec2 toVec2() {
		return Vec2.create(x, y);
	}

	void addEdge(Edge e) {
		this.edges.add(e);
	}

	void addSite(Site site) {
		sites.add(site);
	}

	boolean hasSite(Site site) {
		return sites.contains(site);
	}
	
	void makeListsUnmodifiable() {
		edges = Collections.unmodifiableList(edges);
		sites = Collections.unmodifiableList(sites);
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
	
	@Override
	public int compareTo(Vertex o) {
		if (y < o.y) return -1;
		if (y > o.y) return 1;
		
		if (x < o.x) return -1;
		if (x > o.x) return 1;
		
		return Integer.compare(this.hashCode(), o.hashCode());
	}

	@Override
	public String toString() {
		return "Vertex[x="+x+", y="+y+"]";
	}

}
