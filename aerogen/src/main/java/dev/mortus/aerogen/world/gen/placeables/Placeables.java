package dev.mortus.aerogen.world.gen.placeables;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class Placeables {

	public static final IPlaceable DEAD_BUSH = Placeables.create(Blocks.DEADBUSH);
	public static final IPlaceable DEAD_BUSH_AS_GRASS = Placeables.create(BlockTallGrass.EnumType.DEAD_BUSH);
	public static final IPlaceable TALL_GRASS = Placeables.create(BlockTallGrass.EnumType.GRASS);
	public static final IPlaceable FERN = Placeables.create(BlockTallGrass.EnumType.FERN);
	
	public static final IPlaceable BROWN_MUSHROOM = Placeables.create(Blocks.BROWN_MUSHROOM);
	public static final IPlaceable RED_MUSHROOM = Placeables.create(Blocks.RED_MUSHROOM);

	public static final IPlaceable LILY_PAD = Placeables.create(Blocks.WATERLILY);
	public static final IPlaceable PUMPKIN = Placeables.create((BlockHorizontal) Blocks.PUMPKIN);
	public static final IPlaceable MELON = Placeables.create(Blocks.MELON_BLOCK);
	public static final IPlaceable CACTUS = Placeables.create(Blocks.CACTUS, 2, 4);
	public static final IPlaceable REEDS = Placeables.create(Blocks.REEDS, 1, 3);

	public static final IPlaceable ALLIUM = Placeables.create(BlockFlower.EnumFlowerType.ALLIUM);
	public static final IPlaceable BLUE_ORCHID = Placeables.create(BlockFlower.EnumFlowerType.BLUE_ORCHID);
	public static final IPlaceable DANDELION = Placeables.create(BlockFlower.EnumFlowerType.DANDELION);
	public static final IPlaceable HOUSTONIA = Placeables.create(BlockFlower.EnumFlowerType.HOUSTONIA);
	public static final IPlaceable ORANGE_TULIP = Placeables.create(BlockFlower.EnumFlowerType.ORANGE_TULIP);
	public static final IPlaceable OXEYE_DAISY = Placeables.create(BlockFlower.EnumFlowerType.OXEYE_DAISY);
	public static final IPlaceable PINK_TULIP = Placeables.create(BlockFlower.EnumFlowerType.PINK_TULIP);
	public static final IPlaceable POPPY = Placeables.create(BlockFlower.EnumFlowerType.POPPY);
	public static final IPlaceable RED_TULIP = Placeables.create(BlockFlower.EnumFlowerType.RED_TULIP);
	public static final IPlaceable WHITE_TULIP = Placeables.create(BlockFlower.EnumFlowerType.WHITE_TULIP);
	
	public static final IPlaceable DOUBLE_FERN = Placeables.create(BlockDoublePlant.EnumPlantType.FERN);
	public static final IPlaceable DOUBLE_GRASS = Placeables.create(BlockDoublePlant.EnumPlantType.GRASS);
	public static final IPlaceable PAEONIA = Placeables.create(BlockDoublePlant.EnumPlantType.PAEONIA);
	public static final IPlaceable ROSE = Placeables.create(BlockDoublePlant.EnumPlantType.ROSE);
	public static final IPlaceable SUNFLOWER = Placeables.create(BlockDoublePlant.EnumPlantType.SUNFLOWER);
	public static final IPlaceable SYRINGA = Placeables.create(BlockDoublePlant.EnumPlantType.SYRINGA);
	
	public static final IPlaceable VINES = new PlaceableVine(PlaceableVine.Type.FROM_AIR, 3, 7);
	public static final IPlaceable VINES_AROUND_BLOCK = new PlaceableVine(PlaceableVine.Type.AROUND_BLOCK, 3, 7);
	
	
	public static IPlaceable create(Block block, int minStack, int maxStack) {
		return new PlaceableStackedBlock(block, minStack, maxStack);
	}
	
	public static IPlaceable create(BlockHorizontal block) {
    	return new PlaceableBlockRandomFacing(block);
    }
	
    public static IPlaceable create(Block block) {
    	return new PlaceableBlock(block);
    }
    
    public static IPlaceable create(IBlockState blockState) {
    	return new PlaceableBlock(blockState);
    }
    
    public static IPlaceable create(BlockBush bush) {
    	return new PlaceableBush(bush);
    }
    
    public static IPlaceable create(BlockTallGrass.EnumType grassType) {
    	IBlockState state = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, grassType);
        return new PlaceableBush(state);
    }
    
    public static IPlaceable create(BlockFlower.EnumFlowerType flowerType) {
    	BlockFlower flower = flowerType.getBlockType().getBlock();
        IBlockState state = flower.getDefaultState().withProperty(flower.getTypeProperty(), flowerType);
        return new PlaceableBush(state);
    }
    
    public static IPlaceable create(BlockDoublePlant.EnumPlantType plantType) {
    	return new PlaceableDoublePlant(plantType);
    }
	
}
