package dev.mortus.aerogen.world.gen.features;

import java.util.Random;

import dev.mortus.aerogen.world.gen.placeables.IPlaceable;
import dev.mortus.aerogen.world.islands.Island;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FeatureSingle extends AbstractFeature {

	IPlaceable feature;
	
	public FeatureSingle(IPlaceable placeable) {
		this.feature = placeable;
	}
	
	@Override
	public boolean generate(World world, Island island, BlockPos position, Random rand) {
		return feature.place(world, position, rand);
	}

}
