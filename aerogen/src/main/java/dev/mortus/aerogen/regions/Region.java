package dev.mortus.aerogen.regions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dev.mortus.aerogen.cells.Cell;
import dev.mortus.aerogen.islands.Island;
import dev.mortus.util.math.geom.Polygon;
import dev.mortus.util.math.geom.Rect;
import dev.mortus.voronoi.Edge;
import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.Voronoi;
import dev.mortus.voronoi.VoronoiBuilder;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class Region {

	public static boolean DEBUG_VIEW = false;
	public static boolean SMART_THRESHOLD = false;
	
	private RegionManager manager;
	private Cell regionCell;
	private Random random;
	private RegionBiome biome;
	
	List<Island> islands;
	
	public Region(RegionManager manager, Cell boundaryCell) {
		this.manager = manager;
		boundaryCell.reserve();
		this.regionCell = boundaryCell;
		random = new Random(boundaryCell.getSeed());
		init();
	}

	public void release() {
		regionCell.release();
	}
	
	public void init() {
		List<Site> subCells = createSubCells(manager.cellSize, 4);
		createIslands(subCells);
		System.out.println("Initializing "+this+" "+islands.size()+" islands");
		
		if (Region.DEBUG_VIEW) return;
		biome = RegionBiomes.randomBiome(random);
	}

	private List<Site> createSubCells(double avgCellSize, int relax) {
		double avgCellArea = avgCellSize*avgCellSize;
		Polygon regionPolygon = regionCell.getPolygon();
		Rect bounds = regionPolygon.getBounds();
		bounds.expand(avgCellSize*2);
		double area = bounds.getArea();
		int num = (int) Math.ceil(area / avgCellArea);
		
		// Build a voronoi diagram with desire average cell area, relaxed
		VoronoiBuilder builder = new VoronoiBuilder();
		builder.setBounds(bounds);
		for (int i = 0; i < num; i++) {
			int success = -1;
			while (success == -1) success = builder.addSiteSafe(bounds.randomPoint(random));
		}
		Voronoi voronoi = builder.build();
		for (int i = 0; i < relax; i++) voronoi = voronoi.relax(builder);
		
		// Create a list of cells that are completely inside the region polygon
		List<Site> subCellList = new ArrayList<>();
		for (Site s : voronoi.getSites()) {
			if (regionPolygon.contains(s.getPolygon())) subCellList.add(s);
		}
		
		return subCellList;
	}
	
	private void createIslands(List<Site> cellList) {
		islands = new ArrayList<>();
		
		List<Site> unallocated = cellList;
		List<Site> adjacentSites = new ArrayList<>();
		List<Double> adjacentWeights = new ArrayList<>();
		List<Site> islandSites = new ArrayList<>();
		
		while (unallocated.size() > 0) {
			Site kernel = unallocated.remove(random.nextInt(unallocated.size()));
			adjacentSites.clear();
			adjacentSites.add(kernel);

			double area = kernel.getPolygon().getArea();
			double perimeter = kernel.getPolygon().getPerimeter();
			
			int maxGather = (unallocated.size()+1)/2;
			if (maxGather < 1) maxGather = 1; 
			
			islandSites.clear();
			while (adjacentSites.size() > 0 && islandSites.size() < maxGather) {
				
				// Weight the perimeter sites based on shared edge length
				adjacentWeights.clear();
				double totalShared = 0;
				for (Site s : adjacentSites) {
					double shared = calculateSharedPerimeterLength(s, islandSites)+0.01;
					shared = Math.pow(shared, 3);
					adjacentWeights.add(shared);
					totalShared += shared;
				}
				
				// Select a site based on weights
				Site site = null;
				double target = random.nextDouble();
				Iterator<Double> weightIter = adjacentWeights.iterator();
				for (Site s : adjacentSites) {
					double range = weightIter.next() / totalShared;
					target -= range;
					if (target < 0) {
						site = s;
						break;
					}
				}
				
				adjacentSites.remove(site);

				area += site.getPolygon().getArea();
				perimeter += site.getPolygon().getPerimeter();
				perimeter -= 2*calculateSharedPerimeterLength(site, islandSites);
				
				unallocated.remove(site);
				islandSites.add(site);
				
				for (Edge e : site.getEdges()) {
					Site neighbor = e.getNeighbor(site);
					if (neighbor == null) continue;
					if (adjacentSites.contains(neighbor)) continue;
					if (!unallocated.contains(neighbor)) continue;
					adjacentSites.add(neighbor);
				}

				Thread.yield();
				if (random.nextDouble() < 0.15) break;
			}

			double ratio = area/perimeter;
			double perfectCircle = Math.sqrt(area)/1.128379;
			double threshold = perfectCircle/5.5;
			
			if (ratio < threshold) {
				// Island isn't round enough
				// - Break off a random cell
				Site site = islandSites.remove(random.nextInt(islandSites.size()));
				
				// - Make it an island
				Island island = new Island(this, islands.size(), random.nextLong());
				island.grant(site.getPolygon());
				island.finishGrant();
				islands.add(island);
				
				// - Put the rest back
				unallocated.addAll(islandSites);
				continue;
			}
			
			// Island is round enough
			Island island = new Island(this, islands.size(), random.nextLong());
			for (Site site : islandSites) island.grant(site.getPolygon());
			island.finishGrant();
			islands.add(island);

		}
		
//		Iterator<Island> iter = islands.iterator();
//		while (iter.hasNext()) {
//			Island isle = iter.next();
//			double oddsToRemove = 0.25;
//			for (int i = 1; i < isle.getPolygons().size(); i++) {
//				oddsToRemove *= 0.10;
//			}
//			if (random.nextDouble() < oddsToRemove) iter.remove();
//		}
	}

	private double calculateSharedPerimeterLength(Site elem, List<Site> list) {
		double sharedPerimeter = 0;
		for (Edge e : elem.getEdges()) {
			Site neighbor = e.getNeighbor(elem);
			if (!list.contains(neighbor)) continue;
			sharedPerimeter += e.toLineSeg().length();
		}
		return sharedPerimeter;
	}
	
	public RegionBiome getBiome() {
		return biome;
	}
	
	public List<Island> getIslands() {
		return islands;
	}
	
	@Override
	public String toString() {
		return "Region ("+regionCell.containerKey.x+", "+regionCell.containerKey.y+")";
	}

	public Polygon getRegionPolygon() {
		return regionCell.getPolygon();
	}

	public long getSeed() {
		return regionCell.seed;
	}

	public Point getCoord() {
		return new Point(regionCell.containerKey);
	}
	
}
