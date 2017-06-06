package dev.mortus.aerogen.world.gen;

import java.util.Random;

import dev.mortus.aerogen.world.islands.Island;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class Feature {
	
	protected Placement placement;
	
	public Placement getPlacement() {
		return placement;
	}
	
	public Feature withPlacement(Placement placement) {
		this.placement = placement;
		return this;
	}
	
	public abstract boolean generate(World world, Island island, BlockPos position, Random rand);
	
}
