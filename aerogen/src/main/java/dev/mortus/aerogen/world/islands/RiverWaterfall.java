package dev.mortus.aerogen.world.islands;

import dev.mortus.util.math.geom.Line;
import dev.mortus.util.math.geom.LineSeg;
import dev.mortus.util.math.geom.Ray;
import dev.mortus.util.math.geom.Vec2;

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
			Vec2 intersection = new Vec2(0, 0);
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
	
}
