package com.gpergrossi.aerogen.generator.regions.features.river;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.features.IRegionFeature;
import com.gpergrossi.constraints.integer.IntegerConstraint;
import com.gpergrossi.util.data.WeightedList;
import com.gpergrossi.util.geom.shapes.Convex;
import com.gpergrossi.util.geom.vectors.Double2D;
import com.gpergrossi.voronoi.Edge;
import com.gpergrossi.voronoi.Site;

public class RiverFeature implements IRegionFeature {

	// Settings
	protected int numRivers = 1;
	protected double chanceToShareHeadLake = 1.0;
	protected double minWaterfallWidth = 8;	// Min Edge width to allow waterfall
	protected boolean allowIntersect = false;
	protected double forkChance = 0;
	protected double maxForkAngle = 2.0*Math.PI/3.0;
	protected int minWaterfallHeight = 16;
	
	protected Set<IslandCell> consumedCells;
	
	// Output
	protected Region region;
	protected List<River> rivers;
	
	public RiverFeature() {}
	
	public RiverFeature setRiverCount(int count) {
		this.numRivers = count;
		this.consumedCells = new HashSet<>();
		return this;
	}
	
	public List<River> getRivers() {
		return rivers;
	}
	
	public boolean consumed(IslandCell cell) {
		return consumedCells.contains(cell);
	}
	
	public void consume(IslandCell cell) {
		consumedCells.add(cell);
	}
	
	@Override
	public void create(Region region, Random random) {
		this.region = region;
		this.rivers = new ArrayList<>(numRivers);
		for (int i = 0; i < numRivers; i++) {
			rivers.add(createRiver(region, random));
		}
	}
	
	public River createRiver(Region region, Random random) {
		River river = new River();
		
		// Choose a starting cell
		IslandCell headCell = null;
		if (rivers.size() > 0 && random.nextDouble() <= chanceToShareHeadLake) {
			River previousRiver = rivers.get(rivers.size()-1);
			RiverCell previousHeadCell = previousRiver.getHead();
			headCell = previousHeadCell.islandCell;
		} else {
			headCell = region.getRandomCell(random);
		}
		Double2D startingDirection = Double2D.unit(random.nextDouble()*Math.PI*2.0);
		
		IntegerConstraint waterfallConstraint = IntegerConstraint.less(-minWaterfallHeight);
		
		RandomCellExplorer cellExplorer = new RandomCellExplorer(random, headCell, startingDirection);
		IslandCell previousCell, currentCell = null;
		Island currentIsland = null;
		
		while (cellExplorer.hasNext()) {
			previousCell = currentCell;
			Island previousIsland = currentIsland;
			
			currentCell = cellExplorer.next();
			currentIsland = currentCell.getIsland();
			Edge previousEdge = cellExplorer.previousEdge();
			
			if (currentIsland == null) {
				river.addWaterfall(previousEdge.toLineSeg(), currentCell);
				break;
			}
			
			consume(currentCell);
			
			// First cell only
			if (previousCell == null) {
				river.addCell(currentCell);
				continue;
			}

			if (currentIsland != previousIsland) {
				final boolean canConstrain = region.constrainIslandAltitudes(currentIsland, waterfallConstraint, previousIsland);
				if (canConstrain) {
					// Waterfall to new island
					river.addWaterfall(previousEdge.toLineSeg(), currentCell);
				} else {
					// Waterfall into void
					IslandCell voidCell = new IslandCell(null, currentCell.getVoronoiSite());
					river.addWaterfall(previousEdge.toLineSeg(), voidCell); 
					break;
				}
			} else {
				river.addCell(currentCell);
			}
		}
		
		return river;
	}
	
	public class RandomCellExplorer implements Iterator<IslandCell> {
		
		Random random;
		Double2D previousMove;
		Edge currentEdge;
		Edge nextEdge;
		IslandCell nextCell;
		boolean offTheEdge = false;
		
		public RandomCellExplorer(Random random, IslandCell startingCell, Double2D startingDirection) {
			this.random = random;
			this.nextCell = startingCell;
			this.previousMove = startingDirection.normalize(); 
		}
		
		@Override
		public boolean hasNext() {
			return nextCell != null;
		}
		
		@Override
		public IslandCell next() {
			IslandCell currentCell = nextCell;
			currentEdge = nextEdge;

			if (offTheEdge) {
				offTheEdge = false;
				nextCell = null;
				return currentCell;
			}
			
			nextEdge = chooseNextEdge();
			if (nextEdge != null) {
				Site nextSite = nextEdge.getNeighbor(currentCell.getVoronoiSite());
				
				if (nextSite == null) {
					offTheEdge = true;
					Convex reflected = currentCell.getPolygon().reflect(nextEdge.toLineSeg());
					nextCell = new IslandCell(null, reflected);
					return currentCell;
				}
				
				previousMove = nextSite.getPolygon().getCentroid();
				previousMove = previousMove.subtract(currentCell.getPolygon().getCentroid());
				previousMove = previousMove.normalize();
				
				nextCell = (IslandCell) nextSite.data;
				
			} else {
				nextCell = null;
			}
			
			return currentCell;
		}

		public Edge previousEdge() {
			return currentEdge;
		}
		
		private Edge chooseNextEdge() {
			WeightedList<Edge> nextEdges = new WeightedList<>();
			Site currentSite = nextCell.getVoronoiSite();
			for (Edge e : currentSite.getEdges()) {
				Site n = e.getNeighbor(currentSite);
				if (n != null && consumed((IslandCell) n.data)) continue;
				
				double edgeLength = e.toLineSeg().length();
				if (edgeLength < minWaterfallWidth) continue;

				double dotProduct = -1;
				if (n != null) {
					Double2D nVector = n.getPolygon().getCentroid();
					nVector = nVector.subtract(nextCell.getPolygon().getCentroid());
					nVector = nVector.normalize();
					dotProduct = nVector.dot(previousMove);
				} else {
					Double2D nVector = e.getCenter();
					nVector = nVector.subtract(nextCell.getPolygon().getCentroid());
					nVector = nVector.normalize();
					dotProduct = nVector.dot(previousMove);
				}
				
				double weightD = edgeLength/region.getAverageCellRadius() + (1+dotProduct);
				int weight = (int) (weightD*1000.0);
				if (weight > 0)	nextEdges.add(e, weight);
			}
			if (nextEdges.size() == 0) return null;
			return nextEdges.getRandom(random);
		}
		
	}
	
}
