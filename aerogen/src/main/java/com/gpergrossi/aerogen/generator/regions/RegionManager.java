package com.gpergrossi.aerogen.generator.regions;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.voronoi.InfiniteCell;
import com.gpergrossi.voronoi.InfiniteVoronoi;

public class RegionManager extends InfiniteVoronoi {
	
	AeroGenerator generator;
	
	Region loadedRegionsListSentinel = new Region(this, null);
	int modifyCount;
	
	public RegionManager(AeroGenerator generator) {
		super(generator.getSettings().regionGridSize, generator.getSettings().seed);
		this.generator = generator;
	}

	public synchronized void getAll(List<Region> output, Int2DRange minecraftBlockRangeXZ) {
		List<InfiniteCell> cells = new ArrayList<>();
		getCells(minecraftBlockRangeXZ, cells);
		
		for (InfiniteCell c : cells) {
			Region r = getRegionForCell(c);
			if (output.contains(r)) continue;
			output.add(r);
		}
	}

	public synchronized void getLoadedRegions(List<Region> outputList) {
		for (Region r : getLoadedRegions()) {
			outputList.add(r);
		}
	}
	
	public Iterable<Region> getLoadedRegions() {
		return new Iterable<Region>() {
			public Iterator<Region> iterator() {
				return new Iterator<Region>() {
					Region current = loadedRegionsListSentinel;
					int expectedModifyCount = modifyCount;
					
					@Override
					public boolean hasNext() {
						if (modifyCount != expectedModifyCount) throw new ConcurrentModificationException();
						if (current.loadedRegionsNext == loadedRegionsListSentinel) return false;
						return true;
					}

					@Override
					public Region next() {
						if (modifyCount != expectedModifyCount) throw new ConcurrentModificationException();
						current = current.loadedRegionsNext;
						return current;
					}
				};
			}
		};
	}
	
	private void promoteRegionToHeadOfLoadedList(Region promotedRegion) {
		Region self = promotedRegion;
		
		// 1. Detach from current location
		Region prev = self.loadedRegionsPrev;
		Region next = self.loadedRegionsNext;
			
		// Neighbors point past 'self'
		prev.loadedRegionsNext = next;
		next.loadedRegionsPrev = prev;
		
		// 2. Insert at head
		Region sentinel = loadedRegionsListSentinel;
		Region oldFirst = loadedRegionsListSentinel.loadedRegionsNext;
		
		// Join sentinel <--> promoted region
		sentinel.loadedRegionsNext = promotedRegion;
		promotedRegion.loadedRegionsPrev = sentinel;
		
		// Join promoted region <--> oldFirst
		oldFirst.loadedRegionsPrev = promotedRegion;
		promotedRegion.loadedRegionsNext = oldFirst;
		
		modifyCount++;
	}
	
	private Region getRegionForCell(InfiniteCell cell) {
		Region r = (Region) cell.data;
		if (r == null) {
			r = new Region(this, cell);
			cell.data = r;
		}
		promoteRegionToHeadOfLoadedList(r);
		return r;
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
