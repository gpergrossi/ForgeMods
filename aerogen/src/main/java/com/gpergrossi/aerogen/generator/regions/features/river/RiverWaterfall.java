package com.gpergrossi.aerogen.generator.regions.features.river;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.util.geom.shapes.Line;
import com.gpergrossi.util.geom.shapes.LineSeg;
import com.gpergrossi.util.geom.shapes.Ray;
import com.gpergrossi.util.geom.vectors.Double2D;

import jline.internal.Log;

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
		
		if (source.isWaterfallSource()) throw new IllegalStateException("River cells can only have one waterfall!");
		source.setWaterfallOut(this);
		destination.setWaterfallIn(this);
	}

	public Ray getLocation() {
		if (location == null) {
			Line edgeLine = edge.toLine();
			Double2D.Mutable intersection = new Double2D.Mutable();
			LineSeg riverSeg = null;
			for (LineSeg seg : source.getRiverCurvePost()) {
				if (seg.intersect(intersection, edgeLine)) { riverSeg = seg; break; }
			}
			if (riverSeg == null) {
				for (LineSeg seg : destination.getRiverCurvePre()) {
					if (seg.intersect(intersection, edgeLine)) { riverSeg = seg; break; }
				}
			}
			// Bad default
			if (riverSeg == null) {
				Log.warn("Bad waterfall position");
				Double2D.Mutable midpoint = new Double2D.Mutable();
				edge.getMidpoint(midpoint);
				Double2D pos = midpoint.immutable();
				Double2D dir = edge.getDirection();
				dir = dir.rotate(-90.0*Math.PI/180.0);
				riverSeg = new LineSeg(pos, pos.add(dir.multiply(4)));
			}
			location = riverSeg.toRay().reposition(intersection);
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
