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

	protected List<River> rivers;
	
	protected int numRivers = 1;
	protected double chanceToShareHeadLake = 1.0;
	
	protected double minWaterfallWidth = 8;
	
	protected boolean allowIntersect = false;
	protected double forkChance = 0;
	protected double maxForkAngle = 2.0*Math.PI/3.0;
	
	public RiverFeature() {}
	
	public List<River> getRivers() {
		return rivers;
	}
	
	public void create(Region region, Random random) {
		this.rivers = new ArrayList<>();
		
		List<Edge> nextEdges = new ArrayList<>();
		List<Double> nextWeights = new ArrayList<>();
		Set<IslandCell> consumed = new HashSet<>();
		
		IslandCell riverHeadCell = region.getCells().get(random.nextInt(region.getCells().size()));
		Island riverHeadIsland = riverHeadCell.getIsland();
			
//		int maxLayersUsable = 4;
//		int minLayerUsed = Integer.MAX_VALUE;
//		int maxLayerUsed = Integer.MIN_VALUE;
		
		for (int i = 0; i < numRivers; i++) {
			River currentRiver = new River();
			
			double a = random.nextDouble()*Math.PI*2.0;
			Double2D previousMove = new Double2D(Math.cos(a), Math.sin(a));
			IslandCell currentCell = riverHeadCell;
			RiverCell previousRiverCell = null;
			Edge previouseEdge = null;
			
//			int riverHeight = maxLayersUsable-1;
			
			while (true) {
				consumed.add(currentCell);

				boolean endOfRiver = false;
				boolean newIsland = false;
				boolean createWaterfall = false;
				Island currentIsland = null;
				RiverCell currentRiverCell = null;
				
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
					currentRiver.addWaterfall(previouseEdge.toLineSeg(), new IslandCell(null, currentCell.getVoronoiSite()));
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
				
				previousRiverCell = currentRiverCell;
				previouseEdge = nextEdge;
				currentCell = (IslandCell) nextSite.data;
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
	
}
