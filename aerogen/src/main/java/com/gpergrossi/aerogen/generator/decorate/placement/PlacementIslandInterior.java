package com.gpergrossi.aerogen.generator.decorate.placement;

import java.util.Random;

import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.world.World;

public class PlacementIslandInterior extends AbstractPlacement {
	
	int minDepth = 0;
	int maxDepth = 256;
	
	boolean scaleWithChunkMass = false;
	double averageChunkMass = 16*16*32;
	
	@Override
	public PlacementIslandInterior withDesiredCount(int num) {
		super.withDesiredCount(num);
		return this;
	}
	
	@Override
	public PlacementIslandInterior withChanceForExtra(float chance) {
		super.withChanceForExtra(chance);
		return this;
	}
	
	public PlacementIslandInterior withMinDepth(int depth) {
		this.minDepth = depth;
		return this;
	}
	
	public PlacementIslandInterior withMaxDepth(int depth) {
		this.maxDepth = depth;
		return this;
	}
	
	public PlacementIslandInterior scaleWithChunkMass(boolean shouldScale) {
		this.scaleWithChunkMass = shouldScale;
		return this;
	}
	
	public PlacementIslandInterior expectedChunkMass(double expectedChunkMass) {
		this.averageChunkMass = expectedChunkMass;
		return this;
	}

	@Override
	public int getMinY(World world, Island island, Int2D position) {
		int top = island.getHeightmap().getTop(position);
		int bottom = island.getHeightmap().getBottom(position);
		return Math.max(bottom, top - maxDepth);
	}

	@Override
	public int getMaxY(World world, Island island, Int2D position) {
		int top = island.getHeightmap().getTop(position);
		return top - minDepth;
	}
	
	@Override
	public int getMaxPlacementsPerChunk(double chunkMass, Random random) {
		double normalPlacements = this.desiredPlacementsPerChunk + this.chanceForExtraPlacement;
		double scaledPlacements = normalPlacements * chunkMass / averageChunkMass;
				
		double chanceForOneMore = scaledPlacements - Math.floor(scaledPlacements);
		scaledPlacements = Math.floor(scaledPlacements);
		
		if (chanceForOneMore > 0 && random.nextFloat() < chanceForOneMore) scaledPlacements++;
		return (int) scaledPlacements;
	}
	
}
