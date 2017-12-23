package com.gpergrossi.aerogen.generator.regions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import com.gpergrossi.aerogen.definitions.biomes.IslandBiome;
import com.gpergrossi.aerogen.definitions.regions.RegionBiome;
import com.gpergrossi.aerogen.definitions.regions.RegionBiomes;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.aerogen.generator.regions.features.IRegionFeature;
import com.gpergrossi.util.geom.shapes.Convex;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.voronoi.Edge;
import com.gpergrossi.voronoi.InfiniteCell;
import com.gpergrossi.voronoi.Site;
import com.gpergrossi.voronoi.Voronoi;
import com.gpergrossi.voronoi.VoronoiBuilder;

public class Region {
	
	private RegionManager manager;
	private InfiniteCell regionCell;
	private Random random;
	private RegionBiome biome;

	// Loaded regions are part of a LinkedList
	Region loadedRegionsPrev;
	Region loadedRegionsNext;
	
	List<Island> islands;
	List<IslandCell> islandCells;
	
	int maxHeightLayers = 4;
	int numHeightLayers;
	double averageCellRadius;
	
	List<IRegionFeature> features;
	
	public Region(RegionManager manager, InfiniteCell boundaryCell) {
		this.manager = manager;
		
		this.regionCell = boundaryCell;
		if (boundaryCell != null) {
			boundaryCell.reserve();	
			this.random = new Random(boundaryCell.getSeed());
			init();
		}
		
		// Region not a part of the linked list yet
		this.loadedRegionsPrev = this;
		this.loadedRegionsNext = this;
	}

	public void release() {
		regionCell.release();
	}
	
	public void init() {
		if (regionCell.cellX == 0 && regionCell.cellY == 0) {
			// Region (0, 0) is the spawn region and is always of type START_AREA
			biome = RegionBiomes.START_AREA;
		} else {
			biome = RegionBiomes.randomBiome(random);
		}
		
		List<Site> subCells = createSubCells(manager.getCellSize() * biome.getCellSizeMultiplier(), 4);
		
		this.averageCellRadius = Math.sqrt(manager.getCellSize())/2.0;
		this.islandCells = createIslands(subCells);
		
		createFeatures();
		
		resolveIslandAltitudes();
	}

	private List<Site> createSubCells(double avgCellSize, int relax) {
		double avgCellArea = avgCellSize*avgCellSize;
		
		Convex bounds = regionCell.getPolygon();
		bounds = (Convex) bounds.inset(8);
		double area = bounds.getArea();
		
		int num = (int) Math.ceil(area / avgCellArea);
		
		// Build a voronoi diagram with desire average cell area, relaxed
		VoronoiBuilder builder = new VoronoiBuilder();
		builder.setBounds(bounds.toPolygon(4));
		for (int i = 0; i < num; i++) {
			int success = -1;
			while (success == -1) success = builder.addSiteSafe(bounds.getBounds().getRandomPoint(random));
		}
		Voronoi voronoi = builder.build();
		for (int i = 0; i < relax; i++) voronoi = voronoi.relax(builder);
		
		// Create a list of cells that are completely inside the region polygon
		List<Site> subCellList = new ArrayList<>();
		for (Site s : voronoi.getSites()) {
			subCellList.add(s);
		}
		
		return subCellList;
	}
	
