package com.gpergrossi.aerogen;

import java.util.List;

import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

public class AeroGenGeneratorType extends GeneratorType
{
    public static final AeroGenGeneratorType AEROGEN_SKY = new AeroGenGeneratorType("aerogen_sky");

    /**
     * This method should add the AeroGen Sky GeneratorType to the Minecraft generator types list.
     * In version 1.16 this means inserting the new generator type into a static list of VALUES in the GeneratorType class.
     * In future versions of Minecraft this may change to a registry.
     */
    public static void AddToRegistry(List<GeneratorType> values)
    {
        values.add(AEROGEN_SKY);
    }
    
    AeroGenGeneratorType(String identifier)
    {
        super(identifier);
    }

    @Override
    protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed)
    {
        return AeroGenChunkGenerator.createAerogenGenerator(biomeRegistry, chunkGeneratorSettingsRegistry, seed);
    }

}
