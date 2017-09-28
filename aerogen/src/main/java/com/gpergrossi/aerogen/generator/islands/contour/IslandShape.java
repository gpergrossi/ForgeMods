package com.gpergrossi.aerogen.generator.islands.contour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.aerogen.generator.regions.features.river.RiverCell;
import com.gpergrossi.aerogen.generator.regions.features.river.RiverWaterfall;
import com.gpergrossi.util.data.ranges.Int2DRange;
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
		// The operations needed to compute edge distance are extremely computation intense.
		// In order to make the outline computation as efficient as possible, the code has
		// become somewhat complex. Follow the comments. Basically I am doing am generating
		// a matrix of "distance to the edge of the island from this spot" values. I do this
		// by "smearing" distance values over a matrix in 4 diagonal passes. I use a small
		// model of the true island matrix to compute distance values. Then I use estimates
		// and math to refine the small matrix into a reasonably accurate full-scale matrix
		
		int maxMatrixCells = erosion.preferredMaxMatrixSize();
		
		// This multiplier will be used in conversions between the real island size and the smaller representation
		double multiplier = Math.ceil((double)this.range.size()/maxMatrixCells);
		
		double maxMultiplier = erosion.maximumDownsize();
		if (multiplier > maxMultiplier) multiplier = maxMultiplier; 
		if (multiplier < 1.0) multiplier = 1.0; // no need to scale down, if the island is small, cool!
		
		// Create the small matrix representation of this island
		int smallScale = (int) multiplier;

		int padding = 1;
		Int2DRange.Floats smallDist;
		/* Scope */ {
			int smallWidth = (int) Math.ceil(this.range.width/multiplier);
			int smallDepth = (int) Math.ceil(this.range.height/multiplier);
			smallDist = new Int2DRange(0, 0, smallWidth-1, smallDepth-1).grow(padding).createFloats();
		}
		
		// Set tiles inside the island to Float.Infinity and outside the island to Zero.
		// Note that there is also a padding of 1 tile around the outside, this makes neighbor checks faster.
		for (Int2D.StoredFloat tile : smallDist.getAllFloats()) {
			if (tile.index % 200 == 0) Thread.yield();
			
			// If in the padding region, we are done. All padding tiles should be 0
			if (smallDist.onBorder(tile, padding)) continue;
			
			// Otherwise, check if the cells are in one of the island polygons
			for (IslandCell cell : cells) {
				float trueX = this.range.minX + tile.x()*smallScale;
				float trueZ = this.range.minY + tile.y()*smallScale;
				if (cell.getPolygon().contains(trueX, trueZ)) {
					tile.setValue(Float.POSITIVE_INFINITY);
					break;
				}
			}
		}
		
		// Perform the distanceTransform to acquire the "distance to island boundary" matrix
		float maxDistance = distanceTransform(smallDist) * smallScale;

		// If we are not working at full scale, we need to remember
		// the boundary distances to estimate the full size matrix later
		Int2DRange.Floats boundDist = null;
		if (smallScale > 1) {
			boundDist = new Int2DRange.Floats(smallDist);
			System.arraycopy(smallDist.data, 0, boundDist.data, 0, boundDist.size());
		}
		
		// Begin erosion. Removes some of the island's tiles to make it more natural
		erosion.begin(island, random, maxDistance);
		
		boolean needsUpdate = false;
		for (Int2D.StoredFloat tile : smallDist.getAllFloats()) {
			if (tile.index % 200 == 0) Thread.yield();
			float edgeDist = tile.getValue() * smallScale;
			
			// Some tiles are already outside the island boundary, no need to erode them
			if (edgeDist == 0f) continue; 

			// For the rest we apply the erode function
			float trueX = this.range.minX + tile.x()*smallScale;
			float trueZ = this.range.minY + tile.y()*smallScale;
			if (erosion.erode(trueX, trueZ, edgeDist)) {
				tile.setValue(0f);
				needsUpdate = true;
			}
		}		
		
		// Update the distance transform to include the new erosion information
		if (needsUpdate) maxDistance = distanceTransform(smallDist) * smallScale;
		
		// Done! Now to create the require return values
		
		// If the smallScale representation is full size, we just need to fix the padding
		if (smallScale == 1) {
			Int2DRange trimRange = smallDist.getTrimRange(v -> (v <= 0));
			int originalMinX = range.minX;
			int originalMinY = range.minY;
			this.range = trimRange.offset(originalMinX, originalMinY);
			this.edgeDistance = this.range.copyFloats(smallDist, originalMinX, originalMinY);
			this.maxEdgeDistance = maxDistance;
			return;
		}
		
		// Otherwise, we need to do a bit of magic to up-scale the values
		Int2DRange trimRange = smallDist.getTrimRange(v -> (v <= 0)).grow(0, 0, 2, 2);
		int originalMinX = range.minX;
		int originalMinY = range.minY;
		this.range = trimRange.scale(smallScale).offset(range.minX, range.minY);
		Int2DRange.Floats fullSize = new Int2DRange.Floats(this.range);
		
		for (Int2D.StoredFloat tile : fullSize.getAllFloats()) {
			if (tile.index % 200 == 0) Thread.yield();
			float x = (float) (tile.x() - originalMinX) / (float) (smallScale);
			float y = (float) (tile.y() - originalMinY) / (float) (smallScale);
			
			float edgeDist = smallDist.lerp(x, y, 0);
			
			// All small matrix zeros correspond to true matrix zeros
			if (edgeDist <= 0) {
				tile.setValue(0f);
				
				// Perfect edge
				tile.getNeighbor(-1, 0).ifPresent(n -> n.setValue(v -> Math.min(v, 1)));
				tile.getNeighbor(0, -1).ifPresent(n -> n.setValue(v -> Math.min(v, 1)));
				tile.getNeighbor(1, 0).ifPresent(n -> n.setValue(1f));
				tile.getNeighbor(0, 1).ifPresent(n -> n.setValue(1f));
				
				continue;
			};
			
			// If the edgeDist is small, the tile is on the edge of the island and
			// a simple linear interpolation will not be good enough
			if (edgeDist <= 1.0f) {
				// We apply the erosion function from earlier
				float borderDist = boundDist.lerp(x, y, 0) * smallScale;
				if (erosion.erode(tile.x(), tile.y(), borderDist)) {
					tile.setValue(0f);
					
					// Perfect edge
					tile.getNeighbor(-1, 0).ifPresent(n -> n.setValue(v -> Math.min(v, 1)));
					tile.getNeighbor(0, -1).ifPresent(n -> n.setValue(v -> Math.min(v, 1)));
					tile.getNeighbor(1, 0).ifPresent(n -> n.setValue(1f));
					tile.getNeighbor(0, 1).ifPresent(n -> n.setValue(1f));
					
					continue;
				}
			}

			// In all other cases, we only need to scale up the small distance estimate
			edgeDist *= smallScale;
			
			// Perfect edge
			if (tile.getValue() == 1f && edgeDist > 0) continue;
			
			tile.setValue(edgeDist);
		}
		this.edgeDistance = fullSize;
		this.maxEdgeDistance = maxDistance;
	}
	
	/**
	 * Performs a distance transform on the given array, which is interpreted as
	 * a two dimensional array in y*width+x order. The array must already be initialized
	 * for a distance transform. Values are pulled down to the lowest neighboring value
	 * plus 1 for cardinal and 1.414f for diagonal. Also returns the greatest distance found.
	 */
	private float distanceTransform(Int2DRange.Floats matrix) {
		float[] array = matrix.data;
		int width = matrix.width;
		int height = matrix.height;
		
		float dist;
		int i = 0;
		for (int y = 1; y < height; y++) {
			for (int x = 1; x < width; x++) {
				int index = y*width+x;
				if (array[index] == 0) continue;
				dist = array[index];
				dist = Math.min(array[index-width]+1, dist);
				dist = Math.min(array[index-1]+1, dist);
				dist = Math.min(array[index-width-1]+1.414f, dist);
				array[index] = dist;
				if (i++ == 200) { Thread.yield(); i = 0; }
			}
			for (int x = width-2; x >= 0; x--) {
				int index = y*width+x;
				if (array[index] == 0) continue;
				dist = array[index];
				dist = Math.min(array[index+1]+1, dist);
				dist = Math.min(array[index-width+1]+1.414f, dist);
				array[index] = dist;
				if (i++ == 200) { Thread.yield(); i = 0; }
			}
		}
		float maxDistance = 0;
		for (int y = height-2; y >= 0; y--) {
			for (int x = 1; x < width; x++) {
				int index = y*width+x;
				if (array[index] == 0) continue;
				dist = array[index];
				dist = Math.min(array[index+width]+1, dist);
				dist = Math.min(array[index-1]+1, dist);
				dist = Math.min(array[index+width-1]+1.414f, dist);
				array[index] = dist;
				if (i++ == 200) { Thread.yield(); i = 0; }
			}
			for (int x = width-2; x >= 0; x--) {
				int index = y*width+x;
				if (array[index] == 0) continue;
				dist = array[index];
				dist = Math.min(array[index+1]+1, dist);
				dist = Math.min(array[index+width+1]+1.414f, dist);
				array[index] = dist;
				maxDistance = Math.max(maxDistance, array[index]);
				if (i++ == 200) { Thread.yield(); i = 0; }
			}
		}
		return maxDistance;
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
		if (edgeDistance == null) {
			System.out.println("Island coord: "+this.island.getRegion().getCoord() +":"+this.island.regionIndex);
			System.out.println("Island biome: "+this.island.getBiome());
			System.out.println("Island range: "+this.range);
		}
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
	
	public double riverHeadDist(int x, int z) {
		double riverDist = Double.POSITIVE_INFINITY;
		for (RiverCell irc : island.getShape().getRiverCells()) {
			if (irc.getRiverPrevious() != null) continue;
			riverDist = Math.min(riverDist, irc.minDistToRiver(x, z));
		}
		return riverDist;
	}
	
	public double getRiverDistance(int x, int z) {
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
