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
import com.gpergrossi.util.geom.vectors.Double2D;
import com.gpergrossi.voronoi.Edge;
import com.gpergrossi.voronoi.Site;

public class RiverFeature implements IRegionFeature {

	// Settings
	protected int numRivers = 1;
	protected double chanceToShareHeadLake = 1.0;
	protected double minWaterfallWidth = 8;
	protected boolean allowIntersect = false;
	protected double forkChance = 0;
	protected double maxForkAngle = 2.0*Math.PI/3.0;
	
	// Output
	protected Region region;
	protected List<River> rivers;
	
	public RiverFeature() {}
	
	public RiverFeature setRiverCount(int count) {
		this.numRivers = count;
		return this;
	}
	
	public List<River> getRivers() {
		return rivers;
	}
	
	public void create(Region region, Random random) {
		this.region = region;
		this.rivers = new ArrayList<>(numRivers);
		for (int i = 0; i < numRivers; i++) {
			createRiver(region, random);
		}
	}
	
	public void createRiver(Region region, Random random) {
		River currentRiver = new River();
		
		while (currentCell != null) {

			Island currentIsland = currentCell.getIsland();
			RiverCell currentRiverCell = null;

			boolean endOfRiver = false;
			boolean newIsland = false;
			boolean createWaterfall = false;
			
			if (previousCell == null) newIsland = true;
			else if (currentIsland != previousCell.getIsland()) {
				int islandHeight = currentIsland.getAltitudeLayer(); 
				if (riverHeight <= 0) endOfRiver = true;
				else if (islandHeight == Island.LAYER_UNASSIGNED) riverHeight = riverHeight - 1;
				else if (islandHeight < riverHeight) riverHeight = islandHeight;
				else endOfRiver = true;
				newIsland = true;
			}

			if (endOfRiver) {
				currentRiver.addWaterfall(previouseEdge.toLineSeg(), new IslandCell(null, currentCell.getVoronoiSite())); 
				break;
			}
			
			if (newIsland) {
				currentIsland.setAltitudeLayer(riverHeight);
				minLayerUsed = Math.min(minLayerUsed, riverHeight);
				maxLayerUsed = Math.max(maxLayerUsed, riverHeight);
				if (previousCell != null) createWaterfall = true;
			}
			
			if (createWaterfall) {
				currentRiverCell = currentRiver.addWaterfall(previouseEdge.toLineSeg(), currentCell);
			} else {
				currentRiverCell = currentRiver.addCell(currentCell);
			}
			
			nextWeights.clear();
			nextEdges.clear();
			double totalWeight = 0;
			Site currentSite = currentCell.getVoronoiSite();
			for (Edge e : currentSite.getEdges()) {
				Site n = e.getNeighbor(currentSite);
				if (n == null || consumed.contains(n.data)) continue;
				
				double edgeLength = e.toLineSeg().length();
				if (edgeLength < minWaterfallWidth) continue;
				
				Double2D nVector = n.getPolygon().getCentroid();
				nVector = nVector.subtract(currentCell.getPolygon().getCentroid());
				nVector = nVector.normalize();
				double dotProduct = nVector.dot(previousMove);
				
				double weight = Math.pow(edgeLength, 4) + dotProduct * region.getAverageCellRadius() * 10;
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
			
			previousMove = nextSite.getPolygon().getCentroid();
			previousMove = previousMove.subtract(currentCell.getPolygon().getCentroid());
			previousMove = previousMove.normalize();
			
			previousCell = currentRiverCell;
			previouseEdge = nextEdge;
			currentCell = (IslandCell) nextSite.data;
		}
		
		if (currentRiver.numCells() == 0) break;
		rivers.add(currentRiver);
	}
	
	public class CellIterator implements Iterator<IslandCell> {

		Random random;
		IslandCell currentCell;
		RiverCell previousCell;
		Double2D previousMove;
		Edge previouseEdge;
		List<Edge> nextEdges;
		List<Double> nextWeights;
		
		public CellIterator(Random random) {
			this.random = random;
			
			// Choose a starting cell
			IslandCell riverHeadCell;
			if (rivers.size() > 0 && random.nextDouble() <= chanceToShareHeadLake) {
				River previousRiver = rivers.get(rivers.size()-1);
				RiverCell previousHeadCell = previousRiver.getHead();
				riverHeadCell = previousHeadCell.islandCell;
			} else {
				riverHeadCell = region.getRandomCell(random);
			}
			
			// Choose a starting direction
			Double2D previousMove = Double2D.unit(random.nextDouble()*Math.PI*2.0);
			

		}
		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public IslandCell next() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}
