package com.gpergrossi.aerogen.generator.regions.features.river;

import java.util.ArrayList;
import java.util.List;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.util.geom.shapes.LineSeg;
import com.gpergrossi.util.geom.shapes.Spline;
import com.gpergrossi.util.geom.shapes.Convex;
import com.gpergrossi.util.geom.vectors.Double2D;

public class RiverCell {
	
	River islandRiver;
	IslandCell islandCell;
	int riverIndex;
	
	private RiverWaterfall waterfallOut;
	private RiverWaterfall waterfallIn;
	
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
	
	public RiverCell getRiverCellPrevious() {
		return islandRiver.getCell(riverIndex-1);
	}
	
	public RiverCell getRiverCellNext() {
		return islandRiver.getCell(riverIndex+1);
	}
	
	private void genRiverCurve() {
		RiverCell cell2 = this;
		RiverCell cell1 = cell2.getRiverCellPrevious();
		RiverCell cell0 = cell1 == null ? null : cell1.getRiverCellPrevious();
		RiverCell cell3 = cell2.getRiverCellNext();
		RiverCell cell4 = cell3 == null ? null : cell3.getRiverCellNext();
		
		Spline spline = new Spline();
		spline.setCatmullRomAlpha(0);
		
		if (cell0 != null) spline.addGuidePoint(-2, cell0.getPolygon().getCentroid());
		if (cell1 != null) {
			if (cell1.waterfallIn != null) {
				Double2D.Mutable midpoint = new Double2D.Mutable();
				cell1.waterfallIn.getEdge().getMidpoint(midpoint);
				spline.addGuidePoint(-1.5, midpoint);
			}
			spline.addGuidePoint(-1, cell1.getPolygon().getCentroid());
		}
		if (this.waterfallIn != null) {
			Double2D.Mutable midpoint = new Double2D.Mutable();
			this.waterfallIn.getEdge().getMidpoint(midpoint);
			spline.addGuidePoint(-0.5, midpoint);
		}
		spline.addGuidePoint(0, cell2.getPolygon().getCentroid());
		if (this.waterfallOut != null) {
			Double2D.Mutable midpoint = new Double2D.Mutable();
			this.waterfallOut.getEdge().getMidpoint(midpoint);
			spline.addGuidePoint(0.5, midpoint);
		}
		if (cell3 != null) {
			spline.addGuidePoint(1, cell3.getPolygon().getCentroid());
			if (cell3.waterfallOut != null) {
				Double2D.Mutable midpoint = new Double2D.Mutable();
				cell3.waterfallOut.getEdge().getMidpoint(midpoint);
				spline.addGuidePoint(1.5, midpoint);
			}
		}
		if (cell4 != null) spline.addGuidePoint(2, cell4.getPolygon().getCentroid());

		riverCurve = new ArrayList<>();
		
		// Do blend with previous segment
		if (cell1 != null) {
			riverCurvePre = new ArrayList<>();
			Double2D.Mutable pt = new Double2D.Mutable();
			Double2D last = null;
			for (int i = 0; i <= 5; i++) {
				spline.getPoint(pt, i*0.1-0.5);
				if (last != null) {
					LineSeg seg = new LineSeg(last.x(), last.y(), pt.x(), pt.y());
					riverCurve.add(seg);
					riverCurvePre.add(seg);
				}
				last = pt.immutable();
			}
		}
		
		// Do blend with next segment
		if (cell3 != null) {
			riverCurvePost = new ArrayList<>();
			Double2D.Mutable pt = new Double2D.Mutable();
			Double2D last = null;
			for (int i = 0; i <= 5; i++) {
				spline.getPoint(pt, i*0.1);
				if (last != null) {
					LineSeg seg = new LineSeg(last.x(), last.y(), pt.x(), pt.y());
					riverCurve.add(seg);
					riverCurvePost.add(seg);
				}
				last = pt.immutable();
			}
		}
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

	public boolean isWaterfallSource() {
		return (waterfallOut != null);
	}

	public boolean isWaterfallDestination() {
		return (waterfallIn != null);
	}

	public void setWaterfallOut(RiverWaterfall waterfallOut) {
		this.waterfallOut = waterfallOut;
	}

	public void setWaterfallIn(RiverWaterfall waterfallIn) {
		this.waterfallIn = waterfallIn;
	}
	
}
