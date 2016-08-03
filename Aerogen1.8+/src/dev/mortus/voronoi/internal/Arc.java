package dev.mortus.voronoi.internal;

import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.internal.MathUtil.Parabola;
import dev.mortus.voronoi.internal.MathUtil.Vec2;

public final class Arc {
	
	public final Site site;
	
	public Arc(Site site) {
		this.site = site;
	}

	public boolean equals(Arc o) {
		return site.equals(o.site);
	}
	
	public Parabola getParabola(double sweeplineY) {
		return Parabola.fromPointAndLine(new Vec2(site.getPos()), sweeplineY);
	}
	
}
