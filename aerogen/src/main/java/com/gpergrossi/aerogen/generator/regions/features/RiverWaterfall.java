package com.gpergrossi.aerogen.generator.regions.features;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.util.geom.shapes.Line;
import com.gpergrossi.util.geom.shapes.LineSeg;
import com.gpergrossi.util.geom.shapes.Ray;
import com.gpergrossi.util.geom.vectors.Double2D;

public class RiverWaterfall {

	River river;
	RiverCell source;
	RiverCell destination;
	LineSeg edge;
	Ray location;
	
	public RiverWaterfall(River river, RiverCell source, RiverCell destination, LineSeg edge) {
		this.river = river;
		this.source = source;
		this.destination = destination;
		this.edge = edge;
	}

	public Ray getLocation() {
		if (location == null) {
			Line edgeLine = edge.toLine();
			Double2D.Mutable intersection = new Double2D.Mutable();
			LineSeg edgeSeg = null;
			for (LineSeg seg : source.getRiverCurvePost()) {
				if (seg.intersect(intersection, edgeLine)) { edgeSeg = seg; break; }
			}
			if (edgeSeg == null) {
				for (LineSeg seg : destination.getRiverCurvePre()) {
					if (seg.intersect(intersection, edgeLine)) { edgeSeg = seg; break; }
				}
			}
			Ray ray = edgeSeg.toRay();
			ray.reposition(intersection);
			location = ray;
		}
		return location;
	}
	
	public LineSeg getEdge() {
		return edge;
	}

	public boolean isSource(Island island) {
		return this.source.getIsland() == island;
	}
	
	public boolean isDestination(Island island) {
		return this.destination.getIsland() == island;
	}

	public RiverCell getSource() {
		return source;
	}

	public RiverCell getDestination() {
		return destination;
	}
}
