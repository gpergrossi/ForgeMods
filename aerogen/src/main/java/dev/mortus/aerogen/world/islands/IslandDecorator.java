package dev.mortus.aerogen.world.islands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.world.gen.FeaturePlacement;
import dev.mortus.aerogen.world.gen.IslandFeature;
import dev.mortus.util.data.Int2D;
import dev.mortus.util.data.Int2DRange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IslandDecorator {
	
//	public WorldGenerator underwaterClayGen = new WorldGenClay(4);
//	public WorldGenerator underwaterSandGen = new WorldGenSand(Blocks.SAND, 7);
//	public WorldGenerator underwaterGravelGen = new WorldGenSand(Blocks.GRAVEL, 6);
//	
//	public WorldGenFlowers yellowFlowerGen = new WorldGenFlowers(Blocks.YELLOW_FLOWER, BlockFlower.EnumFlowerType.DANDELION);
//	
//	/** Field that holds mushroomBrown WorldGenFlowers */
//	public WorldGenerator mushroomBrownGen = new WorldGenBush(Blocks.BROWN_MUSHROOM);
//	
//	/** Field that holds mushroomRed WorldGenFlowers */
//	public WorldGenerator mushroomRedGen = new WorldGenBush(Blocks.RED_MUSHROOM);
//	
//	/** Field that holds big mushroom generator */
//	public WorldGenerator bigMushroomGen = new WorldGenBigMushroom();
//	
//	/** Field that holds WorldGenReed */
//	public WorldGenerator reedGen = new WorldGenReed();
//	
//	/** Field that holds WorldGenCactus */
//	public WorldGenerator cactusGen = new WorldGenCactus();
//	
//	/** The water lily generation! */
//	public WorldGenerator waterlilyGen = new WorldGenWaterlily();
	
	List<IslandFeature> features;
	
	public IslandDecorator() {
		this.features = new ArrayList<>();
	}

	public IslandDecorator addFeature(IslandFeature feature) {
		this.features.add(feature);
		return this;
	}
	
	public void decorate(World world, Island island, Int2DRange chunkRange, Int2DRange overlapRange, Random random) {
		BlockPos chunkPos = new BlockPos(chunkRange.minX, 0, chunkRange.minY);

		//System.out.println("Decorating with "+features.size()+" fatures");
		
		for (IslandFeature feature : features) {
			FeaturePlacement placement = feature.getPlacement();
			if (placement == null) continue;

			int numAttempts = placement.getNumAttemptsPerChunk(random);
			int maxPlacements = placement.getMaxPlacementsPerChunk(random); 
			
			Int2D pos = new Int2D();			
			int placements = 0;
			for (int n = 0; n < numAttempts; n++) {
				pos.x = chunkPos.getX() + random.nextInt(16);
				pos.y = chunkPos.getZ() + random.nextInt(16);
				
				int minY = placement.getMinY(world, island, pos);
				int maxY = placement.getMaxY(world, island, pos);
				if (minY > maxY) continue;
				
				int height = random.nextInt(maxY - minY + 1) + minY;
				BlockPos blockPos = new BlockPos(pos.x, height, pos.y);
				if (!placement.canGenerate(world, island, blockPos, random)) continue;
				
				boolean success = feature.generate(world, island, new BlockPos(pos.x, height, pos.y), random);
				if (success) {
					placements++;
					System.out.println("Successfully placed ("+placements+"/"+maxPlacements+")");
					if (placements >= maxPlacements) break;
				}
			}
		}
	}
	
//
//		// Underwater Sand
//		for (int i = 0; i < settings.sandPerChunk; i++) {
//			int j = random.nextInt(16);
//			int k = random.nextInt(16);
//			this.underwaterSandGen.generate(world, random, world.getTopSolidOrLiquidBlock(chunkPos.add(j, 0, k)));
//		}
//
//		// Underwater Clay
//		for (int i = 0; i < settings.clayPerChunk; i++) {
//			int j = random.nextInt(16);
//			int k = random.nextInt(16);
//			this.underwaterClayGen.generate(world, random, world.getTopSolidOrLiquidBlock(chunkPos.add(j, 0, k)));
//		}
//
//		// Underwater Gravel
//		for (int i = 0; i < settings.gravelPerChunk; i++) {
//			int j = random.nextInt(16);
//			int k = random.nextInt(16);
//			this.underwaterGravelGen.generate(world, random, world.getTopSolidOrLiquidBlock(chunkPos.add(j, 0, k)));
//		}
//
//		// Trees
//		int numTrees = settings.treesPerChunk;
//		if (random.nextFloat() < settings.extraTreeChance) numTrees++;
//		for (int i = 0; i < numTrees; i++) {
//			int j = random.nextInt(16);
//			int k = random.nextInt(16);
//			WorldGenAbstractTree worldgenabstracttree = island.getBiome().getRandomTreeGen(random);
//			worldgenabstracttree.setDecorationDefaults();
//			BlockPos blockpos = world.getHeight(chunkPos.add(j, 0, k));
//
//			if (worldgenabstracttree.generate(world, random, blockpos)) {
//				worldgenabstracttree.generateSaplings(world, random, blockpos);
//			}
//		}
//
//		// Big mushroom
//		for (int i = 0; i < settings.bigMushroomsPerChunk; i++) {
//			int j = random.nextInt(16);
//			int k = random.nextInt(16);
//			this.bigMushroomGen.generate(world, random, world.getHeight(chunkPos.add(j, 0, k)));
//		}
//
//		// Flowers
//		for (int n = 0; n < settings.flowersPerChunk; n++) {
//			int i = random.nextInt(16);
//			int k = random.nextInt(16);
//			int j = world.getHeight(chunkPos.add(i, 0, k)).getY();
//
//			if (j > 0) {
//				int randomY = random.nextInt(j+32);
//				BlockPos blockpos1 = chunkPos.add(i, randomY, k);
//				BlockFlower.EnumFlowerType flowerType = island.getBiome().pickRandomFlower(random, blockpos1);
//				BlockFlower blockflower = flowerType.getBlockType().getBlock();
//
//				if (blockflower.getDefaultState().getMaterial() != Material.AIR) {
//					this.yellowFlowerGen.setGeneratedBlock(blockflower, flowerType);
//					this.yellowFlowerGen.generate(world, random, blockpos1);
//				}
//			}
//		}
//
//		// Tall grass
//		for (int n = 0; n < settings.grassPerChunk; n++) {
//			int i = random.nextInt(16);
//			int k = random.nextInt(16);
//			int j = world.getHeight(chunkPos.add(i, 0, k)).getY();
//
//			if (j > 0) {
//				int randomY = random.nextInt(j*2);
//				island.getBiome().getRandomWorldGenForGrass(random).generate(world, random, chunkPos.add(i, randomY, k));
//			}
//		}
//		
//		// Dead bushes
//		for (int n = 0; n < settings.deadBushPerChunk; n++) {
//			int i = random.nextInt(16);
//			int k = random.nextInt(16);
//			int j = world.getHeight(chunkPos.add(i, 0, k)).getY();
//
//			if (j > 0) {
//				int randomY = random.nextInt(j*2);
//				(new WorldGenDeadBush()).generate(world, random, chunkPos.add(i, randomY, k));
//			}
//		}
//
//		// Waterlilies
//		for (int n = 0; n < settings.waterlilyPerChunk; n++) {
//			int i = random.nextInt(16);
//			int k = random.nextInt(16);
//			int j = world.getHeight(chunkPos.add(i, 0, k)).getY();
//
//			if (j > 0) {
//				int randomY = random.nextInt(j*2);
//				BlockPos blockpos4;
//				BlockPos blockpos7;
//
//				// TODO: start at a random height and work downward... stupid algorithm, make it better
//				for (blockpos4 = chunkPos.add(i, randomY, k); blockpos4.getY() > 0; blockpos4 = blockpos7) {
//					blockpos7 = blockpos4.down();
//
//					if (!world.isAirBlock(blockpos7)) {
//						break;
//					}
//				}
//
//				this.waterlilyGen.generate(world, random, blockpos4);
//			}
//		}
//
//		// Small mushrooms
//		for (int n = 0; n < settings.mushroomsPerChunk; n++) {
//			if (random.nextInt(8) == 0) {
//				int i = random.nextInt(16);
//				int k = random.nextInt(16);
//				int j = world.getHeight(chunkPos.add(i, 0, k)).getY();
//				
//				if (j > 0) {
//					int randomY = random.nextInt(j*2);
//					BlockPos pos = chunkPos.add(i, randomY, k);
//					this.mushroomBrownGen.generate(world, random, pos);
//				}
//			}
//			if (random.nextInt(8) == 0) {
//				int i = random.nextInt(16);
//				int k = random.nextInt(16);
//				int j = world.getHeight(chunkPos.add(i, 0, k)).getY();
//				
//				if (j > 0) {
//					int randomY = random.nextInt(j*2);
//					BlockPos pos = chunkPos.add(i, randomY, k);
//					this.mushroomRedGen.generate(world, random, pos);
//				}
//			}
//		}
//
//		// Reeds
//		for (int n = 0; n < settings.reedsPerChunk; n++) {
//			int i = random.nextInt(16);
//			int k = random.nextInt(16);
//			int j = world.getHeight(chunkPos.add(i, 0, k)).getY();
//			if (j > 0) {
//				int randomY = random.nextInt(j*2);
//				this.reedGen.generate(world, random, chunkPos.add(i, randomY, k));
//			}
//		}
//		
//		// Pumpkins
//		if (random.nextInt(32) == 0) {
//			int i = random.nextInt(16);
//			int k = random.nextInt(16);
//			int j = world.getHeight(chunkPos.add(i, 0, k)).getY();
//			
//			if (j > 0) {
//				int randomY = random.nextInt(j*2);
//				(new WorldGenPumpkin()).generate(world, random, chunkPos.add(i, randomY, k));
//			}
//		}
//
//		// Cacti
//		for (int n = 0; n < settings.cactiPerChunk; n++) {
//			int i = random.nextInt(16);
//			int k = random.nextInt(16);
//			int j = world.getHeight(chunkPos.add(i, 0, k)).getY();
//			
//			if (j > 0) {
//				int randomY = random.nextInt(j*2);
//				this.cactusGen.generate(world, random, chunkPos.add(i, randomY, k));
//			}
//		}
//
//		// Water ponds
//		if (settings.useWaterLakes) {
//			for (int n = 0; n < 50; ++n) {
//				int i = random.nextInt(16);
//				int j = random.nextInt(random.nextInt(248) + 8);
//				int k = random.nextInt(16);
//				BlockPos pos = chunkPos.add(i, j, k);
//				(new WorldGenLiquids(Blocks.FLOWING_WATER)).generate(world, random, pos);
//			}
//		}
//
//		// Lava pools
//		if (settings.useLavaLakes) {
//			for (int n = 0; n < 20; ++n) {
//				int i = random.nextInt(16);
//				int k = random.nextInt(16);
//				int j = random.nextInt(random.nextInt(random.nextInt(240) + 8) + 8);
//				BlockPos pos = chunkPos.add(i, j, k);
//				(new WorldGenLiquids(Blocks.FLOWING_LAVA)).generate(world, random, pos);
//			}
//		}
//	}
	
}
