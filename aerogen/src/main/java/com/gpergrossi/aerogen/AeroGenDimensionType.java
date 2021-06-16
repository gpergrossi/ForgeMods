package com.gpergrossi.aerogen;

import java.util.OptionalLong;

import com.mojang.serialization.Lifecycle;

import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.source.HorizontalVoronoiBiomeAccessType;
import net.minecraft.world.dimension.DimensionType;

public class AeroGenDimensionType
{
    public static final Identifier AEROGEN_SKY_ID;
    public static final RegistryKey<DimensionType> AEROGEN_SKY_REGISTRY_KEY;
    public static final DimensionType AEROGEN_SKY;
    
    static
    {
        AEROGEN_SKY_ID = new Identifier("aerogen_sky");
        AEROGEN_SKY_REGISTRY_KEY = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier("aerogen_sky"));
        AEROGEN_SKY = DimensionType.create(OptionalLong.empty(), true, false, false, true, 1.0D, false, false, true, false, true, 0, 256, 256, HorizontalVoronoiBiomeAccessType.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getId(), AEROGEN_SKY_ID, 0.0F);
    }

    public static void AddToRegistry()
    {        
        DynamicRegistryManager drm = DynamicRegistryManager.create();
        MutableRegistry<DimensionType> dimensionRegistry = drm.getMutable(Registry.DIMENSION_TYPE_KEY);
        dimensionRegistry.add(AEROGEN_SKY_REGISTRY_KEY, AEROGEN_SKY, Lifecycle.stable());
    }
}
