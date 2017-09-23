package com.gpergrossi.aerogen.generator.decorate.features;

import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.placement.IPlacement;
import com.gpergrossi.aerogen.generator.islands.Island;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IFeature {
	
	public IPlacement getPlacement();
	
	public IFeature withPlacement(IPlacement placement);
	
	public abstract boolean generate(World world, Island island, BlockPos position, Random rand);
	
}
