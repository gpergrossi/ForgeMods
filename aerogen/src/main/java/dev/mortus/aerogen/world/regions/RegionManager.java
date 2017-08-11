package dev.mortus.aerogen.world.regions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.World;
import dev.mortus.util.math.geom2d.Rect;
import dev.mortus.voronoi.InfiniteCell;
import dev.mortus.voronoi.InfiniteVoronoi;

public class RegionManager extends InfiniteVoronoi {

	public static Map<World, RegionManager> managers = new HashMap<>();
	
	public static synchronized RegionManager instanceFor(World world) {
		RegionManager manager = managers.get(world);
		if (manager == null) {
			manager = new RegionManager();
			managers.put(world, manager);
		}
		System.out.println("Manager for world: "+manager+" : "+world);
		System.out.flush();
		return manager;
	}
	
	public static void put(World world, RegionManager manager) {
		managers.put(world, manager);		
	}
	
	final double regionSize;
	final double cellSize;
	
	public RegionManager() {
		this(512, 64, 875849390123L);
	}
	
	public RegionManager(long seed) {
		this(512, 64, 875849390123L); // TODO fix seed when done testing
	}
	
	public RegionManager(double regionSize, double cellSize, long seed) {
		super(regionSize, seed, 256);
		this.cellSize = cellSize;
		this.regionSize = regionSize;
	}

	public void getAll(List<Region> output, int minX, int minY, int maxX, int maxY) {
		List<InfiniteCell> cells = new ArrayList<>();
		getCells(new Rect(minX, minY, maxX-minX, maxY-minY), cells);
		
		for (InfiniteCell c : cells) {
			Region r = getRegionForCell(c);
			if (output.contains(r)) continue;
			output.add(r);
		}
	}

	private Region getRegionForCell(InfiniteCell cell) {
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
