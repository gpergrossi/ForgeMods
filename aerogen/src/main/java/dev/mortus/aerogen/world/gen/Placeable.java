package dev.mortus.aerogen.world.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Placeable {

	public static final Placeable DEAD_BUSH = instance(BlockTallGrass.EnumType.DEAD_BUSH);
	public static final Placeable TALL_GRASS = instance(BlockTallGrass.EnumType.GRASS);
	public static final Placeable FERN = instance(BlockTallGrass.EnumType.FERN);
	
	public static final Placeable BROWN_MUSHROOM = instance(Blocks.BROWN_MUSHROOM);
	public static final Placeable RED_MUSHROOM = instance(Blocks.RED_MUSHROOM);

	public static final Placeable LILY_PAD = instance(Blocks.WATERLILY);
	public static final Placeable PUMPKIN = instance(Blocks.PUMPKIN);
	public static final Placeable MELON = instance(Blocks.MELON_BLOCK);
	public static final Placeable CACTUS = instance(Blocks.CACTUS);
	public static final Placeable REEDS = instance(Blocks.REEDS);

	public static final Placeable ALLIUM = instance(BlockFlower.EnumFlowerType.ALLIUM);
	public static final Placeable BLUE_ORCHID = instance(BlockFlower.EnumFlowerType.BLUE_ORCHID);
	public static final Placeable DANDELION = instance(BlockFlower.EnumFlowerType.DANDELION);
	public static final Placeable HOUSTONIA = instance(BlockFlower.EnumFlowerType.HOUSTONIA);
	public static final Placeable ORANGE_TULIP = instance(BlockFlower.EnumFlowerType.ORANGE_TULIP);
	public static final Placeable OXEYE_DAISY = instance(BlockFlower.EnumFlowerType.OXEYE_DAISY);
	public static final Placeable PINK_TULIP = instance(BlockFlower.EnumFlowerType.PINK_TULIP);
	public static final Placeable POPPY = instance(BlockFlower.EnumFlowerType.POPPY);
	public static final Placeable RED_TULIP = instance(BlockFlower.EnumFlowerType.RED_TULIP);
	public static final Placeable WHITE_TULIP = instance(BlockFlower.EnumFlowerType.WHITE_TULIP);
	
	public static final Placeable DOUBLE_FERN = instance(BlockDoublePlant.EnumPlantType.FERN);
	public static final Placeable DOUBLE_GRASS = instance(BlockDoublePlant.EnumPlantType.GRASS);
	public static final Placeable PAEONIA = instance(BlockDoublePlant.EnumPlantType.PAEONIA);
	public static final Placeable ROSE = instance(BlockDoublePlant.EnumPlantType.ROSE);
	public static final Placeable SUNFLOWER = instance(BlockDoublePlant.EnumPlantType.SUNFLOWER);
	public static final Placeable SYRINGA = instance(BlockDoublePlant.EnumPlantType.SYRINGA);
	
    public static Placeable instance(Block block) {
    	if (block instanceof BlockReed) return new Placeable.VerticalStack(block, 2, 4);
    	if (block instanceof BlockCactus) return new Placeable.VerticalStack(block, 1, 3);
    	return new Placeable.Generic(block);
    }
    
    public static Placeable instance(IBlockState blockState) {
    	return new Placeable.Generic(blockState);
    }
    
    public static Placeable instance(BlockBush bush) {
    	return new Placeable.Bush(bush);
    }
    
    public static Placeable instance(BlockTallGrass.EnumType grassType) {
    	IBlockState state = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, grassType);
        return new Placeable.Bush(state);
    }
    
    public static Placeable instance(BlockFlower.EnumFlowerType flowerType) {
    	BlockFlower flower = flowerType.getBlockType().getBlock();
        IBlockState state = flower.getDefaultState().withProperty(flower.getTypeProperty(), flowerType);
        return new Placeable.Bush(state);
    }
    
    public static Placeable instance(BlockDoublePlant.EnumPlantType plantType) {
    	return new Placeable.DoublePlant(plantType);
    }
	
	public boolean place(World world, BlockPos pos, Random rand);
	
	public static class Generic implements Placeable {
		final Block block;
		final IBlockState blockState;
		
	    public Generic(Block block) {
	    	this.block = block;
	    	this.blockState = block.getDefaultState();
	    }
	    
	    public Generic(IBlockState blockState) {
	    	this.block = blockState.getBlock();
	    	this.blockState = blockState;
	    }
		
		@Override
		public boolean place(World world, BlockPos pos, Random rand) {
        	if (block.canPlaceBlockAt(world, pos)) {            		
                world.setBlockState(pos, this.blockState, 2);
                return true;
            }
        	return false;
		}
	}
	
	public static class DoublePlant implements Placeable {
		final BlockDoublePlant.EnumPlantType plantType;
		
	    public DoublePlant(BlockDoublePlant.EnumPlantType plantType) {
	    	this.plantType = plantType;
	    }
	    
		@Override
		public boolean place(World world, BlockPos pos, Random rand) {
            if (Blocks.DOUBLE_PLANT.canPlaceBlockAt(world, pos)) {
                Blocks.DOUBLE_PLANT.placeAt(world, pos, this.plantType, 2);
                return true;
            }
            return false;
		}
	}
	
	public static class Bush implements Placeable {
		final BlockBush block;
		final IBlockState blockState;
		
	    public Bush(BlockBush bush) {
	    	this.block = bush;
	    	this.blockState = bush.getDefaultState();
	    }
	    
	    public Bush(IBlockState bush) {
	    	BlockBush block = (BlockBush) bush.getBlock();
	    	this.block = block;
	    	this.blockState = bush;
	    }
	    
		@Override
		public boolean place(World world, BlockPos pos, Random rand) {
            if (block.canBlockStay(world, pos, blockState)) {
                world.setBlockState(pos, blockState, 2);
                return true;
            }
            return false;
		}
	}
	
	public static class VerticalStack implements Placeable {
		final Block block;
		final IBlockState blockState;
		final int minStack = 1;
		final int maxStack = 1;
		
	    public VerticalStack(Block block, int minStack, int maxStack) {
	    	this.block = block;
	    	this.blockState = block.getDefaultState();
	    }
	    
	    public VerticalStack(IBlockState blockState, int minStack, int maxStack) {
	    	this.block = blockState.getBlock();
	    	this.blockState = blockState;
	    }
		
		@Override
		public boolean place(World world, BlockPos pos, Random rand) {
			boolean anyPlaced = false;
            int height = minStack + rand.nextInt(rand.nextInt(maxStack - minStack + 1) + 1);
            for (int h = 0; h < height; h++) {
                if (!Blocks.CACTUS.canBlockStay(world, pos)) break;
                world.setBlockState(pos.up(h), Blocks.CACTUS.getDefaultState(), 2);
                anyPlaced = true;
            }
            return anyPlaced;
		}
	}

	public static class RandomRotationHorizontal implements Placeable {
		final BlockHorizontal block;
		
	    public RandomRotationHorizontal(BlockHorizontal block) {
	    	this.block = block;
	    }
		
		@Override
		public boolean place(World world, BlockPos pos, Random rand) {
        	if (block.canPlaceBlockAt(world, pos)) {
        		IBlockState blockState = this.block.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.Plane.HORIZONTAL.random(rand));
                world.setBlockState(pos, blockState, 2);
                return true;
        	}
        	return false;
		}
	}
	
}
