package com.gpergrossi.aerogen.generator.data;

import java.util.ArrayList;
import java.util.List;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.features.RiverCell;
import com.gpergrossi.util.geom.shapes.Convex;
import com.gpergrossi.util.geom.shapes.Polygon;

public class IslandCell {

	Convex polygon;
	private Convex insetPolygon;
	
	List<RiverCell> riverCells;
	Island island;
	
	public IslandCell(Island island, Convex polygon) {
		this.island = island;
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
