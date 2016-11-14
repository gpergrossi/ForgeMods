package dev.mortus.voronoi;

import dev.mortus.util.math.Vec2;

public class Site {

	public final Voronoi voronoi;
	public final int id;
	public final Vec2 pos;

	Site (Voronoi v, int id, Vec2 pos) {
		this.voronoi = v;
		this.id = id;
		this.pos = pos;
	}
	
//	public boolean equals(Site o) {
//		if (voronoi != o.voronoi) return false;
//		if (id != o.id) return false;
//		return true;
//	}
	
	@Override
	public String toString() {
		return "Site[ID="+id+", X="+pos.x+", Y="+pos.y+"]";
	}
	
}