package dev.mortus.aerogen.cells;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.mortus.util.math.geom.Polygon;
import dev.mortus.util.math.geom.Rect;

public class InfiniteVoronoi {
	
	long globalSeed;
	double gridSize;
	
	int cacheSize;
	Map<Point, Cell> cellCache;

	public InfiniteVoronoi(double gridSize, long seed, int cacheSize) {
		this.gridSize = gridSize;
		this.globalSeed = seed;

		this.cacheSize = cacheSize;
		this.cellCache = new HashMap<Point, Cell>(cacheSize);
	}
	
	/**
	 * Gets a range of cells. Does init(). Does reserve().
	 * All cells returned must call cell.release() in order to be removed from the cellCache.
	 */
	public void getCells(Rect bounds, List<Cell> output) {
		int minCellX = (int) Math.floor(bounds.minX() / gridSize) - 2;
		int minCellY = (int) Math.floor(bounds.minY() / gridSize) - 2;
		int maxCellX = (int) Math.floor(bounds.maxX() / gridSize) + 2;
		int maxCellY = (int) Math.floor(bounds.maxY() / gridSize) + 2;
		Cell[] cells = Cell.initRange(this, minCellX, minCellY, maxCellX, maxCellY, true);
		
		for (Cell cell : cells) {
			if (output.contains(cell)) continue;
			Polygon poly = cell.getPolygon();
			if (poly.intersects(bounds)) output.add(cell);
		}
	}
	
	/**
	 * Gets a cell. Does init(). Does reserve().
	 * Cells returned must call cell.release() in order to be removed from the cellCache.
	 */
	public Cell getCell(double x, double y) {
		int cellX = (int) Math.floor(x / gridSize);
		int cellY = (int) Math.floor(y / gridSize);
		
		double lowestDistance = Double.MAX_VALUE;
		Cell winner = null;

		for (int iy = -2; iy <= 2; iy++) {
			for (int ix = -2; ix <= 2; ix++) {
				Cell cell = peakCell(new Point(cellX+ix, cellY+iy));
				double dist = cell.site.distanceTo(x, y);
				if (dist < lowestDistance) {
					lowestDistance = dist;
					winner = cell;
				}
			}	
		}
		
		winner.reserve();
		return winner;
	}

	/**
	 * Gets a cell. Does init(). Does reserve().
	 * Cells returned must call cell.release() in order to be removed from the cellCache.
	 */
	public synchronized Cell getCell(Point cellKey) {
		return getCell(cellKey, true);
	}
	
	/**
	 * Gets a cell. Does not init(). Does not reserve().
	 * @param cellKey
	 * @return
	 */
	public synchronized Cell peakCell(Point cellKey) {
		return getCell(cellKey, false);
	}
	
	private synchronized Cell getCell(Point cellKey, boolean reserve) {
		Cell cell = cellCache.get(cellKey);
		if (cell == null) cell = new Cell(this, cellKey);
		if (reserve) {
			cell.reserve();
			cell.init();
		}
		return cell;
	}

	public boolean isFull() {
		return cellCache.size() >= cacheSize;
	}
	
}