	private List<IslandCell> createIslands(List<Site> cellList) {
		List<IslandCell> islandCells = new ArrayList<>();
		islands = new ArrayList<>();
		
		List<Site> unallocated = new ArrayList<>(cellList);
		List<Site> adjacentSites = new ArrayList<>();
		List<Double> adjacentWeights = new ArrayList<>();
		List<Site> islandSites = new ArrayList<>();
		
		while (unallocated.size() > 0) {
			Site kernel = unallocated.remove(random.nextInt(unallocated.size()));
			adjacentSites.clear();
			adjacentSites.add(kernel);

			double area = kernel.getPolygon().getArea();
			double perimeter = kernel.getPolygon().getPerimeter();
			
			int maxGather = (int) Math.ceil((unallocated.size()+1) * biome.getIslandCellGatherPercentage());
			if (maxGather < 1) maxGather = 1; 
			
			islandSites.clear();
			while (adjacentSites.size() > 0 && islandSites.size() < maxGather) {
				
				// Weight the perimeter sites based on shared edge length
				adjacentWeights.clear();
				double totalShared = 0;
				for (Site s : adjacentSites) {
					double shared = calculateSharedPerimeterLength(s, islandSites)+0.01;
					shared = Math.pow(shared, 6);
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
				if (random.nextDouble() < 0.2) break;
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
				
				IslandCell cell = new IslandCell(island, site);
				site.data = cell;
				
				islandCells.add(cell);
				island.grantCell(cell);
				
				island.finishGrant();
				islands.add(island);
				
				// - Put the rest back
				unallocated.addAll(islandSites);
				continue;
			}
			
			// Island is round enough
			Island island = new Island(this, islands.size(), random.nextLong());
			for (Site site : islandSites) {
				IslandCell cell = new IslandCell(island, site);
				site.data = cell;
				islandCells.add(cell);
				island.grantCell(cell);
			}
			island.finishGrant();
			islands.add(island);

			islands = Collections.unmodifiableList(islands);
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

		return islandCells;
	}

	private void createFeatures() {
		this.features = new ArrayList<>();
		
		List<IRegionFeature> features = biome.getFeatures(this, random);
		if (features == null) return;
		
		for (IRegionFeature feature : features) {
			feature.create(this, random);	
		}
	}
	
	/**
	 * Gives each island an altitude integer from 0 to numHeightLayers-1
	 */
	private void resolveIslandAltitudes() {
		// TODO
	}

	private double calculateSharedPerimeterLength(Site siteInQuestion, List<Site> otherSites) {
		double sharedPerimeter = 0;
		for (Edge e : siteInQuestion.getEdges()) {
			Site neighbor = e.getNeighbor(siteInQuestion);
			if (!otherSites.contains(neighbor)) continue;
			sharedPerimeter += e.toLineSeg().length();
		}
		return sharedPerimeter;
	}
	
	public RegionBiome getBiome() {
		return biome;
	}

	public List<IslandCell> getCells() {
		return this.islandCells;
	}

	public IslandCell getRandomCell(Random rand) {
		return islandCells.get(rand.nextInt(islandCells.size()));
	}
	
	public double getAverageCellRadius() {
		return this.averageCellRadius;
	}
	
	public int getIslandCount() {
		return islands.size();
	}
	
	public Island getIsland(int index) {
		return islands.get(index);
	}
	
	public List<Island> getIslands() {
		return islands;
	}
	
	public List<IRegionFeature> getFeatures() {
		return features;
	}

	public <T extends IRegionFeature> T getFeature(Class<T> featureClass) {
		for (IRegionFeature feature : features) {
			if (!featureClass.isAssignableFrom(feature.getClass())) continue;
			return featureClass.cast(feature);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "Region ("+regionCell.cellX+", "+regionCell.cellY+")";
	}

	public Convex getRegionPolygon() {
		return regionCell.getPolygon();
	}

	public long getSeed() {
		return regionCell.seed;
	}

	public Int2D getCoord() {
		return regionCell.getCoord();
	}

	public List<IRegionFeature> getRegionFeatures() {
		return features;
	}

	public int numHeightLayers() {
		return numHeightLayers;
	}

	public IslandBiome getIslandBiome(Island island, Random random) {
		return biome.getRandomIslandBiome(random);
	}

	public int getAltitude(Island island, Random random) {
		int minHeight = this.biome.getIslandMinAltitude();
		int maxHeight = this.biome.getIslandMaxAltitude();
		
		if (island.getAltitudeLayer() != Island.LAYER_UNASSIGNED) {
			int heightRange = maxHeight - minHeight + 1;
			double heightPerLayer = heightRange / numHeightLayers();
			minHeight = (int) (minHeight + island.getAltitudeLayer() * heightPerLayer);
			maxHeight = (int) (minHeight + island.getAltitudeLayer() * heightPerLayer + heightPerLayer);
		}
		
		return this.biome.getRandomIslandAltitude(random, minHeight, maxHeight);
	}
	
}
