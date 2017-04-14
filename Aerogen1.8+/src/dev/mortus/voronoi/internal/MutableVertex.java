package dev.mortus.voronoi.internal;

import java.util.Collections;
import java.util.Iterator;

import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.Edge;
import dev.mortus.voronoi.diagram.Vertex;

public class MutableVertex extends Vertex {

	MutableVertex(double x, double y) {
		super(x, y);
	}

	MutableVertex(double x, double y, boolean isBoundary) {
		super(x, y, isBoundary);
	}

	void addEdge(MutableEdge e) {
		this.edges.add(e);
		
	}

	void addSite(MutableSite site) {
		sites.add(site);
	}

	boolean hasSite(MutableSite site) {
		return sites.contains(site);
	}
	
	Iterable<MutableEdge> getEdgeIterable() {
		Iterator<Edge> iter = edges.iterator();
	
		return new Iterable<MutableEdge>() {
			@Override
			public Iterator<MutableEdge> iterator() {
				return new Iterator<MutableEdge>() {

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public MutableEdge next() {
						return (MutableEdge) iter.next();
					}
					
				};
			}
		};
	}
	
	void makeListsUnmodifiable() {
		edges = Collections.unmodifiableList(edges);
		sites = Collections.unmodifiableList(sites);
	}

	public Vec2 toVec2() {
		return Vec2.create(x, y);
	}
	
		

}
