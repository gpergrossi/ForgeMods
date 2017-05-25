package dev.mortus.aerogen.world.islands;

import java.util.ArrayList;
import java.util.List;

import dev.mortus.util.math.geom.Polygon;

public class IslandCell {

	Polygon polygon;
	List<RiverCell> riverCells;
	Island island;
	
	public IslandCell(Island island, Polygon polygon) {
		this.island = island;
		this.polygon = polygon;
		this.riverCells = new ArrayList<>();
	}
	
	protected void addRiverCell(RiverCell riverCell) {
		this.riverCells.add(riverCell);
	}

	public List<RiverCell> getRiverCells() {
		return riverCells;
	}

	public Island getIsland() {
		return island;
	}
	
}
