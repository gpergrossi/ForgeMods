package com.gpergrossi.aerogen.generator.regions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.voronoi.infinite.InfiniteCell;
import com.gpergrossi.voronoi.infinite.InfiniteVoronoi;

public class RegionManager extends InfiniteVoronoi {
	
	AeroGenerator generator;
	LinkedList<Region> loadedRegions;
	
	public RegionManager(AeroGenerator generator) {
		super(generator.getSettings().regionGridSize, generator.getSettings().seed);
		this.generator = generator;
		this.loadedRegions = new LinkedList<>();
	}

	public synchronized void getAll(List<Region> output, Int2DRange minecraftBlockRangeXZ) {
		List<InfiniteCell> cells = new ArrayList<>();
		getCells(minecraftBlockRangeXZ, cells);
		
		for (InfiniteCell cell : cells) {
			Region region = getRegionForCell(cell);
			if (output.contains(region)) continue;
			output.add(region);
			cell.release();
		}
	}

	public synchronized void getLoadedRegions(List<Region> outputList) {
		for (Region r : getLoadedRegions()) {
			outputList.add(r);
		}
	}
	
	public Iterable<Region> getLoadedRegions() {
		return new Iterable<Region>() {
			@Override
			public Iterator<Region> iterator() {
				return loadedRegions.iterator();
			}
		};
	}
	
	private void promoteRegionToHeadOfLoadedList(Region promotedRegion) {
		loadedRegions.remove(promotedRegion);
		loadedRegions.addFirst(promotedRegion);
	}
	
	private Region getRegionForCell(InfiniteCell regionCell) {
		Region r = (Region) regionCell.data;
		if (r == null) {
			r = new Region(this, regionCell);
			regionCell.data = r;
		}
		promoteRegionToHeadOfLoadedList(r);
		return r;
	}
	
	public Region getSpawnRegion() {
		return getRegion(0, 0);
	}

	public synchronized Region getRegion(int x, int y) {
		return getRegionForCell(getCell(x, y));
	}

	public double getRegionSize() {
		return generator.getSettings().regionGridSize;
	}
	
	public double getCellSize() {
		return generator.getSettings().islandCellBaseSize;
	}
	
}
