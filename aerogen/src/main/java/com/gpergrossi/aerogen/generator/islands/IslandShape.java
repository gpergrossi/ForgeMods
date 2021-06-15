package com.gpergrossi.aerogen.generator.islands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.generator.regions.features.river.RiverCell;
import com.gpergrossi.aerogen.generator.regions.features.river.RiverWaterfall;
import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.geom.ranges.DistanceTransform;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.ranges.Int2DRange.Bits;
import com.gpergrossi.util.geom.shapes.Rect;
import com.gpergrossi.util.geom.vectors.Int2D;

public class IslandShape {

	public final Island island;
	public Int2DRange range;
	public List<IslandCell> cells;
	
	public List<RiverCell> riverCells;
	public List<RiverWaterfall> waterfalls;
	
	public Int2DRange.Floats edgeDistance;
	public float maxEdgeDistance;
	
	
	public IslandShape(Island island, List<IslandCell> cells) {
		this.island = island;
		this.cells = cells;
		
		Rect boundsXZ = null;
		for (IslandCell cell : cells) {
			Rect r = cell.getPolygon().getBounds();
			if (boundsXZ == null) boundsXZ = r;
			else boundsXZ = boundsXZ.union(r);
		}
		range = Int2DRange.fromRect(boundsXZ);
	}
	
	
	/**
	 * Generate the top-down shape of the island.
	 */
	public void erode(IslandErosion erosion, Random random) {		
		final Int2DRange.Bits shape = createMultiPolyShape();
		
		// Perform the distanceTransform to acquire the "distance to island boundary" matrix
		Tuple2<Int2DRange.Floats, Float> result = DistanceTransform.transform(shape);
		
		// Begin erosion: Remove some of the island's tiles to make it look more natural
		erosion.begin(island, random, result.second);
		for (Int2D.StoredFloat tile : result.first.getAllFloats()) {
			if (tile.index % 200 == 0) Thread.yield();
			float edgeDist = tile.getValue();
			
			// Some tiles are already outside the island boundary, no need to erode them
			if (edgeDist <= 0f) continue; 

			// For the rest we apply the erode function
			if (erosion.erode(tile.x(), tile.y(), edgeDist)) {
				shape.set(tile.index, true);
			}
		}		
		
		// Update the distance transform to include the new erosion information
		result = DistanceTransform.transform(shape);
		
		// Done! Now to create the require return values
		this.range = result.first.getTrimmedRange(v -> (v <= 0));
		this.edgeDistance = this.range.copyFloats(result.first, 0, 0);
		this.maxEdgeDistance = result.second;
	}
	
	private Bits createMultiPolyShape() {
		Int2DRange.Bits shape = this.range.grow(1).createBits();
		
		// Set tiles inside the island to Float.Infinity and outside the island to Zero.
		// Note that there is also a padding of 1 tile around the outside, this makes neighbor checks faster.
		for (Int2D.StoredBit tile : shape.getAllBits()) {
			if (tile.index % 200 == 0) Thread.yield();
			
			tile.setValue(true);
			
			// Otherwise, check if the cells are in one of the island polygons
			for (IslandCell cell : cells) {
				if (cell.getPolygon().contains(tile.x(), tile.y())) {
					tile.setValue(false);
					break;
				}
			}
		}
		
		return shape;
	}


	public int minX() { return range.minX; }
	public int maxX() { return range.maxX; }
	public int width() { return range.width; }
	
	public int minZ() { return range.minY; }
	public int maxZ() { return range.maxY; }
	public int depth() { return range.height; }
	
	public boolean contains(int x, int z) {
		return getEdgeDistance(x, z) > 0;
	}

	public boolean contains(Int2D pos) {
		return contains(pos.x(), pos.y());
	}
	
	public float getEdgeDistance(int x, int z) {
		return edgeDistance.getSafe(x, z, 0);
	}
	
	public float getEdgeDistance(Int2D pos) {
		return edgeDistance.getSafe(pos.x(), pos.y(), 0);
	}

	public float getMaxEdgeDistance() {
		return maxEdgeDistance;
	}

	public Rect getBoundingBox() {
		return new Rect(range.minX, range.minY, range.width+1, range.height+1);
	}

	public IslandCell getCell(int x, int z) {
		if (!this.contains(x, z)) return null;
		for (IslandCell cell : cells) {
			if (cell.getPolygon().contains(x, z)) return cell;
		}
		return null;
	}
	
	public double riverHeadDist(float x, float z) {
		double riverDist = Double.POSITIVE_INFINITY;
		for (RiverCell irc : island.getShape().getRiverCells()) {
			if (irc.getRiverCellPrevious() != null) continue;
			riverDist = Math.min(riverDist, irc.minDistToRiver(x, z));
		}
		return riverDist;
	}
	
	public double getRiverDistance(float x, float z) {
		double riverDist = Double.POSITIVE_INFINITY;
		for (RiverCell irc : island.getShape().getRiverCells()) {
			riverDist = Math.min(riverDist, irc.minDistToRiver(x, z));
		}
		return riverDist;
	}

	private void genRiverLists() {
		this.riverCells = new ArrayList<>();
		this.waterfalls = new ArrayList<>();
		Rect boundsXZ = null;
		for (IslandCell cell : cells) {
			Rect r = cell.getPolygon().getBounds();
			if (boundsXZ == null) boundsXZ = r;
			else boundsXZ.union(r);
			for (RiverCell irc : cell.getRiverCells()) {
				riverCells.add(irc);
				if (irc.hasWaterfall()) irc.getWaterfalls(waterfalls);
			}
		}
		riverCells = Collections.unmodifiableList(riverCells);
		waterfalls = Collections.unmodifiableList(waterfalls);
	}
	
	public List<RiverCell> getRiverCells() {
		if (riverCells == null) genRiverLists();
		return riverCells;
	}

	public List<RiverWaterfall> getWaterfalls() {
		if (waterfalls == null) genRiverLists();
		return waterfalls;
	}

	public List<IslandCell> getCells() {
		return cells;
	}
	
}
