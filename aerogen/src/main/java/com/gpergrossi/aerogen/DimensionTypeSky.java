package com.gpergrossi.aerogen;

import java.util.OptionalLong;
import java.util.Random;

import com.gpergrossi.aerogen.gui.GuiAerogenWorldSettingsScreen;

import net.minecraft.tags.BlockTags;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.ColumnFuzzedBiomeMagnifier;
import net.minecraft.world.biome.IBiomeMagnifier;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DimensionTypeSky extends DimensionType {

    public static final ResourceLocation AEROGEN_SKY_EFFECTS = new ResourceLocation("overworld");
    public static final RegistryKey<DimensionType> AEROGEN_SKY_LOCATION = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("aerogen_sky"));
 
	public DimensionTypeSky() {
		super(
           OptionalLong.empty(),                            // = fixedTime
           true,                                            // = hasSkylight
           false,                                           // = hasCeiling
           false,                                           // = ultraWarm
           true,                                            // = natural
           1.0D,                                            // = coordinateScale
           false,                                           // = createDragonFight
           false,                                           // = piglinSafe
           true,                                            // = bedWorks
           false,                                           // = respawnAnchorWorks
           true,                                            // = hasRaids
           256,                                             // = logicalHeight
           ColumnFuzzedBiomeMagnifier.INSTANCE,             // = biomeZoomer
           BlockTags.INFINIBURN_OVERWORLD.getName(),        // = infiniburn
           AEROGEN_SKY_EFFECTS,                             // = effectsLocation
           0.0F                                             // = ambientLight; brightnessRamp = fillBrightnessRamp(ambientLight);
        );
	}

	
    /**
     * Gets the translation key for the name of this world type.
     */
    @Override
	@SideOnly(Side.CLIENT)
    public String getTranslationKey() {
    	// TODO change this back to generator.AEROGEN_SKY and add
    	// the translation to an actual localization file somewhere
    	return "Aerogen Sky";
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
    	String options = world.getWorldInfo().getGeneratorOptions();
    	if (!options.equals(generatorOptions)) {
    		AeroGenMod.log.error("!!! World info generator options do not match generator options parameter !!!");
    	}
        return new ChunkGeneratorSky(world);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
    	return new BiomeProviderSky(world);
    }
    
    @Override
    public int getMinimumSpawnHeight(World world) {
        return 5;
    }

    @Override
    public double getHorizon(World world) {
        return 0;
    }

    @Override
    public double voidFadeMagnitude() {
    	// How quickly above y=0 that the black atmosphere fades out
        return 5.0;
    }
    
    @Override
    public float getCloudHeight() {
    	// Height to render the clouds for this world type
        return 0.0F;
    }
    
    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world) {
    	// Seems to return true when a slime should not be spawned or maybe when they should despawn
        return false; //random.nextInt(4) != 1 : 
    }
    
}
