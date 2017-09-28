package com.gpergrossi.aerogen.generator.regions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.gpergrossi.aerogen.definitions.biomes.IslandBiome;
import com.gpergrossi.aerogen.definitions.regions.RegionBiome;
import com.gpergrossi.aerogen.definitions.regions.RegionBiomes;
import com.gpergrossi.aerogen.generator.data.IslandCell;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.features.River;
import com.gpergrossi.aerogen.generator.regions.features.RiverCell;
import com.gpergrossi.util.geom.shapes.Convex;
import com.gpergrossi.util.geom.vectors.Double2D;
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
	
	int numHeightLayers;
	Island riverHeadIsland;
	IslandCell riverHeadCell;
	List<River> rivers;
	
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
		if (regionCell.cellX == 0 && regionCell.cellY == 0) biome = RegionBiomes.START_AREA;
		else biome = RegionBiomes.randomBiome(random);
		
		List<Site> subCells = createSubCells(manager.getCellSize() * biome.getCellSizeMultiplier(), 4);
		
		double avgRadius = Math.sqrt(manager.getCellSize())/2.0;
		Map<Site, IslandCell> siteToCell = createIslands(subCells);
		
		createRivers(subCells, siteToCell, avgRadius);
		
		System.out.println("Initializing "+this+" "+islands.size()+" islands");
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
	
	private Map<Site, IslandCell> createIslands(List<Site> cellList) {
		Map<Site, IslandCell> siteToCell = new HashMap<>();
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
				IslandCell cell = new IslandCell(island, site.getPolygon());
				siteToCell.put(site, cell);
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
				IslandCell cell = new IslandCell(island, site.getPolygon());
				siteToCell.put(site, cell);
				island.grantCell(cell);
			}
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

		return siteToCell;
	}

	private void createRivers(List<Site> subCells, Map<Site, IslandCell> siteToCell, double avgCellRadius) {
		rivers = new ArrayList<>();
		int numRivers = biome.getRandomNumberOfRivers(random);
		
		List<Edge> nextEdges = new ArrayList<>();
		List<Double> nextWeights = new ArrayList<>();
		List<Site> consumed = new ArrayList<>();
				
		Site riverHeadSite = subCells.get(random.nextInt(subCells.size()));
		riverHeadCell = siteToCell.get(riverHeadSite);
		riverHeadIsland = riverHeadCell.getIsland();
			
		int maxLayersUsable = 4;
		int minLayerUsed = Integer.MAX_VALUE;
		int maxLayerUsed = Integer.MIN_VALUE;
		
		for (int i = 0; i < numRivers; i++) {
			River currentRiver = new River();
			
			double a = random.nextDouble()*Math.PI*2.0;
			Double2D previousMove = new Double2D(Math.cos(a), Math.sin(a));
			Site currentSite = riverHeadSite;
			RiverCell previousRiverCell = null;
			Edge previouseEdge = null;
			
			int riverHeight = maxLayersUsable-1;
			
			while (true) {
				consumed.add(currentSite);

				boolean endOfRiver = false;
				boolean newIsland = false;
				boolean createWaterfall = false;
				Island currentIsland = null;
				RiverCell currentRiverCell = null;
				
				IslandCell currentCell = siteToCell.get(currentSite);
				
				if (currentCell == null) endOfRiver = true;
				else {
					currentIsland = currentCell.getIsland();
					if (previousRiverCell == null) newIsland = true;
					else if (currentIsland != previousRiverCell.getIsland()) {
						int islandHeight = currentIsland.getAltitudeLayer(); 
						if (riverHeight <= 0) endOfRiver = true;
						else if (islandHeight == Island.LAYER_UNASSIGNED) riverHeight = riverHeight - 1;
						else if (islandHeight < riverHeight) riverHeight = islandHeight;
						else endOfRiver = true;
						newIsland = true;
					}
				}

				if (endOfRiver) {
					currentRiver.addWaterfall(previouseEdge.toLineSeg(), new IslandCell(null, currentSite.getPolygon()));
					break;
				}
				
				if (newIsland) {
					currentIsland.setAltitudeLayer(riverHeight);
					minLayerUsed = Math.min(minLayerUsed, riverHeight);
					maxLayerUsed = Math.max(maxLayerUsed, riverHeight);
					if (previousRiverCell != null) createWaterfall = true;
				}
				
				if (createWaterfall) {
					currentRiverCell = currentRiver.addWaterfall(previouseEdge.toLineSeg(), currentCell);
				} else {
					currentRiverCell = currentRiver.addCell(currentCell);
				}
				
				nextWeights.clear();
				nextEdges.clear();
				double totalWeight = 0;
				for (Edge e : currentSite.getEdges()) {
					Site n = e.getNeighbor(currentSite);
					if (n == null || consumed.contains(n)) continue;
					Double2D nCenter = n.getPolygon().getCentroid();
					Double2D nVector = nCenter.copy();
					nVector.subtract(currentSite.getPolygon().getCentroid());
					nVector.normalize();
					double weight = Math.pow(e.toLineSeg().length(), 4) + nVector.dot(previousMove)*avgCellRadius*10;
					nextEdges.add(e);
					nextWeights.add(weight);
					totalWeight += weight;
				}
				if (nextEdges.size() == 0) break;
				
				Edge nextEdge = null;
				double target = random.nextDouble();
				Iterator<Double> weightIter = nextWeights.iterator();
				for (Edge e : nextEdges) {
					double range = weightIter.next() / totalWeight;
					target -= range;
					if (target < 0) { nextEdge = e;	break; }
				}
				
				Site nextSite = nextEdge.getNeighbor(currentSite);
				
				previousMove = nextSite.getPolygon().getCentroid().copy();
				previousMove.subtract(currentSite.getPolygon().getCentroid());
				previousMove.normalize();
				previousRiverCell = currentRiverCell;
				previouseEdge = nextEdge;
				currentSite = nextSite;
			}
			
			if (currentRiver.numCells() == 0) break;
			rivers.add(currentRiver);
		}
		
		// Adjust island layers to a contiguous range from 0 to numHeightLayers
		this.numHeightLayers = 2*maxLayerUsed-2*minLayerUsed+1;
		for (Island island : islands) {			
			int layer = island.getAltitudeLayer(); 
			if (layer != Island.LAYER_UNASSIGNED) {
				island.setAltitudeLayer(2*(layer-minLayerUsed));
			}
		}
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

	public List<River> getRivers() {
		return rivers;
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
