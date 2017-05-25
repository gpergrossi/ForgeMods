package dev.mortus.aerogen.world.islands;

import java.util.ArrayList;
import java.util.List;

import dev.mortus.util.math.geom.LineSeg;
import dev.mortus.util.math.geom.Polygon;
import dev.mortus.util.math.geom.Vec2;

public class RiverCell {
	
	River islandRiver;
	IslandCell islandCell;
	int riverIndex;
	
	RiverWaterfall waterfallOut;
	RiverWaterfall waterfallIn;
	
	private List<LineSeg> riverCurve;
	private List<LineSeg> riverCurvePre;
	private List<LineSeg> riverCurvePost;

	protected RiverCell(River islandRiver, IslandCell cell) {
		this.islandRiver = islandRiver;
		this.islandCell = cell;
	}

	protected void setIndex(int index) {
		this.riverIndex = index;
	}

	public Island getIsland() {
		return islandCell.island;
	}
	
	public IslandCell getIslandCell() {
		return islandCell;
	}

	public Polygon getPolygon() {
		return islandCell.polygon;
	}
	
	public RiverCell getRiverPrevious() {
		return islandRiver.getCell(riverIndex-1);
	}
	
	public RiverCell getRiverNext() {
		return islandRiver.getCell(riverIndex+1);
	}
	
	private void genRiverCurve() {
		Vec2 pt0 = this.getPolygon().getCentroid();
		Vec2 pt1 = pt0, pt2 = pt0, pt3 = pt0, pt4 = pt0;
		RiverCell prev = getRiverPrevious(); 
		if (prev != null) {
			pt0 = pt1 = prev.getPolygon().getCentroid();
			prev = prev.getRiverPrevious();		
			if (prev != null) pt0 = prev.getPolygon().getCentroid();
		}
		RiverCell next = getRiverNext(); 
		if (next != null) {
			pt4 = pt3 = next.getPolygon().getCentroid();
			next = next.getRiverNext();		
			if (next != null) pt4 = next.getPolygon().getCentroid();
		}

		riverCurve = new ArrayList<>();
		
		// Do previous blend
		if (pt1 != pt2) {
			riverCurvePre = new ArrayList<>();
			Vec2 last = catmullRomSmooth2d(0.5f, pt0, pt1, pt2, pt3);
			for (int i = 6; i <= 10; i++) {
				Vec2 pt = catmullRomSmooth2d(i*0.1f, pt0, pt1, pt2, pt3);
				LineSeg seg = new LineSeg(last.x(), last.y(), pt.x(), pt.y());
				riverCurve.add(seg);
				riverCurvePre.add(seg);
				last = pt;
			}
		}
		
		// Do next blend
		if (pt2 != pt3) {
			riverCurvePost = new ArrayList<>();
			Vec2 last = catmullRomSmooth2d(0.0f, pt1, pt2, pt3, pt4);
			for (int i = 1; i <= 5; i++) {
				Vec2 pt = catmullRomSmooth2d(i*0.1f, pt1, pt2, pt3, pt4);
				LineSeg seg = new LineSeg(last.x(), last.y(), pt.x(), pt.y());
				riverCurve.add(seg);
				riverCurvePost.add(seg);
				last = pt;
			}
		}
	}

	private Vec2 catmullRomSmooth2d(float t, Vec2 pt0, Vec2 pt1, Vec2 pt2, Vec2 pt3) {
		double x = catmullRomSmooth(t, (float) pt0.x(), (float) pt1.x(), (float) pt2.x(), (float) pt3.x());
		double y = catmullRomSmooth(t, (float) pt0.y(), (float) pt1.y(), (float) pt2.y(), (float) pt3.y());
		return new Vec2(x, y);
	}
	
	/**
	 * Thanks to Richard Hawkes
	 * http://hawkesy.blogspot.com/2010/05/catmull-rom-spline-curve-implementation.html
	 */
	private double catmullRomSmooth(float t, float x0, float x1, float x2, float x3) {
		return 0.5f * ((2*x1) + (x2-x0)*t + (2*x0-5*x1+4*x2-x3)*t*t + (-x0+3*x1-3*x2+x3)*t*t*t);
	}

	public List<LineSeg> getRiverCurve() {
		if (riverCurve == null) genRiverCurve();
		return riverCurve;
	}
	
	public List<LineSeg> getRiverCurvePre() {
		if (riverCurve == null) genRiverCurve();
		return riverCurvePre;
	}
	
	public List<LineSeg> getRiverCurvePost() {
		if (riverCurve == null) genRiverCurve();
		return riverCurvePost;
	}
	
	public double minDistToRiver(float x, float z) {
		getRiverCurve();
		
		double minDist = Double.POSITIVE_INFINITY;
		Vec2 queryPt = new Vec2(x, z);
		Vec2 scratch = new Vec2(0, 0);
		
		for (LineSeg line : riverCurve) {
			minDist = Math.min(minDist, line.closestPoint(queryPt, scratch));
		}
		
		return minDist;
	}

	public int getRiverIndex() {
		return riverIndex;
	}

	public River getRiver() {
		return islandRiver;
	}

	public void getWaterfalls(List<RiverWaterfall> waterfalls) {
		if (waterfallIn != null && !waterfalls.contains(waterfallIn)) waterfalls.add(waterfallIn);
		if (waterfallOut != null && !waterfalls.contains(waterfallOut)) waterfalls.add(waterfallOut);
	}

	public boolean hasWaterfall() {
		return (waterfallIn != null || waterfallOut != null);
	}

	public boolean hasWaterfallSource() {
		return (waterfallOut != null);
	}

	public boolean hasWaterfallDestination() {
		return (waterfallIn != null);
	}
	
}
