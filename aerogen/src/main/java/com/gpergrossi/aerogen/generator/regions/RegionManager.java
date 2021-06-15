package com.gpergrossi.aerogen.generator.regions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.aerogen.generator.AeroGeneratorSettings;
import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.voronoi.infinite.InfiniteCell;
import com.gpergrossi.voronoi.infinite.InfiniteVoronoi;

public class RegionManager extends InfiniteVoronoi {
	
	final AeroGenerator generator;
	final AeroGeneratorSettings settings;
	final LinkedList<Region> loadedRegions;
	
	public RegionManager(AeroGenerator generator, AeroGeneratorSettings settings) {
		super(settings.regionGridSize, settings.seed);
		this.generator = generator;
		this.settings = settings;
		this.loadedRegions = new LinkedList<>();
	}

	public AeroGenerator getGenerator() {
		return generator;
	}
	
	public synchronized void getRegions(List<Region> output, Int2DRange minecraftBlockRangeXZ) {
		List<InfiniteCell> cells = new ArrayList<>();
		getCells(minecraftBlockRangeXZ, cells);
		
		for (InfiniteCell cell : cells) {
			Region region = getRegionForCell(cell);
			if (!output.contains(region)) output.add(region);
			cell.release();
		}
	}

	public synchronized Region getRegion(int x, int y) {
		return getRegionForCell(getCell(x, y));
	}
	
	public synchronized Region getSpawnRegion() {
		return getRegion(0, 0);
	}
	
	private synchronized Region getRegionForCell(InfiniteCell regionCell) {
		Region region = (Region) regionCell.data;
		if (region == null) {
			region = new Region(this, regionCell, this.settings);
			regionCell.data = region;
			loadedRegions.add(region);
		}
		return region;
	}
	
	/**
	 * Non-thread-synchronous iterator for loaded regions
	 */
	public Iterable<Region> debugGetLoadedRegions() {
		return new Iterable<Region>() {
			@Override
			public Iterator<Region> iterator() {
				return loadedRegions.iterator();
			}
		};
	}
	
}
