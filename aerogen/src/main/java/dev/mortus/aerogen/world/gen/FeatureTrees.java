package dev.mortus.aerogen.world.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import dev.mortus.aerogen.world.islands.Island;

public class FeatureTrees extends Feature {

	List<WorldGenAbstractTree> treeTypes;
	List<Float> treeWeights;
	float totalWeights = 0f;
	
	public FeatureTrees() {
		this.treeTypes = new ArrayList<>();
		this.treeWeights = new ArrayList<>();
	}
	
	public FeatureTrees addTreeType(float weight, WorldGenAbstractTree treeType) {
		this.treeTypes.add(treeType);
		this.treeWeights.add(weight);
		this.totalWeights += weight;
		return this;
	}
	
	protected WorldGenAbstractTree getRandomTree(Random rand) {
		if (totalWeights == 0f) return null;
		float roll = rand.nextFloat()*totalWeights;
		for (int i = 0; i < treeTypes.size(); i++) {
			roll -= treeWeights.get(i);
			if (roll <= 0) return treeTypes.get(i);
		}
		return null;
	}
	
	@Override
	public boolean generate(World world, Island island, BlockPos position, Random rand) {
		WorldGenAbstractTree treeGen = getRandomTree(rand);
		if (treeGen == null) return false;
		treeGen.setDecorationDefaults();
		if (treeGen.generate(world, rand, position)) {
			treeGen.generateSaplings(world, rand, position);
			return true;
		}
		return false;
	}
	
}
