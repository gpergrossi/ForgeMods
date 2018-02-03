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
import com.gpergrossi.util.constraints.integer.IntegerConstraint;
import com.gpergrossi.util.constraints.matrix.ConstraintMatrix;
import com.gpergrossi.util.geom.shapes.Convex;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.voronoi.Edge;
import com.gpergrossi.voronoi.Site;
import com.gpergrossi.voronoi.Voronoi;
import com.gpergrossi.voronoi.VoronoiBuilder;
import com.gpergrossi.voronoi.infinite.InfiniteCell;

public class Region {
	
	// Region details
	private final RegionManager manager;
	private final InfiniteCell regionCell;
	
	private Random random;
	private RegionBiome biome;

	List<Island> islands;
	List<IslandCell> islandCells;
	double averageCellRadius;
	
	ConstraintMatrix<IntegerConstraint> islandAltitudeConstraints;
	List<IRegionFeature> features;
	
	private int minIslandAltitude = 32;
	private int maxIslandAltitude = 128;
	
	public Region(RegionManager manager, InfiniteCell boundaryCell) {
		this.manager = manager;
				
		this.regionCell = boundaryCell;
		if (boundaryCell != null) {
			boundaryCell.reserve();	
			this.random = new Random(boundaryCell.getSeed());
			init();
		}
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
		this.islandCells = Collections.unmodifiableList(islandCells);
		this.islands = Collections.unmodifiableList(islands);

		initIslandAltitudeConstraints();
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
			while (success == -1) success = builder.addSiteSafe(bounds.getBounds().getRandomPoint(random), 1);
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
	
	private void initIslandAltitudeConstraints() {
		islandAltitudeConstraints = new ConstraintMatrix<>(IntegerConstraint.CLASS, islands.size()+1);
		IntegerConstraint globalHeightConstraint = IntegerConstraint.greaterOrEqual(minIslandAltitude);
		globalHeightConstraint = globalHeightConstraint.and(IntegerConstraint.lessOrEqual(maxIslandAltitude));
		for (int i = 0; i < islands.size(); i++) {
			islandAltitudeConstraints.andConstraint(i+1, globalHeightConstraint, 0);
		}
	}
	
	/**
	 * Attempts to add the given altitude constraint to this region's island altitude constraints.
	 * If the constraint is impossible to satisfy, this method will return false.
	 * @param islandA - island left of constraint
	 * @param constraint - an integer constraint on island altitude. (e.g. IntegerConstraint.LESS)
	 * @param islandB - island right of the constraint
	 * @return true if the constraint is possible, false if it creates a contradiction.<br/>
	 * 		   (e.g. adding C < A to a system: A < B, B < C)
	 */
	public boolean constrainIslandAltitudes(Island islandA, IntegerConstraint constraint, Island islandB) {
		if (islandA == null || islandB == null) throw new IllegalArgumentException("Islands must be non-null");
		if (islandA.getRegion() != this || islandB.getRegion() != this) throw new IllegalArgumentException("Islands must belong to this region!");
		
		final int indexA = islandA.regionIndex+1;
		final int indexB = islandB.regionIndex+1;
		
		return islandAltitudeConstraints.andConstraint(indexA, constraint, indexB);
	}
	
	/**
	 * Gives each island an altitude integer from 0 to numHeightLayers-1
	 */
	private void resolveIslandAltitudes() {
		final AltitudeSolver solver = new AltitudeSolver(this, random);
		islandAltitudeConstraints = solver.solve(islandAltitudeConstraints);
		for (int i = 0; i < islands.size(); i++) {
			final Island island = islands.get(i);
			final IntegerConstraint relative = islandAltitudeConstraints.getMatrixEntry(i+1, 0).getConstraint();
			final int altitude = relative.valueFor(0);
			island.setAltitude(altitude);
		}
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

	public IslandBiome getIslandBiome(Island island, Random random) {
		return biome.getRandomIslandBiome(random);
	}
	
}
