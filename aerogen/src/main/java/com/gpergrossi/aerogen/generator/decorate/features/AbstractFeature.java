package com.gpergrossi.aerogen.generator.decorate.features;

import java.util.Random;

import com.gpergrossi.aerogen.generator.decorate.placement.IPlacement;
import com.gpergrossi.aerogen.generator.islands.Island;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractFeature {
	
	protected IPlacement placement;
	
	public IPlacement getPlacement() {
		return placement;
	}
	
	public AbstractFeature withPlacement(IPlacement placement) {
		this.placement = placement;
		return this;
	}
	
	public abstract boolean generate(World world, Island island, BlockPos position, Random rand);
	
}
