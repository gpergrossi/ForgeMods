package com.gpergrossi.aerogen.generator.islands;

import java.util.ArrayList;
import java.util.List;

import com.gpergrossi.aerogen.generator.regions.features.river.RiverCell;
import com.gpergrossi.util.geom.shapes.Convex;
import com.gpergrossi.util.geom.shapes.Polygon;
import com.gpergrossi.voronoi.Site;

public class IslandCell {

	Island island;
	Site voronoiSite;

	Convex polygon;
	private Convex insetPolygon;
	
	List<RiverCell> riverCells;
	
	public IslandCell(Island island, Site voronoiSite) {
		this.island = island;
		this.voronoiSite = voronoiSite;
		this.polygon = voronoiSite.getPolygon();
		this.riverCells = new ArrayList<>();
	}
	
	public IslandCell(Island island, Convex polygon) {
		this.island = island;
		this.voronoiSite = null;
		this.polygon = polygon;
		this.riverCells = new ArrayList<>();
	}
	
	public void addRiverCell(RiverCell riverCell) {
		this.riverCells.add(riverCell);
	}

	public List<RiverCell> getRiverCells() {
		return riverCells;
	}

	public Island getIsland() {
		return island;
	}

	public Site getVoronoiSite() {
		return voronoiSite;
	}
	
	public Convex getPolygon() {
		return polygon;
	}
	
	public Convex getInsetPolygon() {
		if (insetPolygon == null) {
			Polygon inset = null;
			try {
				inset = getPolygon().inset(2);
			} catch (RuntimeException e) {}
			if (inset instanceof Convex) insetPolygon = (Convex) inset;
		}
		return insetPolygon;
	}
	
}
