package dev.mortus.aerogen.world.gen.placeables;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPlaceable {
	
	public boolean place(World world, BlockPos pos, Random rand);
	
}
