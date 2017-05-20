package dev.mortus.aerogen.regions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import dev.mortus.aerogen.cells.Cell;
import dev.mortus.aerogen.cells.InfiniteVoronoi;
import dev.mortus.util.math.geom.Rect;

public class RegionManager extends InfiniteVoronoi {

	final double regionSize;
	final double cellSize;
	
	public RegionManager() {
		this(512, 64, 875849390123L);
	}
	
	public RegionManager(double regionSize, double cellSize, long seed) {
		super(regionSize, seed, 256);
		this.cellSize = cellSize;
		this.regionSize = regionSize;
	}

	public void getAll(List<Region> output, int minX, int minY, int maxX, int maxY) {
		List<Cell> cells = new ArrayList<>();
		getCells(new Rect(minX, minY, maxX-minX, maxY-minY), cells);
		
		for (Cell c : cells) {
			Region r = getRegionForCell(c);
			if (output.contains(r)) continue;
			output.add(r);
		}
	}

	private Region getRegionForCell(Cell cell) {
		Region r = (Region) cell.data;
		if (r == null) {
			r = new Region(this, cell);
			cell.data = r;
		}
		return r;
	}
	
	public double getRegionSize() {
		return regionSize;
	}
	
	public Region getRegion(int x, int y) {
		return getRegionForCell(getCell(new Point(x, y)));
	}
	
}
