package com.gpergrossi.aerogen.generator.regions.features.river;

import java.util.ArrayList;
import java.util.List;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.util.geom.shapes.LineSeg;
import com.gpergrossi.util.geom.shapes.Convex;
import com.gpergrossi.util.geom.vectors.Double2D;

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
		return islandCell.getIsland();
	}
	
	public IslandCell getIslandCell() {
		return islandCell;
	}

	public Convex getPolygon() {
		return islandCell.getPolygon();
	}
	
	public RiverCell getRiverPrevious() {
		return islandRiver.getCell(riverIndex-1);
	}
	
	public RiverCell getRiverNext() {
		return islandRiver.getCell(riverIndex+1);
	}
	
	private void genRiverCurve() {
		Double2D pt0 = this.getPolygon().getCentroid();
		Double2D pt1 = pt0, pt2 = pt0, pt3 = pt0, pt4 = pt0;
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
		
		// Do blend with previous segment
		if (pt1 != pt2) {
			riverCurvePre = new ArrayList<>();
			Double2D last = catmullRomSmooth2d(0.5f, pt0, pt1, pt2, pt3);
			for (int i = 6; i <= 10; i++) {
				Double2D pt = catmullRomSmooth2d(i*0.1f, pt0, pt1, pt2, pt3);
				LineSeg seg = new LineSeg(last.x(), last.y(), pt.x(), pt.y());
				riverCurve.add(seg);
				riverCurvePre.add(seg);
				last = pt;
			}
		}
		
		// Do blend with next segment
		if (pt2 != pt3) {
			riverCurvePost = new ArrayList<>();
			Double2D last = catmullRomSmooth2d(0.0f, pt1, pt2, pt3, pt4);
			for (int i = 1; i <= 5; i++) {
				Double2D pt = catmullRomSmooth2d(i*0.1f, pt1, pt2, pt3, pt4);
				LineSeg seg = new LineSeg(last, pt);
				riverCurve.add(seg);
				riverCurvePost.add(seg);
				last = pt;
			}
		}
	}

	private Double2D catmullRomSmooth2d(double t, Double2D pt0, Double2D pt1, Double2D pt2, Double2D pt3) {
		double x = catmullRomSmooth(t, pt0.x(), pt1.x(), pt2.x(), pt3.x());
		double y = catmullRomSmooth(t, pt0.y(), pt1.y(), pt2.y(), pt3.y());
		return new Double2D(x, y);
	}
	
	/**
	 * Thanks to Richard Hawkes
	 * http://hawkesy.blogspot.com/2010/05/catmull-rom-spline-curve-implementation.html
	 */
	private double catmullRomSmooth(double t, double x0, double x1, double x2, double x3) {
		return 0.5 * ((2*x1) + (x2-x0)*t + (2*x0-5*x1+4*x2-x3)*t*t + (-x0+3*x1-3*x2+x3)*t*t*t);
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
		Double2D queryPt = new Double2D(x, z);
		Double2D.Mutable scratch = new Double2D.Mutable();
		
		for (LineSeg line : riverCurve) {
			double closestDist = line.closestPoint(queryPt, scratch);
			minDist = Math.min(minDist, closestDist);
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
