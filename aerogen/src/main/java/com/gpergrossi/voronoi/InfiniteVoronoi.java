package com.gpergrossi.voronoi;

import java.util.List;
import com.gpergrossi.util.data.Large2DArray;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.util.geom.shapes.Convex;
import com.gpergrossi.util.geom.shapes.Rect;

public class InfiniteVoronoi {
	
	public final long seed;
	public final double gridSize;
	
	Large2DArray<InfiniteCell> cellCache;

	public InfiniteVoronoi(double gridSize, long seed) {
		this.gridSize = gridSize;
		this.seed = seed;

		this.cellCache = new Large2DArray<>(t -> new InfiniteCell[t]);
	}
	
	/**
	 * Gets a range of cells. Does init(). Does reserve().
	 * All cells returned must call cell.release() in order to be removed from the cellCache.
	 */
	public synchronized void getCells(Int2DRange range, List<InfiniteCell> output) {
		int minCellX = (int) Math.floor(range.minX / gridSize) - 2;
		int minCellY = (int) Math.floor(range.minY / gridSize) - 2;
		int maxCellX = (int) Math.floor(range.maxX / gridSize) + 2;
		int maxCellY = (int) Math.floor(range.maxY / gridSize) + 2;
		InfiniteCell[] cells = InfiniteCell.initRange(this, minCellX, minCellY, maxCellX, maxCellY, true);
		
		Rect bounds = new Rect(range.minX, range.minY, range.maxX-range.minX+1, range.maxY-range.minY+1);
		
		for (InfiniteCell cell : cells) {
			if (output.contains(cell)) continue;
			Convex poly = cell.getPolygon();
			if (poly.intersects(bounds)) output.add(cell);
		}
	}
	
	/**
	 * Gets a cell. Does init(). Does reserve().
	 * Cells returned must call cell.release() in order to be removed from the cellCache.
	 */
	public synchronized InfiniteCell getCell(double x, double y) {
		int cellX = (int) Math.floor(x / gridSize);
		int cellY = (int) Math.floor(y / gridSize);
		
		double lowestDistance = Double.MAX_VALUE;
		InfiniteCell winner = null;

		for (int iy = -2; iy <= 2; iy++) {
			for (int ix = -2; ix <= 2; ix++) {
				InfiniteCell cell = peakCell(cellX+ix, cellY+iy);
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
	 * Gets a cell. Does reserve() and init(), meaning the cell will be stored in the cellCache
	 * and the poylgon and neighbor information will be calculated.
	 * Cells returned must call cell.release() in order to be removed from the cellCache.
	 */
	public synchronized InfiniteCell getCell(int x, int y) {
		return getCell(x, y, true);
	}
	
	/**
	 * Gets a cell. Does not reserve() or init(), meaning there is no polygon or neighbor data
	 * and the cell does not take up memory after the returned pointer is discarded.
	 * @param cellKey
	 * @return
	 */
	public synchronized InfiniteCell peakCell(int x, int y) {
		return getCell(x, y, false);
	}
	
	private synchronized InfiniteCell getCell(int x, int y, boolean reserve) {
		InfiniteCell cell = cellCache.get(x, y);
		if (cell == null) cell = new InfiniteCell(this, x, y);
		if (reserve) {
			cell.reserve();
			cell.init();
		}
		return cell;
	}
	
}
