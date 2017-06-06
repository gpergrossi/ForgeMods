package dev.mortus.aerogen.world.gen;

import java.util.Random;

import dev.mortus.aerogen.world.islands.Island;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class IslandFeature {
	
	protected FeaturePlacement placement;
	
	public FeaturePlacement getPlacement() {
		return placement;
	}
	
	public IslandFeature withPlacement(FeaturePlacement placement) {
		this.placement = placement;
		return this;
	}
	
	public abstract boolean generate(World world, Island island, BlockPos position, Random rand);
	
}
