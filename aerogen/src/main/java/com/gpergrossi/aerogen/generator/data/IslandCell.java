package com.gpergrossi.aerogen.generator.data;

import java.util.ArrayList;
import java.util.List;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.features.RiverCell;
import com.gpergrossi.util.geom.shapes.Convex;

public class IslandCell {

	Convex polygon;
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
	
}
