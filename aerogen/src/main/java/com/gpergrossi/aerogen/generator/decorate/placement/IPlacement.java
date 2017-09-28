package com.gpergrossi.aerogen.generator.decorate.placement;

import java.util.Random;

import com.gpergrossi.aerogen.generator.GenerationPhase;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.util.geom.vectors.Int2D;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPlacement {
	
	public GenerationPhase getPhase();
	
	public int getMaxPlacementsPerChunk(double chunkMass, Random random);
	
	public int getMinY(World world, Island island, Int2D position);
	public int getMaxY(World world, Island island, Int2D position);
		
	public boolean canGenerate(World world, Island island, BlockPos position, Random random);
	
}
